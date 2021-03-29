package driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import conditional.analysis.ConditionalValue;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartConditionalValue {
	//eas: make sure artifacts is a source folder.
	public static boolean print = true;
	public static boolean writeToFile = true;
	public static boolean writeTime = true;
	public static String className;
	public static String methodId;
	public static String domain;
	public static String condition;
	public static String path;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String className = "test.Example1M";
		String methodId = "4";
		String domain = "dom4.txt";
		String symbolic = "sN";
		String condition = "1f1t";
		path = "./ConditionalTACAS/resultsVA/";

		if(args.length > 0){
			path = args[0];
			className = args[1];
			methodId = args[2];
			domain = args[3]+".txt";
			symbolic = "sN";
			condition = args[4];
			writeToFile = args[5].equals("y");
		}
		
		StartConditionalValue.methodId = methodId;
		StartConditionalValue.className = className;
	
		
//		try {
//			timeDataFile = new FileWriter(resultsPath+"timeData",true);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
				StartConditionalValue.domain = domain.split("\\.")[0];
				analysisType = StartConditionalValue.domain + "_"+ symbolic;
				try {	
					System.out.println(condition);
					StartConditionalValue.condition = condition.isEmpty()?"":condition.replaceAll(",", "");
					new StartConditionalValue(className, domain, /*symbolicOn, */condition, methodId);
					G.reset();
					System.gc();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//		try {
//			timeDataFile.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	//The class should have static fields for the files to write to
	//className_sY_domainName
	//where className is the name of class being analyzed (with all its methods)
	//domainName is the domain that the analysis uses
	//sY means using symbolic helper state and sN means not using symbolic helper state.
	
	
	public static FileWriter fileToWrite;
	public static FileWriter timeDataFile;
	public static String analysisType;
	
	//each instance should open/close that file
	
	public StartConditionalValue(String className, String domainFile, /*String symbolicHelper,*/ String condition, String methodId) throws IOException{
		String domainPath = path+"/domains/";
		//private static String conditionPath = "ScratchData/conditions/";
		String resultsPath = path+"/invariants/c2/";
		//instantiate the list of domains from a file
		String domainDescription = domainPath+domainFile;
		DomainReader dr = new DomainReader(domainDescription);
		List<Domain> domain = dr.getReadDomains();
		System.out.println(domain);
		//file that contains the conditions to be excluded.
		//1f say that the first encountered branch -- do not explore false branch,
		//i.e., propagate bot element there
		//1f, 3t means exclude 1f and 3t branches. We will assume a BFS ordering.
		//String conditions = "20f,29t";//20t,29f
		//create the file to write to
		if(writeToFile){
			String fileName = resultsPath+className+"_"+methodId +"_"+condition.replaceAll(",", "")+"_"+domainFile;
			fileToWrite = new FileWriter(fileName);
		//boolean symbolicOn = symbolicHelper.equals("sY");
		}
		String[] sootArgs = {"-f", "n", className};
		PackManager.v().getPack("jtp").
			add(new Transform("jtp.disjoint", new ConditionalValue(domain, Integer.parseInt(methodId),condition)));
		//adding runtime to the path
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
				+ ":" + System.getProperty("sun.boot.class.path"));
		//run soot
		soot.Main.main(sootArgs);
		if(writeToFile){
			fileToWrite.close();
		}
	}

}
