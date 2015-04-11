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
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.DrawingPanel;


/**
 * IsingTriApp class.
 *
 *  @author Hui Wang
 *  @version 1.0   revised 11/26/06
 */
public class IsingTriangularAntiApp extends AbstractSimulation {
  IsingTriangularAnti ising;
  DrawingPanel drawingPanel;
  DrawingFrame drawingFrame;
   DrawingFrame plottingFrame;
  PlotFrame plotFrame = new PlotFrame("time", "E and M", "Thermodynamic Quantities");
  NumberFormat nf;

  /**
   * Constructor IsingTriangularAntiApp
   */
  public IsingTriangularAntiApp() {
    ising = new IsingTriangularAnti();
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    drawingPanel = new DrawingPanel();
    drawingPanel.addDrawable(ising);
    drawingFrame = new DrawingFrame("Spin Configuration", drawingPanel);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
  }

  public void initialize() {
    ising.temperature = control.getDouble("temperature");
    ising.initialize(control.getInt("L"));
    ising.resetData();
    plotFrame.clearData();
    plotFrame.repaint();
    drawingPanel.setPreferredMinMax(-5, ising.L+5, -5, ising.L+5);
    drawingPanel.repaint();
    control.clearMessages();
  }

  public void doStep() {
    ising.doOneMCStep();
    plotFrame.append(0, ising.mcs, ising.magnetization*1.0/ising.N);
    plotFrame.append(1, ising.mcs, ising.energy*1.0/ising.N);
     drawingPanel.repaint();
  }

  public void stop() {
    double norm = 1.0/(ising.mcs*ising.N);
    control.println("mcs = "+ising.mcs);
    control.println("acceptance ratio = "+ControlUtils.f2(ising.acceptedMoves*norm));
    control.println("<E> = "+ControlUtils.f3(ising.energyAccumulator*norm));
    control.println("Specific heat = "+ControlUtils.f3(ising.specificHeat()));
    control.println("<M> = "+ControlUtils.f3(ising.magnetizationAccumulator*norm));
    control.println("Susceptibility = "+ControlUtils.f3(ising.susceptibility()));
  }

  public void reset() {
    control.setValue("L", 32);
    control.setAdjustableValue("temperature", 4.0); // IsingTriangularAnti.criticalTemperature);
    initialize();
    enableStepsPerDisplay(true);
  }

  public static void main(String[] args) {
    IsingTriangularAntiApp app = new IsingTriangularAntiApp();
    SimulationControl control = new SimulationControl(app);
    control.setTitle("Anti-Ferromagnetic Ising Model on a Hexagonal Lattice");
    app.setControl(control);
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
