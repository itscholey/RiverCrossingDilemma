package physicalLayer;

/**
 * A class to model a dangerous Trap object.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Trap extends CellObject{
	/**
	 * Creates a dangerous Trap object which as a location.
	 * @param thisLocation The location.
	 */
	public Trap(Cell thisLocation) {
		super(thisLocation);
		isDangerous = true;
	}
}