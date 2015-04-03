package disjoint.driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import disjoint.analysis.ValueTransfomer;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartAnalysisKestrel {
	
	public static boolean print = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String[] classNames = {"test.BallonFactory", "test.OneTcas", "test.Base64", "test.client", "test.GeoData", "test.GeoEngine", 
//				"test.InfBlocks", "test.InfCodes", "test.InfTree", "test.MapViewer", "test.QRCodeDataBlockReader", 
//				"test.StructurePanel", "test.TileRenderor", "test.WorldController", "test.Class11", "test.Class13",
//		};
		String className = args[0];
		String domainName = args[1];
		String symbolicOn = args[3];
		
		try {
			timeDataFile = new FileWriter(resultsPath+"timeData",true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
				try {
					analysisType = domainName.split("\\.")[0] + "_"+ symbolicOn;
					new StartAnalysisKestrel(className, domainName, symbolicOn);
					G.reset();
					System.gc();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	private static String domainPath = "ExperimentData/domains/";
	private static String resultsPath = "ExperimentData/results/";
	public static FileWriter timeDataFile;
	public static String analysisType;
	
	//each instance should open/close that file
	
	public StartAnalysisKestrel(String className, String domainFile, String symbolicHelper) throws IOException{
		//instantiate the list of domains from a file
		String domainDescription = domainPath+domainFile;
		DomainReader dr = new DomainReader(domainDescription);
		List<Domain> domain = dr.getReadDomains();
		System.out.println(domain);
	
		//create the file to write to
		fileToWrite = new FileWriter(resultsPath+className+"_"+symbolicHelper+"_"+domainFile);
		boolean symbolicOn = symbolicHelper.equals("sY");
		
		String[] sootArgs = {"-f", "n", className};
		PackManager.v().getPack("jtp").
			add(new Transform("jtp.disjoint", new ValueTransfomer(domain, symbolicOn)));
		//adding runtime to the path
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
				+ ":" + System.getProperty("sun.boot.class.path"));
		//run soot
		soot.Main.main(sootArgs);
		fileToWrite.close();
	}

}
