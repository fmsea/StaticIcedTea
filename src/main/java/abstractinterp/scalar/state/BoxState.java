package abstractinterp.scalar.state;

import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JNegExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	 * the algorithms is from https://en.wikipedia.org/wiki/Interval_arithmetic
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
			case 2: new_high = Math.max(x1*y1,Math.max(x1*y2, Math.max(x2*y1, x2*y2)));
			new_low = Math.min(x1*y1,Math.min(x1*y2, Math.min(x2*y1, x2*y2)));
			break;
			case 3: 
				if(y1 > 0 || y2 < 0){
					//if 0 not in [y1,y2] range
					new_high = Math.max(x1/y2,Math.max(x1/y1, Math.max(x2/y2, x2/y1)));
					new_low = Math.min(x1/y2,Math.min(x1/y1, Math.min(x2/y2, x2/y1)));
				} else if (y1 == 0 && y2 != 0){
					//y1 is zero but y2 is not
					new_low = Math.min(x1/y2, x2/y2);
				} else if (y2 == 0){
					//y2 is zero but y1 is not
					new_high = Math.max(x1/y1, x2/y1);
				} 
				//0 in between y1 and y2 - use the top values as set above

				break;//just use the default for now
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

	public void updateCond(BoxState inState, Value left, Value right, byte type) {
		//update to new values so that the condition holds with that type
		Int32_Box leftBox = eval(inState, left);
		Int32_Box rightBox = eval(inState, right);
		List<Int32_Box> result = transferCond(leftBox, rightBox, type);
		if(left instanceof Local){
			state.put((Local)left, result.get(0));
		}
		if(right instanceof Local){
			state.put((Local) right, result.get(1));
		}
	}

	public static List<Int32_Box> transferCond(Int32_Box lhs, Int32_Box rhs, byte type){
		List<Int32_Box> ret = new ArrayList<Int32_Box>();
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

			//lhs [x1,x2], rhs [y1,y2] returns the true evals
			byte position = -1;
			//[x1,x2] ... [y1,y2]
			if(x2 < y1){
				position = 0;
			} else if (x1 < y1 && y1 <= x2 && x2 < y2){
				//[x1, y1, x2, y2]
				position = 1;
			} else if (x1 <= y1 && y2 <= x2){
				//[x1,y1,y2,x2]
				position = 2;
			} else if (y1 < x1 && x1 <= y2 && y2 < x2){
				//[y1,x1,y2,x2]
				position = 3;
			}  else if (y2 < x1){
				//[y1,y2] .. [x1,x2]
				position = 4;
			} else if (y1 <= x1 && x2 <= y2){
				//[y1,x1,x2,y2]
				position = 5;
			} else {
				System.out.println("Not considered position cases");
			}
			int x2_new = x2;
			int x1_new = x1; 
			int y2_new = y2;
			int y1_new = y1;
			switch(type){
			case 0 : //equal
				switch(position){
				case 0://infeasible if do not intersect
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 1://common elements [y1,x2]
					x1_new = y1;
					y2_new = x2;
					break;
				case 2:// inner interval [y1,y2]
					x1_new = y1;
					x2_new = y2;
					break;
				case 3: //common elements [x1,y2]
					x2_new = y2;
					y1_new = x1;
					break;
				case 4://infeasible if do not intersect 
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 5:// inner interval [x1,x2]
					y2_new = x2;
					y1_new = x1;
					break;
				}
				break;
			case 1 : //not equal
				//check if there is only one point at it is is the same point
				if(x1==x2 && x2 == y2 && y2 == y1){
					//infeasible
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
				} 
				//otherwise the previous values
				break;
			case 2 : // <=
				switch(position){
				case 0:// all x's are <= than y's so leave the same
					break;
				case 1:
					//leave the same
					break;
				case 2:
					//remove some x's that are grater than y2
					x2_new = y2;
					break;
				case 3:
					//remove some y's that are less than x1 and some x's that are grater than y2
					y1_new = x1;
					x2_new = y2;
					break;
				case 4://infeasible
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 5:
					//remove some y's that are less than x1
					y1_new = x1;
					break;
				}
				break;
			case 3 : // < - should be similar to case 3, just need to add +1 ?
				switch(position){
				case 0://the same
					break;
				case 1: //leave the same
					break;
				case 2: //remove some x's that are strictly greater than y2
					if(x1 == x2 && x2 == y1 && y1 == y2){
						//infeasible
						x2_new = Integer.MIN_VALUE;
						x1_new = Integer.MAX_VALUE;
						y2_new = Integer.MIN_VALUE;
						y1_new = Integer.MAX_VALUE;
					} else {
						x2_new = y2 - 1;//it's ok to leave x1 since there are some elements in y that are greater than x1
						if(x1 == y1){
							//increase y1 by one
							y1_new = y1 + 1;
						}
					}
					break;
				case 3://shift x by one up and y by one down
					y1_new = x1 + 1;
					x2_new = y2 - 1;
					break;
				case 4://infeasible
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 5: //remove some y's that are less or equal to x1
					y1_new = x1 + 1;
					break;
				}
				break;
			case 4 : // >=
				switch(position){
				case 0:
					//infeasible
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 1: //remove some x and some y
					x1_new = y1;
					y2_new = x2;
					
					break;
				case 2:
					x1_new = y1;
					break;
				case 3:
					//stay the same
					break;
				case 4:
					//stay the same
					break;
				case 5:
					y1_new = x1;
					y2_new = x2;
					break;
				}
				break;
			case 5 : // > similar to case 4 only +/- 1
				switch(position){
				case 0:
					//infeasible
					x2_new = Integer.MIN_VALUE;
					x1_new = Integer.MAX_VALUE;
					y2_new = Integer.MIN_VALUE;
					y1_new = Integer.MAX_VALUE;
					break;
				case 1:// remove som x1 and some y and offset by one
					x1_new = y1 + 1;
					y2_new = x2 - 1;
					break;
				case 2:
					if(x1 == x2 && y2 == x2 && y2 == y1){
						//infeasible
						x2_new = Integer.MIN_VALUE;
						x1_new = Integer.MAX_VALUE;
						y2_new = Integer.MIN_VALUE;
						y1_new = Integer.MAX_VALUE;
					} else {
						x1_new = y1 + 1;
					}
					break;
				case 3://stay the same
					break;
				case 4://stay the same
					break;
				case 5://offset by one
					y1_new = x1 + 1;
					y2_new = x2 - 1;
					break;
				}
				break;
			}
			Int32_Box ret1 = new Int32_Box(1,Degenerate_Element.UNIVERSE);
			Constraint cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(x1_new)));
			ret1.add_constraint(cs_ppl);
			cs_ppl = new Constraint((new Linear_Expression_Variable(var)).unary_minus(), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(x2_new)) );
			ret1.add_constraint(cs_ppl);
			Int32_Box ret2 = new Int32_Box(1,Degenerate_Element.UNIVERSE);
			cs_ppl = new Constraint(new Linear_Expression_Variable(var), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(y1_new)));
			ret2.add_constraint(cs_ppl);
			cs_ppl = new Constraint((new Linear_Expression_Variable(var)).unary_minus(), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(y2_new)) );
			ret2.add_constraint(cs_ppl);
			ret.add(ret1);
			ret.add(ret2);
		} else {
			System.out.println("Not dealing with yet");
		}


		return ret;
	}
}
