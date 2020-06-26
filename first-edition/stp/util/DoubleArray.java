/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.util;

/**
 * Dynamic double arrays.
 * @author Kipton Barros
 */
public class DoubleArray {
  private int length;
  private double[] data;

  /*
   * Static methods
   */

  /**
   * Concatenates two double[]s into one.
   *
   * @param a
   * @param b
   * @return a+b
   */
  public static double[] concat(double[] a, double[] b) {
    double[] ret = new double[a.length+b.length];
    System.arraycopy(a, 0, ret, 0, a.length);
    System.arraycopy(b, 0, ret, a.length, b.length);
    return ret;
  }

  /**
   * Finds the index i which maximizes a[i]
   *
   * @param a
   * @return maximizing index
   */
  public static int maxIndex(double[] a) {
    // assert (a.length > 0);
    int maxi = 0;
    double max = a[0];
    for(int i = 1; i<a.length; i++) {
      if(a[i]>max) {
        max = a[i];
        maxi = i;
      }
    }
    return maxi;
  }

  /**
   * Finds the index i which minimizes a[i]
   *
   * @param a
   * @return minimizing index
   */
  public static int minIndex(double[] a) {
    // assert (a.length > 0);
    int mini = 0;
    double min = a[0];
    for(int i = 1; i<a.length; i++) {
      if(a[i]<min) {
        min = a[i];
        mini = i;
      }
    }
    return mini;
  }

  /**
   * Finds the maximum value in a[]
   *
   * @param a
   * @return max{a}
   */
  public static double max(double[] a) {
    return a[maxIndex(a)];
  }

  /**
   * Finds the minimum value in a[]
   *
   * @param a
   * @return min{a}
   */
  public static double min(double[] a) {
    return a[minIndex(a)];
  }

  /*
   * Dynamic methods
   */

  /**
   * Constructs a new DoubleArray
   */
  public DoubleArray() {
    data = new double[128];
    length = 0;
  }

  /**
   * Returns the length of the array
   */
  public int length() {
    return length;
  }

  /**
   * Appends a value
   *
   * @param x
   */
  public void append(double x) {
    if(length>=data.length) {
      increaseCapacity();
    }
    data[length++] = x;
  }

  /**
   * Gets an indexed value
   *
   * @param i
   * @return array[i]
   */
  public double get(int i) {
    if(i>=length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return data[i];
  }

  private void increaseCapacity() {
    double[] temp = new double[2*length];
    System.arraycopy(data, 0, temp, 0, length);
    data = temp;
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
