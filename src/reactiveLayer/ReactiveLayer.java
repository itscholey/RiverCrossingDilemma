package reactiveLayer;

import java.util.ArrayList;
import physicalLayer.*;

/**
 * A class to model the decision-making processes for reactive behaviour. Behaviour is determined by the chosen sub-goals - 
 * and is in this case determined by a hill-climbing landscape based on those goals.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class ReactiveLayer {
	/**	The neural network structure representing the reactive layer. */
	private NeuralNetwork network;
	/** The assigned target Resources to collect in order to achieve the goal. */
	private ArrayList<Resource> targets;
	/** A value to determine the height of hills and depths of valleys in the activity landscape. */
	private static final int IOTA = 15;
	
	/**
	 * Creates an empty 2D activity landscape to model the decision-making processes for reactive behaviour, without no initial targets.
	 * 
	 * @param rows The number of rows.
	 * @param cols The number of columns.
	 */
	public ReactiveLayer(int rows, int cols) {
		network = new NeuralNetwork(rows, cols);
		targets = new ArrayList<Resource>();
	}
	
	/**
	 * Allocates a Resource object as a target to collect to achieve goals.
	 * 
	 * @param target The target Resource to collect.
	 */
	public void setResource(Resource target) {
		targets.add(target);
	}
	
	/**
	 * Traverses the reactive layer model and updates the activation landscape based on the chosen sub-goals and
	 * the state of the environment. <b>Locations where other Agents reside should have value of -IOTA, but they 
	 * are currently 0. </b>
	 * 
	 * <p>Decision output and Iota value mappings:</p>
	 * <ul>
	 * 	<li>output = 1, +IOTA</li>
	 * 	<li>output = -1, -IOTA</li>
	 * 	<li>output = 0, no change</li>
	 * 	<li>agent, -IOTA (not yet implemented, is 0)</li> TODO
	 * </ul>
	 * 
	 * 
	 * @param decisionOutput The chosen sub-goals.
	 * @param partialBridgeExists true if a bridge has been partially made in the environment.
	 * @param grid The state of the environment.
	 */
	public void updateActivations(double[] decisionOutput, boolean partialBridgeExists, Cell[][] grid) {
		// TODO -IOTA for location of other agents
		// a 2D grid to track the iota values based on chosen sub-goals
		int[][] iotaGrid = new int[grid.length][grid[0].length];
		// traverse 2D landscape
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].getObject() instanceof Resource) {
					if (decisionOutput[0] >= 1.0) {
						iotaGrid[i][j] = IOTA;
					}
					else if (decisionOutput[0] <= -1.0) {
						iotaGrid[i][j] = -IOTA;
					}
					// if the cell contains a Resource which is not allocated to this, -IOTA
					if  (!targets.contains(grid[i][j].getObject())) {
						iotaGrid[i][j] = -IOTA;
					}
				}
				else if (grid[i][j].getObject() instanceof Stone) {
					if (decisionOutput[1] >= 1.0) {
						iotaGrid[i][j] = IOTA;
					}
					else if (decisionOutput[1] <= -1.0) {
						iotaGrid[i][j] = -IOTA;
					}
				}
				else if (grid[i][j].getObject() instanceof Water) {
					if (decisionOutput[2] >= 1.0) {
						// if a bridge has been part built
						if (partialBridgeExists) { // go towards shallowest part 
							if (((Water)grid[i][j].getObject()).getDepth() <= 1) {
									iotaGrid[i][j] = IOTA;
							}
							else {
								iotaGrid[i][j] = -IOTA;
							}
						}
						// if no bridge is built, make all water attractive
						else {
							iotaGrid[i][j] = IOTA;
						}
					}
					else if (decisionOutput[2] <= -1.0) {
						iotaGrid[i][j] = -IOTA;
					}
				}
			}
		}
		network.updateActivations(iotaGrid);
	}
	
	/**
	 * Returns the activity landscape as a 2D array of values.
	 * Used for the {@link engine.ActivityLandscapeView} display.
	 * 
	 * @return A 2D array of values.
	 */
	public Double[][] getActivationLandscape() {
		return network.getActivationLandscape();
	}
	
	/**
	 * Returns the Moore's neighbourhood of neurons, which is the surrounding eight locations, of a specified location.
	 * 
	 * @param loc The location to find the neighbours of.
	 * @return An array of the eight valid neuron positions surrounding the given location; elements will be the Neuron at 
	 * the neighbourhood location if valid, or null if invalid (like if the Neuron is on an edge or corner of the grid).
	 */
	public Neuron[] getNeighbourLandscape(Location loc) {
		Neuron[] result = new Neuron[network.get(loc.toArray()[0], loc.toArray()[1]).getNeighbours().length];
		for (int i = 0; i < result.length; i++) {
			if (network.get(loc.toArray()[0], loc.toArray()[1]).getNeighbours() != null) {
				result[i] = network.get(loc.toArray()[0], loc.toArray()[1]).getNeighbours()[i];
			}
			else {
				result[i] = null;
			}
		}
		return result;
	}
}