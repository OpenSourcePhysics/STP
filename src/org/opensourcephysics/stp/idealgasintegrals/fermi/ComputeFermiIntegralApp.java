/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.idealgasintegrals.fermi;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.*;

import java.awt.Color;
import java.text.NumberFormat;

/**
 * ComputeFermiIntegralApp class.
 *
 *  @author Hui Wang
 *  @version 1.0   revised 07/19/06
 */
public class ComputeFermiIntegralApp extends AbstractCalculation implements Function {
  PlotFrame plotFrame = new PlotFrame("T*", "\u03bc*", ""); // ��
  PlotFrame energyFrame = new PlotFrame("T*", "e", "");     // ��
  double b = 1.0;
  double T = 1.0;
  double mu = 1.0;
  double rho = 0.5;
  double x_low = 0;
  double x_high = 1e2;
  int npoints = 0;
  double[] mua, Ta, Ea;
  double tolerance = 0.01;
  int nmaxTrials = 1000;
  NumberFormat nf = NumberFormat.getInstance();

  ComputeFermiIntegralApp() {
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
    control.setValue("T*", 1.0);
    control.setValue("\u03bc*", 1.0); // mu
	control.println("Calculated integral \t T* \t guess for \u03bc*");
	control.println("(= 1 if \u03bc* is correct)");
	npoints = 0;
  }

  public double evaluate(double x) {
    return 1.5*Math.sqrt(x)/(Math.exp(b*(x-mu))+1);
  }

  public void calculate() {
    mu = control.getDouble("\u03bc*");
    T = control.getDouble("T*");
    b = 1.0/T;
    rho = Integral.simpson(this, x_low, x_high, 2000, tolerance);
    //control.println();
    //control.println("\u03bc* = "+mu);              // mu
    //control.println("Calculated \u03c1 = " + rho);  //rho
    control.println( nf.format(rho) + "\t \t" +  T  + "\t" + mu);	// integral 
    //control.println("Calculated Integral = "+rho); // rho
    plot();
  }

  public double calculateE(double m, double t) {
    FermiEnergyIntegral f = new FermiEnergyIntegral();
    f.b = 1/t;
    f.mu = m;
    double e = Integral.simpson(f, x_low, x_high, 2000, tolerance);
    return e;
  }

  public void accept() {
    mua[npoints] = mu;
    Ta[npoints] = T;
    Ea[npoints] = calculateE(mu, T);
    npoints++;
    if(npoints>=nmaxTrials) {
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
    CalculationControl control = CalculationControl.createApp(new ComputeFermiIntegralApp());
    control.addButton("accept", "Plot (\u03bc*,T*)");
  }

}

class FermiEnergyIntegral implements Function {
  double b, mu;

  public double evaluate(double x) {
    return 1.5*Math.sqrt(x*x*x)/(Math.exp(b*(x-mu))+1);
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
