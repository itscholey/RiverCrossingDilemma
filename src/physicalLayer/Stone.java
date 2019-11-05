package physicalLayer;

/**
 * A class to model a non-dangerous Stone object, which can be picked up and used to build bridges in Water.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Stone extends CellObject {
	/**
	 * Creates a non-dangerous Stone object which has a location.
	 * @param thisLocation The location.
	 */
	public Stone(Cell thisLocation) {
		super(thisLocation);
		isDangerous = false;
	}	
}