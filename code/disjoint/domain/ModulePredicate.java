package disjoint.domain;

import soot.Value;
import soot.jimple.ConditionExpr;

public class ModulePredicate extends Predicate {
	String mod;
	String rem;
	
	public ModulePredicate(String setMod, String setRem){
		op = "==";
		mod = setMod;
		rem = setRem;
	}


	@Override
	public boolean evaluate(long... solutions) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "X % " + mod + op + rem;
	}


	@Override
	public ConditionExpr instantitate(Value var) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if(o instanceof ModulePredicate){
			ModulePredicate other = (ModulePredicate) o;
			if(mod.equals(other.getMod()) && rem.equals(other.getRem())){
				ret = true;
			}
		}
		return ret;
	}
	
	public String getMod(){
		return mod;
	}
	
	public String getRem(){
		return rem;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return mod.concat(rem).hashCode();
	}

}
