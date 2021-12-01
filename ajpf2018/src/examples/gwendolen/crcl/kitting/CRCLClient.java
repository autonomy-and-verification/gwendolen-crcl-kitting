/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting;

/* 
 * Compile with :
 * javac -cp ../../crcl4java-utils/target/crcl4java-utils-1.0-SNAPSHOT-jar-with-dependencies.jar CRCLClient.java
 * 
 * Run with:
 * java -cp ../../crcl4java-utils/target/crcl4java-utils-1.0-SNAPSHOT-jar-with-dependencies.jar:.  CRCLClient
 * 
 */
import crcl.base.*;
//import crcl.base.CRCLStatusType;
//import crcl.base.ModelsStatusType;
//import crcl.base.JointStatusType;
import crcl.base.*;
import static crcl.utils.CRCLPosemath.point;
import static crcl.utils.CRCLPosemath.pose;
import static crcl.utils.CRCLPosemath.vector;
import static crcl.utils.CRCLPosemath.toPose;
import crcl.utils.CRCLSocket;
import crcl.utils.CRCLPosemath;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.math3.util.Pair;

// Pose math
import rcs.posemath.*;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmEulerZyx;
import rcs.posemath.PmException;
import rcs.posemath.PmHomogeneous;
import rcs.posemath.PmPose;
import rcs.posemath.PmRotationMatrix;
import rcs.posemath.PmRotationVector;
import rcs.posemath.PmRpy;
import rcs.posemath.Posemath;
import rcs.posemath.PmQuaternion;

import static gwendolen.crcl.kitting.CShapes.definitions;
import static gwendolen.crcl.kitting.CShapes.findDefinition;
import static gwendolen.crcl.kitting.CShapes.instances;
import static gwendolen.crcl.kitting.CShapes.snapshotInstances;
import static gwendolen.crcl.kitting.KittingInterface.fakeSetup;
import static gwendolen.crcl.kitting.KittingInterface.dumpInstances;
import static gwendolen.crcl.kitting.KittingInterface.fakeFirstOrderLogic;
import static gwendolen.crcl.kitting.KittingInterface.dumpInferences;
import static gwendolen.crcl.kitting.KittingInterface.dumpKittingSetup;

import java.util.Vector;
import java.util.logging.*;

/**
 * CRCL client written in java. Globals.bLoopback is a flag that fakes a crcl
 * connection by accpeting crcl commands and simulating the actions that the
 * robot would take. This means defining the scene and gear;/tray properties
 * that is size, location, type, and for trays all the slots within the tray.
 * Inferences are performed locally to determine to understand all the
 * underlying slot states of the trays, either open or contains a gear. At the
 * beginning, the supply vessels ctonain all gears of one size, and the kits
 * have open slots of varying gear size. Self-contained inferences determines
 * the state of all the slots and gear locations, and is used by the kitting
 * demo state logic to determine a sequence of operations to grasp a free gear
 * that matches the kit open slot sizes. To achieve updates of gear locations,
 * the gripper close and open operations are monitored. When a gripper is
 * closed, the last moveto pose is used to find the closet gear, and that is the
 * object that is being moved. When the gripper is opened then the gear is being
 * placed into the kit open slot and the gear's location is now set the the open
 * slot location.
 * 
 * When CRCL server communication is established then CRCL commands are sent,
 * and the status contains the model and model inferences. Clearly, the
 * inferences could be done here or done at a lower level of control here we go
 * hanging again, hanging again.n It was auto formatting of all lines in the
 * file.
 * 
 *
 * @author john michaloski
 */
public class CRCLClient implements Runnable {

	// These are the kitting items associated with this robot and crcl client
	public static CShape _gear; // free gear in supply tray
	public static CShape _kit; // kit with open slot
	public static CShape _openslot; // open slot in kit

	public static Logger myLogger = Logger.getLogger("crcl");
	public static Level myLevel = Level.INFO;
	public static Logger diagLogger = Logger.getLogger("trace");
	

	public static class action_type {
		String kit;
		String openslot;
		String gear;
	}

	/**
	 * The constructor either sets i[ a crcl socket for communication or starts a
	 * fake kitting demo - by establishing the instances that would be found in a
	 * kitting demonstration.
	 */
	public CRCLClient() {
		try {
			myLogger = Globals.loggerInit("crcl");
			diagLogger = Globals.loggerInit("trace");
			// Connect to the server
			if (!Globals.bLoopback) {
				s = new CRCLSocket("localhost", CRCLSocket.DEFAULT_PORT);
			} else {
				fakeSetup();

				fakeFirstOrderLogic();
				// Herein first order logic done when gear moved.
				if (Globals.bDebug) {
					myLogger.log(myLevel,"INITIAL KTTING WORLD\n"+dumpKittingSetup());
				}
			}

			// Create an instance to wrap all commands.
			instance = new crcl.base.CRCLCommandInstanceType();
			id = 0;


		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * start the status thread - not used.
	 */
	public void startStatus() {
		Thread statusthread = new Thread();
		statusthread.start();
	}

	/**
	 * synchronous waiting till commandid and done are reported in crcl status.
	 * 
	 * @param commandid
	 * @return
	 */
	public int waitDone(long commandid) {

		long IDback = 1;
		CommandStatusType cmdStat = null;
		if (Globals.bLoopback) {
			try {
				for (int i = 0; i < 1; i++) {
					Thread.sleep(1000);
				}
				Globals.mutex.lock();
				Globals.curStatusCmdId = Globals.latestCmdId;
				Globals.crclCommandStatus = CommandStateEnumType.CRCL_DONE; // done

			} catch (Exception ex) {

			} finally {
				Globals.mutex.unlock();
			}
			return 0;
		}
		try {
			do {
				// We will assume that status is being generated automatically

				// Create and send getStatus request.
//                GetStatusType getStat = new GetStatusType();
//                getStat.setCommandID(incrementId(1));
//                instance.setCRCLCommand(getStat);
//                issueCrclCommand();

				// Read status from server
				CRCLStatusType stat = s.readStatus();

				// Print out the status details.
				cmdStat = stat.getCommandStatus();
				IDback = cmdStat.getCommandID();
				myLogger.log(myLevel, "Status:");
				myLogger.log(myLevel, "CommandID = " + IDback);
				myLogger.log(myLevel, "State = " + cmdStat.getCommandState());
				PointType pt = stat.getPoseStatus().getPose().getPoint();
				myLogger.log(myLevel, "pose = " + pt.getX() + "," + pt.getY() + "," + pt.getZ());
				JointStatusesType jst = stat.getJointStatuses();
				if (null != jst) {
					List<JointStatusType> l = jst.getJointStatus();
					myLogger.log(myLevel, "Joints:");
					for (JointStatusType js : l) {
						myLogger.log(myLevel, "Num=" + js.getJointNumber() + " Pos=" + js.getJointPosition());
					}
				}
				List<ModelsStatusType> models = stat.getModelStatus();
				for (ModelsStatusType model : models) {
					String name = model.getName();
					PmPose pose = CRCLPosemath.toPmPose(model.getPose());
					CShapes.storeInstance(name, pose);
				}

				try {
					Globals.mutex.lock();
					Globals.curStatusCmdId = cmdStat.getCommandID();
					Globals.crclCommandStatus = stat.getCommandStatus().getCommandState();

				} finally {
					Globals.mutex.unlock();
				}

			} while (!(IDback == commandid) || cmdStat.getCommandState().equals(CommandStateEnumType.CRCL_WORKING));
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return -1;
		}
		return 0;
	}

	@Override
	public void run() {
		try {
			long IDback = 1;
			CommandStatusType cmdStat = null;
			do {
				// Create and send getStatus request.
				GetStatusType getStat = new GetStatusType();
				getStat.setCommandID(incrementId(1));
				instance.setCRCLCommand(getStat);
				issueCrclCommand();

				// Read status from server
				CRCLStatusType stat = s.readStatus();

				// Print out the status details.
				cmdStat = stat.getCommandStatus();
				IDback = cmdStat.getCommandID();
				myLogger.log(myLevel, "Status:");
				myLogger.log(myLevel, "CommandID = " + IDback);
				myLogger.log(myLevel, "State = " + cmdStat.getCommandState());
				PointType pt = stat.getPoseStatus().getPose().getPoint();
				myLogger.log(myLevel, "pose = " + pt.getX() + "," + pt.getY() + "," + pt.getZ());
				JointStatusesType jst = stat.getJointStatuses();
				if (null != jst) {
					List<JointStatusType> l = jst.getJointStatus();
					myLogger.log(myLevel, "Joints:");
					for (JointStatusType js : l) {
						myLogger.log(myLevel, "Num=" + js.getJointNumber() + " Pos=" + js.getJointPosition());
					}
				}
				// now parse any all models

			} while (!IsQuit());
		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * crcl command to set the length units.
	 * 
	 * @param units
	 */
	public void setLengthUnitsType(String units) {
		try {
			// Create and send init command.
			SetLengthUnitsType lenuits = new SetLengthUnitsType();
			lenuits.setCommandID(incrementId(1));
			if (units.toLowerCase().startsWith("meter")) {
				lenuits.setUnitName(LengthUnitEnumType.METER);
			}
			instance.setCRCLCommand(lenuits);
			issueCrclCommand();
			waitDone(lenuits.getCommandID());
		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * send crcl command to dwell. Wait til done.
	 * 
	 * @param seconds amount of time in seconds to dwell.
	 */
	public void doDwell(double seconds) {
		try {
			// Create and send init command.
			DwellType dwell = new DwellType();
			dwell.setCommandID(incrementId(1));
			dwell.setDwellTime(seconds);
			instance.setCRCLCommand(dwell);
			issueCrclCommand();
			waitDone(dwell.getCommandID());
		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * send crcl command to close gripper.
	 */
	public void closeGripper() {
		setGripper(0.0);
	}

	/**
	 * send crcl command to open gripper.
	 */
	public void openGripper() {
		setGripper(1.0);
	}

	/**
	 * crcl setgripper command. 0 is closed, 1 is open.
	 * 
	 * @param position oercentage open of the gripper. 0 is closed. 1 is open.
	 */
	public void setGripper(double position) {
		try {
			// FIXME: check if <0 or >1
			// Create and send init command.
			SetEndEffectorType ee = new SetEndEffectorType();
			ee.setCommandID(incrementId(1));
			ee.setSetting(position);
			instance.setCRCLCommand(ee);
			issueCrclCommand();
			waitDone(ee.getCommandID());

		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * send init_canon crcl command. Wait till done.
	 */
	public void init() {
		try {
			// Create and send init command.
			InitCanonType init = new InitCanonType();
			init.setCommandID(incrementId(1));
			instance.setCRCLCommand(init);
			issueCrclCommand();
			waitDone(init.getCommandID());
		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * crcl status command, and then output status.
	 */
	public void status() {
		try {
			// Create and send getStatus request.
			GetStatusType getStat = new GetStatusType();
			getStat.setCommandID(incrementId(1));
			instance.setCRCLCommand(getStat);
			issueCrclCommand();

			// Read status from server
			CRCLStatusType stat = s.readStatus();
			List<ModelsStatusType> models = stat.getModelStatus();
			for (int i = 0; i < models.size(); i++) {
				ModelsStatusType model = models.get(i);
				String str = model.getName();
				str += "," + model.getPose().getPoint().getX();
				str += "," + model.getPose().getPoint().getY();
				str += "," + model.getPose().getPoint().getZ();
				myLogger.log(myLevel, str);
			}

		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * crcl moveto pose.
	 * 
	 * @param p pose cartesian translation
	 * @param q pose robtation in quaternion.
	 */
	public void moveTo(PmCartesian p, PmQuaternion q) {
		moveTo(p, q, true);
	}

	public int acquire(action_type action) {
		Pair<CShape, CShape> p = CShapes.findOpenKittingGearSlot();
		_kit = p.getKey();
		_openslot = p.getValue();
		if (_kit == null) {
			// ROS_FATAL_ONCE ( "Error: No open slots in any kits fopenslotpropound!");
			return -1;
		}
		CShape.inference_type inference = _kit.findInference(_openslot.name());
		String geartype = inference.type;
		// this gear is in this slot should be checked to exist every iteration
		// of state table.
		if ((_gear = CShapes.findFreeGear(geartype)) == null) {
			// ROS_FATAL_ONCE("Error: No matching free gear in supply vessel to move");
			return -1;
		}
		String s = "Free slot kit=" + _kit.name() + " slot=" + _openslot.name() + " Gear=" + _gear.name() + " at"
				+ KittingInterface.dumpPmPose(_gear._location) + "\n";
		if (Globals.bDebug) {
			System.out.print(s);
		}

		action.kit = _kit.name();
		action.openslot = _openslot.name();
		action.gear = _gear.name();

		return 0;
	}

	public int takepart(String gearname) {

		PmPose pickpose = new PmPose();
		// PmPose affpose = new PmPose();
		PmPose gearRobotCrd;
		PmPose gearWorldCrd;
		PmPose gripperoffset = new PmPose();
		PmQuaternion bend = rcs_robot.QBend;
		PmCartesian offset = new PmCartesian();
		try {

			Vector<CShape> nowinstances = CShapes.snapshotInstances();
			_gear = CShapes.findInstance(nowinstances, gearname);
			gearWorldCrd = _gear._location;
			if (Globals.bDebug) {
				myLogger.log(myLevel, "world gear    pose" + KittingInterface.dumpPmPose(gearWorldCrd));
				myLogger.log(myLevel, "world base    pose" + KittingInterface.dumpPmPose(rcs_robot.BasePose));
				myLogger.log(myLevel, "world baseinv pose" + KittingInterface.dumpPmPose(rcs_robot.BasePoseInv));
			}
			// affpoase = Posemath.pmPosePoseMult(affpose, affpose,
			// affpose)rcs_robot.basePoseInv * affpose;
			gearRobotCrd = new PmPose();

			Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, gearWorldCrd, gearRobotCrd);

			if (Globals.bDebug) {
				myLogger.log(myLevel, "robot gear pose" + KittingInterface.dumpPmPose(gearRobotCrd));
			}

			// The object gripper offset is where on the object it is to be gripped
			// e.g., offset.gripper.largegear = 0.0,0.0, -0.030, 0.0, 0.0.,0.0
			gripperoffset = rcs_robot.GripperOffset.get(_gear.type());

			bend = rcs_robot.QBend;

			// The gripperoffset is the robot gripper offset back to the 0T6 equivalent
			// pickpose = tf::Pose(bend, affpose.getOrigin()) * gripperoffset ;
			Posemath.pmPosePoseMult(new PmPose(gearRobotCrd.tran, bend),
					new PmPose(gripperoffset.tran, gripperoffset.rot), pickpose);

			if (Globals.bDebug) {
				myLogger.log(myLevel, "pickpose" + KittingInterface.dumpPmPose(pickpose));
			}

			offset = pickpose.tran;
			// Retract
			// r.moveTo(rcs_robot.Retract * PmPose(offset, bend));
			PmPose retractpose = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					retractpose);
			moveTo(retractpose);
			doDwell(_mydwell);

			// move to grasping posiiton of gear
			moveTo(offset, bend);
			doDwell(_mydwell);

			// step: grasp gear
			closeGripper();
			doDwell(_mygraspdwell);

			// step: retract robot after grasping gear
			// r.moveTo(rcs_robot.Retract * PmPose(offset, bend));
			retractpose = new PmPose();
			Posemath.pmPosePoseMult(rcs_robot.Retract, new PmPose(offset, bend), retractpose);
			moveTo(retractpose);
			doDwell(_mydwell);
		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
			return -1;
		}
		return 0;
	}

	/**
	 * step: move to approach kitting open slot Use existing found open kit slot
	 * Offset from the centroid of the kit and reorientation should be part of
	 * inferences /now_instances.findSlot(this._kit, this._openslot.name());
	 *
	 * @param gearname
	 * @param kit
	 * @param openslotname
	 * @return
	 */
	public int placepart(String gearname, String kitname, String openslotname) {

		PmPose pickpose = new PmPose();
		// PmPose affpose = new PmPose();
		PmPose gearRobotCrd;
		PmPose gearWorldCrd;
		PmPose gripperoffset = new PmPose();
		PmQuaternion bend = rcs_robot.QBend;
		PmCartesian offset = new PmCartesian();
		PmPose slotpose = new PmPose();
		PmPose slotoffset = new PmPose();
		PmPose placepose = new PmPose();
		try {
			Vector<CShape> nowinstances = CShapes.snapshotInstances();
			_gear = CShapes.findInstance(nowinstances, gearname);
			_kit = CShapes.findInstance(nowinstances, kitname);
			_openslot = CShapes.findSlot(_kit, openslotname);

			CShape.inference_type inference = _kit.findInference(_openslot.name());

			if (_gear == null)
				throw new IllegalArgumentException("gear is null" + gearname);
			if (_kit == null)
				throw new IllegalArgumentException("kit is null" + kitname);
			if (_openslot == null)
				throw new IllegalArgumentException("Kitting slot is null" + kitname + " " + openslotname);

			PmPose slotloc = Globals.convertTranPose(inference.location);
			if (Globals.bDebug) {
				myLogger.log(myLevel, "kitloc" + KittingInterface.dumpPmPose(_kit._location));
				myLogger.log(myLevel, "slotloc" + KittingInterface.dumpPmPose(slotloc));
			}
			// This xyz already has been reoriented by tray rotation.
			// CShape openslot = CShapes.findSlot(_kit, _openslot.name());

			// compute reorient based on kit rotation

			Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, slotloc, slotpose);
			if (Globals.bDebug) {
				myLogger.log(myLevel, "slotpose" + KittingInterface.dumpPmPose(slotpose));
			}

			// up in z only for grasping now - hard coded
			slotoffset = rcs_world.slotoffset.get("vessel");

			// placepose = PmPose(bend, slotpose.tran) * slotoffset; // fixme: what if gear
			// rotated
			Posemath.pmPosePoseMult(new PmPose(slotpose.tran, bend), slotoffset, placepose);
			if (Globals.bDebug) {
				myLogger.log(myLevel, "slotoffset" + KittingInterface.dumpPmPose(slotoffset));
				myLogger.log(myLevel, "placepose" + KittingInterface.dumpPmPose(placepose));
			}
			offset = placepose.tran; // xyz position

			// Approach
			// r.moveTo(rcs_robot.Retract * PmPose(bend, offset));
			PmPose p = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					p);
			moveTo(p);

			doDwell(_mydwell);

			// Place the grasped object
			moveTo(new PmPose(offset, bend));
			doDwell(_mydwell);

			// open gripper and wait
			openGripper();
			doDwell(_mygraspdwell);

			// Retract from placed object
			// r.moveTo(rcs_robot.Retract * PmPose(bend, offset));
			p = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					p);
			moveTo(p);
			doDwell(_mydwell);

		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
			return -1;
		}
		return 0;
	}

	/**
	 * crcl moveto pose.
	 *
	 * @param p pose
	 */
	public void moveTo(PmPose p) {
		moveTo(p.tran, p.rot, true);
	}

	/**
	 * crcl moveto pose.
	 * 
	 * @param p         pose cartesian translation
	 * @param q         pose robtation in quaternion.
	 * @param bstraight whether to make a straight line motion.
	 */
	public void moveTo(PmCartesian p, PmQuaternion q, boolean bstraight) {
		try {
			PmPose pmpose = new PmPose(p, q);
			lastmoveto = pmpose.clone();
			PoseType pose = toPose(pmpose);
			// Create and send MoveTo command.
			MoveToType moveTo = new MoveToType();
			moveTo.setCommandID(incrementId(1));
			// PoseType pose = p; // pose(point(0.65, 0.05, 0.15), vector(1, 0, 0),
			// vector(0, 0, 1));
			moveTo.setEndPosition(pose);
			moveTo.setMoveStraight(bstraight);
			instance.setCRCLCommand(moveTo);
			// s.writeCommand(instance, true);
			issueCrclCommand();
			waitDone(moveTo.getCommandID());

		} catch (Exception ex) {
			myLogger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * mutexed increment command id number.
	 * 
	 * @param n amount to incremeent
	 * @return updated id .
	 */
	public static long incrementId(int n) {
		// Always good practice to enclose locks in a try-finally block
		try {
			Globals.mutex.lock();
			id = id + n;
			Globals.latestCmdId = id;
			rcs_robot.latestCmdId = id;
		} finally {
			Globals.mutex.unlock();
		}
		return id;
	}

	/**
	 * mutexed quit flag access.
	 * 
	 * @return true/false to quit. True is quit.
	 */
	public static boolean IsQuit() {
		boolean bflag;
		try {
			Globals.mutex.lock();
			bflag = bQuit;
		} finally {
			Globals.mutex.unlock();
		}
		return bflag;
	}

	public static void setQuit(boolean bflag) {
		try {
			Globals.mutex.lock();
			bQuit = bflag;
		} finally {
			Globals.mutex.unlock();
		}
	}

	/**
	 * send crcl xml command and wait til done. If bLoopback simulate communication
	 * by sleeping and save moveto poses to identify grasped gear, and when released
	 * update gear location to kitting slot.
	 */
	public void issueCrclCommand() {
		try {
			if (Globals.bLoopback) {
				// we don't actually send to socket if loopback
				if (instance.getCRCLCommand().getClass().toString().equalsIgnoreCase("class crcl.base.MoveToType")) {
					try {
						MoveToType m = (MoveToType) instance.getCRCLCommand();
						myLogger.log(myLevel,
								instance.getCRCLCommand().getClass() + KittingInterface.dumpPmPose(lastmoveto));
						// lastmoveto = CRCLPosemath.toPmPose(m.getEndPosition());
					} catch (Exception ex) {
						myLogger.log(Level.SEVERE, null, ex);
					}

				} else if (instance.getCRCLCommand().getClass().toString()
						.equalsIgnoreCase("class crcl.base.DwellType")) {
					DwellType d = (DwellType) instance.getCRCLCommand();
					//myLogger.log(myLevel, instance.getCRCLCommand().getClass() + ":" + d.getDwellTime());

				} else if (instance.getCRCLCommand().getClass().toString()
						.equalsIgnoreCase("class crcl.base.SetEndEffectorType")) {
					SetEndEffectorType e = (SetEndEffectorType) instance.getCRCLCommand();
					if (e.getSetting() == 0.0) {
						myLogger.log(myLevel, instance.getCRCLCommand().getClass() + " close");
						graspedgear = KittingInterface.closestGear(lastmoveto);
						if (graspedgear == null) {
							myLogger.log(myLevel, "No grasping gear found");
						}
					} else {
						// update graspgear location
						myLogger.log(myLevel, instance.getCRCLCommand().getClass() + " open");
						if (graspedgear == null) {
							myLogger.log(myLevel, "No grasping gear found");
							throw new java.lang.Error("Java has no debugbreak -sad");
						}
						if (_openslot == null) {
							myLogger.log(myLevel, "No open slot for placing gear found to updte");
							throw new java.lang.Error("Java has no debugbreak -sad");
						}
						CShape.inference_type inference = _kit.findInference(_openslot.name());
						PmPose slotloc = Globals.convertTranPose(inference.location);

						CShape gear = CShapes.findInstance(CShapes.instances, graspedgear.name());
						gear._location = slotloc; // offset?

						myLogger.log(myLevel, "release gear " + graspedgear.name()
								+ " to new location" + KittingInterface.dumpPmPose(slotloc));
						
						// New location - now compute from locations first order logic
						KittingInterface.fakeFirstOrderLogic();
						myLogger.log(myLevel,
								"New kitting scene inferences after gear move\n" + KittingInterface.dumpKittingSetup());

					}
				}
			} else {
				myLogger.log(myLevel, "issueCrclCommand " + instance.getCRCLCommand().getClass());
				issueCrclCommand();

			}
		} catch (Exception ex) {
			myLogger.log(myLevel, "Failed issueCrclCommand " + instance.getName());
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * saved last moveto pose for identifying the gear for loopback simulation.
	 */
	public static PmPose lastmoveto = new PmPose();

	/**
	 * using the lastmoveto the grasped gear is identified and save in this
	 * variable.
	 */
	public static CShape graspedgear;

	// dwell varaibles - one general and one after grasping
	public static double _mygraspdwell = 2.0;
	public static double _mydwell = 0.1;

	/**
	 * the crcl communication socket.
	 */
	CRCLSocket s;
	/**
	 * an instance to wrap all commands.
	 */
	CRCLCommandInstanceType instance;
	/**
	 * CRCL command id
	 */
	static long id = 0;
	/**
	 * quit thread flag.
	 */
	static boolean bQuit = false;

}
