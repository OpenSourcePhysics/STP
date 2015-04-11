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
import org.opensourcephysics.display2d.CellLattice;

public class LatGas2D implements Drawable {
  public static final double criticalTemperature = 0.5/Math.log(1.0+Math.sqrt(2.0));
  public int[][] site;
  public int L;
  public int N;                // Number of ns
  public double v0 = 1.0;       // Interaction strength
  public double T;             // Temperature
  public double g;             // External gravitational field
  public double E;             // System energy
  public double E_acc;         // E accumulator
  public double E2_acc;        // E^2 accumulator
  public int NP;               // Number of particles
  public int mcs;              // Number of MC moves per site
  public int acceptedMoves;    // Used to determine acceptance ratio
  private CellLattice lattice; // Used only for drawing

  public void initialize(int _L, double _T, int _NP) {
    L = _L;
    N = L*L;
    T = _T;
    NP = _NP;
    lattice = new CellLattice(L, L); // only used for drawing
    lattice.setIndexedColor(0, Color.white);
    lattice.setIndexedColor(1, Color.green);
    site = new int[L][L];
    for(int n = 0; n<NP; ++n) {
    	int i = n % L;
    	int j = n / L;
    	site[i][j] = 1;  // location of particle     
    }
     E = 0;
     for(int i = 0; i < L; i++)
    	 for(int j = 0; j < L; j++)
    		 E += g*j*site[i][j]-site[i][j]*(site[(i+1)%L][j] + site[i][(j+1)%L]);
    resetData();
  }

  public void setTemperature(double _T) {
    T = _T;
  }
  public void setField(double _g) {
	    double dg = _g - g;
	    g= _g;
	    for(int i = 0; i < L; i++)
	    	 for(int j = 0; j < L; j++)
	    		 E += dg*j*site[i][j];
	  }
  public double specificHeat() {
    int mcs = (this.mcs==0)
              ? 1
              : this.mcs;
    double E2_avg = E2_acc/(N*mcs);
    double E_avg = E_acc/(N*mcs);
    return(E2_avg-E_avg*E_avg)/(T*T*N);
  }

  
  public void resetData() {
    mcs = 0;
    E_acc = 0;
    E2_acc = 0;
    acceptedMoves = 0;
  }

  public void doOneMCStep() {
    for(int k = 0; k<N; ++k) {
      int i = (int) (Math.random()*L);
      int j = (int) (Math.random()*L);
      int iNN = (i+1)%L;
      int jNN = j;
      if(Math.random() < 0.5  && (g==0 || j < L-1)) { //choose other neighbor
    	  iNN = i;
          jNN = (j+1)%L;
      }
      if(site[i][j] + site[iNN][jNN] == 1){
       double dE = 0;
       if (j != jNN) dE += g*(site[i][j] - site[iNN][jNN]);
       if(g==0 || (j > 0 && jNN < L-1)) 
    	   dE += -v0*(site[iNN][jNN]- site[i][j])*
        (site[(i+1)%L][j]+site[(i-1+L)%L][j]+site[i][(j+1)%L]+site[i][(j-1+L)%L]-site[iNN][jNN]
        -site[(iNN+1)%L][jNN]-site[(iNN-1+L)%L][jNN]-site[iNN][(jNN+1)%L]-site[iNN][(jNN-1+L)%L]+site[i][j]);
       else
    	   dE += inFielddE(i,j,iNN,jNN);
       if((dE<=0)||(Math.random()<Math.exp(-dE/T))) {
    	  int stemp = site[i][j];
    	  site[i][j] = site[iNN][jNN];
    	  site[iNN][jNN] = stemp;
          acceptedMoves++;
          E += dE;
       }
      }
      E_acc += E;
      E2_acc += E*E;
    }
     mcs++;
  }

  public double inFielddE(int i, int j, int iNN, int jNN) {
	  if(j == 0 && jNN == 0) return
	  -v0*(site[iNN][jNN]- site[i][j])*
      (site[(i+1)%L][j]+site[(i-1+L)%L][j]+site[i][(j+1)%L]-site[iNN][jNN]
      -site[(iNN+1)%L][jNN]-site[(iNN-1+L)%L][jNN]-site[iNN][(jNN+1)%L]+
      site[i][j]);
     else if(j==0) return
    	 -v0*(site[iNN][jNN]- site[i][j])*
         (site[(i+1)%L][j]+site[(i-1+L)%L][j]+site[i][(j+1)%L]-site[iNN][jNN]
         -site[(iNN+1)%L][jNN]-site[(iNN-1+L)%L][jNN]-site[iNN][(jNN+1)%L]-site[iNN][(jNN-1+L)%L]+site[i][j]);
     else if(j == L-1 && jNN == L-1)return
     -v0*(site[iNN][jNN]- site[i][j])*
     (site[(i+1)%L][j]+site[(i-1+L)%L][j]+site[i][(j-1+L)%L]-site[iNN][jNN]
     -site[(iNN+1)%L][jNN]-site[(iNN-1+L)%L][jNN]-site[iNN][(jNN-1+L)%L]+site[i][j]);
    else return
    -v0*(site[iNN][jNN]- site[i][j])*
    (site[(i+1)%L][j]+site[(i-1+L)%L][j]+site[i][(j+1)%L]+site[i][(j-1+L)%L]-site[iNN][jNN]
    -site[(iNN+1)%L][jNN]-site[(iNN-1+L)%L][jNN]-site[iNN][(jNN-1+L)%L]+site[i][j]);
    }
  

  


  public void draw(DrawingPanel panel, Graphics g) {
    if(lattice==null) {
      return;
    }
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        lattice.setValue(i, j, (byte) (site[i][j]));
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
