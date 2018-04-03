import java.util.HashMap;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class CPSC331_Ass4{

	/*
	 * ArrayList that keeps all the edges
	 * HashMap that keeps the neighbors of the vertices
	 * 2D Array that keeps the adjacency matrix table
	 */
	public static ArrayList<String> verticeList = new ArrayList<String>();
	public static HashMap<String, String> adjList = new HashMap<String, String>();
	public static String twoDimArray [][];
	
	// Number of vertices, PrintWriters and StrBuilders
	// (2n and 3rd are to be used for writing)
	public static int numOfVer;
	public static PrintWriter pw, pw2;
	public static StringBuilder sb, sb2;
	
	public static void main(String []args) throws InterruptedException, IOException{
	
		Scanner inp = new Scanner(System.in);
		
		// Clear the screen
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		
		
		//////////////////////////////////////////////////////////////////////
		//////////////// 	S1 - Reading from the file and    ////////////////
		//////////////// 	initialization and calculations   ////////////////
		//////////////////////////////////////////////////////////////////////
			
		/*
		===== Precondition: =====
		* Strings that contain
			* for 1) At least the value of 0 up to 32768 - inclusive
				* Restricted to numbers only within the above limit	
				* (In my algorithm, there is restriction on the above for specific CSV
				* file since my algorithm automatically creates required vertices)
			* for 2) String in the form:
				* "path/filename.csv" or filename.csv if in the same path as the java file
			* for 3) String in the form:
				* "path/" or "" - same idea
		* Buffered reader and a 2D array to read and the keep the results in
		===== Postcondition: =====
		* Print/+Ask for the strings and get input from the user
		* Keep the user inputs in strings that are to be used later
		* Read from the user input path/file and make a 2d array
		*/
		System.out.println("Enter number of vertices");
		String inpVer = inp.nextLine();
		numOfVer = Integer.parseInt(inpVer);
		
		System.out.println("Enter the path for the CSV file - \"path/filename.csv\"\n"
						+  "(If in the same folder, type \"filename.csv\")");
		String inpCSV = inp.nextLine();
		
		System.out.println("Enter the path for the output files - \"path/\"\n"
				+  "(If you want the same folder press enter");
		String outCSV = inp.nextLine();

		// temporary strings to read on and to split from "'"
		String line = "";
		String cvsSplitBy = ",";
		
		// Bufffer reader that reads the CSV file and splits the vertices and
		// adds them to the verticeList with the form " a,b " -a and b are numbers
		try (BufferedReader br = new BufferedReader(new FileReader(inpCSV))) {
		    while ((line = br.readLine()) != null) {
		        String[] edge = line.split(cvsSplitBy);
		        verticeList.add(edge[0] + "," + edge[1]);
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}

		
		// Initialize the 2D array:
		twoDimArray = new String [numOfVer+1][numOfVer+1];
		//--------------------------------------------------
		// Set [0][0] to be the X
		twoDimArray[0][0] = "X";
		// Set the first row vertex numbers
		for (int i = 0; i < twoDimArray.length-1; i++){
			twoDimArray[0][i+1] = Integer.toString(i);			
		}
		// Set the first column vertex numbers
		for (int i = 0; i < twoDimArray.length-1; i++){
			twoDimArray[i+1][0] = Integer.toString(i);			
		}		
		// Fill the matrix with 0's (excluding the first row and column)
		for (int i = 1; i < twoDimArray.length; i++){
			for (int j = 1; j < twoDimArray.length; j++){
				twoDimArray[i][j] = "0";
			}
		}
		//--------------------------------------------------
		
		//////////////////////////////////////////////////////////////////////
		//////////////// 	S2 - Algorithms, calculations 	  ////////////////
		//////////////// 	and output on the screen   	      ////////////////
		//////////////////////////////////////////////////////////////////////
		
		/*
		===== Precondition: =====
		* 	Global variables and the inputs from S1
		* 	Results from S1
		* 	Temporary counters(ints) and strings for calculations 
		* 	MPV and LPV initialization
		===== Postcondition: =====
		* 	Do the calculations and set up the following:
			*	adjList, twoDimArray and MPV & LPV
		*/

		
		// temporary string to keep the neighbors 
		String temp = "";
		
		// SET UP THE adjList
		// for(all the vertices up to the given input)
			// for(check all the elements in the verticeList)
				// The following four lines might be inefficient but the reason I used them is
				// for easier readability of the code
		for (int i = 0; i < numOfVer; i++){			
			for(int j = 0; j < verticeList.size(); j++){
				String getCurrent = verticeList.get(j);		// current element being checked
				int commaIndex = getCurrent.indexOf(',');	// index of the comma	- can be omitted using .split()
				int beforeComma = Integer.parseInt(getCurrent.substring(0, commaIndex));	// vertex on the left
				int afterComma = Integer.parseInt(getCurrent.substring(commaIndex+1));		// vertex on the right
				
				// if the given number is in either side and the other side contains a vertex
				// that is less than the user input number, add it to a temporary string with "," 
				// (or the other way around)
				if(beforeComma == i && afterComma < numOfVer){	
					temp += "," + Integer.toString(afterComma);
				}else if (beforeComma < numOfVer && afterComma == i){
					temp += "," + Integer.toString(beforeComma);
				}
			}
			// add a "," in the end so that it's easier for the future algorithms
			// also if the length is 0 or greater, this will be useful to calculate LPV
			// (since you cannot substring from -1 if the input = 0)
			adjList.put(Integer.toString(i), (temp + ","));
			temp = "";
		}
		
		// FILL UP THE twoDimArray MATRIX
		// Compare the vertices using the adjList
		// for (row many times -except first)
			// for (column many times -except first)
				// check if the dictionary has that neighbor (Y ? put 1 : 0)
		for (int i = 1; i < twoDimArray.length; i++){
			for (int j = 1; j < twoDimArray.length; j++){
				if(adjList.get(Integer.toString(i-1)).contains("," + Integer.toString(j-1) + ",")){
					twoDimArray[i][j] = "1";
				}
			}
		}
		
		// Set these in this way so that we can find the least and the most
		int MPV = 0;
		int LPV = numOfVer;
		
		// SET UP THE VALUE OF MPV AND LPV
		// for (adjList many times)
			// check the number of the neighbors of the current vertex
			// numOfCommas - 1 = # of neighbors since the numbers are set in the form of:
			// 0 neighbors? adjList(i) = ,		thus -> commas - 1 = 0 neighbors
			// 1 neighbor?  adjList(i) = ,#,	thus -> commas - 1 = 1 neighbor
		for (int i = 0; i < adjList.size(); i++){
			String adjListVal = adjList.get(Integer.toString(i));
			int numOfNeighbors = adjListVal.length() - adjListVal.replaceAll(",","").length() - 1;
			// current vertex has more neighbors ? replace MPV by that number : do nothing
			if(numOfNeighbors > MPV){
				MPV = numOfNeighbors;
			}
			// same idea above with the other way around but also
			// 
			if(numOfNeighbors < LPV && numOfNeighbors >= 0){
				LPV = numOfNeighbors;
			}else if (numOfNeighbors < 0) {	// if negative (meaning no vertex, set it to 0)
				LPV = 0;
			}
		}

		
		//////////////////////////////////////////////////////////////////////
		//////////////// 	S3 - MPV & LPV calculations  	  ////////////////
		//////////////// 	and writing output on 2 files     ////////////////
		//////////////////////////////////////////////////////////////////////
		
		/*
		===== Precondition: =====
		* 	Global variables and the inputs from S1
		* 	Results from S1 and S2
		* 	StringBuilders and PrintWriters
		* 	Temporary counters(ints) and strings for calculations 
		===== Postcondition: =====
		* 	Do the calculations and write to two separate files:
			* 	adjList and adjMatrix
		*/
		
		
		
		// clear screen
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		
		// Print title
		System.out.printf("Number of vertices entered: %s \n", numOfVer);
				
		System.out.printf("\nNumber of neighbors for MPV: %s \n"
						+ "--------------------------------\n"
				        + "MPV \t Neighbours\n", MPV);
		
		// for (number of vertices)
			// if the vertex's neighbor number is equal to MPV then print it with its neighbors
		for (int p = 0; p < adjList.size(); p++){
			String adjListVal = adjList.get(Integer.toString(p));
			int numOfNeighbors = adjListVal.length() - adjListVal.replaceAll(",","").length() - 1;
			int lastComma = (adjListVal.length() > 1 ? (adjListVal.lastIndexOf(',')) : 1);
													// to make sure we don't get a negative value
			if(numOfNeighbors == MPV && MPV != 0){	// if 0 then don't print anything - trivial
				System.out.println(p + " \t " + adjListVal.substring(1, lastComma));
			}
		}

		
		System.out.printf("\nNumber of neighbors for LPV: %s \n"
						+ "--------------------------------\n"
						+ "LPV \t Neighbours\n", LPV);
		
		// same logic as above
		for (int p = 0; p < adjList.size(); p++){
			String adjListVal = adjList.get(Integer.toString(p));
			int numOfNeighbors = adjListVal.length() - adjListVal.replaceAll(",","").length() - 1;
			int lastComma = (adjListVal.length() > 1 ? (adjListVal.lastIndexOf(',')) : 1);
			if(numOfNeighbors == LPV){
				System.out.println(p + " \t " + adjListVal.substring(1, lastComma));
			}
		}
		
		// Initialize the material to use for writing on files
		sb = new StringBuilder();
		sb2 = new StringBuilder();
		pw = new PrintWriter(new File(outCSV + "AdjacencyList.csv"));
		pw2 = new PrintWriter(new File(outCSV + "AdjacencyMatrix.csv"));
		
		
		// for (adjList times)
			// append (to sb) " vertex + "," if(there is a neighbor) + neighbors + newLine "
		for (int p = 0; p < adjList.size(); p++){
			String adjListVal = adjList.get(Integer.toString(p));
			int lastComma = (adjListVal.length() > 1 ? (adjListVal.lastIndexOf(',')) : 1);
			//System.out.printf("Vertex%s: %s\n", p, adjListVal.substring(1, lastComma));
			sb.append(p + (numOfVer > 1 ? "," : "") + adjListVal.substring(1, lastComma));
					// ( ) to make sure not to print "," on the file when input is 1
			sb.append("\n");
		}
		
		// write to the file
		pw.write(sb.toString());

		
		// This is trivial
		// append (to sb2) rows and columns using a nested for loop and handle the newLine
		for (int row = 0; row < twoDimArray.length; row++) {
		    for (int column = 0; column < twoDimArray[row].length; column++) {
		    	//System.out.print(twoDimArray[row][column] + (column < twoDimArray[row].length-1 ? "," : ""));
		    	sb2.append(twoDimArray[row][column] + (column < twoDimArray[row].length-1 ? "," : ""));
		    										// to make sure not to add the last ","
		    }
		    //System.out.println();
		    sb2.append("\n");
		}
		
		// write to the file
		pw2.write(sb2.toString());
		
		// Close the PrintWriters
	    pw.close();
	    pw2.close();
		
	    
	    // Useful lines of code to automatically open the files for the user
	    // Make sure you close the CSV files before running the code / reaching this part
	    Desktop.getDesktop().open(new File(outCSV + "AdjacencyList.csv"));
	    Desktop.getDesktop().open(new File(outCSV + "AdjacencyMatrix.csv"));

	}
}
