/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting.gwendolencrclclient;

import crcl.base.CRCLStatusType;
import crcl.base.CommandStateEnumType;
import crcl.base.CommandStatusType;
import crcl.base.GetStatusType;
import crcl.base.InitCanonType;
import crcl.base.JointStatusType;
import crcl.base.JointStatusesType;
import crcl.base.MessageType;
import crcl.base.MoveToType;
import crcl.base.PointType;
import crcl.base.PoseType;
import static crcl.utils.CRCLPosemath.point;
import static crcl.utils.CRCLPosemath.pose;
import static crcl.utils.CRCLPosemath.vector;
import static gwendolen.crcl.kitting.gwendolencrclclient.CRCLClient.incrementId;
import static gwendolen.crcl.kitting.gwendolencrclclient.GwendolynCrclClient.crcl;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmQuaternion;

/**
 * A smattering of  methods to test the crcl communication
 * as well as lower level simulation validaity.
 * @author michaloski
 */
public class TestCases {

    /**
     * simple test of crcl communication reading commands and reading status.
     * Shows how to build a command message and how to parse a crcl status
     * report.
     *
     * @param crcl
     */
    public void test(CRCLClient crcl) {
        try {

            // Create and send init command.
            InitCanonType init = new InitCanonType();
            init.setCommandID(incrementId(1));
            crcl.instance.setCRCLCommand(init);
            crcl.issueCrclCommand();

            // Create and send MoveTo command.
            MoveToType moveTo = new MoveToType();
            moveTo.setCommandID(incrementId(1));
            PoseType pose = pose(point(0.65, 0.05, 0.15), vector(1, 0, 0), vector(0, 0, 1));
            moveTo.setEndPosition(pose);
            moveTo.setMoveStraight(false);
            crcl.instance.setCRCLCommand(moveTo);
            // true is additional validation
            //s.writeCommand(instance, true);
            crcl.issueCrclCommand();

            MessageType message = new MessageType();
            message.setCommandID(incrementId(1));
            message.setMessage("some message");
            long IDback = 1;
            CommandStatusType cmdStat = null;

            do {
                // Create and send getStatus request.
                GetStatusType getStat = new GetStatusType();
                getStat.setCommandID(incrementId(1));
                crcl.instance.setCRCLCommand(getStat);
                crcl.issueCrclCommand();

                // Read status from server
                CRCLStatusType stat = crcl.s.readStatus();

                // Print out the status details.
                cmdStat = stat.getCommandStatus();
                IDback = cmdStat.getCommandID();
                System.out.println("Status:");
                System.out.println("CommandID = " + IDback);
                System.out.println("State = " + cmdStat.getCommandState());
                PointType pt = stat.getPoseStatus().getPose().getPoint();
                System.out.println("pose = " + pt.getX() + "," + pt.getY() + "," + pt.getZ());
                JointStatusesType jst = stat.getJointStatuses();
                if (null != jst) {
                    List<JointStatusType> l = jst.getJointStatus();
                    System.out.println("Joints:");
                    for (JointStatusType js : l) {
                        System.out.println("Num=" + js.getJointNumber() + " Pos=" + js.getJointPosition());
                    }
                }
            } while (!(IDback == moveTo.getCommandID()) || cmdStat.getCommandState().equals(CommandStateEnumType.CRCL_WORKING));
        } catch (Exception ex) {
            Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * perform a hard coded kitting sequence for the fanuc robot
     * @param crcl pointer to the crcl client.
     */
    public static void fanuckitting(CRCLClient crcl) {
        try {
            crcl.init();
            crcl.setLengthUnitsType("METER");

            System.out.println("move = 0.40, -0.06, 0.05 ");
            // THese motions are all relative to the base origin of the robot,
            // which is 0,0,0 mapped from 0.169, -1.140, 0.934191

            crcl.moveTo(new PmCartesian(0.40, -0.06, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.40, -0.06, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.0);
            crcl.doDwell(2.0);
            crcl.moveTo(new PmCartesian(0.40, -0.06, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.41, 0.04, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.41, 0.04, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.moveTo(new PmCartesian(0.41, 0.04, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.32, -0.06, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.32, -0.06, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.);
            crcl.doDwell(2.);

            crcl.moveTo(new PmCartesian(0.41, 0.12, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.41, 0.12, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.41, 0.12, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.32, -0.14, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.32, -0.14, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.32, -0.14, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.62, 0.05, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.62, 0.05, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.62, 0.05, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.40, -0.14, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.40, -0.14, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.40, -0.14, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.62, 0.13, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.62, 0.13, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.62, 0.13, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.56, -0.07, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.56, -0.07, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(.56, -0.07, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.53, 0.10, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.53, 0.10, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.53, 0.10, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.56, -0.18, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.56, -0.18, 0.01),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(0.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.56, -0.18, 0.05),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.31, 0.09, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.moveTo(new PmCartesian(0.31, 0.09, 0.02),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);
            crcl.setGripper(1.);
            crcl.doDwell(2.);
            crcl.moveTo(new PmCartesian(0.31, 0.09, 0.06),
                    new PmQuaternion(0.000, 1.000, 0.000, 0.000),
                    false);
            crcl.doDwell(0.1);

        } catch (Exception ex) {
            Logger.getLogger(GwendolynCrclClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
