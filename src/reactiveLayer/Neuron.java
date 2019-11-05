package reactiveLayer;

import physicalLayer.Location;

/**
 * A class to model a Neuron with a location, which holds the neurons it is neighbours to, and has an activation value.
 * 
 * <pre>
 * The Moore's neighbourhood is modelled as follows:
 *   1 2 3
 *   \ | /
 * 4 - x - 5
 *   / | \  
 *   6 7 8 
 * </pre>
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Neuron {
	/** The Moore's neighbourhood of Neurons. Elements will be null if invalid. */
	private Neuron[] neighbours;
	/** The activation value of the Neuron. */
	private double activation;
	/** The location of the Neuron in the surrounding grid. */
	private Location location;
	/** The weights connecting to its surrounding neighbours. */
	private static final double[] WEIGHTS = {
												Math.sqrt(2), //   \ | /
												1,            //   - x -
												Math.sqrt(2), //   / | \
												1,
												1,
												Math.sqrt(2),
												1,
												Math.sqrt(2)
											};
	/**
	 * Creates a Neuron with a specified location and activation. The Moore neighbourhood is initialised as empty.
	 * 
	 * @param activation The activation value.
	 * @param loc The location in the network.
	 */
	public Neuron(double activation, Location loc) {
		this.activation = activation;
		neighbours = new Neuron[8];
		location = loc;
	}
	
	/**
	 * Creates a Neuron with a specified location, and an activation of 0. The Moore neighbourhood is initialised as empty.
	 * 
	 * @param loc The location in the network.
	 */
	public Neuron(Location loc) {
		this(0.0, loc);
	}
	
	/**
	 * Returns the location of the Neuron.
	 * 
	 * @return The location.
	 */
	public Location getLocation() {
		return location;
	}
		
	/**
	 * Returns the activation value of the Neuron.
	 * 
	 * @return The activation value.
	 */
	public double getActivation() {
		return activation;
	}
	
	/**
	 * Sets the activation value of the Neuron to a specified value.
	 * 
	 * @param act The activation value to set for the Neuron.
	 */
	public void setActivation(double act) {
		activation = act;
	}
	
	/**
	 * Returns the Moore's neighbourhood of the Neuron, with elements as null if invalid.
	 * 
	 * @return The Moore's neighbourhood of neurons.
	 */
	public Neuron[] getNeighbours() {
		return neighbours;
	}
	
	/**
	 * Allocates a neighbour to the Neuron at a specified neighbourhood position.
	 * 
	 * @param nhbr The neighbouring Neuron to add.
	 * @param numNhbr The position of the neighbouring Neuron in the neighbourhood array.
	 */
	public void setNeighbour(Neuron nhbr, int numNhbr) {
		neighbours[numNhbr] = nhbr;
	}
	
	/**
	 * Returns the weights of the connections to the neighbouring neurons.
	 * 
	 * @return The connecting weights.
	 */
	public double[] getWeights() {
		return WEIGHTS;
	}
	
	/**
	 * Returns a string representation of the Neuron, by outputting the activations of the neighbours.
	 * 
	 * @return A string representation of the Neuron.
	 */
	@Override
	public String toString() {
		String result = "[";
		
		for (int i = 0; i < neighbours.length; i++) {
			if (neighbours[i] != null) {
				result += neighbours[i].getActivation() + " ";
			}
			else {
				result += "null ";
			}
		}
		
		result += "]";
		return result;
	}
}