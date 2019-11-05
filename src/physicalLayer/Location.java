package physicalLayer;

import java.util.Objects;

/**
 * A class to model a location in a 2D environment.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class Location {
	/** The x position. */
	private int x;
	/** The y position. */
	private int y;
	
	/**
	 * Creates a location object which contains an x and y position.
	 * 
	 * @param x The x position.
	 * @param y The y position.
	 */
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Converts the location to an array.
	 * 
	 * @return An int array containing the x and y position.
	 */
	public int[] toArray() {
		int[] result = {x, y};
		return result;
	}
	
	/**
	 * Returns whether the given location is equal to the current location.
	 * 
	 * @param o An object to compare to.
	 * @return true if the locations are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof Location)) {
			return false;
		}
		Location l = (Location) o;
		return l.x == x && l.y == y;
	}
	
    /** 
     * Returns a hashcode of the object based on the x and y position.
     * 
     * @return A hashcode ased on the x and y position.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x,y);
    }
}