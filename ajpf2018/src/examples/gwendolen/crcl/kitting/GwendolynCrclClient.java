/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting;
// Pose math

import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmQuaternion;

// https://www.programcreek.com/java-api-examples/org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.*;


/**
 * GwendolynCrclClient is the main class.
 * @author michalos
 */
public class GwendolynCrclClient {
   

    public static CRCLClient crcl;

    /**
     * main method. sets up the glbal flags and '
     * if a "fake" crcl then calls all the fake 
     * setups. Once setup runs through the
     * kitting demo until done.
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        
//        // Configuration options
//        Globals.bDebug = false;  // no in depth diagnostics
//        Globals.bLoopback = false; // crcl loopback
//
//        Options options = new Options();
//        options.addOption("l", "loopback", false, "loopback crcl behavior");
//        options.addOption("d", "debug", false, "debug statements");
//        options.addOption("h", "help", false, "print usage");
//        
//        CommandLineParser parser = new DefaultParser();
//
//        CommandLine commandLine = null;
//        try {
//            commandLine = parser.parse(options, args);
//            if (commandLine.hasOption('l')) {
//                Globals.bLoopback = true;
//            }
//            if (commandLine.hasOption('d')) {
//                Globals.bDebug = true;
//            }
//            if (commandLine.hasOption('h')) {
//            new HelpFormatter().printHelp("java -jar GwendolynCrclClient.jar [-d] [-h] [-l]", options,true);
//            System.exit(0);
//        }
//        } catch (ParseException e) {
//            Logger.getLogger(GwendolynCrclClient.class.getName()).log(Level.SEVERE, "Unable to parse command line.", e);
//            new HelpFormatter().printHelp("GwendolynCrclClient [args]", options);
//            System.exit(-1);
//        }
//     
//        try {
//
//            // TODO code application logic here
//            CShapes.initDefinitions();  // define gears trays object properties
//            // Seems to work
//            //System.out.print(KittingDemo.dumpDefinitions());
//            rcs_robot.hardcode();  // define robot
//            rcs_world.hardcode();
//
//            crcl = new CRCLClient();
//
////            KittingDemo kittingdemo = new KittingDemo(crcl);
//// 
////            int demostate = 0;
////            int bozo;
//
//            while(true) {
//                CRCLClient.action_type action= new CRCLClient.action_type ();
//                if(crcl.acquire(action)<0)
//                    break;
//                if(crcl.takepart(action.gear)<0)
//                    break;
//                if(crcl.placepart(action.gear, action.kit, action.openslot)<0)
//                    break;
//                Thread.sleep(50);
//            }
////                if (kittingdemo.isDone(demostate)) {
////                    demostate = 0;
////                }
////                bozo = kittingdemo.issueRobotCommands(demostate);
////                demostate = bozo;
////                Thread.sleep(50);
////            } while (demostate >= 0);
//        } catch (Exception ex) {
//            Logger.getLogger(GwendolynCrclClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

}
