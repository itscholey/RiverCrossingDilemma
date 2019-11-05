package engine;

import java.awt.Graphics;
import java.awt.Color;
import physicalLayer.*;

/**
 * A panel view to display the 2D environment from the overall perspective, using colour to 
 * indicate the different objects that exist in the environment.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class EnvironmentView extends ViewPanel {
	/**
	 * Creates a new view for the environment based on the underlying model.
	 * 
	 * @param model The underlying model of the environment to display.
	 */
	public EnvironmentView(PhysicalLayer model) {
		super(model);
	}	
	
	/**
	 * Paints a visual representation of the elements in the environment onto the panel.
	 * 
	 * @param g The Graphics object used to paint.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.clearRect(0, 0, getWidth(), getHeight());
		int width  = (getWidth()-cols)  / cols;
		int height = (getHeight()-rows) / rows;
		
		for (int i = 0; i < rows; i++) {
			// draw row border
			g.setColor(Color.BLACK);
			g.drawLine(0, ((height*i)+i), ((width*cols)+cols), ((height*i)+i));
			
			for (int j = 0; j < cols; j++) {
				if (model.getCell(i, j).getObject() instanceof Stone){
					g.setColor(Color.GRAY);
				}
				else if (model.getCell(i, j).getObject() == null){ // grass
					g.setColor(Color.WHITE);
				}
				else if (model.getCell(i, j).getObject() instanceof Resource){
					g.setColor(Color.MAGENTA);
				}
				else if (model.getCell(i, j).getObject() instanceof Trap){
					g.setColor(Color.RED);
				}
				else if (model.getCell(i, j).getObject() instanceof Water){
					if(((Water) model.getCell(i,j).getObject()).getDepth() >= 2) {
						g.setColor(new Color(0, 0, 200));
					}
					else {
						g.setColor(new Color(0, 128, 255));
					}
				}
				else {
					g.setColor(Color.WHITE);
				}
				
				for (int k = 0; k < model.getNumAgents(); k++) {
					if (model.getAgentLocation(k) == model.getCell(i, j)) {
						g.setColor(Color.CYAN);
					}
				}
				int x = (j * width)  + j + 1;
				int y = (i * height) + i + 1;
				g.fillRect(x, y, width, height);
				
				// draw column border
				g.setColor(Color.BLACK);
				g.drawLine(x-1, y-1, x-1, ((height*rows)+rows));
			}
		}
		g.setColor(Color.BLACK);
		// last row
		g.drawLine(0, ((height*cols)+cols), ((width*cols)+cols), ((height*cols)+cols));
		// last column
		g.drawLine(((width*rows)+rows), 0, ((width*rows)+rows), ((height*cols)+rows));		
	}
}