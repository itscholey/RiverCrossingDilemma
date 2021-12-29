package physicalLayer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to model a 2D grid-world environment with different objects placed in it.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class PhysicalLayer {
	/** A 2D array of cells which represents the environment. */
	private Cell[][] grid;
	/** The number of rows. */
	private int rows;
	/** The number of columns. */
	private int cols;
	/** A collection storing the agents in the environment. */
	private ArrayList<Agent> agents;
	/** A collection of the Resource objects in the environment, which are to be collected so goals can be achieved. */
	private ArrayList<Resource> resources;
	/** A collection of the activation landscapes of the agents, which they use to hill-climb towards their goals.
	 * Used in {@link engine.ActivityLandscapeView}. */
	private ArrayList<Double[][]> landscapes;
	
	/**
	 * Creates a new environment with a specified size, and sets up the objects contained within the environment such as
	 * Stones, Traps, Water and Resources.
	 * 
	 * @param rows The number of rows the environment has.
	 * @param cols The number of columns the environment has.
	 */
	public PhysicalLayer(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		setup();
		landscapes = new ArrayList<Double[][]>();
		agents = new ArrayList<Agent>();
	}	
	
	/**
	 * Adds an Agent to the environment, and allocates its target Resource objects.
	 * 
	 * @param a The agent to add to the environment.
	 */
	public void addAgent(Agent a) {
		agents.add(a);
		agents.get(agents.size()-1).setResource(resources.get(agents.size()-1));
		agents.get(agents.size()-1).setResource(resources.get(agents.size()+1));

		landscapes.add(new Double[rows][cols]);
	}
	
	/**
	 * Returns a collection of the agents within the environment.
	 * 
	 * @return The agents in the environment.
	 */
	public ArrayList<Agent> getAgents() {
		return agents;
	}
	
	/**
	 * Returns the Cell at a specified location in the environment.
	 * 
	 * @param x The row of the Cell in the environment.
	 * @param y The column of the Cell in the environment.
	 * @return The Cell object at the specified location.
	 */
	public Cell getCell(int x, int y) {
		return grid[x][y];
	}
	
	/**
	 * Returns the number of rows the environment has.
	 * 
	 * @return The number of rows.
	 */
	public int getRows() {
		return rows;
	}
	
	/**
	 * Returns the number of columns the environment has.
	 * 
	 * @return The number of columns.
	 */
	public int getCols() {
		return cols;
	}
	
	/**
	 * Simulates one timestep where agents will move if they are alive.
	 */
	public void update() {

		for (int i = 0; i < agents.size(); i++) {

			if (agents.get(i).isAlive()) {
				if (agents.get(i).isAware()) {
					double[] s = getAgentStatus(i);
					for (int f = 0; f < agents.get(i).getDecisions().length; f++) {
						s[f+s.length-agents.get(i).getDecisions().length] = agents.get((i+1)%agents.size()).getDecisions()[f];
					}
					agents.get(i).move(s, grid);
					// TODO this will only work properly with two agents for now
				}
				agents.get(i).move(getAgentStatus(i), grid);
				landscapes.add(i, agents.get(i).getActivationLandscape());
			}
		}		
	}
	
	/**
	 * Returns the number of agents in the environment.
	 * 
	 * @return The number of agents in the environment.
	 */
	public int getNumAgents() {
		return agents.size();
	}
	
	/**
	 * Returns whether a specified agent in the environment is alive or not.
	 * 
	 * @param i The agent to check.
	 * @return true if the agent is alive, false otherwise.
	 */
	public boolean agentAliveStatus(int i) {
		return agents.get(i).isAlive();
	}
	
	/**
	 * Returns the number of moves a specified agent has taken in the environment.
	 * 
	 * @param i The agent to check.
	 * @return The number of moves the agent has taken.
	 */
	public int getAgentMoves(int i) {
		return agents.get(i).getMoves();
	}
	
	/**
	 * Returns the location of a specified agent in the environment.
	 * 
	 * @param i The agent to check.
	 * @return The Cell that the agent resides in in the environment.
	 */
	public Cell getAgentLocation(int i) {
		return agents.get(i).getLocation();
	}
	
	/**
	 * Returns the fitness of a specified agent in the environment.
	 * 
	 * @param i The agent to check.
	 * @return The calculated fitness of the specified agent.
	 */
	public double evaluateAgent(int i) {
		return agents.get(i).evaluate();
	}
	
	/**
	 * Returns the activity landscape for a specified agent. Used for {@link engine.ActivityLandscapeView}.
	 * 
	 * @param i The agent in which the landscape should be returned.
	 * @return The landscape of the specified agent.
	 */
	public Double[][] getLandscape(int i) {
		return landscapes.get(i);
	}
	
	/**
	 * Reset the environment to a clean state. Reset all objects and remove all agents from the environment.
	 */
	public void resetAll() {
		setup();
		landscapes = new ArrayList<Double[][]>();
		agents = new ArrayList<Agent>();
	}
	
	/**
	 * Reset the environment to a clean state. Reset all objects, and reset the agents that are within the environment.
	 */
	public void reset() {
		setup();
		landscapes = new ArrayList<Double[][]>();
		for (int i = 0; i < agents.size(); i++) {
			agents.get(i).reset();
		}
	}
	
	/**
	 * Returns the status of a specified agent in the environment as an array, based on where it currently is in the environment etc.
	 * 
	 * @param i The agent to check.
	 * @return An array of the agent status: [grass, resource, stone, water, isCarrying, partialBridgeExists], where values will be 1 if true and 0 otherwise.
	 */
	private double[] getAgentStatus(int i) {
		double[] result = new double[agents.get(i).getStatusSize()];
		
		int locX = agents.get(i).getLocation().getLocation().toArray()[0];
		int locY = agents.get(i).getLocation().getLocation().toArray()[1];
		// if empty on grass
		result[0] = (grid[locX][locY].getObject() == null) ? 1 : 0;
		// if on a Resource		
		result[1] = (grid[locX][locY].getObject() instanceof Resource) ? 1 : 0;
		// if on a Stone
		result[2] = (grid[locX][locY].getObject() instanceof Stone) ? 1 : 0;
		// if on Water
		result[3] = (grid[locX][locY].getObject() instanceof Water) ? 1 : 0;
		// if carrying	
		result[4] = (agents.get(i).isCarrying()) ? 1 : 0;
		// if bridge partially built in environment
		result[5] = (agents.get(i).getPartialBridgeExists()) ? 1 : 0;
		
		return result;
	}

	/**
	 * Initialise the environment and setup with a standard arrangement of objects: stones, a river and resources.
	 */
	private void setup() {
		grid = new Cell[rows][cols];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				grid[i][j] = new Cell(new Location(i, j));
			}
		}
		resources = new ArrayList<Resource>();
		// TODO 5/13 RESOURCES WRONG WAY ROUND // fixed, but need to address this
		// set resources
		grid[2][16].addObject(new Resource(grid[2][16])); // a
		grid[16][2].addObject(new Resource(grid[16][2])); // b
		grid[13][5].addObject(new Resource(grid[13][5])); // a
		grid[5][13].addObject(new Resource(grid[5][13])); // b
		resources.add((Resource)grid[2][16].getObject());
		resources.add((Resource)grid[16][2].getObject());
		resources.add((Resource)grid[13][5].getObject());
		resources.add((Resource)grid[5][13].getObject());
		// set stones
		grid[1][12].addObject(new Stone(grid[1][12])); 		// b
		grid[17][6].addObject(new Stone(grid[17][6])); 		// a
		grid[3][4].addObject(new Stone(grid[3][4]));		// a
		grid[15][14].addObject(new Stone(grid[15][14]));	// b
		grid[6][7].addObject(new Stone(grid[6][7]));		// a
		grid[12][11].addObject(new Stone(grid[12][11]));	// b
		grid[10][17].addObject(new Stone(grid[10][17]));	// b
		grid[9][1].addObject(new Stone(grid[9][1]));		// a
		// set river
		for(int i = 0; i < rows; i++) {
			grid[i][9].addObject(new Water(grid[i][9], 2));
		}
	}
}