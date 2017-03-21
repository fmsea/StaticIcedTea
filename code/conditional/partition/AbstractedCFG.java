package conditional.partition;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains only relevant
 * part of cfg that we can use
 * to generate paths
 * @author elenasherman
 *
 */
public class AbstractedCFG {
	Node start;
	Set<Node> endNodes;
	Set<Node> allNodes;
	
	public AbstractedCFG(){
		allNodes = new HashSet<Node>();
		endNodes = new HashSet<Node>();
	}
	
	public Node addStart(String name){
		start = new Node(name);
		allNodes.add(start);
		return start;
	}
	
	public Node addEnd(String name){
		Node end = new Node(name);
		allNodes.add(end);
		endNodes.add(end);
		return end;
	}
	
	public Node addNode(String name){
		Node n = new Node(name);
		allNodes.add(n);
		return n;
	}
	
	public void addFalse(String from, String to){
		
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
			if(!endNodes.contains(n)){
				ret +=n.getName()+"\t"+n.getTrue().getName()+"\t"+n.getFalse().getName()+"\n";
			}
		}
		return ret;
	}
}
