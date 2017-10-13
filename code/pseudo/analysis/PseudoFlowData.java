package pseudo.analysis;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Comparator;

import soot.Local;
import soot.Unit;
import util.Variables;


public class PseudoFlowData {
	
	public TreeMap <Local, Set<Unit>> data;
	public boolean visible = true;
	
	public PseudoFlowData(TreeMap<Local, Set<Unit>> info, boolean vis){
		data = new TreeMap<Local, Set<Unit>>(new Comparator<Local>(){

			@Override
			public int compare(Local o1, Local o2) {
				return o1.getNumber() - o2.getNumber();
			}
			
				
		});
		for (Local l: info.keySet()){
			HashSet<Unit> addSet = new HashSet<Unit>();
			addSet.addAll(info.get(l));
			data.put(l, addSet);
		}
		
		visible = vis;
	}
	
	@Override
	public boolean equals(Object compare){
		PseudoFlowData toCompare = (PseudoFlowData) compare;
		return (this.isVisible() == toCompare.isVisible() && this.getData().equals(toCompare.getData()));
			
	}
	
	public void put(Local key, Set<Unit> value){
		data.put(key, value);
	}
	
	public Set<Unit> get(Local key){
		return data.get(key);
	}
	
	public void remove(Local key){
		data.remove(key);
	}
	
	public void makeVisible(){
		visible = true;
	}
	
	public void makeInvisible(){
		visible = false;
	}
	
	public boolean isVisible(){
		return visible;
	}
	
	public TreeMap getData(){
		return data;
	}
	
	public void clear(){
		data.clear();
		//for (Local l: data.keySet()){
			//data.remove(l);
		//}
	}
	
	@Override
	public String toString(){
		StringBuilder toReturn = new StringBuilder();
		for (Local l: data.keySet()){
			//toReturn.append("Local: ");
//			toReturn.append(l.getNumber());
//			
			
			toReturn.append(l.toString());
			toReturn.append(":");
			toReturn.append(" ");
			
			for (Unit u: data.get(l)){
				toReturn.append(u);
				toReturn.append(Variables.numbers.get(u));
				toReturn.append(": ");
				
			}
			toReturn.delete(toReturn.length()-2, toReturn.length()-1);
			toReturn.append("\n");
		}
		
		
		return toReturn.toString();
	}

}
