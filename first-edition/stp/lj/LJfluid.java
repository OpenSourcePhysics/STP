/*

 * Open Source Physics software is free software as described near the bottom of this code file.

 *

 * For additional information and documentation on Open Source Physics please see:

 * <http://www.opensourcephysics.org/>

 */

package org.opensourcephysics.stp.lj;
import java.awt.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.numerics.*;

/**

 * LJfluidApp evolves a two-dimensional system of interacting particles

 * via the Lennard-Jones potential using a Verlet ODESolver.

 *

 * getHeatCapacity method corrected based on bug report by Mike Cooke.

 *

 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould

 * @version 1.1 revised 01/14/06, , 8/14/08

 */
public class LJfluid implements Drawable, ODE {
  public double state[];
  public double ax[], ay[];
  public int N, nx, ny;       // number of particles, number per row, number per column
  public double Lx, Ly;
  public double rho = N/(Lx*Ly);
  public double initialKineticEnergy;
  public int steps = 0;
  public double dt = 0.01;
  public double t;
  public double totalPotentialEnergyAccumulator;
  public double totalKineticEnergyAccumulator, totalKineticEnergySquaredAccumulator;
  public double virialAccumulator;
  public String initialConfiguration;
  public double radius = 0.5; // radius of particles on screen
  Verlet odeSolver = new Verlet(this);

  public void initialize() {
    N = nx*ny;
    t = 0;
    rho = N/(Lx*Ly);
    resetAverages();
    state = new double[1+4*N];
    ax = new double[N];
    ay = new double[N];
    if(initialConfiguration.equals("triangular")) {
      setTriangularLattice();
    } else if(initialConfiguration.equals("rectangular")) {
      setRectangularLattice();
    } else {
      setRandomPositions();
    }
    setVelocities();
    computeAcceleration();
    odeSolver.setStepSize(dt);
  }

  // end break
  // start break
  // setRandomPositions
  public void setRandomPositions() { // particles placed at random, but not closer than rMinimumSquared
    double rMinimumSquared = Math.pow(2.0, 1.0/3.0);
    boolean overlap;
    for(int i = 0; i<N; ++i) {
      do {
        overlap = false;
        state[4*i] = Lx*Math.random();   // x
        state[4*i+2] = Ly*Math.random(); // y
        int j = 0;
        while((j<i)&&!overlap) {
        	double dx = pbcSeparation(state[4*i]-state[4*j], Lx);
        	double dy = pbcSeparation(state[4*i+2]-state[4*j+2], Ly);
        	if(dx*dx+dy*dy<rMinimumSquared) {
            overlap = true;
          }
          j++;
        }
      } while(overlap);
    }
  }

  // end break
  // start break
  // setRectangularLattice
  public void setRectangularLattice() {  // place particles on a rectangular lattice
    double dx = Lx/nx; // distance between columns
    double dy = Ly/ny; // distance between rows
    for(int ix = 0; ix<nx; ++ix) {     // loop through particles in a row
      for(int iy = 0; iy<ny; ++iy) {   // loop through rows
        int i = ix+iy*ny;
        state[4*i] = dx*(ix+0.5);
        state[4*i+2] = dy*(iy+0.5);
      }
    }
  }

  // end break
  // start break
  // setTriangularLattice
  public void setTriangularLattice() { // place particles on triangular lattice
    double dx = Lx/nx; // distance between particles on same row
    double dy = Ly/ny; // distance between rows
    for(int ix = 0; ix<nx; ++ix) {
      for(int iy = 0; iy<ny; ++iy) {
        int i = ix+iy*ny;
        state[4*i+2] = dy*(iy+0.5);
        if(iy%2==0) {
          state[4*i] = dx*(ix+0.25);
        } else {
          state[4*i] = dx*(ix+0.75);
        }
      }
    }
  }

  // end break
  // start break
  // setVelocities
  public void setVelocities() {
    double vxSum = 0.0;
    double vySum = 0.0;
    for(int i = 0; i<N; ++i) {          // assign random initial velocities
      state[4*i+1] = Math.random()-0.5; // vx
      state[4*i+3] = Math.random()-0.5; // vy
      vxSum += state[4*i+1];
      vySum += state[4*i+3];
    }
    // zero center of mass momentum
    double vxcm = vxSum/N; // center of mass momentum (velocity)
    double vycm = vySum/N;
    for(int i = 0; i<N; ++i) {
      state[4*i+1] -= vxcm;
      state[4*i+3] -= vycm;
    }
    double v2sum = 0; // rescale velocities to obtain desired initial kinetic energy
    for(int i = 0; i<N; ++i) {
      v2sum += state[4*i+1]*state[4*i+1]+state[4*i+3]*state[4*i+3];
    }
    double kineticEnergyPerParticle = 0.5*v2sum/N;
    double rescale = Math.sqrt(initialKineticEnergy/kineticEnergyPerParticle);
    for(int i = 0; i<N; ++i) {
      state[4*i+1] *= rescale;
      state[4*i+3] *= rescale;
    }
  }

  // end break
  // start break
  // averages
  public double getMeanTemperature() {
    return totalKineticEnergyAccumulator/(N*steps);
  }

  public double getMeanEnergy() {
    return totalKineticEnergyAccumulator/steps+totalPotentialEnergyAccumulator/steps;
  }

  public double getMeanPressure() {
    double meanVirial;
    meanVirial = virialAccumulator/steps;
    return 1.0+0.5*meanVirial/(N*getMeanTemperature()); // quantity PA/NkT
  }

  public void quench(double quenchRate) {
    for(int i = 0; i<N; i++) {
      state[4*i+1] *= quenchRate; // vx
      state[4*i+3] *= quenchRate; // vy
    }
  }

  /**

   * Gets the heat capacity.

   *

   * Errata: On page 276 in Eq. 8.11, after the equal sign, the first appearance of N should be 1/N

   * and in Eq. 8.12 after the minus sign 1/N should be N. We thank Mike Cooke for pointing out this error.

   *

   * @return double

   */
  public double getHeatCapacity() {
    double meanTemperature = getMeanTemperature();
    double meanKineticEnergySquared = totalKineticEnergySquaredAccumulator/steps;
    double meanKineticEnergy = totalKineticEnergyAccumulator/steps;
    // heat capacity related to fluctuations of kinetic energy
    double sigma2 = meanKineticEnergySquared-meanKineticEnergy*meanKineticEnergy;
    double denom = 1.0-sigma2/(N*meanTemperature*meanTemperature);
    return N/denom;
  }

  public void resetAverages() {
    steps = 0;
    virialAccumulator = 0;
    totalPotentialEnergyAccumulator = 0;
    totalKineticEnergyAccumulator = 0;
    totalKineticEnergySquaredAccumulator = 0;
  }

  // end break
  // start break
  // computeAcceleration
  public void computeAcceleration() {
    for(int i = 0; i<N; i++) {
      ax[i] = 0;
      ay[i] = 0;
    }
    for(int i = 0; i<N-1; i++) {
      for(int j = i+1; j<N; j++) {
        double dx = pbcSeparation(state[4*i]-state[4*j], Lx);
        double dy = pbcSeparation(state[4*i+2]-state[4*j+2], Ly);
        double r2 = dx*dx+dy*dy;
        double oneOverR2 = 1.0/r2;
        double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
        double fOverR = 48.0*oneOverR6*(oneOverR6-0.5)*oneOverR2;
        double fx = fOverR*dx; // force in x-direction
        double fy = fOverR*dy; // force in y-direction
        ax[i] += fx;           // use Newton's third law
        ay[i] += fy;
        ax[j] -= fx;
        ay[j] -= fy;
        totalPotentialEnergyAccumulator += 4.0*(oneOverR6*oneOverR6-oneOverR6);
        virialAccumulator += dx*fx+dy*fy;
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

  // end break
  // start break
  // odeMethods
  public void getRate(double[] state, double[] rate) {
    // getRate is called twice for each call to step.
    // accelerations computed for every other call to getRate because
    // new velocity is computed from previous and current acceleration.
    // Previous acceleration is saved in step method of Verlet.
    if(odeSolver.getRateCounter()==1) {
      computeAcceleration();
    }
    for(int i = 0; i<N; i++) {
      rate[4*i] = state[4*i+1];   // rates for positions are velocities
      rate[4*i+2] = state[4*i+3]; // vy
      rate[4*i+1] = ax[i];        // rate for velocity is acceleration
      rate[4*i+3] = ay[i];
    }
    rate[4*N] = 1; // dt/dt = 1
  }

  public double[] getState() {
    return state;
  }

  public void step(HistogramFrame xVelocityHistogram) {
    odeSolver.step();
    double totalKineticEnergy = 0;
    for(int i = 0; i<N; i++) {
      totalKineticEnergy += (state[4*i+1]*state[4*i+1]+state[4*i+3]*state[4*i+3]);
      xVelocityHistogram.append(state[4*i+1]);
      state[4*i] = pbcPosition(state[4*i], Lx);
      state[4*i+2] = pbcPosition(state[4*i+2], Ly);
    }
    totalKineticEnergy *= 0.5;
    steps++;
    totalKineticEnergyAccumulator += totalKineticEnergy;
    totalKineticEnergySquaredAccumulator += totalKineticEnergy*totalKineticEnergy;
    t += dt;
  }

  // end break
  // start break
  // draw
  public void draw(DrawingPanel panel, Graphics g) {
    if(state==null) {
      return;
    }
    int pxRadius = Math.abs(panel.xToPix(radius)-panel.xToPix(0));
    int pyRadius = Math.abs(panel.yToPix(radius)-panel.yToPix(0));
    g.setColor(Color.red);
    for(int i = 0; i<N; i++) {
      int xpix = panel.xToPix(state[4*i])-pxRadius;
      int ypix = panel.yToPix(state[4*i+2])-pyRadius;
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    } // draw central cell boundary
    g.setColor(Color.black);
    int xpix = panel.xToPix(0);
    int ypix = panel.yToPix(Ly);
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
