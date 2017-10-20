/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-2000 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-2000.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

/*
 2000, March 20 - Updated code provided by Patrick Lam
 <plam@sable.mcgill.ca>
 from 1.beta.4.dev.60
 to 1.beta.6.dev.34
 Plus some bug fixes.
 -- Janus <janus@place.org>


 KNOWN LIMITATION: the analysis doesn't handle traps since traps
 handler statements have predecessors, but they
 don't have the trap handler as successor.  This
 might be a limitation of the CompleteUnitGraph
 tho.
 */

package conditional.scalar;

import soot.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.util.*;

import java.util.*;

import soot.toolkits.graph.interaction.*;
import soot.jimple.IfStmt;
import soot.jimple.internal.JGotoStmt;
import soot.options.*;

/**
 * Abstract class providing an engine for branched forward flow analysis.
 * WARNING: This does not handle exceptional flow as branches!
 * */
public abstract class ConditionalForwardBranchedFlowAnalysis<A> extends BranchedFlowAnalysis<Unit, A> {

	public List<ConditionalInfo> myBranchList;
	Map<Unit, Integer> condToLine;



	public ConditionalForwardBranchedFlowAnalysis(UnitGraph graph, List<ConditionalInfo> branchList) {
		super(graph);
		myBranchList = branchList;
		//System.out.println("BL " + myBranchList);
		condToLine = new HashMap<Unit, Integer>(); //a map of conditional units to the number of that conditional statement. //the first conditional statement is the 0 conditional statement
		makeCondToLineMap(condToLine);

	}

	protected boolean isForward() {
		return true;
	}

	// Accumulate the previous afterFlow sets. //This method is called twice in fixed point algorithm
	//The first time is before the flowThrough is called, to accumulate the previousAfterFlows for that node
	//the second time is after the flowThough is caled to accumulate teh afterFlows for that node
	//if the iterative algorithm only goes through the graph one time without loops, then the previousAfterFlows will always be empty
	private void accumulateAfterFlowSets(Unit s, A[] flowRepositories, List<Object> previousAfterFlows) {
		//System.out.println("accumulating the after flow set");
		int repCount = 0;

		//		if( s instanceof JGotoStmt){
		//			System.out.println(s.fallsThrough() + " or " + s.branches());
		//			System.out.println(flowRepositories[0] + " \n" + flowRepositories[1]);
		//		}

		previousAfterFlows.clear();
		if (s.fallsThrough()) {
			copy(unitToAfterFallFlow.get(s).get(0), flowRepositories[repCount]);
			previousAfterFlows.add(flowRepositories[repCount++]);
		} 

		if (s.branches()) {
			//System.out.println("this unit branches");
			List<A> l = (getBranchFlowAfter(s));
			Iterator<A> it = l.iterator();

			while (it.hasNext()) {
				A fs = (it.next());
				copy(fs, flowRepositories[repCount]);
				previousAfterFlows.add(flowRepositories[repCount++]);
			}
		}
	} // end accumulateAfterFlowSets

	//	private void specialDFS(Map<Unit, Integer> condToLine, List<Unit>  orderedWorkList, Unit root){ //special depth first search to get the orderedWorkingList by only traversing down the branches we select to go down
	//		
	//		HashSet<Unit> visited = new HashSet<Unit>();
	//		visited.add(root);
	//		
	//		//create an empty stack of Unit
	//		int last = 0; //corresponds to the level of the stack we are at. always 1 more than the level we are at
	//		ArrayList <Unit> stmtStack = new ArrayList<Unit>(); //add and remove from end to implement as stack
	//		//add the root to the stack
	//		ArrayList <Integer> indexStack = new ArrayList<Integer>(); //each element corresponds to the next child for the stmtStack
	//		
	//		stmtStack.set(last,root);
	//		indexStack.set(last, 0);
	//		last++;
	//		
	//		
	//		while (last > 0){
	//			Unit current = stmtStack.get(last - 1);
	//			int index = indexStack.get(last - 1);
	//			indexStack.set(last - 1, index + 1);
	//			
	//			List<Unit> succs = graph.getSuccsOf(current);
	//			
	//			if (index >=succs.size()){
	//				orderedWorkList.add(current);
	//				last--;
	//			}else{
	//				if (visited.add(root)){
	//					stmtStack.set(last, succs.get(index));
	//					indexStack.set(last, 0);
	//					last++;
	//				}
	//			}
	//			
	//			
	//		}
	//		
	//		
	//	}

	private void makeCondToLineMap(Map<Unit, Integer> condToLine){
		int value = 1;
		Iterator gIt = graph.iterator();
		while(gIt.hasNext()){
			Unit next = (Unit) gIt.next();
			if (next instanceof IfStmt){
				condToLine.put(next, value);
			//	value++; BBBBBBBUUUUUGGGG!!!
			}
			value++;
		}
	}


	@Override
	protected void doAnalysis() {	
		final Map<Unit, Integer> numbers = new HashMap<Unit, Integer>(); //a map of each unit or line in a program to its line number
		ConditionalTopologicalOrderer<Unit> myOrderer = new ConditionalTopologicalOrderer<Unit>();
		myOrderer.putInformation(condToLine, myBranchList);
		//List<Unit> orderedUnits = (new PseudoTopologicalOrderer<Unit>()).newList(graph, false); //ordering the nodes in the graph topologically
		List<Unit> orderedWorkList = myOrderer.newList(graph, false);
		//List<Unit> orderedWorkList = new ArrayList<Unit>(); //will contain a pseudotopologically ordered set of the units we wish to process
		//if (myBranchList!= null){
		//specialDFS(condToLine, orderedWorkList, orderedUnits.get(0));
		//}else{
		//orderedWorkList = orderedUnits;
		//}

		//		int condStatement = 0;
		{
			int i = 1;
			for (Unit u : orderedWorkList) { //for (Unit u: orderedUnits){
				//System.out.println(u);
				numbers.put(u, new Integer(i));
				i++;

			}
			//			}
		}

		//System.out.println("Num of nodes " + orderedWorkList.size());
		//initialize our changedUnit treeset
		TreeSet<Unit> changedUnits = new TreeSet<Unit>(new Comparator<Unit>() {
			public int compare(Unit o1, Unit o2) {
				Integer i1 = numbers.get(o1);
				Integer i2 = numbers.get(o2);
				return (i1.intValue() - i2.intValue());
			}
		});




		Map<Unit, ArrayList<A>> unitToIncomingFlowSets = new HashMap<Unit, ArrayList<A>>(graph.size() * 2 + 1, 0.7f); //mapping each unit to incoming sets. e.g. a unit with three units merging into it will me mapped to three lists
		List<Unit> heads = graph.getHeads(); //entry points into the program flow
		int numNodes = graph.size(); //getting the number of nodes in the graph
		int numComputations = 0; //we have made 0 computations so far
		int maxBranchSize = 0; //our max side is needed for our flowRepositories. we will update this later

		// initialize unitToIncomingFlowSets
		{
			for (Unit s : graph) {
				unitToIncomingFlowSets.put(s, new ArrayList<A>());
			}
		}


		for (Unit u: orderedWorkList){
			changedUnits.add(u);
		}
		//		


		// Set initial values and nodes to visit.
		// WARNING: DO NOT HANDLE THE CASE OF THE TRAPS
		{
			Chain<Unit> sl = ((UnitGraph) graph).getBody().getUnits(); //getting all the units in the graph and putting them in sl
			for (Unit s : graph) {
				//changedUnits.add(s); //we want the changedUnits to initially be all the units

				unitToBeforeFlow.put(s, newInitialFlow()); //the flowBefore each unit is set to an initial flow. This is a hash map of unit to whatever our flow type is and is slightly different from our unitToIncomingFlow, which maps to a list of incoming flows
				//the unitToBeforeFlow always maps to one incoming flow. the unitToIncomingFlow can map to a list of incoming flow, which sometimes is merged into one flow which is put in unitToBeforeFlow

				if (s.fallsThrough()) {
					List<A> fl = new ArrayList<A>();

					fl.add((newInitialFlow()));
					unitToAfterFallFlow.put(s, fl);

					Unit succ = sl.getSuccOf(s);
					// it's possible for someone to insert some (dead)
					// fall through code at the very end of a method body
					if (succ != null) {
						List<A> l = (unitToIncomingFlowSets.get(sl.getSuccOf(s)));
						l.addAll(fl); //now the afterflow of s is in the incoming flow set of its successor
					}
				} else
					unitToAfterFallFlow.put(s, new ArrayList<A>()); //if s doesn't fall through no need to put in incoming flow set of successor and just put an empty list 

				List<A> l = new ArrayList<A>(); 
				if (s.branches()) {
					List<A> incList;
					for (UnitBox ub : s.getUnitBoxes()) { //somehow the unitBoxes of s are its branches
						A f = (newInitialFlow());

						l.add(f);
						Unit ss = ub.getUnit();
						incList = (unitToIncomingFlowSets.get(ss));

						incList.add(f); //again, the successor of s's flow in it list of incoming flow
					}

				}
				unitToAfterBranchFlow.put(s, l); //now the afterBranchFlow of s is initiallized as a new initial flow, and put in the map 

				if (s.getUnitBoxes().size() > maxBranchSize)
					maxBranchSize = s.getUnitBoxes().size(); //updating the max branch size if we need to
			}
		}
		//System.out.println("I'm printing out the unit to Incoming Flow because my program is annoying");
		//		for (Unit u: unitToIncomingFlowSets.keySet()){
		//			System.out.println("Unit: " + u + " " + "Incoming Flow Set: " + System.identityHashCode(unitToIncomingFlowSets.get(u)));
		//		}

		// Feng Qian: March 07, 2002
		// init entry points
		{
			for (Unit s : heads) {
				// this is a forward flow analysis
				unitToBeforeFlow.put(s, entryInitialFlow()); //the unitToBeforeFlow is initialized for all nodes
			}
		}

		// Perform fixed point flow analysis
		{
			List<Object> previousAfterFlows = new ArrayList<Object>(); //used to keep track of the previous After Flows for each node. for the first iteration, this is going to be the empty set
			List<Object> afterFlows = new ArrayList<Object>(); //used to keep track of the after Flows for each list of objects. will be compared to the previousAfter flows to see if the units after flow was changed
			A[] flowRepositories = (A[]) new Object[maxBranchSize + 1]; //can't figure out why this is needed
			for (int i = 0; i < maxBranchSize + 1; i++)
				flowRepositories[i] = newInitialFlow();
			A[] previousFlowRepositories = (A[]) new Object[maxBranchSize + 1];
			for (int i = 0; i < maxBranchSize + 1; i++)
				previousFlowRepositories[i] = newInitialFlow(); //again can't figure out why this is needed

			while (!changedUnits.isEmpty()) {
				//System.out.println("At the beginning of the loop for the iterative algorithm!!");
				//System.out.println(changedUnits);
				A beforeFlow;


				Unit s = changedUnits.first();
				changedUnits.remove(s);
				boolean isHead = heads.contains(s);

				accumulateAfterFlowSets(s, previousFlowRepositories, previousAfterFlows); //just updating the previousAfterFlow to be the current afterFlow, before we've called the flowThrough method
				//don't know why flow repository is necessary


				// Compute and store beforeFlow
				{
					List<A> preds = unitToIncomingFlowSets.get(s); //getting all the incoming flows

					beforeFlow = getFlowBefore(s); //getting the current before flow of the unit we are at (in first iteration this should just be whatever initialFlow() returns)

					//the unitToIncomingFlow sets was initialized to be connected to the incoming flow from previous branches and fallOuts 
					//if in the last iteration we changed the afteflow of these previous precsessor nodes we need to copy that new inflow into our unitToFlowBefore

					if (preds.size() == 1) //if there is only one incoming flow, just copy it to the current beforeFlow
						copy(preds.get(0), beforeFlow); 
					else if (preds.size() != 0) { //otherwise, copy the first incmoing flow, then copy all the branches that are merging into this unit
						Iterator<A> predIt = preds.iterator();

						copy(predIt.next(), beforeFlow);

						while (predIt.hasNext()) {
							A otherBranchFlow = predIt.next();
							//System.out.println("Merging branches " + "Branch1: " + otherBranchFlow);
							//System.out.println("Branch 2: " + beforeFlow);
							A newBeforeFlow = newInitialFlow();
							merge(s, beforeFlow, otherBranchFlow, newBeforeFlow);
							//System.out.println("Merged branch is " + newBeforeFlow);
							copy(newBeforeFlow, beforeFlow);
						}


					}

					if (isHead && preds.size() != 0) //if it's the head and has no pred nodes, then mergeInto the beforeFlow (not really sure what this is doing), i feel like if its the head it should just have no beforeFlow
						mergeInto(s, beforeFlow, entryInitialFlow());
				}

				// Compute afterFlow and store it.
				{
					List<A> afterFallFlow = unitToAfterFallFlow.get(s);
					List<A> afterBranchFlow = getBranchFlowAfter(s);

					//System.out.println("in flow " + beforeFlow + "before fall " + afterFallFlow + " before branch " + afterBranchFlow );
					flowThrough(beforeFlow, s, afterFallFlow, afterBranchFlow);



					numComputations++; //the number of computations is one more
				}
				//if (unitToAfterFallFlow.get(s).size() > 0){
				//	System.out.println("printing out after fall again " + unitToAfterFallFlow.get(s).get(0));
				//}
				accumulateAfterFlowSets(s, flowRepositories, afterFlows); //just updating the afterFlows variable
				//System.out.println("the accumulated after flow is: " + afterFlows);

				// Update queue appropriately //well at least these lines makes sense to me
				boolean onlyFall = false;
				boolean onlyBranch = false;

				if (!afterFlows.equals(previousAfterFlows)) { //whatever you are flowing through needs to override Objects equals method otherwise this won't work
					//System.out.println("Adding successors because after flow has changed");
					//System.out.println(afterFlows);
					//System.out.println(previousAfterFlows);
					//System.out.println("The previous after flow does not equal the after flow");
					//System.out.println("The previous after flow is: " + previousAfterFlows.toString());
					//System.out.println("The current after flow is: " + afterFlows.toString() + "\n");
					if (s instanceof IfStmt && myBranchList != null){
						for (ConditionalInfo f: myBranchList){
							if (f.getLine() == condToLine.get(s)){
								if (f.getBranch()){
									//we need to remove the fallOut branch
									onlyBranch = true;
								}else{
									//we need to remove all the branchOut branches
									onlyFall = true;
								}
							}
						}
					}

					if (onlyFall){
						changedUnits.add(graph.getSuccsOf(s).get(0));
					}else if (onlyBranch){
						for (UnitBox ub : s.getUnitBoxes()) {
							Unit toAdd = ub.getUnit();
							changedUnits.add(toAdd);
						}
					}else{//otherwise just put all children into list of children
						for (Unit succ : graph.getSuccsOf(s)) {
							changedUnits.add(succ);
						}
					}
				}



			}
		}

		//printing out RAM Locations
		//for (Unit u: orderedWorkList){
		//System.out.println("Unit: " + u);
		//System.out.println("Fall Flow List Location: " + System.identityHashCode(unitToAfterFallFlow.get(u)));
		//for (int i = 0; i < unitToAfterFallFlow.get(u).size(); i++){
		//System.out.println("element: " + i + " " + System.identityHashCode(unitToAfterFallFlow.get(u).get(0)));
		//}
		//System.out.println("Branch Flow List Location: " + System.identityHashCode(unitToAfterBranchFlow.get(u)));
		//for (int i = 0; i < unitToAfterBranchFlow.get(u).size(); i++){
		//System.out.println("element: " + i + " " + System.identityHashCode(unitToAfterBranchFlow.get(u).get(0)));
		//}
		//}

		// G.v().out.println(graph.getBody().getMethod().getSignature() +
		// " numNodes: " + numNodes +
		// " numComputations: " + numComputations + " avg: " +
		// Main.truncatedOf((double) numComputations / numNodes, 2));

		Timers.v().totalFlowNodes += numNodes;
		Timers.v().totalFlowComputations += numComputations;

	} // end doAnalysis



} // end class ForwardBranchedFlowAnalysis