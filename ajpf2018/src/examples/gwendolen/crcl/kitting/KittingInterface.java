package gwendolen.crcl.kitting;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import crcl.base.*;
import static gwendolen.crcl.kitting.CShapes.definitions;
import static gwendolen.crcl.kitting.CShapes.findDefinition;
import static gwendolen.crcl.kitting.CShapes.instances;
import static gwendolen.crcl.kitting.CShapes.snapshotInstances;
import static gwendolen.crcl.kitting.KittingInterface.dumpKittingSetup;

//import org.apache.commons.math3.util.Pair;
import java.lang.*;
import org.apache.commons.math3.util.Pair;

/**
 * Provides methods to perform or fake performing the kitting demonstration for
 * the fanuc robot. If live crcl model status, then the robot will use the model
 * inferences to determine the free gear and matching kit open slot.
 *
 * @author michalos
 */
public class KittingInterface {
	public static Logger myLogger = Logger.getLogger("crcl");
	public static Level myLevel = Level.INFO;
	public static Logger diagLogger = Logger.getLogger("trace");

	/**
	 * construct the kittingdemo, provide a reference to the crcl client api.
	 *
	 * @param crcl CRCLClient class pointer.
	 */
	public KittingInterface(CRCLClient crcl) {
		r = crcl;
		myLogger = Globals.loggerInit("crcl");
		diagLogger = Globals.loggerInit("trace");

	}

	public boolean isGear(String item) {
		if (item.indexOf('.') < 0)
			return true;
		return false;
	}

	public boolean isKit(String item) {
		if (item.indexOf('.') >= 0)
			return true;
		return false;
	}

	public boolean move(String location) {
		// step: approach gear
		// gearname = _gear.name();
		CShape _item;
		CShape _kit;
		boolean bReturn = false;

		if (location.indexOf('.') < 0) {
			_item = CShapes.findInstance(CShapes.instances, location);

			if (_item == null)
				return bReturn;
			if (_item.isGear()) {
				diagLogger.log(myLevel, "moveto gear" + _item.name());
				bReturn = pickupGear(_item);
			} else
				bReturn = false;
		} else {
			String kit = location.substring(0, location.indexOf("."));
			_kit = CShapes.findInstance(CShapes.instances, kit);
			String slot = location.substring(location.indexOf(".") + 1);
			diagLogger.log(myLevel, "move gear to kit " + _kit.name() + " slot=" + slot);
			bReturn = placeGear(_kit, slot);
		}
		return bReturn;
	}

	public PmCartesian getGearLocation(String gearname, boolean bWorld)// throws PmException 
	{
		// assumes gearname is actually gearname
		PmCartesian location = new PmCartesian();
		try {
			if (gearname.indexOf('.') > 0)
				new Exception("getGearLocation gearname not a gear");

			CShape _item;
			_item = CShapes.findInstance(CShapes.instances, gearname);
			if (_item == null)
				new Exception("getGearLocation gearname not found");

			PmPose gearRobotCrd = new PmPose();
			Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, _item._location, gearRobotCrd);

			// Pick world or robot coordinate system
			if (bWorld)
				location = _item._location.tran;
			else
				location = gearRobotCrd.tran;
		} catch (PmException pmex) {
			//throw new PmException(pmex.pmErrno, pmex.getMessage());
		}

		return location;
	}

	public void setGearLocation(String gearname, PmCartesian location, boolean bWorld) {
		try {
			// assumes gearname is actually gearname
			if (gearname.indexOf('.') > 0)
				new Exception("setGearLocation gearname not a gear");

			CShape _item;
			_item = CShapes.findInstance(CShapes.instances, gearname);
			if (_item == null)
				new Exception("getGearLocation gearname not found");
			if (bWorld) {
				_item._location.tran = location;
			} else {
				PmPose gearWorldCrd = new PmPose();
				Posemath.pmPosePoseMult(rcs_robot.BasePose, _item._location, gearWorldCrd);
				_item._location.tran = gearWorldCrd.tran;

			}
		} catch (PmException pmex) {
			// throw new PmException(pmex.pmErrno, pmex.getMessage());
		}
	}

	public boolean pickupGear(CShape gear) {
		if (gear == null) {
			return false;
		}
		try {
			// Define robot gear we will pickup and place into open slot;
			myLogger.log(myLevel, "pickupGear=" + gear.name() + " approach/move/close/retract");
			r._gear = gear;

			// step: approach gear

			gearname = r._gear.name();
			gearWorldCrd = r._gear._location;
			if (Globals.bDebug) {
				diagLogger.log(myLevel, "world gear    pose" + KittingInterface.dumpPmPose(gearWorldCrd));
				diagLogger.log(myLevel, "world base    pose" + KittingInterface.dumpPmPose(rcs_robot.BasePose));
				diagLogger.log(myLevel, "world baseinv pose" + KittingInterface.dumpPmPose(rcs_robot.BasePoseInv));
			}

			gearRobotCrd = new PmPose();
			Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, gearWorldCrd, gearRobotCrd);

			if (Globals.bDebug) {
				diagLogger.log(myLevel, "robot gear pose" + KittingInterface.dumpPmPose(gearRobotCrd));
			}

			// The object gripper offset is where on the object it is to be gripped
			// e.g., offset.gripper.largegear = 0.0,0.0, -0.030, 0.0, 0.0.,0.0
			gripperoffset = rcs_robot.GripperOffset.get(r._gear.type());

			bend = rcs_robot.QBend;

			// The gripperoffset is the robot gripper offset back to the 0T6 equivalent
			// pickpose = tf::Pose(bend, affpose.getOrigin()) * gripperoffset ;
			Posemath.pmPosePoseMult(new PmPose(gearRobotCrd.tran, bend),
					new PmPose(gripperoffset.tran, gripperoffset.rot), pickpose);

			if (Globals.bDebug) {
				diagLogger.log(myLevel, "pickpose" + KittingInterface.dumpPmPose(pickpose));
			}

			offset = pickpose.tran;

			// Approach
			PmPose approachPose = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					approachPose);
			r.moveTo(approachPose);
			r.doDwell(r._mydwell);

			// move to grasping position of gear
			r.moveTo(offset, bend);
			r.doDwell(r._mydwell);

			// step: grasp gear
			r.closeGripper();
			r.doDwell(r._mygraspdwell);

			// step: retract robot after grasping gear
			PmPose retractpose = new PmPose();
			Posemath.pmPosePoseMult(rcs_robot.Retract, new PmPose(offset, bend), retractpose);
			r.moveTo(retractpose);
			r.doDwell(r._mydwell);

		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
	}

	public boolean placeGear(CShape kit, String slotname) {
		try {
			if (kit == null) {
				diagLogger.log(myLevel, "NULL kit");
				return false;
			}
			myLogger.log(myLevel,
					"placeGear kit=" + kit.name() + " slot= " + slotname + "  approach/move/open/retract");

			// Define robot open slot we will use to place gear
			r._kit = kit;
			r._openslot = CShapes.findSlot(kit, slotname);

			if (r._openslot == null) {
				diagLogger.log(myLevel, "NULL open slot");
				return false;
			}
			CShape.inference_type inference = r._kit.findInference(r._openslot.name());
			PmPose slotloc = Globals.convertTranPose(inference.location);
			if (Globals.bDebug) {
				diagLogger.log(myLevel, "kitloc" + dumpPmPose(r._kit._location));
				diagLogger.log(myLevel, "slotloc" + dumpPmPose(slotloc));
			}
			Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, slotloc, slotpose);
			if (Globals.bDebug) {
				diagLogger.log(myLevel, "slotpose" + dumpPmPose(slotpose));
			}

			// up in z only for grasping now - hard coded
			slotoffset = rcs_world.slotoffset.get("vessel");

			// placepose = PmPose(bend, slotpose.tran) * slotoffset; // fixme: what if gear
			// rotated
			Posemath.pmPosePoseMult(new PmPose(slotpose.tran, bend), slotoffset, placepose);
			if (Globals.bDebug) {
				diagLogger.log(myLevel, "slotoffset" + dumpPmPose(slotoffset));
				diagLogger.log(myLevel, "placepose" + dumpPmPose(placepose));
			}
			offset = placepose.tran; // xyz position

			// Approach
			// r.moveTo(rcs_robot.Retract * PmPose(bend, offset));
			PmPose p = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					p);
			r.moveTo(p);

			r.doDwell(r._mydwell);

			// Place the grasped object
			r.moveTo(new PmPose(offset, bend));
			r.doDwell(r._mydwell);

			// open gripper and wait
			r.openGripper();
			r.doDwell(r._mygraspdwell);

			// Retract from placed object
			p = new PmPose();
			Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot), new PmPose(offset, bend),
					p);
			r.moveTo(p);

			r.doDwell(r._mydwell);

		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
	}

	public boolean isWorking() {
		long latestCmdId;
		long curCmdId;
		CommandStateEnumType strStatus;
		CommandStateEnumType curCmdStatus;
		try {
			Globals.mutex.lock();
			latestCmdId = Globals.latestCmdId;
			curCmdId = Globals.curStatusCmdId;
			curCmdStatus = rcs_robot.crclCommandStatus;
			strStatus = rcs_robot.crclCommandStatus;
		} finally {
			Globals.mutex.unlock();
		}
		boolean bFlag = ((curCmdId == latestCmdId)
				// && (curCmdStatus ==CommandStatusType.CRCL_DONE));
				&& strStatus.value().equalsIgnoreCase("CRCL_Done"));
		return bFlag;
	}

	public static void fakeSetup() {
		try {// !!! NOTE public PmQuaternion(double starts, double startx, double startz,
				// double starty) throws PmException {
			CShapes.storeInstance("sku_kit_m2l1_vessel14",
					new PmPose(new PmCartesian(0.40, -1.05, 0.92), new PmQuaternion(0.6940, 0.000, -0.720, 0.000)));
			CShapes.storeInstance("sku_kit_m2l1_vessel15",
					new PmPose(new PmCartesian(0.18, -1.05, 0.92), new PmQuaternion(0.6940, 0.000, -0.720, 0.000)));
			CShapes.storeInstance("sku_medium_gear_vessel16",
					new PmPose(new PmCartesian(0.19, -1.24, 0.92), new PmQuaternion(1.000, 0.000, 0.017, 0.000)));
			CShapes.storeInstance("sku_part_medium_gear17",
					new PmPose(new PmCartesian(.23, -1.20, 0.92), new PmQuaternion(1.000, -0.011, 0.022, -0.003)));
			CShapes.storeInstance("sku_part_medium_gear18",
					new PmPose(new PmCartesian(0.15, -1.20, 0.92), new PmQuaternion(1.000, -0.001, 0.017, -0.000)));
			CShapes.storeInstance("sku_part_medium_gear19",
					new PmPose(new PmCartesian(0.15, -1.28, 0.92), new PmQuaternion(1.000, 0.001, 0.018, -0.003)));
			CShapes.storeInstance("sku_part_medium_gear20",
					new PmPose(new PmCartesian(0.23, -1.28, 0.92), new PmQuaternion(1.000, -0.002, 0.012, 0.001)));
			CShapes.storeInstance("sku_large_gear_vessel21",
					new PmPose(new PmCartesian(0.39, -1.26, 0.92), new PmQuaternion(0.693, 0.000, 0.721, 0.000)));
			CShapes.storeInstance("sku_part_large_gear22",
					new PmPose(new PmCartesian(0.39, -1.21, 0.92), new PmQuaternion(1.000, 0.002, 0.016, -0.002)));
			CShapes.storeInstance("sku_part_large_gear23",
					new PmPose(new PmCartesian(0.39, -1.32, 0.92), new PmQuaternion(0.982, -0.002, 0.188, -0.001)));
			Globals.bReadAllInstances = true;
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static int fakeFirstOrderLogic() {
		// check if all instances have been read at least oncee
		if (Globals.bReadAllInstances != true) {
			return -1;
		}

		try {
			// clear part updates
			for (int i = 0; i < CShapes.instances.size(); i++) {
				if (CShapes.instances.get(i).isGear()) {
					CShape gear = CShapes.instances.get(i);
					gear._updated = "";
				}
			}

			// reassign instances properties - slots
			for (int i = 0; i < CShapes.instances.size(); i++) {
				CShape tray;
				// If not a sku skip
				if (!CShapes.instances.get(i).isSkuPart()) {
					continue;
				}

				// Don't want a gear - assume gears standalone
				if (CShapes.instances.get(i).isGear()) {
					continue;
				}

				// search if one of my robot kits/vessel, if not continue
				// assume only my trays and gears
				// we now have a kit or vessel , i.e., tray
				tray = CShapes.instances.get(i);

				// clear properties
				CShapes.instances.get(i).inferences.clear();

				// Get tray definition for slots
				CShape slotz = CShapes.findDefinition(tray.type());

				// Should find element description
				if (slotz == null) {
					continue;
				}

				// now reassign first order subelement properties
				for (int j = 0; j < slotz._contains.size(); j++) {
					CShape slot = slotz._contains.get(j);
					CShape.inference_type inference = new CShape.inference_type();
					inference.name = slot.name();
					inference.type = slot.type();
					inference.state = "empty";

					PmRotationMatrix m = new PmRotationMatrix();
					Posemath.pmQuatMatConvert(tray._location.rot, m);
					PmCartesian rotorigin = new PmCartesian();
					Posemath.pmMatCartMult(m, slot._location.tran, rotorigin);
					PmCartesian pos_slot = Posemath.add(tray._location.tran, rotorigin);

					inference.location = String.format("%7.4f,%7.4f,%7.4f", pos_slot.x, pos_slot.y, pos_slot.z);
					tray.inferences.add(inference);
				}

				// Now see where gears are located
				for (int j = 0; j < CShapes.instances.size(); j++) {
					// Don't want a kit
					if (CShapes.instances.get(j).isKit()) {
						continue;
					}
					// don't want a vessel
					if (CShapes.instances.get(j).isVessel()) {
						continue;
					}
					// If not a sku skip
					if (!CShapes.instances.get(j).isSkuPart()) {
						continue;
					}

					// found a gear
					CShape gear = CShapes.instances.get(j);

					// now cycle through the slots in the tray to see if part is in it
					for (int k = 0; k < slotz._contains.size(); k++) {
						CShape slot = slotz._contains.get(k);

						// Position of slot in actual tray - rotation included
						// slot= tray_centroid + (vessel_rotrayation * slotoffset)
						// tf::Matrix3x3 m(tray._location.getRotation());
						PmRotationMatrix m = new PmRotationMatrix();
						Posemath.pmQuatMatConvert(tray._location.rot, m);
						PmCartesian rotorigin = new PmCartesian();
						Posemath.pmMatCartMult(m, slot._location.tran, rotorigin);

						PmCartesian pos_slot = Posemath.add(tray._location.tran, rotorigin);

						// Determine if part contained in this slot - some error sphere
						// Position of part
						PmCartesian pos_part = gear._location.tran;

						// Compute distance between slot and part
						double mag = Posemath.mag(Posemath.subtract(pos_slot, pos_part));

						// is gear in the slot - loose criteria 50 mm?
						if (Math.abs(mag) < 0.05) {
							CShape.inference_type inference = new CShape.inference_type();
							inference.name = gear.name();
							inference.parent = tray.name();
							inference.slot = slot.name();
							gear._updated = tray.name();
							gear.inferences.add(inference);

							CShape.inference_type trayinference = tray.findInference(slot.name());
							trayinference.state = gear.name();
						}

					}
				}
			}
			// if part not updated clear parent/slot
			for (int i = 0; i < CShapes.instances.size(); i++) {
				if (!CShapes.instances.get(i).isGear()) {
					continue;
				}

				if (CShapes.instances.get(i)._updated.isEmpty()) {
					CShape gear = CShapes.instances.get(i);
					CShape.inference_type inference = new CShape.inference_type();
					inference.parent = "";
					inference.slot = "";
					gear.inferences.add(inference);
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return -1;
		}
		return 0;
	}

	public static int gearInSupplyTray(String gearname) {

		// check if all instances have been read at least once
		if (Globals.bReadAllInstances != true) {
			return -1;
		}

		try {
			CShape gear = CShapes.findInstance(CShapes.instances, gearname);

			// now cycle through the slots in the tray to see if part is in it
			for (int i = 0; i < CShapes.instances.size(); i++) {
				CShape tray = CShapes.instances.get(i);
				if (!tray.isVessel()) {
					continue;
				}
				CShape slotz = CShapes.findDefinition(tray.type());

				for (int k = 0; k < slotz._contains.size(); k++) {
					CShape slot = slotz._contains.get(k);

					// Position of slot in actual tray - rotation included
					// slot= tray_centroid + (vessel_rotrayation * slotoffset)
					// tf::Matrix3x3 m(tray._location.getRotation());
					PmRotationMatrix m = new PmRotationMatrix();
					Posemath.pmQuatMatConvert(tray._location.rot, m);
					PmCartesian rotorigin = new PmCartesian();
					Posemath.pmMatCartMult(m, slot._location.tran, rotorigin);

					PmCartesian pos_slot = Posemath.add(tray._location.tran, rotorigin);

					// Determine if part contained in this slot - some error sphere
					// Position of part
					PmCartesian pos_part = gear._location.tran;

					// Compute distance between slot and part
					double mag = Posemath.mag(Posemath.subtract(pos_slot, pos_part));

					// is gear in the slot - loose criteria 50 mm?
					if (Math.abs(mag) < 0.05) {
						return 1;
					}

				}
			}

		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return -1;
		}
		return 0;
	}

	public static CShape closestGear(PmPose location) {
		if (Globals.bDebug) {
			diagLogger.log(myLevel, "Closest gear location=" + dumpPmPose(location));
		}
		CShape gear = null;

		try {
			PmCartesian pos_move = location.tran;
			Vector<CShape> now_instances = CShapes.snapshotInstances();
			for (CShape shape : now_instances) {
				if (!shape.isGear()) {
					continue;
				}
				PmPose shapeRobotCrd = new PmPose();
				Posemath.pmPosePoseMult(rcs_robot.BasePoseInv, shape._location, shapeRobotCrd);
				PmCartesian pos_part = shapeRobotCrd.tran;
				if (Globals.bDebug) {
					diagLogger.log(myLevel, "Gear " + shape.name() + "location=" + dumpPmPose(shapeRobotCrd));
				}

				// Compute distance between slot and part
				double mag = Posemath.mag(Posemath.subtract(pos_move, pos_part));

				// is gear close enough pick it, random distance threshold
				// fixme: should I search all gears and pick one closest...
				if (Math.abs(mag) < 0.025) {
					try {
						gear = (CShape) shape.clone();
						diagLogger.log(myLevel, "Closest Gear to commanded location" + shape.name());

						return gear;
					} catch (Exception ex) {
						Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		return gear;
	}

	public static String dumpPmPose(PmPose pose) {
		String s;

		s = String.format("%7.4f, ", pose.tran.x) + String.format("%7.4f, ", pose.tran.y)
				+ String.format("%7.4f ", pose.tran.z) + String.format("%7.4f, ", pose.rot.x)
				+ String.format("%7.4f, ", pose.rot.y) + String.format("%7.4f, ", pose.rot.z)
				+ String.format("%7.4f, ", pose.rot.s);
		return s;
	}

	public static String dumpPmPosePos(PmPose pose) {
		String s;

		s = String.format("%7.4f, ", pose.tran.x) + String.format("%7.4f, ", pose.tran.y)
				+ String.format("%7.4f ", pose.tran.z);
		return s;
	}

	public static String dumpInstances() {
		String ss = "";
		for (CShape instance : CShapes.instances) {
			ss += instance.name() + " at " + dumpPmPose(instance._location) + "\n";
		}
		return ss;
	}

	/**
	 * dumpInferences dump of logical and physical kitting information with physical
	 * locations.
	 * 
	 * @return String containing complete kitting world model - kits, trays, gears
	 *         and slots.
	 */
	public static String dumpInferences() {
		Vector<CShape> now_instances = CShapes.snapshotInstances();

		String ss = "";
		for (int i = 0; i < now_instances.size(); i++) {
			CShape instance = now_instances.get(i);

			ss += instance.name() + " at " + KittingInterface.dumpPmPose(instance._location) + "\n";

			if (instance.isGear()) {
				CShape.inference_type inference = instance.inferences.get(0);

				ss += "\tIn: " + inference.parent + "(" + inference.slot + ")\n";
				continue;
			}

			// either kit or vessel - now do slots
			CShape slotz = CShapes.findDefinition(instance.type());

			if (slotz == null) {
				continue;
			}

			for (int j = 0; j < slotz._contains.size(); j++) {
				CShape slot = slotz._contains.get(j);
				CShape.inference_type inference = instance.findInference(slot.name());
				if (inference == null) {
					continue;
				}
				ss += "\t" + inference.name + " " + inference.type + " " + inference.state + " (" + inference.location
						+ ")\n";
			}

		}
		return ss;
	}

	/**
	 * dumpKittingSetup dump of logical kitting information with no physical
	 * locations. Hopefully easier to understand
	 * 
	 * @return String containing logical kitting world model - state of kits, trays,
	 *         gears and slots.
	 */
	public static String dumpKittingSetup() {
		Vector<CShape> now_instances = CShapes.snapshotInstances();

		String ss = "";
		for (int i = 0; i < now_instances.size(); i++) {
			CShape instance = now_instances.get(i);

			// skip output of where gear is - assume in tray/kit/slot for now.
			if (instance.isGear())
				continue;

			ss += instance.name() + " at [" + dumpPmPosePos(instance.centroid()) + "]\n";

			// either kit or vessel - now do slots
			CShape slotz = CShapes.findDefinition(instance.type());

			if (slotz == null) {
				continue;
			}

			for (int j = 0; j < slotz._contains.size(); j++) {
				CShape slot = slotz._contains.get(j);
				CShape.inference_type inference = instance.findInference(slot.name());
				if (inference == null) {
					continue;
				}
				ss += "\t" + inference.name + " " + inference.type + " (" + inference.state + ") at ["
						+ inference.location + "]\n";
			}

		}
		return ss;
	}

	public static String dumpDefinitions() {
		String s = "";
		for (CShape shape : CShapes.definitions) {
			s += shape.dumpShape();
		}
		return s;
	}

	public static PmHomogeneous toHMat(PmPose pose) {
		try {
			PmRotationMatrix m = new PmRotationMatrix();
			Posemath.pmQuatMatConvert(pose.clone().rot, m);
			return new PmHomogeneous(pose.tran.clone(), m);
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public static PmRotationMatrix toRotMat(PmPose pose) {
		try {
			PmRotationMatrix m = new PmRotationMatrix();
			Posemath.pmQuatMatConvert(pose.clone().rot, m);
			return m;
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public static PmPose multiply(PmPose a, PmPose b) {
		try {
			PmRotationMatrix arot = toRotMat(a);
			PmRotationMatrix brot = toRotMat(b);

			// m_origin += m_basis * t.m_origin;
			PmCartesian res = new PmCartesian();
			Posemath.pmMatCartMult(arot, b.tran, res);
			PmCartesian tran = Posemath.add(a.tran, res);

			// m_basis *= t.m_basis
			PmRotationMatrix rot = new PmRotationMatrix();
			Posemath.pmMatMatMult(arot, brot, rot);

			PmPose p = new PmPose();
			Posemath.pmMatQuatConvert(rot, p.rot);
			p.tran = tran;
			return p;

		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	////////////////////////////////////////////
	// gear, kit, slot defined in CRCLClient
	CRCLClient r;

	// Demo state varaibles
	PmPose pickpose = new PmPose();
	String gearname;
	// PmPose affpose = new PmPose();
	PmPose gearRobotCrd;
	PmPose gearWorldCrd;
	PmPose gripperoffset = new PmPose();
	PmQuaternion bend = rcs_robot.QBend;
	PmCartesian offset = new PmCartesian();
	PmPose slotpose = new PmPose();
	PmPose slotoffset = new PmPose();
	PmPose placepose = new PmPose();
}
