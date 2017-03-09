package processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Take two files and produce the 
 * disjunction of each formula
 * @author elenasherman
 *
 */
public class CombinePartial {
	
	public static void main(String [] strs) throws FileNotFoundException{
		String fileName1 = "./test.OneTcas_sN_20t29t.txt";
		String fileName2 = "./test.OneTcas_sN_20t29f.txt";
	
		//read a line from each file
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		if(file1.exists() && file2.exists()){
		Scanner scan1 = new Scanner(file1);
		Scanner scan2 = new Scanner(file2);
		String line1 = "";
		String line2 ="";
		Pattern pt = Pattern.compile("\\d+t");
		Pattern pf = Pattern.compile("\\d+f");
		while(scan1.hasNextLine() && scan2.hasNextLine()){
			//advance to the next line
			line1 = scan1.nextLine();
			line2 = scan2.nextLine();
			//check if they are the same
			if(line1.equals(line2)){
				//then we can compare their one
				//or two lines
				if(line1.contains(" if ")){
					//conditional statement
					//should have two outputs
				}
			} else if(line1.matches("\\d+t") || line1.matches("\\d+f")){
				//found 
			} else {
				//different lines
				//the decide which to advance
			}
			 
		}
		
		scan1.close();
		scan2.close();
		} else {
			System.out.println("Cannot find " + file1.exists() + " " + file2.exists());
			System.exit(2);
		}
		
	}

}
