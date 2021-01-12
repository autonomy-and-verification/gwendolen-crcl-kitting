package gwendolen.crcl.kitting;

import ail.mas.DefaultEnvironment;
import ail.syntax.Action;
import ail.syntax.ListTerm;
import ail.syntax.ListTermImpl;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.psl.MCAPLListTerm;
import ajpf.psl.MCAPLTerm;
import gwendolen.crcl.kitting.gwendolencrclclient.CRCLClient;
import gwendolen.crcl.kitting.gwendolencrclclient.CShapes;
import gwendolen.crcl.kitting.gwendolencrclclient.Globals;
import gwendolen.crcl.kitting.gwendolencrclclient.GwendolynCrclClient;
import gwendolen.crcl.kitting.gwendolencrclclient.KittingDemo;
import gwendolen.crcl.kitting.gwendolencrclclient.rcs_robot;
import gwendolen.crcl.kitting.gwendolencrclclient.rcs_world;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;



public class KittingEnv extends DefaultEnvironment{
	static final String logname = "gwendolen.crcl.kitting.KittingEnv";
	
	public static CRCLClient crcl;

	
	Predicate gear = new Predicate("gear");
	Predicate gripper = new Predicate("gripper");
	Predicate action_res = new Predicate("action_result");
	Predicate grasped = new Predicate("grasped");
	
	Predicate gear_tray = new Predicate("gear_tray");
	
	public KittingEnv() {
		super();
	    Globals.bDebug = false;  // no in depth diagnostics
	    Globals.bLoopback = true; // crcl loopback
		
		gripper.addTerm(new StringTermImpl("open"));
		addPercept(gripper);
		
		gear_tray.addTerm(new StringTermImpl("gear_tray_1"));
		gear_tray.addTerm(new StringTermImpl("small"));
		ListTerm slots = new ListTermImpl();
		slots.add(new StringTermImpl("gear_small_3"));
		slots.add(new StringTermImpl("gear_small_2"));
		slots.add(new StringTermImpl("gear_small_1"));
		gear_tray.addTerm(slots);
		addPercept(gear_tray);
		
		gear_tray = new Predicate("gear_tray");
		gear_tray.addTerm(new StringTermImpl("gear_tray_2"));
		gear_tray.addTerm(new StringTermImpl("medium"));
		slots = new ListTermImpl();
		slots.add(new StringTermImpl("gear_medium_3"));
		slots.add(new StringTermImpl("gear_medium_2"));
		slots.add(new StringTermImpl("gear_medium_1"));
		gear_tray.addTerm(slots);
		addPercept(gear_tray);
		
		gear_tray = new Predicate("gear_tray");
		gear_tray.addTerm(new StringTermImpl("gear_tray_3"));
		gear_tray.addTerm(new StringTermImpl("large"));
		slots = new ListTermImpl();
		slots.add(new StringTermImpl("gear_large_3"));
		slots.add(new StringTermImpl("gear_large_2"));
		slots.add(new StringTermImpl("gear_large_1"));
		gear_tray.addTerm(slots);
		addPercept(gear_tray);
		
		System.out.println("Environment started.");
		Thread t1 = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	try {

		            // TODO code application logic here
		            CShapes.initDefinitions();  // define gears trays object properties
		            // Seems to work
		            //System.out.print(KittingDemo.dumpDefinitions());
		            rcs_robot.hardcode();  // define robot
		            rcs_world.hardcode();

		            crcl = new CRCLClient();

		            KittingDemo kittingdemo = new KittingDemo(crcl);
		 
		            int demostate = 0;
		            int bozo;

		            do {
		                if (kittingdemo.isDone(demostate)) {
		                    demostate = 0;
		                }
		                bozo = kittingdemo.issueRobotCommands(demostate);
		                demostate = bozo;
		                Thread.sleep(50);
		            } while (demostate >= 0);
		        } catch (Exception ex) {
		            Logger.getLogger(GwendolynCrclClient.class.getName()).log(Level.SEVERE, null, ex);
		        }
		    }
		});  
		t1.start();
        
		
//		Predicate kit_quantity = new Predicate("kit_quantity");
//		kit_quantity.addTerm(new StringTermImpl("s2b2"));
//		kit_quantity.addTerm(new NumberTermImpl(0));
//		kit_quantity.addTerm(new NumberTermImpl(0));
//		kit_quantity.addTerm(new NumberTermImpl(0));
//		addPercept(kit_quantity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ail.mas.DefaultEnvironment#executeAction(java.lang.String, ail.syntax.Action)
	 */
	public Unifier executeAction(String agName, Action act) throws AILexception {
		String actionname = act.getFunctor();
		boolean action_result = false;
		System.out.println("Environment is simulating action: "+actionname);
//		clearPercepts();
		if (actionname.equals("find_gear")) {
			StringTerm id =  (StringTerm) act.getTerm(0);
			StringTerm size =  (StringTerm) act.getTerm(1);
			ListTerm slots = (ListTerm) act.getTerm(2);
			for (Term slot : slots) {
				if (!slot.toString().contains("empty")) {
					gear_tray = new Predicate("gear_tray");
					gear_tray.addTerm(id);
					gear_tray.addTerm(size);
					gear_tray.addTerm(slots);
					removePercept(gear_tray);
					gear_tray = new Predicate("gear_tray");
					gear_tray.addTerm(id);
					gear_tray.addTerm(size);
					slots.remove(slot);
					gear_tray.addTerm(slots);
					addPercept(gear_tray);
					removePercept(gear);
					gear = new Predicate("gear");
					gear.addTerm(slot);
					addPercept(gear);
					break;
				}
			}
		} else if (actionname.equals("find_slot")) {
			ListTerm slots = (ListTerm) act.getTerm(0);
			for (Term slot : slots) {
				for (MCAPLTerm slotpar : slot.getTerms()) {
					if (!slotpar.toString().contains("empty")) {
						addPercept((Predicate) slot);
					}
				}
			}
		} else if (actionname.equals("move")) {
			action_result = simulate_action(0.9, 1000);
			if (!action_result) {
				System.out.println("Action: "+actionname+" has failed!");
			}
		} else if (actionname.equals("open_gripper")) {
			action_result = simulate_action(0.9, 1000);
			if (!action_result) {
				System.out.println("Action: "+actionname+" has failed!");
			}
			else {
				removePercept(gripper);
				gripper = new Predicate("gripper");
				gripper.addTerm(new StringTermImpl("open"));
				addPercept(gripper);
				removePercept(grasped);
			}
		} else if (actionname.equals("close_gripper")) {
			action_result = simulate_action(0.9, 1000);
			if (!action_result) {
				System.out.println("Action: "+actionname+" has failed!");
			}
			else {
				removePercept(gripper);
				gripper = new Predicate("gripper");
				gripper.addTerm(new StringTermImpl("closed"));
				addPercept(gripper);
				grasped = new Predicate("grasped");
				grasped.addTerm(gear.getTerm(0));
				addPercept(grasped);
			}
		} else if (actionname.equals("wait")) {
			NumberTerm time = (NumberTerm) act.getTerm(0);
			try {
				Thread.sleep((int) time.solve());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!actionname.equals("print") && !actionname.equals("minus") && !actionname.equals("sum") && !actionname.equals("find_gear") && !actionname.equals("find_slot") && !actionname.equals("wait")) {
			removePercept(action_res);
			action_res = new Predicate("action_result");
			action_res.addTerm(new StringTermImpl(""+action_result));
			addPercept(action_res);
		}
		
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
	
	
//	@Override
//	public boolean done() {
//		return false;
//	}

}

	