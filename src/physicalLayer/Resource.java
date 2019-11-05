package physicalLayer;

/**
 * A class to model a Resource object, which has a location. Resources are to be collected in order to achieve goals.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Resource extends CellObject {
	/**
	 * Creates a non-dangerous Resource object with a location.
	 * 
	 * @param thisLocation The location.
	 */
	public Resource(Cell thisLocation) {
		super(thisLocation);
		isDangerous = false;
	}
}