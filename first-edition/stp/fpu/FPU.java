/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.fpu;

/**
 * The Fermi-Pasta-Ulam model.
 *
 * @author Hui Wang
 * @version 1.0
 * @created 05/21/2007
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.numerics.*;

public class FPU implements Drawable, ODE {
  public double[] state;
  public double[] ax;
  int N;                  // # of blocks
  double dt;
  double stiffness = 1.0; // Default k
  double spacing = 1.0;   // lattice spacing, only for plotting
  double alpha, beta;
  //Verlet odeSolver = new Verlet(this);
  ODEMultistepSolver odeSolver = new ODEMultistepSolver(this);
  /*
   *  Ek: energy in the kth mode;
   * Etot, total energy of particle i
   * pe: potential energy
   * ke: kinetic energy
   */
  double[] Ek, pe;
  Random rnd;
  double t = 0.0;
  double E = 0.0, KE = 0.0, PE = 0.0;
  double L;
  int R = 1;
  Metric peMetric;

  public void initialize(int n, int k, double _alpha, double b, double dt0, double e0) {
    N = n;
    state = new double[1+2*N]; // x0, vx0, x1, vx1, ... t;
    ax = new double[N];
    L = N*spacing;
    R = 1;
    alpha = _alpha;
    beta = b;
    dt = dt0;
    stiffness /= (1.0*R);
    beta /= (1.0*R);
    peMetric = new Metric(n);
    Ek = new double[N];
    pe = new double[N];
    odeSolver.setTolerance(0.00001);
    odeSolver.initialize(dt);
    rnd = new Random(1);
    initMode(k, e0);
  }

  public void initRadom(double e0) {
    double vc = 0;
    for(int i = 0; i<N; i++) {
      double v = rnd.nextDouble()-0.5;
      state[2*i+1] = v;
      vc += v;
    }
    //Zero center of mass vel
    vc /= N;
    for(int i = 0; i<N; i++) {
      state[2*i+1] -= vc;
    }
    //rescale velocities to make the initial KE = 0
    double e = getE();
    e /= N;
    double a;
    a = Math.sqrt(e0/e);
    for(int i = 0; i<N; i++) {
      state[2*i+1] *= a;
    }
  }

  public void initMode(int k, double e0) {
    double p = Math.PI;
    if(k==0) {                              // initialize the velocities randomly so that KE = e0;
      for(int i = 0; i<N; i++) {
        state[2*i] = 0.0;
      }
      initRadom(e0);
    } else {                                // initialize in mode k
      for(int i = 0; i<N; i++) {
        state[2*i] = Math.sin(p*k*i/(N-1)); // (Math.sqrt(N)/k)*Math.sin(p*k*i/N);// + i*spacing;
      }
      initRadom(0.0);
    }
  }

  public void doStep() {
    odeSolver.step();
    t = state[2*N];
    getE();
    peMetric.append(pe);
  }

  public void getRate(double[] _state, double[] rate) {
    double[] _ax = computeAcceleration(_state);
    for(int i = 0; i<N; i++) {
      rate[2*i] = _state[2*i+1]; // rates for positions are velocities
      rate[2*i+1] = _ax[i];      // rate for velocity is acceleration
    }
    rate[2*N] = 1; // dt/dt = 1
  }

  public double[] computeAcceleration(double[] _state) {
    int j;
    for(int i = 0; i<N; i++) {
      ax[i] = 0;
    }
    for(int i = 0; i<N-1; i++) {
      for(j = 1; j<R+1; j++) {
        int j0 = i+j;
        double dx = _state[2*j0]-_state[2*i];
        double a = force(dx);
        ax[i] += a;
        ax[j0] -= a;
      }
    }
    ax[0] = ax[N-1] = 0; // fixed ends
    return ax;
  }

  public double[] getState() {
    return state;
  }

  public void getEnergy() {
    for(int i = 1; i<N-1; i++) {
      Ek[i] = getEnergy(i);
    }
  }

  //calculate the energy in the mth mode
  public double getEnergy(int m) {
    double qk = 0, qkDot = 0;
    double pi = Math.PI;
    int n = N-2;
    for(int i = 1; i<=n; i++) {
      qk += state[2*i]*Math.sin(pi*m*i/(N-1));
      qkDot += state[2*i+1]*Math.sin(pi*m*i/(N-1));
    }
    qk *= 2*Math.sin(pi*m/2/(n+1));
    return 0.5*(qk*qk+qkDot*qkDot)*2/(N-1);
  }

  public double getE() {
    double dx;
    int j;
    for(int i = 0; i<N; i++) {
      pe[i] = 0.0;
    }
    for(int i = 0; i<N-1; i++) {
      for(j = 1; j<R+1; j++) {
        int j0 = i+j;
        dx = state[2*j0]-state[2*i];
        double v = potential(dx);
        v = 0.5*v;
        pe[i] += v;
        pe[j0] += v;
      }
    }
    E = 0.0;
    KE = 0.0;
    PE = 0.0;
    for(int i = 0; i<N; i++) {
      double a = 0.5*state[2*i+1]*state[2*i+1];
      KE += a;
      PE += pe[i];
    }
    E = KE+PE;
    return E;
  }

  //  check energy in k space and real space  
  public void checkE() {
    getEnergy();
    double e = 0.0;
    for(int i = 1; i<N-1; i++) {
      e += Ek[i];
    }
    System.out.println(e+"\t"+getE());
  }

  public double setZeroMetric() {
    peMetric.reset();
    getE();
    peMetric.append(pe);
    double pm0 = peMetric.metric; // get Omega(0)
    peMetric.setM0(pm0);
    return pm0;
  }

  public double force(double dx) {
    return stiffness*dx+alpha*dx*dx+beta*dx*dx*dx;
  }

  public double potential(double dx) {
    return 0.5*stiffness*dx*dx+1.0/3.0*dx*dx*dx+0.25*beta*dx*dx*dx*dx;
  }

  public void resetMetric() {
    peMetric.reset();
    setZeroMetric();
    peMetric.zeroData();
    peMetric.counter = 0;
  }

  public void draw(DrawingPanel myWorld, Graphics g) {
    g.setColor(Color.red);
    double a = L/N; // get lattice spacing
    int pxRadius = (int) a;
    int pyRadius = (int) a;
    for(int i = 0; i<N; i++) {
      int xpix = myWorld.xToPix(i*a);
      int ypix = myWorld.xToPix(4*state[2*i]+L/2.0); // Exaggerate displacements in y
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
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
