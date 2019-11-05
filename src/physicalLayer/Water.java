package physicalLayer;

/**
 * A class to model a dangerous Water object, which has a depth. Objects can be placed in it to reduce depth.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Water extends CellObject {
	/** The depth of the Water. */
	private int depth;
	
	/**
	 * Creates a dangerous Water object with a location and a depth.
	 * 
	 * @param thisLocation The location.
	 * @param depth The depth.
	 */
	public Water(Cell thisLocation, int depth) {
		super(thisLocation);
		isDangerous = true;
		this.depth = depth;
	}
	
	/**
	 * Sets the depth of the Water.
	 * 
	 * @param depth The depth to set.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	/**
	 * Returns the depth of the Water.
	 * 
	 * @return The depth.
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * Add a stone into the Water to decrease the depth and build or partially build a bridge.
	 * 
	 * @return true if the Water still has depth (depth &gt; 0), false otherwise.
	 */
	public boolean addStone() {
		depth--;
		if (depth <= 0) {
			return false;
		}
		return true;
	}
}