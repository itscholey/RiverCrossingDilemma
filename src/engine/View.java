package engine;

import javax.swing.JFrame;
import physicalLayer.PhysicalLayer;

/**
 * A class to display a visual representation of the environment, using an underlying model of the environment.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class View {
	/** The window to display the representation of the environment. */
	private JFrame mainFrame;
	/** The panel in which the environment representation will be displayed. */
	private ViewPanel grid;
	/** The underlying model of the environment. */
	private PhysicalLayer model;
	
	/**
	 * Creates a new view window of a specified type, based on a model of an environment, from the perspective of a 
	 * specified agent.
	 * 
	 * @param physicalLayer The underlying model.
	 * @param type The type of ViewPanel that should be created.
	 * @param i The index of the agent that the view is from the perspective of.
	 */
	public View(PhysicalLayer physicalLayer, String type, int i) {
		mainFrame = new JFrame();
		mainFrame = new JFrame("River Crossing Task");
		this.model = physicalLayer;
		if (type.equals("activity")) {
			grid = new ActivityLandscapeView(physicalLayer, i);
			mainFrame.setTitle(mainFrame.getTitle() + " || Agent " + i + "'s Activity Landscape");
		}
		else {
			grid = new EnvironmentView(physicalLayer);
			mainFrame.setTitle(mainFrame.getTitle() + " || Environmental Landscape");
		}
		buildGUI();
	}
	
	/**
	 * Creates a new view window of a specified type, based on a model of an environment, from the perspective of a 
	 * specified agent. Used mainly for an EnvironmentView type, where it is not from a particular agent's perspective.
	 * 
	 * @param physicalLayer The underlying model.
	 * @param type The type of ViewPanel that should be created.
	 */
	public View(PhysicalLayer physicalLayer, String type) {
		this(physicalLayer, type, 0);
	}
	
	/**
	 * Sets up the GUI size and makes it visible.
	 */
	private void buildGUI() {	
		mainFrame.add(grid);
		mainFrame.setSize(grid.getPreferredSize());
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
	
	/**
	 * Updates the display to reflect changes in the model, and repaints the view.
	 * 
	 * @param physicalLayer The underlying model.
	 */
	public void update(PhysicalLayer physicalLayer) {
		this.model = physicalLayer;
		grid.update(model);
		grid.repaint();
	}
}