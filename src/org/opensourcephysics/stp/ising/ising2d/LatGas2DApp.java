/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.frames.*;

public class LatGas2DApp extends AbstractSimulation {
  LatGas2D ising;
  DisplayFrame displayFrame = new DisplayFrame("Particle Configuration");
  PlotFrame plotFrame = new PlotFrame("time", "E/N", "Thermodynamic Quantities");
  NumberFormat nf;
  double bondProbability;
  boolean metropolis = true;

  /**
   * Constructor Ising2DApp
   */
  public LatGas2DApp() {
    ising = new LatGas2D();
    plotFrame.setPreferredMinMaxX(0, 10);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    displayFrame.addDrawable(ising);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
 }

  public void initialize() {
    ising.initialize(control.getInt("Length"), control.getDouble("Temperature"), control.getInt("Number of particles"));
    ising.g = control.getDouble("Gravitational field");
    displayFrame.setPreferredMinMax(-5, ising.L+5, -5, ising.L+5);
    control.clearMessages();
  }


  public void doStep() {
    ising.setTemperature(control.getDouble("Temperature"));
    ising.setField(control.getDouble("Gravitational field"));
    ising.doOneMCStep();
    plotFrame.append(1, ising.mcs, (double) ising.E/ising.N);
  }

  public void stopRunning() {
    double norm = (ising.mcs==0)
                  ? 1
                  : 1.0/(ising.mcs*ising.N);
    control.println("mcs = "+ising.mcs);
    control.println("<E/N> = "+nf.format(ising.E_acc*norm/ising.N));
    control.println("Specific heat = "+nf.format(ising.specificHeat()));
    control.println("Acceptance ratio = "+nf.format(ising.acceptedMoves*norm));
    control.println();
  }

  public void reset() {
    control.setValue("Length", 32);
    control.setValue("Number of particles",512);
    control.setAdjustableValue("Temperature", nf.format(LatGas2D.criticalTemperature));
    control.setAdjustableValue("Gravitational field", 0);
    enableStepsPerDisplay(true);
  }

  public void zeroAverages() {
    control.clearMessages();
    ising.resetData();
    plotFrame.clearData();
    plotFrame.repaint();
  }



  void customize() {
    OSPFrame f = getMainFrame();
    if((f==null)||!f.isDisplayable()) {
      return;
    }
    addChildFrame(displayFrame);
    addChildFrame(plotFrame);
  }

  public static void main(String[] args) {
	LatGas2DApp app = new LatGas2DApp();
    SimulationControl control = SimulationControl.createApp(app, args);
    control.addButton("zeroAverages", "Zero averages");
    app.customize();
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
