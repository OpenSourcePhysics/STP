package org.opensourcephysics.stp.idealgasintegrals.bose;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.*;

import java.awt.Color;
import java.text.NumberFormat;

/**
 * ComputeBoseIntegralApp class.
 *
 *  @author Hui Wang
 *  @version 1.0   revised 07/19/06
 *  @version 2.0   revised 07/30/08 by Jan Tobochnik
 */
public class ComputeBoseIntegralApp extends AbstractCalculation implements Function{
	PlotFrame plotFrame = new PlotFrame("T*", "\u03bc*", "Temperature Dependence of the Chemical Potential");	//mu

	double b = 1.0;
	double T = 1.0;
	double mu = -1.0;
	double x_low = 0.0;
	double x_high = 1e3;
	int npoints = 0;
	double[] mua, Ta;
	double tolerance = 0.001;
	int nmaxTrials = 1000;
	NumberFormat nf = NumberFormat.getInstance();
	
	ComputeBoseIntegralApp(){
		mua = new double[nmaxTrials];
		Ta = new double[nmaxTrials];
		plotFrame.setPreferredMinMaxX(0, 10);
		plotFrame.setAutoscaleX(true);
		nf.setMaximumFractionDigits(3);
		plotFrame.setMarkerColor(0,Color.RED);
		plotFrame.setXYColumnNames(0,"T*", "\u03bc*");
	}
   public void reset() {
	   control.setValue("T*", 10.0);   
	   control.setValue("Guess for \u03bc*", -1.0);	//mu
	   control.println("Calculated integral \t T* \t guess for \u03bc*");
	   control.println("(= 1 if \u03bc* is correct)");
	   npoints=0;
   }

   public double evaluate(double x){
	   return Math.sqrt(x)/(Math.exp(b*(x - mu)) - 1);
   }
   
   public void calculate1() {
	   for (double T = 0.1; T < 15; T = T + 0.1){
		   double  ex = Math.exp(-1.0/T);
		   double ex10 = Math.exp(-10.0/T);
		   double z = 1 + ex + ex10;
		   double e1 = (ex + 10*ex10)/z;
		   double T2 = T + 0.001;
		     ex = Math.exp(-1.0/T2);
		    ex10 = Math.exp(-10.0/T2);
		    z = 1 + ex + ex10;
		   double e2 = (ex + 10*ex10)/z;
		   double c = (e2-e1)/0.001;
		   plotFrame.append(0,T,c);
		   }
   }
   public void calculate() {

      mu = control.getDouble("Guess for \u03bc*");      
      T = control.getDouble("T*");
      b = 1.0/T;
      double a = -b*mu;
      double rhs;
      if(a >= 0 && a < 0.1){
    	  rhs = Math.pow(b,-1.5)*(1.0/2.612)*(-3.54*Math.sqrt(a) + 2.612 + 1.46*a - 0.104*a*a + 0.00425*a*a*a);
          // expansion based on Pathria, Statistical Mechanics, page 504. 
      }
      else
          rhs = 0.432*Integral.simpson(this, 0, x_high, 2, tolerance);
      //double A = Math.exp(-b*mu) - 1.0;
      //double B = b*Math.exp(-b*mu);
     // rhs += 0.432*(2.0/B)*(Math.sqrt(x_low) - Math.sqrt(A/B)*Math.atan(Math.sqrt(B*x_low/A)));
      //control.println();
      //control.println("T* = " + T + "  Guess for \u03bc* = " + mu);	//mu
      control.println( nf.format(rhs) + "\t \t" +  T  + "\t" + mu);	// integral 
      
     plot();
   }

   public void accept(){
	   mua[npoints] = mu;
	   Ta[npoints] = T;
	   npoints++;
	   if(npoints > nmaxTrials){
		   control.print("Too many points");
		   System.exit(-1);
	   }
	   plot();
   }
   public void plot(){
	   plotFrame.clearData();
	   for(int i = 0; i < npoints; i++){
		   plotFrame.append(0,Ta[i],mua[i]);
	   }
	   plotFrame.render();
   }
   public static void main(String[] args) {
	   CalculationControl control = CalculationControl.createApp(new ComputeBoseIntegralApp(),args);
	   control.addButton("accept", "Plot (\u03bc*,T*)");
   }
}
