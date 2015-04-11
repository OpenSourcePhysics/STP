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
 * @created 11/01/2006
 */
public class Metric {
  int numOfPoints;
  double[] data, data_av;
  int counter;
  double metric;
  double m0 = 1.0;

  /**
   * Constructor Metric
   * @param n
   */
  public Metric(int n) {
    numOfPoints = n;
    data = new double[n];
    data_av = new double[n];
    for(int i = 1; i<n; i++) {
      data[i] = data_av[i] = 0.0;
    }
    counter = 0;
  }

  public double append(double[] e) {
    counter++;
    for(int i = 1; i<e.length; i++) {
      data[i] += e[i];
      data_av[i] = data[i]/counter;
    }
    double s = 0.0, s2 = 0.0;
    for(int i = 1; i<e.length; i++) {
      s += data_av[i];
      s2 += data_av[i]*data_av[i];
    }
    s /= (e.length-1);
    s2 /= (e.length-1);
    metric = s2-s*s;
    metric /= e.length;
    metric /= m0;
    return metric;
  }

  public void setM0(double m) {
    m0 = m;
    //System.out.println("#Zero metric is " + 1/m);
  }

  public void zeroData() {
    for(int i = 0; i<numOfPoints; i++) {
      data[i] = data_av[i] = 0.0;
    }
  }

  public void reset() {
    zeroData();
    counter = 0;
    m0 = 1;
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
