/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.HardDisksMC;
import java.awt.*;
import java.util.Random;
import org.opensourcephysics.display.*;

/**
 * LJParticlesApp evolves a two-dimensional system of interacting particles
 * via the Lennard-Jones potential using a Verlet ODESolver.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05
 */
public class HardDisksMC implements Drawable {
  public double x[];
  public double y[];
  public int N;               // number of particles, number per row, number per column
  public double L, equivL;
  public double rho = N/(L*L);
  public int steps = 0;
  public double mcs;
  public String initialConfiguration;
  public double radius = 0.5; // radius of particles on screen
  public double beta;
  public double stepSize;
  Random r = new Random();
  public double s;
  public int accept = 0;

  public void initialize() {
    accept = 0;
    mcs = 0;
    rho = N/(L*L);
    x = new double[N];
    y = new double[N];
     equivL = L;
    steps = 0;
    radius = 0.5;
    if(initialConfiguration.equals("hexagonal")) {
      setHexagonalLattice();
    } else if(initialConfiguration.equals("rectangular")) {
      setRectangularLattice();
    } else {
      setRandomPositions();
    }
  }

  public void setRandomPositions() { // particles placed at random, but not closer than rMinimumSquared
    double rMinimumSquared = 1;
    boolean overlap;
    for(int i = 0; i<N; ++i) {
      do {
        overlap = false;
        x[i] = L*Math.random();         // x
        y[i] = L*Math.random();         // y
        int j = 0;
        while((j<i)&&!overlap) {
          double dx = pbcSeparation(x[i]-x[j], L);
          double dy = pbcSeparation(y[i]-y[j], L);
          double r2 = dx*dx+dy*dy;
          if(r2<rMinimumSquared) {
            overlap = true;
          }
          j++;
        }
      } while(overlap);
    }
  }

  public void setRectangularLattice() { // place particles on a rectangular lattice
    int nx = (int) Math.sqrt(N);
    int ny = nx;
    double dx = L/nx; // distance between columns
    double dy = dx;
    for(int ix = 0; ix<nx; ++ix) {   // loop through particles in a row
      for(int iy = 0; iy<ny; ++iy) { // loop through rows
        int i = ix+iy*ny;
        x[i] = dx*(ix+0.5);
        y[i] = dy*(iy+0.5);
      }
    }
  }

  public void setHexagonalLattice() {
    // place particles on hexagonal lattice
    double dnx = Math.sqrt(N);
    int ns = (int) dnx;
    if(dnx-ns>0.001) {
      ns++;
    }
    double ax = L/ns;
    double ay = L/ns;
    int i = 0;
    int iy = 0;
    while(i<N) {
      for(int ix = 0; ix<ns; ++ix) {
        if(i<N) {
          y[i] = ay*(iy+0.5);
          if(iy%2==0) {
            x[i] = ax*(ix+0.25);
          } else {
            x[i] = ax*(ix+0.75);
          }
          i++;
        }
      }
      iy++;
    }
  }

  public boolean checkOverlap(int i, double xTry, double yTry) {
    boolean overlap = false;
    for(int j = 0; j<N; j++) 
    	if(i != j) {
         double dx = pbcSeparation(xTry-x[j], L);
         double dy = pbcSeparation(yTry-y[j], L);
         double r2 = dx*dx+dy*dy;
         double r = Math.sqrt(r2);
         double d = 2*radius;
         if(r<d) {
          overlap = true;
        }
    }
    return overlap;
  }

  public void compress() {
	  radius = s*radius;
	  if(s > 1){
	  double minSeparation = L; 
	   for(int i = 0; i<N-1; i++) 
	    	for(int j = i+1; j < N; j++){
	         double dx = pbcSeparation(x[i]-x[j], L);
	         double dy = pbcSeparation(y[i]-y[j], L);
	         double r2 = dx*dx+dy*dy;
	         double r = Math.sqrt(r2);
	         if(r<minSeparation) 
	           minSeparation = r;
	   }
	 
	    if(radius > 0.5*minSeparation)
	        radius = 0.5*minSeparation;
	  }
	  equivL = L*Math.sqrt(0.25/(radius*radius));
	  } 
  

  private double pbcSeparation(double ds, double L) {
    if(ds>0) {
      while(ds>0.5*L) {
        ds -= L;
      }
    } else {
      while(ds<-0.5*L) {
        ds += L;
      }
    }
    return ds;
  }

  private double pbcPosition(double s, double L) {
    if(s>0) {
      while(s>L) {
        s -= L;
      }
    } else {
      while(s<0) {
        s += L;
      }
    }
    return s;
  }

  public class TrialMove {
    int n;
    double xTry, yTry;
    boolean overlap;

  }

  public TrialMove makeTrialMove() {
    TrialMove tm = new TrialMove();
    tm.n = r.nextInt(N);
    double dx = 2*stepSize*(Math.random()-0.5);
    double dy = 2*stepSize*(Math.random()-0.5);
    tm.xTry = pbcPosition(x[tm.n] + dx, L);  
    tm.yTry = pbcPosition(y[tm.n] + dy, L);
    tm.overlap = checkOverlap(tm.n,tm.xTry,tm.yTry);
    return tm;
  }

  public void step() {
    TrialMove tm = makeTrialMove();
    if(!tm.overlap) {
      x[tm.n] = tm.xTry;
      y[tm.n] = tm.yTry;
      accept++;
    }
    steps++;
  }

  public void oneMCstep() {
    for(int i = 0; i<N; i++) {
      step();
    }
    mcs++;
    compress();
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if((x==null)||(y==null)) {
      return;
    }
    int pxRadius = Math.abs(panel.xToPix(radius)-panel.xToPix(0));
    int pyRadius = Math.abs(panel.yToPix(radius)-panel.yToPix(0));
    g.setColor(Color.red);
    for(int i = 0; i<N; i++) {
      int xpix = panel.xToPix(x[i])-pxRadius;
      int ypix = panel.yToPix(y[i])-pyRadius;
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    } // draw central cell boundary
    g.setColor(Color.black);
    int xpix = panel.xToPix(0);
    int ypix = panel.yToPix(L);
    int lx = panel.xToPix(L)-panel.xToPix(0);
    int ly = panel.yToPix(0)-panel.yToPix(L);
    g.drawRect(xpix, ypix, lx, ly);
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
