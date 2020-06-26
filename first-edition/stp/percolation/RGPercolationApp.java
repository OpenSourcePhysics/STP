/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.percolation;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import java.awt.Color;

/**
 * RGApp implements a visual interpretation of the renormalization group.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould, Kipton Barros
 * @version 1.1  revised 05/18/05
 * @version 2.0 revised 05/05/09
 */
public class RGPercolationApp extends AbstractCalculation {
  LatticeFrame[] blockedLattices;
  int n = 0;
 
  
  public void calculate() {
	   int nOld = n;
	   int L = control.getInt("L");
	   n = 1 + (int) (Math.log(L)/Math.log(2) +0.001);
	   	if(nOld != n) {
	   	   
	   		if(nOld > 0) for(int i = 0; i < nOld; i++){
			blockedLattices[i].setVisible(false);
		    blockedLattices[i].dispose();
		   }
		   blockedLattices = new LatticeFrame[n];
	    }
    double p = control.getDouble("p");
    if(n != nOld) blockedLattices[0] = new LatticeFrame("Original Lattice");
    newLattice(L, p, blockedLattices[0]);
    int Lnew = L/2;
    setLatticeColors(blockedLattices[0]);
    blockedLattices[0].setVisible(true);
    for(int i = 1; i < n; i++){
    	if(n != nOld) {
    		blockedLattices[i] = new LatticeFrame(Lnew + "x" + Lnew + " Coarse Grained Lattice");
    	    setLatticeColors(blockedLattices[i]);
    	}
    	block(blockedLattices[i-1],blockedLattices[i],Lnew);
    	blockedLattices[i].setVisible(true);
    	Lnew = Lnew/2;
    }
    }

  public void reset() {
    control.setValue("L", 64);
    control.setValue("p", 0.5927);
  }

  public void newLattice(int L, double p, LatticeFrame lattice) { // new lattice
    lattice.resizeLattice(L, L);
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        if(Math.random()<p) {
          lattice.setValue(i, j, 1);
        }
      }
    }
  }

  public void block(LatticeFrame lattice, LatticeFrame blockedLattice, int Lb) {
    blockedLattice.resizeLattice(Lb, Lb);
    for(int ib = 0; ib<Lb; ib++) {
      for(int jb = 0; jb<Lb; jb++) {
        int leftCellsProduct = lattice.getValue(2*ib, 2*jb)*lattice.getValue(2*ib, 2*jb+1);
        int rightCellsProduct = lattice.getValue(2*ib+1, 2*jb)*lattice.getValue(2*ib+1, 2*jb+1);
        if((leftCellsProduct==1)||(rightCellsProduct==1)) {
          blockedLattice.setValue(ib, jb, 1); // vertical spanning rule
        }
      }
    }
  }

  public void setLatticeColors(LatticeFrame lattice) {
    lattice.setIndexedColor(0, Color.WHITE);
    lattice.setIndexedColor(1, Color.BLUE);
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new RGPercolationApp(), args);
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
