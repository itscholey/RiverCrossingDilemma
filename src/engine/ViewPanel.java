package engine;

import java.awt.Dimension;
import javax.swing.JPanel;
import physicalLayer.PhysicalLayer;

/**
 * A class to model the panel which displays a representation of the underlying model of the environment.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public abstract class ViewPanel extends JPanel {
	/** Specifies the preferred size of each grid square. */
	protected static final int PREFERRED_TILE_SIZE = 25;
	/** Specifies the preferred width of a grid square. */
	protected int prefWidth;
	/** Specifies the preferred height of a grid square. */
	protected int prefHeight;
	/** The number of rows in the environment. */
	protected int rows;
	/** The number of columns in the environment. */
	protected int cols;
	/** The underlying model to display. */
	protected PhysicalLayer model;
	
	/**
	 * Creates a new panel containing a visual representation of the underlying model of the environment,
	 * in the preferred dimensions.
	 * 
	 * @param model The underlying model of the environment.
	 */
	public ViewPanel(PhysicalLayer model) {
		this.model = model;
		rows = model.getRows();
		cols = model.getCols();
		
		prefWidth  = (cols * PREFERRED_TILE_SIZE) + cols + 1;
		prefHeight = (rows * PREFERRED_TILE_SIZE) + rows + 1;
		setPreferredSize(new Dimension(prefWidth, prefHeight));
	}	
	
	/**
	 * Updates the model to reflect changes in the environment.
	 * 
	 * @param pl The underlying model.
	 */
	public void update(PhysicalLayer pl) {
		model = pl;
	}
}