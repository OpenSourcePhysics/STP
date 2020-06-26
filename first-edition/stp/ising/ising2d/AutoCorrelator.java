/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
import java.util.*;
import org.opensourcephysics.display.*;

/**
 * This class computes the auto correlation function of a time series
 * @author Peter Sibley
 */
public class AutoCorrelator {
  private int numberToSave;
  private double[] correlationArray;
  private double[] saveArray;
  private double aTotal;  // we will accumulate value of A
  private double a2Total; // and A squared then use the average in our calculations
  private int time;       // counter of number of elements we have processed.
  private boolean isComputed = false;
  private Dataset dataset;

  /**
   * Create a new AutoCorrelator object with a correlation buffer of size ten
   *
   */
  public AutoCorrelator() {
    dataset = new Dataset();
    setNumberToSave(10);
    setupVaribles();
  }

  /**
   *
   * @param _n, numberOf entries of the time series to save aka size of correlation buffer.
   * @see #setNumberToSave
   */
  public AutoCorrelator(int _n) {
    dataset = new Dataset();
    setNumberToSave(_n);
    setupVaribles();
  }

  /**
   *  intializes the correlation arrays , and zeros them out.
   */
  protected void setupArrays() {
    correlationArray = new double[numberToSave+1];
    saveArray = new double[numberToSave];
    Arrays.fill(correlationArray, 0); // use built-in funnctions instead of for loop.
    Arrays.fill(saveArray, 0);
  }

  /**
   * sets counters to zero.
   */
  protected void setupVaribles() {
    time = 0;
    aTotal = 0;
    a2Total = 0;
    isComputed = false;
  }

  /**
   *  This collects the data points from a time series.
   *  @param a , the data to add the the correlation buffer
   */
  public void updateStatistics(double a) {
    time++;
    int index0 = (time-1)%numberToSave; // find index of the oldest element.
    if(time>numberToSave) {
      int index = index0;                 // index is corrent index
      for(int tDiff = numberToSave; tDiff>=1; tDiff--) {
        correlationArray[tDiff] += a*saveArray[index];
        index = ((index+1)%numberToSave); // if index == number to save wrap to 0th index
      }
    }
    saveArray[index0] = a;
    aTotal += a;
    a2Total += (a*a);
  }

  /**
   * This computes and fills the auto correlation data points.
   * This should be invoked after you have invoked
   * updateStatistics for each element in the time series.
   * @see #getCorrelationDataset
   * @see #getCorrelationArray
   *
   */
  public void compute() {
    if(isComputed) {
      return;
    }
    double aAvg = aTotal/(time);
    double a2Avg = a2Total/(time); // average of the squares
    double aAvg2 = aAvg*aAvg;      // squuare of the average
    // set A_o the intial value to be sigma^2
    correlationArray[0] = (a2Avg-aAvg2);
    double norm = 1/((double) (time)-(double) numberToSave);
    for(int i = 1; i<=numberToSave; i++) {
      correlationArray[i] = (correlationArray[i]*norm-aAvg2)/correlationArray[0];
    }
    isComputed = true;
  }

  /**
   * This returns a Dataset with entries as follows: (time , Correlation_A(time) ).
   * The dataset starts at time = 1 , that data is already normalized (divided by Correlation_A(t0)).
   * The length of the dataset is the size of correlation buffer.
   * (if you have not invoked compute() , it will be invoked)
   * @return the Dataset.
   */
  public Dataset getCorrelationDataset() {
    if(!isComputed) {
      compute();
    }
    dataset.clear();
    for(int i = 1; i<=numberToSave; i++) {
      dataset.append(i, correlationArray[i]);
    }
    return dataset;
  }

  /**
   *
   * This returns an array in which each entry corresonds to Correlation_A(Index) )
   * The data starts at time = 0 , that data is already normalized (divided by Correlation_A(t0)) (except the zeroth element).
   * The length of the dataset is one more than the size of correlation buffer.
   * (if you have not invoked compute() , it will be invoked)
   * @return the data.
   */
  public double[] getCorrelationArray() {
    if(!isComputed) {
      compute();
    }
    return(double[]) correlationArray.clone();
  }

  /**
   * This sets the number of entries in the Time series to save in the correlation buffer, a larger number will
   *  give you a better approximation.
   *  This number should be at most one less than the length of the time series, and greather than zero.
   * @param _n , number of entries in the time series to save
   */
  public void setNumberToSave(int _n) {
    numberToSave = _n;
    setupArrays();
  }

  /**
   * This returns a the string formatted as follows . Starting from time = 0;
   *  time :\t correlation_a(time)
   * (if you have not invoked compute() , it will be invoked)
   * @return the string representation.A
   */
  public String toString() {
    if(!isComputed) {
      compute();
    }
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i<=numberToSave; i++) {
      buffer.append(i);
      buffer.append(":\t");
      buffer.append(correlationArray[i]);
      buffer.append("\n");
    }
    return(buffer.toString());
  }

  /**
   * Resets the AutoCorrelator , includeing the size of the correlation buffer
   * so you can process a new time series.
   */
  public void reset() {
    dataset.clear();
    setNumberToSave(10);
    setupVaribles();
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
