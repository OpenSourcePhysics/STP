/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.idealgas;
import org.opensourcephysics.stp.util.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;

public class IdealGasApp extends AbstractSimulation {
  PlotFrame densityPlot;
  PlotFrame histogramPlot;
  int mcs;      // Number of Monte-Carlo steps performed, per particle
  int N;        // Number of particles
  double[] v;   // v[i] is the i'th particle's velocity (the total particle
  // specification)
  double vdel;  // Maximum perturbation to a velocity
  double E;     // Energy of current velocity configuration
  double E_max; // Upper bound of allowed energy
  int E_bins;   // Number of energy bins; length of g[] and H[]
  int[] H;      // Log of histogram; when this becomes flat, reduce f, zero H[],
  // and continue
  double[] g;   // ln (g(E)), where g(E) is the density of states as a function
  // of energy
  double f;     // The granularity of updates to g(E); decreases with time.

  int energyIndex(double e) {
    return(int) (e*E_bins/E_max);
  }

  void perturbParticles() {
    for(int iter = 0; iter<N; iter++) {
      // Find an arbitrary perturbation
      int r = MathPlus.randomInt(0, N-1);      // Random particle index
      double p = MathPlus.random(-vdel, vdel); // Perturbation of r'th
      // particle
      double dE = p*p+2*p*v[r];                // dE = (v+p)^2 - v^2
      // Optionally accept perturbation
      if(E+dE<E_max) {
        if(Math.random()<Math.exp(g[energyIndex(E)]-g[energyIndex(E+dE)])) {
          v[r] += p;
          E += dE;
        }
      }
      // Always update density of states and histogram
      g[energyIndex(E)] += Math.log(f);
      H[energyIndex(E)] += 1;
    }
  }

  boolean isFlat() {
    double avgH = (double) sum(H)/E_bins;
    for(int e = 0; e<E_bins; e++) {
      if((H[e]==0)||(H[e]<0.8*avgH)||(H[e]>1.2*avgH)) {
        return false;
      }
    }
    return true;
  }

  int sum(int H[]) {
    int s = 0;
    for(int e = 0; e<E_bins; e++) {
      s += H[e];
    }
    return s;
  }

  public void doStep() {
    int mcsMax = mcs+Math.max(10000/N, 1);
    for(; mcs<mcsMax; mcs++) {
      perturbParticles();
    }
    if(isFlat() && f > 1.0001) {
      f = Math.sqrt(f);
      control.println("f = "+ControlUtils.f4(f)  + "   mcs = "+mcs/N);
    }
    densityPlot.clearData();
    densityPlot.setMessage("mcs = " + +mcs/N);
    histogramPlot.clearData();
    for(int e = 0; e<E_bins; e++) {
      densityPlot.append(0, Math.log(e*E_max/E_bins), g[e]);
      histogramPlot.append(0, e*E_max/E_bins, H[e]);
    }
  }

  public void initialize() {
    mcs = 0;
    N = control.getInt("num. particles")*control.getInt("num. dimensions");
    v = new double[N]; // Initialized to [0]
    E = 0;
    E_bins = control.getInt("num. energy bins");
    E_max = control.getDouble("maximum energy");
    vdel = control.getDouble("\u2206 velocity");
    control.clearMessages();
    H = new int[E_bins]; // Initialized to [0]
    g = new double[E_bins];
    f = Math.exp(1);
    control.println("f = "+ControlUtils.f4(f));
  }

  public void reset() {
    control.setValue("num. particles", 4);
    control.setValue("num. dimensions", 1);
    control.setValue("num. energy bins", 128);
    control.setValue("maximum energy", 10);
    control.setValue("\u2206 velocity", 0.4);
    densityPlot.clearDataAndRepaint();
    histogramPlot.clearDataAndRepaint();
    enableStepsPerDisplay(true);
  }

  /**
   * Constructor IdealGasApp
   */
  public IdealGasApp() {
    densityPlot = new PlotFrame("ln E", "ln \u03C9(E)", "Density of States for an Ideal Gas");
    densityPlot.setAnimated(true);
    histogramPlot = new PlotFrame("E", "H(E)", "Histogram for an Ideal Gas");
    histogramPlot.setAnimated(true);
  }

  public static void main(String[] args) {
    SimulationControl.createApp(new IdealGasApp(), args);
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
