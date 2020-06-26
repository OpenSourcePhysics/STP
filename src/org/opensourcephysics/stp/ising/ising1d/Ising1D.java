/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.Ising1d;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display2d.*;
import java.awt.*;

public class Ising1D implements Drawable {
  public int[] spin;
  public int N;                // Number of sites
  public double T;             // Temperature
  public double H;             // External magnetic field
  public double E;             // System energy
  public double E_acc;         // E accumulator
  public double E2_acc;        // E^2 accumulator
  public int M;                // System magnetization
  public double M_acc;         // M accumulator
  public double M2_acc;        // M^2 accumulator
  public int mcs;              // Number of MC moves per spin
  public int acceptedMoves;    // Used to determine acceptance ratio
  private CellLattice lattice; // Used only for drawing

  public void initialize(int _N, double _T, double _H) {
    N = _N;
    T = _T;
    H = _H;
    lattice = new CellLattice(N, 1);
    lattice.setIndexedColor(0, Color.yellow);
    lattice.setIndexedColor(2, Color.blue);
    // all spins up
    spin = new int[N];
    for(int i = 0; i<N; ++i) {
      spin[i] = 1;
    }
    M = N;
    E = -N-H*M;
    resetData();
  }

  public void setTemperature(double _T) {
    T = _T;
  }

  public void setExternalField(double _H) {
    E += H*M-_H*M;
    H = _H;
  }

  public double specificHeat() {
    int mcs = (this.mcs==0)
              ? 1
              : this.mcs;
    double E2_avg = E2_acc/mcs;
    double E_avg = E_acc/mcs;
    return(E2_avg-E_avg*E_avg)/(T*T*N);   // originally divided by N*N 
  }

  public double susceptibility() {
    int mcs = (this.mcs==0)
              ? 1
              : this.mcs;
    double M2_avg = M2_acc/mcs;
    double M_avg = M_acc/mcs;
    return(M2_avg-M_avg*M_avg)/(T*N);
  }

  public void resetData() {
    mcs = 0;
    E_acc = 0;
    E2_acc = 0;
    M_acc = 0;
    M2_acc = 0;
    acceptedMoves = 0;
  }

  public void doOneMCStep() {
    for(int k = 0; k<N; ++k) {
      int i = (int) (Math.random()*N);
      double dE = 2*spin[i]*(H+spin[(i+1)%N]+spin[(i-1+N)%N]);
      if((dE<=0)||(Math.random()<Math.exp(-dE/T))) {
        spin[i] = -spin[i];
        acceptedMoves++;
        E += dE;
        M += 2*spin[i];
      }
    }
    E_acc += E;
    E2_acc += E*E;
    M_acc += M;
    M2_acc += M*M;
    mcs++;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(lattice==null) {
      return;
    }
    for(int i = 0; i<N; i++) {
      lattice.setValue(i, 0, (byte) (spin[i]+1));
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
