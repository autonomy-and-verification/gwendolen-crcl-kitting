package gwendolen.crcl.kitting;

import ail.mas.DefaultEnvironment;
import ail.syntax.Action;
import ail.syntax.ListTerm;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.StringTerm;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.psl.MCAPLTerm;

import java.util.Random;



public class KittingEnv extends DefaultEnvironment{
	static final String logname = "gwendolen.crcl.kitting.KittingEnv";
	
	Predicate gear = new Predicate("gear");
	Predicate gripper = new Predicate("gripper");
	Predicate action_res = new Predicate("action_result");
	Predicate grasped = new Predicate("grasped");
	
	public KittingEnv() {
		super();
		
		gripper.addTerm(new StringTermImpl("open"));
		addPercept(gripper);
		
		System.out.println("Environment started.");
		
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
			ListTerm slots = (ListTerm) act.getTerm(0);
			for (Term slot : slots) {
				if (!slot.toString().contains("empty")) {
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

	