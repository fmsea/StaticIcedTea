package processing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.List;


public class CountTime {

	public static void main(String[] args) throws FileNotFoundException {

		Map<String, List<Integer>> methodToData = new HashMap<String,List<Integer>>();
				String path = "FSEData/";
				String fileName = "timeDatadom9dom8";
				//System.out.println("fileName " + fileName);
				File file = new File(path+fileName);
				Scanner scanner = new Scanner(new FileReader(file));
				List<Integer> count = null;
				while(scanner.hasNext()){
					String line = scanner.nextLine();
					String[] tabs = line.split("\\t");
					String key = tabs[0]+"\t"+tabs[1];
					if(!methodToData.containsKey(key)){
						//create a list of size 4 for it
						//index 0 -> dom5sN
						//index 1 -> dom5sY
						//index 2 -> dom4sN
						//index 3 -> dom4Sy
						count = new ArrayList<Integer>();
						for(int i=0; i< 4; i++){// 5 files, each with 4 counts
							count.add(0);
						}
						methodToData.put(key, count);
					} else {
						count = methodToData.get(key);
					}
					int index = -1;
					if(tabs[2].equals("dom9_sN")){
						index = 0;
					} else if (tabs[2].equals("dom9_sY")){
						index = 1;
					} else if(tabs[2].equals("dom8_sN")){
						index = 2;
					} else if(tabs[2].equals("dom8_sY")){
						index = 3;
					}else {
						System.out.println("Something wrong1");
						System.exit(2);
					}
					int val = Integer.valueOf(tabs[3]);
					incrementAt(index, val, count);
					//System.out.println(tabs[0] + " " + tabs[2]);
//					//if line start with a number
//					if(line.matches("^[0-9].*")){
//						//System.out.println(line);
//						//then get the method signature
//						String methodSig = "<"+line.split(":<")[1];
//						//System.out.println(methodSig);
//						if(!methodToData.containsKey(methodSig)){
//							//create a list of size 4 for it
//							//index 0 -> sat/sat
//							//index 1 -> unsat/sat
//							//index 2 -> sat/unsat
//							//index 3 -> unsat/unsat
//							count = new ArrayList<Integer>();
//							for(int i=0; i< 20; i++){// 5 files, each with 4 counts
//								count.add(0);
//							}
//							methodToData.put(methodSig, count);
//						} else {
//							count = methodToData.get(methodSig);
//						}
//					} else {
//						if(line.equals("sat")){
//							//read the next line
//							String line2 = scanner.nextLine();
//							if(line2.equals("sat")){
//								//add to index 0
//								incrementAt(0+offset, count);
//							} else if (line2.equals("unsat")){
//								//add to index 2
//								incrementAt(2+offset,count);
//							} else {
//								System.out.println("Something wrong1");
//								System.exit(2);
//							}
//						} else if (line.equals("unsat")){
//							//read the next line
//							String line2 = scanner.nextLine();
//							//System.out.println(line + " " + line2);
//							if(line2.equals("sat")){
//								//add to index 1
//								incrementAt(1+offset, count);
//							} else if (line2.equals("unsat")){
//								//add to index 3
//								incrementAt(3+offset,count);
//							} else {
//								System.out.println("Something wrong2");
//								System.exit(2);
//							}
//						}
//					}
				}
				scanner.close();
		//Total methods
		System.out.println("methods " + methodToData.keySet().size());
		//print the map
		for(Entry<String, List<Integer>> es : methodToData.entrySet()){
			System.out.print(es.getKey());
			for(Integer val : es.getValue()){
				System.out.print("\t" + Math.round((double)val/3));
			}
			System.out.println();
		}
		System.out.flush();
	}

	private static void incrementAt(int index, int value, List<Integer> list){
		int temp = list.get(index);
		temp+=value;
		list.set(index, temp);
	}

}
