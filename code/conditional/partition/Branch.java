package conditional.partition;

import java.util.List;

import soot.Unit;
import soot.jimple.IfStmt;

public class Branch {
	private IfStmt ifS;
	private List<Unit> succ;
	private int diff;
	private int perc;
	private int index;
	
	public Branch(IfStmt ifS, List<Unit> succ, int index){
		this.ifS = ifS;
		this.succ = succ;
		this.index = index;
	}
	
	public void setDiff(int diff){
		this.diff = diff;
	}
	
	public void setPerc(int max){
		this.perc = max;
	}
	
	public IfStmt getIfStmt(){
		return ifS;
	}
	
	public List<Unit> getSucc(){
		return succ;
	}
	
	public int getPerc(){
		return perc;
	}
	
	public int getDiff(){
		return diff;
	}
	
	public int getIndex(){
		return index;
	}
	
	@Override
	public String toString(){
		return index + ": " + ifS;
	}
	
	@Override
	public boolean equals(Object o){
		boolean ret = false;
		if(o instanceof Branch){
			Branch other = (Branch)o;
			ret = other.getIfStmt().equals(ifS);
		}
		return ret;
	}
	
	@Override
	public int hashCode(){
		return ifS.hashCode();
	}
}
