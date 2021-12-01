/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting;
import crcl.base.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.*;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmQuaternion;
/**
 * Basic definitions of the robot that will be manipulated. 
 * Contains definitions for robot base offset from the world
 * origin (0,0,0), grasping offsets, bend pose for the robot
 * to grasp the gears, and a approach/retract amount for
 * grasp/release pre/post operation.
 * @author michaloski
 */
public  class rcs_robot {
    
    /**
     * instad of reading a configuration file, these are the 
     * hared coded definitions of the gazebo kitting simulation 
     * for the Fanuc LRMate robot. All commands are cartesian pose
     * but must be inte the robot coordinate frame, not the world frame.
     */
    public static void hardcode() {
        try {
            Retract = new PmPose(new PmCartesian(0.0, 0.0, 0.04), new PmQuaternion(1.0, 0.0, 0.0, 0.0));
            RetractInv = rcs.posemath.Posemath.inv(new PM_POSE(Retract.tran, Retract.rot));
            BasePose = new PmPose(new PmCartesian(-0.169, -1.140, 0.934191), new PmQuaternion(1.0, 0.0, 0.0, 0.0));
            BasePoseInv = rcs.posemath.Posemath.inv(new PM_POSE(BasePose.tran, BasePose.rot));
			if (Globals.IkSolver == "IKFAST")
				QBend = new PmQuaternion(0.0, 1.0, 0.0, 0.0);
			else
				QBend = new PmQuaternion(0, 0.707107, 0, 0.707107);
            GripperOffset.put("sku_part_large_gear", 
                    new PmPose(new PmCartesian( 0.0, 0.0, -0.015),
                    new PmQuaternion(1.0, 0.0, 0.0, 0.0)));
            GripperOffset.put("sku_part_medium_gear", 
                    new PmPose(new PmCartesian( 0.0, 0.0, -0.015),
                    new PmQuaternion(1.0, 0.0, 0.0, 0.0)));
            GripperOffset.put("sku_part_small_gear", 
                    new PmPose(new PmCartesian( 0.0, 0.0, -0.015),
                    new PmQuaternion(1.0, 0.0, 0.0, 0.0)));
            
        } catch (Exception ex) {
            Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     /**
     *  isBusy read the current status to determine if
     * the robot is still busy executing a command
     * @return true if busy
     */
    public static boolean isBusy()
    {
        // FIXME: read crcl status for done
        return false;
    }

    

    // Canned poses
    /** pose offset for retract */
    public static PmPose Retract;        
     /** inverse pose offset for retract */
    public static PmPose RetractInv;
    /**rotation to achieve pose rotation for grasping */
    public static PmQuaternion QBend; 
    //public static PmPose currentPose;
    /** robot base pose offset from world origin*/
    public static PmPose BasePose;
    /** inverse of robot base pose offset from world origin*/
    public static PmPose BasePoseInv;
    /** map gripper offset for different gear types. key is the 
     size of the gear: sm, med, lg*/
    public static java.util.Map<String, PmPose> GripperOffset= new  HashMap<>();       
/// gripper offset for each part


    // CRCL status numbers
    /** crcl current status command number*/
    public static long curStatusCmdId;
    /** crcl status of command*/
    public static CommandStateEnumType crclCommandStatus;
    /** last crcl command number used */
    public static long latestCmdId;
}
