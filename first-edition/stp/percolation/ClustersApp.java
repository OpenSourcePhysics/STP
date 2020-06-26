/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.percolation;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;

/**
 *  ClustersApp models the Newman-Ziff algorithm for identifying clusters and displays the clusters.
 *
 *  @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 *  @version 1.0 06/15/05
 */
public class ClustersApp extends AbstractSimulation {
  Scalar2DFrame grid = new Scalar2DFrame("Newman-Ziff cluster algorithm");
  PlotFrame plot1 = new PlotFrame("p", "Mean Cluster Size", "Mean cluster size");
  PlotFrame plot2 = new PlotFrame("p", "P_\u221e", "P\u221e");
  PlotFrame plot3 = new PlotFrame("p", "P_span", "P_span");
  PlotFrame plot4 = new PlotFrame("s", "      <n_{s}>", "Cluster size distribution");
  Clusters lattice;
  double pDisplay;
  double[] meanClusterSize;
  double[] P_infinity;
  double[] P_span;           // probability of a spanning cluster
  double[] numClustersAccum; // number of clusters of size s
  int numberOfTrials;

  public void initialize() {
    int L = control.getInt("lattice size L");
    grid.resizeGrid(L, L);
    lattice = new Clusters(L);
    pDisplay = control.getDouble("display lattice at this value of p");
    grid.setMessage("p = "+pDisplay);
    plot1.setPreferredMinMaxX(0, 1);
    plot2.setPreferredMinMax(0, 1, 0, 1);
    plot3.setPreferredMinMax(0, 1, 0, 1);
    plot4.setPreferredMinMaxY(1, L);
    plot4.setMessage("p = "+pDisplay);
    plot4.setLogScale(true, true);
    meanClusterSize = new double[L*L];
    P_infinity = new double[L*L];
    P_span = new double[L*L];
    numClustersAccum = new double[L*L+1];
    numberOfTrials = 0;
  }

  public void doStep() {
    control.clearMessages();
    control.println("Trial "+numberOfTrials); // same as number of configurations
    // adds sites to new cluster, and accumulate results
    lattice.newLattice();
    for(int i = 0; i<lattice.N; i++) {
      lattice.addRandomSite();
      meanClusterSize[i] += (double) lattice.getMeanClusterSize();
      P_infinity[i] += (double) lattice.getSpanningClusterSize()/lattice.numSitesOccupied;
      P_span[i] += ((lattice.getSpanningClusterSize()==0)
                    ? 0
                    : 1);
      if((int) (pDisplay*lattice.N)==i) {
        for(int j = 0; j<lattice.N; j++) {
          numClustersAccum[j] += lattice.numClusters[j];
        }
        displayLattice();
      }
    }
    // display accumulated results
    numberOfTrials++;
    plotAverages();
  }

  private void plotAverages() {
    plot1.clearData();
    plot2.clearData();
    plot3.clearData();
    plot4.clearData();
    int del = 1 + lattice.N/10000;  // limit number of points plotted
    for(int i = 0; i<lattice.N; i = i + del) {
      double p = (double) i/lattice.N; // occupation probability
      plot1.append(0, p, meanClusterSize[i]/numberOfTrials);
      plot2.append(0, p, P_infinity[i]/numberOfTrials);
      plot3.append(0, p, P_span[i]/numberOfTrials);
    }
    for(int i = 0; i<lattice.N; i++) {   
      if(numClustersAccum[i+1]>0) {
        plot4.append(0, i+1, numClustersAccum[i+1]/numberOfTrials);
      }
    }
  }

  private void displayLattice() {
    double display[] = new double[lattice.N];
    for(int s = 0; s<lattice.N; s++) {
      display[s] = lattice.getClusterSize(s);
    }
    grid.setAll(display);
  }

  public void reset() {
    control.setValue("lattice size L", 128);
    control.setValue("display lattice at this value of p", 0.5927);
  }

  public static void main(String args[]) {
    SimulationControl.createApp(new ClustersApp());
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
