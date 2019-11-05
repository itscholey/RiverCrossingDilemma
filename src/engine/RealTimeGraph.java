package engine;

import org.jfree.chart.ChartPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A class to generate a 2D line graph that can be used to visualise performance in real-time.
 * A RealTimeGraph can be used to generate a graph for 1:* series in the same panel, and will
 * update to average performance across a number of iterations/runs if necessary or specified.
 * 
 * @author Chloe M. Barnes
 * @version v1.2
 */
public class RealTimeGraph extends ApplicationFrame {
	/** The dataset for the graph. */
	private XYSeriesCollection dataset;
	
	/**
	 * Creates a graph for performance based on a dataset that will be updated with data in real-time, with the x-axis
	 * as "Generation" and the y-axis as "Fitness". 
	 * 
	 * @param title The title for the graph.
	 * @param numSeries The number of sets of data (series) that the graph should visualise.
	 * @see RealTimeGraph#update(Double[], Integer, Integer)
	 */
	public RealTimeGraph(String title, int numSeries) {
		super(title);
		createBestDataset(numSeries);
	    JFreeChart chart = createChart(dataset, title);
	    ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(1250, 500));
	    setContentPane(chartPanel);
	    pack();
	    setVisible(true);
	}
	
	/**
	 * Updates the dataset for the graph and displays the changes to the visualisation of the data. If the iteration is &gt; 0,
	 * the data across all iterations so far will be averaged in real-time.
	 * 
	 * @param fitnesses An array containing the data to update the graph on the y-axis. The length of the array must be equal 
	 * to the number of series specified when creating the RealTimeGraph.
	 * @param iteration The iteration of the data currently. If &gt; 0, the data across all iterations so far will be averaged.
	 * @param generation The generation of the data. This is the x-axis.
	 */
	public void update(Double[] fitnesses, Integer iteration, Integer generation) {
		if (iteration > 0) {
			for (int i = 0; i < fitnesses.length; i++) {
				dataset.getSeries(i).update(generation, 
						(Double)((((Double)dataset.getSeries(i).getY(generation) * iteration) + fitnesses[i]) / (iteration+1)));
			}
		}
		else {
			for (int i = 0; i < fitnesses.length; i++) {
				dataset.getSeries(i).add(new XYDataItem(generation, fitnesses[i]));
			}
		}
	}

	/**
	 * Creates a dataset that a graph will use as a model to visualise data.
	 * 
	 * @param numSeries The number of sets of data (series) that the graph should visualise.
	 */
	private void createBestDataset(int numSeries) {
    	dataset = new XYSeriesCollection();
        for (int i = 0; i < numSeries; i++) {
        	if (i < numSeries) {
        		XYSeries s = new XYSeries("Fitness of Agent " + (i));
        		dataset.addSeries(s);
        	}
        }
    }
	
    /**
     * Constructs and returns a chart with x-axis as "Generation" and y-axis as "Fitness",
     * that is created with a specified dataset and a specified title.
     * 
     * @param dataset The dataset that the chart should visualise.
     * @param title The title of the chart.
     * @return A {@link org.jfree.chart.JFreeChart} with the specified parameters.
     */
    private JFreeChart createChart(XYDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,      				// chart title
            "Generation",         		// x axis label
            "Fitness",                  // y axis label
            dataset,                  	// data
            PlotOrientation.VERTICAL,
            true,                    	// include legend
            true,                     	// tooltips
            false                     	// urls
        );
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesStroke(0,new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
        		10.0f, new float[] {10.0f}, 0.0f));
        plot.setRenderer(renderer);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        return chart;
    }
}