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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Take two files and produce the 
 * disjunction of each formula
 * @author elenasherman
 *
 */
public class CombineConditionalTimeRD {

	//static LinkedHashMap<Integer,String> lineCount = new LinkedHashMap<Integer,String>();
	static String className = "test.Example1M";
	static String methodId = "4";
	static String type = "c2";//c1 for pseudo-conditional and c2 for true conditional
	static String path = "./ScratchData/resultsRD";
	public static void main(String [] args) throws IOException{

		//to use with a script that passes classes and methods
		if(args.length > 0){
			//get the input
			path = args[0];
			className = args[1];
			methodId = args[2]; 
			type = args[3];
		}

		//file that contains the prefix and the time it took to run
		String timeFileName = path+"/time/"+className+"_"+methodId;
		//System.out.println("timeFileName " + timeFileName);
		File timeFile = new File(timeFileName);
		if(timeFile.exists()){
			//get the data into the map and order it 
			Map<String, List<Integer>> pathRuns = new HashMap<String, List<Integer>>();
			Scanner scan = new Scanner(timeFile);
			while(scan.hasNextLine()){
				String[] line = scan.nextLine().split("\t");
				//System.out.println(line[0]);
				String analysisType = line[0];
				if(analysisType.equals(type)||analysisType.equals("f")){
					String path = line[1];
					//System.out.println(path);
					Integer time = Integer.parseInt(line[2]);
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
			SortedMap<Float, String> averPath = new TreeMap<Float, String>();
			float fullTime = 0;
		
			for(Entry<String, List<Integer>> e : pathRuns.entrySet()){
				float average = 0;
				for(Integer i : e.getValue()){
					average+=i;
				}
				average = average/e.getValue().size();
				//offset a bit in case the same value is already there
				while(averPath.containsKey(average)){
					average += 0.0001;
				}
				if(e.getKey().isEmpty()){
					//it means the average for the fullpath
					fullTime = average;
				} else {
					averPath.put(average, e.getKey());
				}
			}
			
			//System.out.println("size2 " + averPath.size() + " " + fullTime);

			//should be ordered now
			//System.out.println(averPath.size());
			//print overall time result in some kind of file?
			String resultsOutput ="";			
			//for each entry generate the output file in combined
			//the first should just copy the file without changes
			int fileCount = 1;
			//the accumulated map of statements and their RD data
			//Statement -> variable -> its reaching definitions
			Map<String,Map<String,Set<Integer>>> currentRDValue = new HashMap<String, Map<String,Set<Integer>>> ();
			Map<String,Map<String,Set<Integer>>> allRDValue = process(path+"/invariants/"+className+"_"+methodId);
			int currInvCount = 0;
			int totalElements = countInvariants(allRDValue);
			//System.out.println(totalElements);
			for(Entry<Float, String> e : averPath.entrySet()){
				//System.out.println("combing for time " + e.getKey());
				Map<String,Map<String,Set<Integer>>> newRDValue = process(path+"/invariants/"+className+"_"+methodId+"_"+e.getValue()+"_"+type);
				if(currInvCount != 0){
					combine(currentRDValue, newRDValue);
				}
				currInvCount = countInvariants(newRDValue);
				currentRDValue = newRDValue;
				//				int newElements =  newInvCount - currInvCount;
				resultsOutput +=className +"\t" + methodId +"\t" + e.getValue() +"\t" + fileCount + "\t" + fullTime + "\t" + e.getKey()+ "\t"+currInvCount+"\t"+totalElements+"\n";
				fileCount++;
			}
			//			System.out.println(resultsOutput);
			//			System.out.println(allRDValue);
			//			System.out.println(currentRDValue);
			//write resultOutput to a file
			String resultOutFileName = path+"/time/time_"+type;
			File resultOutFile = new File(resultOutFileName);
			if(!resultOutFile.exists()){
				resultOutFile.createNewFile();
			}
			FileWriter timeOutWrite = new FileWriter(resultOutFile, true);
			timeOutWrite.write(resultsOutput);
			timeOutWrite.close();
		} else {
			System.out.println("Connot fine time file " + timeFileName);
		}

	}

	/**
	 * augments the newRDValue with currentRDvalues
	 * @param currentRDValue
	 * @param newRDValue
	 * @return
	 */
	private static void combine(Map<String, Map<String, Set<Integer>>> currentRDValue,
			Map<String, Map<String, Set<Integer>>> newRDValue) {
		//System.out.println(newRDValue);
		for(String stmt : newRDValue.keySet()){
			Map<String,Set<Integer>> newMap = newRDValue.get(stmt);
			Map<String,Set<Integer>> currMap = currentRDValue.get(stmt);
			Set<String> combinedKeySet = new HashSet<String>();
			combinedKeySet.addAll(newMap.keySet());
			//System.out.println(stmt + " " + newMap.keySet() + " " + currMap.keySet());
			combinedKeySet.addAll(currMap.keySet());
			for(String var : combinedKeySet){
				if(currMap.containsKey(var) && newMap.containsKey(var)){
					Set<Integer> newRdSet = newMap.get(var);
					Set<Integer> currRdSet = currMap.get(var);
					//merge them together
					newRdSet.addAll(currRdSet);
				} else if(currMap.containsKey(var)){
					//create a set for that var and add all entyr to the newRDVal map
					Set<Integer> newRdSet = new HashSet<Integer>();
					newRdSet.addAll(currMap.get(var));//do a deep copy just in case
					newMap.put(var, newRdSet);
				} //otherwise do nothing - newRDVal just keeps it
			}
		}
		//System.out.println(newRDValue);
	}
	/**
	 * Computes the number of new invariants discovered by the additional analysis. 
	 * @param newValues - the map with the new (combined) values
	 * @param oldValue - the map of the previous values
	 * @return
	 */
	private static int countInvariants(Map<String, Map<String, Set<Integer>>> values) {
		int invCount = 0;
		for(Map <String, Set<Integer>> val : values.values()){
			invCount += val.values().size();
		}
		return invCount;
	}
	/**
	 * 
	 * @param string - the file name of the RD data, i.e., invariants
	 * @return
	 * @throws FileNotFoundException 
	 */
	private static Map<String, Map<String, Set<Integer>>> process(String fileName) throws FileNotFoundException {
		Map<String, Map<String, Set<Integer>>> data = new HashMap<String,Map<String, Set<Integer>>>();
		File invariantFile = new File(fileName);
		if(invariantFile.exists()){
			Scanner scan = new Scanner(invariantFile);
			Map<String,Set<Integer>> stmt = null;
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				//System.out.println(line + " " + line.matches("^\\d(.*)"));
				if(line.matches("^\\d(.*)")){
					//the it is the actual line number
					//create the map for it
					stmt = new HashMap<String, Set<Integer>>();
					//added to the data map
					data.put(line, stmt);

				} else if(!line.isEmpty()){
					//we are reading variables and their RD values
					String[] varLine = line.split(":");
					String var = varLine[0];
					//create the set for that var
					Set<Integer> varDataSet = new HashSet<Integer>();
					//add the set to the stmt map
					stmt.put(var, varDataSet);
					for(int i = 1; i < varLine.length; i++){
						int varData = Integer.parseInt(varLine[i].trim());
						varDataSet.add(varData);
					}
				} 
			}
			scan.close();
		} else {
			System.out.println("Cannot find invariant file " + fileName);
		}
		return data;
	}

}
///*
//else if(line1.startsWith("*") || line2.startsWith("*")){
////found where they are taking different branches
//System.out.println("conditional " + line1 + " " + line2);
//*/