package disjoint.driver;

import java.util.HashMap;

import soot.PackManager;
import soot.Scene;
import soot.Transform;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Log;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Version;
import com.microsoft.z3.Z3Exception;

import disjoint.analysis.ValueTransfomer;

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
		//run soot
		soot.Main.main(sootArgs);
	}
	
	private static void z3Test() throws Z3Exception{
		System.out.println("Trying to play with z3");
		Context.ToggleWarningMessages(true);
     Log.Open("test.log");

     System.out.print("Z3 Major Version: ");
     System.out.println(Version.Major());
     System.out.print("Z3 Full Version: ");
     System.out.println(Version.getString());
     
    // setting up properties
     HashMap<String, String> cfg = new HashMap<String, String>();
     Context ctx = new Context(cfg);
     Solver solver = ctx.MkSolver();
     
     IntExpr x = ctx.MkIntConst("x");
     
     IntExpr const0 = ctx.MkInt(0);
     IntExpr const2 = ctx.MkInt(-1);
     IntExpr const3 = ctx.MkInt(3);
     IntExpr const4 = ctx.MkInt(4);
     solver.Push();
     BoolExpr e1 = ctx.MkGt(x, const0);//ctx.MkGt(ctx.MkMul(new ArithExpr[] { x, ctx.MkAdd(new ArithExpr[]{x,const4}) }), const0);
     BoolExpr e2 = ctx.MkEq(ctx.MkMod(const3, (IntExpr)ctx.MkAdd(new ArithExpr[]{x,const2})), const0);
     BoolExpr e3 = ctx.MkEq(x, const0);
     long startTime = System.nanoTime();
     solver.Assert(e1);
     long endTime = System.nanoTime();
     long duration = endTime - startTime;
     System.out.println(solver.Check() + " " + duration);
	}

}
