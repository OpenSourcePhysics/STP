/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.isinglg;
import java.awt.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.numerics.PBC;

/**
 * Ising models a two-dimensional system of interacting spins.
 *
 * @author Jan Tobochnik, Wolfgang Christan, Harvey Gould
 * @version 1.0  revised 07/05/05
 */
public class IsingLG2 {
  public static final double criticalTemperature = 2.0/Math.log(1.0+Math.sqrt(2.0));
  public int L = 32;
  public int N = L*L;                           // number of spins
  public double temperature = criticalTemperature;
  public double chemicalPotentialLeft, chemicalPotentialRight;
  public int mcs = 0;                           // number of MC moves per spin
  public int nLeft, nRight, np;
  public int acceptedMoves = 0;
  private double[][][] w = new double[9][2][2]; // array to hold Boltzmann factors
  public LatticeFrame lattice;
  public int[] x, y;
  public int[] nnx = {1, 0, -1, 0};
  public int[] nny = {0, 1, 0, -1};

  public void initialize(int L, LatticeFrame displayFrame) {
    lattice = displayFrame;
    this.L = L;
    N = L*L;
    lattice.resizeLattice(L+2, L); // set lattice size
    lattice.setIndexedColor(1, Color.red);
    lattice.setIndexedColor(0, Color.green);
    np = nLeft+nRight;
    x = new int[np];
    y = new int[np];
    int i = 0;
    int j = 0;
    int n = 0;
    for(int s = 0; s<nLeft; ++s) {
      do {
        j = (int) (Math.random()*L);
        i = 1+(int) (Math.random()*0.5*L);
      } while(lattice.getValue(i, j)==1);
      x[n] = i;
      y[n] = j;
      n++;
      lattice.setValue(i, j, 1);
    }
    for(int s = 0; s<nRight; ++s) {
      do {
        j = (int) (Math.random()*L);
        i = 1+(L/2)+(int) (Math.random()*0.5*L);
      } while(lattice.getValue(i, j)==1);
      x[n] = i;
      y[n] = j;
      n++;
      lattice.setValue(i, j, 1);
    }
    resetData();
    setBoltzmannArrays();
  }

  public void setBoltzmannArrays() {
    for(int dE = 0; dE<9; dE++) {
      w[dE][0][0] = Math.exp(-(dE-4)/temperature);                                                // both new and old position on left
      w[dE][1][0] = Math.exp(-(dE-4+(chemicalPotentialLeft-chemicalPotentialRight))/temperature); // old on right, new on left
      w[dE][0][1] = Math.exp(-(dE-4+(chemicalPotentialRight-chemicalPotentialLeft))/temperature); // old on left, new on right
      w[dE][1][1] = Math.exp(-(dE-4)/temperature); // both new and old position on right
    }
  }

  public void resetData() {
    mcs = 0;
    acceptedMoves = 0;
  }

  public void doOneMCStep() {
    for(int k = 0; k<np; ++k) {
      int n = (int) (Math.random()*np);
      int dir = (int) (Math.random()*4);
      int inn = x[n]+nnx[dir];
      int jnn = PBC.position(y[n]+nny[dir], L);
      if((inn>0)&&(inn<(L+1))&&(lattice.getValue(inn, jnn)==0)) {
        //temporarily set the current site to 0 to calculate energy change
        lattice.setValue(x[n], y[n], 0);
        int dE = 4-(lattice.getValue((inn+1), jnn)+lattice.getValue(inn-1, jnn)+lattice.getValue(inn, (jnn+1)%L)
                    +lattice.getValue(inn, (jnn-1+L)%L));
        dE += (lattice.getValue((x[n]+1), y[n])+lattice.getValue(x[n]-1, y[n])+lattice.getValue(x[n], (y[n]+1)%L)
               +lattice.getValue(x[n], (y[n]-1+L)%L));
        //restore
        lattice.setValue(x[n], y[n], 1);
        int oldOnRight = (int) (2*(x[n]-1)/L);
        int newOnRight = (int) (2*(inn-1)/L);
        if(w[dE][oldOnRight][newOnRight]>Math.random()) {
          lattice.setValue(x[n], y[n], 0);
          lattice.setValue(inn, jnn, 1);
          x[n] = inn;
          y[n] = jnn;
          acceptedMoves++;
          nRight += newOnRight-oldOnRight;
          nLeft -= newOnRight-oldOnRight;
        }
      }
    }
    mcs++;
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
