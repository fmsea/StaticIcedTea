package conditional.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import conditional.analysis.PartialTransformer;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartAnalysis {
	//eas: make sure artifacts is a source folder.
	public static boolean print = true;
	public static boolean writeToFile = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String[] classNames = {"test.BallonFactory", "test.OneTcas", "test.Base64", "test.client", "test.GeoData", "test.GeoEngine", 
//				"test.InfBlocks", "test.InfCodes", "test.InfTree", "test.MapViewer", "test.QRCodeDataBlockReader", 
//				"test.StructurePanel", "test.TileRenderor", "test.WorldController", "test.Class11", "test.Class13",
//		};
		String[] classNames = {"test.BallonFactory"};
		String[] domainNames = {"dom9.txt"};
		//String[] domainNames = {"dom3.txt", "dom2.txt"};
		String[] symbolic = {"sN"};
//		String[] conditions = {
//				"1f,2f,3f",
//				"1f,2f,3t,4f,5f",
//				"1f,2f,3t,4f,5t",
//				"1f,2f,3t,4t",
//				"1f,2t,3f",
//				"1f,2t,3t,4f,5f",
//				"1f,2t,3t,4f,5t",
//				"1f,2t,3t,4t",
//				"1t,2f,3f",
//				"1t,2f,3t,4f,5f",
//				"1t,2f,3t,4f,5t",
//				"1t,2f,3t,4t",
//				"1t,2t,3f",
//				"1t,2t,3t,4f,5f",
//				"1t,2t,3t,4f,5t",
//				"1t,2t,3t,4t"};
		//String[] conditions = {"1f,2f,3t,4f,5f"};
		List<String> conditions = new ArrayList<String>();
		//populate the conditions
		//read the file:

		//String symbolicOn = "sY";
		String methodId = "1";
		File file = new File("./ScratchData/conditions/paths/"+classNames[0]+"_"+methodId+".txt");
		if(file.exists()){
			try {
				Scanner scan = new Scanner(file);
				while(scan.hasNextLine()){
					String ln = scan.nextLine();
					if(!ln.isEmpty()){
						conditions.add(ln);
					}
				}
				scan.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		
		try {
			timeDataFile = new FileWriter(resultsPath+"timeData",true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(String className : classNames){
			for(String symbolicOn : symbolic){
			for(String domainName : domainNames){
				for(String condition : conditions){
				try {
					analysisType = domainName.split("\\.")[0] + "_"+ symbolicOn;
					System.out.println(condition);
					new StartAnalysis(className, domainName, symbolicOn, condition, methodId);
					G.reset();
					System.gc();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
		}
		}
		try {
			timeDataFile.close();
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
	private static String domainPath = "ScratchData/domains/";
	//private static String conditionPath = "ScratchData/conditions/";
	private static String resultsPath = "ScratchData/results/";
	public static FileWriter timeDataFile;
	public static String analysisType;
	
	//each instance should open/close that file
	
	public StartAnalysis(String className, String domainFile, String symbolicHelper, String condition, String methodId) throws IOException{
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
		String fileName = resultsPath+className+"_"+methodId +"_"+symbolicHelper+"_"+(condition.isEmpty()?"":condition.replaceAll(",", "")+"_")+domainFile;
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
