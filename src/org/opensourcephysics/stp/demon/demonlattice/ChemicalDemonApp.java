/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon.demonlattice;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.controls.ControlUtils;

public class ChemicalDemonApp extends AbstractSimulation {
  ChemicalDemon demon = new ChemicalDemon();
  PlotFrame ePlot = new PlotFrame("E", "ln (P(E))", "Demon Energy distribution");
  PlotFrame nPlot = new PlotFrame("N", "ln (P(N))", "Demon Number distribution");
  DisplayFrame display = new DisplayFrame("x", "p", "Phase Space Lattice");

  public void initialize() {
    demon.N = control.getInt("N");
    demon.L = control.getInt("L");
    demon.pmax = control.getInt("p_max");
    demon.E = control.getInt("E");
    demon.hc = control.getInt("hard core (set to 1 for hard core)");
    demon.well = control.getInt("attractive well depth");
    demon.initialize();
    display.addDrawable(demon);
    display.setSquareAspect(false);
    display.setPreferredMinMax(0, demon.L, -demon.Lp/2, demon.Lp/2);
    nPlot.setPreferredMinMaxX(0, 1);
    nPlot.setAutoscaleX(true);
  }

  public void stop() {
    demon.averages();
    control.println("mcs = "+demon.mcs);
    control.println("Mean demon energy = "+ControlUtils.f2(demon.eave)+",  Mean demon particle number = "+ControlUtils.f2(demon.nave));
    control.println("Mean system energy = "+ControlUtils.f2(demon.Eave)+",  Mean system particle number= "+ControlUtils.f2(demon.Nave));
    control.println("acceptance ratio = "+ControlUtils.f4(1.0*demon.accept/demon.steps));
  }

  public void reset() {
    control.setValue("N", 100);
    control.setValue("L", 100);
    control.setValue("p_max", 10);
    control.setValue("E", 200);
    control.setValue("hard core (set to 1 for hard core)", 0);
    control.setValue("attractive well depth", 0);
    control.setAdjustableValue("E for P(N,E)", 1);
    control.setAdjustableValue("N for P(N,E)", 1);
    control.setAdjustableValue("steps per display", 10);
    //    enableStepsPerDisplay(true);
  }

  public void doStep() {
    int stepsPerDisplay = control.getInt("steps per display");
    for(int i = 0; i<stepsPerDisplay; i++) {
      demon.demonStep();
    }
    display.setMessage("mcs = "+demon.mcs);
  }

  public void calculate() {
    ePlot.clearData();
    nPlot.clearData();
    int e = control.getInt("E for P(N,E)");
    int n = control.getInt("N for P(N,E)");
    for(int i = 0; i<demon.arraySize; i++) {
      if(demon.p[i][n]>0) {
        ePlot.append(0, i, Math.log(demon.p[i][n]*1.0/demon.steps));
      }
    }
    for(int i = 0; i<demon.arraySize; i++) {
      if(demon.p[e][i]>0) {
        nPlot.append(0, i, Math.log(demon.p[e][i]*1.0/demon.steps));
      }
    }
    ePlot.render();
    nPlot.render();
  }

  public void resetAverages() {
    demon.reset();
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new ChemicalDemonApp());
    control.addButton("calculate", "Plot Demon Distributions");
    control.addButton("resetAverages", "Reset Averages");
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
