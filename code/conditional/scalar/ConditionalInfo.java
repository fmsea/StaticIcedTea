package conditional.scalar;

import java.util.ArrayList;
import java.util.List;

public class ConditionalInfo {
	
	int line = 0;
	boolean branch = true;
	
	public ConditionalInfo(int line, boolean branch){
		this.line = line;
		this.branch = branch;
	}
	
	public boolean getBranch(){
		return this.branch;
	}
	
	public int getLine(){
		return this.line;
	}
	
	@Override
	public String toString(){
		return Integer.toString(line) + " " + branch;
	}
	
	public static List<ConditionalInfo> createFlowInfoList(String flowList){
		//System.out.println("I'm here " + flowList);
		List<ConditionalInfo> myList = new ArrayList<ConditionalInfo>();
		int lineNumber = 1;
		int last = 0;
		for (int i = 0; i < flowList.length(); i++){
			//System.out.println(i);
			//System.out.println(flowList.charAt(i));
			
			
			Character current = flowList.charAt(i);
			if (current == 'f' || current == 't'){
				lineNumber = Integer.parseInt(flowList.substring(last, i));
				//System.out.println(lineNumber);
				last = i+ 1;
				boolean branch = true;
				if (current == 'f'){
					branch = false;
				}
				myList.add(new ConditionalInfo(lineNumber, branch));
			}
			
//			if (i%2 == 0){
//				lineNumber = Character.getNumericValue(flowList.charAt(i));
//				//int linenumber
//			}else{
//				boolean branch = true;
//				if (flowList.charAt(i) == 'f'){
//					branch = false;
//				}else{
//					branch = true;
//				}
//				branchInfo.add(new MyFlowInfo(lineNumber, branch));
//				//boolean branch
//				//add linenumber and branch 
//			}
		}
//		System.out.println("printing out the branch Infos");
//		for (ConditionalInfo f: myList){
//			System.out.println(f);
//		}
//System.out.println("ML " + myList);
		return myList;
	}
}
