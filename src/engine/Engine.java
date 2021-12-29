package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import deliberativeLayer.DecisionNetwork;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import physicalLayer.ActionType;
import physicalLayer.Agent;
import physicalLayer.SocialActionAgent;
import physicalLayer.PhysicalLayer;

/**
 * <p>A class to initialise a 2D grid-world River Crossing Dilemma environment and the agent population(s) that inhabit it.</p>
 * 
 * <p>An RCD instance is made of grass cells, with a column of Water objects in the centre, which creates an impassable river.
 * Each agent in the environment is allocated two Resources to collect in order to achieve its goal (one on either side of the river).
 * A bridge can be built by placing Stones into the river. The number of Stones needed to create a bridge successfully is equal to 
 * the depth of the river.</p>
 * 
 * <p>Currently the agents evolve with a Steady State Genetic Algorithm in order to learn how to achieve their goals. A goal-rational 
 * agent can be created, or one that uses both goal-rational and traditional action, or goal-rational and random action. Agents can 
 * not yet employ more than two types of social action.</p>
 * 
 * <p>Visualisations of the state of the environment and the decision-making processes of the agents can be 
 * turned on with the {@link Engine#TURN_ON_VISUALS} field, and a graph of the performance with the 
 * {@link Engine#TURN_ON_GRAPH} field.</p>
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Engine {
	/** A model of the environment in which the agent(s) resides. */
	protected PhysicalLayer 	model;
	/** An array of the best agent in each population, updated at each generation. */
	protected Agent[] 			currentBest;
	/** An array of the fitnesses of the whole population, for each population of agents in the environment. */
	protected double[][] 		fitnesses;
	/** The directory name to write the output files in. */
	protected String			directoryName;
	/** A boolean to track whether the run has finished or not (false if all agents have reached their goal, or the max steps has been reached). */
	protected boolean 			running			= true;
	/** The time the run was started. */
	protected long 				startTime 		= 0;
	/** The time the generation was started. */
	protected long 				generationTime 	= 0;
	/** The elapsed time of the run. */
	protected long 				elapsedTime 	= 0;
	/** The total time for the run. */
	protected long 				totalTime 		= 0;
	/** The overall time for the run. */
	protected long 				overallTime 	= 0;
	/** A random variable used to decide the choice of social action. */
	protected Random 			goalRandom 		= new Random();
	/** A random variable used throughout the program, seeded either by argument or random. */
	public    static Random 	random 			= new Random();
	/** The seed used for the random generator. */
	public    static int 		SEED;
	/** TODO */
	public 	  static boolean	useNeuromodulation;
	private	  static int		numEnvironments;
	private	  static boolean	useConsistentPartner;
	private   static boolean[]  aware;
	/** Filenames for the generational stats for each Agent population. */
	protected static String[]	GEN_STATS_FILES;
	/** The number of generations that the Agent populations will run for. */
	protected static	   int			NUM_GENERATIONS 	= 500; 
	/** The number of Agent populations that exist in the environment (usually 1 or 2 - one for each side of the river). */
	protected static       int	  		POPULATION_NUMBER	= 1; 
	/** The maximum number of time-steps that an Agent has to achieve its goals in the environment. */
	protected static final int 	  		TIME_STEPS 		  	= 500;	
	/** The number of Agents that will be selected in a tournament during the evolutionary process. */
	protected static final int 	  		TOURNAMENT_SIZE 	= 3;
	/** The number of Agents that exist in a population. */
	protected static final int	  		POPULATION_SIZE 	= 25;
	/** The number of rows in the 2D environment. */
	protected static final int 	  		ROWS 				= 19;
	/** The number of columns in the 2D environment. */
	protected static final int 	  		COLS 				= 19;
	/** A container of each of the Agent populations, with a size of POPULATION_NUMBER, and each inner container with a size of POPULATION_SIZE. */
	protected ArrayList<ArrayList<Agent>> agents;
	
	
	/**
	 * Creates a new Engine that will generate a River Crossing Dilemma instance and run the evolutionary process for
	 * the specified number of generations, and repeat for a specified number of iterations.
	 * 
	 * An environment can be created with 1:* agents in it, either from a random seed or a specified seed, that will
	 * evolve in order to achieve their goals.
	 * The agent(s) in the environment use either goal-rational action or a specified social action type as well as
	 * goal-rationality.
	 * 
	 * @param numAgentPopulations The number of Agent populations to exist in the environment (usually 1 or 2).
	 * @param fromRandom true if the agent(s) should be initialised with random weights, false if seeds are specified.
	 * @param seeds An array of seeds that the agents should be initialised from - if the first element contains a non-zero
	 * integer element. If the first element is 0, agents will be randomly initialised. A mixture of non-zero and zero elements
	 * can be specified; elements that equal 0 will randomly initialise an agent, whereas non-zero ones will be initialised with
	 * the element as the seed (as long as the first element is non-zero).
	 * @param goalRationality The percentage of Agent actions that will be goal-rational (1.0 for fully goal-rational). If &lt; 1, 
	 * there will be a percentage chance for actions to be of the specified {@link ActionType}.
	 * @param gens The number of generations that the Agent(s) will evolve for.
	 * @param iters The number of iterations that the evolutionary process will be run for, default is 1 (&gt; 1 is useful for graphing
	 * average performance across a number of iterations of evolution).
	 * @param at The action type that the Agent(s) will employ, default {@link ActionType#GOAL_RATIONAL}. If not GOAL_RATIONAL, then
	 * there will be a percentage chance (specified with the goalRationality param) for the specified action type to occur, with
	 * goal-rational action occuring the remainder of the time.
	 * TODO
	 */
	public Engine(int numAgentPopulations, boolean fromRandom, int gens, boolean nm, 
			int numEnvs, boolean startFromParsedWeights, boolean consPartner, boolean aware1, boolean aware2, int[] seeds) {
		POPULATION_NUMBER = numAgentPopulations;
		currentBest = new Agent[POPULATION_NUMBER];
		agents = new ArrayList<ArrayList<Agent>>();
		fitnesses = new double[POPULATION_NUMBER][POPULATION_SIZE];
		NUM_GENERATIONS = gens;
		useNeuromodulation = nm;
		numEnvironments = numEnvs;
		useConsistentPartner = consPartner;
		aware = new boolean[2];
		aware[0] = aware1;
		aware[1] = aware2;
		System.out.println("Parameters:\nUse NM?: " + useNeuromodulation + ", " + seeds[0] + ", number of environments evaluated on: " + numEnvironments + 
				", start from parsed weights: " + startFromParsedWeights + ", consistent partner/s: " + useConsistentPartner + ", aware agents: " + aware1 + " " + aware2);
		
		if (fromRandom) {
			newSeed();
			seeds[0] = SEED; // TODO does this only work for one agent?
		}
		// keep all files for this run in one directory
		directoryName = seeds[0] + "";
				
			System.out.println("Starting Evolution");
			setupFilenames();
			
			try {
				File file = new File(directoryName);
			    file.mkdir();
			    file.createNewFile();
			}
			catch(Exception e) {
			    System.out.println("cannot create directory exception");
			}

			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				//boolean aware = (numAware == 0 || (numAware == 1 && popNum == 1)) ? false : true;
				String prefix = "Agent " + popNum + ": " + seeds[popNum] + ", aware: " + aware[popNum] + "\n" + printFilePrefix() + "gen,";
				for (int i = 1; i <= numEnvironments; i++) {
					prefix += "fitness"+i+",movesMade"+i+",isAlive"+i+",hasCarried"+i+",hasMadeBridge"+i+",numStones"+i+",targetsFound"+i+",successful"+i; 
					if (i == numEnvironments && numEnvironments != 1) {
						prefix += ",totalFitness";
					}
					if (i != numEnvironments) {
						prefix += ",";
					}
					else if (i == numEnvironments) {
						prefix += "\n";
					}		
				}
				writeToFile(GEN_STATS_FILES[popNum], prefix);
			}
			
			evolve(fromRandom, seeds, startFromParsedWeights, useConsistentPartner);
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				writeToFile(GEN_STATS_FILES[popNum], printFileSuffix());
			}
		
	}
	
	/**
	 * 
	 * @param args The program requires the following arguments:
	 * <ol>
	 * 	<li>int numAgentPopulations - the number of agents present in an environment (i.e. 1 for alone, 2 for together).</li>
	 * 	<li>boolean fromRandom - true if the agent(s) should be randomly initialised, false if the agent(s) should be initialised
	 * 		from the given seeds.</li>
	 * 	<li>double goalRationality - percentage of actions that are goal-rational (i.e. 1.0 for fully goal-rational).</li>
	 * 	<li>int generations - the number of generations the agent(s) should evolve for.</li>
	 * 	<li>int iterations - the number of times the evolutionary process will be repeated. Usually set as 1; 
	 * 		only used when visuals are turned on to see averages graphed in live-time over a number of iterations.</li>
	 * 	<li>ActionType type - the type of action the agent(s) express: GOAL-RATIONAL, TRADITIONAL (and GR), RANDOM (and GR).</li>
	 * 	<li>int[] seeds - the remaining arguments are parsed as seeds. If the first element of the array is 0 then the agent(s)
	 * 		will be randomly initialised, regardless of whether the array contains other non-zero elements. If the first element
	 * 		of the array is non-zero (negative values valid), then the agent will be initialised with that seed; subsequent agents
	 * 		will be initialised with a seed if the array element is non-zero, or randomly initialised if 0.</li>
	 * </ol>
	 * TODO
	 */
	public static void main(String[] args) throws RuntimeException {
		int numArgs = 9;
		int[] seeds = new int[args.length-numArgs];
		for (int i = 0; i < seeds.length; i++) {
			seeds[i] = Integer.valueOf(args[i+numArgs]);
			System.out.println(seeds[i]);
		}
		if (!Boolean.valueOf(args[6]) && Integer.valueOf(args[0]) == 2) {
			throw new RuntimeException("Cannot have 2 agent populations as well as using a non-consistent partner. Change agent populations to 1 to have a non-consistent partner (Random Together Experiments),"
					+ " or change to be a consistent partner (Together experiments).");
		}
		new Engine(Integer.valueOf(args[0]), Boolean.valueOf(args[1]), Integer.valueOf(args[2]), Boolean.valueOf(args[3]),
			Integer.valueOf(args[4]), Boolean.valueOf(args[5]),	Boolean.valueOf(args[6]), Boolean.valueOf(args[7]), Boolean.valueOf(args[8]), seeds);
			//Integer.valueOf(args[7]), Boolean.valueOf(args[8]),	Boolean.valueOf(args[9]), Boolean.valueOf(args[9]), seeds);
			// protRiv should be 10 instead of 9... (wouldn't make a difference as only run for CE SS
		
	}	
	
	/**
	 * <p>Evolves agents with a Steady State Genetic Algorithm in order for them to learn how to achieve their goals in the
	 * environment. Progress of the evolutionary process is output to files, using the seed of the first agent in the 
	 * environment as the directory name.</p>
	 * <p>Agents can be initialised with random weights, from a mixture of specified seeds and random seeds, or the evolution
	 * can continue from a previous state of the population(s) without re-initialisation of the weights (when evolving agents
	 * alone, and then evolving them together for example).</p>
	 * 
	 * @param randomise true if the agents should be initialised from random weights, false if they should be initialised 
	 * from specified weights, or if the evolution should carry on from a previous state of the agent population(s), in 
	 * which case no new initialisation takes place.
	 * @param seeds The seeds in which the agent population(s) are to be initialised from. If the first element is 0, 
	 * and randomise is false, the evolution will carry on from a previous state of the agent population(s). 
	 * If randomise is true, the seeds array is ignored and the agent population(s) is randomly initialised. 
	 * If the first element is non-zero, the element is used as a seed to initialise the agent populations; any 
	 * subsequent elements that are 0 will mean that the population with that index is randomly initialised. If both 
	 * TODO
	 */
	protected void evolve(boolean randomise, int[] seeds, boolean startFromParsedWeights, boolean consistentPartner) throws IndexOutOfBoundsException {
		if (!randomise && seeds[0] == 0 && agents.isEmpty()) {
			throw new IndexOutOfBoundsException("Cannot evolve as no seeds are specified, randomise is false, and no "
					+ "current population exists to continue from.");
		}
		
		String[] outputs = new String[POPULATION_NUMBER];
		Arrays.fill(outputs, "");
		model = new PhysicalLayer(ROWS, COLS);

		if (randomise) {
			// go by population index, across each population
			for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
				Agent[] agentsToLoop = new Agent[POPULATION_NUMBER];
				
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					if (popNum != 0) {
						newSeed(); 
					}
					seeds[popNum] = SEED;
					System.out.println(SEED);
					agents.add(new ArrayList<Agent>());
					//boolean aware = (numAware == 0 || (numAware == 1 && popNum == 1)) ? false : true; // TODO change if more than 2 agents
					agents.get(popNum).add(new SocialActionAgent(null, ROWS, COLS, null, aware[popNum]));
					agentsToLoop[popNum] = agents.get(popNum).get(popIndex);
				}
				// once initialised, run an instance so the agents get a fitness
				loop(agentsToLoop, false, 0);
				for (int f = 0; f < POPULATION_NUMBER; f++) {				
					fitnesses[f][popIndex] = agents.get(f).get(popIndex).getFitness(); // was .evaluate()
				}
			}
		}
		else if (seeds[0] != 0) { // start from specified seeds
			currentBest = new Agent[POPULATION_NUMBER];
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				agents.add(new ArrayList<Agent>());
				if (startFromParsedWeights && seeds[popNum] != 0 && popNum == 0) {
					String filename = "../alone";
					filename = (useNeuromodulation) ? filename + "-nm/" : filename + "/";
					filename += seeds[popNum] + "/fitnesses-agent0-iter0.csv";
					
					WeightsParser wp = new WeightsParser();
					ArrayList<DecisionNetwork> parsedAgents = wp.toArray(filename);
					
					for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
						//boolean aware = (numAware == 0 || (numAware == 1 && popIndex == 1)) ? false : true; // TODO change if more than 2 agents
						agents.get(popNum).add(new SocialActionAgent(ROWS, COLS, parsedAgents.get(popIndex).getGenes(), parsedAgents.get(popIndex).getNeurons(), aware[popNum]));
					}
					System.out.println("If using a partner, please note the partner will not be initialised from parsed weights and instead is randomly initialised.");
					// To change this, take out the && popNum==0 condition! As currently for the second seed, it will go to the else clause below and randomly initialise
				}
				else {
					if (seeds[popNum] == 0) {
						newSeed();
						seeds[popNum] = SEED;
					}
					else {
						SEED = seeds[popNum];
						random.setSeed(SEED);
					}
					for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
						//boolean aware = (numAware == 0 || (numAware == 1 && popNum == 1)) ? false : true;
						agents.get(popNum).add(new SocialActionAgent(null, ROWS, COLS, null, aware[popNum]));
					}
				}
			}
			goalRandom.setSeed(SEED);
			// evaluate starting fitnesses			
			for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
				Agent[] agentsToLoop = new Agent[POPULATION_NUMBER];
				
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					agentsToLoop[popNum] = agents.get(popNum).get(popIndex);
				}
				loop(agentsToLoop, false, 0);
				for (int f = 0; f < POPULATION_NUMBER; f++) {				
					fitnesses[f][popIndex] = agents.get(f).get(popIndex).getFitness(); // was .evaluate
				}
			}
		}
		// start evolution
		for (int gen = 1; gen < NUM_GENERATIONS; gen++) {
			if ((gen+1) % (5000) == 0) {
				System.out.println((gen+1) + " GENS");
			}
			Agent[][] tournament = new Agent[POPULATION_NUMBER][TOURNAMENT_SIZE];
			Double[][] tournamentFitness = new Double[POPULATION_NUMBER][TOURNAMENT_SIZE];
			int[][] indexes = new int[POPULATION_NUMBER][TOURNAMENT_SIZE];
			// set up the tournament
			 for (int t = 0; t < TOURNAMENT_SIZE; t++) {
				Agent[] tnmt = new Agent[POPULATION_NUMBER];
				// tournament selection
				for (int pop = 0; pop < POPULATION_NUMBER; pop++) {
					indexes[pop][t] = random.nextInt(POPULATION_SIZE-t);
					tournament[pop][t] = agents.get(pop).get(indexes[pop][t]);
					tnmt[pop] = tournament[pop][t];
				}
				
				loop(tnmt, false, gen);
				
				for (int pop = 0; pop < POPULATION_NUMBER; pop++) {
					tournamentFitness[pop][t] = tournament[pop][t].getFitness(); // was .evaluate
					fitnesses[pop][indexes[pop][t]] = tournamentFitness[pop][t];
				}
			}

			int[] worstIndex = new int[POPULATION_NUMBER];
			for (int p = 0; p < POPULATION_NUMBER; p++) {
				currentBest[p] = findBestParent(tournament[p]);
				if (numEnvironments > 1) {
					outputs[p] += gen + "," + currentBest[p].getEvalStatus() + currentBest[p].getFitness() + "\n";
				}
				else {
					outputs[p] += gen + "," + currentBest[p].getStringStatus() + "\n";
				}
				worstIndex[p] = findWorstParent(tournament[p]); // worst index in the tournament;
			}
			
			ArrayList<ArrayList<Integer>> parentIndexes = new ArrayList<ArrayList<Integer>>();	
			for (int i = 0; i < POPULATION_NUMBER; i++) {
				parentIndexes.add(new ArrayList<Integer>());
			}
			for (int t = 0; t < TOURNAMENT_SIZE; t++) {
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					
					agents.get(popNum).set(indexes[popNum][t], tournament[popNum][t]);
					agents.get(popNum).get(indexes[popNum][t]).reset();
					// the worst will be replaced, so add the best ones to the parent index array for breeding
					if (t != worstIndex[popNum]) {
						parentIndexes.get(popNum).add(t);
					}
				}
			}
			
		    for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
		    	agents.get(popNum).set(indexes[popNum][worstIndex[popNum]],
		    			agents.get(popNum).get(indexes[popNum][parentIndexes.get(popNum).get(0)]).
		    				produceOffspring(agents.get(popNum).get(indexes[popNum][parentIndexes.get(popNum).get(1)])));
		    }
			
			
			// loop through newly-made offspring
			Agent[] agentsToLoop = new Agent[POPULATION_NUMBER];
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				agentsToLoop[popNum] = agents.get(popNum).get(indexes[popNum][worstIndex[popNum]]);
			}
			loop(agentsToLoop, false, gen);
			for (int f = 0; f < POPULATION_NUMBER; f++) {				
				fitnesses[f][indexes[f][worstIndex[f]]] = agents.get(f).get(indexes[f][worstIndex[f]]).getFitness(); // was .evaluate(); 
			}
			
		    // no need to add a new population - the current one is changed incrementally
			if ((gen+1) % 125 == 0) {
		    	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
			        outputs[popNum] = "";
		    	}
		    }
		} // evolution finished
		
	   	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
	        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
	        outputs[popNum] = "";
    	}
		// print population weights to fitness files
		for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			outputs[popNum] += "//\nWeights" + popNum + "------------------------\n";
			outputs[popNum] += currentBest[popNum].toString() + "\n";
			outputs[popNum] += "\nEnd of Weights" + popNum + "------------------------\n//\nSeed: " + seeds[popNum] + "\n";
		}
		
    	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
	        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
	        outputs[popNum] = "";
    	}
	}
	
	/**
	 * Returns the best fitness in a specified agent population.
	 * 
	 * @param popNum The index of the agent population to check for the best fitness.
	 * @return The fitness of the highest-performing agent in the specified population.
	 */
	protected double getBestFitness(int popNum) {
		double result = agents.get(popNum).get(0).getFitness();
		for (int i = 1; i < POPULATION_SIZE; i++) {
			if (agents.get(popNum).get(i).getFitness() > result) {
				result = agents.get(popNum).get(i).getFitness();
			}
		}
		return result;
	}
	
	/**
	 * Returns the worst fitness in a specified agent population.
	 * 
	 * @param popNum The index of the agent population to check for the worst fitness.
	 * @return The fitness of the worst-performing agent in the specified population.
	 */
	protected double getWorstFitness(int popNum) {
		double result = agents.get(popNum).get(0).getFitness();
		for (int i = 1; i < POPULATION_SIZE; i++) {
			if (agents.get(popNum).get(i).getFitness() < result) {
				result = agents.get(popNum).get(i).getFitness();
			}
		}
		return result;
	}
	
	/**
	 * Calculates and returns the average fitness across a specified agent population.
	 * 
	 * @param popNum The index of the agent population to calculate the average fitness of.
	 * @return The calculated average fitness of the specified population.
	 */
	protected double getAverageFitness(int popNum) {
		double output = 0.0;
		
		for (int i = 0; i < fitnesses[popNum].length; i++) {
			output += fitnesses[popNum][i];
		}
		return output/fitnesses[popNum].length;
	}
	
	/**
	 * Calculates and returns the standard deviation of fitness across a specified agent population.
	 * 
	 * @param popNum The index of the agent population to calculate the standard deviation of.
	 * @param avg The average fitness of the specified population.
	 * @return The calculated standard deviation of the specified population, given the average.
	 */
	protected double getStandardDeviationFitness(int popNum, double avg) {
		double std = 0.0;
		for (int i = 0; i < fitnesses[popNum].length; i++) {
			std += (fitnesses[popNum][i] - avg) * (fitnesses[popNum][i] - avg);
		}
		std /= fitnesses[popNum].length;
		return Math.sqrt(std);
	}
	
	/**
	 * Retrieves the highest-performing agent in a given tournament.
	 * 
	 * @param tnmnt The tournament in which to find the highest-performing agent.
	 * @return The highest-performing agent in the given tournament.
	 */
	protected Agent findBestParent(Agent[] tnmnt) {
		Agent best = tnmnt[0];
		for (int i = 1; i < tnmnt.length; i++) {
			if (tnmnt[i].getFitness() > best.getFitness()) {
				best = tnmnt[i];
			}
		}
		return best;
	}
	
	/**
	 * Retrieves the worst-performing agent in a given tournament.
	 * 
	 * @param tnmnt The tournament in which to find the worst-performing agent.
	 * @return The index in the tournament of the worst-performing agent.
	 */
	protected int findWorstParent(Agent[] tnmnt) {
		int index = 0;
		for (int i = 1; i < tnmnt.length; i++) {
			if (tnmnt[i].getFitness() < tnmnt[index].getFitness()) {
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Generates a new random seed, and sets the seed of the random variables.
	 */
	protected void newSeed() {
		SEED = random.nextInt();
		random.setSeed(SEED);
		goalRandom.setSeed(SEED);
	}
	

	/**
	 * Sets up an environment instance with the specified agent(s), which have a maximum number of
	 * time-steps to achieve their goals. The run will stop when all agents in the environment
	 * have achieved their goals, or if the maximum number of time-steps has been reached.
	 * Note: this currently only works with one or two agents.
	 * 
	 * @param a An array of agents to be placed into the environment instance. This is not a whole
	 * population of agents, just those individuals who will compete in this run. The index of the
	 * element in the array correlates to the population number of the {@link agents} container.
	 * @param showViews true if the visualisations of the environment and the activity landscape
	 * (decision-making processes of the agent(s)) should be displayed, false otherwise.
	 * TODO
	 */
	protected void loop(Agent[] a, boolean showViews, int gen) {
		if (numEnvironments == 1) {
			model.resetAll();
			running = true;
			int count = 0;
			
			if (!useConsistentPartner) { // if non-consistent partner then add
				a = Arrays.copyOf(a, 2);
				//boolean aware = (numAware == 0 || numAware == 1) ? false : true;
				a[1] = new SocialActionAgent(null, ROWS, COLS, new Random(gen), aware[1]);
			}
			
			for (int i = 0; i < a.length; i++) {
				a[i].reset();
				model.addAgent(a[i]);
			}
			// TODO change so any number of agents can have a start cell
			model.getAgents().get(0).setStartCell(model.getCell(0, 0));
			if (model.getAgents().size() == 2) {
				model.getAgents().get(1).setStartCell(model.getCell(ROWS-1, COLS-1));
				//model.getAgents().get(1).setStartCell(model.getCell(ROWS-1, 0));
			}
			
			while (count < TIME_STEPS && running) {
				update(showViews);
				count++;
				if (showViews) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// isFinished is assigned FALSE if one+ agents have not achieved the goal
				boolean isFinished = true;
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					if (!model.getAgents().get(popNum).hasAchievedGoal()) {
						isFinished = false;
					}
				}
				if (isFinished) running = false;
			}
			for (int i = 0; i < POPULATION_NUMBER; i++) {
				model.getAgents().get(i).evaluate();
			}
		}
		else {
			loopManyEnvironments(a, showViews, gen);
		}		
	}
	
	/**
	 * Sets up an environment instance with the specified agent(s), which have a maximum number of
	 * time-steps to achieve their goals. The run will stop when all agents in the environment
	 * have achieved their goals, or if the maximum number of time-steps has been reached.
	 * Note: this currently only works with one or two agents.
	 * 
	 * @param a An array of agents to be placed into the environment instance. This is not a whole
	 * population of agents, just those individuals who will compete in this run. The index of the
	 * element in the array correlates to the population number of the {@link agents} container.
	 * @param showViews true if the visualisations of the environment and the activity landscape
	 * (decision-making processes of the agent(s)) should be displayed, false otherwise.
	 * @param gen TODO
	 */
	protected void loopManyEnvironments(Agent[] a, boolean showViews, int gen) {
		Agent[] aCopy = Arrays.copyOf(a, a.length);
		// TODO this is hardcoded for now - clean up later
		for (int i = 0; i < a.length; i++) {
			a[i].resetCumulativeFitness();
		}
		for (int eval = 0; eval < numEnvironments; eval++) {
			a = Arrays.copyOf(aCopy, aCopy.length);
			model.resetAll();
			running = true;
			int count = 0;
			if ((eval%2) == 1) { // if odd then add a pair
				if (!useConsistentPartner) {
					a = Arrays.copyOf(a, 2);
					// this works with 2, 3 and 4 environments
					//boolean aware = (numAware == 0 || numAware == 1) ? false : true;
					a[1] = (eval == 1) ? new SocialActionAgent(null, ROWS, COLS, new Random(gen), aware[1]) : new SocialActionAgent(null, ROWS, COLS, new Random(gen+NUM_GENERATIONS), aware[1]);
				}
				else { // use consistent partner, but if eval4 choose a different partner for evals 2 and 4
					a = Arrays.copyOf(a, 2);
					a[1] = (eval == 1) ? aCopy[1] : aCopy[2];
				}
				// else it just uses its pair already
				// TODO Change so that if eval4 needs to have to different consistent partners, i.e. agent x paired with Y and Z
			}
			else if ((eval%2) == 0) {
				a = Arrays.copyOf(a, 1);
			}
			
			for (int i = 0; i < a.length; i++) {
				a[i].reset();
				model.addAgent(a[i]);
			}
			// TODO change so any number of agents can have a start cell
			model.getAgents().get(0).setStartCell(model.getCell(0, 0));
			if (model.getAgents().size() == 2) {
				model.getAgents().get(1).setStartCell(model.getCell(ROWS-1, COLS-1));
			}
			
			while (count < TIME_STEPS && running) {
				update(showViews);
				count++;
				if (showViews) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// isFinished is assigned FALSE if one+ agents have not achieved the goal TODO
				boolean isFinished = true;
				for (int popNum = 0; popNum < a.length; popNum++) {
					if (!model.getAgents().get(popNum).hasAchievedGoal()) {
						isFinished = false;
					}
				}
				if (isFinished) running = false;
			}
			
			model.getAgents().get(0).incrementCumulativeFitness(model.getAgents().get(0).evaluate());
			model.getAgents().get(0).addToStatus(model.getAgents().get(0).getStringStatus());
			if (model.getAgents().size() > 1 && POPULATION_NUMBER > 1) {
				model.getAgents().get(1).incrementCumulativeFitness(model.getAgents().get(1).evaluate());
				model.getAgents().get(1).addToStatus(model.getAgents().get(1).getStringStatus());
			}
		}
		
		a[0].setTotalFitness();
		if (model.getAgents().size() > 1) {
			a[1].setTotalFitness();
		}
	}
	
	/**
	 * Updates the model of the environment for one time-step, as well as any views that visualise 
	 * the model. The run will stop if all agents in the environment are no longer alive.
	 * 
	 * @param showViews true if the visualisations of the environment and the activity landscape 
	 * (decision-making processes of the agent(s)) should be displayed, false otherwise.
	 */
	protected void update(boolean showViews) 
	{
		model.update();
		boolean atLeastOneAgentAlive = false;
		for (int i = 0; i < model.getNumAgents(); i++) {
			if (model.agentAliveStatus(i)) {
				atLeastOneAgentAlive = model.agentAliveStatus(i);
			}
		}
		
		if (!atLeastOneAgentAlive) {
			running = false;
		}
	}
	
	/**
	 * Generates a comma-separated String of all of the fitness in a specified population.
	 * 
	 * @param popNum The index of the population to generate a fitness String for.
	 * @return A comma-separated String of all of the fitnesses in a specified population.
	 */
	protected String fitnessArrayToString(int popNum) {
		String output = "";
		
		for (int i = 0; i < fitnesses[popNum].length; i++) {
			output += fitnesses[popNum][i];
			if (i < fitnesses[popNum].length-1) {
				output += ",";
			}
		}
		return output;
	}
	
	/**
	 * Generates a String containing the data for the run, including the seed, iteration and time.
	 * Used as a file header.
	 * 
	 * @return A String containing data for the run.
	 */
	protected String printFilePrefix() {
		String output = "";
		startTime = System.nanoTime();
		generationTime = startTime;
		
		output = "Created: " + LocalDateTime.now() + "; Seed: " + SEED + "\n";
		//generationFitness = new RealTimeGraph("Agent Performance, Run " + i);
		return output;
	}
	
	/**
	 * Generates a String containing the data for the run, including time taken and iteration.
	 * Used as a file footer.
	 * 
	 * @return A String containing data for the run.
	 */
	protected String printFileSuffix() {
		String output = "";
		elapsedTime = System.nanoTime() - generationTime;
		output += "Elapsed time: " + elapsedTime/1000000000.0 + " seconds";
		output += "//\nEnd of Run ------------------------\n//\n";
		
		generationTime = System.nanoTime();
		
		totalTime = System.nanoTime() - startTime;
		overallTime += totalTime;
		output += "//\nTotal Elapsed time: " + totalTime/1000000000.0 + " seconds\nEnd\n//";
		
		return output;
	}
	
	/**
	 * Writes the given content to a given filename.
	 * 
	 * @param filename The name of the file to write to.
	 * @param content The content to write to the specified file.
	 */
	protected void writeToFile(String filename, String content) {
		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(filename, true);
			bw = new BufferedWriter(fw);
			bw.write(content);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally
		{ 
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}
	} 
	
	/**
	 * Sets up the String representation of the CSV filenames that are used to contain the output data 
	 * for the runs. Filenames begin with the {@link directoryName} specified, which is usually the
	 * first seed that is used in the run.
	 */
	protected void setupFilenames() {
		GEN_STATS_FILES = new String[POPULATION_NUMBER];
		
		for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			GEN_STATS_FILES[popNum] = directoryName + "/generationStats-agent"  + popNum + ".csv";
		}
	}
}