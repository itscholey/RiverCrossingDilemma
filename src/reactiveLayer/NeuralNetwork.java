package reactiveLayer;

import physicalLayer.Location;

/**
 * A class to model a topologically-organised lattice of neurons, resembling a 2D grid of neurons that are connected
 * to each neuron in its Moore neighbourhood (its surrounding eight neurons). * 
 * 
 * <ul>
 * 	<li>MDF = Monotonically Decreasing Function</li>
 *	<li>A   = Passive Decay Rate Constant</li>
 * 	<li>D	= Euclidean Distance (1 or sqrt(2))</li>
 * 	<li>Some good parameter combinations for the shunting equation are:
 * 		<ul>
 * 			<li>A=0.2,  MDF=1/6D</li>
 * 			<li>A=0.25, MDF=1/6D</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class NeuralNetwork {
	/** The data structure to contain the activations of the neural network. */
	private Neuron[][] network;
	/** The number of rows. */
	private int rows;
	/** The number of columns. */
	private int cols;
	/** The iota value: a value representing the boundaries of the activation landscape. */
	private static final int IOTA = 15;
	
	/**
	 * Creates a topologically-organised lattice of neurons in a rows x cols grid.
	 * 
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public NeuralNetwork(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		network = new Neuron[rows][cols];
		setup();
	}
	
	/**
	 * Returns the neuron at a specified location.
	 * 
	 * @param i The row in the network.
	 * @param j The column in the network.
	 * @return The neuron at the specified location.
	 */
	public Neuron get(int i, int j) {
		return network[i][j];
	}
	
	/**
	 * Returns a 2D representation of the activation landscape.
	 * 
	 * @return a 2D representation of the activation landscape.
	 */
	public Double[][] getActivationLandscape() {
		Double[][] landscape = new Double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				landscape[i][j] = network[i][j].getActivation();
			}
		}
		return landscape;
	}
	
	/**
	 * Generates a new, empty neural network.
	 */
	private void setup() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				network[i][j] = new Neuron(new Location(i, j));
			}
		}
		setNeighbours();
	}
	
	/**
	 * Traverses the neural network to identify and set the neighbouring neurons for each neuron.
	 */
	private void setNeighbours() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int k = 0;
				for (int i2 = i-1; i2 <= i+1; i2++) {
					for (int j2 = j-1; j2 <= j+1; j2++) {
						if (i2 >= 0 && j2 >= 0 && i2 < rows && j2 < cols) {
							if (network[i2][j2] != null && !(i == i2 && j == j2)) {
								network[i][j].setNeighbour(network[i2][j2], k);
							}
						}
						// increment index counter unless it is the neuron itself
						if (!(i == i2 && j == j2)) k++;
					}
				}
			}
		}
	}
	
	/**
	 * Traverses the neural network and updates the activations based on the inputted iota grid, to create a hill-climbing landscape. 
	 * The iota grid has the same width and height as the neural network and is filled with zeros unless:
	 * <ul>
	 * 	<li>A cell is marked as attractive, in which case the iota is very positive;</li>
	 * 	<li>A cell is marked as repulsive, in which case the iota is very negative.</li>
	 * </ul>
	 * 
	 * @param iotaGrid A 2D grid that maps to the neural network structure, where values are either 0, +iota or -iota.
	 */
	public void updateActivations(int[][] iotaGrid) {	
		Neuron[][] newGrid = new Neuron[rows][cols];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// -Axi + sum(wij[xj]+) + Ii
				Double newActivation = -0.2*network[i][j].getActivation() + iotaGrid[i][j];
				
				Double sumWeightedActivations = 0.0;
				for (int k = 0; k < 8; k++) {
					double temp = 0.0;
					if (network[i][j].getNeighbours()[k] != null) {
						temp = Math.max(0, network[i][j].getNeighbours()[k].getActivation());
					}
					sumWeightedActivations += (temp * (1 / (6*network[i][j].getWeights()[k])));
				}
				newActivation = Math.min(IOTA, (sumWeightedActivations + newActivation));
				// limit the accuracy of the data
				if (newActivation < 0.0001 && newActivation > 0.0) {
					newActivation = 0.0;
				}
				newGrid[i][j] = new Neuron(newActivation, new Location(i,j));
			}
		}
		network = newGrid;
		setNeighbours();
	}
}