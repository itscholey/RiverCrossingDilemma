package deliberativeLayer;
import java.util.ArrayList;
import java.util.Arrays;
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
 * h        input
 * i { [ x x x x x x ],
 * d   [ x x x x x x ],
 * d   [ x x x x x x ],
 * e   [ x x x x x x ]  }  each row is the incoming connections from the previous layer
 * n
 * 
 * 
 * weights2:
 * 
 *       hidden
 * o { [ x x x x ],
 * u   [ x x x x ],
 * t   [ x x x x ]  }  each row is the incoming connections from the previous layer
 * </pre>
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class DecisionNetwork {
	/** The number of inputs for the DecisionNetwork. */
	private static int	inputLayerSize 	= 6;
	/** The number units in the hidden layer for the DecisionNetwork. */
	private static int  hiddenLayerSize = 4;
	/** The number of outputs that the DecisionNetwork will generate. */
	private static int	outputLayerSize = 3;

	/** The input data to initialise the DecisionNetwork (only used for testing). */
	private double[] 	inputData;
	/** The generated output data. */
	private double[]	outputData;
	/** The expected output data used to compare actual results to expected results (only used for testing). */
	private double[] 	expectedOutputData;

	/** 
	 * <p>A double array containing the weights for the first layer of the DecisionNetwork.</p>
	 * <p>The number of rows equals the number of hidden units, and the columns equals the number of inputs. </p>
	 * <p><code>double[hiddenUnits][inputs]</code></p>
	 */
	private double[][]  weights1;
	
	/** 
	 * <p>A double array containing the weights for the second layer of the DecisionNetwork.</p>
	 * <p>The number of rows equals the number of outputs, and the columns equals the number of hidden units.</p>
	 * <p><code>double[outputs][hiddenUnits]</code></p>
	 */
	private double[][]  weights2;

	/** The rate in which the DecisionNetwork weights will mutate at each generation. */
	private static final double MUTATION_RATE  = 0.01;
	/** The rate in which the DecisionNetwork will crossover between parents at each generation; otherwise inherits from one parent. */
	private static final double CROSSOVER_RATE = 0.05; 

	/**
	 * Creates a new DecisionNetwork with the default {@link #inputLayerSize}, {@link #hiddenLayerSize} and {@link #outputLayerSize}, 
	 * with weights randomly initialised between -1 and 1.
	 */
	public DecisionNetwork() {
		inputData = new double[inputLayerSize];
		outputData = new double[outputLayerSize];	

		weights1 = new double[inputLayerSize][hiddenLayerSize]; 	// 6 / 4
		weights2 = new double[hiddenLayerSize][outputLayerSize];	// 4 / 3

		weights1 = randomiseWeights(weights1, true);
		weights2 = randomiseWeights(weights2, true);
	}

	/**
	 * <p>Creates a new DecisionNetwork populated using given weights.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * 
	 * @param weights A 3D array containing the weights in which to populate the new DecisionNetwork.
	 */
	public DecisionNetwork(double[][][] weights) {
		weights1 = weights[0];
		weights2 = weights[1];
	}

	/**
	 * <p>Creates the weights for a new offspring with the current DecisionNetwork as one parent, and the weights passed in as another parent.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * 
	 * @param other The weights of the other parent DecisionNetwork.
	 * @return The new weights of an offspring generated from both parents.
	 */
	public double[][][] createOffspring(double[][][] other) {
		// variables to contain the new offspring's weights
		double[][] offspring1 = new double[inputLayerSize][hiddenLayerSize];
		double[][] offspring2 = new double[hiddenLayerSize][outputLayerSize];

		// generate weights for first layer
			// here, weights1[0].length = cols = hiddenLayerSize = 4 (input connections for each hidden neuron)
		for (int col = 0; col < weights1[0].length; col++) {
			double probInheritFromOne = Engine.random.nextDouble();
			if (probInheritFromOne > CROSSOVER_RATE) {
				// inherit from a random parent
				if (Engine.random.nextBoolean()) {
					for (int row = 0; row < weights1.length; row++) {
						offspring1[row][col] = weights1[row][col];
					}
				}
				else {
					for (int row = 0; row < other[0].length; row++) {
						offspring1[row][col] = other[0][row][col];
					}
				}
			}
			else { 
				// single point crossover
				int point = Engine.random.nextInt(weights1.length);
				boolean firstParent = Engine.random.nextBoolean();

				for (int row = 0; row < weights1.length; row++) {
					if (row < point) {
						if (firstParent) {
							offspring1[row][col] = weights1[row][col];
						}
						else {
							offspring1[row][col] = other[0][row][col];
						}
					}
					else {
						if (!firstParent) {
							offspring1[row][col] = weights1[row][col];
						}
						else {
							offspring1[row][col] = other[0][row][col];
						}
					}
				}
			}
			
			// mutate all weights
			for (int w = 0; w < weights1.length; w++) {
				offspring1[w][col] = offspring1[w][col] + (Engine.random.nextGaussian() * MUTATION_RATE);
			} 
		}

		// generate weights for second layer
			// here, weights2[0].length = cols = outputLayerSize = 3 (input connections for each output neuron)
		for (int col = 0; col < weights2[0].length; col++) {
			double probInheritFromOne = Engine.random.nextDouble();
			if (probInheritFromOne > CROSSOVER_RATE) {
				// inherit from a random parent
				if (Engine.random.nextBoolean()) {
					for (int row = 0; row < weights2.length; row++) {
						offspring2[row][col] = weights2[row][col];
					}
				}
				else {
					for (int row = 0; row < other[1].length; row++) {
						offspring2[row][col] = other[1][row][col];
					}
				}
			}
			else { 
				// single point crossover
				int point = Engine.random.nextInt(weights2.length);
				boolean firstParent = Engine.random.nextBoolean();
				for (int row = 0; row < weights2.length; row++) {
					if (row < point) {
						if (firstParent) {
							offspring2[row][col] = weights2[row][col];
						}
						else {
							offspring2[row][col] = other[1][row][col];
						}
					}
					else {
						if (!firstParent) {
							offspring2[row][col] = weights2[row][col];
						}
						else {
							offspring2[row][col] = other[1][row][col];
						}
					}
				}
			}

			// mutate all weights
			for (int w = 0; w < weights2.length; w++) {					 
				offspring2[w][col] = offspring2[w][col] + (Engine.random.nextGaussian() * MUTATION_RATE);
			} 
		}
		
		return new double[][][] {offspring1, offspring2};
	}


	/**
	 * <p>Returns the genes of the DecisionNetwork in the format of a 3D array.</p>
	 * <p><code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p> 
	 * 
	 * @return A 3D array containing the weights of the DecisionNetwork.
	 */
	public double[][][] getGenes() {
		return new double[][][] {weights1, weights2};
	}

	/**
	 * <p>Sets the weights that a DecisionNetwork contains for both layers.</p>
	 * <p>Genes are passed in through a 3D array in the format <code>double[][rows][cols]</code>,</p>
	 * <p>where <code>double[0]</code> contains {@link #weights1} and <code>double[1]</code> {@link #weights2}.</p>
	 * 
	 * @param genes A 3D array containing the weights that the DecisionNetwork will use.
	 */
	public void setGenes(double[][][] genes) {
		weights1 = genes[0];
		weights2 = genes[1];
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
	 * 
	 * @param input A double array containing the input values for the calculations.
	 * @return A double array containing the output values, based on the input values, after forward propagation through the DecisionNetwork.
	 */
	public double[] forward(double[] input) {
		// the output array: this will end up as double[outputLayerSize][1] (i.e. one row)
		double[][] yHat;
		// get first activation value
		double[][] z2 = matrixMult(new double[][] {input}, weights1);
		// apply activation function
		double[][] a2 = hyperbolicTangent(z2);
		// get second activation value
		double[][] z3 = matrixMult(a2, weights2);
		// apply final activation function to get predicted output
		yHat = hyperbolicTangent(z3); 
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
	 * Applies the hyperbolic tangent function to the given input matrix. The matrix will be processed as follows:
	 * <ul>
	 * 	<li>a value below 0.35 will be set as -1</li>
	 * 	<li>a value above 0.65 will be set as 1</li>
	 * 	<li>all other values will be set as 0</li>
	 * </ul>
	 * 
	 * @param matrix The matrix to apply the hyperbolic tangent function to.
	 * @return The processed input with the hyperbolic tangent function applied.
	 */
	public double[][] hyperbolicTangent(double[][] matrix) {
		double[][] result = new double[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				int ans = 0;
				if (matrix[i][j] < 0.35) {
					ans = -1;
				}
				else if (matrix[i][j] > 0.65) {
					ans = 1;
				}
				result[i][j] = ans;
			}
		}
		return result;
	}

	/**
	 * Returns the number of neurons in the input layer.
	 * 
	 * @return {@link #inputLayerSize}
	 */
	public int getInputSize() {
		return inputLayerSize;
	}

	/**
	 * Returns the number of neurons in the output layer.
	 * @return {@link #outputLayerSize}
	 */
	public int getOutputSize() {
		return outputLayerSize;
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
		double[][][] results = {new double[inputLayerSize][hiddenLayerSize], new double[hiddenLayerSize][outputLayerSize]};
		
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
}