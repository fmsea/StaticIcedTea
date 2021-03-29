

/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai, Patrick Lam
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
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package conditional.scalar;

//import soot.*;
import soot.toolkits.graph.Orderer;
//import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.Unit;
import soot.jimple.IfStmt;

import java.util.Arrays;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Orders in pseudo-topological order, the nodes of a DirectedGraph instance.
 *
 * @author Steven Lambeth
 * @author Marc Berndl
 */
public class ConditionalTopologicalOrderer<N> implements Orderer<N> {	
	public static final boolean REVERSE = true;

	private Set<N> visited;

	private int[] indexStack;

	private N[] stmtStack;
	private ArrayList<N> order;
	private int orderLength;
	
	private HashMap<Unit, Integer> condToLine = new HashMap<Unit, Integer>();
	private List<ConditionalInfo> branchList = new ArrayList<ConditionalInfo>();

	private boolean mIsReversed = false;

	private DirectedGraph<N> graph;

	public ConditionalTopologicalOrderer() {
	}

	/**
	 * Reverses the order of the elements in the specified array.
	 * 
	 * @param array
	 */
	private static <T> void reverseArray(T[] array) {
		final int max = array.length >> 1;
		for (int i = 0, j = array.length - 1; i < max; i++, j--) {
			T temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void putInformation(Map<Unit, Integer> condToLine2, List<ConditionalInfo> infoList){
		condToLine = (HashMap) condToLine2;
		branchList = infoList;
	}
	
	
	public List<N> newList(DirectedGraph<N> g, boolean reverse) {
		this.mIsReversed = reverse;
		return computeOrder(g, !mIsReversed);
	}

	/**
	 * Orders in pseudo-topological order.
	 * 
	 * @param g
	 *            a DirectedGraph instance we want to order the nodes for.
	 * @return an ordered list of the graph's nodes.
	 */

	@SuppressWarnings("unchecked")
	protected final List<N> computeOrder(DirectedGraph<N> g, boolean reverse) {
		//System.out.println("Checking condToLine in Orderer " + condToLine);
		//System.out.println("Checking branchList in Orderer " + branchList);
		
		
		final int n = g.size();
		visited = Collections.newSetFromMap(new IdentityHashMap<N, Boolean>(n*2+1));//new HashMap((3 * g.size()) / 2, 0.7f);
		indexStack = new int[n];
		stmtStack = (N[]) new Object[n];
		order = new ArrayList<N>();
		graph = g;
		orderLength = 0;
		//System.out.println("The class of g is" + g.getClass());
		// Visit each node
		
		for (N s : g.getHeads()) {
			//System.out.println("The node we are visiting is " + s);
			if (visited.add(s))
				visitNode(s);
				
			if (orderLength == n)
				break;
		}

		if (reverse)
			Collections.reverse(order);

		

		indexStack = null;
		stmtStack = null;
		visited = null;
		
		return order;
	}

	// Unfortunately, the nice recursive solution fails
	// because of stack overflows

	// Fill in the 'order' list with a pseudo topological order
	// list of statements starting at s. Simulates recursion with a stack.

	private boolean specialCond(boolean branch, N node){ //returns true if the node we are at is a conditional node, and it only takes the branch we pass in 
		if (node instanceof IfStmt){
			//System.out.println("ifstmt " + node);
			if (branchList != null){
				for (ConditionalInfo f: branchList){
					//System.out.println("ci " + f + " " + condToLine.get(node));
					if (f.getLine() == condToLine.get(node)){
						if (f.getBranch() == branch){return true;}
						
					}
				}
			}
		}
		
		return false;
	}
	
	protected final void visitNode(N startStmt) {
		int last = 0;

		stmtStack[last] = startStmt;
		//check if startStmt is an if statement and we only want the falloutbranch, which is the false branch and weirdly the first child
		boolean onlyBranch = specialCond(true, startStmt);
				
		if (onlyBranch){
			//System.out.println("We are only going down the true branch");
			indexStack[last++] = 0;}
		else{
			indexStack[last++] = -1; //corresponds to the node at that same index in the stmtStack. always 1 less then the next child we want to visit for that node. 
		}
		
		
		
		while (last > 0) { //simulating recursion. last will get to 0 when the stack of nodes is empty
			int toVisitIndex = ++indexStack[last - 1]; //which child are we visiting
			N toVisitNode = stmtStack[last - 1];
			boolean onlyFall = false;
			onlyBranch = false; 
			
			onlyFall = specialCond(false, toVisitNode);
			//if (onlyFall){System.out.println("We are only going down the fall branch");}
				
			List<N> succs = graph.getSuccsOf(toVisitNode);
			if (toVisitIndex >= succs.size() || (onlyFall && toVisitIndex > 0)) {
				// Visit this node now that we ran out of children
				order.add(toVisitNode);
				last--;
			} else {
				N childNode = succs.get(toVisitIndex);
				//System.out.println(childNode + " -> " + toVisitIndex);
				
				onlyBranch = specialCond(true, childNode);
				//System.out.println("only branch is " + onlyBranch);
								
				if (visited.add(childNode)) {
					stmtStack[last] = childNode;
					if (onlyBranch){
						//System.out.println("We are only going down the true branch");
						indexStack[last++] = 0;
					}
					else{
						indexStack[last++] = -1;
					}
				}
			}
		}
	}

	// deprecated methods and constructors follow

	/**
	 * @deprecated use {@link #PseudoTopologicalOrderer()} instead
	 */
	@Deprecated
	public ConditionalTopologicalOrderer(boolean isReversed) {
		mIsReversed = isReversed;
	}

	/**
	 * @param g
	 *            a DirectedGraph instance whose nodes we wish to order.
	 * @return a pseudo-topologically ordered list of the graph's nodes.
	 * @deprecated use {@link #newList(DirectedGraph, boolean))} instead
	 */
	@Deprecated
	public List<N> newList(DirectedGraph<N> g) {
		return computeOrder(g, !mIsReversed);
	}

	/**
	 * Set the ordering for the orderer.
	 * 
	 * @param isReverse
	 *            specify if we want reverse pseudo-topological ordering, or
	 *            not.
	 * @deprecated use {@link #newList(DirectedGraph, boolean))} instead
	 */
	@Deprecated
	public void setReverseOrder(boolean isReversed) {
		mIsReversed = isReversed;
	}

	/**
	 * Check the ordering for the orderer.
	 * 
	 * @return true if we have reverse pseudo-topological ordering, false
	 *         otherwise.
	 * @deprecated use {@link #newList(DirectedGraph, boolean))} instead
	 */
	@Deprecated
	public boolean isReverseOrder() {
		return mIsReversed;
	}

}
