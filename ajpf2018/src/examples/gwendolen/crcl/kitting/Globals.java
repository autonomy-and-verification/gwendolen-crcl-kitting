/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting;

import java.util.concurrent.locks.ReentrantLock;

import crcl.base.*;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.PmPose;
import rcs.posemath.PmQuaternion;
import java.io.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.Calendar;
import java.util.GregorianCalendar;
/**
 * Globals is a wrapper for global flags and other general purpose utilities.
 *
 * @author michaloski
 * @version 1.0
 */
public class Globals {
	/**
	 * type of CRCL IK solver. Effects how Orientation is modeled.
	 * Choices so far are IKFAST or KDL
	 */
	public static String IkSolver="KDL";
	
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
    
//	public static Logger myLogger=Logger.getLogger(CRCLClient.class.getName());
//	public static Level myLevel;

	public static Logger loggerInit(String loggername) {
		Logger myLogger = Logger.getLogger(loggername);
		String crcllogname;
		
		// create logs folder if doesn't exist
		File dir = new File(System.getProperty("user.dir") + "\\logs");
		boolean bCreated=dir.mkdir();

		if(loggername.equals("crcl"))
		{
			crcllogname = System.getProperty("user.dir") + "\\logs\\CrclWorldModel.log";			
		}
		else if(loggername.equals("gwen"))
		{
			crcllogname = System.getProperty("user.dir") + "\\logs\\GwenTrace.log";			
		}
		else
		{	
			crcllogname = System.getProperty("user.dir") + "\\logs\\CrclTrace.log";			
		}
		try {

			myLogger.setLevel(Level.FINE);
			FileHandler handler = new FileHandler(crcllogname);
			//handler.setFormatter(new java.util.logging.SimpleFormatter());
			handler.setLevel(Level.FINEST);
			
			handler.setFormatter(new Formatter() {
	            @Override
	            public String format(LogRecord record) {
	                SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                Calendar cal = new GregorianCalendar();
	                cal.setTimeInMillis(record.getMillis());
	                return logTime.format(cal.getTime())
	                        + " || "
	                        + record.getSourceClassName().substring(
	                                record.getSourceClassName().lastIndexOf(".")+1,
	                                record.getSourceClassName().length())
	                        + "."
	                        + record.getSourceMethodName()
	                        + "() : "
	                        + record.getMessage() + "\n";
	            }
	        });
			
			myLogger.addHandler(handler);
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		return myLogger;
	}
	public static Logger leanLoggerInit(String loggername) {
		Logger myLogger = Logger.getLogger(loggername);
		String crcllogname;
		
		// create logs folder if doesn't exist
		File dir = new File(System.getProperty("user.dir") + "\\logs");
		boolean bCreated=dir.mkdir();

		if(loggername.equals("crcl"))
		{
			crcllogname = System.getProperty("user.dir") + "\\logs\\CrclWorldModel.log";			
		}
		else if(loggername.equals("gwen"))
		{
			crcllogname = System.getProperty("user.dir") + "\\logs\\GwenTrace.log";			
		}
		else
		{	
			crcllogname = System.getProperty("user.dir") + "\\logs\\CrclTrace.log";			
		}
		try {

			myLogger.setLevel(Level.FINE);
			FileHandler handler = new FileHandler(crcllogname);
			//handler.setFormatter(new java.util.logging.SimpleFormatter());
			handler.setLevel(Level.FINEST);
			
			handler.setFormatter(new Formatter() {
	            @Override
	            public String format(LogRecord record) {
	                return record.getMessage() + "\n";
	            }
	        });
			
			myLogger.addHandler(handler);
		} catch (Exception ex) {
			Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		return myLogger;
	}
}
