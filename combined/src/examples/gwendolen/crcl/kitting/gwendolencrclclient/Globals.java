/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting.gwendolencrclclient;

import java.util.concurrent.locks.ReentrantLock;
import crcl.base.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.PmPose;
import rcs.posemath.PmQuaternion;

/**
 * Globals is a wrapper for global flags and other general purpose utilities.
 *
 * @author michaloki
 * @version 1.0
 */
public class Globals {

    public static boolean bDebug=false;
    /**
     * a flag to signal if all the object instances in the model status have
     * been read at least once.
     */
    public static boolean bReadAllInstances;
    /**
     * a flag to signal a non-crcl sockdt reading application - uses self
     * inferencing and "smart" object guesses as to gear picked up and placed in
     * an open kitting slot.
     */
    public static boolean bLoopback = true;

    /**
     * latest command id sent in crcl command
     */
    public static long latestCmdId;

    /**
     * current status command id returned from CRCL socket report.
     */
    public static long curStatusCmdId;
    /**
     * current crcl command status.
     */
    public static CommandStateEnumType crclCommandStatus;

    /**
     * mutex for thread safe updating
     */
    public static ReentrantLock mutex = new ReentrantLock();

    /**
     * convertTranPose accept a string with three doubles and uses these doubles
     * as the translation. It creates a pose using this translation with zero
     * rotation.
     *
     * @param tran string with 3 comma separated doubles
     * @return a PmPose containing the trans or null if the string was illegal.
     */
    public static PmPose convertTranPose(String tran) {
        PmPose p = new PmPose();
        try {
            StringTokenizer st = new StringTokenizer(tran, ",");
            p.tran.x = Double.parseDouble(st.nextToken());
            p.tran.y = Double.parseDouble(st.nextToken());
            p.tran.z = Double.parseDouble(st.nextToken());
            p.rot = new PmQuaternion(1., 0.0, 0.0, 0.0);
        } catch (Exception ex) {
            Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return p;
    }

}
