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
	//static String startNode = "4";
	static String filePrefixOrig = "./ScratchData/results/invariants/"+className+"_"+methodId+"_";
	static String filePrefixComb = "./ScratchData/results/combined/"+className+"_"+methodId+"_";
	static String type = "c1";//c1 for pseudo-conditional and c2 for true conditional
	public static void main(String [] strs) throws IOException{

		//file that contains the prefix and the time it took to run
		String timeFileName = "./ScratchData/resultsRD/time/"+className+"_"+methodId;
		System.out.println("timeFileName " + timeFileName);
		File timeFile = new File(timeFileName);
		if(timeFile.exists()){
			//get the data into the map and order it 
			Map<String, List<Integer>> pathRuns = new HashMap<String, List<Integer>>();
			Scanner scan = new Scanner(timeFile);
			while(scan.hasNextLine()){
				String[] line = scan.nextLine().split("\t");
				System.out.println(line[0]);
				String analysisType = line[0];
				if(analysisType.equals(type)){
					String path = line[1];
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
			//remove the empty key since we do not use it in the comparison
			averPath.remove(fullTime);
			//should be ordered now
			System.out.println(averPath);
			//print overall time result in some kind of file?
			String timeOutput ="";			
			//for each entry generate the output file in combined
			//the first should just copy the file without changes
			int fileCount = 1;
			//the accumulated map of statements and their RD data
			//Statement -> variable -> its reaching definitions
			Map<String,Map<String,Set<Integer>>> currentRDValue = new HashMap<String, Map<String,Set<Integer>>> ();
			Map<String,Map<String,Set<Integer>>> allRDValue = process("./ScratchData/resultsRD/invariants/"+className+"_"+methodId);
			System.out.println(allRDValue);
			int totalElements = 0;//compare(allRDValue,currentRDValue);
			for(Entry<Integer, String> e : averPath.entrySet()){
				System.out.println("combing for time " + e.getKey());
				Map<String,Map<String,Set<Integer>>> newRDValue = process("./ScratchData/resultsRD/invariants/"+className+"_"+methodId+"_"+e.getValue()+"_"+type);
				//Map<String,Map<String,Set<Integer>>> combinedRDValue = combine(currentRDValue, newRDValue);
				int newElements = 0;//compare(combinedRDValue, currentRDValue);
				timeOutput +=className +"\t" + methodId +"\t" + e.getValue() +"\t" + fileCount + "\t" + fullTime + "\t" + e.getKey()+ "\t"+newElements+"\t"+totalElements+"\n";
				fileCount++;
			}
			//write timeOutput to a file
			//			String timeOutFileName = "./ScratchData/results/time/time"+dom;
			//			File timeOutFile = new File(timeOutFileName);
			//			if(!timeOutFile.exists()){
			//				timeOutFile.createNewFile();
			//			}
			//			FileWriter timeOutWrite = new FileWriter(timeOutFile, true);
			//			timeOutWrite.write(timeOutput);
			//			timeOutWrite.close();
		} else {
			System.out.println("Connot fine time file " + timeFileName);
		}

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