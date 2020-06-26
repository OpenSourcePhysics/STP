/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.xymodel;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.PlotFrame;

/**
 * plot heat capacity and vorticity versus temperature of the xy model on a
 * square lattice
 *
 * @author Joshua Gould
 * @author Peter Sibley
 * @author Rongfeng Sun
 */
public class XYScanApp extends AbstractSimulation {
  XYModel xyModel = new XYModel();
  PlotFrame heatFrame = new PlotFrame("temperature", "C", "C(t) v T");
  PlotFrame vortexFrame = new PlotFrame("temperature", "vorticty", "Vorticity versus T");
  PlotFrame chiFrame = new PlotFrame("temperature", "Chi", "Chi versus T");
  int N;

  /**
   * Constructor XYScanApp
   */
  public XYScanApp() {
    xyModel = new XYModel();
  }

  public void initialize() {
    int dimensionoflattice = control.getInt("linear dimension of lattice");
    N = dimensionoflattice*dimensionoflattice;
    double initialtemperature = control.getDouble("start temperature");
    double deltatemperature = control.getDouble("temperature interval");
    int nequil = control.getInt("MC steps per spin for equilibrium");
    int mcs = control.getInt("MC steps per spin for data");
    double dThetaMax = control.getDouble("Maximum angle change");
    xyModel.initialConfiguration = control.getString("initial configuration");
    heatFrame.setAnimated(true);
    xyModel.setdeltatemperature(deltatemperature);
    xyModel.setLinearDimension(dimensionoflattice);
    xyModel.setKeepVorticity(true);
    xyModel.setMcs(mcs);
    xyModel.setNequil(nequil);
    xyModel.setDThetaMax(dThetaMax);
    xyModel.setTemperature(initialtemperature);
    xyModel.fillSpinsUp();
    xyModel.initialize();
  }

  public void reset() {
    control.setValue("linear dimension of lattice", 20);
    control.setValue("start temperature", 0.5);
    control.setAdjustableValue("temperature interval", 0.1);
    control.setValue("initial configuration", "ordered");
    control.setValue("MC steps per spin for equilibrium", 100);
    control.setValue("MC steps per spin for data", 10000);
    control.setValue("Maximum angle change", 1.5);
    enableStepsPerDisplay(true);
  }

  public static void main(String args[]) {
    SimulationControl.createApp(new XYScanApp(), args);
  }

  protected void doStep() {
    // TODO Auto-generated method stub
    double temperature = xyModel.getTemperature();
    temperature += xyModel.getdeltatemperature();
    xyModel.setTemperature(temperature);
    //control.println(String.valueOf(temperature));
    xyModel.initialize();
    xyModel.compute();
    heatFrame.append(0, temperature, (xyModel.getHeatCapacity()/N));
    vortexFrame.append(0, temperature, xyModel.getVorticity()/N);
    chiFrame.append(0, temperature, xyModel.getSusceptibility()/N);
    control.clearMessages();
    control.println("temperature = "+ControlUtils.f2(xyModel.getTemperature()));
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
