package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;

public class Variables {

	
	UnitGraph myGraph;
	public static HashMap<Unit, Integer> numbers; 
	
	public Variables(UnitGraph g){
		myGraph = g; //pointing myGraph to g, rather than a deep copy, so renameLocals should actually work
	}

	public void makeVariables(){
		int count = 0;
		numbers = new HashMap<Unit, Integer>();
		for (Unit u: myGraph){
			count++;
			numbers.put(u, count);
		}
	}
	
	public void renameLocals(){
		
		HashSet<Local> localSet = new HashSet<Local>(); //keeping track of which locals we've come across
		HashSet<String> localNames = new HashSet<String>(); //keeping track of which local names we have used so far
		
		for (Unit u: myGraph){ //loop through all the units in the graph
			Iterator defIt = u.getDefBoxes().iterator();
			
			while (defIt.hasNext()){ //for each unit loop through definition boxes
				ValueBox defBox = (ValueBox)defIt.next();
						
				if (defBox.getValue() instanceof Local){ //if its a local
					Local current = (Local) defBox.getValue();
					if (localSet.add(current)){ //if it's not already in the localSet, add it  
						String localName = current.getName(); //get the name
						if (localNames.add(localName)){ //if we can add the name do nothing
							//do nothing
						}else{
							//rename it and add to the localNames set
							int i = 1;
							while(true){
								String newName = localName + "_" + Integer.toString(i);
								if (localNames.add(newName)){
									current.setName(newName);
									break;
								}else{
									i++;
								}
							}
														
						}
					}
										
				}
			}
		}
	}
	
	


}
