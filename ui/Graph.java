package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

@SuppressWarnings("serial")
public class Graph extends JPanel 
{
	private static final int PREF_W = 800;
	private static final int PREF_H = 650;
	int startGapText = 10;
	private static final int BORDER_GAP = 30;
	private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
	private List< Tuple<Integer, Integer> > data;

	public Color lineColor = new Color(255, 0, 0, 150);
	public Color pointColor = new Color(255, 150, 50, 255);
	public int pointSize = 10;
	public int hatchMarksSize = 10;

	private String title;
	public String xAxisLabel = "", yAxisLabel = "";
	public int maxY; // Max Y value.
	public int minY;
	public boolean recalcMinMaxY = true; // If false, lock them at current values.
   
	private int maxX, minX;
	int hatchMarksX = 10, hatchMarksY = 4; // Instead of varying it with number of data points.
	
	public Graph(String name, String xAxisLabel, String yAxisLabel)
	{
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		title = name;
		data = new ArrayList< Tuple<Integer, Integer> >();
		Random random = new Random();
		int maxDataPoints = 16;
		// Random initial content.
		int maxScore = 20;
		System.out.println("Scores: ");
		for (int i = 0; i < maxDataPoints ; i++) 
		{
			int dataPoint = random.nextInt(maxScore);
			data.add(new Tuple<Integer, Integer>(i, dataPoint));
			System.out.print(dataPoint+", ");
		}
		System.out.println("");
	}
   
	void RecalcMinMaxY()
	{
		if (recalcMinMaxY == false)
			return;
		// Reset min/max.
		maxY = minY = data.get(0).y;
		for (int i = 1; i < data.size(); i++) 
		{
			Tuple<Integer, Integer> tuple = data.get(i);
			int value = tuple.y;
			if (value > maxY)
				maxY = value;
			if (value < minY)
				minY = value;
		}		
	}
	void RecalcMinMaxX()
	{
		// Reset min/max.
		maxX = minX = data.get(0).x;
		for (int i = 1; i < data.size(); i++) 
		{
			Tuple<Integer, Integer> tuple = data.get(i);
			int value = tuple.x;
			if (value > maxX)
				maxX = value;
			if (value < minX)
				minX = value;
		}		
	}
	@Override
	protected void paintComponent(Graphics g) 
	{
		super.paintComponent(g);

		RecalcMinMaxY(); // Recalc min/max Y as needed.
		RecalcMinMaxX(); // Used for Time, so should be done regularly.
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int xSize = maxX - minX, 
			ySize = maxY - minY;
		System.out.println("xSize: "+xSize+" ySize: "+ySize);
		/// Pixels per X/Y value step.
		int graphContentWidth = getWidth() - 2 * BORDER_GAP,
				graphContentHeight = getHeight() - 2 * BORDER_GAP;
		int startPixelX = BORDER_GAP,
			startPixelY = getHeight() - BORDER_GAP; // Since Y goes from 0 and increases downwards, the bottom is at (height - gap).
		int stopPixelX = startPixelX + graphContentWidth,
			stopPixelY = startPixelY - graphContentHeight;
		double xScale = ((double) graphContentWidth) / (xSize);
		double yScale = ((double) graphContentHeight) / (ySize);
		
		List<Point> graphPoints = new ArrayList<Point>();
		int max = 0, min = 0;
		for (int i = 0; i < data.size(); i++) 
		{
			Tuple<Integer, Integer> tuple = data.get(i);
			int x1 = (int) (startPixelX + tuple.x * xScale); // Point to render, X
//			System.out.println("x render point: "+x1);
			int y1 = (int) (startPixelY - (tuple.y - minY) * yScale); // Point to render, Y
			graphPoints.add(new Point(x1, y1));
		}
	
		/// Draw title, hopefully in the middle?
		g2.setColor(Color.GRAY);
		Font font = new Font("TimesRoman", Font.PLAIN, 20);
		g2.setFont(font); 
		g2.drawString(title, getWidth() / 2 - title.length() * font.getSize() * 0.15f, getHeight() / 2);

		// Add labels for x and y.
		g2.setFont(new Font("TimesRoman", Font.PLAIN, 14)); 
		g2.drawString(yAxisLabel, BORDER_GAP, 20); // Y axis
		g2.drawString(xAxisLabel, getWidth() / 2, getHeight()); // X axis
		
		// Draw the min/max values.
		g2.drawString(""+maxY, startGapText - 5, BORDER_GAP + 8);
		g2.drawString(""+minY, startGapText, startPixelY);
		
		// create x and y axes 
		g2.drawLine(startPixelX, startPixelY, startPixelX, stopPixelY); // Y axis
		g2.drawLine(startPixelX, startPixelY, stopPixelX, startPixelY); // X axis

		// create hatch marks for y axis. 
		for (int i = 0; i < hatchMarksY; i++) {
			int x0 = BORDER_GAP;
			int x1 = hatchMarksSize + BORDER_GAP;
			int y0 = getHeight() - (((i + 1) * (getHeight() - BORDER_GAP * 2)) / hatchMarksY + BORDER_GAP);
			int y1 = y0;
			g2.drawLine(x0, y0, x1, y1);
		}
		// and for x axis
		for (int i = 0; i < hatchMarksX - 1; i++) 
		{
			int x0 = (i + 1) * (getWidth() - BORDER_GAP * 2) / (hatchMarksX - 1) + BORDER_GAP;
			int x1 = x0;
			int y0 = getHeight() - BORDER_GAP;
			int y1 = y0 - hatchMarksSize;
			g2.drawLine(x0, y0, x1, y1);
		}

		/// Lines between the points.
		Stroke oldStroke = g2.getStroke();
		g2.setColor(lineColor);
		g2.setStroke(GRAPH_STROKE);
		for (int i = 0; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x;
			int y1 = graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x;
			int y2 = graphPoints.get(i + 1).y;
			g2.drawLine(x1, y1, x2, y2);         
		}

		g2.setStroke(oldStroke);      
		g2.setColor(pointColor);
		for (int i = 0; i < graphPoints.size(); i++) {
			int x = graphPoints.get(i).x - pointSize / 2;
			int y = graphPoints.get(i).y - pointSize / 2;;
			int ovalW = pointSize;
			int ovalH = pointSize;
			g2.fillOval(x, y, ovalW, ovalH);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}

	public void SetMinMaxY(int min, int max) {
		recalcMinMaxY = false;
		minY = min;
		maxY = max;
	}

	/*
   private static void createAndShowGui() {
      List<Integer> scores = new ArrayList<Integer>();
      Random random = new Random();
      int maxDataPoints = 16;
      int maxScore = 20;
      for (int i = 0; i < maxDataPoints ; i++) {
         scores.add(random.nextInt(maxScore));
      }
      Graph mainPanel = new Graph(scores);

      JFrame frame = new JFrame("DrawGraph");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(mainPanel);
      frame.pack();
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
          //  createAndShowGui();
         }
      });
   }*/
}