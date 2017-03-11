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
		String fileName1 = "./ScratchData/results/test.OneTcas_sN_20t29t_dom2.txt";
		String fileName2 = "./ScratchData/results/test.OneTcas_sN_20t29f_dom2.txt";
	
		//read a line from each file
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		if(file1.exists() && file2.exists()){
		Scanner scan1 = new Scanner(file1);
		Scanner scan2 = new Scanner(file2);
		String line1 = "";
		String line2 ="";
		int num1 = 0;
		int num2 = 0;
		while(scan1.hasNextLine() && scan2.hasNextLine()){
			if(num1 == num2){
				//if previous are the same then scan together.
				line1 = scan1.nextLine();
				line2 = scan2.nextLine();
				num1 = Integer.parseInt(line1.split(" ")[0]);
				num2 = Integer.parseInt(line2.split(" ")[0]);
			} else if (num1 > num2){
				//scan only line 2
				line2 = scan2.nextLine();
				num2 = Integer.parseInt(line2.split(" ")[0]);
			} else {
				//scan only line 1
				line1 = scan1.nextLine();
				num1 = Integer.parseInt(line1.split(" ")[0]);
			}
			//advance to the next line
			
			System.out.println("l1 " + line1);
			System.out.println("l2 " + line2);
			//get the numbers
			
			//check if the next number are the same
			if(num1 == num2){
				//then we can compare their one
				//or two lines
				if(line1.contains(" if ")){
					String s1t = "";
					String s1f = "";
					String s2t = "";
					String s2f = "";
					//conditional statement
					//should have two outputs
					//scan the first line and
					String s11 = scan1.nextLine();
					String s12 = scan1.nextLine();
					String s21 = scan2.nextLine();
					String s22 = scan2.nextLine();
					if(s11.startsWith("*")){
						if(s11.endsWith("f")){
							s1f = s12;
						} else {
							s1t = s12;
						}
					} else {
						s1t = s11;
						s1f = s12;
					}
					if(s21.startsWith("*")){
						if(s21.endsWith("f")){
							s2f = s22;
						} else 
							s2t = s22;
					} else {
						s2t = s21;
						s2f = s22;
					}
					//now the branches are defined
					System.out.println("t " + s1t + " or " + s2t);
					System.out.println("f " + s1f + " or " + s2f);
				} else {
					//regular assignment statement
					String s1 = scan1.nextLine();
					String s2 = scan2.nextLine();
					if(s1.equals(s2)){
						System.out.println(s1);
					} else {
						//parse and use the solver to compare them.
						//do disjunction
						System.out.println(s1 + " or " + s2);
					}
				}
			} else if(num1 > num2){
				//put values of num2 first
				System.out.println(line2);
				if(line2.contains(" if ")){
					//do two scans 
					String t = scan2.nextLine();
					String f = scan2.nextLine();
					String sf ="";
					String st ="";
					if(t.startsWith("*")){
						if(t.endsWith("f")){
							sf = f;
						} else {
							st = f;
						}
					} else {
						st = t;
						sf = f;
					}
					System.out.println(st);
					System.out.println(sf);
				} else {
					//do only one scan
					String s = scan2.nextLine();
					System.out.println(s);
				}
			} else {
				System.out.println(line1);
				if(line1.contains(" if ")){
					//do two scans 
					String t = scan1.nextLine();
					String f = scan1.nextLine();
					String sf ="";
					String st ="";
					if(t.startsWith("*")){
						if(t.endsWith("f")){
							sf = f;
						} else {
							st = f;
						}
					} else {
						st = t;
						sf = f;
					}
					System.out.println(st);
					System.out.println(sf);
				} else {
					//do only one scan
					String s = scan1.nextLine();
					System.out.println(s);
				}
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
/*
else if(line1.startsWith("*") || line2.startsWith("*")){
//found where they are taking different branches
System.out.println("conditional " + line1 + " " + line2);
*/