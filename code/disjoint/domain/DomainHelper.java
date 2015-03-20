package disjoint.domain;

import java.util.ArrayList;
import java.util.List;

//this is a helper class that instantiates some simple domains.
//later will use antlr-based instantiator

public class DomainHelper {
	
	public static Domain zeroDomain(){
		Domain d = new Domain();
		Predicate zero = new IntervalPredicate("=","0");
		BaseElement be = new BaseElement();
		be.addPredicate(zero);
		d.addBaseElement(be);
		Predicate notZero = new IntervalPredicate("!=","0");
		be = new BaseElement();
		be.addPredicate(notZero);
		d.addBaseElement(be);
		d.done();
		return d;
	}
	
	public static Domain oneDomain(){
		Domain d = new Domain();
		Predicate zero = new IntervalPredicate("=","1");
		BaseElement be = new BaseElement();
		be.addPredicate(zero);
		d.addBaseElement(be);
		Predicate notZero = new IntervalPredicate("!=","1");
		be = new BaseElement();
		be.addPredicate(notZero);
		d.addBaseElement(be);
		d.done();
		return d;
	}
	
	public static Domain signDomain(){
		Domain d = new Domain();
		Predicate zero = new IntervalPredicate("=","0");
		BaseElement be = new BaseElement();
		be.addPredicate(zero);
		d.addBaseElement(be);
		Predicate gt = new IntervalPredicate(">","0");
		be = new BaseElement();
		be.addPredicate(gt);
		d.addBaseElement(be);
		Predicate lt = new IntervalPredicate("<","0");
		be = new BaseElement();
		be.addPredicate(lt);
		d.addBaseElement(be);
		d.done();
		return d;
	}
	
	public static Domain zeroAndOnes(){
		Domain d = new Domain();
		Predicate zero = new IntervalPredicate("=","0");
		BaseElement be = new BaseElement();
		be.addPredicate(zero);
		d.addBaseElement(be);
		Predicate gt = new IntervalPredicate("=","1");
		be = new BaseElement();
		be.addPredicate(gt);
		d.addBaseElement(be);
		Predicate lt = new IntervalPredicate("=","-1");
		be = new BaseElement();
		be.addPredicate(lt);
		d.addBaseElement(be);
		Predicate lt1 = new IntervalPredicate("<","-1");
		be = new BaseElement();
		be.addPredicate(lt1);
		d.addBaseElement(be);
		Predicate gt1 = new IntervalPredicate(">","1");
		be = new BaseElement();
		be.addPredicate(gt1);
		d.addBaseElement(be);
		d.done();
		return d;
	}
	
	public static Domain zeroAndOnesAndTwos(){
		Domain d = new Domain();
		Predicate zero = new IntervalPredicate("=","0");
		BaseElement be = new BaseElement();
		be.addPredicate(zero);
		d.addBaseElement(be);
		Predicate gt = new IntervalPredicate("=","1");
		be = new BaseElement();
		be.addPredicate(gt);
		d.addBaseElement(be);
		Predicate lt = new IntervalPredicate("=","-1");
		be = new BaseElement();
		be.addPredicate(lt);
		d.addBaseElement(be);
		Predicate lt1 = new IntervalPredicate("<","-1");
		Predicate gt2 = new IntervalPredicate(">=", "-2");
		be = new BaseElement();
		be.addPredicate(lt1);
		be.addPredicate(gt2);
		d.addBaseElement(be);
		Predicate leq3 = new IntervalPredicate("<=", "-3");
		be = new BaseElement();
		be.addPredicate(leq3);
		d.addBaseElement(be);
		Predicate gt1 = new IntervalPredicate(">","1");
		gt2 = new IntervalPredicate("<=","2" );
		be = new BaseElement();
		be.addPredicate(gt1);
		be.addPredicate(gt2);
		d.addBaseElement(be);
		leq3 = new IntervalPredicate(">=", "3");
		be = new BaseElement();
		be.addPredicate(leq3);
		d.addBaseElement(be);
		d.done();
		System.out.println(d.element);
		return d;
	}
	
	public static Domain unstructured2(){
		Domain d = new Domain();
		d.disjoint = false;//calculates conjunctions
		Predicate lt2 = new IntervalPredicate("<", "2");
		BaseElement be = new BaseElement();
		be.addPredicate(lt2);
		d.addBaseElement(be);
		//add the negation of that predicate since
		//it is unstructured domain
		Predicate ge2 = new IntervalPredicate(">=", "2");
		be = new BaseElement();
		be.addPredicate(ge2);
		d.addBaseElement(be);
		Predicate gt1 = new IntervalPredicate(">", "1");
		be = new BaseElement();
		be.addPredicate(gt1);
		d.addBaseElement(be);
		//its negation
		Predicate le1 = new IntervalPredicate("<=", "1");
		be = new BaseElement();
		be.addPredicate(le1);
		d.addBaseElement(be);
		d.done();
		return d;
	}
	
	public static Domain disjoint1(){
		Domain d = new Domain();
		List<BaseElement> list = disjointList1();
		for(BaseElement be : list){
			d.addBaseElement(be);
		}
		return d;
	}
	
	public static Domain disjoint2(){
		Domain d = new Domain();
		List<BaseElement> list = disjointList2();
		for(BaseElement be : list){
			d.addBaseElement(be);
		}
		return d;
	}
	
	public static Domain unstructuredBoth(){
		Domain d = new Domain();
		d.disjoint = false;//calculates conjunction
		List<BaseElement> list = disjointList1();
		for(BaseElement be2 : disjointList2()){
			if(!list.contains(be2)){
				System.out.println("!contains " + be2 + " in " + list);
				list.add(be2);
			}
		}
		//list.addAll(disjointList2());
		for(BaseElement be : list){
			d.addBaseElement(be);
		}
		System.out.println("List unstrc " + list);
		return d;
	}
	
	private static List<BaseElement> disjointList1(){
		List<BaseElement> ret = new ArrayList<BaseElement>();
		Predicate p1 = new IntervalPredicate("=", "0");
		BaseElement be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate("=", "1");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate("=", "-1");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate(">=", "2");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate("<=", "-2");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		return ret;
	}
	
	private static List<BaseElement> disjointList2(){
		List<BaseElement> ret = new ArrayList<BaseElement>();
		Predicate p1 = new IntervalPredicate("=", "0");
		BaseElement be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate("<=", "-5");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate(">=", "5");
		be = new BaseElement(); be.addPredicate(p1);
		ret.add(be);
		p1 = new IntervalPredicate("<", "0");
		Predicate p2 = new IntervalPredicate(">", "-5");
		be = new BaseElement(); be.addPredicate(p1); be.addPredicate(p2);
		ret.add(be);
		p1 = new IntervalPredicate(">", "0");
		p2 = new IntervalPredicate("<", "5");
		be = new BaseElement(); be.addPredicate(p1); be.addPredicate(p2);
		ret.add(be);
		return ret;
	}
	
	

}
