/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.widom;
import java.awt.*;
import org.opensourcephysics.display.*;

/**
 * LJParticlesApp evolves a two-dimensional system of interacting particles
 * via the Lennard-Jones potential using a Verlet ODESolver.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05
 */
public class Widom implements Drawable {
  public double[] x, y;
  public int N;               // number of particles
  public double Lx, Ly,xc,yc;
  public double rho = N/(Lx*Ly);
  public int steps = 0;
  public double virialAccumulator, energyAccumulator, energySquaredAccumulator;
  public double virial, energy;
  public String initialConfiguration;
  public double radius = 0.5; // radius of particles on screen
  public double chemPotAccumulator;
  public double temperature, beta, ds, accept;

  public void initialize() {
    chemPotAccumulator = 0;
    rho = N/(Lx*Ly);
    resetAverages();
    x = new double[N];
    y = new double[N];
    setRandomPositions();
    beta = 1.0/temperature;
    computeTotals();
  }

  // end break
  // start break
  // setRandomPositions
  public void setRandomPositions() { // particles placed at random, but not closer than rMinimumSquared
    for(int i = 0; i<N; ++i) {
      x[i] = Lx*Math.random(); // x
      y[i] = Ly*Math.random(); // y
    }
  }

  // end break
  // start break
  // averages
  public double getMeanEnergy() {
    return energyAccumulator/(N*steps);
  }

  public double getMeanPressure() {
    return 0.5*virialAccumulator/(steps*Lx*Ly);
  }

  public double getMeanChemicalPotential() {
    return -temperature*Math.log(chemPotAccumulator/(steps*N));
  }

  public double getHeatCapacity() {  //actually specific heat
    double averageE = energyAccumulator/steps;
    double averageE2 = energySquaredAccumulator/steps;
    return beta*beta*(averageE2-averageE*averageE)/N;
  }

  public void resetAverages() {
    steps = 0;
    virialAccumulator = 0;
    energyAccumulator = 0;
    energySquaredAccumulator = 0;
    chemPotAccumulator = 0;
    accept = 0;
  }

  // end break
  // start break
  // computeTotals
  public void computeTotals() {
    energy = 0;
    virial = 0;
    for(int i = 0; i<N-1; i++) {
      for(int j = i+1; j<N; j++) {
        double dx = pbcSeparation(x[i]-x[j], Lx);
        double dy = pbcSeparation(y[i]-y[j], Ly);
        double r2 = dx*dx+dy*dy;
        double oneOverR2 = 1.0/r2;
        double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
        energy += 4.0*oneOverR6*(oneOverR6-1);
        double fOverR = 48.0*oneOverR6*(oneOverR6-0.5)*oneOverR2;
        double fx = fOverR*dx; // force in x-direction
        double fy = fOverR*dy; // force in y-direction
        virial += dx*fx+dy*fy;
      }
    }
  }

  // end break
  // start break
  // pbcSeparation
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

  // end break
  // start break
  // pbcPosition
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

  public void step() {
    double[] change = {0, 0};
    for(int j = 0; j<N; j++) {
      int i = (int) (Math.random()*N);
      double xTry = x[i]+ds*(Math.random()-0.5);
      double yTry = y[i]+ds*(Math.random()-0.5);
      change = computeChange(i, xTry, yTry);
      double dE = change[0];
      if((dE<0)||(Math.exp(-dE*beta)>Math.random())) { // accept
        x[i] = xTry;
        y[i] = yTry;
        energy += dE;
        virial += change[1];
        accept++;
      }
      computeChemicalPotential();
    }
    steps++;
    energyAccumulator += energy;
    virialAccumulator += virial;
    energySquaredAccumulator += energy*energy;
  }

  public double[] computeChange(int i, double xTry, double yTry) {
    double change[] = {0, 0};
    for(int j = 0; j<N; j++) {
      if(j!=i) {
        // old value
        double dx = pbcSeparation(x[i]-x[j], Lx);
        double dy = pbcSeparation(y[i]-y[j], Ly);
        double r2 = dx*dx+dy*dy;
        double oneOverR2 = 1.0/r2;
        double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
        change[0] -= 4.0*oneOverR6*(oneOverR6-1);
        double fOverR = 48.0*oneOverR6*(oneOverR6-0.5)*oneOverR2;
        double fx = fOverR*dx; // force in x-direction
        double fy = fOverR*dy; // force in y-direction
        change[1] -= dx*fx+dy*fy;
        // new value
        dx = pbcSeparation(xTry-x[j], Lx);
        dy = pbcSeparation(yTry-y[j], Ly);
        r2 = dx*dx+dy*dy;
        oneOverR2 = 1.0/r2;
        oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
        change[0] += 4.0*oneOverR6*(oneOverR6-1);
        fOverR = 48.0*oneOverR6*(oneOverR6-0.5)*oneOverR2;
        fx = fOverR*dx;        // force in x-direction
        fy = fOverR*dy;        // force in y-direction
        change[1] += dx*fx+dy*fy;
      }
    }
    return change;
  }

  public void computeChemicalPotential() {
     xc = Lx*Math.random();
     yc = Ly*Math.random();
    double dU = 0;
    for(int j = 0; j<N; j++) {
      double dx = pbcSeparation(xc-x[j], Lx);
      double dy = pbcSeparation(yc-y[j], Ly);
      double r2 = dx*dx+dy*dy;
      double oneOverR2 = 1.0/r2;
      double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
      dU += 4.0*oneOverR6*(oneOverR6-1);
    }
    chemPotAccumulator += Math.exp(-beta*dU);
  }

  // end break
  // start break
  // draw
  public void draw(DrawingPanel panel, Graphics g) {
    if(x==null) {
      return;
    }
    int pxRadius = Math.abs(panel.xToPix(radius)-panel.xToPix(0));
    int pyRadius = Math.abs(panel.yToPix(radius)-panel.yToPix(0));
    g.setColor(Color.green);
    int xpix = panel.xToPix(xc)-pxRadius;
    int ypix = panel.yToPix(yc)-pyRadius;
    g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);

    g.setColor(Color.red);
    for(int i = 0; i<N; i++) {
      xpix = panel.xToPix(x[i])-pxRadius;
      ypix = panel.yToPix(y[i])-pyRadius;
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    } // draw central cell boundary
    g.setColor(Color.black);
    xpix = panel.xToPix(0);
    ypix = panel.yToPix(Ly);
    int lx = panel.xToPix(Lx)-panel.xToPix(0);
    int ly = panel.yToPix(0)-panel.yToPix(Ly);
    g.drawRect(xpix, ypix, lx, ly);
  }
  // end break

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
