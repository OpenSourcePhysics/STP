/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.util;

/**
 * Extra math functions
 * @author Kipton Barros
 */
public class MathPlus {
  /**
   * Returns a random double in [lo, hi] with uniform probability distribution
   *
   * @param lo
   * @param hi
   * @return r in [lo, hi]
   */
  public static double random(double lo, double hi) {
    // assert (lo < hi);
    return Math.random()*(hi-lo)+lo;
  }

  /**
   * Returns a random int in [lo, hi] with uniform probability distribution
   *
   * @param lo
   * @param hi
   * @return r in [lo, hi]
   */
  public static int randomInt(int lo, int hi) {
    // assert (lo < hi);
    return(int) random(lo, hi+1-1e-10);
  }

  /**
   * Performs the square operation
   *
   * @param x
   * @return x*x
   */
  public static double sqr(double x) {
    return x*x;
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
