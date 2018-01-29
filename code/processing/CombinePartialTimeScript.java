package processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.Set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Take two files and produce the 
 * disjunction of each formula
 * @author elenasherman
 *
 */
public class CombinePartialTimeScript {

	//static LinkedHashMap<Integer,String> lineCount = new LinkedHashMap<Integer,String>();
	static String className = "test.BallonFactory";
	static String methodId = "2";
	//static String startNode = "4";
	static String filePrefixOrig;
	static String filePrefixComb;
	static String dom = "_dom4.txt";
	public static void main(String [] args) throws IOException{
		String dataPath = args[0];
		className = args[1];
		methodId = args[2]; 
		dom = "_"+args[3]+".txt";
		String type = args[4];
		filePrefixOrig = dataPath+"/invariants/"+type+"/"+className+"_"+methodId+"_";
		filePrefixComb = dataPath+"/combined/"+type+"/"+className+"_"+methodId+"_";
		//file that contains the prefix and the time it took to run
		String timeFileName = dataPath+"/time/"+className+"_"+methodId+dom;
		File timeFile = new File(timeFileName);
		if(timeFile.exists()){
			//get the data into the map and order it 
			Map<String, List<Integer>> pathRuns = new HashMap<String, List<Integer>>();
			Scanner scan = new Scanner(timeFile);
			while(scan.hasNextLine()){
				String[] line = scan.nextLine().split("\t");
				String typeIn = line[0];
				if(typeIn.equals(type) || typeIn.isEmpty()){
					String path = "";
					Integer time = 0;
					if(typeIn.isEmpty()){
						time = Integer.parseInt(line[1]);
					} else {
						path = line[1];
						time = Integer.parseInt(line[2]);
					}
					
					List<Integer> timeList = null;
					if(pathRuns.containsKey(path)){
						timeList = pathRuns.get(path);
					} else {
						timeList = new ArrayList<Integer>();
						pathRuns.put(path, timeList);
					}
					timeList.add(time);
				}
			}
			scan.close();
			//now we should have map populated 
			//and we need to calculate the average 
			//and put in the map Average -> path
			SortedMap<Integer, String> averPath = new TreeMap<Integer, String>();
			int fullTime = 0;
			for(Entry<String, List<Integer>> e : pathRuns.entrySet()){
				int average = 0;
				for(Integer i : e.getValue()){
					average+=i;
				}
				average = average/e.getValue().size();
				//offset a bit in case the same value is already there
				while(averPath.containsKey(average)){
					average++;
				}
				if(e.getKey().isEmpty()){
					//it means the average for the fullpath
					fullTime = average;
				} else {
					averPath.put(average, e.getKey());
				}
			}
			//should be ordered now
			System.out.println(averPath);
			//print overall time result in some kind of file?
			String timeOutput ="";			
			//for each entry generate the output file in combined
			//the first should just copy the file without changes
			int fileCount = 1;
			for(Entry<Integer, String> e : averPath.entrySet()){
				System.out.println("combing for time " + e.getKey());
				timeOutput +=className +"\t" + methodId +"\t" + e.getValue() +"\t" + fileCount + "\t" + fullTime + "\t" + e.getKey()+"\n";
				if(fileCount == 1){
					//just copy to the combined folder
					Path p1 = FileSystems.getDefault().getPath(dataPath+"/invariants/"+type+"/",className+"_"+methodId+"_"+e.getValue()+dom);
					Path p2 = FileSystems.getDefault().getPath(dataPath+"/combined/"+type+"/",className+"_"+methodId+"_"+fileCount+dom);
					Files.copy(p1, p2, StandardCopyOption.REPLACE_EXISTING);
				} else {
					//call the combine method
					combine(String.valueOf(fileCount-1),e.getValue(), String.valueOf(fileCount));
				}
				fileCount++;
			}
			//write timeOutput to a file
			String timeOutFileName = dataPath+"/time/time_"+type;
			File timeOutFile = new File(timeOutFileName);
			if(!timeOutFile.exists()){
				timeOutFile.createNewFile();
			}
			FileWriter timeOutWrite = new FileWriter(timeOutFile, true);
			timeOutWrite.write(timeOutput);
			timeOutWrite.close();
		} else {
			System.out.println("Connot fine time file " + timeFileName);
		}

	}

	//	public static String prefix(String prefix, Node n) throws IOException{
	//		String ret = prefix;
	//		if(!n.getName().equals("end")){
	//			String prefixT = prefix(prefix+n.getName()+"t", n.getTrue());
	//			String prefixF = prefix(prefix+n.getName()+"f", n.getFalse());
	//			combine(prefixT,prefixF, prefix);
	//			//get the corresponding files
	//			//System.out.println("T " + prefixT + " F " + prefixF);
	//			//create a new file name with prefix name
	//			System.out.println("P " + prefix);
	//		}
	//		
	//		return ret;
	//	}
	/**
	 * 
	 * @param p1 previous count
	 * @param p2 current prefix
	 * @param p current count
	 * @throws IOException
	 */
	public static void combine(String p1, String p2, String p) throws IOException{		

		String file1Name = filePrefixComb +p1+dom;
		String file2Name = filePrefixOrig+p2+dom;

		//file to write the combine output to
		Writer fileOut = new FileWriter(filePrefixComb+p+dom);
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
			//System.out.println(stmt);
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