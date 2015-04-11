

package org.opensourcephysics.stp.productprocess;

import java.text.NumberFormat;
import java.util.Random;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.HistogramFrame;

/*
 *@author     Joshua Gould
 *@uathor     Natali Gulbahce
 *@created    Oct 6, 2002
 */

public class ProductProcessApp extends AbstractSimulation
{
	HistogramFrame histogramFrame = new HistogramFrame("log(Product)", "Occurences", "Histogram");
	Random random;
	NumberFormat numberFormat;

	double probability, x, y, product, time, dt, sum = 0.0, sum2 = 0.0;
	int numberOfMultiplication, numberOfRuns = 0;

	public ProductProcessApp()
	{
		random = new Random();
		numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(4);
		
	}

	public void initialize()
	{
		numberOfMultiplication = control.getInt("N");
		probability = control.getDouble("probability");
		x = control.getDouble("x1");
		y = control.getDouble("x2");
		product = 1.0;
		double maxXY = x;
		if(y > x) maxXY = y; 
		maxXY=numberOfMultiplication*Math.log(maxXY);
		int numberOfBins= numberOfMultiplication;
		double binWidth= maxXY/numberOfBins;
		//histogramFrame.setPreferredMinMaxX(0, 1);
		histogramFrame.setAutoscaleX(true);
		histogramFrame.setAutoscaleY(true);
		histogramFrame.setBinWidth(binWidth);
		random.setSeed(System.currentTimeMillis());
		product = 1.0;
		sum = 0.0;
		sum2 = 0.0;
		numberOfRuns = 0;

		}

	public void start()
	{
		probability = control.getDouble("probability");
	}
	public void stop(){
		output();
	}
	public void reset()
	{
		probability = 0.5;
		numberOfMultiplication = 4;
		x = 2.0;
		y = 0.5;
		control.setValue("N", numberOfMultiplication);
		control.setValue("probability", probability);
		control.setValue("x1", x);
		control.setValue("x2", y);
		//histogramFrame.clearData();
		//histogramFrame.repaint();
		product = 1.0;
		control.clearMessages();
		sum = 0.0;
		sum2 = 0.0;
		numberOfRuns = 0;
		enableStepsPerDisplay(true);
//		control.println("# of runs = 0");
//		control.println("<product> = 0");
//		control.println("<product*product> = 0");
//		control.println("sigma = 0");
	}

	public void output()
	{
		double productbar = sum / numberOfRuns;
		double product2bar = sum2 / numberOfRuns;
		double sigma = Math.sqrt(product2bar - productbar * productbar);
		control.clearMessages();
		control.println("# of trials = " + numberOfRuns);
		control.println("<product> = " + numberFormat.format(productbar));
		control.println("<product*product> = "
				+ numberFormat.format(product2bar));
		control.println("sigma = " + numberFormat.format(sigma));
	}

	public static void main(String[] args)
	{
		SimulationControl.createApp(new ProductProcessApp(),args);
	}

	protected void doStep()
	{
		probability = control.getDouble("probability");
		product = 1.0;

		for (int i = 0; i < numberOfMultiplication; i++)
		{
			if (random.nextDouble() < probability)
			{
				product *= x;
			}
			else
			{
				product *= y;
			}
		}
		// System.out.println("product = " +product);
		sum += product;
		sum2 += product * product;
		numberOfRuns++;
		histogramFrame.append(Math.log(product));
		//histogramFrame.render();
	}
}
