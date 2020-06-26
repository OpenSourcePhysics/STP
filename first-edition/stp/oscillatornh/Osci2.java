/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.oscillatornh;
import java.awt.Color;
import java.awt.Graphics;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

public class Osci2 implements Drawable {
  double x, vx, accel;
  double K = 1.0;
  double psi;
  double T = 2.5;
  double Q = 0.05;
  double s = 0;
  double t = 0.0;
  double Et;

  /**
   * Constructor Osci2
   */
  public Osci2() {
    vx = accel = 0;
    x = 1.0;
    psi = 0;
  }

  public void step(double dt) {
    double vo, vn, v2, psin, psio;
    double err = 1.0, err1 = 1.0;
    t += dt;
    //psi = 0;
    x += vx*dt+(accel-psi*vx)*0.5*dt*dt;
    vx += (accel-psi*vx)*0.5*dt;
    s += psi*dt+(vx*vx-T)*0.5*dt*dt/Q;
    psi += (vx*vx-T)*0.5*dt/Q;
    getAccel();
    vx += accel*0.5*dt;
    //Force is velocity dependent
    //iterate
    vn = vx;
    psin = psi;
    Et = 0.5*K*x*x+0.5*vx*vx; // + psi*psi*Q/2;
    //if(true)return;
    while((err>1e-10)||(err1>1e-10)) {

      vo = vn;
      psio = psin;
      v2 = vn*vn;
      psin = psi+(v2-T)*0.5*dt/Q;
      vn = vx-psin*vn*0.5*dt;
      err = Math.abs(vn-vo)/Math.abs(vo);
      err1 = Math.abs((psin-psio)/psio);
    }
    vx = vn;
    psi = psin;
    Et = 0.5*K*x*x+0.5*vx*vx+psi*psi*Q/2;
  }

  public double getAccel() {
    accel = -K*x;
    return accel;
  }

  public void draw(DrawingPanel myWorld, Graphics g) {
    double radius = 0.5/2.0;
    int pxRadius = Math.abs(myWorld.xToPix(radius)-myWorld.xToPix(0));
    int pyRadius = Math.abs(myWorld.yToPix(radius)-myWorld.yToPix(0));
    //Make sure circle size is not 0
    if(pxRadius<1) {
      pxRadius = 1;
    }
    if(pyRadius<1) {
      pxRadius = 1;
    }
    g.setColor(Color.red);
    int xpix = myWorld.xToPix(x)-pxRadius;
    g.fillOval(xpix, 10, 2*pxRadius, 2*pyRadius);
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
