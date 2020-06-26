/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */
 
package org.opensourcephysics.stp.widom;
import java.text.DecimalFormat;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.display.GUIUtils;

/**
 * WidomApp simulates a two-dimensional system of interacting particles
 * via the Lennard-Jones potential.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05, 3/29/05
 */
public class WidomApp extends AbstractSimulation {
  Widom mc = new Widom();
  //PlotFrame pressureData = new PlotFrame("mcs", "P- P_ideal", "Mean pressure");
  //PlotFrame energyData = new PlotFrame("mcs", "PE", "Mean potential energy");
  PlotFrame cpData = new PlotFrame("mcs", "mu*", "Mean excess chemical potential");
  DisplayFrame display = new DisplayFrame("x", "y", "Lennard-Jones system");

  /**
   * Initializes the model by reading the number of particles.
   */
  public void initialize() {
	decimalFormat = new DecimalFormat("#.###");
    mc.temperature = control.getDouble("temperature");
    mc.ds = 2.0*control.getDouble("maximum change \u03B4");
    mc.Lx = control.getDouble("L");
    mc.Ly = control.getDouble("L");
    mc.N = control.getInt("number of particles");
    mc.initialize();
    display.addDrawable(mc);
    display.setPreferredMinMax(0, mc.Lx, 0, mc.Ly);
    //pressureData.setPreferredMinMax(0, 10, 0, 10);
    //pressureData.setAutoscaleX(true);
    //pressureData.setAutoscaleY(true);
    //energyData.setPreferredMinMax(0, 10, 0, 10);
    //energyData.setAutoscaleX(true);
    //energyData.setAutoscaleY(true);
    cpData.setPreferredMinMax(0, 10, 0, 10);
    cpData.setAutoscaleX(true);
    cpData.setAutoscaleY(true);
    for(int i = 0; i < 100; i++) mc.step();
    resetData();
  }

  /**
   * Does a simulation step and appends data to the views.
   */
  public void doStep() {
    mc.step();
    //pressureData.append(0, mc.steps, mc.getMeanPressure());
    //energyData.append(0, mc.steps, mc.getMeanEnergy());
    cpData.append(0, mc.steps, mc.getMeanChemicalPotential());
  }

  /**
   * Prints the LJ model's data after the simulation has stopped.
   */
  public void stop() {
    control.println("Density = "+decimalFormat.format(mc.rho));
    control.println("Number of MC steps per particle = "+mc.steps);
    control.println("<E> = "+decimalFormat.format(mc.getMeanEnergy()));
    control.println("Specific heat = "+decimalFormat.format(mc.getHeatCapacity()));
    control.println("<P - P(ideal)> = "+decimalFormat.format(mc.getMeanPressure()));
    control.println("<mu*> = "+decimalFormat.format(mc.getMeanChemicalPotential()));
    control.println("acceptance ratio = "+decimalFormat.format(mc.accept/(mc.steps*mc.N)));
  }

  /**
   * Reads adjustable parameters before the program starts running.
   */
  public void startRunning() {
    double Lx = control.getDouble("L");
    double Ly = control.getDouble("L");
    if((Lx!=mc.Lx)||(Ly!=mc.Ly)) {
      mc.Lx = Lx;
      mc.Ly = Ly;
      display.setPreferredMinMax(0, Lx, 0, Ly);
      resetData();
    }
  }

  /**
   * Resets the LJ model to its default state.
   */
  public void reset() {
	control.setValue("number of particles", 100);
	control.setAdjustableValue("L", 20.0);
    //control.setAdjustableValue("Ly", 30.0);
    control.setValue("temperature", 1.0);
    control.setValue("maximum change \u03B4", 0.2);
    enableStepsPerDisplay(true);
    setStepsPerDisplay(10);        // draw configurations every 10 steps
    display.setSquareAspect(true); // so particles will appear as circular disks
  }

  /**
   * Resets the LJ model and the data graphs.
   *
   * This method is invoked using a custom button.
   */
  public void resetData() {
    mc.resetAverages();
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this program.
   *
   * LJParticle data can now be saved using the Save menu item in the control.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new WidomLoader();
  }

  /**
   * Starts the Java application.
   * @param args  command line parameters
   */
  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new WidomApp());
    control.addButton("resetData", "Reset Data");
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
