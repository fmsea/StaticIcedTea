package pseudo.analysis;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.z3.BoolExpr;

import disjoint.analysis.ValueAnalysis;
import disjoint.domain.Domain;
import disjoint.driver.StartAnalysisKestrel;
import soot.Local;
import soot.Timers;
import soot.Unit;
import soot.grimp.internal.GAndExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.Expr;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import disjoint.state.*;
import driver.StartPseudoConditionalValue;
import driver.StartAnalysisScript;

public class PseudoConditionalValueAnalysis extends ValueAnalysis {

	//branches to be excluded that is \pi condition
	Map<IfStmt, Boolean> include; //the opposite will be excluded
    UnitGraph graph;



	public PseudoConditionalValueAnalysis(UnitGraph graph, List<Domain> domain, Map<IfStmt, Boolean> include ) {
		super(graph, domain, false);
		this.include = include;
		this.graph = graph;
		System.out.println("analysis");
		//actual fixed-point iterations
		//super.doAnalysis();

		//post processing
		//printOutput(graph);
	}

//@Override
//public void report(){
//		Chain<Local> locals = graph.getBody().getLocals();
//		Iterator<Unit> iter = graph.getBody().getUnits().iterator();
//		int stmtCount = 0;
//		//File to write the output to
//		while(iter.hasNext()){
//			Unit u = iter.next();
//			stmtCount++;
//			//String that keeps that state info for the current state
//			String output = stmtCount + " " + u +":" + graph.getBody().getMethod().getSignature() + "\n";
//			System.out.println(output);
//			AbstractState fall = getFallFlowAfter(u);
//			if(!fall.getStates().isEmpty() && fall.isFeasible()){
//				for(Local l : locals){
//					if(outputStmt.contains(u)  && changedVariables.get(u).contains(l)){
//						System.out.println(" l " + l + " u " + u + " \n" + fall);
//						BoolExpr smt2State = (BoolExpr)generateExpr(fall, l);
//						System.out.println("l->" + smt2State);
//					}
//				}
//				//for other branch if exists
//				List<AbstractState> branches = getBranchFlowAfter(u);
//				if(!branches.isEmpty()){
//					for(AbstractState branch : branches){
//						if(!branch.getStates().isEmpty() && branch.isFeasible()){
//							for(Local l : locals){
//								if(changedVariables.get(u).contains(l)){
//									System.out.println(" lf " + l + " u " + u + " \n" + fall);
//									BoolExpr smt2State = generateExpr(branch, l);
//									System.out.println(l+"f" + "->" + smt2State);
//								}
//							}
//						}
//
//					}
//				}
//			}
//
//		}
//	}
	
	//@Override
	public void report(){
	System.out.println("Done in " + (end - start)+" fn " + Timers.v().totalFlowNodes + ", fc"+ Timers.v().totalFlowComputations + "\n");
	String timeData = "c1\t" + StartPseudoConditionalValue.condition + "\t"+ (end - start)+"\n";
	
	if(StartPseudoConditionalValue.writeTime){
	try {
		//StartAnalysisKestrel.timeDataFile.append(timeData);
		String timeDataFile = StartPseudoConditionalValue.path + "/time/"+StartPseudoConditionalValue.className+"_"+StartPseudoConditionalValue.methodId+"_"+StartPseudoConditionalValue.domain+".txt";
		RandomAccessFile rf = new RandomAccessFile(timeDataFile, "rwd");
		FileChannel fileChannel = rf.getChannel();
		FileLock lock = fileChannel.lock();
		fileChannel.position(fileChannel.size());
		fileChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(timeData)));
		fileChannel.force(false);
		lock.release();
		fileChannel.close();
		rf.close();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	}
	//if(StartAnalysisScript.print){
	if(true){
	Iterator<Unit> iter = b.getUnits().iterator();
	int stmtCount = 0;
	//File to write the output to
	while(iter.hasNext()){
		Unit u = iter.next();
		//check against statement after
		//which state has been changed
		stmtCount++;
		if(outputStmt.contains(u)){
			String output = "";
			//String that keeps that state info for the current state

			output += stmtCount + " " + u +":" + b.getMethod().getSignature() + "\n";
			if(include.containsKey(u)){
			if(include.get(u)){
				output +="*"+stmtCount+ "t\n";
			} else {
				output +="*"+stmtCount+"f\n";
			}
		}
			//System.out.println(output);
			AbstractState fall = getFallFlowAfter(u);
			if(!fall.getStates().isEmpty() && fall.isFeasible()){
				for(Local l : b.getLocals()){
					if(changedVariables.get(u).contains(l)){
						//System.out.println(" l " + l + " u " + u + " \n" + fall);
						//System.out.flush();
						Set<BinopExpr> varPerState = evaluateStates(fall, l);
						Expr state = null;
						for(Expr be : varPerState){
							if(state == null){
								state = be;
							} else {
								state = new GAndExpr(state, be);
							}
						}
						output += l + "->" + solver.generate((BinopExpr)state) + "\n";
						//System.out.println(l + "->" + solver.generate((BinopExpr)state));
					}
				}
			}
			//for other branch if exists
			List<AbstractState> branches = getBranchFlowAfter(u);
			if(!branches.isEmpty()){
				for(AbstractState branch : branches){
					if(!branch.getStates().isEmpty() && branch.isFeasible()){
						for(Local l : b.getLocals()){
							if(changedVariables.get(u).contains(l)){
								//System.out.println(" lf " + l + " u " + u + " \n" + fall);
								Set<BinopExpr> varPerState = evaluateStates(branch, l);
								Expr state = null;
								for(Expr be : varPerState){
									if(state == null){
										state = be;
									} else {
										state = new GAndExpr(state, be);
									}
								}
								output += l+"f" + "->" + solver.generate((BinopExpr)state) + "\n";
								//System.out.println(l+"f" + "->" + solver.generate((BinopExpr)state));
							}
						}
					}

				}
			}
			if(StartPseudoConditionalValue.writeToFile){
			//write the string to the file
			try {
				StartPseudoConditionalValue.fileToWrite.write(output);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			} else {
				//System.out.println(output);
			}
		}//end outputStmt check
	
}
	}
}

	@Override
	protected void flowThrough(AbstractState in, Unit u, List<AbstractState> fallIn, List<AbstractState> branchOut) {
		Stmt s = (Stmt) u;
		AbstractState inState = in;
		AbstractState ifStmtTrue = inState.copy();//outbranch only when s is a branch
		AbstractState ifStmtFalse = inState.copy();//followed after s

		
		//only process feasible branches
		if(in.isFeasible()){
			if(s instanceof AssignStmt){
				//process as usual
				processAssignStmt((AssignStmt)s, inState, ifStmtFalse);
			} else if (s instanceof IfStmt){
				//System.out.println("---------------------------------------"+"\n"+u);
				//System.out.println("In state PA \t" + in + " " + in.isFeasible());
				//for now we will process it as usual:
				processIfStmt((IfStmt)s, inState, ifStmtFalse, ifStmtTrue);
				//then set one of the branches to be infeasible
				if(include.containsKey(s)){
//					System.out.println("-----------------------------");
//					System.out.println("false/fall through " + ifStmtFalse.isFeasible());
//					System.out.println("true/fall out " + ifStmtTrue.isFeasible());
//					System.out.println("include " + s + " " + include.get(s));
					if(include.get(s)){
						//exclude branch
						ifStmtFalse.setInfeasible();
					} else {
						ifStmtTrue.setInfeasible();
					}
//					System.out.println("false/fall through " + ifStmtFalse.isFeasible());
//					System.out.println("true/fall out " + ifStmtTrue.isFeasible());
				}
				//TODO: re-factor processIfStmt so it will not calculate values
				//for excluded branches.
				
			}
		}
		
		for(Iterator<AbstractState> it = fallIn.iterator(); it.hasNext();){
			copy(ifStmtFalse, it.next());
		}

		//System.out.println("FallIn " + fallIn);

		for(Iterator<AbstractState> it = branchOut.iterator(); it.hasNext();){
			copy(ifStmtTrue, it.next());
		}

	}

	//------ eas: no need to override anything right now
	/*
	 * This method should be used for \Theta condition
	 * which should be expressed as a logical formula 
	 * perhaps in smt2 format
	 */
	//	@Override
	//	protected AbstractState entryInitialFlow(){
	//		AbstractState as = new AbstractState(states);
	//		as.setEntryFlow();
	//		return as;
	//	}
	//	@Override
	//	protected AbstractState newInitialFlow() {
	//		AbstractState as = new AbstractState();
	//		as.setInitialFlow();
	//		return as;
	//	}
	//
	//
	//
	//	@Override
	//	protected void copy(AbstractState source, AbstractState dest) {
	//		dest.copy(source);
	//	}
	//
	//	@Override
	//	protected void merge(AbstractState in1, AbstractState in2, AbstractState out) {
	//		if(in1.isFeasible() && in2.isFeasible()){
	//			out.copy(in1.merge(in2)); //regular
	//		} else if (!in2.isFeasible()){
	//			copy(in1,out); //if in2 is infeasible then copy in1 to out, does not matter if in1 is feasible or not
	//		} else if (!in1.isFeasible()){
	//			copy(in2,out); //if in2 is feasible but in1 is infeasible then copy in2
	//		}
	//
	//	}

}
