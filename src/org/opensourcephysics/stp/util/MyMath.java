/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.util;
public class MyMath {
  private MyMath() {} // don't allow users to instantiate this class

  public static double logBase10(double n) {
    return Math.log(n)/Math.log(10);
  }

  public static int factorial(int n) {
    if(n<0) {
      throw new IllegalArgumentException("Can't compute the factorial of a number less than 0");
    }
    int total = 1;
    for(int i = n; i>1; i--) {
      total *= i;
    }
    return total;
  }

  public static double factorial(double n) {
    if(n<0) {
      throw new IllegalArgumentException("Can't compute the factorial of a number less than 0");
    }
    double total = 1;
    for(double i = n; i>1; i--) {
      total *= i;
    }
    return total;
  }

  public static double stirling(int n) {
    if(n==0) {
      return 0;
    }
    double result = n*Math.log(n)-n+0.5*Math.log(2.0*Math.PI*n);
    return result;
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
