package gwendolen.crcl.kitting.gwendolencrclclient;

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
import static gwendolen.crcl.kitting.gwendolencrclclient.CShapes.definitions;
import static gwendolen.crcl.kitting.gwendolencrclclient.CShapes.findDefinition;
import static gwendolen.crcl.kitting.gwendolencrclclient.CShapes.instances;
import static gwendolen.crcl.kitting.gwendolencrclclient.CShapes.snapshotInstances;
import org.apache.commons.math3.util.Pair;
import java.lang.*;

/**
 * Provides methods to perform or fake performing the kitting demonstration for
 * the fanuc robot. If live crcl model status, then the robot will use the model
 * inferences to determine the free gear and matching kit open slot.
 *
 * @author michalos
 */
public class KittingDemo {

    /**
     * construct the kittingdemo, provide a reference to the crcl client api.
     *
     * @param crcl CRCLClient class pointer.
     */
    public KittingDemo(CRCLClient crcl) {
        r = crcl;
    }

    /**
     * use a state table to issue robot command. KittingDemo class will do the
     * following: 1) find an available gear (in supply tray) and kit open slot
     * to place the 2-5) approach, move to gear, grasp gear, and retract 5-8)
     * approach kit open slot move to near open slot, release gear, and then
     * retract
     *
     * @param state current state to execute.
     * @return updated state number.
     */
    public int issueRobotCommands(int state) {
        // Must declare all variables beforehand

        if (CShapes.instances.size() == 0) {
            //std::cerr << "Error: No gear instances can be read from gazebo_ro_api topic - restart Gazebo!\n";
            return -1;
        }

        //Vector<CShape> now_instances = CShapes.snapshotInstances();
        try {

            switch (state) {
                case 0: {
                    // Step: Find an open kitting slot and a free gear

                    // Search kits for empty slot. Identify type of gear
                    // find corredponding gear size in supply gtray.
                    ;
                    Pair<CShape, CShape> p = CShapes.findOpenKittingGearSlot();
                    _kit = p.getKey();
                    _openslot = p.getValue();
                    if (_kit == null) {
                        //ROS_FATAL_ONCE ( "Error: No open slots in any kits fopenslotpropound!");
                        return -1;
                    }
                    CShape.inference_type inference = _kit.findInference(_openslot.name());
                    String geartype = inference.type;
                    // this gear is in this slot should be checked to exist every iteration
                    // of state table
                    if ((_gear = CShapes.findFreeGear(geartype)) == null) {
                        //ROS_FATAL_ONCE("Error: No matching free gear in supply vessel to move");
                        return -1;
                    }
                    String s = "Free slot kit=" + _kit.name() + " slot=" + _openslot.name()
                            + " Gear=" + _gear.name() + " at" + dumpPmPose(_gear._location) + "\n";
//                    if (Globals.bDebug) {
                        System.out.print(s);
//                    }

                    ++state;
                    return state;
                }
                case 1: {

                    // step: approach gear
                    gearname = _gear.name();

                    gearWorldCrd = _gear._location;
                    if (Globals.bDebug) {
                        System.out.println("world gear    pose" + KittingDemo.dumpPmPose(gearWorldCrd));
                        System.out.println("world base    pose" + KittingDemo.dumpPmPose(rcs_robot.BasePose));
                        System.out.println("world baseinv pose" + KittingDemo.dumpPmPose(rcs_robot.BasePoseInv));
                    }
                    //affpoase = Posemath.pmPosePoseMult(affpose, affpose, affpose)rcs_robot.basePoseInv * affpose;
                    gearRobotCrd = new PmPose();

                    Posemath.pmPosePoseMult(rcs_robot.BasePoseInv,
                            gearWorldCrd,
                            gearRobotCrd);

                    if (Globals.bDebug) {
                        System.out.println("robot gear pose" + KittingDemo.dumpPmPose(gearRobotCrd));
                    }

                    // The object gripper offset is where on the object it is to be gripped
                    // e.g., offset.gripper.largegear = 0.0,0.0, -0.030, 0.0, 0.0.,0.0
                    gripperoffset = rcs_robot.GripperOffset.get(_gear.type());

                    bend = rcs_robot.QBend;

                    // The gripperoffset is the robot gripper offset back to the 0T6 equivalent
                    //         pickpose =  tf::Pose(bend, affpose.getOrigin()) * gripperoffset ;
                    Posemath.pmPosePoseMult(new PmPose(gearRobotCrd.tran, bend),
                            new PmPose(gripperoffset.tran, gripperoffset.rot),
                            pickpose);

                    if (Globals.bDebug) {
                        System.out.println("pickpose" + KittingDemo.dumpPmPose(pickpose));
                    }

                    offset = pickpose.tran;
                    // Retract
                    //r.moveTo(rcs_robot.Retract * PmPose(offset, bend));
                    PmPose retractpose = new PmPose();
                    Posemath.pmPosePoseMult(
                            new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot),
                            new PmPose(offset, bend), retractpose);
                    r.moveTo(retractpose);
                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }
                case 2: {
                    // move to grasping posiiton of gear
                    r.moveTo(offset, bend);
                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }
                case 3: {
                    // step: grasp gear
                    r.closeGripper();
                    r.doDwell(r._mygraspdwell);
                    ++state;
                    return state;
                }
                case 4: {
                    // step: retract robot after grasping gear
                    //r.moveTo(rcs_robot.Retract * PmPose(offset, bend));
                    PmPose retractpose = new PmPose();
                    Posemath.pmPosePoseMult(rcs_robot.Retract,
                            new PmPose(offset, bend),
                            retractpose);
                    r.moveTo(retractpose);
                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }

                case 5: {
                    // step: move to approach kitting open slot
                    // Use existing found open kit slot
                    // Offset from the centroid of the kit and reorientation
                    // should be part of inferences
                    //now_instances.findSlot(this._kit, this._openslot.name());

                    CShape.inference_type inference = _kit.findInference(_openslot.name());
                    PmPose slotloc = Globals.convertTranPose(inference.location);
                    if (Globals.bDebug) {
                        System.out.println("kitloc" + dumpPmPose(_kit._location));
                        System.out.println("slotloc" + dumpPmPose(slotloc));
                    }
                    // This xyz already has been reoriented by tray rotation.
                    // CShape openslot = CShapes.findSlot(_kit, _openslot.name());

                    // compute reorient based on kit rotation
                    //PmPose slotloc = openslot._location; // offset of locatino in tray
//                    PmRotationMatrix m = toRotMat(_kit._location);
//                    //        PmCartesian vec_slot = _kit._location.tran +  (m*slotloc.tran);
//                    PmCartesian vec_slot = new PmCartesian();
//                    Posemath.pmMatCartMult(m, slotloc.tran, vec_slot);
//                    PmCartesian tran = Posemath.add(_kit._location.tran, vec_slot);
//                    slotpose = new PmPose(tran, new PmQuaternion(1., 0., 0., 0.));
                    //PmPose slotpose = new PmPose();
                    // slotpose = rcs_robot.basePose.inverse() * slotpose;
                    Posemath.pmPosePoseMult(rcs_robot.BasePoseInv,
                            slotloc,
                            slotpose);
                    if (Globals.bDebug) {
                        System.out.println("slotpose" + dumpPmPose(slotpose));
                    }

                    // up in z only for grasping now - hard coded
                    slotoffset = rcs_world.slotoffset.get("vessel");

                    //placepose = PmPose(bend, slotpose.tran) * slotoffset; // fixme: what if gear rotated
                    Posemath.pmPosePoseMult(new PmPose(slotpose.tran, bend),
                            slotoffset,
                            placepose);
                    if (Globals.bDebug) {
                        System.out.println("slotoffset" + dumpPmPose(slotoffset));
                        System.out.println("placepose" + dumpPmPose(placepose));
                    }
                    offset = placepose.tran; // xyz position

                    // Approach
                    //r.moveTo(rcs_robot.Retract * PmPose(bend, offset));
                    PmPose p = new PmPose();
                    Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot),
                            new PmPose(offset, bend), p);
                    r.moveTo(p);

                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }

                case 6: {
                    // Place the grasped object
                    r.moveTo(new PmPose(offset, bend));
                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }
                case 7: {
                    // open gripper and wait
                    r.openGripper();
                    r.doDwell(r._mygraspdwell);
                    ++state;
                    return state;
                }
                case 8: {
                    // Retract from placed object
                    //r.moveTo(rcs_robot.Retract * PmPose(bend, offset));
                    PmPose p = new PmPose();
                    Posemath.pmPosePoseMult(new PmPose(rcs_robot.Retract.tran, rcs_robot.Retract.rot),
                            new PmPose(offset, bend), p);
                    r.moveTo(p);

                    r.doDwell(r._mydwell);
                    ++state;
                    return state;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        //state = 0;
        return state;
    }

    public boolean isDone(int state) {
        return state >= 9;
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
                //&& (curCmdStatus ==CommandStatusType.CRCL_DONE));
                && strStatus.value().equalsIgnoreCase("CRCL_Done"));
        return bFlag;
    }

    public void start() {

    }

    public void stop() {

    }

    public static void fakeSetup() {
        try {// !!! NOTE    public PmQuaternion(double starts, double startx, double startz, double starty) throws PmException {
            CShapes.storeInstance("sku_kit_m2l1_vessel14", new PmPose(new PmCartesian(0.40, -1.05, 0.92), new PmQuaternion(0.6940, 0.000, -0.720, 0.000)));
            CShapes.storeInstance("sku_kit_m2l1_vessel15", new PmPose(new PmCartesian(0.18, -1.05, 0.92), new PmQuaternion(0.6940, 0.000, -0.720, 0.000)));
            CShapes.storeInstance("sku_medium_gear_vessel16", new PmPose(new PmCartesian(0.19, -1.24, 0.92), new PmQuaternion(1.000, 0.000, 0.017, 0.000)));
            CShapes.storeInstance("sku_part_medium_gear17", new PmPose(new PmCartesian(.23, -1.20, 0.92), new PmQuaternion(1.000, -0.011, 0.022, -0.003)));
            CShapes.storeInstance("sku_part_medium_gear18", new PmPose(new PmCartesian(0.15, -1.20, 0.92), new PmQuaternion(1.000, -0.001, 0.017, -0.000)));
            CShapes.storeInstance("sku_part_medium_gear19", new PmPose(new PmCartesian(0.15, -1.28, 0.92), new PmQuaternion(1.000, 0.001, 0.018, -0.003)));
            CShapes.storeInstance("sku_part_medium_gear20", new PmPose(new PmCartesian(0.23, -1.28, 0.92), new PmQuaternion(1.000, -0.002, 0.012, 0.001)));
            CShapes.storeInstance("sku_large_gear_vessel21", new PmPose(new PmCartesian(0.39, -1.26, 0.92), new PmQuaternion(0.693, 0.000, 0.721, 0.000)));
            CShapes.storeInstance("sku_part_large_gear22", new PmPose(new PmCartesian(0.39, -1.21, 0.92), new PmQuaternion(1.000, 0.002, 0.016, -0.002)));
            CShapes.storeInstance("sku_part_large_gear23", new PmPose(new PmCartesian(0.39, -1.32, 0.92), new PmQuaternion(0.982, -0.002, 0.188, -0.001)));
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
            for (int i = 0; i < instances.size(); i++) {
                if (instances.get(i).isGear()) {
                    CShape gear = instances.get(i);
                    gear._updated = "";
                }
            }

            // reassign instances properties - slots
            for (int i = 0; i < instances.size(); i++) {
                CShape tray;
                // If not a sku skip
                if (!instances.get(i).isSkuPart()) {
                    continue;
                }

                // Don't want a gear - assume gears standalone
                if (instances.get(i).isGear()) {
                    continue;
                }

                // search if one of my robot kits/vessel, if not continue
                // assume only my trays and gears
                // we now have a kit or vessel , i.e., tray
                tray = instances.get(i);

                // clear properties
                instances.get(i).inferences.clear();

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
                    inference.state = "open";

                    PmRotationMatrix m = new PmRotationMatrix();
                    Posemath.pmQuatMatConvert(tray._location.rot, m);
                    PmCartesian rotorigin = new PmCartesian();
                    Posemath.pmMatCartMult(m, slot._location.tran, rotorigin);
                    PmCartesian pos_slot = Posemath.add(tray._location.tran, rotorigin);

                    inference.location = String.format("%7.4f,%7.4f,%7.4f", pos_slot.x, pos_slot.y, pos_slot.z);
                    tray.inferences.add(inference);
                }

                // Now see where gears are located
                for (int j = 0; j < instances.size(); j++) {
                    // Don't want a kit
                    if (instances.get(j).isKit()) {
                        continue;
                    }
                    // don't vant a vessesl
                    if (instances.get(j).isVessel()) {
                        continue;
                    }
                    // If not a sku skip
                    if (!instances.get(j).isSkuPart()) {
                        continue;
                    }

                    // found a gear
                    CShape gear = instances.get(j);

                    // now cycle through the slots in the tray to see if part is in it
                    for (int k = 0; k < slotz._contains.size(); k++) {
                        CShape slot = slotz._contains.get(k);

                        // Position of slot in actual tray - rotation included
                        // slot= tray_centroid + (vessel_rotrayation * slotoffset)
                        //tf::Matrix3x3 m(tray._location.getRotation());
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
            for (int i = 0; i < instances.size(); i++) {
                if (!instances.get(i).isGear()) {
                    continue;
                }

                if (instances.get(i)._updated.isEmpty()) {
                    CShape gear = instances.get(i);
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

    public static CShape closestGear(PmPose location) {
        if (Globals.bDebug) {
            System.out.println("Closest gear location=" + dumpPmPose(location));
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
                Posemath.pmPosePoseMult(rcs_robot.BasePoseInv,
                        shape._location,
                        shapeRobotCrd);
                PmCartesian pos_part = shapeRobotCrd.tran;
                if (Globals.bDebug) {
                    System.out.println("Gear " + shape.name() + "location=" + dumpPmPose(shapeRobotCrd));
                }

                // Compute distance between slot and part
                double mag = Posemath.mag(Posemath.subtract(pos_move, pos_part));

                // is gear close enough pick it, random distance threshold
                // fixme: should I search all gears and pick one closest...
                if (Math.abs(mag) < 0.025) {
                    try {
                        gear = (CShape) shape.clone();
                        System.out.println("Closest Gear to commanded location" + shape.name());

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

        s = String.format("%7.4f, ", pose.tran.x)
                + String.format("%7.4f, ", pose.tran.y)
                + String.format("%7.4f ", pose.tran.z)
                + String.format("%7.4f, ", pose.rot.x)
                + String.format("%7.4f, ", pose.rot.y)
                + String.format("%7.4f, ", pose.rot.z)
                + String.format("%7.4f, ", pose.rot.s);
        return s;
    }

    public static String dumpInstances() {
        String ss = "";
        for (CShape instance : CShapes.instances) {
            ss += instance.name() + " at " + dumpPmPose(instance._location) + "\n";
        }
        return ss;
    }

    public static String dumpInferences() {
        Vector<CShape> now_instances = CShapes.snapshotInstances();

        String ss = "";
        for (int i = 0; i < now_instances.size(); i++) {
            CShape instance = now_instances.get(i);

            ss += instance.name() + " at " + KittingDemo.dumpPmPose(instance._location) + "\n";

            if (instance.isGear()) {
                CShape.inference_type inference = instance.inferences.get(0);

                ss += "\tIn: " + inference.parent
                        + "(" + inference.slot + ")\n";
                continue;
            }

            // either kit or vessel - now do slots
            CShape slotz = findDefinition(instance.type());

            if (slotz == null) {
                continue;
            }

            for (int j = 0; j < slotz._contains.size(); j++) {
                CShape slot = slotz._contains.get(j);
                CShape.inference_type inference = instance.findInference(slot.name());
                if (inference == null) {
                    continue;
                }
                ss += "\t" + inference.name + " "
                        + inference.type + " "
                        + inference.state + " ("
                        + inference.location + ")\n";
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

            //m_origin += m_basis * t.m_origin;
            PmCartesian res = new PmCartesian();
            Posemath.pmMatCartMult(arot, b.tran, res);
            PmCartesian tran = Posemath.add(a.tran, res);

            //m_basis *= t.m_basis
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
    CRCLClient r;
    // Demo primary objects
    public static CShape _gear;  // free gear in supply tray
    public static CShape _kit;    // kit with open slot
    public static CShape _openslot;  // open slot in kit

    // Demo state varaibles
    PmPose pickpose = new PmPose();
    String gearname;
    //PmPose affpose = new PmPose();
    PmPose gearRobotCrd;
    PmPose gearWorldCrd;
    PmPose gripperoffset = new PmPose();
    PmQuaternion bend = rcs_robot.QBend;
    PmCartesian offset = new PmCartesian();
    PmPose slotpose = new PmPose();
    PmPose slotoffset = new PmPose();
    PmPose placepose = new PmPose();
}
