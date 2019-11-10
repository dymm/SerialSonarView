package com.dymm.sonar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JComponent;

public class MyCanvas extends JComponent {

	List<Measure> measures;
	
	public MyCanvas(List<Measure> measures) {
		this.measures = measures;
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8326903659037256901L;

	 private static Color m_tRed = new Color(255, 0, 0, 150);
	  private static Color m_tGreen = new Color(0, 255, 0, 150);
	  private static Color m_tBlue = new Color(0, 0, 255, 150);
	  private static Font monoFont = new Font("Monospaced", Font.BOLD | Font.ITALIC, 36);

	  private static Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
	  private static Font serifFont = new Font("Serif", Font.BOLD, 24);

	  public void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    int width = this.getWidth();
	    int heigth = this.getHeight();
	    
	    int maxRange = 100;				//max de 100cm
	    int centerX = width / 2;
	    int centerY = heigth / 2;
	    
	    // maxRange*2 -> width
	    // dist -> x
	    // x = (width * dist) / (maxRange*2)
	    
	    g.setColor(Color.BLACK);
	    g.drawLine(0, centerY, width, centerY);
	    g.drawLine(centerX, 0, centerX, heigth);
	    
	    g.setColor(Color.GRAY);
	    for(int i=0;i<=800;i+=20) {
	    	int c = (width * i) / (maxRange*2);
	    	g.drawOval(centerX - c/2, centerY - c/2, c, c);
	    }
	    
	    g.setColor(Color.magenta);
	    double xCoef = 0;
	    double yCoef = 0;
	    for(Measure measure : measures ) {
	    	xCoef = Math.cos( Math.toRadians(measure.getAngle()));
	    	double xPos = xCoef*measure.getDistance();
	    	int x = -1*(int)Math.floor((width * xPos) / (maxRange*2)) + centerX;	//-1 pour inverser les x
	    	
	    	yCoef = -1*Math.sin(Math.toRadians(measure.getAngle()));			//-1 pour inverser les y 
	    	double yPos = yCoef*measure.getDistance();
	    	int y = (int)Math.floor((width * yPos) / (maxRange*2)) + centerY;	//width parsque je ne peux pas faire des ovales facilement

		    final int circleWidth = 10;
		    final int circleHeigth = 10;
	    	g.fillRect(x - circleWidth/2, y-circleHeigth/2, circleWidth, circleHeigth);
	    }
	    
	    //Trace une ligne indiquant le dernier angle lu
	    g.setColor(Color.BLUE);
	    g.drawLine(centerX, centerY, centerX + (int)(xCoef*maxRange*100), centerY + (int)(yCoef*maxRange*100));
	    
	  }

	  public Dimension getPreferredSize() {
	    return new Dimension(400, 400);
	  }

	  public Dimension getMinimumSize() {
	    return getPreferredSize();
	  }
}
