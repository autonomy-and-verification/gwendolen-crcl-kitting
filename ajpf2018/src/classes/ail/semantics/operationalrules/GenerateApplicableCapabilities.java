// ----------------------------------------------------------------------------
// Copyright (C) 2014 Louise A. Dennis, and Michael Fisher 
// 
// This file is part of the Agent Infrastructure Layer (AIL)
//
// The AIL is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// The AIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//----------------------------------------------------------------------------

package ail.semantics.operationalrules;

import ail.semantics.AILAgent;
import ail.semantics.OSRule;
import ail.syntax.Capability;
import ail.syntax.CapabilityLibrary;
import ail.syntax.Unifier;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generate all capabilities applicable in the current state of the agent.
 * @author lad
 *
 */
public class GenerateApplicableCapabilities implements OSRule {
	private static String name = "Generate Applicable Capabilities";

	/*
	 * (non-Javadoc)
	 * @see ail.semantics.OSRule#checkPreconditions(ail.semantics.AILAgent)
	 */
	public boolean checkPreconditions(AILAgent a) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see ail.semantics.OSRule#apply(ail.semantics.AILAgent)
	 */
	public void apply(AILAgent a) {
		ArrayList<Capability> cs = new ArrayList<Capability>();
		CapabilityLibrary CL = a.getCL();
		
		Iterator<Capability> ci = CL.iterator();
		while (ci.hasNext()) {
			Capability c = ci.next();
			if (c.getPre().logicalConsequence(a, new Unifier(), c.getVarNames(), AILAgent.SelectionOrder.LINEAR).hasNext()) {
				cs.add(c);
			}
		}
		
		a.setApplicableCapabilities(cs.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see ail.semantics.OSRule#getName()
	 */
	public String getName() {
		return name;
	}

}
