package disjoint.driver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.microsoft.z3.Z3Exception;

import disjoint.analysis.ValueTransformer;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import solver.SolverWrapper;
import solver.SolverWrapperZ3;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The driver for the analysis
 * you're welcome to come up with your own.
 * @author elenasherman
 *
 */
public class StartAnalysis {

    private static Logger LOGGER = LoggerFactory.getLogger(StartAnalysis.class);
	/**
	 *  The class should have static fields for the files to write to
	 *  className_sY_domainName
	 *  where  (with all its methods)
	 *  domainName is the domain that the analysis uses
	 *  sY means using symbolic helper state and sN means not using symbolic helper state.
	 * @param args, where 
	 * args[0] - the name of class being analyzed, make sure its location in your classpath
	 * args[1] - the integer representing the order in which the method
	 *           occurs in the class file, i.e., first method, second method and so on
	 * args[2] - the path to the domain file
	 * args[3] - "sY" for adding symbolic analysis
	 */
	public static void main(String[] args) {

        LOGGER.debug("arguments: {}", args.length);
        for (int i = 0; i < args.length; i++) {
            LOGGER.debug("arguments[{}]: {}", i, args[i]);
        }
		//Parse arguments
        String className = "test.Example1M";
        Integer methodId = 6;
        String domainName = Paths.get("ExperimentData/domains/dom3.txt").toAbsolutePath().toString();
        String symbolicOn = "sY";
        if (args.length >= 4) {
            className = args[0];
            methodId = Integer.parseInt(args[1]);
            domainName = Paths.get(args[2]).toAbsolutePath().toString();
            symbolicOn = args[3];
        }

		//Print info based on the arguments
		LOGGER.info("Running analysis for {} _ {}", domainName, symbolicOn);
		try {
			//instantiate domain and analysis
			new StartAnalysis(className, methodId, domainName, symbolicOn.equals("sY"));
		} catch (IOException e) {
            LOGGER.error("unable to complete analysis", e);
		}
	}

	public StartAnalysis(String className, int methodId, String domainFile, boolean symbolicOn) throws IOException{
		//instantiate the list of domains from a file
		DomainReader dr = new DomainReader(domainFile);
		List<Domain> domain = dr.getReadDomains();
		//show the domain encoding used in the analysis
		LOGGER.info("Domain provided: \n{}", domain);
		//"-f" "n" means for soot not to output the compiled files 
		String[] sootArgs = {"-f", "n", className};
		//add the analysis into the compiler
		PackManager.v().getPack("jtp").
		add(new Transform("jtp.disjoint", new ValueTransformer(domain, methodId, symbolicOn)));
		//system separator
		String pathSeparator = System.getProperty("path.separator");
		//adding runtime to the path
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+pathSeparator+System.getProperty("java.class.path") 
				+ pathSeparator + System.getProperty("sun.boot.class.path"));
		//run soot with the added analysis
		soot.Main.main(sootArgs);
	}

	/*
	 * Instantiates solver based on the choice
	 * of the solver 
	 * For now just a single one that
	 * we have based on Z3
	 */
	public static SolverWrapper getSolver(){
		SolverWrapper s = null;
		try {
			s = new SolverWrapperZ3();

		} catch (Z3Exception e) {
            LOGGER.error("Cannot instantiate the solver", e);
			System.exit(2);
		}
		//set the timeout if applicable
		//in milliseconds
		s.setTimeOut(10000000);
		return s;
	}

}
