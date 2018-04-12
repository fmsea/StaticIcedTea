package abstractinterp.scalar.state;

import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JNegExpr;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Constraint_System;
import parma_polyhedra_library.Degenerate_Element;
import parma_polyhedra_library.Int32_Box;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Coefficient;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Relation_Symbol;
import parma_polyhedra_library.Variable;

public class BoxState {
	//map of variables to its interval abstract state
	
	private Map<Local, Int32_Box> state;
	
	private static Variable var = new Variable(0);

	public BoxState(Set<Local> keys, boolean top){
		
		state = new HashMap<Local, Int32_Box>();
		if(top){
			//infinite intervals
			for(Local l : keys){
				Int32_Box rb = new Int32_Box(1, Degenerate_Element.UNIVERSE);
				Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MIN_VALUE)));
				rb.add_constraint(cs_ppl);
				cs_ppl = new Constraint((new Linear_Expression_Variable(var)), Relation_Symbol.LESS_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MAX_VALUE)) );
				rb.add_constraint(cs_ppl);
				state.put(l, rb);
			}
		} else {
			//by default create the empty intervals
			for(Local l : keys){
				Int32_Box rb = new Int32_Box(1, Degenerate_Element.UNIVERSE);
				state.put(l, rb);
			}
		}
	}
	
	public boolean isFeasible(){
		boolean ret = true;
		for(Int32_Box v : state.values()){
			if(!v.contains_integer_point()){
				ret = false;
				break;
			}
		}
		return ret;
	}

	public void copyTo(BoxState dest) {
		for(Entry<Local, Int32_Box> entry : state.entrySet()){
			Local l = entry.getKey();
			Int32_Box val = entry.getValue();
			//create a new entry
			Int32_Box newVal = new Int32_Box(val);
			dest.update(l, newVal);
		}
		
	}
	
	public Map<Local, Int32_Box> getMap(){
		return state;
	}
	
	public void update(Local l, Int32_Box b){
		state.put(l, b);
	}
	
	public Int32_Box getValue(Local l){
		return state.get(l);
	}

	public void mergeWith(BoxState in) {
		//merge in1 and in2 and assign the result to this
		for(Local l : state.keySet()){
			//getValue(l).add_constraints(in.getValue(l).constraints());
			getValue(l).upper_bound_assign(in.getValue(l));//smallet box containing the union of two
		}
		
	}

	public void widenWith(BoxState prevBeforeFlow) {
		for(Local l : state.keySet()){
			getValue(l).widening_assign(prevBeforeFlow.getValue(l), null);
		}
		
	}
	/**
	 * [x1,x2] op [y1,y2]
	 * @param lhs
	 * @param rhs
	 * @param type of the operation: 0 - addition, 1 - subtraction, 2 - multiplication, 3 - division
	 * @return
	 */
	public static Int32_Box transferBinary(Int32_Box lhs, Int32_Box rhs, byte type){
		Int32_Box ret = new Int32_Box(1,Degenerate_Element.UNIVERSE);
		//find low of lhs
		Constraint_System cs_lhs = lhs.constraints();
		Constraint_System cs_rhs = rhs.constraints();
		if(cs_lhs.size() == 2 && cs_rhs.size() == 2 ){
			Constraint low_lhs = cs_lhs.get(0);
			Constraint high_lhs = cs_lhs.get(1);
			int x1 = ((Linear_Expression_Coefficient) low_lhs.right_hand_side()).argument().getBigInteger().intValue();
			int x2 = ((Linear_Expression_Coefficient) high_lhs.right_hand_side()).argument().getBigInteger().intValue();
			Constraint low_rhs = cs_rhs.get(0);
			Constraint high_rhs = cs_rhs.get(1);
			int y1 = ((Linear_Expression_Coefficient)low_rhs.right_hand_side()).argument().getBigInteger().intValue();
			int y2 = ((Linear_Expression_Coefficient)high_rhs.right_hand_side()).argument().getBigInteger().intValue();
			//adding them up
			int new_high = Integer.MAX_VALUE;
			int new_low = Integer.MIN_VALUE; 
			switch(type){
			case 0: new_low = x1==Integer.MIN_VALUE || y1 == Integer.MIN_VALUE ?  Integer.MIN_VALUE : x1 + y1;
					new_high =  x2==Integer.MAX_VALUE || y2 == Integer.MAX_VALUE ? Integer.MAX_VALUE : x2 + y2;
					break;
			case 1: new_low = x1 - y2; //do more checks here too
					new_high = x2 - y1;
					break;
			case 3: new_high = Math.max(x1*y1,Math.max(x1*y2, Math.max(x2*y1, x2*y2)));
					new_low = Math.min(x1*y1,Math.min(x1*y2, Math.min(x2*y1, x2*y2)));
					break;
			case 4: break;//just use the default for now
			}
			//create a new constraint
			Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(new_low)));
			ret.add_constraint(cs_ppl);
			cs_ppl = new Constraint((new Linear_Expression_Variable(var)).unary_minus(), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(new_high)) );
			ret.add_constraint(cs_ppl);
		} else {
			System.out.println("Not dealing yet - we assume there is always an interval");
		}
		return ret;
	}
	
	/**
	 * Returns the negated copy inBox argument [x1,x2]
	 * @param inBox
	 * @return [-x2,-x1]
	 */
	public static Int32_Box negate(Int32_Box inBox){
		Int32_Box ret = new Int32_Box(1,Degenerate_Element.UNIVERSE);
		//should be two constraints there?
		for(Constraint c : inBox.constraints()){
			//negate lhs
			Linear_Expression lhs = c.left_hand_side().unary_minus();
			Constraint cs_ppl = new Constraint(lhs, Relation_Symbol.GREATER_OR_EQUAL, c.right_hand_side());
			ret.add_constraint(cs_ppl);
		}
		return ret;
	}
	
	public static Int32_Box constant(int val){
		Int32_Box ret = new Int32_Box(1,Degenerate_Element.UNIVERSE);
		Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.EQUAL, new Linear_Expression_Coefficient(new Coefficient(val)));
		ret.add_constraint(cs_ppl);
		return ret;
	}

	public void updateState(Local lVar, BoxState inState, Value left, Value right, byte type) {
		Int32_Box leftBox = eval(inState, left);
		Int32_Box rightBox = eval(inState, right);
		state.put(lVar, transferBinary(leftBox, rightBox, type));		
	}
	
	private static Int32_Box eval(BoxState inState, Value v){
		Int32_Box ret = null;
		if(v instanceof IntConstant){
			ret = constant(((IntConstant)v).value);
		} else if (v instanceof Local){
			ret = inState.getValue((Local)v);
		} else {
			//return a new unresticted box - any value
			Int32_Box rb = new Int32_Box(1, Degenerate_Element.UNIVERSE);
			Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MIN_VALUE)));
			rb.add_constraint(cs_ppl);
			cs_ppl = new Constraint((new Linear_Expression_Variable(var)).unary_minus(), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MAX_VALUE)) );
			rb.add_constraint(cs_ppl);
		}
		
		return ret;
	}

	public void updateState(Local lVar, BoxState inState, Value v) {
		if(v instanceof JNegExpr){
			v = ((JNegExpr)v).getOp();
			state.put(lVar, negate(eval(inState,v)));
		} else {
			state.put(lVar, eval(inState,v));
		}
	}
	
	@Override
	public String toString(){
		return state.toString();
	}

	public void updateTop(Local l) {
		Int32_Box rb = new Int32_Box(1, Degenerate_Element.UNIVERSE);
		Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MIN_VALUE)));
		rb.add_constraint(cs_ppl);
		cs_ppl = new Constraint((new Linear_Expression_Variable(var)), Relation_Symbol.LESS_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(Integer.MAX_VALUE)) );
		rb.add_constraint(cs_ppl);
		state.put(l, rb);
		
	}
}
