/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.wanglandau;
import java.awt.Color;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.frames.PlotFrame;

public class WangLandauApp extends AbstractSimulation {
  PlotFrame histogramFrame = new PlotFrame("E", "H(E)", "Histogram"),
            densityFrame = new PlotFrame("E", "ln g(E)", "Density of States"),
            heatFrame = new PlotFrame("T", "C(T)", "Heat Capacity"),
            pEnergyFrame = new PlotFrame("E", "P(E)", "Energy Distribution");
   // energy of current spin configuration (translated by 2N), number of
  // reductions to f
  int mcs, L, N, E, iterations;
  double[] g;    // logarithm of the density of states (energy argument
  // translated by 2N)
  int[] H, spin; // histogram (reduce f when it is "flat")
  double f;      // multiplicative modification factor to g

  int sumNeighbors(int i) {
    int u = i-L;
    int d = i+L;
    int l = i-1;
    int r = i+1;
    if(u<0) {
      u += N;
    }
    if(d>=N) {
      d -= N;
    }
    if(i%L==0) {
      l += L;
    }
    if(r%L==0) {
      r -= L;
    }
    return spin[u]+spin[d]+spin[l]+spin[r];
  }

  public void variable_initialize() {
    mcs = 0;
    N = L*L;
    f = Math.exp(1);
    iterations = 0;
    spin = new int[N];
    for(int i = 0; i<N; i++) {
      spin[i] = (Math.random()<0.5)
                ? 1
                : -1;
    }
    g = new double[4*N+1];
    H = new int[4*N+1];
    for(int e = 0; e<=4*N; e++) {
      g[e] = 0;
      H[e] = 0;
    }
    E = 0;
    for(int i = 0; i<N; i++) {
      E += -spin[i]*sumNeighbors(i);
    }
    E /= 2;   // we double counted all interacting pairs
    E += 2*N; // translate energy by 2*N to facilitate array access
  }

  void flipSpins() {
    for(int steps = 0; steps<N; steps++) {
      int i = (int) (N*Math.random());
      int dE = 2*spin[i]*sumNeighbors(i);
      if(Math.random()<Math.exp(g[E]-g[E+dE])) {
        spin[i] = -spin[i];
        E += dE;
      }
      g[E] += Math.log(f);
      H[E] += 1;
    }
  }

  boolean isFlat() {
    int netH = 0;
    double numEnergies = 0;
    for(int e = 0; e<=4*N; e += 4) {
      if(H[e]>0) {
        netH += H[e];
        numEnergies++;
      }
    }
    for(int e = 0; e<=4*N; e += 4) {
      if((0<H[e])&&(H[e]<0.8*netH/numEnergies)) {
        return false;
      }
    }
    return true;
  }

  public void doStep() {
	double beta = 1.0/control.getDouble("T for P(E)");
    int mcsMax = mcs+Math.max(100000/N, 1);
    for(; mcs<mcsMax; mcs++) {
      flipSpins();
    }
    histogramFrame.clearData();
    densityFrame.clearData();
    pEnergyFrame.clearData();
    double z = 0;
    for(int e = 0; e<=4*N; e += 4) {
      if(g[e]>0) {
        densityFrame.append(0, e-2*N, g[e]-g[0]);
        histogramFrame.append(0, e-2*N, H[e]);
        z += Math.exp(g[e]-g[0])*Math.exp(-beta*(e-2*N));
      }
    }
    histogramFrame.setMessage("mcs = "+mcs);
    for(int e = 0; e<=4*N; e += 4) {
        if(g[e]>0) {
          pEnergyFrame.append(0,e-2*N, Math.exp(g[e]-g[0])*Math.exp(-beta*(e-2*N))/z);
        }
      }
   
    heatFrame.clearData();
    for(double T = 0.5; T<6; T += 0.1) {
      heatFrame.append(0, T, Thermodynamics.heatCapacity(N, g, 1/T)/N);
    }
    for(double T = 1.9; T<2.7; T += 0.02) {
      heatFrame.append(0, T, Thermodynamics.heatCapacity(N, g, 1/T)/N);
    }
    if(isFlat() && f>1.0001) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(4);
      f = Math.sqrt(f);
      control.println("mcs = "+mcs);
      control.println("iteration = "+iterations);
      control.println("f = "+nf.format(f));
      iterations++;
      for(int e = 0; e<=4*N; e += 4) {
        H[e] = 0;
      }
    }
  }

  public void initialize() {
    L = control.getInt("L");
    variable_initialize();
    histogramFrame.clearData();
    densityFrame.clearData();
    heatFrame.clearData();
    control.clearMessages();
  }

  public void reset() {
	control.setValue("L", 16);
	control.setAdjustableValue("T for P(E)", 2.5);
    variable_initialize();
    enableStepsPerDisplay(true);
  }

  /**
   * Constructor WangLandauApp
   */
  public WangLandauApp() {
    histogramFrame.setPreferredMinMaxY(0, 10000);
    histogramFrame.setAutoscaleY(true);
    densityFrame.setMarkerColor(0, Color.blue);
    densityFrame.setPreferredMinMaxY(0, 10000);
    densityFrame.setAutoscaleY(true);
    heatFrame.setMarkerColor(0, Color.red);
    heatFrame.setPreferredMinMax(0.5, 6, 0, 1000);
    heatFrame.setAutoscaleX(true);
    heatFrame.setAutoscaleY(true);
    heatFrame.setMarkerShape(0, Dataset.NO_MARKER);
    heatFrame.getDataset(0).setSorted(true);
    heatFrame.setConnected(true);
  }

  public static void main(String[] args) {
    SimulationControl.createApp(new WangLandauApp(), args);
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
