package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import physicalLayer.PhysicalLayer;

/**
 * A panel view to display the activation landscape view from the perspective of a specified agent, using colour to 
 * visualise the hill-climbing landscape and generated sub-goals.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class ActivityLandscapeView extends ViewPanel {
	/** The index of the agent in which the activity landscape is from the perspective of. */
	private int agentNum;

	/**
	 * Creates a new view for the activity landscape of a specified agent based on the underlying model of the environment.
	 * 
	 * @param model The underlying model of the environment to display.
	 * @param agentNum The index of the agent in which the activity landscape is from the perspective of.
	 */
	public ActivityLandscapeView(PhysicalLayer model, int agentNum) {
		super(model);
		this.agentNum = agentNum;
		System.out.println("agent activity: " + agentNum);
	}
	
	/**
	 * Paints a visual representation of the activity landscape onto the panel, using colour to indicate the strength of attraction or avoidance.
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
				if (model.getAgentLocation(agentNum) == model.getCell(i, j)) {
					g.setColor(Color.CYAN);
				}
				else {
					if (model.getLandscape(agentNum)[i][j] > 0) {
						int gb = 255 - (int)(Math.round((model.getLandscape(agentNum)[i][j]/15) * 255)*2);
						if (gb < 0) {
							gb = 0;
						}
						g.setColor(new Color(255, gb, gb));
					}
					else if (model.getLandscape(agentNum)[i][j] == 0.0) {
						g.setColor(Color.WHITE);
					}
					else {
						g.setColor(Color.GRAY);
					}
				}
				int x = (j * width)  + j + 1;
				int y = (i * height) + i + 1;
				g.fillRect(x, y, width, height);
				
				// draw column border
				g.setColor(Color.BLACK);
				g.drawLine(x-1, y-1, x-1, ((height*rows)+rows));
				g.setFont(new Font("Times", Font.PLAIN, 13));
				g.drawString(model.getLandscape(agentNum)[i][j]+"", x+10, y+10);
			}
		}
		g.setColor(Color.BLACK);
		// last row
		g.drawLine(0, ((height*cols)+cols), ((width*cols)+cols), ((height*cols)+cols));
		// last column
		g.drawLine(((width*rows)+rows), 0, ((width*rows)+rows), ((height*cols)+rows));		
	}
}