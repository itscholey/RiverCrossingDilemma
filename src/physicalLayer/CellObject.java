package physicalLayer;

/**
 * An abstract class to model an object that is contained in a {@link physicalLayer.Cell}. Objects reside in one cell only.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public abstract class CellObject {
	/** The location of this specific cell. */
	protected Cell thisLocation;
	/** If the object is dangerous, and will cause loss of life or damage. */
	protected boolean isDangerous;
	
	/**
	 * For invocation by subclasses. Creates an object in a cell, with a location.
	 * 
	 * @param thisLocation The location of the current cell.
	 */
	public CellObject(Cell thisLocation) {
		this.thisLocation = thisLocation;
	}
	
	/**
	 * Returns whether the object in the cell is dangerous.
	 * 
	 * @return true if it is dangerous and will cause loss of life or damage, false otherwise.
	 */
	public boolean isDangerous() {
		return isDangerous;
	}
	
	/**
	 * Returns the cell where the object is located.
	 * 
	 * @return The cell where the object is located.
	 */
	public Cell getCell() {
		return thisLocation;
	}
	
	/**
	 * Sets the cell where the object is to be located.
	 * 
	 * @param location The cell where the object is to be located.
	 */
	public void setLocation(Cell location) {
		thisLocation = location;
	}	
}