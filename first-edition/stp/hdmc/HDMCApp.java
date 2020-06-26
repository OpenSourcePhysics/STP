/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.hdmc;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.stp.util.Rdf;
import org.opensourcephysics.controls.ControlUtils;


public class HDMCApp extends AbstractSimulation {
  HDMC mc = new HDMC();
  DisplayFrame display = new DisplayFrame("Hard Disks");
  PlotFrame grFrame = new PlotFrame("r", "g(r)", "Radial distribution function");
  Rdf gr = new Rdf();
  int timestep = 0;

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new HDMCApp(), args);
    control.addButton("resetData", "Reset Data");
  }

  public void reset() {
    OSPCombo combo = new OSPCombo(new String[] {"64", "144", "256"}, 0); // second argument is default
    control.setValue("number of particles", combo);
    control.setAdjustableValue("L", 40);
    control.setAdjustableValue("step size", 1.0);
    OSPCombo combo2 = new OSPCombo(new String[] {"hexagonal", "rectangular", "random"}, 1); // second argument is default
    control.setValue("initial configuration", combo2);
    control.setAdjustableValue("scale lengths", 1);
    enableStepsPerDisplay(true);
    super.setStepsPerDisplay(100); // draw configurations every 10 steps
    display.setSquareAspect(true); // so particles will appear as circular disks
    resetData();
  }

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
    mc.stepSize = control.getDouble("step size");
    mc.initialize();
    gr.initialize(mc.L, mc.L, 0.1);
    grFrame.setPreferredMinMax(0, 0.5*mc.L, 0, 10);
    grFrame.setAutoscaleY(true);
    display.addDrawable(mc);
    display.setPreferredMinMax(0, mc.L, 0, mc.L);
    resetData();
  }

  protected void doStep() {
    mc.s = control.getDouble("scale lengths");
    mc.oneMCstep();
    display.setMessage("mcs = " + mc.steps/mc.N);
    gr.append(mc.x, mc.y);
    gr.normalize();
    grFrame.clearData();
    grFrame.append(0, gr.rx, gr.ngr);
    grFrame.render();
  }

  public void stop() {
	double x = mc.N/(mc.equivL*mc.equivL);
    control.println("Density = " + ControlUtils.f4(x));
    x = 1.0*mc.accept/mc.steps;
    control.println("acceptance ratio = " + ControlUtils.f3(x));
    control.println("mcs = " + mc.steps/mc.N);
  }

  public void resetData() {
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
    gr.reset();
    mc.steps = 0;
    mc.mcs = 0;
    mc.accept= 0;
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
