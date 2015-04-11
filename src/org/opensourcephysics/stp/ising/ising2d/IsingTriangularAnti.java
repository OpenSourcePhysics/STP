/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
import java.awt.Color;
import java.awt.Graphics;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display2d.TriangularByteLattice;

/**
 * IsingTri class.
 *
 *  @author Hui Wang
 *  @version 1.0   revised 11/26/06
 */
public class IsingTriangularAnti implements Drawable {
  public static final double criticalTemperature = 3.641;
  public int spin[][];
  public int L;
  public int N;
  public double temperature = criticalTemperature;
  public int mcs = 0;                  // number of MC moves per spin
  public int J = -1;
  public int energy;
  public double energyAccumulator = 0;
  public double energySquaredAccumulator = 0;
  public int magnetization = 0;
  public double magnetizationAccumulator = 0;
  public double magnetizationAbsAccumulator = 0;
  public double magnetizationSquaredAccumulator = 0;
  public int acceptedMoves = 0;
  private double w[] = new double[13]; // array to hold Boltzmann factors
  private TriangularByteLattice lattice;

  public void initialize(int _L) {
    L = _L;
    lattice = new TriangularByteLattice(L, L); // only used for drawing
    lattice.setIndexedColor(0, Color.red);
    lattice.setIndexedColor(2, Color.blue);
    spin = new int[L][L];
    N = L*L;
    for(int i = 0; i<L; ++i) {
      for(int j = 0; j<L; ++j) {
        spin[i][j] = 1; // all spins up
      }
    }
    magnetization = N;
    energy = -3*J*N;
    resetData();
    w[12] = Math.exp(-12.0/temperature); // other array elements never occur for H = 0
    w[8] = Math.exp(-8.0/temperature);
    w[4] = Math.exp(-4.0/temperature);
  }

  public double magnetizationAverage() {
    return magnetizationAbsAccumulator/mcs;
  }

  public double specificHeat() {
    double energySquaredAverage = energySquaredAccumulator/mcs;
    double energyAverage = energyAccumulator/mcs;
    double heatCapacity = energySquaredAverage-energyAverage*energyAverage;
    heatCapacity = heatCapacity/(temperature*temperature);
    return(heatCapacity/N);
  }

  public double susceptibility() {
    double magnetizationSquaredAverage = magnetizationSquaredAccumulator/mcs;
    return(magnetizationSquaredAverage-Math.pow(magnetizationAverage(), 2))/(temperature*N);
  }

  public void resetData() {
    mcs = 0;
    energyAccumulator = 0;
    energySquaredAccumulator = 0;
    magnetizationAccumulator = 0;
    magnetizationAbsAccumulator = 0;
    magnetizationSquaredAccumulator = 0;
    acceptedMoves = 0;
  }

  public void doOneMCStep() {
    for(int k = 0; k<N; ++k) {
      int i = (int) (Math.random()*L);
      int j = (int) (Math.random()*L);
      int iu = (i+1)%L;
      int id = (i-1+L)%L;
      int ju = (j+1)%L;
      int jd = (j-1+L)%L;
      int dE = 2*J*spin[i][j]*(spin[iu][j]+spin[id][j]+spin[i][ju]+spin[i][jd]+spin[iu][jd]+spin[id][ju]);
      if((dE<=0)||(w[dE]>Math.random())) {
        spin[i][j] = -spin[i][j];
        acceptedMoves++;
        energy += dE;
        magnetization += 2*spin[i][j];
      }
    }
    energyAccumulator += energy;
    energySquaredAccumulator += energy*energy;
    magnetizationAccumulator += magnetization;
    magnetizationAbsAccumulator += Math.abs(magnetization);
    magnetizationSquaredAccumulator += magnetization*magnetization;
    mcs++;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(lattice==null) {
      return;
    }
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        lattice.setCell((i+j/2)%L, j, (byte) (spin[i][j]+1));
      }
    }
    lattice.draw(panel, g);
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
