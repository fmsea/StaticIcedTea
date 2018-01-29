package conditional.partition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class that contains only relevant
 * part of cfg that we can use
 * to generate paths
 * @author elenasherman
 *
 */
public class AbstractedCFG {
	String className;
	String methodID;
	Node start;
	Node end;
	Set<Node> allNodes;
	
	public AbstractedCFG(){
		allNodes = new LinkedHashSet<Node>();
	}
	
	/**
	 * Instantiate ACFG from a file
	 * @param fileName
	 */
	public AbstractedCFG(String fileName){
		allNodes = new LinkedHashSet<Node>();
		File file = new File(fileName);
		if(file.exists()){
			try {
				Scanner scan = new Scanner(file);
				//add end and start
				//addStart("1");
				//addEnd("end");
				boolean first = true;
				while(scan.hasNextLine()){
					String ln = scan.nextLine();

						StringTokenizer tk = new StringTokenizer(ln);
						//get three tokens
						String from = tk.nextToken();
						if(!from.equals("t")){
						String onT = tk.nextToken();
						String onF = tk.nextToken();
						Node fromN = addNode(from);
						if(first){
							//make as the start state
							fromN = addStart(from);
							first = false;
						} else {
							fromN = addNode(from);
						}
						Node fromTN = addNode(onT);
						Node fromFN = addNode(onF);
						add(fromN, fromTN, true);
						add(fromN, fromFN, false);
					}
				}
				scan.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("cannot find the file " + fileName);
		}
	}
	
	/**
	 * Write ACFG encoding to a file
	 * @param fileName
	 */
	public void writeToFile(String fileName){
		try {
			Writer fileOut = new FileWriter(fileName);
			fileOut.write(toString());
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Write ACFG paths to a file
	 * @param fileName
	 */
	public void writePaths(String fileName){
		try {
			Writer fileOut = new FileWriter(fileName);
			for(String path : getPaths()){
				fileOut.write(path+"\n");
			}
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Node addStart(String name){
		start = new Node(name);
		allNodes.add(start);
		return start;
	}
	
	public Node getStartNode(){
		return start;
	}
	
	public Node addEnd(String name){
		 end = new Node(name);
		 allNodes.add(end);
		return end;
	}
	
	public Node addNode(String name){
		Node n = findNode(name);
		if(n==null){
			n = new Node(name);
			allNodes.add(n);
		}
		return n;
	}
	
	public void add(Node from, Node to, boolean on){
		if(on){
		from.addTrue(to);
		} else {
			from.addFalse(to);
		}
	}
	public boolean contains(String name){
		return !(findNode(name) == null);
	}
	//find node or create one
	public Node findNode(String name){
		Node ret = null;
		for(Node n : allNodes){
			if(n.getName().equals(name)){
				ret = n;
				break;
			}
		}
		//should never be null
		//if(ret == null) System.out.println("oops cannot be null");
		return ret;
	}
	
	public String toString(){
		String ret = "\tt\tf\n";
		for(Node n : allNodes){
			if(!end.equals(n)){
				//System.out.println(n.getName() + " " + n.getTrue() + " " + n.getFalse());
				//System.out.println("n " + n.getName() + " " + n.getTrue().getName() + " " + n.getFalse().getName());
				ret +=n.getName()+"\t"+n.getTrue().getName()+"\t"+n.getFalse().getName()+"\n";
			}
		}
		return ret;
	}
	
	public Set<String> getPaths(){
		Set<String> ret = buildPath(start, "");
		//start from the start state
		
		return ret;
	}
	
	private Set<String> buildPath(Node n, String prefix){
		Set<String> ret = new HashSet<String>();
		if(end.equals(n)){
			ret.add(prefix.substring(0, prefix.length()-1));
		} else {
			//regular node
			String truePrefix = prefix+n.getName()+"t,";
			ret.addAll(buildPath(n.getTrue(), truePrefix));
			String falsePrefix = prefix+n.getName()+"f,";
			ret.addAll(buildPath(n.getFalse(), falsePrefix));
		}
		
		return ret;
	}
}
