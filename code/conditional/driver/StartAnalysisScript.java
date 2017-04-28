package conditional.driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import conditional.analysis.PartialTransformer;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartAnalysisScript {
	//eas: make sure artifacts is a source folder.
	public static boolean print = true;
	public static boolean writeToFile;
	public static String className;
	public static String methodId;
	public static String domain;
	public static String condition;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String className = args[0];
		String methodId = args[1];
		String domain = args[2]+".txt";
		String symbolic = "sN";
		String condition = args[3];
		writeToFile = args[4].equals("y") ? true : false;
			
		StartAnalysisScript.className = className;
		StartAnalysisScript.domain = domain.split("\\.")[0];
		StartAnalysisScript.methodId = methodId;
	
					System.out.println(condition);
					StartAnalysisScript.condition = condition.isEmpty()?"":condition.replaceAll(",", "");
					try {
						new StartAnalysisScript(className, domain, symbolic, condition, methodId);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	}
	
	//The class should have static fields for the files to write to
	//className_sY_domainName
	//where className is the name of class being analyzed (with all its methods)
	//domainName is the domain that the analysis uses
	//sY means using symbolic helper state and sN means not using symbolic helper state.
	
	
	public static FileWriter fileToWrite;
	private static String domainPath = "./ScratchData/domains/";
	//private static String conditionPath = "ScratchData/conditions/";
	private static String resultsPath = "./ScratchData/results/invariants/";
	public static FileWriter timeDataFile;
	public static String analysisType;
	public static boolean writeTime = true;
	
	//each instance should open/close that file
	
	public StartAnalysisScript(String className, String domainFile, String symbolicHelper, String condition, String methodId) throws IOException{
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
		String fileName = resultsPath+className+"_"+methodId +"_"+(condition.isEmpty()?"":condition.replaceAll(",", "")+"_")+domainFile;
		fileToWrite = new FileWriter(fileName);
		boolean symbolicOn = symbolicHelper.equals("sY");
		
		String[] sootArgs = {"-f", "n", className};
		PackManager.v().getPack("jtp").
			add(new Transform("jtp.disjoint", new PartialTransformer(domain, symbolicOn, condition, methodId)));
		//adding runtime to the path
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
				+ ":" + System.getProperty("sun.boot.class.path"));
		//run soot
		soot.Main.main(sootArgs);
		fileToWrite.close();
	}

}
