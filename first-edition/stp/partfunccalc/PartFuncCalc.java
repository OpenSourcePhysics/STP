/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.partfunccalc;
import java.awt.Color;
import org.opensourcephysics.frames.LatticeFrame;

public class PartFuncCalc {
  public int mcs;                      // number of MC moves before each flatness test
  public int L = 4;
  public int N;                        // number of spins
  public int H[];                      // histogram
  public double lnZ[];                 // partition function
  public double increment[];
  public double T[];                   // temperatures
  public double f;                     // multiplicative factor accumulated for density of states
  double lnf = 1.0;
  public double acceptEnergy = 0;      // number of accepted energy MC moves
  public double acceptTemperature = 0; // number of accepted temperature MC moves
  public double tryEnergy = 0;         // number of trial energy MC moves
  public double tryTemperature = 0;    // number of trial temperature MC moves
  public double fMin = Math.exp(0.001);
  public double flatnessFactor = 0.7;  // fraction of average H considered flat
  public double cutoffFraction = 0.1;  // fraction allowed below cutoff to be flat
  public int nRunMax = 500;
  public int Et;                       // total energy
  public LatticeFrame lattice;
  public int Tsize;                    // number of sampling temperatures.
  public double B[];
  public int temperature;
  public int k = 1;
  public double prob = 0.001;
  public int currenttempindex;

  public void resetData() {
    acceptEnergy = 0;
    acceptTemperature = 0;
    tryEnergy = 0;
    tryTemperature = 0;
    for(int i = 0; i<Tsize; i++) { // Reset temperature histogram
      H[i] = 0;
    }
  }

  public void settemps() {
    int counter = 0;
    for(double i = 0.1; i<1.04; i += 0.1) {
      increment[counter] = 0.1;
      T[counter] = i;
      counter++;
    }
    for(double i = 1.04; i<1.82; i += 0.04) {
      increment[counter] = 0.04;
      T[counter] = i;
      counter++;
    }
    for(double i = 1.82; i<2.005; i += 0.02) {
      increment[counter] = 0.02;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.005; i<2.2025; i += 0.005) {
      increment[counter] = 0.005;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.2025; i<2.252; i += 0.0025) {
      increment[counter] = 0.0025;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.252; i<2.301; i += 0.002) {
      increment[counter] = 0.002;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.305; i<2.352; i += 0.005) {
      increment[counter] = 0.005;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.36; i<=2.505; i += 0.01) {
      increment[counter] = 0.01;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.52; i<2.705; i += 0.02) {
      increment[counter] = 0.02;
      T[counter] = i;
      counter++;
    }
    for(double i = 2.75; i<3.62; i += 0.05) {
      increment[counter] = 0.05;
      T[counter] = i;
      counter++;
    }
    for(double i = 3.67; i<5.05; i += 0.07) {
      increment[counter] = 0.07;
      T[counter] = i;
      counter++;
    }
    for(double i = 5.1; i<6.05; i += 0.1) {
      increment[counter] = 0.1;
      T[counter] = i;
      counter++;
    }
    for(double i = 6.2; i<8.1; i += 0.2) {
      increment[counter] = 0.2;
      T[counter] = i;
      counter++;
    }
    for(int j = 0; j<Tsize; j++) {
      B[j] = 1/(T[j]);
    }
  }

  public void initial(int L, double prob, LatticeFrame displayFrame) {
    lattice = displayFrame;
    this.L = L;
    this.prob = prob;
    N = L*L;
    Tsize = 218;
    B = new double[Tsize];
    lnZ = new double[Tsize];
    increment = new double[Tsize];
    H = new int[Tsize];
    T = new double[Tsize];
    currenttempindex = Tsize-1;
    settemps();
    lattice.resizeLattice(L, L); // set lattice size
    lattice.setIndexedColor(1, Color.green);
    lattice.setIndexedColor(-1, Color.red);
    lnf = 1.0;
    resetData();
    Et = 0;
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        lattice.setValue(j, i, -1);
        //if (Math.random() > 0.5)
        //  lattice.setValue(j, i, 1); // set random spins
      }
    }
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        Et += -lattice.getValue(j, i)*(lattice.getValue((j+1)%L, i)+lattice.getValue(j, (i+1)%L));
      }
    }
    Et += 2*N;
    for(int i = 0; i<Tsize; i++) { // Reset temperature histogram and partition function
      lnZ[i] = 0.0;
      H[i] = 0;
    }
  }

  public void doMcStep() {
    int newtempindex;
    if(Math.random()>prob) {
      //Energy move at constant temperature
      int x = (int) (Math.random()*L);
      int y = (int) (Math.random()*L);
      int dE = 2*lattice.getValue(x, y)
               *(lattice.getValue((x+1)%L, y)+lattice.getValue((x-1+L)%L, y)+lattice.getValue(x, (y+1)%L)
                 +lattice.getValue(x, (y-1+L)%L));
      if((dE<=0)||(Math.exp(-dE*B[currenttempindex])>Math.random())) {
        lattice.setValue(x, y, -lattice.getValue(x, y));
        Et = Et+dE;
        acceptEnergy++;
      }
      tryEnergy++;
    } else {
      //Temperature move at constant energy
      do {
        int stepsize = (int) (Math.random()*20+1);
        if(Math.random()>0.5) {
          stepsize *= -1;
        }
        newtempindex = currenttempindex+stepsize;
      } while((newtempindex<0)||(newtempindex>217));
      double arg = (-B[newtempindex]+B[currenttempindex])*Et+lnZ[currenttempindex]-lnZ[newtempindex];
      if((arg>0)||(Math.exp(arg)>Math.random())) {
        currenttempindex = newtempindex;
        acceptTemperature++;
      }
      tryTemperature++;
    }
    lnZ[currenttempindex] += lnf;
    H[currenttempindex]++;
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
