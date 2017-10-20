package pseudo.analysis;

import java.util.HashMap;

import java.util.TreeMap;

import conditional.scalar.ConditionalInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import soot.Unit;
//import soot.NopStmt;
import soot.Local;
import soot.ValueBox;
import soot.jimple.IfStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;


public class PseudoConditionalReachingDefinitionsAnalysis extends ForwardBranchedFlowAnalysis<PseudoFlowData>{

	ArrayList<ConditionalInfo> branchInfo = new ArrayList<ConditionalInfo>();
	Map<Unit, Integer> condToLine = new HashMap<Unit, Integer>();
	private long start;
	private long end;
	
	
	
	public PseudoConditionalReachingDefinitionsAnalysis(UnitGraph graph, String option){
		super(graph);
		createBranchInfo(option);	
		makeCondToLineMap();
		//System.out.println(condToLine);
		//System.out.println(branchInfo);
		start = System.currentTimeMillis();
		doAnalysis();
		end = System.currentTimeMillis();
		
	}
	
	public long getTime(){
		return end - start;
	}
	
	private void makeCondToLineMap(){
		int value = 1;
		Iterator gIt = graph.iterator();
		while(gIt.hasNext()){
			Unit next = (Unit) gIt.next();
			if (next instanceof IfStmt){
				condToLine.put(next, value);
				//System.out.println(value + ": " + next);
				//value++; -> prehaps differen encoding
			}
			value++;
		}
	}
	
	public void createBranchInfo(String flowList){
		
//		System.out.println("printing out the branch Infos");
//		for (ConditionalInfo f: branchInfo){
//			System.out.println(f);
//		}
		
		int lineNumber = 1;
		int last = 0;
		for (int i = 0; i < flowList.length(); i++){
			//System.out.println(i);
			//System.out.println(flowList.charAt(i));
			
			
			Character current = flowList.charAt(i);
			if (current == 'f' || current == 't'){
				lineNumber = Integer.parseInt(flowList.substring(last, i));
				//System.out.println("line number is: " + lineNumber);
				last = i + 1;
				boolean branch = true;
				if (current == 'f'){
					branch = false;
				}
//				System.out.println(lineNumber);
//				System.out.println(branch);
				branchInfo.add(new ConditionalInfo(lineNumber, branch));
			}
			
			
			
		}
		//System.out.println("printing out the branch Infos");
//		for (ConditionalInfo f: branchInfo){
//			System.out.println(f);
//		}
	}
	
	@Override
	protected void flowThrough(PseudoFlowData in, Unit s,
			List<PseudoFlowData> fallOut, List<PseudoFlowData> branchOuts) {
		
		//System.out.println("s " + s + " " + s.getClass());
		//System.out.println("The in is visible: " + in.isVisible());
		//System.out.println("in is" + in);
		//TreeMap<Local, Set<Unit>> inMap = (TreeMap) in.getData();
		
		
		
		
		if (!in.isVisible()){
			//return;
			//if (in instanceof GotoStmt){
				//return; // we don't want to make the branchout invisible 
			//}
			
			for (int i = 0; i < fallOut.size(); i ++){
				fallOut.get(i).makeInvisible();
			}
			
			for (int i = 0; i < branchOuts.size(); i ++){
				branchOuts.get(i).makeInvisible();
			}
//			
			return;
		}
		

		
		//copy in the branchOuts branch. We should always do this as branchOuts is the true branch
		for (int i = 0; i < branchOuts.size(); i ++){
			copy(in, branchOuts.get(i));
			//branchOuts.get(i).makeVisible();
		}
		
		for (int i = 0; i < fallOut.size(); i++){
			copy(in, fallOut.get(i));
			//fallOut.get(i).makeVisible();
		}
		
		if(s instanceof IfStmt){
			//System.out.println("Cond stmt ");
			
			for (ConditionalInfo f: branchInfo){
				//System.out.println(f.getLine() + " " + condToLine.get(s));
				if (f.getLine() == condToLine.get(s)){
					//System.out.println(f.getLine());
					//System.out.println(condToLine.get(s));
					if (f.getBranch()){
						//System.out.println("we are at conditional statement: " + s);
						//System.out.println("Making the false branch invisible");
						//make the fallOut branch invisible
						for (int i = 0; i < fallOut.size(); i++){
							fallOut.get(i).makeInvisible();
						}
						
					}else{
						//make the branchOut branch invisible
						for (int i = 0; i < branchOuts.size(); i ++){
							branchOuts.get(i).makeInvisible();
							
						}
					}
				}
			}
		}
			
				
				
			
				
		
		//now iterate over the definitions of the statement (there may be none)
		//note that if there are def boxes, we would have already copied the in into the fallOut 
		Iterator defIt = s.getDefBoxes().iterator();
		
		while (defIt.hasNext()){
			ValueBox defBox = (ValueBox)defIt.next();
					
			if (defBox.getValue() instanceof Local){
				
				//System.out.println("U " + s + "\nin " + in);
				//get the local variable from the unit called lineVar
				Local lineVar = (Local) defBox.getValue();
												
				
				//I don't understand how the branchOut comes into play in this function
				
				
						
				//check if lineVar is already in the in map
				//if it is need to get rid of that pair in the fallOut
				if (in.getData().containsKey(lineVar)){
					for (int i = 0; i < fallOut.size(); i++){
						fallOut.get(i).remove(lineVar); //we already copied the in into FallOut, so this line is safe
					}
					//System.out.println("I'm killing the following variable because it has been redfined: " + lineVar);
					
				}//else{
				
				//finally create a new set called addSet just containing s, and put into the out map the pair of (lineVar, outSet)
				//we do this regardless of whether it's decleration of a new or old variable
				Set <Unit> addSet = new HashSet<Unit>();
				addSet.add(s);
				for (int i = 0; i < fallOut.size(); i++){
					fallOut.get(i).put(lineVar, addSet);
					//System.out.println("the new fall out is " + fallOut.get(i));
				}
				
				//}
				
				
				
				//our in set will be updated, and the branch set is not applicable in this case
				 
				
			}
			

		}
		
		
	}

	@Override
	protected void merge(PseudoFlowData in1, PseudoFlowData in2,
			PseudoFlowData out) {
		// TODO Auto-generated method stub
		//just union the two sets 
		//out = new MyFlowData(new HashMap<Local, Set<Unit>>(), true);
//		System.out.println(in1.isVisible());
//		System.out.println("in 1 " + in1.toString());
//		System.out.println(in2.isVisible());
//		System.out.println("in 2 " + in2.toString());
		
		out.makeInvisible();
		if (in1.isVisible()){
			
			copy(in1, out);
			
		}
		
		if (in2.isVisible()){ 
			TreeMap<Local, Set<Unit>> in2Map= (TreeMap) in2.getData();
			
			for(Local l : in2Map.keySet()){
				
				Set<Unit> newSet = null;
				if(out.getData().containsKey(l)){
					newSet = out.get(l); 
				} else {
					newSet = new HashSet<Unit>();
					out.put(l, newSet);
				}
				newSet.addAll(in2.get(l));
				
			}
		}
		//System.out.println("Im merging two branches");
		if (in1.isVisible() || in2.isVisible()){
			//System.out.println("in1 is: ");
			//System.out.println(in1);
			//System.out.println("in2 is: ");
			//System.out.println(in2);
			//tricky solution to bug here
			//in1.makeVisible();
			//in2.makeVisible();
			out.makeVisible();
		}
		
		//System.out.println("out " + out);
		//copy the contents of in1 into out.
		//loop through the contents of in2, and if the entry is already in out, don't add it else add it
		
	}

	@Override
	protected void copy(PseudoFlowData source, PseudoFlowData dest) {
		//System.out.println("copy " + source + " " + dest);
		//need to think about whether I should clear the destintation if the soruce is invisible
		//System.out.println(dest);
		dest.clear();
		if (!source.isVisible()){
			//System.out.println("invisible branch copied to invisible branch");
			dest.makeInvisible();
			//System.out.println(dest);
			return;
			
		}
		//System.out.println("visibile branch copied to visible branch");	
		
		
		TreeMap<Local, Set<Unit>> sourceMap = (TreeMap) source.getData();
		for (Local l: sourceMap.keySet()){
			HashSet<Unit> newSet = null;
			if (dest.getData().containsKey(l)){
				newSet = (HashSet) dest.getData().get(l);
			}else{
				newSet = new HashSet<Unit>();
				dest.put(l, newSet); //calling the put method of the MyFlowData class	
			}
						
			newSet.addAll(sourceMap.get(l));
			
		}
		dest.makeVisible();
		
		
		
		
		
	}

	@Override
	protected PseudoFlowData newInitialFlow() {
		//System.out.println("newInitFlow");
		
		return new PseudoFlowData(new TreeMap<Local, Set<Unit>>(), false);
		
	}
	
	@Override
	protected PseudoFlowData entryInitialFlow() {
		//System.out.println("newEntryFlow");
		return new PseudoFlowData(new TreeMap<Local, Set<Unit>>(), true);
		
	}
	
	

}
