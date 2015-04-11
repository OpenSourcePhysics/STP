/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.sensitive;
import org.opensourcephysics.display.*;
import java.awt.*;

public class LJgas implements Drawable {
  double x[], y[], vx[], vy[], ax[], ay[];
  int numberOfParticles = 11;
  double cellLength = 11;
  double x0, x1;
  int steps = 0;
  double dt = 0.01;
  double radius = 0.5;
  double cut2 = 3.0*3.0;
  double t = 0;
  double timeDirection = 1;

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
    steps = 0;
    t = 0;
    dt = 0.01;
    numberOfParticles = 11;
    cellLength = 11.0;
    timeDirection = 1;
    setArrays();
    setPositions();
    setVelocities();
    accel();
  }

  public void zeroAverages() {
    steps = 0;
  }

  public void step() { // Velocity Verlet algorithm
    double dt2half = 0.5*dt*dt;
    double dthalf = 0.5*dt;
    for(int i = 0; i<numberOfParticles; i++) {
      x[i] += vx[i]*dt+ax[i]*dt2half;
      y[i] += vy[i]*dt+ay[i]*dt2half;
      x[i] = image(x[i]);
      y[i] = image(y[i]);
      vx[i] += ax[i]*dthalf;
      vy[i] += ay[i]*dthalf;
    }
    accel();
    for(int i = 0; i<numberOfParticles; i++) {
      vx[i] += ax[i]*dthalf;
      vy[i] += ay[i]*dthalf;
    }
    steps++;
    t += timeDirection*dt;
  }

  public void perturb(double a) {
    vx[6] *= a;
  }

  public void reverse() {
    for(int i = 0; i<numberOfParticles; i++) {
      vx[i] = -vx[i];
      vy[i] = -vy[i];
    }
    timeDirection *= -1;
  }

  public void draw(DrawingPanel myWorld, Graphics g) {
    if(x==null) {
      return;
    }
    int pxRadius = Math.abs(myWorld.xToPix(radius)-myWorld.xToPix(0));
    int pyRadius = Math.abs(myWorld.yToPix(radius)-myWorld.yToPix(0));
    g.setColor(Color.red);
    for(int i = 0; i<numberOfParticles; i++) {
      int xpix = myWorld.xToPix(x[i])-pxRadius;
      int ypix = myWorld.yToPix(y[i])-pyRadius;
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    }
    g.setColor(Color.black);
    int xpix = myWorld.xToPix(0);
    int ypix = myWorld.yToPix(cellLength); // bug fix WC
    int lx = myWorld.xToPix(cellLength)-myWorld.xToPix(0);
    int ly = myWorld.yToPix(0)-myWorld.yToPix(cellLength);// bug fix WC
    g.drawRect(xpix, ypix, lx, ly);
  }

  private void setPositions() {
    double halfLength = cellLength/2.0;
    double ds = cellLength/numberOfParticles;
    for(int i = 0; i<numberOfParticles; ++i) {
      x[i] = halfLength;
      y[i] = (i+0.5)*ds;
    }
  }

  private void setVelocities() {
    for(int i = 0; i<numberOfParticles; ++i) {
      vx[i] = 1.0; // sets random initial velocities
      vy[i] = 0;
    }
  }

  private void accel() {
    double dx;
    double dy;
    double fx;
    double fy;
    double r2;
    double fOverR;
    double rm2;
    double rm6;
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
