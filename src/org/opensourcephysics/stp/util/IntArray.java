/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.util;

/**
 * Dynamic int arrays.
 * @author Kipton Barros
 */
public class IntArray {
  private int length;
  private int[] data;

  /**
   * Constructs a new DoubleArray
   */
  public IntArray() {
    data = new int[32];
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
  public void append(int x) {
    if(length>=data.length) {
      increaseCapacity();
    }
    data[length++] = x;
  }

  /**
   * Returns the last element, and decrements the array
   */
  public int unappend() {
    if(length==0) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return data[--length];
  }

  /**
   * Gets an indexed value
   *
   * @param i
   * @return array[i]
   */
  public int get(int i) {
    if(i>=length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return data[i];
  }

  /**
   * Sets value v to index i.  Grows the array by one if i=length()
   *
   * @param i
   * @param v
   */
  public void set(int i, int v) {
    if(i>length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    data[i] = v;
  }

  /**
   * Gets the entire array as an int[]
   *
   * @return Entire contents of array
   */
  public int[] getArray() {
    int[] ret = new int[length];
    System.arraycopy(data, 0, ret, 0, length);
    return ret;
  }

  /**
   * Returns true if array contains value
   *
   * @param v
   * return boolean
   */
  public boolean contains(int v) {
    for(int i = 0; i<length; i++) {
      if(data[i]==v) {
        return true;
      }
    }
    return false;
  }

  private void increaseCapacity() {
    int[] temp = new int[2*length];
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
