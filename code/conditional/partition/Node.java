package conditional.partition;

public class Node {
	String name;
	Node trueN;
	Node falseN;
	
	public Node(String n){
		name = n;
	}
	
	public void addTrue(Node n){
		trueN = n;
	}
	
	public void addFalse(Node n){
		falseN = n;
	}
	
	public Node getTrue(){
		return trueN;
	}
	
	public Node getFalse(){
		return falseN;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
