package physicalLayer;

/**
 * A class to model the different types of social action that an agent can employ in the River Crossing Dilemma.
 * <ul>
 * 	<li><b>Goal-Rational</b>: during the breeding process, the agents that are chosen as parents are those with the 
 * 		highest fitness in the tournament. These combine to create an offspring, which replaces the worst-performing 
 * 		agent in the tournament. Goal-rationality here is taking the action that is most likely to bring success and
 * 		is most likely to enable the agent to achieve its goal.</li>
 * 	<li><b>Traditional</b>: traditional action is operationalised in the breeding process, by replacing the worst-
 * 		performing agent in the tournament, with an agent that is a representative state of the population using the
 * 		median weights across the population at that generation. If the percentage of goal-rationality is set to 0.0
 * 		then the tradition breeding process is always used; else there is a percentage chance of traditional action,
 * 		where the rest of the time goal-rational action is used.</li>
 * 	<li><b>Random</b>: random action is operationalised in the breeding process by replacing the worst-performing
 * 		agent in the tournament with a randomly-initialised agent. If the percentage of goal-rationality is set to 
 * 		0.0 then the random breeding process is always used; else there is a percentage change of random action, 
 * 		where the rest of the time goal-rational action is used. Random action here is used in order to compare 
 * 		with traditional action, to ensure that traditional action and random action are not the same.</li>
 * </ul>
 * @author Chloe M. Barnes
 * @version v1.2
 */
public enum ActionType {
	GOAL_RATIONAL,
	TRADITIONAL,
	RANDOM
}