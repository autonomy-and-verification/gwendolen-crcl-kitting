package gwendolen.crcl.kitting;

import rcs.posemath.*;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmQuaternion;

import ail.mas.DefaultEnvironment;
import ail.semantics.AILAgent;
import ail.syntax.Action;
import ail.syntax.BeliefBase;
import ail.syntax.ListTerm;
import ail.syntax.Literal;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILConfig;
import ail.util.AILexception;
import ajpf.psl.MCAPLTerm;
import ajpf.util.AJPFLogger;
import ajpf.util.VerifyMap;
import ajpf.util.VerifySet;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import rcs.posemath.*;
import static gwendolen.crcl.kitting.CShapes.*;
import static gwendolen.crcl.kitting.KittingInterface.dumpInferences;
import static gwendolen.crcl.kitting.KittingInterface.dumpInstances;
import static gwendolen.crcl.kitting.KittingInterface.fakeFirstOrderLogic;
import static gwendolen.crcl.kitting.CRCLClient.myLogger;
import java.util.Properties;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Inherited implementation of DefaultEnvironment/AILEnv. Implements AILEnv which is an interface to be satisfied 
 * by any environment that is to interact with the AIL classes. The class KittingEnv is a 
 * provides an ``action'' \gwendolen\ call which  connects  \gwendolen\  plans to the  
 * physical ``real''  world. In essence \gwendolen\ does goal-directed planning using a belief 
 * system in conjunction with the real world beliefs to in order to run a plan.
 * 
 * @author michalos
 *
 */
public class KittingEnv extends DefaultEnvironment implements Runnable {
	static final String logname = "gwendolen.crcl.kitting.KittingEnv";
	static boolean bPrintWorldDone = false;
	public static Logger gwenLogger = null;
	public static Level myLevel = Level.INFO;

	// Variables from CRCL Kitting operation
	public CRCLClient r;
	public KittingInterface crcl;
	public PmPose pickpose;
	public String gearname;
	public Predicate gearPredicate;
	public Predicate action_resPredicate ;
	public String last_action_result="";
	// public Predicate humanProximityViolationPredicate;
	public boolean bHumanWSViolation = true;
	public boolean bPlanning = false;
	public Thread myWorkerWSViolationThread;
	public long nCountdown = (long)(10 + new Random().nextInt(15)); // 25;
	public long nGearDrop = (long)(new Random().nextInt(5));

	public Map<String, Predicate> predicatemap = new VerifyMap<String, Predicate>();

	public KittingEnv() {
		super();
		
	}

	/**
	 * canReach determines whether an object such as a gear is reachable and thus in
	 * the robot workspace.
	 * 
	 * @param x of the object position in world coordinate space
	 * @param y of the object position in world coordinate space
	 * @param z of the object position in world coordinate space
	 * @return true if robot can reach object, otherwise false
	 */

	public boolean canReachWorld(double x, double y, double z) {
		// hard code fanuc reachability numbers
		double robotLength = 1.1631; // robot fully extended + 1/2 gripper length in meters
		double elevation = 0.33; // robot on pedastal on table, not flat
		double robotReach = Math.sqrt(robotLength * robotLength - elevation * elevation);
		PmCartesian Base = new PmCartesian(-0.169, -1.140, 0.934191);
		PmCartesian Reach = new PmCartesian(x, y, z);
		PmCartesian offset = Reach.subtract(Base);
		double dist = offset.mag();
		if (dist < robotReach)
			return true;
		return false;

	}

	/**
	 * Thread to detect human Proximity Violation. NOT USED OR TESTED.
	 */
	public void run() {
		try {
			while (bPlanning) {
				if (bHumanWSViolation) {
					System.out.println("Now the thread is running ...");
					Predicate p = predicatemap.get("humanProximityViolation");
					if (p == null)
						p = predicatemap.put("humanProximityViolation", new Predicate("humanProximityViolation"));
					addPercept(p);
					bHumanWSViolation = false;
				}
				Thread.sleep(1000 * 5);
			}
		} catch (Exception ex) {
			Logger.getLogger(KittingEnv.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Override of DefaultEnvironment begin() statement
	 */
	public void begin() {
		try {
			super.begin();

			// test canReachWorld
			boolean bTrue = canReachWorld(0.0, 0.0, 0.0); // false
			bTrue = canReachWorld(0.19, -1.24, 0.92); // true
			// 0.1900, -1.4400, 0.9200

			gwendolen.crcl.kitting.Globals.bDebug = true;
			gwendolen.crcl.kitting.Globals.bLoopback = true; // loopback - ignore crcl socket command

			CShapes.initDefinitions(); // define gears trays object properties
			rcs_robot.hardcode(); // define robot
			rcs_world.hardcode();

			Globals.bReadAllInstances = true;
			r = new gwendolen.crcl.kitting.CRCLClient();
			crcl = new gwendolen.crcl.kitting.KittingInterface(r);
			pickpose = new PmPose();

			System.out.println("Environment started.");

			if (gwenLogger == null) {
				gwenLogger = Globals.leanLoggerInit("gwen");
				gwenLogger.log(myLevel, "START GWENDOLEN TRACE\n");
			}
			bPlanning = true;
			// myWorkerWSViolationThread = new Thread(this);
			// myWorkerWSViolationThread.start();
			
			action_resPredicate = new Predicate("action_result");

		} catch (Exception ex) {
			Logger.getLogger(KittingEnv.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Override of DefaultEnvironment cleanup() statement
	 */
	public void cleanup() {
		try {
			super.cleanup();
			myWorkerWSViolationThread.stop();
		} catch (Exception ex) {
			Logger.getLogger(KittingEnv.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Override Defaultenvironment configure method to read the AILConfig.
	 */
	public void configure(AILConfig config) {
		super.configure(config);
	}

	/**
	 * Add environment predicate. Save in predicate map for later deletion.
	 * @param name of belief
	 */
	public void addMappedBelief(String name) {
		Predicate p = predicatemap.get(name);
		if (p == null)
			p = predicatemap.put(name, new Predicate(name));
		addPercept(p);
	}
	/**
	 * If name of belief is found predicate map delete.
	 * @param name of belief
	 */
	public void removeMappedBelief(String name) {
		Predicate p = predicatemap.get(name);
		if (p != null)
			removePercept(p);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ail.mas.DefaultEnvironment#executeAction(java.lang.String,
	 * ail.syntax.Action)
	 */
	public Unifier executeAction(String agName, Action act) throws AILexception {
		String actionname = act.getFunctor();

		if (bHumanWSViolation && nCountdown < 0) {
			addMappedBelief("humanProximityViolation");
			bHumanWSViolation = false;
		}
		nCountdown = nCountdown - 1;

		if (!actionname.equals("trace")) {
			String args = new String("null");
			if (act != null) {
				if (act.getTerm(0).isString())
					args = ((StringTerm) act.getTerm(0)).toString();
				if (act.getTerm(0).isNumeric())
					args = ((NumberTerm) act.getTerm(0)).toString();
			}
			if (gwenLogger != null) {
				gwenLogger.log(myLevel, "\t" + actionname + "(" + args.toString() + ")");
			}
		}
		boolean action_result = false;
		System.out.println("Environment is simulating action: " + actionname);

		/**
		 * THIS BELIEF PRUNES TYPES OF GEAR SIZES "B gear_tray(IdGearTray, Size, Slots)"
		 * 
		 * +!kitting(Id, Size) [perform] : { ~B grasped(_), B gear_tray(IdGearTray,
		 * Size, Slots) } <- print("kitting"), find_gear(Slots),
		 */
		if (actionname.equals("find_gear")) {
			ListTerm slots = (ListTerm) act.getTerm(0);
			for (Term slot : slots) {
				// No gear has empty assigned as a property
				// instead will check to see if in tray
				if (crcl.gearInSupplyTray(slot.toString()) > 0) {
					gearPredicate = new Predicate("gear");
					gearPredicate.addTerm(slot);
					addPercept(gearPredicate);
					break;
				}
			}
		}

		else if (actionname.equals("find_slot")) {
			ListTerm slots = (ListTerm) act.getTerm(0);
			for (Term slot : slots) {
				for (MCAPLTerm slotpar : slot.getTerms()) {
					if (!slotpar.toString().contains("empty")) {
						addPercept((Predicate) slot);
					}
				}
			}
		}

		else if (actionname.equals("place_part")) {
			ListTerm locations = (ListTerm) act.getTerm(0);
			if (crcl.isGear(locations.toString()))
				gearname = locations.toString();

			System.out.println("Action: " + actionname + " location=" + locations.toString());
			// move to location where location indicates gear_name or kit_name.slot#
			// and for now is really a grasp or release bundled under move actionname.
			crcl.move(locations.toString());

			if (!crcl.isGear(locations.toString())) {
				if (gearPredicate != null)
					removePercept(gearPredicate);
			}

			action_result = true;
			if (!action_result) {
				System.out.println("Action: " + actionname + " has failed!");
			}
		} else if (actionname.equals("reachable_gear")) {
			ListTerm locations = (ListTerm) act.getTerm(0);
			System.out.println("Action: " + actionname + " location=" + locations.toString());
			
			if (crcl.isGear(locations.toString()))
				gearname = locations.toString();
			
			PmCartesian gear_location= crcl.getGearLocation(gearname, true);
		
			if(!canReachWorld(gear_location.x, gear_location.y, gear_location.z))
			{
				// Signal dropped gear
				reportActionResult("abortGear");
			}
			reportActionResult("reachableGear");
			return super.executeAction(agName, act);			
		} else if (actionname.equals("take_part")) {

			// action_result = simulate_action(0.9, 1000);
			ListTerm locations = (ListTerm) act.getTerm(0);
			System.out.println("Action: " + actionname + " location=" + locations.toString());
			
			if (crcl.isGear(locations.toString()))
				gearname = locations.toString();
			
			//FIXME: fail is not gear

			if(this.nGearDrop==0)
			{
				//simulate drop by just moving gear to another place on table.
				// Fixme: in gazebo this could be a what? an open gear while moving.
				crcl.setGearLocation(gearname, new PmCartesian(0.1900, -1.4400, 0.9200), true);
							
				// Report failure belief
				reportActionResult("droppedGear");
				nGearDrop=nGearDrop-1;
				return super.executeAction(agName, act);
			}
			nGearDrop=nGearDrop-1;
			
			// move to location where location indicates gear_name or kit_name.slot#
			// and for now is really a grasp or release bundled under move actionname.
			crcl.move(locations.toString());

			if (!crcl.isGear(locations.toString())) {
				if (gearPredicate != null)
					removePercept(gearPredicate);
			}

			action_result = true;
			if (!action_result) {
				System.out.println("Action: " + actionname + " has failed!");
			}

		} else if (actionname.equals("wait")) {
			NumberTerm time = (NumberTerm) act.getTerm(0);
			try {
				Thread.sleep((int) time.solve());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (actionname.equals("print")) {
			StringTerm args = (StringTerm) act.getTerm(0);
			System.out.println("Action: " + actionname + " args=" + args.toString());
			System.out.println("List of things that all agents can perceive");
			System.out.println(percepts.toString());
			System.out.println("AgentMap");
			System.out.println(agentmap.get(agName).toString());

		} 
		// This removes a named belief from the environment which CANNOT be removed
		else if (actionname.equals("remove_belief")) {
			StringTerm args = (StringTerm) act.getTerm(0);
			
			removeMappedBelief(args.toString());
		}

		else if (actionname.equals("trace")) {
			StringTerm args = (StringTerm) act.getTerm(0);
			if (gwenLogger != null)
				gwenLogger.log(myLevel, args.toString());

		} else if (actionname.equals("printPercepts")) {
			Set<Predicate> percepts = this.getPercepts("robot", false);
			if (percepts != null) {
				System.out.println("List of things that all agents can perceive");
				System.out.println(percepts.toString());
				gwenLogger.log(myLevel, "List of things that all agents can perceive\n" + percepts.toString());
				gwenLogger.log(myLevel, agentmap.get(agName).toString());
			}
		} else if (actionname.equals("printGwen")) {
			gwenLogger.log(myLevel, agentmap.get(agName).toString());

		} else if (actionname.equals("printWorld")) {
			if (!bPrintWorldDone) {
				bPrintWorldDone = true;
				myLogger.log(Level.FINEST, crcl.dumpKittingSetup());
			}
		}
		
		// Add 
		if (!actionname.equals("print") && !actionname.equals("minus") && !actionname.equals("sum")
				&& !actionname.equals("find_gear") && !actionname.equals("find_slot") && !actionname.equals("wait")) {
			
			if(action_result)
				reportActionResult("true");
			else
				reportActionResult("false");
		}
		// act.setLogLevel(AJPFLogger.FINER);

		return super.executeAction(agName, act);

	}

	public boolean simulate_action(double prob, int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new Random().nextDouble() <= prob;
	}

	public void reportActionResult(String action_result)
	{
		if (!last_action_result.isEmpty()) {
			Predicate action_resPredicate = new Predicate("action_result");
			action_resPredicate.addTerm(new StringTermImpl(last_action_result));
			removePercept(action_resPredicate);
		}

		last_action_result = action_result;
		
		Predicate action_resPredicate = new Predicate("action_result");
		action_resPredicate.addTerm(new StringTermImpl(action_result));
		addPercept(action_resPredicate);
	}
//	@Override
//	public boolean done() {
//		return false;
//	}

}
