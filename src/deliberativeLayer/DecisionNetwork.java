package deliberativeLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

import engine.Engine;

/**
 * A data structure used to contain genes/weights with a static structure and size. 
 * 
 * <pre>
 * Output: (action: Resource, Stone, Water) 
 * 
 *     R   S   W
 * 
 *   x   x   x   x
 * 
 *  g  r  s  w  c  b
 * 
 * Input: (status of animat: on grass, resource, stone, water, carrying status, bridgeBeenBuilt)
 * 
 * 
 * 
 * weights1: 
 * 
 *     h i d d e n
 *   { [ x x x x ],
 * i   [ x x x x ],
 * n   [ x x x x ],
 * p   [ x x x x ],
 * u   [ x x x x ],
 * t   [ x x x x ]  }  each row is the incoming connections from one neuron in the previous layer to one in the next
 * 
 * 
 * 
 * weights2:
 * 
 * h     o u t
 * i { [ x x x ],
 * d   [ x x x ],
 * d   [ x x x ],
 * e   [ x x x ]  }    each row is the incoming connections from one neuron in the previous layer to one in the next
 * n               
 * </pre>
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class DecisionNetwork {

	/** An array that dictates the structure of the network, where each element contains the number of neurons in each layer sequentially.
	 * The first element dictates the number of inputs, and the last element dictates the number of outputs. All other elements dictate the 
	 * number of hidden layers and the number of neurons they hold respectively. TODO*/
	private static int[] networkStructure = {6,8,6,4,3};
	
	/** The generated output data. TODO */
	private double[]	outputData;
	
	/** TODO
	 * <p>A double array containing the weights of the DecisionNetwork.</p>
	 * <p>The number of rows equals the number of neurons in the preceding layer, and the columns equals the 
	 * number of neurons in the connecting layer.</p>
	 */
	private double[][][] weights;
	
	/** TODO */
	private int[][] neurons;

	/** The rate in which the DecisionNetwork weights will mutate at each generation. */
	private static final double MUTATION_RATE  = 0.01;
	/** The rate in which the DecisionNetwork will crossover between parents at each generation; otherwise inherits from one parent. */
	private static final double CROSSOVER_RATE = 0.05;
	/** The rate in which the DecisionNetwork will mutate neuromodulatory neurons at each generation. TODO */
	private static final double MODULATION_RATE = 0.15;

	/** TODO
	 * Creates a new DecisionNetwork with the default structure specified with <code>numberOfNeurons</code>, 
	 * with weights randomly initialised between -1 and 1.
	 */
	public DecisionNetwork() {
		
		weights = new double[networkStructure.length-1][][];
		neurons = new int[networkStructure.length-2][];
		
		for(int i = 1; i < networkStructure.length; i++) {
			weights[i-1] = new double[networkStructure[i-1]][networkStructure[i]];
			weights[i-1] = randomiseWeights(weights[i-1], true);
		}
		for (int i = 0; i < neurons.length; i++) {
			neurons[i] = new int[networkStructure[i+1]]; // neurons start as 0 - standard neurons
		}
	}

	/**
	 * <p>Creates a new DecisionNetwork populated using given weights.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * TODO
	 * @param weights A 3D array containing the weights in which to populate the new DecisionNetwork.
	 */
	public DecisionNetwork(double[][][] w, int[][] n) {
		weights = w;
		neurons = n;
	}

	/**
	 * <p>Creates the weights for a new offspring with the current DecisionNetwork as one parent, and the weights passed in as another parent.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * TODO
	 * @param other The weights of the other parent DecisionNetwork.
	 * @return The new weights of an offspring generated from both parents.
	 */
	public DecisionNetwork createOffspring(double[][][] other, int[][] otherNeurons) {
		// variable to contain the new offspring's weights
		double[][][] offspring = new double[networkStructure.length-1][][];
		int[][] offNeurons = new int[networkStructure.length-2][];
		for(int i = 1; i < networkStructure.length; i++) {
			offspring[i-1] = new double[networkStructure[i-1]][networkStructure[i]];
		}
		for (int i = 0; i < offNeurons.length; i++) {
			offNeurons[i] = new int[networkStructure[i+1]]; // neurons start as 0 - standard neurons
		}
		
		for (int layer = 0; layer < weights.length; layer++) {
			for (int col = 0; col < weights[layer][0].length; col++) {
			
				double probInheritFromOne = Engine.random.nextDouble();
				if (probInheritFromOne > CROSSOVER_RATE) {
					// inherit from a random parent
					if (Engine.random.nextBoolean()) {
						for (int row = 0; row < weights[layer].length; row++) {
							offspring[layer][row][col] = weights[layer][row][col];
						}
					}
					else {
						for (int row = 0; row < other[layer].length; row++) {
							offspring[layer][row][col] = other[layer][row][col];
						}
					}
				}
				else { 
					// single point crossover
					int point = Engine.random.nextInt(weights[layer].length);
					boolean firstParent = Engine.random.nextBoolean();

					for (int row = 0; row < weights[layer].length; row++) {
						if (row < point) {
							if (firstParent) {
								offspring[layer][row][col] = weights[layer][row][col];
							}
							else {
								offspring[layer][row][col] = other[layer][row][col];
							}
						}
						else {
							if (!firstParent) {
								offspring[layer][row][col] = weights[layer][row][col];
							}
							else {
								offspring[layer][row][col] = other[layer][row][col];
							}
						}
					}
				}
				
				// mutate all weights
				for (int w = 0; w < weights[layer].length; w++) {
					offspring[layer][w][col] = offspring[layer][w][col] + (Engine.random.nextGaussian() * MUTATION_RATE);
				} 
			}
			
			// inherit from a random parent TODO
			if (Engine.random.nextBoolean()) {
				for (int a = 0; a < neurons.length; a++) {
					for (int b = 0; b < neurons[a].length; b++) {
						offNeurons[a][b] = neurons[a][b];
					}
				}
			}
			else {
				for (int a = 0; a < otherNeurons.length; a++) {
					for (int b = 0; b < otherNeurons[a].length; b++) {
						offNeurons[a][b] = otherNeurons[a][b];
					}
				}
			}			
		}
		
		if (Engine.random.nextDouble() < MODULATION_RATE) {
			// mutate one neuron
			int[] target = new int[2];
			target[0] = Engine.random.nextInt(offNeurons.length);
			target[1] = Engine.random.nextInt(offNeurons[target[0]].length);
			offNeurons[target[0]][target[1]] = (offNeurons[target[0]][target[1]] + 1) % 2;
		}
		return new DecisionNetwork(offspring, offNeurons);
	}


	/**
	 * <p>Returns the genes of the DecisionNetwork in the format of a 3D array.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p> 
	 * TODO
	 * @return A 3D array containing the weights of the DecisionNetwork.
	 */
	public double[][][] getGenes() {
		return weights;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public int[][] getNeurons() {
		return neurons;
	}

	/**
	 * <p>Sets the weights that a DecisionNetwork contains for both layers.</p>
	 * <p>Genes are passed in through a 3D array in the format <code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * 
	 * @param genes A 3D array containing the weights that the DecisionNetwork will use.
	 */
	public void setGenes(double[][][] genes) {
		weights = genes; // TODO copy
	}
	
	/**
	 * TODO
	 * @param n
	 */
	public void setNeurons(int[][] n) {
		neurons = n;
	}

	/**
	 * Returns a double array of the values the DecisionNetwork generated in the last run.
	 * 
	 * @return A double array containing the output values.
	 */
	public double[] getOutput() {
		return outputData;
	}

	/**
	 * Generates output values by performing forward propagation based on the given input values, using the {@link #hyperbolicTangent(double[][])} activation function.
	 * TODO
	 * @param input A double array containing the input values for the calculations.
	 * @return A double array containing the output values, based on the input values, after forward propagation through the DecisionNetwork.
	 */
	public double[] forward(double[] input) {
		// the output array: this will end up as double[outputLayerSize][1] (i.e. one row)
		double[][] yHat;
		// get first activation value
		double[][] z = matrixMult(new double[][] {input}, weights[0]);
		double[][] a;
		
		
		for (int i = 1; i < weights.length; i++) {
			// apply activation function
			a = tanh(z);
			// modulate and get activation value
			z = matrixMult(modulate(a, i-1), weights[i]);
		}
		// apply final activation function to get output
		yHat = tanh(z);
		//limit output
		for (int i = 0; i < yHat[0].length; i++) {
			if (yHat[0][i] > 0.3) {
				yHat[0][i] = 1.0;
			}
			else if (yHat[0][i] < -0.3) {
				yHat[0][i] = -1.0;
			}
			else {
				yHat[0][i] = 0.0;
			}
		}
		// applying the activation function returns a single row
		return yHat[0];
	}

	/**
	 * Applies the sigmoid function to the given input matrix.
	 * 
	 * @param matrix The matrix to apply the sigmoid function to.
	 * @return The processed input with the sigmoid function applied.
	 */
	private double[][] sigmoid(double[][] matrix) {
		double[][] result = new double[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				result[i][j] = (1 / (1 + Math.exp(-matrix[i][j])));
			}
		}
		return result;
	}
	
	/**
	 * TODO 
	 * @param matrix
	 * @return
	 */
	private double[][] tanh(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = Math.tanh(matrix[i][j]);
			}
		}
		return matrix;
	}
	
	/**
	 * TODO a is instantiated with a set size, so throws error every time
	 * @param matrix
	 * @param layer
	 * @return
	 * @throws Exception
	 */
	private double[][] modulate(double[][] matrix, int layer) {
		/*if (matrix[0].length != neurons[layer].length) {
			throw new Exception("Tried to modulate a hidden layer but the input and neuron layer do not have the same size.\n "
					+ "Matrix dimensions: matrix.length = " + matrix.length + ", matrix[0].length = " + matrix[0].length 
					+ ", layer size = " + neurons[layer].length);
		}*/
		// if neuron is 1, modulate, if input is -ve, turn off
		for (int i = 0; i < neurons[layer].length; i++) {
			if (neurons[layer][i] == 1 && matrix[0][i] < 0) {
				matrix[0][i] = 0;
			}
			// if input is +ve, leave as is
		}
		return matrix;
	}

	/**
	 * Returns the number of neurons in the input layer.
	 * 
	 * @return {@link #inputLayerSize}
	 */
	public int getInputSize() {
		return networkStructure[0];
	}

	/**
	 * Returns the number of neurons in the output layer.
	 * @return {@link #outputLayerSize}
	 */
	public int getOutputSize() {
		return networkStructure[networkStructure.length-1];
	}

	/**
	 * Performs matrix multiplication between the input matrices and returns the resulting matrix.
	 * 
	 * @param first The first matrix operand.
	 * @param second The second matrix operand.
	 * @return A matrix resulting from multiplying the first by the second matrix.
	 */
	private double[][] matrixMult(double[][] first, double[][] second) {
		double[][] result = new double[first.length][second[0].length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				for (int k = 0; k < first[i].length; k++) {
					result[i][j] += first[i][k] * second[k][j];
				}
			}
		}
		return result;
	}

	/**
	 * Generates random weights for all layers in the DecisionNetwork, or fills the weights with zeros if <code>randomise</code> is false.
	 * 
	 * @param matrix The matrix to populate.
	 * @param randomise Randomises weight values between -1 and 1 if true, zeros if false
	 * @return The resulting, populated matrix.
	 */
	private double[][] randomiseWeights(double[][] matrix, boolean randomise) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (randomise) {
					matrix[i][j] = ((Engine.random.nextDouble() * 2) - 1); 
				}
				else {
					matrix[i][j] = 0.0;
				}
			}
		}
		return matrix;
	}

	/**
	 * Calculates and returns a representative state of a collection of DecisionNetworks to simulate tradition. The <b>median value</b> for each weight in each DecisionNetwork in the
	 * collection is calculated, resulting in a single set of weights that reflect the current state of the collection. This assumes that calculating the median weights is a sufficient
	 * way to simulate tradition.
	 * 
	 * @param genes A collection of many DecisionNetwork weights, in which to find a representative/traditional state.
	 * @return A single set of weights in a 3D array, which are a representative/traditional state correlating to the collective population.
	 */
	public static double[][][] getCommonGenes(ArrayList<double[][][]> genes) {
		double[][][] results = new double[networkStructure.length][][];
		
		for (int w = 0; w < genes.get(0).length; w++) {
			for (int row = 0; row < genes.get(0)[w].length; row++) {
				for (int col = 0; col < genes.get(0)[w][row].length; col++) {
					double[] medianTmp = new double[genes.size()];
					for (int gene = 0; gene < genes.size(); gene++) {
						medianTmp[gene] = genes.get(gene)[w][row][col];
					}
					Arrays.sort(medianTmp);
					results[w][row][col] = medianTmp[medianTmp.length/2];
				}
			}
		}
		return results;
	}
	
	// TODO UPDATE
	public String toString() {
		String result = "--WEIGHTS--\n";
		
		for (int i = 0; i < weights.length; i++) {
			result += "W" + i + ":\n";
			for (int j = 0; j < weights[i].length; j++) {
				result += Arrays.toString(weights[i][j]) + "\n";
			}
		}
		
		result += "\n--NEURONS--\n";
		
		for (int i = 0; i < neurons.length; i++) {
			result += "N" + i + ": " + Arrays.toString(neurons[i]) + "\n";
		}
		return result; 
	}
	
}