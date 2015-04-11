/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.partfunccalc;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;

public class PartFuncCalcApp extends AbstractSimulation {
  PartFuncCalc ising = new PartFuncCalc();
  private PlotFrame plotFrame = new PlotFrame("T", "[ln Z(T)-ln Z(0.1)]/N", "Partition Function");
  private PlotFrame plotFrame2 = new PlotFrame("T", "H[T]", "Temperature Histogram");
  private LatticeFrame lattice = new LatticeFrame("Ising Model");
  public int displayDelay = 100;
  public int n = 0;

  /**
   * Constructor PartFuncCalcApp
   */
  public PartFuncCalcApp() {
    plotFrame.setPreferredMinMaxX(0, 10);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    plotFrame2.setPreferredMinMax(0, 10, 0, 10);
    plotFrame2.setAutoscaleX(true);
    plotFrame2.setAutoscaleY(true);
  }

  public void initialize() {
    ising.initial(control.getInt("L"), control.getDouble("prob"), lattice);
    displayDelay = control.getInt("display delay");
  }

  public void doStep() {
    int mcs = (int) (100*ising.N/Math.sqrt(ising.lnf));
    displayDelay = control.getInt("display delay");
    for(int i = 0; i<mcs; i++) {
      if(i%displayDelay==0) {
        lattice.render();
      }
      ising.doMcStep();
    }
    control.println("ln f = "+ControlUtils.f4(ising.lnf));
    //System.out.print("logZ latest = "+ControlUtils.f4(ising.lnZ[ising.currenttempindex]));
    control.println(" E accept = "+ControlUtils.f4(ising.acceptEnergy/(ising.tryEnergy)));
    control.println(" T accept = "+ControlUtils.f4(ising.acceptTemperature/(ising.tryTemperature)));
    plotFrame.clearData();
    plotFrame2.clearData();
    for(int j = 0; j<ising.Tsize; j++) {
      if(ising.lnZ[j]>0) {
        plotFrame.append(0, ising.T[j], (ising.lnZ[j]/ising.N)-(ising.lnZ[0]/ising.N));
      }
      if(ising.H[j]>0) {
        plotFrame2.append(0, ising.T[j], ising.H[j]*ising.increment[j]);
      }
    }
    ising.lnf /= Math.sqrt(10.0);
    ising.resetData();
    if(ising.lnf<1.0E-9) {
      System.out.println("done");
      stopSimulation();
    }
  }

  public void reset() {
    control.setValue("L", 32);
    control.setValue("prob", 0.01);
    control.setAdjustableValue("display delay", 10000);
    //   enableStepsPerDisplay(true); // allow user to speed up simulation
  }

  public void resetData() {
    ising.resetData();
    plotFrame.clearData();
    plotFrame2.clearData();
    plotFrame.repaint();
    plotFrame2.repaint();
    control.clearMessages();
  }

  public static void main(String[] args) {
    SimulationControl.createApp(new PartFuncCalcApp());
    //   control.addButton("resetData", "Reset Data");
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
