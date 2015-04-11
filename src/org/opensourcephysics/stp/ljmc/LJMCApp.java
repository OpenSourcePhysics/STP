/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ljmc;
import org.opensourcephysics.controls.AbstractSimulation;
import org.opensourcephysics.controls.OSPCombo;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.stp.util.Rdf;

/**
 * LJParticlesApp simulates a two-dimensional system of interacting particles
 * via the Lennard-Jones potential.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05, 3/29/05
 */
public class LJMCApp extends AbstractSimulation {
  LJMC mc = new LJMC();
  DisplayFrame display = new DisplayFrame("Lennard-Jones system");
  PlotFrame grFrame = new PlotFrame("r", "g(r)", "Radial distribution function");
  PlotFrame pressure = new PlotFrame("t", "PA/NkT", "Pressure");
  Rdf gr = new Rdf();
  int timestep = 0;

  /**
   * Resets the LJ model to its default state.
   */
  public void reset() {
    OSPCombo combo = new OSPCombo(new String[] {"64", "144", "256"}, 0); // second argument is default
    control.setValue("number of particles", combo);
    control.setAdjustableValue("L", 18);
    control.setAdjustableValue("T", 1);
    control.setAdjustableValue("step size", 0.5);
    OSPCombo combo2 = new OSPCombo(new String[] {"triangular", "rectangular", "random"}, 0); // second argument is default
    control.setValue("initial configuration", combo2);
    enableStepsPerDisplay(true);
    super.setStepsPerDisplay(10);  // draw configurations every 10 steps
    display.setSquareAspect(true); // so particles will appear as circular disks
    resetData();

  }

  /**
   * Initializes the model by reading the number of particles.
   */
  public void initialize() {
    String number = control.getString("number of particles");
    if(number.equals("64")) {
      mc.N = 64;
    } else if(number.equals("144")) {
      mc.N = 144;
    } else {
      mc.N = 256;
    }
    mc.L = control.getDouble("L");
    mc.initialConfiguration = control.getString("initial configuration");
    mc.T = control.getDouble("T");
    mc.stepSize = control.getDouble("step size");
    mc.initialize();
    gr.initialize(mc.L, mc.L, 0.1);
    grFrame.setPreferredMinMax(0, 0.5*mc.L, 0, 10);
    grFrame.setAutoscaleY(true);
    display.addDrawable(mc);
    display.setPreferredMinMax(0, mc.L, 0, mc.L);
    pressure.setPreferredMinMax(0, 10, 0, 10);
    pressure.setAutoscaleX(true);
    pressure.setAutoscaleY(true);
    resetData();
  }

  /**
   * Reads adjustable parameters before the program starts running.
   */
  public void startRunning() {
    double L = control.getDouble("L");
    if((L!=mc.L)) {
      mc.L = L;
      display.setPreferredMinMax(0, L, 0, L);
      resetData();
    }
    mc.T = control.getDouble("T");
    mc.stepSize = control.getDouble("step size");
  }

  /**
   * Does a simulation step and appends data to the views.
   */
  public void doStep() {
    mc.oneMCstep();
    gr.append(mc.x, mc.y);
    gr.normalize();
    grFrame.clearData();
    grFrame.append(0, gr.rx, gr.ngr);
    grFrame.render();
    pressure.append(0, mc.mcs, mc.getMeanPressure());
    display.setMessage("mcs = " + mc.steps/mc.N);
  }

  /**
   * Prints the LJ model's data after the simulation has stopped.
   */
  public void stop() {
	control.println("mcs = " + mc.steps/mc.N);
    control.println("Mean potential energy per particle = "+decimalFormat.format(mc.totalPotentialEnergyAccumulator/(mc.mcs*mc.N)));
    control.println("Specific heat = "+decimalFormat.format(mc.getSpecificHeat()));
    control.println("PA/NkT = "+decimalFormat.format(mc.getMeanPressure()));
  }

  /**
   * Resets the LJ model and the data graphs.
   *
   * This method is invoked using a custom button.
   */
  public void resetData() {
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
    gr.reset();
    mc.steps = 0;
    mc.mcs = 0;
    mc.totalPotentialEnergyAccumulator = 0;
    mc.totalPotentialEnergyAccumulator2 = 0;
    mc.virialAccumulator = 0;
}

  /**
   * Returns an XML.ObjectLoader to save and load data for this program.
   *
   * LJParticle data can now be saved using the Save menu item in the control.
   *
   * @return the object loader
   */

  /**
   * Starts the Java application.
   * @param args  command line parameters
   */
  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new LJMCApp());
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
