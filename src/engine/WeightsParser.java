package engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import deliberativeLayer.DecisionNetwork;

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
	 * @return A collection of the parsed weights, arranged in a 3D array for each member of the population in the file.
	 * TODO
	 */
	public ArrayList<DecisionNetwork> toArray(String filename) {
		ArrayList<DecisionNetwork> population = new ArrayList<DecisionNetwork>(); //change back to [][][]

		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedReader(new FileReader(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int countRows = 0;
		int countNeurons = 0;
		boolean finishedWeights = false;
		
		while(sc.hasNextLine()) {
			String[] line = sc.nextLine().trim().split(",");
						
			/*///////{6,8,6,4,3}
			 * if line starts with "X start", indicates the start of a block of weights to read in, while line is not "X end"
			 */
			if (line[0].contains("start")) {
				// one less layer of weights than there are neuronal layers
				double[][][] weights = new double[DecisionNetwork.getNetworkStructure().length-1][][];
				int[][] neurons = new int[DecisionNetwork.getNetworkStructure().length-2][];
				for (int i = 0; i < DecisionNetwork.getNetworkStructure().length-1; i++) {
					weights[i] = new double[DecisionNetwork.getNetworkStructure()[i]][DecisionNetwork.getNetworkStructure()[i+1]];
				}
				for (int i = 0; i < DecisionNetwork.getNetworkStructure().length-2; i++) {
					neurons[i] = new int[DecisionNetwork.getNetworkStructure()[i+1]];
				}
				line = sc.nextLine().trim().split(",");
				//line = sc.nextLine().trim().split(",");
				while (!line[0].contains("end")) {
					if (line[0].startsWith("--")) {
						line = sc.nextLine().trim().split(",");
					}
					if (line[0].startsWith("W")) { // e.g. starts with W0 - populate weight layer 0. Weights arrays start on new lines contained in []
						line = sc.nextLine().trim().split(",");
						for (int row = 0; row < DecisionNetwork.getNetworkStructure()[countRows]; row++) {
							for (int col = 0; col < DecisionNetwork.getNetworkStructure()[countRows+1]; col++) {
								line[col] = line[col].replaceAll("\\[", "").replaceAll("\\]", "");
								weights[countRows][row][col] = Double.parseDouble(line[col]);
							}
							line = sc.nextLine().trim().split(",");
						}
						countRows++;
					}
					
					
					
					if (line[0].startsWith("N")) { // e.g. starts with N0 - populate neuron layer 0.
						
						for (int row = 0; row < DecisionNetwork.getNetworkStructure()[countNeurons+1]; row++) {
							line[row] = line[row].replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("N[0-9]*: ", "").replaceAll(" ", "");
							neurons[countNeurons][row] = Integer.parseInt(line[row]);
							
						}
						line = sc.nextLine().trim().split(",");
						
						countNeurons++;
					}
					
					if (countRows == DecisionNetwork.getNetworkStructure().length-1) {
						finishedWeights = true;
					}

					if (finishedWeights && countNeurons == DecisionNetwork.getNetworkStructure().length-2) {
						population.add(new DecisionNetwork(weights, neurons));
						line = sc.nextLine().trim().split(",");
						countRows = 0;
						countNeurons = 0;
						finishedWeights = false;
					}
					
					if (countRows == DecisionNetwork.getNetworkStructure().length || line[0].isEmpty()) {
						line = sc.nextLine().trim().split(",");
						countRows = 0;
						countNeurons = 0;
					}
				}
				countRows = 0;
				countNeurons = 0;
			}
		}
		return population;
	}
	
}