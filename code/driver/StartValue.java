package driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import conditional.analysis.ConditionalValue;
import disjoint.analysis.ValueTransfomer;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartValue {
	//eas: make sure artifacts is a source folder.
	public static boolean print = true;
	public static boolean writeToFile = true;
	public static boolean writeTime = true;
	public static String className;
	public static String methodId;
	public static String domain;
	//public static String condition;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String[] classNames = {"test.BallonFactory", "test.OneTcas", "test.Base64", "test.client", "test.GeoData", "test.GeoEngine", 
//				"test.InfBlocks", "test.InfCodes", "test.InfTree", "test.MapViewer", "test.QRCodeDataBlockReader", 
//				"test.StructurePanel", "test.TileRenderor", "test.WorldController", "test.Class11", "test.Class13",
//		};
		
		String domain = "dom5.txt";
		String symbolic = "sN";
		className = "test.Example1m";
		methodId = "3";
		if(args.length > 0){
			 className = args[0];
			 methodId = args[1];
			domain = args[2]+".txt";
			writeToFile = args[3].equals("y");
		}
		

				StartValue.domain = domain.split("\\.")[0];
				analysisType = StartValue.domain + "_" + symbolic;
				//for(String condition : conditions){
				try {	
					//System.out.println(condition);
					//StartValue.condition = condition.isEmpty()?"":condition.replaceAll(",", "");
					new StartValue(className, domain, symbolic,  methodId);
				} catch (Exception e) {
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
	private static String domainPath = "ExperimentDataConditional/domains/";
	private static String resultsPath = "ScratchData/resultsVA/invariants/";
	public static String analysisType;
	
	//each instance should open/close that file
	
	public StartValue(String className, String domainFile, String symbolicHelper, String methodId) throws IOException{
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
		String fileName = resultsPath+className+"_"+methodId +"_"+domainFile;
		fileToWrite = new FileWriter(fileName);
		boolean symbolicOn = symbolicHelper.equals("sY");
		
		String[] sootArgs = {"-f", "n", className};
		PackManager.v().getPack("jtp").
			add(new Transform("jtp.disjoint", new ValueTransfomer(domain, Integer.parseInt(methodId),symbolicOn)));
		//adding runtime to the path
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
				+ ":" + System.getProperty("sun.boot.class.path"));
		//run soot
		soot.Main.main(sootArgs);
		fileToWrite.close();
	}

}
