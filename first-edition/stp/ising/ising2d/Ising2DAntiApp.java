/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;

public class Ising2DAntiApp extends AbstractSimulation {
  Ising2DAnti ising;
  DisplayFrame displayFrame = new DisplayFrame("Spin Configuration");
  PlotFrame plotFrame = new PlotFrame("time", "E(Blue),M(Black),M_S(Red)", "Instantaneous Thermodynamic Quantities)");
  NumberFormat nf;

  /**
   * Constructor Ising2DAntiApp
   */
  public Ising2DAntiApp() {
    ising = new Ising2DAnti();
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    displayFrame.addDrawable(ising);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
  }

  public void initialize() {
    ising.J = -1.0; // initialize to be anti-ferromagnetic
    ising.initialize(control.getInt("Length"), control.getDouble("Temperature"), control.getDouble("External field"));
    displayFrame.setPreferredMinMax(-5, ising.L+5, -5, ising.L+5);
    control.clearMessages();
    plotFrame.clearData();
    plotFrame.repaint();
    displayFrame.repaint();
  }

  public void doStep() {
    ising.setTemperature(control.getDouble("Temperature"));
    ising.setExternalField(control.getDouble("External field"));
    ising.doOneMCStep();
    plotFrame.append(0, ising.mcs, (double) ising.M/ising.N);
    plotFrame.append(1, ising.mcs, (double) ising.E/ising.N);
    plotFrame.append(2, ising.mcs, (double) ising.getStaggeredM()/ising.N);
       plotFrame.repaint();
    displayFrame.repaint();
  }
    
    public void stopRunning() {
    double norm = 1.0/(ising.mcs*ising.N);
    control.println("mcs = "+ising.mcs);
    control.println("<E> = "+nf.format(ising.E_acc*norm));
    control.println("Specific heat = "+nf.format(ising.specificHeat()));
    control.println("<M> = "+nf.format(ising.M_acc*norm));
    control.println("Susceptibility = "+nf.format(ising.susceptibility()));
    control.println("Staggered susceptibility = "+nf.format(ising.Staggeredsusceptibility()));
    control.println("Staggered magnetization = "+nf.format(ising.Sm_acc*norm));
       control.println("Acceptance ratio = "+nf.format(ising.acceptedMoves*norm));
  }

  public void reset() {
    control.setAdjustableValue("Length", 32);
    control.setAdjustableValue("Temperature", nf.format(Ising2D.criticalTemperature));
    control.setAdjustableValue("External field", 0);
    enableStepsPerDisplay(true);
  }

  public void cleardata() {
    control.clearMessages();
    ising.resetData();
    plotFrame.clearData();
    plotFrame.repaint();
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new Ising2DAntiApp(), args);
    control.addButton("cleardata", "Zero averages");
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
