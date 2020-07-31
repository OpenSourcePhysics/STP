/** Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.Ch06;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.*;

import java.awt.Color;
import java.text.NumberFormat;

/**
 * QuantumGasIntegralApp class.
 *
 *  @author Jan Tobochnik
 *  @version 1.0   6/17/20
 */
public class QuantumGasIntegralApp extends AbstractCalculation implements Function {
  PlotFrame plotFrame = new PlotFrame("T*", "\u03bc*", "Temperature Dependence of the Chemical Potential");
  PlotFrame energyFrame = new PlotFrame("T*", "e* = E*/NkT*", "Temperature Dependence of the Energy");  
  String particleType;
  double constant = 0.432;
  double beta = 1.0;
  double plusOrMinus1 = -1;
  double T = 1.0;
  double mu = 1.0;
  double rho = 0.5;
  double pow = 0.5;
  int npoints = 0;
  double[] mua, Ta, Ea;
  double tolerance = 0.001;
  int nmaxTrials = 1000;
  NumberFormat nf = NumberFormat.getInstance();

  QuantumGasIntegralApp() {
	  OSPRuntime.setAppClass(this);
    mua = new double[nmaxTrials];
    Ta = new double[nmaxTrials];
    Ea = new double[nmaxTrials];
    plotFrame.setPreferredMinMax(0, 10, 0, 10);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    energyFrame.setPreferredMinMax(0, 10, 0, 10);
    energyFrame.setAutoscaleX(true);
    energyFrame.setAutoscaleY(true);
    plotFrame.setMarkerColor(0,Color.RED);
    energyFrame.setMarkerColor(0,Color.RED);
    plotFrame.setXYColumnNames(0,"T*", "\u03bc*");
    energyFrame.setXYColumnNames(0,"T*", "e");
  }
  
 
  
  public void reset() {
       OSPCombo combo = new OSPCombo(new String[] {"Boson", "Fermion"}, 0); // second argument is default
       control.setValue("Particle type", combo);	   
       control.setValue("T*", 10.0);   
	   control.setValue("Guess for \u03bc* (negative for bosons)", -2.0);	//mu
	   control.println("Calculated integral \t T* \t guess for \u03bc*");
	   control.println("(= 1 if \u03bc* is correct)");
	   npoints=0;
   }
   
   public void calculate() {
	    particleType = control.getString("Particle type");
	    if(particleType.equals("Boson")) {
	    	plusOrMinus1 = -1;
	        constant = 0.432;
	    }
	    else{
	    	plusOrMinus1 = 1;
	        constant = 1.5;
	    } 
      mu = control.getDouble("Guess for \u03bc* (negative for bosons)");  
      if(particleType.equals("Boson")) mu = -Math.abs(mu);
      T = control.getDouble("T*");
      beta = 1.0/T;
      pow = 0.5;
      double rhs = Integral.simpson(this, 0, 100*T, 2000, tolerance);
      control.println( nf.format(rhs) + "\t \t" +  T  + "\t" + mu);	// integral 
   }
  

   public double evaluate(double x){
	   return constant*Math.pow(x,pow)/(Math.exp(beta*(x - mu)) + plusOrMinus1);
   }
   
   
   public double calculateE(double mu, double T) {
	   beta  = 1.0/T;
	   pow = 1.5;
	   return Integral.simpson(this, 0, 100*T, 2000, tolerance);
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
	   CalculationControl control = CalculationControl.createApp(new QuantumGasIntegralApp(),args);
	   control.addButton("accept", "Plot (\u03bc*,T*)");
   }
   
  }