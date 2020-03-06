package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
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
	/** An array of windows - one for each agent performing in the environment - showing the decision-making process of the agent. */
	protected View[]			activityViews;
	/** A visualisation of the environment and the agent(s) in it. */
	protected View 				environmentView;
	/** A graph visualising the best performance at each generation for the agent(s) in the environment. */
	protected RealTimeGraph 	bestFitnessGraph;
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
	/** A counter to track the current iteration (how many times the run is repeated). */
	protected Integer 			iteration 		= 0;	
	/** A random variable used to decide the choice of social action. */
	protected Random 			goalRandom 		= new Random();
	/** A random variable used throughout the program, seeded either by argument or random. */
	public    static Random 	random 			= new Random();
	/** The seed used for the random generator. */
	public    static int 		SEED;
	/** TODO */
	public 	  static boolean	useNeuromodulation;
	/** Filenames for the generational stats for each Agent population. */
	protected static String[]	GEN_STATS_FILES;
	/** Filenames for the generation fitnesses for each Agent population. */
	protected static String[]	FITNESS_FILES;
	///** Filenames for the generation averages and other stats for each Agent population. */
	//protected static String[]	AVERAGE_FILES;
	/** The number of generations that the Agent populations will run for. */
	protected static	   int			NUM_GENERATIONS 	= 500; 
	/** The number of iterations the evolutionary process will be run for. Default is 1 (only &gt; 1 if graph is to show averages for many runs). */
	protected static 	   int	  		ITERATIONS			= 1;
	/** The number of Agent populations that exist in the environment (usually 1 or 2 - one for each side of the river). */
	protected static       int	  		POPULATION_NUMBER	= 1; 
	/** The percentage of Agent actions that are goal-rational (i.e. 1.0 is fully goal-rational). */
	protected static 	   double		GOAL_RATIONALITY  	= 1.0;
	/** The type of social action that an Agent will express (i.e. TRADITIONAL will be a combination of goal-rationality and traditional action). */
	protected static	   ActionType	ACTION_TYPE		  	= ActionType.GOAL_RATIONAL;
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
	/** true if visualisations of the environment and decision-making processes should be shown, false otherwise. */
	protected static final boolean  	TURN_ON_VISUALS   	= false;
	/** true if graphs of performance should be shown, false otherwise. */
	protected static final boolean  	TURN_ON_GRAPH	  	= false;
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
	public Engine(int numAgentPopulations, boolean fromRandom, double goalRationality, int gens, int iters, ActionType at, boolean nm, int[] seeds) {
		POPULATION_NUMBER = numAgentPopulations;
		currentBest = new Agent[POPULATION_NUMBER];
		agents = new ArrayList<ArrayList<Agent>>();
		fitnesses = new double[POPULATION_NUMBER][POPULATION_SIZE];
		ACTION_TYPE = at;
		GOAL_RATIONALITY = goalRationality;
		NUM_GENERATIONS = gens;
		ITERATIONS = iters;
		useNeuromodulation = nm;
		System.out.println("Parameters:\nGoal-Rationality: " + GOAL_RATIONALITY + ", Action Type: " + ACTION_TYPE.toString() + 
				", use NM?: " + useNeuromodulation + ", " + seeds[0]);
		
		if (TURN_ON_GRAPH) {
			bestFitnessGraph = new RealTimeGraph("Best in Population Performance", POPULATION_NUMBER);
		}
		
		if (fromRandom) {
			newSeed();
			seeds[0] = SEED;
		}
		// keep all files for this run in one directory
		directoryName = seeds[0] + "";
				
		for (int i = 0; i < ITERATIONS; i++) {
			System.out.println("Starting Iteration " + i);
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
				writeToFile(GEN_STATS_FILES[popNum], "Agent " + popNum + ": " + seeds[popNum] + "\n" + printFilePrefix() + "generation,fitness,movesMade,isAlive,hasCarried,hasMadeBridge,numStones,targetsFound,successful\n");
				//writeToFile(AVERAGE_FILES[popNum],   "Agent " + popNum + ": " + seeds[popNum] + "\n" + printFilePrefix() + "gen,avg,best,worst,stDev\n");
				writeToFile(FITNESS_FILES[popNum],   printFilePrefix() + "Generational Fitnesses: Agent " + popNum + ": " + seeds[popNum] + "\ngen,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24\n");
			}
			
			evolve(fromRandom, seeds);
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				writeToFile(GEN_STATS_FILES[popNum], printFileSuffix());
				//writeToFile(AVERAGE_FILES[popNum],   printFileSuffix());
				writeToFile(FITNESS_FILES[popNum], 	 printFileSuffix());
			}
			iteration++;
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
	public static void main(String[] args) {
		int numArgs = 7;
		int[] seeds = new int[args.length-numArgs];
		for (int i = 0; i < seeds.length; i++) {
			seeds[i] = Integer.valueOf(args[i+numArgs]);
			System.out.println(seeds[i]);
		}
		new Engine(Integer.valueOf(args[0]), Boolean.valueOf(args[1]), Double.valueOf(args[2]), 
				Integer.valueOf(args[3]), Integer.valueOf(args[4]), ActionType.valueOf(args[5]), Boolean.valueOf(args[6]), seeds);
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
	 */
	protected void evolve(boolean randomise, int[] seeds) throws IndexOutOfBoundsException {
		if (!randomise && seeds[0] == 0 && agents.isEmpty()) {
			throw new IndexOutOfBoundsException("Cannot evolve as no seeds are specified, randomise is false, and no "
					+ "current population exists to continue from.");
		}
		
		String[] outputs = {"", ""};
		String[] outputFit = {"",""};
		//String[] outputAvg = {"",""};
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
					agents.add(new ArrayList<Agent>());
					agents.get(popNum).add(new SocialActionAgent(ROWS, COLS));
					agentsToLoop[popNum] = agents.get(popNum).get(popIndex);
				}
				// once initialised, run an instance so the agents get a fitness
				loop(agentsToLoop, false);
				for (int f = 0; f < POPULATION_NUMBER; f++) {				
					fitnesses[f][popIndex] = agents.get(f).get(popIndex).evaluate(TIME_STEPS); 
				}
			}
		}
		else if (seeds[0] != 0) { // start from specified seeds
			currentBest = new Agent[POPULATION_NUMBER];
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				agents.add(new ArrayList<Agent>());
				if (seeds[popNum] == 0) {
					newSeed();
				}
				else {
					SEED = seeds[popNum];
					random.setSeed(SEED);
				}
				for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
					agents.get(popNum).add(new SocialActionAgent(ROWS, COLS));
				}
			}
			goalRandom.setSeed(SEED);
			// evaluate starting fitnesses			
			for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
				Agent[] agentsToLoop = new Agent[POPULATION_NUMBER];
				
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					agentsToLoop[popNum] = agents.get(popNum).get(popIndex);
				}
				loop(agentsToLoop, false);
				for (int f = 0; f < POPULATION_NUMBER; f++) {				
					fitnesses[f][popIndex] = agents.get(f).get(popIndex).evaluate(TIME_STEPS); 
				}
			}
		}
		// else will start from where the population left off. directoryName is still assigned		
		double[] avgs = new double[POPULATION_NUMBER];
		for (int out = 0; out < POPULATION_NUMBER; out++) {
			outputFit[out] += "0," + fitnessArrayToString(out) + "\n";
			avgs[out] = getAverageFitness(out);
			//outputAvg[out] += "0," + avgs[out] + "," + getBestFitness(out) + "," + getWorstFitness(out) 
			//	+ "," + getStandardDeviationFitness(out, avgs[out]) + "\n";
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
				
				loop(tnmt, false);
				
				for (int pop = 0; pop < POPULATION_NUMBER; pop++) {
					tournamentFitness[pop][t] = tournament[pop][t].evaluate(TIME_STEPS);
					fitnesses[pop][indexes[pop][t]] = tournamentFitness[pop][t];
				}
			}

			int[] worstIndex = new int[POPULATION_NUMBER];
			for (int p = 0; p < POPULATION_NUMBER; p++) {
				currentBest[p] = findBestParent(tournament[p]);
				outputs[p] += gen + "," + currentBest[p].getStringStatus() + "\n";
				worstIndex[p] = findWorstParent(tournament[p]); // worst index in the tournament;
			}

			if (TURN_ON_GRAPH) {
				Double[] fit = new Double[POPULATION_NUMBER];
				for (int pop = 0; pop < POPULATION_NUMBER; pop++) {
					fit[pop] = currentBest[pop].getFitness();
				}
				bestFitnessGraph.update(fit, iteration, gen);
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
			
			// add offspring to replace the worst, update fitnesses
			if (goalRandom.nextDouble() < GOAL_RATIONALITY) {
			    for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			    	agents.get(popNum).set(indexes[popNum][worstIndex[popNum]],
			    			agents.get(popNum).get(indexes[popNum][parentIndexes.get(popNum).get(0)]).
			    				produceOffspring(agents.get(popNum).get(indexes[popNum][parentIndexes.get(popNum).get(1)])));
					
			    	fitnesses[popNum][indexes[popNum][worstIndex[popNum]]] = 
			    			agents.get(popNum).get(indexes[popNum][worstIndex[popNum]]).evaluate(TIME_STEPS);
			    	}
			    //TODO loop new
			}
			else if (ACTION_TYPE == ActionType.TRADITIONAL) { // traditional
				ArrayList<double[][][]> commonGenes = new ArrayList<double[][][]>();
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {	
					for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
						commonGenes.add(agents.get(popNum).get(popIndex).getGenes());
					}
					agents.get(popNum).set(indexes[popNum][worstIndex[popNum]], 
							agents.get(popNum).get(indexes[popNum][parentIndexes.get(popNum).get(0)]).produceTraditionalOffspring(commonGenes));
				    fitnesses[popNum][indexes[popNum][worstIndex[popNum]]] = 
				    		agents.get(popNum).get(indexes[popNum][worstIndex[popNum]]).evaluate(TIME_STEPS);
				    //TODO loop new
				    commonGenes.clear();
				}
			}
			else { // ACTION_TYPE == ActionType.RANDOM
				Agent[] agentsToLoop = new Agent[POPULATION_NUMBER];
				
				for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
					agents.get(popNum).set(indexes[popNum][worstIndex[popNum]], new SocialActionAgent(ROWS, COLS));
					agentsToLoop[popNum] = agents.get(popNum).get(indexes[popNum][worstIndex[popNum]]);
				}
				loop(agentsToLoop, false);
				for (int f = 0; f < POPULATION_NUMBER; f++) {				
					fitnesses[f][indexes[f][worstIndex[f]]] = agents.get(f).get(indexes[f][worstIndex[f]]).evaluate(TIME_STEPS); 
				}
			}
			
			for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
				outputFit[popNum] += gen + "," + fitnessArrayToString(popNum) + "\n";
				avgs[popNum] = getAverageFitness(popNum);
				//outputAvg[popNum] += gen + "," + avgs[popNum] + "," + getBestFitness(popNum) + ","
				//		+ getWorstFitness(popNum) + "," + getStandardDeviationFitness(popNum, avgs[popNum]) + "\n";
 			}	
		    // no need to add a new population - the current one is changed incrementally
			if ((gen+1) % 500 == 0) {
		    	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
			        writeToFile(FITNESS_FILES[popNum], outputFit[popNum]);
			    	//writeToFile(AVERAGE_FILES[popNum], outputAvg[popNum]);
			        outputs[popNum] = "";
			        outputFit[popNum] = "";
			        //outputAvg[popNum] = "";
		    	}
		    }
		} // evolution finished
		
	   	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
	        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
	        writeToFile(FITNESS_FILES[popNum], outputFit[popNum]);
	    	//writeToFile(AVERAGE_FILES[popNum], outputAvg[popNum]);
	        outputs[popNum] = "";
	        outputFit[popNum] = "";
	        //outputAvg[popNum] = "";
    	}
		// print population weights to fitness files
		for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			outputs[popNum] += "//\nWeights" + popNum + "------------------------\n";
			outputFit[popNum] += "//\nWeights" + popNum + "------------------------\n";
			
			outputs[popNum] += currentBest[popNum].toString() + "\n";

			for (int popIndex = 0; popIndex < POPULATION_SIZE; popIndex++) {
				outputFit[popNum] += popIndex + " start\n";
				
				outputFit[popNum] += agents.get(popNum).get(popIndex).toString() + "\n";
				
				outputFit[popNum] += popIndex + " end\n";
			}	
			outputs[popNum] += "\nEnd of Weights" + popNum + "------------------------\n//\n";
		}
		
    	for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
	        writeToFile(GEN_STATS_FILES[popNum], outputs[popNum]);
	        writeToFile(FITNESS_FILES[popNum], outputFit[popNum]);
	    	//writeToFile(AVERAGE_FILES[popNum], outputAvg[popNum]);
	        outputs[popNum] = "";
	        outputFit[popNum] = "";
	        //outputAvg[popNum] = "";
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
	 */
	protected void loop(Agent[] a, boolean showViews) {
		model.resetAll();
		running = true;
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			a[i].reset();
			model.addAgent(a[i]);
		}
		// TODO change so any number of agents can have a start cell
		model.getAgents().get(0).setStartCell(model.getCell(0, 0));
		if (model.getAgents().size() == 2) {
			model.getAgents().get(1).setStartCell(model.getCell(ROWS-1, COLS-1));
		}
		
		if (TURN_ON_VISUALS) {
			environmentView = new View(model, "environment");
			activityViews = new View[POPULATION_NUMBER];
	
			for (int i = 0; i < POPULATION_NUMBER; i++) {
				activityViews[i] = new View(model, "activity", i);
			}
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
			if (showViews) {
				activityViews[i].update(model);
			}
		}
		if (showViews) {
			environmentView.update(model);
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
		
		output = "Created: " + LocalDateTime.now() + "; Seed: " + SEED + "\nIteration " + iteration + "\n";
		//generationFitness = new RealTimeGraph("Agent Performance, Run " + i);
		output += "//\nRun: " + iteration + "----------------\n";
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
		output += "//\nEnd of Run " + iteration + "------------------------\n//\n";
		
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
		FITNESS_FILES 	= new String[POPULATION_NUMBER];
		//AVERAGE_FILES 	= new String[POPULATION_NUMBER];
		
		for (int popNum = 0; popNum < POPULATION_NUMBER; popNum++) {
			GEN_STATS_FILES[popNum] = directoryName + "/generationStats-agent"  + popNum + "-iter" + iteration + ".csv";
			FITNESS_FILES[popNum]	= directoryName + "/fitnesses-agent" 	    + popNum + "-iter" + iteration + ".csv";
			//AVERAGE_FILES[popNum]	= directoryName + "/averages-agent" 		+ popNum + "-iter" + iteration + ".csv";
		}
	}
}