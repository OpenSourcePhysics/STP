/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
public class Ising2DAnti extends Ising2D {
  double Sm_acc = 0, Sm2_acc = 0;

  public void accumulate_EM() {
    super.accumulate_EM();
    double sm = getStaggeredM();
    Sm_acc += sm;
    Sm2_acc += sm*sm;
  }

  public double getStaggeredM() {
    double sm = 0;
    for(int i = 0; i<L; ++i) {
      for(int j = 0; j<L; ++j) {
        if((i+j)%2==0) {
          sm += spin[i][j];
        }
      }
    }
    return sm;
  }

  public double Staggeredsusceptibility() {
    double M2_avg = Sm2_acc/mcs;
    double M_avg = Sm_acc/mcs;
    return(M2_avg-M_avg*M_avg)/(T*N);
  }

  public void resetData() {
    super.resetData();
    Sm_acc = 0;
    Sm2_acc = 0;
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
