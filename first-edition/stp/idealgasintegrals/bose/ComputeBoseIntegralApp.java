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
 *  @version 2.1   revised 06/04/18 by Jan Tobochnik
 *  @version 3.0   revised 09/08/19 by Jan Tobochnik
 */
public class ComputeBoseIntegralApp extends AbstractCalculation implements Function{
	PlotFrame plotFrame = new PlotFrame("T*= T/Tc", "\u03bc*", "Temperature Dependence of the Chemical Potential");	//mu
	PlotFrame energyFrame = new PlotFrame("T*=T/Tc", "e* = E/NkTc ", "Temperature Dependence of the Energy");     // ��
	double b = 1.0;
	double T = 1.0;
	double mu = -1.0;
	int npoints = 0;
	double[] mua, Ta, Ea;
	double tolerance = 0.0001;
	int nmaxTrials = 1000;
	NumberFormat nf = NumberFormat.getInstance();
	
	ComputeBoseIntegralApp(){
		mua = new double[nmaxTrials];
		Ta = new double[nmaxTrials];
		Ea = new double[nmaxTrials];
		plotFrame.setPreferredMinMaxX(0, 10);
		plotFrame.setAutoscaleX(true);
		nf.setMaximumFractionDigits(3);
		plotFrame.setMarkerColor(0,Color.RED);
		plotFrame.setXYColumnNames(0,"T*", "\u03bc*");
	    energyFrame.setPreferredMinMax(0, 10, 0, 10);
	    energyFrame.setAutoscaleX(true);
	    energyFrame.setAutoscaleY(true);
	    energyFrame.setMarkerColor(0,Color.RED);
	    energyFrame.setXYColumnNames(0,"T*", "e*");

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
   
   public double evaluate2(double x){
	   if(x > 0) 
	   return x*x*x/(Math.exp(x)- 1);
	   else
		   return 0;
   }
   
   public double calculateE(double m, double t) {
	   
	   if(m == 0){
		   return 0.77*Math.pow(t,2.5);
	   }
	   else{
	       BoseEnergyIntegral f = new BoseEnergyIntegral();
	       f.b = 1/t;
	       f.mu = m;
	       return Integral.simpson(f, 0, 100*t, 20000, tolerance);
	   }
	  }
  
   
   public void calculate() {

      mu = control.getDouble("Guess for \u03bc*");      
      T = control.getDouble("T*");
      b = 1.0/T;
      double rhs;
      rhs = 0.432*Integral.simpson(this, 0, 100*T, 2000, tolerance);
      control.println( nf.format(rhs) + "\t \t" +  T  + "\t" + mu);	// integral 
        }
   
   
   public void accept(){
	   mua[npoints] = mu;
	   Ta[npoints] = T;
	   Ea[npoints] = calculateE(mu, T);
	   npoints++;
	   if(npoints > nmaxTrials){
		   control.print("Too many points");
		   System.exit(-1);
	   }
	   plot();
   }
   public void plot() {
		plotFrame.clearData(); 
		energyFrame.clearData(); 
  for(int i = 0; i<npoints; i++) {
     plotFrame.append(0, Ta[i], mua[i]);
     energyFrame.append(0, Ta[i], Ea[i]);
   }
   plotFrame.render();
   energyFrame.render();
 }
   
   public static void main(String[] args) {
	   CalculationControl control = CalculationControl.createApp(new ComputeBoseIntegralApp(),args);
	   control.addButton("accept", "Plot (\u03bc*,T*)");
   }
}

class BoseEnergyIntegral implements Function {
	  double b, mu;
      
      
	  public double evaluate(double x) {
	    return 0.432*Math.sqrt(x*x*x)/(Math.exp(b*(x-mu))-1);
	  }

	}
