/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.diffusion;
import java.awt.Graphics;
import java.util.Random;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display2d.BinaryLattice;

/**
 *  simulation of particle diffusion in a lattice gas
 *
 * @author     Peter Sibley
 * @author     Joshua Gould
 *
 * @created    December 26, 2002
 * modified for the new lattice class April 14, 2003
 * @Natali
 */
public class LatticeGas implements Drawable {
  int L;      // linear dimension of lattice
  double L_2; // 0.5 * L
  int N;      // number of particles
  final int OCCUPIED = 1;
  final int EMPTY = 0;
  int site[][];
  int x[];    // keeps track of the occupied sites
  int y[];    // keeps track of the occupied sites
  int x0[];   // keeps track of the occupied sites
  int y0[];
  int wrapX[], wrapY[];
  Random random;
  int xposition;
  int yposition;
  double R2bar;
  double t;
  BinaryLattice lattice;

  public void initialize(int L, int N) {
    this.L = L;
    this.N = N;
    lattice = new BinaryLattice(L, L);
    site = new int[L][L];
    x = new int[N];
    y = new int[N];
    x0 = new int[N];
    y0 = new int[N];
    wrapX = new int[N];
    wrapY = new int[N];
    L_2 = 0.5*L;
    random = new Random(System.currentTimeMillis());
  }

  public void fillLattice() {
    int i = 0;
    while(i<N) {
      int xadd = (int) (L*random.nextDouble());
      int yadd = (int) (L*random.nextDouble());
      if(site[xadd][yadd]==EMPTY) {
        site[xadd][yadd] = OCCUPIED; // site occupied
        x[i] = xadd;                 // x[0] is ignored
        y[i] = yadd;
        x0[i] = x[i];                // x-coordinate at t = 0
        y0[i] = y[i];
        i = i+1;                     // number of particles added
      }
    }
  }

  public void move() {
    int wrap[] = {0, 0};
    for(int particle = 0; particle<N; particle++) {
      int i = (int) (N*random.nextDouble());
      xposition = x[i];
      yposition = y[i];
      chooseDirection(wrap);                  // move left, right, up, or down
      if(site[xposition][yposition]==EMPTY) { // if the new site is unoccupied
        site[x[i]][y[i]] = EMPTY;             // place where it was is now empty
        x[i] = xposition;                     // assigns new positions
        y[i] = yposition;
        wrapX[i] += wrap[0];                  // number of times used pbc
        wrapY[i] += wrap[1];
        site[x[i]][y[i]] = OCCUPIED;          // new site occupied
      }
    }
  }

  public static int sgn(double d) {
    if(d<0) {
      return -1;
    } else if(d>0) {
      return 1;
    } else {
      return 0;
    }
  }

  // choose random direction and use periodic boundary conditions
  void chooseDirection(int wrap[]) {
    wrap[0] = 0; // x wrap
    wrap[1] = 0; // y wrap
    int dir = (int) (4*random.nextDouble())+1;
    switch(dir) {
       case 1 :
         xposition = xposition+1;
         if(xposition>=L) {
           xposition = 0;
           wrap[0] = L;
         }
         break;
       case 2 :
         xposition = xposition-1;
         if(xposition<0) {
           xposition = L-1;
           wrap[0] = -L;
         }
         break;
       case 3 :
         yposition = yposition+1;
         if(yposition>=L) {
           yposition = 0;
           wrap[1] = L;
         }
         break;
       case 4 :
         yposition = yposition-1;
         if(yposition<0) {
           yposition = L-1;
           wrap[1] = -L;
         }
         break;
    }
  }

  double computeR2Bar() {
    double R2 = 0;
    for(int i = 0; i<N; i++) {
      double dx = x[i]-x0[i]+wrapX[i];
      double dy = y[i]-y0[i]+wrapY[i];
      R2 += dx*dx+dy*dy;
    }
    R2bar = R2/N;
    return R2bar;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(lattice==null) {
      return;
    }
    lattice.setBlock(0, 0, site);
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
