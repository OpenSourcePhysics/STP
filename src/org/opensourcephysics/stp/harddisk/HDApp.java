/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.harddisk;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.stp.util.Rdf;;

/**
 * Simulates hard disks.
 *
 * @author Jan Tobochnik
 * @author Joshua Gould
 * @author Peter Sibley
 * @created December 26, 2002
 */
public class HDApp extends AbstractSimulation {
  PlotFrame temperatureFrame = new PlotFrame("time", "Temperature", "Temperature versus time");
  PlotFrame pressureFrame = new PlotFrame("time", "pressure", "Pressure versus time");
  DisplayFrame displayFrame = new DisplayFrame("HD Display");
  HistogramFrame histogramFrame = new HistogramFrame("v_x", "H(v_x)", "H(v_x) versus v_x");
  Rdf gr = new Rdf();
  PlotFrame grFrame = new PlotFrame("r", "g(r)", "Radial distribution function");
  HD hd;
  NumberFormat numberFormatTwoDigits = NumberFormat.getInstance();
  NumberFormat numberFormatFourDigits = NumberFormat.getInstance();

  /**
   * Constructor HDApp
   */
  public HDApp() {
    numberFormatTwoDigits.setMaximumFractionDigits(2);
    numberFormatTwoDigits.setMinimumFractionDigits(2);
    numberFormatFourDigits.setMaximumFractionDigits(4);
    numberFormatFourDigits.setMinimumFractionDigits(4);
    hd = new HD();
    displayFrame.setPreferredMinMax(-0.1*hd.Lx, 1.1*hd.Lx, -0.1*hd.Lx, 1.1*hd.Lx);
    displayFrame.addDrawable(hd);
    temperatureFrame.setPreferredMinMaxX(0, 10);
    temperatureFrame.setAutoscaleX(true);
    temperatureFrame.setPreferredMinMaxY(0, 10);
    temperatureFrame.setAutoscaleY(true);
    pressureFrame.setPreferredMinMaxX(0, 10);
    pressureFrame.setAutoscaleX(true);
    pressureFrame.setPreferredMinMaxY(0, 2);
    histogramFrame.setAutoscaleX(true);
    histogramFrame.setPreferredMinMaxY(0, 10);
    histogramFrame.setAutoscaleY(true);
    histogramFrame.addDrawable(hd.getVelocityHistogram());
    grFrame.setPreferredMinMaxX(0, 10);
    grFrame.setAutoscaleX(true);
    grFrame.setPreferredMinMaxY(0, 10);
    grFrame.setAutoscaleY(true);
  }

  public void initialize() {
    hd.setNumberOfDisks(control.getInt("N"));
    hd.setLx(control.getDouble("Lx"));
    hd.setLy(control.getDouble("Ly"));
    hd.setVelocityMax(control.getDouble("temperature"));
    hd.initialConfiguration = control.getString("initial configuration");
    hd.initialize();
    temperatureFrame.clearData();
    pressureFrame.clearData();
    gr.initialize(hd.Lx, hd.Ly, 0.1);
    displayFrame.setPreferredMinMax(-0.1*hd.Lx, 1.1*hd.Lx, -0.1*hd.Lx, 1.1*hd.Lx);
    //      renderPanels();
  }

  public void renderPanels() {
    temperatureFrame.append(0, hd.getTime(), hd.getInstantaneousTemperature());
    pressureFrame.append(0, hd.getTime(), hd.getInstantanousPressure());
    displayFrame.setPreferredMinMax(-0.1*hd.Lx, 1.1*hd.Lx, -0.1*hd.Lx, 1.1*hd.Lx);
    histogramFrame.render();
  }

  public void doStep() {
    hd.step();
    gr.append(hd.positionX, hd.positionY);
    gr.normalize();
    grFrame.clearData();
    grFrame.append(0, gr.rx, gr.ngr);
    grFrame.render();
    renderPanels();
  }

  public void reset() {
    control.clearMessages();
    control.setValue("N", 64);
    control.setValue("Lx", 18.0);
    control.setValue("Ly", 18.0);
    control.setValue("temperature", 1.0);
    OSPCombo combo = new OSPCombo(new String[] {"crystal", "random"}, 0); // second argument is default
    control.setValue("initial configuration", combo);
    gr.reset();
    this.delayTime = 0;
    enableStepsPerDisplay(true);
  }

  public void zeroAverages() {
    hd.zeroAverages();
    control.clearMessages();
  }

  public void stop() {
    output();
  }

  public void output() {
    //control.println("Density = " + numberFormatFourDigits.format(hd.rho));
    control.println("Temperature = "+numberFormatFourDigits.format(hd.getTemperature()));
    control.println("<PA/NkT> = "+numberFormatFourDigits.format(hd.getMeanPressure()));
    control.println("Mean free path = "+numberFormatFourDigits.format(hd.getMFP()));
    control.println("Mean collision time = "+numberFormatFourDigits.format(hd.getMFT()));
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new HDApp(), args);
    control.addButton("zeroAverages", "Zero averages");
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
