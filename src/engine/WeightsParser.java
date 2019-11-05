package engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A utility class to parse in weights from a file, and arrange them into an ArrayList 
 * containing 3D arrays of the weights that the file contains.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class WeightsParser {
		
	/**
	 * Creates a new WeightParser object.
	 */
	public WeightsParser() {
	}

	/**
	 * Parses a specified file for weights and arranges them into a container, which is then returned.
	 * The row/col parameters correspond to the deliberative layer's neural network architecture.
	 * 
	 * @param filename The file to parse, containing the weights of a population.
	 * @param rows1 The number of rows of the first weights array.
	 * @param cols1 The number of columns of the first weights array.
	 * @param rows2 The number of rows of the second weights array.
	 * @param cols2 The number of columns of the second weights array.
	 * @return
	 */
	public ArrayList<double[][][]> toArray(String filename, int rows1, int cols1, int rows2, int cols2) {
		double[][] weights1 = new double[rows1][cols1];
		double[][] weights2 = new double[rows2][cols2];
		ArrayList<double[][][]> weightsAll = new ArrayList<double[][][]>();
		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedReader(new FileReader(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int countRows1 = 0;
		int countRows2 = 0;

		while(sc.hasNextLine()) {
			String[] line = sc.nextLine().trim().split(",");
			if (line[0].startsWith("W1")) {
				for (int str = 1; str < line.length; str++) {
					line[str] = line[str].replaceAll("\\[", "").replaceAll("\\]", "");
					weights1[countRows1][str-1] = Double.parseDouble(line[str]);	
				}
				countRows1++;
				if (countRows1 == rows1) countRows1 = 0;
			}
			else if (line[0].startsWith("W2")) {

				for (int str = 1; str < line.length; str++) {
					line[str] = line[str].replaceAll("\\[", "").replaceAll("\\]", "");
					weights2[countRows2][str-1] = Double.parseDouble(line[str]);		
				}
				countRows2++;
				if (countRows2 == rows2) {
					countRows2 = 0;
					weightsAll.add(new double[][][] {weights1, weights2});
					weights1 = new double[rows1][cols1];
					weights2 = new double[rows2][cols2];
				}
			}
		}
		return weightsAll;
	}
}