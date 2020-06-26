/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.fpu;

/**
 * The Fermi-Pasta-Ulam model.
 *
 * @author Hui Wang
 * @version 1.0
 * @created 05/21/2007
 */
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import java.awt.Color;
import java.text.NumberFormat;

public class FPUApp extends AbstractSimulation {
  FPU fpu = new FPU();
  DisplayFrame displayFrame = new DisplayFrame("Displacements");
  PlotFrame energyFrame = new PlotFrame("time", "Energy", "Energy in different modes");
  PlotFrame metricFrame = new PlotFrame("time", "Inverse metric", "Metric versus time");
  int kmode;
  //Accumulator for energy in each mode
  double[] EkAccum = new double[4];
  int nAccum = 0;
  NumberFormat numberFormatFourDigits = NumberFormat.getInstance();

  public void doStep() {
    for(int i = 0; i<100; i++) {
      fpu.doStep();
    }
    fpu.getEnergy();
    if(kmode!=0) {
      int c = 0; // index for energy in each mode
      for(int i = kmode-1; i<kmode+2+1; i++) {
        if((i>=0)&&(i<fpu.N)) {
          energyFrame.append(c, fpu.t, fpu.Ek[i]);
          EkAccum[c] += fpu.Ek[i];
          nAccum++;
        }
        c++;
      }
    } else {
      energyFrame.append(1, fpu.t, fpu.Ek[1]);
    }
    metricFrame.append(0, fpu.t, 1.0/fpu.peMetric.metric);
    if((int) fpu.t%100000==0) {
      control.println("t = "+fpu.t);
      control.println("E/N = "+fpu.E/fpu.N);
      control.println("KE/N = "+fpu.KE/fpu.N);
      control.println();
    }
  }

  public void initialize() {
    int n;
    double beta;
    for(int i = 0; i<4; i++) {
      EkAccum[i] = 0;
    }
    nAccum = 0;
    n = control.getInt("N");
    double alpha = 0.25;
    beta = control.getDouble("b");
    kmode = control.getInt("mode");
    double dt = control.getDouble("\u0394t");
    double e0 = 0.1; // control.getDouble("e0");
    if(kmode<1) {
      control.println("Initial mode must be positive integer");
      return;
    }
    fpu.initialize(n, kmode, alpha, beta, dt, e0);
    displayFrame.addDrawable(fpu);
    control.clearMessages();
    displayFrame.setPreferredMinMax(-0.1*fpu.L, 1.1*fpu.L, -0.2*fpu.L, 0.2*fpu.L);
    energyFrame.setMarkerColor(1, Color.green);
    energyFrame.setMarkerColor(2, Color.blue);
    energyFrame.setMarkerColor(3, Color.red);
    energyFrame.setPreferredMinMax(0, 10, 0, 10);
    metricFrame.setPreferredMinMax(0, 10, 0, 10);
    energyFrame.setAutoscaleX(true);
    energyFrame.setAutoscaleY(true);
    metricFrame.setAutoscaleX(true);
    metricFrame.setAutoscaleY(true);
    this.delayTime = 0;
  }

  public void stop() {
    int k;
    k = kmode-1;
    if((k>0)&&(k<fpu.N)) {
      control.println("Average energy in mode "+k+" = "+numberFormatFourDigits.format(EkAccum[1]/nAccum));
    }
    k = kmode;
    if((k>0)&&(k<fpu.N)) {
      control.println("Average energy in mode "+k+" = "+numberFormatFourDigits.format(EkAccum[2]/nAccum));
    }
    k = kmode+1;
    if((k>0)&&(k<fpu.N)) {
      control.println("Average energy in mode "+k+" = "+numberFormatFourDigits.format(EkAccum[3]/nAccum));
    }
  }

  public void reset() {
    control.setValue("N", "18");
    control.setValue("mode", 1);
    control.setValue("b", 0.25);
    control.setValue("\u0394t", 0.05);
    this.delayTime = 0;
    energyFrame.clearData();
    metricFrame.clearData();
    fpu.t = 0;
    enableStepsPerDisplay(true);
  }

  public void resetMetric() {
    metricFrame.clearData();
    energyFrame.clearData();
    fpu.resetMetric();
    for(int i = 0; i<4; i++) {
      EkAccum[i] = 0;
    }
    nAccum = 0;
  }

  /**
   * Constructor FPUApp
   */
  public FPUApp() {
    numberFormatFourDigits.setMinimumFractionDigits(4);
    metricFrame.setMarkerColor(0,Color.RED);
    
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new FPUApp());
    //control.addButton("resetMetric","Reset Metric");
    control.addButton("resetMetric", "Zero averages");
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
