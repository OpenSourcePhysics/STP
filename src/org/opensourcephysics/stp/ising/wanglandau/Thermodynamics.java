/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.wanglandau;
public class Thermodynamics {
  static double logZ(int N, double[] g, double beta) {
    // m = max {e^(g - beta E)}
    double m = 0;
    for(int E = -2*N; E<=2*N; E += 4) {
      m = Math.max(m, g[E+2*N]-beta*E);
    }
    // s = Sum {e^(g - beta E)} * e^(-m)
    // => s = Z * e^(-m)
    // => log s = log Z - m
    double s = 0;
    for(int E = -2*N; E<=2*N; E += 4) {
      s += Math.exp(g[E+2*N]-beta*E-m);
    }
    return Math.log(s)+m;
  }

  static double heatCapacity(int N, double[] g, double beta) {
    double logZ = logZ(N, g, beta);
    double E_avg = 0;
    double E2_avg = 0;
    for(int E = -2*N; E<=2*N; E += 4) {
      if(g[E+2*N]==0) {
        continue;
      }
      E_avg += E*Math.exp(g[E+2*N]-beta*E-logZ);
      E2_avg += E*E*Math.exp(g[E+2*N]-beta*E-logZ);
    }
    return(E2_avg-E_avg*E_avg)*beta*beta;
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
