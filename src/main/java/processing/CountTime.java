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
		
		String more = "8";
		String less = "9";

		Map<String, List<Integer>> methodToData = new HashMap<String,List<Integer>>();
				String path = "ExperimentData/results/";
				String fileName = "timeData1";
				//System.out.println("fileName " + fileName);
				File file = new File(path+fileName);
				Scanner scanner = new Scanner(new FileReader(file));
				List<Integer> count = null;
				while(scanner.hasNext()){
					String line = scanner.nextLine();
					String[] tabs = line.split("\\t");
					//String key = tabs[0]+"\t"+tabs[1];
					String key = tabs[1];
					if(!methodToData.containsKey(key)){
						//create a list of size 4 for it
						//index 0 -> dom5sN
						//index 1 -> dom5sY
						//index 2 -> dom4sN
						//index 3 -> dom4Sy
						//index 4 -> dom54sN
						//index 5 -> dom54sY
						count = new ArrayList<Integer>();
						for(int i=0; i< 6; i++){//6 elements
							count.add(0);
						}
						methodToData.put(key, count);
					} else {
						count = methodToData.get(key);
					}
					int index = -1;
					//System.out.println("tab2 " + tabs[2]);
					if(tabs[2].equals("dom"+less+"_sN")){
						index = 0;
					} else if (tabs[2].equals("dom" + less+"_sY")){
						index = 1;
					} else if(tabs[2].equals("dom"+more+"_sN")){
						index = 2;
					} else if(tabs[2].equals("dom"+more+"_sY")){
						index = 3;
					}else if(tabs[2].equals("dom"+less+"_"+more+"_sN")){
						index = 4;
					}else if(tabs[2].equals("dom"+less+"_"+more+"_sY")){
						index = 5;
					} else {
						System.out.println("Something wrong1");
						//System.exit(2);
						continue;
					}
					int val = Integer.valueOf(tabs[3]);
					incrementAt(index, val, count);
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
