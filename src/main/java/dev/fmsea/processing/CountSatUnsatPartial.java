package dev.fmsea.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public class CountSatUnsatPartial {
    public static void main(String[] args) throws IOException {
        // String[] classes = {"BallonFactory", "Base64", "client",
        // "GeoData", "GeoEngine", "InfBlocks", "InfCode",
        // "InfTree", "MapViewer", "QRCodeDataBlockReader",
        // "StructurePanel", "TileRenderor", "WorldController",
        // "Class11", "Class13", "OneTcas"
        // };

        // String[] classes = {"Base64"};
        String className = "test.BallonFactory";
        String methodId = "1";
        String domain = "dom9";
        String dataPath = "./ConditionalTACAS/";
        String type = "c1";
        if (args.length > 0) {
            dataPath = args[0];
            className = args[1];
            methodId = args[2];
            domain = args[3];
            type = args[4];
        }

        // read the path file and get all the number of path from it
        String pathFileName = dataPath + "/conditions/paths/" + className + "_" + methodId + ".txt";
        File pathFile = new File(pathFileName);
        String satunsatOutput = "";
        if (pathFile.exists()) {
            // get the scanner
            Scanner pathScan = new Scanner(pathFile);
            int pathId = 1;
            while (pathScan.hasNextLine()) {
                String l = pathScan.nextLine();
                if (!l.isEmpty()) {
                    List<Integer> pathData = new ArrayList<Integer>();
                    // create a list of size 4 for it
                    // index 0 -> sat/sat
                    // index 1 -> unsat/sat
                    // index 2 -> sat/unsat
                    // index 3 -> unsat/unsat
                    for (int i = 0; i < 4; i++) {// each with 4 counts
                        pathData.add(0);
                    }
                    // smt2 file to count sat/unsat for
                    String smt2File = dataPath + "/resultsVA/satunsat/" + type + "/" +
                            className + "_" + methodId + "_" +
                            String.valueOf(pathId) + "_" + domain + "_satunsat.txt";

                    // String[] fileNames = {"res1", "res2", "res3", "res4", "res5"};
                    // //for(String className : classes){
                    // for(int dataFile =0 ; dataFile < fileNames.length; dataFile ++) {
                    // int offset = dataFile *4;
                    // String path = "ExperimentData/z3Results/"+domless + "_" + dommore +"/";
                    // String fileName = fileNames[dataFile];
                    // System.out.println("fileName " + fileName);
                    File file = new File(smt2File);
                    Scanner scanner = new Scanner(new FileReader(file));
                    // List<Integer> count = null;
                    while (scanner.hasNext()) {
                        String line = scanner.nextLine();

                        // if line start with a number
                        // if(line.matches("^[0-9].*")){
                        // //System.out.println(fileName + "\t " + line);
                        // //then get the method signature
                        // String methodSig = "<"+line.split(":<")[1];
                        // //System.out.println(methodSig);
                        // if(!pathData.containsKey(methodSig)){
                        //
                        // pathData.put(methodSig, count);
                        // } else {
                        // count = pathData.get(methodSig);
                        // }
                        // } else {
                        if (line.equals("sat")) {
                            // read the next line
                            String line2 = scanner.nextLine();
                            if (line2.equals("sat")) {
                                // add to index 0
                                incrementAt(0, pathData);
                            } else if (line2.equals("unsat")) {
                                // add to index 2
                                incrementAt(2, pathData);
                            } else {
                                System.out.println("Something wrong1");
                                System.exit(2);
                            }
                        } else if (line.equals("unsat")) {
                            // read the next line
                            String line2 = scanner.nextLine();
                            // System.out.println(line + " " + line2);
                            if (line2.equals("sat")) {
                                // add to index 1
                                incrementAt(1, pathData);
                            } else if (line2.equals("unsat")) {
                                // add to index 3
                                incrementAt(3, pathData);
                            } else {
                                System.out.println("Something wrong2");
                                System.exit(2);
                            }
                        }
                    }
                    // }
                    scanner.close();
                    // System.out.println("methods " + methodToData.keySet().size());
                    // print the map
                    // write timeOutput to a file

                    satunsatOutput += className + "\t" + methodId + "\t" + pathId;
                    for (Integer val : pathData) {
                        satunsatOutput += "\t" + val;
                    }
                    satunsatOutput += "\n";

                    pathId++;
                } // end reading datafile
            }
            String timeOutFileName = dataPath + "/resultsVA/satunsat/" + type + "/satunsat_" + domain + ".txt";
            File timeOutFile = new File(timeOutFileName);
            if (!timeOutFile.exists()) {
                timeOutFile.createNewFile();
            }
            FileWriter timeOutWrite = new FileWriter(timeOutFile, true);
            timeOutWrite.write(satunsatOutput);
            timeOutWrite.close();
            pathScan.close();
        } else {
            System.out.println("Cannot find file " + pathFileName);
        }
        // }//end of class
        // Total methods
    }

    private static void incrementAt(int index, List<Integer> list) {
        int temp = list.get(index);
        temp++;
        list.set(index, temp);
    }

}
