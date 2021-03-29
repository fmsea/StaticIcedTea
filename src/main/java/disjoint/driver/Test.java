package disjoint.driver;

import java.util.HashMap;

import soot.Scene;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Log;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Version;
import com.microsoft.z3.Z3Exception;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//test soot
		sootTest("test.Example1M");
		//test z3
		try {
			z3Test();
		} catch (Z3Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void sootTest(String className){
		String[] sootArgs = {"-f", "n", className};
		//adding runtime to the path
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
				+ ":" + System.getProperty("sun.boot.class.path"));
		System.out.println(Scene.v().getSootClassPath());
		//run soot
		soot.Main.main(sootArgs);
	}
	
	private static void z3Test() throws Z3Exception{
		System.out.println("Trying to play with z3");
		//Context.ToggleWarningMessages(true);
        //Log.Open("test.log");

     System.out.print("Z3 Major Version: ");
     System.out.println(Version.getMajor());
     System.out.print("Z3 Full Version: ");
     System.out.println(Version.getString());
     
    // setting up properties
     HashMap<String, String> cfg = new HashMap<String, String>();
     Context ctx = new Context(cfg);
     Solver solver = ctx.mkSolver();
     
     IntExpr x = ctx.mkIntConst("x");
     
     IntExpr const0 = ctx.mkInt(0);
     IntExpr const2 = ctx.mkInt(-1);
     IntExpr const3 = ctx.mkInt(3);
     IntExpr const4 = ctx.mkInt(4);
     solver.push();
     BoolExpr e1 = ctx.mkGt(x, const0);//ctx.MkGt(ctx.MkMul(new ArithExpr[] { x, ctx.MkAdd(new ArithExpr[]{x,const4}) }), const0);
     BoolExpr e2 = ctx.mkEq(ctx.mkMod(const3, (IntExpr)ctx.mkAdd(new ArithExpr[]{x,const2})), const0);
     BoolExpr e3 = ctx.mkEq(x, const0);
     long startTime = System.nanoTime();
     solver.assertAndTrack(e1, ctx.mkBoolConst("c1"));
     Long endTime = System.nanoTime();
     long duration = endTime - startTime;
     System.out.println(solver.check() + " " + duration);
	}

}
