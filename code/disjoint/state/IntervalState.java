package disjoint.state;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import disjoint.domain.Domain;

import soot.Value;

public class IntervalState implements State{
	
	/* A string representation of this bit set. 
	 * For every index for which this BitSet contains 
	 * a bit in the set state, the decimal 
	 * representation of that index is included in the result.
	 */
	/*
	 * Maps a variable to the list of bitvectors, where each bitvector
	 * is the abstract value of the variable in particular domain
	 */
	Map<Value,List<BitSet>> varToValue;
//	static public List<BitSet> bitsInDomainFalse = new ArrayList<BitSet>();
//	static public List<BitSet> bitsInDomainTrue = new ArrayList<BitSet>();
//	static public Map<Value, List<BitSet>> initialFlow = new HashMap<Value, List<BitSet>>();
//	static public Map<Value, List<BitSet>> entryFlow = new HashMap<Value, List<BitSet>>();
	static public List<Integer> bitsInDomain = new ArrayList<Integer>();
	
	public IntervalState(){
		varToValue = new HashMap<Value,List<BitSet>>();
	}
	
	public IntervalState(Map<Value, List<BitSet>> map){
		varToValue = new HashMap<Value,List<BitSet>>();
		for(Entry<Value, List<BitSet>> pair : map.entrySet()){
			List<BitSet> value = new ArrayList<BitSet>();
			value.addAll(copy(pair.getValue())); //make sure to make a deep copy for list of bitsets
			varToValue.put(pair.getKey(), value);
		}
	}

	@Override
	public State copy() {
		return new IntervalState(varToValue);
	}
	
	@Override
	public String toString(){
		return varToValue.toString();
	}
	
	@Override
	public boolean equals(Object o ){
		boolean ret = true;
		if(o instanceof IntervalState){
			IntervalState other = (IntervalState)o;
			//the same variable should have the same bitSets
			//maps should be the same
			ret = varToValue.equals(other.varToValue);
//			for(Value v : varToValue.keySet()){
//				if(!varToValue.get(v).equals(other.getState(v))){
//					ret = false;//if at least one is not the same then return false
//					break;
//				}
//			}
		} else {
			ret = false;
		}
		//System.out.println("comparing " + this + " and " + o + " => " + ret);
		return ret;
	}

	@Override
	public void initFlowVar(Value var) {
		//add a list of bitsets 
		//where all bitsets are set to 0
		//1. need to know the size of the list
		//2. need to know the number of bits in each list
		List<BitSet> init = new ArrayList<BitSet>();
		for(Integer size : bitsInDomain){
			BitSet ret = new BitSet(size);
			init.add(ret);
		}
		varToValue.put(var, init);
		
	}

	@Override
	public void initEntryVar(Value var) {
		List<BitSet> init = new ArrayList<BitSet>();
		for(Integer size : bitsInDomain){
			BitSet ret = new BitSet(size);
			ret.set(0,size, true);
			init.add(ret);
		}
		varToValue.put(var, init);
		
	}

	@Override
	public State merge(State state) {
		IntervalState ret = null;
		if(state instanceof IntervalState){
			Map<Value,List<BitSet>> retVarToValue = new HashMap<Value,List<BitSet>>();
			IntervalState other = (IntervalState) state;
			for(Value var : varToValue.keySet()){
				//System.out.println("Merging for " + var);
				List<BitSet> thisValues = varToValue.get(var);
				List<BitSet> newValues = copy(thisValues);
				List<BitSet> otherValues = other.getState(var);
				for(int i=0; i < newValues.size(); i++){
					BitSet otherValue = otherValues.get(i);
					BitSet newValue = newValues.get(i);
					//System.out.println("Other  " + otherValue);
					//System.out.println("This  " + newValue);
					//now do the actual merge
					newValue.or(otherValue);//moving up the lattice
					//System.out.println("New value  " + newValue);
				}
				retVarToValue.put(var, newValues);
			}
			ret = new IntervalState(retVarToValue);
		} else {
			System.err.println("Merging interval state and " + state.getClass());
			System.exit(2);
		}
		return ret;
	}
	
	public List<BitSet> getState(Value var){
		return varToValue.get(var);
	}
	
	private List<BitSet> copy(List<BitSet> src){
		List<BitSet> ret = new ArrayList<BitSet>();
		for(BitSet bs : src){
			ret.add((BitSet)bs.clone());
		}
		return ret;
	}

	@Override
	public void initFlow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initEntry() {
		// TODO Auto-generated method stub
		
	}	

}
