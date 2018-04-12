package abstractinterp.scalar;

import abstractinterp.scalar.state.BoxState;
import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Degenerate_Element;
import parma_polyhedra_library.Int32_Box;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Parma_Polyhedra_Library;
import parma_polyhedra_library.Rational_Box;
import parma_polyhedra_library.Relation_Symbol;
import parma_polyhedra_library.Variable;
import parma_polyhedra_library.Variables_Set;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Coefficient;

public class StartPPL {

	public static void main(String[] args) {
		//System.load("/Users/elenasherman/Documents/research/jpf-symbc/lib/libppl_java.jnilib");
		System.load("/usr/local/lib/ppl/libppl_java.jnilib");
		Parma_Polyhedra_Library.initialize_library();
		C_Polyhedron cp = new C_Polyhedron(2, Degenerate_Element.UNIVERSE);
		System.out.println(cp.is_empty());
		C_Polyhedron cp2 = new C_Polyhedron(2, Degenerate_Element.UNIVERSE);
		Linear_Expression lhs = new Linear_Expression_Coefficient(new Coefficient(2));
		Variable var = new Variable(0);
		Linear_Expression_Variable rhs = new Linear_Expression_Variable(var);
		Relation_Symbol rs = Relation_Symbol.EQUAL;
		Constraint cs_ppl = new Constraint(lhs, rs, rhs);
		cp.add_constraint(cs_ppl);
		cp.minimized_constraints();
		cs_ppl = new Constraint(new Linear_Expression_Coefficient(new Coefficient(3)), Relation_Symbol.EQUAL,  new Linear_Expression_Variable(var));
		cp2.add_constraint(cs_ppl);
		cs_ppl =  new Constraint(new Linear_Expression_Coefficient(new Coefficient(1)), Relation_Symbol.EQUAL,  new Linear_Expression_Variable(var));
		C_Polyhedron cp3 = new C_Polyhedron(2, Degenerate_Element.UNIVERSE);
		cp3.add_constraint(cs_ppl);
		System.out.println("cp " + cp +"\t cp3 " + cp3);
		cp.upper_bound_assign(cp3);
		//cp3.upper_bound_assign(cp);
		cp.upper_bound_assign(cp2);
		System.out.println(cp + "\t " + cp.is_topologically_closed());
		//topologically closed -- all the constrained defining a polyhedron are non-strict
		System.out.println(cp2 + "\t " + cp2.is_topologically_closed());
		if(cp.contains(cp2)){
			cp.H79_widening_assign(cp2, null);
			System.out.println("cp2 in cp" + cp);
		} else if(cp2.contains(cp)) {
			cp2.H79_widening_assign(cp, null);
			System.out.println("cp in cp2 " + cp2);
		} else {
			System.out.println("cannot wide");
		}
		System.out.println("before " + cp2);
		System.out.println("before " + cp3);
		Variables_Set vs = new Variables_Set();
		vs.add(var);
		cp2.unconstrain_space_dimensions(vs);
		cp2.add_constraints(cp3.constraints());
		System.out.println("all gone ? " + cp2);
		Rational_Box rb = new Rational_Box(2, Degenerate_Element.UNIVERSE);
		rb.CC76_narrowing_assign(rb);
		
		Int32_Box b = new Int32_Box(1, Degenerate_Element.UNIVERSE);
		//b.add_constraints(cp2.constraints());
		cs_ppl = new Constraint(new Linear_Expression_Coefficient(new Coefficient(8)), Relation_Symbol.EQUAL,  new Linear_Expression_Variable(var));
		//b.add_constraint(cs_ppl);
		cs_ppl = new Constraint(new Linear_Expression_Coefficient(new Coefficient(100)).unary_minus(), Relation_Symbol.LESS_OR_EQUAL,  new Linear_Expression_Variable(var));
		b.add_constraint(cs_ppl);
		cs_ppl = new Constraint(new Linear_Expression_Coefficient(new Coefficient(-80)).unary_minus(), Relation_Symbol.GREATER_OR_EQUAL,  new Linear_Expression_Variable(var));
		b.add_constraint(cs_ppl);
		for(Constraint c : b.constraints()){
			System.out.println("c " + c + " " + c.kind());
		}
		System.out.println(b.contains_integer_point());
		
		Coefficient c  = new Coefficient(-3);
		System.out.println(c.getBigInteger().intValue());
		
		System.out.println("bNeg " + b);
		Int32_Box bNeg = BoxState.negate(b);
		System.out.println("bNeg " + bNeg);
		
	}

}
