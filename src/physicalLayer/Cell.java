package physicalLayer;

/**
 * A class to model a position in the 2D environment. A Cell has a {@link physicalLayer.Location}, 
 * and can contain zero or one {@link physicalLayer.CellObject CellObjects}.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 *
 */
public class Cell {
	/** An object contained in the Cell, or null. */
	private CellObject object = null;
	/** The Location of the Cell in the 2D environment. */
	private Location location;

	/**
	 * Creates a cell with a location and no object.
	 * 
	 * @param loc The location of the cell in the 2D environment.
	 */
	public Cell(Location loc) {
		location = loc;
	}

	/**
	 * Returns the location of the cell in the 2D environment.
	 * 
	 * @return The location of the cell.
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Adds an object to the cell. Zero or one objects can be contained in a cell.
	 * 
	 * @param object The object to add.
	 */
	public void addObject(CellObject object) {
		this.object = object;
	}
	
	/**
	 * Removes any objects from the cell. If null, the cell is Grass.
	 */
	public void removeObject() {
		object = null;
	}

	/**
	 * Returns the object that is contained in the cell.
	 * 
	 * @return The object contained in the cell.
	 */
	public CellObject getObject() {
		return object;
	}
}