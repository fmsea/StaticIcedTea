package processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.Set;

import javax.swing.plaf.synth.SynthSeparatorUI;

import conditional.partition.AbstractedCFG;
import conditional.partition.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Take two files and produce the 
 * disjunction of each formula
 * @author elenasherman
 *
 */
public class CombinePartial {
	
	//static LinkedHashMap<Integer,String> lineCount = new LinkedHashMap<Integer,String>();
	static String className = "test.BallonFactory";
	static String methodId = "9";
	static String filePrefix = "./ScratchData/results/"+className+"_"+methodId+"_sN_";
	static String dom = "_dom9.txt";
	public static void main(String [] strs) throws IOException{
		
		String acfgName = "./ScratchData/conditions/"+className+"_"+methodId+".txt";
		//instantiate ACFG from the file
		AbstractedCFG aCFG = new AbstractedCFG(acfgName);
		//System.out.println(aCFG.toString());
		String ret = prefix("", aCFG.findNode("1"));
		System.out.println(ret);
	}
	
	public static String prefix(String prefix, Node n) throws IOException{
		String ret = prefix;
		if(!n.getName().equals("end")){
			String prefixT = prefix(prefix+n.getName()+"t", n.getTrue());
			String prefixF = prefix(prefix+n.getName()+"f", n.getFalse());
			combine(prefixT,prefixF, prefix);
			//get the corresponding files
			System.out.println("T " + prefixT + " F " + prefixF);
			//create a new file name with prefix name
			System.out.println("P " + prefix);
		}
		
		return ret;
	}

public static void combine(String p1, String p2, String p) throws IOException{		
		
		String file1Name = filePrefix +p1+dom;
		String file2Name = filePrefix+p2+dom;
		
		//file to write the combine output to
		Writer fileOut = new FileWriter(filePrefix+p+dom);
		String writeTo ="";
		//read a line from each file
		LinkedHashMap<Integer,String> lineCount = new LinkedHashMap<Integer,String>();
		Map<String,Map<String,String>> file1Map = createMap(file1Name, lineCount);
		Map<String,Map<String,String>> file2Map = createMap(file2Name, lineCount);
		//now we need to assemble them together.
		List <Integer> orderedStmt = new ArrayList<Integer>();
		orderedStmt.addAll(lineCount.keySet());
		Collections.sort(orderedStmt);
		for(Integer lineId : orderedStmt){
			if(!writeTo.equals("")){
				fileOut.write(writeTo);
				writeTo="";
			}
			String stmt = lineCount.get(lineId);
			System.out.println(stmt);
			writeTo +=stmt+"\n";
			Map<String,String> val1Map = file1Map.get(stmt);
			Map<String, String> val2Map = file2Map.get(stmt);
			if(val1Map == null){
				//go over val2Map;
				for(String var : val2Map.keySet()){
					//System.out.println(var + "->" + val2Map.get(var));
					writeTo +=var + "->" + val2Map.get(var)+"\n";
				}
			} else if (val2Map == null){
				//go over  val1Map;
				for(String var : val1Map.keySet()){
					//System.out.println(var + "->" + val1Map.get(var));
					writeTo +=var + "->" + val1Map.get(var)+"\n";
				}
			} else {
				//should iterate over the union of both keys
				Set<String> allVars = new HashSet<String>();
				allVars.addAll(val1Map.keySet());
				allVars.addAll(val2Map.keySet());
//				System.out.println(val1Map);
//				System.out.println(val2Map);
				for(String var : allVars){
					//get the formulas for each
					String formula1 = val1Map.get(var);
					String formula2 = val2Map.get(var);
					if(formula1 == null && formula2 !=null){
						//System.out.println(var+"->"+formula2);
						writeTo +=var + "->" + formula2+"\n";
					} else if(formula2 == null && formula1 != null){
						//System.out.println(val1Map);
						//System.out.println(val2Map);
						//System.out.println(var+"-->" + formula1);
						writeTo +=var + "->" + formula1+"\n";
					} else if(formula2 != null && formula1 != null){
						if(formula1.equals(formula2)){
							//System.out.println(var+"->" + formula1);
							writeTo +=var + "->" + formula1+"\n";
						} else {
							//System.out.println(var+"->(or " + formula1 +" " + formula2 + ")");
							writeTo +=var+"->(or " + formula1 +" " + formula2 + ")\n";
						}
					} else {
						System.out.println("Nonthing for " + var);
					}
				}
			}
		}//end for loop
		//write the last set of lines
		fileOut.write(writeTo);
		fileOut.flush();
		fileOut.close();
		
//		if(file1.exists() && file2.exists()){
//		Scanner scan1 = new Scanner(file1);
//		Scanner scan2 = new Scanner(file2);
//		String line1 = "";
//		String line2 ="";
//		int num1 = 0;
//		int num2 = 0;
//		while(scan1.hasNextLine() && scan2.hasNextLine()){
//			if(num1 == num2){
//				//if previous are the same then scan together.
//				line1 = scan1.nextLine();
//				line2 = scan2.nextLine();
//				//check in neither of them contain
//				num1 = Integer.parseInt(line1.split(" ")[0]);
//				num2 = Integer.parseInt(line2.split(" ")[0]);
//			} else if (num1 > num2){
//				//scan only line 2
//				line2 = scan2.nextLine();
//				num2 = Integer.parseInt(line2.split(" ")[0]);
//			} else {
//				//scan only line 1
//				line1 = scan1.nextLine();
//				num1 = Integer.parseInt(line1.split(" ")[0]);
//			}
//			//advance to the next line
//			
//			System.out.println("l1 " + line1);
//			System.out.println("l2 " + line2);
//			//get the numbers
//			
//			//check if the next number are the same
//			if(num1 == num2){
//				//then we can compare their one
//				//or two lines
//				if(line1.contains(" if ")){
//					String s1t = "";
//					String s1f = "";
//					String s2t = "";
//					String s2f = "";
//					//conditional statement
//					//should have two outputs
//					//scan the first line and
//					String s11 = scan1.nextLine();
//					String s12 = scan1.nextLine();
//					String s21 = scan2.nextLine();
//					String s22 = scan2.nextLine();
//					if(s11.startsWith("*")){
//						if(s11.endsWith("f")){
//							s1f = s12;
//						} else {
//							s1t = s12;
//						}
//					} else {
//						s1t = s11;
//						s1f = s12;
//					}
//					if(s21.startsWith("*")){
//						if(s21.endsWith("f")){
//							s2f = s22;
//						} else 
//							s2t = s22;
//					} else {
//						s2t = s21;
//						s2f = s22;
//					}
//					//now the branches are defined
//					System.out.println("t " + s1t + " or " + s2t);
//					System.out.println("f " + s1f + " or " + s2f);
//					//now we need to check whether there is a set 
//					//of second variables in the cond sttmt
//				} else {
//					//regular assignment statement
//					String s1 = scan1.nextLine();
//					String s2 = scan2.nextLine();
//					if(s1.equals(s2)){
//						System.out.println(s1);
//					} else {
//						//parse and use the solver to compare them.
//						//do disjunction
//						System.out.println(s1 + " or " + s2);
//					}
//				}
//			} else if(num1 > num2){
//				//put values of num2 first
//				System.out.println(line2);
//				if(line2.contains(" if ")){
//					//do two scans 
//					String t = scan2.nextLine();
//					String f = scan2.nextLine();
//					String sf ="";
//					String st ="";
//					if(t.startsWith("*")){
//						if(t.endsWith("f")){
//							sf = f;
//						} else {
//							st = f;
//						}
//					} else {
//						st = t;
//						sf = f;
//					}
//					System.out.println(st);
//					System.out.println(sf);
//				} else {
//					//do only one scan
//					String s = scan2.nextLine();
//					System.out.println(s);
//				}
//			} else {
//				System.out.println(line1);
//				if(line1.contains(" if ")){
//					//do two scans 
//					String t = scan1.nextLine();
//					String f = scan1.nextLine();
//					String sf ="";
//					String st ="";
//					if(t.startsWith("*")){
//						if(t.endsWith("f")){
//							sf = f;
//						} else {
//							st = f;
//						}
//					} else {
//						st = t;
//						sf = f;
//					}
//					System.out.println(st);
//					System.out.println(sf);
//				} else {
//					//do only one scan
//					String s = scan1.nextLine();
//					System.out.println(s);
//				}
//			}
//			 
//		}
//		
//		scan1.close();
//		scan2.close();
//		} else {
//			System.out.println("Cannot find " + file1.exists() + " " + file2.exists());
//			System.exit(2);
//		}
		
	}
	

	/**
	 * for now we will work with strings
	 * and after that create a parser
	 * for smt2 format to instantiate
	 * constraints for better combining power
	 * method return map of
	 * stmt -> variable -> smt2 formula for it
	 * variable for a conditional stmt can be t or f
	 * @throws FileNotFoundException 
	 */
	public static Map<String,Map<String,String>> createMap(String fileName, Map<Integer,String> lineCount) throws FileNotFoundException{
		Map<String,Map<String,String>> ret = new HashMap<String, Map<String,String>>();
		System.out.println(fileName);
		File file = new File(fileName);
		if(file.exists()){
			//create a scanner
			Map<String,String> stmtTo = null;
			String formula = "";
			String var = "";
			Scanner scan = new Scanner(file);
			while(scan.hasNextLine()){
				String ln = scan.nextLine();
				if(ln.matches("^[0-9].*")){
					//finish the previous var
					//in case both branches are infeasible
					if(stmtTo != null && var != "" && formula!=""){
						//if not the first iteration
						stmtTo.put(var, formula);
						//clear out for the next variable
						var="";
						formula="";
					}
					//System.out.println("Stmt " + ln);
					//found a new statement
					//create a map for it
					stmtTo = new HashMap<String,String>();
					ret.put(ln, stmtTo);
					//get the line number
					//System.out.println(ln.split(" ")[0]);
					int lineNum = Integer.parseInt(ln.split(" ")[0]);
					if(!lineCount.containsKey(lineNum)){
						lineCount.put(lineNum, ln);
					}
				} else if(ln.contains("->")){
					//new variable is seen
					if(!formula.equals("") && !var.equals("")){
						//put the previous var into the map
						stmtTo.put(var, formula);
					}
					//System.out.println("ln " + ln);
					String[] data = ln.split("->");
					var = data[0];
					formula = data[1];
				} else if (ln.startsWith("*")){
					//the branch where it was parted
					//System.out.println("matches *");
				} else {
					//formula might have several lines
					//so all other lines are nothing else
					//but the continuation of that formula
					formula += ln.trim();
				}
			
			} //end of the scanner loop
			//add the last var 
			if(var != "" && formula!=""){
				stmtTo.put(var, formula);
			}
			scan.close();
		} else {
			System.out.println("Cannot find file, exiting " + fileName);
			System.exit(2);
		}
		return ret;
	}
}
///*
//else if(line1.startsWith("*") || line2.startsWith("*")){
////found where they are taking different branches
//System.out.println("conditional " + line1 + " " + line2);
//*/