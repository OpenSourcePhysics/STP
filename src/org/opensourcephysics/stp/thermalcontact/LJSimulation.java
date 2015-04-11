/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.thermalcontact;
public class LJSimulation {
  public double[] x, y, vx, vy;
  double[] ax, ay;
  double Lx, Ly;
  public int Na, Nb, N;
  double sigma_aa = 1;
  double sigma_bb = 1;
  double sigma_ab = 1;
  double sigma_w = 1;
  double epsilon_aa = 1;
  double epsilon_bb = 1;
  double epsilon_ab = 1;
  double dt;
  double r_cutoff = Double.MAX_VALUE; // No cutoff
  public double Ka, Kb, Va, Vb;

  /**
   * Constructor LJSimulation
   * @param _Na
   * @param _Nb
   * @param _Lx
   * @param _Ly
   * @param _dt
   */
  public LJSimulation(int _Na, int _Nb, double _Lx, double _Ly, double _dt) {
    Lx = _Lx;
    Ly = _Ly;
    Na = _Na;
    Nb = _Nb;
    N = Na+Nb;
    dt = _dt;
    x = new double[N];
    y = new double[N];
    vx = new double[N];
    vy = new double[N];
    ax = new double[N];
    ay = new double[N];
    setHexagonalPositions();
  }

  public void setInteractionCoefficients(double _sigma_aa, double _sigma_bb, double _sigma_w, double _epsilon_aa,
                                         double _epsilon_bb) {
    sigma_aa = _sigma_aa;
    sigma_bb = _sigma_bb;
    sigma_ab = 0.5*(sigma_aa+sigma_bb);
    sigma_w = _sigma_w;
    epsilon_aa = _epsilon_aa;
    epsilon_bb = _epsilon_bb;
    epsilon_ab = 0.5*(epsilon_aa+epsilon_bb);
  }

  public void setTimeStep(double _dt) {
    dt = _dt;
  }

  void setHexagonalPositions() {
    double Nx = Math.sqrt(N*Lx*Math.sqrt(3)/(Ly*2));
    double Ny = N/Nx;
    int _Nx = (int) Math.ceil(Nx-0.0000001);
    int _Ny = (int) Math.ceil(Ny-0.0000001);
    double dx = Lx/(_Nx+1.5);
    double dy = Ly/(_Ny+1);
    int cnt = 0;
    for(int i = 0; i<_Ny; i++) {
      for(int j = 0; j<_Nx; j++) {
        if(cnt>=N) {
          return;
        }
        x[cnt] = (1+j+0.5*(i%2))*dx;
        y[cnt] = (1+i)*dy;
        cnt++;
      }
    }
  }

  // returns {acceleration, potential}
  double[] wallAccel(double x, double L) {
    double[] ret = new double[2];
    double r;
    if((x<r_cutoff)&&(x<L/2)) {
      r = x;
    } else if((L-x<r_cutoff)&&(x>L/2)) {
      r = x-L;
    } else {
      return ret;
    }
    double ir2 = (sigma_w*sigma_w)/(r*r);
    double ir6 = ir2*ir2*ir2;
    ret[0] = (48*ir6-24)*ir6/r;
    ret[1] = (4*ir6-4)*ir6;
    return ret;
  }

  double sigma2(int i, int j) {
    if((i<Na)&&(j<Na)) {
      return sigma_aa*sigma_aa;
    }
    if(i<Na) {
      return sigma_ab*sigma_ab;
    } else {
      return sigma_bb*sigma_bb;
    }
  }

  double epsilon(int i, int j) {
    if((i<Na)&&(j<Na)) {
      return epsilon_aa;
    }
    if(i<Na) {
      return epsilon_ab;
    } else {
      return epsilon_bb;
    }
  }

  // returns {accel_x, accel_y, potential}
  double[] particleAccel(int i, int j) {
    double[] ret = new double[3];
    double dx = x[i]-x[j];
    double dy = y[i]-y[j];
    double r2 = dx*dx+dy*dy;
    if(r2<r_cutoff*r_cutoff) {
      double eps = epsilon(i, j);
      double ir2 = sigma2(i, j)/r2;
      double ir6 = ir2*ir2*ir2;
      ret[0] = eps*((48*ir6-24)*ir6/r2)*dx;
      ret[1] = eps*((48*ir6-24)*ir6/r2)*dy;
      ret[2] = eps*(4*ir6-4)*ir6;
    }
    return ret;
  }

  public void accel() {
    for(int i = 0; i<N; i++) {
      double[] ret1, ret2;
      ret1 = wallAccel(x[i], Lx);
      ret2 = wallAccel(y[i], Ly);
      ax[i] = ret1[0];
      ay[i] = ret2[0];
      if(i<Na) {
        Va += ret1[1]+ret2[1];
      } else {
        Vb += ret1[1]+ret2[1];
      }
    }
    for(int i = 0; i<N-1; i++) {
      for(int j = i+1; j<N; j++) {
        double[] ret = particleAccel(i, j);
        ax[i] += ret[0];
        ay[i] += ret[1];
        ax[j] -= ret[0];
        ay[j] -= ret[1];
        double p = ((i<Na)
                    ? 0.5
                    : 0)+((j<Na)
                          ? 0.5
                          : 0);
        Va += ret[2]*p;
        Vb += ret[2]*(1-p);
      }
    }
  }

  public void step() {
    Va = Vb = Ka = Kb = 0;
    for(int i = 0; i<N; i++) {
      x[i] += vx[i]*dt+ax[i]*dt*dt/2;
      y[i] += vy[i]*dt+ay[i]*dt*dt/2;
      vx[i] += ax[i]*dt/2;
      vy[i] += ay[i]*dt/2;
    }
    accel();
    for(int i = 0; i<N; i++) {
      vx[i] += ax[i]*dt/2;
      vy[i] += ay[i]*dt/2;
      double K = (vx[i]*vx[i]+vy[i]*vy[i])/2;
      if(i<Na) {
        Ka += K;
      } else {
        Kb += K;
      }
    }
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
