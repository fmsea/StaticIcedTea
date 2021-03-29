package original.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import util.Variables;

public class ReachingDefinitionsAnalysis extends ForwardBranchedFlowAnalysis<HashMap<Local, Set<Unit>>>{

	protected long start;
	protected long end;
	
	
	
	
	public ReachingDefinitionsAnalysis(UnitGraph graph, String option){
		super(graph);
		start = System.currentTimeMillis();	
		doAnalysis();
		end = System.currentTimeMillis();
	}
	
	public long getTime(){
		return end - start;
	}
	
	
	

	@Override
	protected void flowThrough(HashMap<Local, Set<Unit>> in, Unit s,
		List<HashMap<Local, Set<Unit>>> fallOut, List<HashMap<Local, Set<Unit>>> branchOuts) {
		
//		System.out.println(Variables.numbers.get(s));
//		System.out.println("s " + s + " " + s.getClass());
//		System.out.println("in is:\n" + this.toString(in));
		//TreeMap<Local, Set<Unit>> inMap =  (TreeMap) in.getData();
		
		
		for (int i = 0; i < branchOuts.size(); i ++){
			copy(in, branchOuts.get(i));
			
		}
		
		for (int i = 0; i < fallOut.size(); i ++){
			copy(in, fallOut.get(i));
			
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
												
									
				//check if lineVar is already in the in map
				//if it is need to get rid of that pair in the fallOut
				if (in.containsKey(lineVar)){
					for (int i = 0; i < fallOut.size(); i++){
						fallOut.get(i).remove(lineVar); //we already copied the in into FallOut, so this line is safe
					}
					//System.out.println("I'm killing the following variable because it has been redfined: " + lineVar);
					
				}
				
				//finally create a new set called addSet just containing s, and put into the out map the pair of (lineVar, outSet)
				//we do this regardless of whether it's decleration of a new or old variable
				
				for (int i = 0; i < fallOut.size(); i++){
					Set <Unit> addSet = new HashSet<Unit>();
					addSet.add(s);
					fallOut.get(i).put(lineVar, addSet);
					//System.out.println("the new fall out is " + fallOut.get(i));
				}
				
						 
				
			}
			

		}
			
	}

	@Override
	protected void merge(HashMap<Local, Set<Unit>> in1, HashMap<Local, Set<Unit>> in2,
			HashMap<Local, Set<Unit>> out) {
		// TODO Auto-generated method stub
		//just union the two sets 
		//out = new MyFlowData(new TreeMap<Local, Set<Unit>>(), true);
		//System.out.println("in 1 " + in1);
		//System.out.println("in 2 " + in2);
		
		copy(in1, out);
				
		
		//HashMap<Local, Set<Unit>> in2Map= (TreeMap) in2.getData();
			
		for(Local l : in2.keySet()){
				
			Set<Unit> newSet = null;
			if(out.containsKey(l)){
				newSet = out.get(l); 
			} else {
				newSet = new HashSet<Unit>();
				out.put(l, newSet);
			}
			newSet.addAll(in2.get(l));
				
		}
		
	}

	@Override
	protected void copy(HashMap<Local, Set<Unit>> source, HashMap<Local, Set<Unit>> dest) {
			
		
		dest.clear();
		//HashMap<Local, Set<Unit>> sourceMap = (TreeMap) source.getData();
		
		
		for (Local l: source.keySet()){
			Set<Unit> newSet = new HashSet<Unit>();
			//if (dest.containsKey(l)){
				//newSet = dest.get(l);
			//}else{
				//newSet = new HashSet<Unit>();
				dest.put(l, newSet);
				
			//}
			newSet.addAll(source.get(l));
		}
		
			//Set<Unit> newSet = new HashSet<Unit>();
			//newSet.addAll(source.get(l));
			//dest.put(l, newSet); //calling the put method of the MyFlowData class
		//}
		
		
		
		
		
	}

	@Override
	protected HashMap<Local, Set<Unit>> newInitialFlow() {
		//System.out.println("newInitFlow");
		
		return new HashMap<Local, Set<Unit>>();
		
	}
	
	@Override
	protected HashMap<Local, Set<Unit>> entryInitialFlow() {
		//System.out.println("newEntryFlow");
		return new HashMap<Local, Set<Unit>>();
		
	}
	
	
	public String toString(HashMap<Local, Set<Unit>> toPrint){
		StringBuilder toReturn = new StringBuilder();
		for (Local l: toPrint.keySet()){
			//toReturn.append("Local: ");
//			toReturn.append(l.getNumber());
//			
			
			toReturn.append(l.toString());
			toReturn.append(":");
			toReturn.append(" ");
			
			for (Unit u: toPrint.get(l)){
				//toReturn.append(u);
				toReturn.append(Variables.numbers.get(u));
				toReturn.append(": ");
				
			}
			toReturn.delete(toReturn.length()-2, toReturn.length()-1);
			toReturn.append("\n");
		}
		
		
		return toReturn.toString();
	}

}