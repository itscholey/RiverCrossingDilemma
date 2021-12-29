package physicalLayer;

import java.util.ArrayList;
import java.util.Random;

import deliberativeLayer.DecisionNetwork;
import reactiveLayer.ReactiveLayer;

/**
 * An abstract class to model an Agent that is situated in an environment, that has a two-layered
 * neural network architecture for learning and decision-making. Sub-goals are generated by the
 * deliberative layer, and these sub-goals are actioned by the reactive layer which decides on the
 * immediate next steps based on the chosen sub-goals.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public abstract class Agent {
	/** The current location. */
	protected Cell 			cell;
	/** The initial location. */
	protected Cell 			startCell;
	/** The object that is being carried, or null if otherwise. */
	protected CellObject 	carriedObject = null;
	/** Whether the goal has been achieved. */
	protected boolean 		achievedGoal = false;
	/** Whether a Stone is being carried (initially false). */
	protected boolean 		carrying = false;
	/** Whether a Stone has ever been carried (initially false). */
	protected boolean 		hasCarried = false;
	/** Whether it has dropped a Stone in the river to build a bridge (initially false). */
	protected boolean 		hasDroppedinRiver = false;
	/** Whether it is alive. */
	protected boolean 		isAlive;
	/** The number of moves taken. */
	protected int 			moves = 0;
	/** The number of Stones it has dropped in the river. */
	protected int 			numStones = 0;	
	/** The number of Resource objects that have been found. */
	protected int 			resourcesFound = 0;
	/** The number of rows in the environment. */
	protected int 			rows;
	/** The number of columns in the environment. */
	protected int 			cols;
	/** The current fitness based on performance. */
	protected Double 		fitness;
	/** TODO */
	protected Double		cumulativeFitness;
	/** The reactive network used to hill-climb towards sub-goals. */
	protected ReactiveLayer reactiveLayer;
	/** The deliberative network used to generate sub-goals based on the current state. */
	protected DecisionNetwork decisionNetwork;
	/** The assigned target Resources to collect in order to achieve the goal. */
	protected ArrayList<Resource> targets;
	/** A bridge is built if the number of Stones put onto a Water cell equals the depth of the river. A partial bridge exists
	 * when this is not equal but is more than 0. */
	protected static boolean partialBridgeExists = false;
	
	protected boolean aware;
	/** Decision network output from the previous timestep */
	protected double[] decisionOutput;
	/** TODO */
	protected String status = "";

	/**
	 * For invocation by subclasses. Creates an agent with an initial location in its environment, empty decision-making layers (random genes)
	 * and no allocated targets.
	 *  
	 * @param cell The initial location in the environment.
	 * @param rows The number of rows in the environment.
	 * @param cols The number of columns in the environment.
	 * @param TODO
	 */
	public Agent(Cell cell, int rows, int cols, Random rand, boolean aware) {
		startCell = cell;
		this.rows = rows;
		this.cols = cols;
		this.cell = cell;
		this.aware = aware;
		reactiveLayer = new ReactiveLayer(rows, cols);
		decisionNetwork = new DecisionNetwork(rand, aware);
		decisionOutput = new double[decisionNetwork.getOutputSize()];
		isAlive = true;
		targets = new ArrayList<Resource>();
		fitness = null;
		numStones = 0; // can be deleted? TODO
		partialBridgeExists = false;
	}
	
	/**
	 * For invocation by subclasses. Creates an agent with no initial location, populated with given genes in the deliberative layer, an empty
	 * reactive layer and no allocated targets. 
	 * 
	 * @param rows The number of rows in the environment.
	 * @param cols The number of columns in the environment.
	 * @param genes A 3D array containing the genes to populate the new agent with.
	 */
	public Agent(int rows, int cols, double[][][] genes, int[][] nns, boolean aware) {
		this.aware = aware;
		this.rows = rows;
		this.cols = cols;
		this.cell = null;
		reactiveLayer = new ReactiveLayer(rows, cols);
		decisionNetwork = new DecisionNetwork(genes, nns, aware);
		decisionOutput = new double[decisionNetwork.getOutputSize()];
		isAlive = true;
		targets = new ArrayList<Resource>();
		fitness = null;
		numStones = 0;
		partialBridgeExists = false;
	}
	
	/**
	 * Returns the collection of allocated Resources which are the targets to collect in order to achieve the goal.
	 * 
	 * @return The collection of target Resources that have been allocated.
	 */
	public ArrayList<Resource> getTargets() {
		return targets;
	}
	
	/**
	 * Allocates a Resource as a target to collect in order to achieve the goal. All targets must be collected.
	 * 
	 * @param target A Resource which must be collected in order to achieve the goal.
	 */
	public void setResource(Resource target) {
		targets.add(target);
		reactiveLayer.setResource(target);
	}
	
	/**
	 * Reset the current state, including starting location, moves, whether it is alive etc.
	 */
	public void reset() {
		cell = startCell;
		targets.clear();
		reactiveLayer = new ReactiveLayer(rows, cols);
		isAlive = true;
		carrying = false;
		hasCarried = false;
		hasDroppedinRiver = false;
		moves = 0;
		achievedGoal = false;
		carriedObject = null;
		resourcesFound = 0;
		numStones = 0;
		partialBridgeExists = false;
	}
	
	/**
	 * TODO
	 */
	public void resetCumulativeFitness() {
		cumulativeFitness = 0.0;
		status = "";
	}
	
	/** 
	 * Generate a {@link java.lang.String} representation of the current state, with text labels.
	 * Used for debugging purposes.
	 * 
	 * @return A {@link java.lang.String} representation of the current state.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Fitness: " + fitness + ", Moves: " + moves + ", Alive: " + isAlive + ", Carried: " + hasCarried +
				", Dropped in River: " + hasDroppedinRiver + ", Resources Found: " + resourcesFound + ", Completed Task: " + achievedGoal; 
	}
	
	/**
	 * Generate a {@link java.lang.String} representation of the current state, without text labels.
	 * 
	 * @return A {@link java.lang.String} representation of the current state.
	 */
	public String getStringStatus() {
		String s = "";
		s += fitness + "," + moves + ",";
		s += isAlive ? "t" : "f";
		s += ",";
		s += hasCarried ? "t" : "f";
		s += ",";
		s += hasDroppedinRiver ? "t" : "f";
		s += "," + numStones + "," + resourcesFound + ",";
		s += achievedGoal ? "t" : "f";
		return s;
		//return fitness + "," + moves + "," + isAlive + "," + hasCarried + "," + hasDroppedinRiver + "," + numStones + "," + resourcesFound + "," + achievedGoal;
	}
	
	/**
	 * Creates a new offspring based on this and another Agent, with no initial location.
	 * 
	 * @param other The other Agent in which this Agent should produce an offspring with.
	 * @return A new offspring with this and another Agent as its parents.
	 */
	public abstract Agent produceOffspring(Agent other);
	
	/** 
	 * Creates a new offspring with no initial location, which is a representative state of the population 
	 * at the current timestep.
	 *  	 
	 * @param genes The genes of the population, which can be processed depending on the definition of "tradition".
	 * @return A new agent that is a representative state of the population at the current timestep.
	 */
	public abstract Agent produceTraditionalOffspring(ArrayList<double[][][]> genes);
	
	/**
	 * Calculates and returns the fitness of the Agent based on a specified metric.
	 * 
	 * @return The calculated fitness of the Agent.
	 */
	public abstract Double evaluate();

	/** 
	 * Perform actions in a single move in a single timestep.
	 *
	 * @param status The status of the agent, like what type of cell it is on in the environment.
	 * @param grid The state of the environment at the current timestep.
	 * @return The updated state of the environment.
	 */
	public abstract Cell[][] move(double[] status, Cell[][] grid);
		
	/**
	 * Sets the starting location in the environment, and therefore the current location.
	 * 
	 * @param sc The starting and current location.
	 */
	public void setStartCell(Cell sc) {
		startCell = sc;
		cell = sc;
	}
	
	/**
	 * Returns the genes of the agent which are represented by a 3D array.
	 * 
	 * @return A 3D array containing the genes of the agent.
	 */
	public double[][][] getGenes() {
		return decisionNetwork.getGenes();
	}
	
	/**
	 * Returns the neuronal genes of the agent which are represented by a 3D array.
	 * TODO
	 * @return A 2D array containing the neurons of the agent.
	 */
	public int[][] getNeurons() {
		return decisionNetwork.getNeurons();
	}
	
	/**
	 * Sets the genes of the agent.
	 * 
	 * @param genes A 3D array representation of the genes.
	 */
	public void setGenes(double[][][] genes) {
		decisionNetwork.setGenes(genes);
	}
	
	/**
	 * Returns the current location.
	 * 
	 * @return The current location.
	 */
	public Cell getLocation() {
		return cell;
	}
	
	/**
	 * Sets the current location.
	 * 
	 * @param cell The location to set.
	 */
	public void setLocation(Cell cell) {
		this.cell = cell;
	}	
	
	/**
	 * Returns the number of elements that are tracked in the status of the agent, which are the inputs to the deliberative layeer.
	 * 
	 * @return The number of elements in the status.
	 */
	public int getStatusSize() {
		return decisionNetwork.getInputSize();
	}
	
	/**
	 * Returns whether a {@link physicalLayer.CellObject} is being carried or not (currently only a Stone).
	 * 
	 * @return true if something is being carried, false otherwise.
	 */
	public boolean isCarrying() {
		return carrying;
	}
	
	/**
	 * Returns whether the agent is alive or not. It will be alive unless it moves to a dangerous location in the environment.
	 * 
	 * @return true if the agent is alive, false otherwise.
	 */
	public boolean isAlive() {
		return isAlive;
	}
	
	/**
	 * Sets whether the agent is alive or not.
	 * 
	 * @param a true if it is alive, false otherwise.
	 */
	public void setAlive(boolean a) {
		isAlive = a;
	}
	
	/**
	 * Returns whether the goal has been achieved or not (if all the target Resources have been collected).
	 * 
	 * @return true if all target Resources have been collected, false otherwise.
	 */
	public boolean hasAchievedGoal() {
		return achievedGoal;
	}
	
	/**
	 * Returns the current fitness.
	 * 
	 * @return The current fitness.
	 */
	public Double getFitness() {
		return fitness;
	}
	
	/**
	 * TODO 
	 * @return
	 */
	public Double getCumulativeFitness() {
		return cumulativeFitness;
	}
	
	public void incrementCumulativeFitness(Double amount) {
		cumulativeFitness += amount;
	}
	
	public Double setTotalFitness() {
		fitness = cumulativeFitness;
		return fitness;
	}
	
	public void addToStatus(String s) {
		status += s + ",";
	}
	
	public String getEvalStatus() {
		return status;
	}
	
	/**
	 * Returns the number of moves that have been taken.
	 * 
	 * @return The number of moves taken.
	 */
	public int getMoves() {
		return moves;
	}
	
	public double[] getDecisions() {
		return decisionOutput;
	}
	
	public boolean isAware() {
		return aware;
	}
	
	/**
	 * Sets whether a bridge has been partially built in the world. A bridge is built successfully when the number of Stones placed into
	 * a Water cell equal its depth. A partial bridge is built if a number of Stones less than the depth but more than zero are placed in it.
	 * 
	 * @param exists Whether a partial bridge exists.
	 */
	public void setPartialBridgeExists(boolean exists) {
		partialBridgeExists = exists;
	}
	
	/**
	 * Returns if a bridge has been partially built in the world. A bridge is built successfully when the number of Stones placed into
	 * a Water cell equal its depth. A partial bridge is built if a number of Stones less than the depth but more than zero are placed in it.
	 * 
	 * @return true if a partial bridge exists, false otherwise.
	 */
	public boolean getPartialBridgeExists() {
		return partialBridgeExists;
	}
	
	/**
	 * Returns a 2D array representing the activation landscape, which is used to hill-climb towards sub-goals.
	 * 
	 * @return A 2D array representing the activation landscape.
	 */
	public Double[][] getActivationLandscape() {
		return reactiveLayer.getActivationLandscape();
	}
}