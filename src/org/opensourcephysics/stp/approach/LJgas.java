/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/**
 * particles start in a central box and
 * eventually fill the simulation cell showing the approach to equilibrium. There
 * is a button which allows you to reverse the velocities and see the particles go
 * back to their original positions.
 * @author Jan Tobochnik
 * @author Peter Sibley
 */
package org.opensourcephysics.stp.approach;
import org.opensourcephysics.display.*;
import java.awt.*;

public class LJgas implements Drawable {
  public double x[], y[], vx[], vy[], ax[], ay[];
  public int numberOfParticles = 270;
  public double cellLength;
  public double rho = 0.2; // fixed density
  public double x0, x1;
  public double temperature = 5.0;
  public int steps = 0;
  public double dt = 0.01;
  public int n0 = 0, n1 = 0, n2 = 0;
  double radius = 0.3;
  public double cut2 = 3.0*3.0;
  public double t = 0;

  /**
   * Constructor LJgas
   */
  public LJgas() {
    setArrays();
  }

  public void setArrays() {
    int N = numberOfParticles;
    x = new double[N];
    y = new double[N];
    vx = new double[N];
    vy = new double[N];
    ax = new double[N];
    ay = new double[N];
  }

  public void initialize() {
    cellLength = Math.sqrt(numberOfParticles/rho);
    reset();
    setArrays();
    setPositions();
    setVelocities();
    accel();
  }

  public void setPositions() {
    boolean overlap;
    double ds = cellLength/3.0;
    double s0 = cellLength/3.0;
    for(int i = 0; i<numberOfParticles; ++i) {
      do {
        overlap = false;
        x[i] = s0+ds*Math.random();
        y[i] = 0+cellLength*Math.random();
        int j = 0;
        while((j<i)&&!overlap) {
          double dx = x[i]-x[j];
          double dy = y[i]-y[j];
          dy = pbc(dy);
          if(dx*dx+dy*dy<1.0) {
            overlap = true;
          }
          j++;
        }
      } while(overlap);
    }
  }

  public void setVelocities() {
    double tpi = 2.0*Math.PI;
    for(int i = 0; i<numberOfParticles; ++i) {
      double r1 = Math.random();
      double r2 = Math.random()*tpi;
      vx[i] = Math.sqrt(-2*temperature*Math.log(r1))*Math.cos(r2); // sets random initial velocities
      vy[i] = Math.sqrt(-2*temperature*Math.log(r1))*Math.sin(r2);
    }
    // zero Momentum
    double vxsum = 0.0;
    double vysum = 0.0;
    for(int i = 0; i<numberOfParticles; ++i) {
      vxsum += vx[i];
      vysum += vy[i];
    }
    double vxcm = vxsum/numberOfParticles;
    double vycm = vysum/numberOfParticles;
    for(int i = 0; i<numberOfParticles; ++i) {
      vx[i] -= vxcm;
      vy[i] -= vycm;
    }
  }

  public void reset() {
    steps = 0;
    t = 0;
    n0 = 0;
    n1 = 0;
    n2 = 0;
    x0 = cellLength/3.0;
    x1 = 2*x0;
  }

  public void zeroAverages() {
    steps = 0;
    n0 = 0;
    n1 = 0;
    n2 = 0;
  }

  public void accel() {
    double dx, dy, fx, fy, r2, fOverR, rm2, rm6;
    for(int i = 0; i<numberOfParticles; i++) {
      ax[i] = 0;
      ay[i] = 0;
    }
    for(int i = 0; i<numberOfParticles-1; i++) {
      for(int j = i+1; j<numberOfParticles; j++) {
        dx = pbc(x[i]-x[j]);
        dy = pbc(y[i]-y[j]);
        r2 = dx*dx+dy*dy;
        if(r2<cut2) {
          rm2 = 1.0/r2;
          rm6 = rm2*rm2*rm2;
          fOverR = 48.0*rm6*(rm6-0.5)*rm2;
          fx = fOverR*dx;
          fy = fOverR*dy;
          ax[i] += fx;
          ay[i] += fy;
          ax[j] -= fx;
          ay[j] -= fy;
          //PEsum += 4*(rm6*rm6 - rm6);
          //Vsum += dx*fx + dy*fy;     //virial calculation
        }
      }
    }
  }

  private double pbc(double s) {
    if(s>0.5*cellLength) {
      s -= cellLength;
    } else if(s<-0.5*cellLength) {
      s += cellLength;
    }
    return s;
  }

  private double image(double s) {
    if(s>cellLength) {
      s -= cellLength;
    } else if(s<0) {
      s += cellLength;
    }
    return s;
  }

  public void step() { // Velocity Verlet algorithm
    double dt2half = 0.5*dt*dt;
    double dthalf = 0.5*dt;
    n0 = 0;
    n1 = 0;
    n2 = 0;
    for(int i = 0; i<numberOfParticles; i++) {
      x[i] += vx[i]*dt+ax[i]*dt2half;
      y[i] += vy[i]*dt+ay[i]*dt2half;
      x[i] = image(x[i]);
      y[i] = image(y[i]);
      vx[i] += ax[i]*dthalf;
      vy[i] += ay[i]*dthalf;
      if(x[i]<x0) {
        n0++;
      } else if(x[i]<x1) {
        n1++;
      } else {
        n2++;
      }
    }
    accel();
    for(int i = 0; i<numberOfParticles; i++) {
      vx[i] += ax[i]*dthalf;
      vy[i] += ay[i]*dthalf;
    }
    steps++;
    t += dt;
  }

  public void reverse() {
    for(int i = 0; i<numberOfParticles; i++) {
      vx[i] = -vx[i];
      vy[i] = -vy[i];
    }
  }

  public void draw(DrawingPanel myWorld, Graphics g) {
    if(x==null) {
      return;
    }
    int pxRadius = Math.abs(myWorld.xToPix(radius)-myWorld.xToPix(0));
    int pyRadius = Math.abs(myWorld.yToPix(radius)-myWorld.yToPix(0));
    if(pxRadius<1) {
      pxRadius = 1;
    }
    if(pyRadius<1) {
      pyRadius = 1;
    }
    for(int i = 0; i<numberOfParticles; i++) {
      int xpix = myWorld.xToPix(x[i])-pxRadius;
      int ypix = myWorld.yToPix(y[i])-pyRadius;
      Color color = Color.BLACK;
      if(x[i]<cellLength/3) color=Color.BLUE;
      if(x[i]>2*cellLength/3) color=Color.RED;
      g.setColor(color);
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    }
    g.setColor(Color.black);
    int xpix = myWorld.xToPix(0);
    int ypix = myWorld.yToPix(0); // bug fix by WC
    int lx = myWorld.xToPix(cellLength)-myWorld.xToPix(0);
    int ly = myWorld.yToPix(cellLength)-myWorld.yToPix(0);
    g.drawRect(xpix, myWorld.yToPix(cellLength), lx, -ly);  // bug fix by WC
    g.drawLine(xpix+lx/3, ypix, xpix+lx/3, ypix+ly);
    g.drawLine(xpix+2*lx/3, ypix, xpix+2*lx/3, ypix+ly);
    g.drawLine(xpix, ypix, xpix, ypix+ly);
    g.drawLine(xpix+lx, ypix, xpix+lx, ypix+ly);
    g.drawLine(xpix, ypix, xpix+lx, ypix);
    g.drawLine(xpix, ypix+ly, xpix+lx, ypix+ly);
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
