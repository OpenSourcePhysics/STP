/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.xymodel;
import java.text.DecimalFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;

/**
 * Animation for XY model that displays the lattice
 *
 * @author Joshua Gould
 * @author Peter Sibley
 * @author Rongfeng Sun
 */
public class XYAnimationApp extends AbstractSimulation {
  XYModel xy = new XYModel();
  DisplayFrame displayFrame = new DisplayFrame("lattice");
  PlotFrame correlationPlot = new PlotFrame("r","<s(r)s(0)>","Spin-spin correlation function");
   boolean resetFlag = true;
  DecimalFormat numberFormatTwoDigits = (DecimalFormat) DecimalFormat.getInstance();
  DecimalFormat numberFormatFourDigits = (DecimalFormat) DecimalFormat.getInstance();
  int N;
  boolean showVortices = false;

  /**
   * Constructor XYAnimationApp
   */
  public XYAnimationApp() {
    numberFormatTwoDigits.setMaximumFractionDigits(2);
    numberFormatTwoDigits.setMinimumFractionDigits(2);
    numberFormatTwoDigits.setGroupingSize(100);
    // never show commas so we set grouping size to a big number.
    numberFormatFourDigits.setMaximumFractionDigits(4);
    numberFormatFourDigits.setMinimumFractionDigits(4);
    numberFormatFourDigits.setGroupingSize(100);
    xy = new XYModel();
  }

  public void initialize() {
    int L = control.getInt("linear dimension of lattice");
    N = L*L;
    double T = control.getDouble("temperature");
    xy.setTemperature(T);
    //int nequil = control.getInt("MC steps per spin for equilibrium");
    //int mcs = control.getInt("MC steps per spin for data");
    double dThetaMax = control.getDouble("Maximum angle change");
    long seed = Math.abs((int) System.currentTimeMillis());
    xy.initialConfiguration = control.getString("initial configuration");
      xy.setSeed(seed);
      xy.setLinearDimension(L);
      //xy.setNequil(nequil);
      xy.initialize();
      displayFrame.clearData();
      displayFrame.addDrawable(xy);
      displayFrame.repaint();
      xy.setDThetaMax(dThetaMax);
    //xy.setMcs(mcs);
  }

  public void stop() {
    output();
  }

  public void output() {
    control.clearMessages();
    control.println("number of MC steps = "+xy.getTime());
    control.println("temperature = "+numberFormatTwoDigits.format(xy.getTemperature()));
    //control.println("energy = "+numberFormatFourDigits.format(xy.getTotalEnergy()));
    control.println("acceptance probability = "+numberFormatTwoDigits.format(xy.getAcceptanceProbability()));
    control.println("<E/N> = "+numberFormatFourDigits.format(xy.getMeanEnergy()/N));
    //control.println("<E*E> = "+numberFormatFourDigits.format(xy.getMeanEnergySquared()));
    control.println("<M_x/N> = "+numberFormatFourDigits.format(xy.getMeanMagnetizationX()/N));
    control.println("<M_y/N> = "+numberFormatFourDigits.format(xy.getMeanMagnetizationY()/N));
    //control.println("<M*M> = "+numberFormatFourDigits.format(xy.getMeanMagnetizationSquared()));
    //control.println("# of vortices = "+xy.getTotalNumberOfVortices());
    control.println("vorticity = "+numberFormatFourDigits.format(xy.getVorticity()));
    //control.println("<# of vortices> = "+numberFormatTwoDigits.format(xy.getMeanNumberOfVortices()));
    control.println("specific heat = "+numberFormatTwoDigits.format(xy.getHeatCapacity()/N));
    control.println("susceptibility = "+numberFormatTwoDigits.format(xy.getSusceptibility()/N));
    displayFrame.repaint();
  }

  public void reset() {
    control.clearMessages();
    control.setValue("linear dimension of lattice", 20);
    control.setAdjustableValue("temperature", 0.89);
    //control.setValue("MC steps per spin for equilibrium", 20);
    //control.setValue("MC steps per spin for data", 10000);
    control.setValue("Maximum angle change", 6.28);
    //control.setValue("seed", Math.abs((int) System.currentTimeMillis()));
    OSPCombo combo2 = new OSPCombo(new String[] {"random","ordered"}, 0); // second argument is default
    control.setValue("initial configuration", combo2);
    //control.setValue("initial configuration", "random");
    resetFlag = true;
    enableStepsPerDisplay(true);
  }

  public void showVortex() {
	showVortices = !showVortices;
    xy.showVortex(showVortices);
    displayFrame.repaint();
  }

  public void hideVortex() {
    xy.showVortex(false);
    displayFrame.repaint();
  }
  
  public void xeroAverages() {
	  xy.clearData();
	  double T = control.getDouble("temperature");
	  xy.setTemperature(T);
  }
  
  

  public static void main(String args[]) {
    SimulationControl control = SimulationControl.createApp(new XYAnimationApp(), args);
    control.addButton("showVortex", "Show/Hide Vortices");
    control.addButton("xeroAverages", "Xero Averages");
   }

  protected void doStep() {
       xy.step();
       xy.correlationFunction();
       correlationPlot.clearData();
       for(int r = 0; r < xy.L/2; r++) {
    	   correlationPlot.append(0,r,xy.correlation[r]/xy.norm[r]);
       }
       
       correlationPlot.setMessage("mcs = " + xy.getTime());
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
