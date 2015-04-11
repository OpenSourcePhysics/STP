/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.saw;

/**
 *  Flat histogram method for self avoiding walkers
 *  @author Jan Tobochnik
 *  7/14/06
 */
public class Flat {
  double xAccum, xSquaredAccum; // accumulated data on displacement of walkers, index is time
  double yAccum, ySquaredAccum; // accumulated data on displacement of walkers, index is time
  double norm;
  int numberOfWalkers;          // not fixed
  OneWalker[] walker;
  int remove[];
  int maxWalkers;

  public void initialize() {
    xAccum = 0;
    yAccum = 0;
    xSquaredAccum = 0;
    ySquaredAccum = 0;
    norm = 0;
    maxWalkers = (int) (numberOfWalkers*5);
    walker = new OneWalker[maxWalkers];
    remove = new int[maxWalkers];
    for(int i = 0; i<numberOfWalkers; i++) {
      walker[i] = new OneWalker();
      walker[i].newWalker(this);
    }
  }

  public void step() {
    xAccum = 0;
    yAccum = 0;
    xSquaredAccum = 0;
    ySquaredAccum = 0;
    norm = 0;
    int numberToRemove = 0;
    for(int i = 0; i<numberOfWalkers; i++) {
      walker[i].step(this);
    }
    int currentNumberOfWalkers = numberOfWalkers;
    for(int i = 0; i<currentNumberOfWalkers; i++) {
      double r = currentNumberOfWalkers*walker[i].weight/norm;
      if(r>1) {
        int numberOfCopies = (int) r;
        if(walker[i].m<numberOfCopies) {
          numberOfCopies = walker[i].m;
        }
        double newWeight = walker[i].weight/numberOfCopies;
        for(int j = 0; j<numberOfCopies; j++) {
          if(numberOfWalkers<maxWalkers) {
            walker[numberOfWalkers] = new OneWalker();
            walker[numberOfWalkers].copyWalker(walker[i], newWeight);
            numberOfWalkers++;
          }
        }
      } else if(Math.random()<1-r) { // will eliminate walker
        remove[numberToRemove] = i;  // make list of walkers to remove
        numberToRemove++;
      }
    }
    for(int k = 0; k<numberToRemove; k++) {
      numberOfWalkers--;
      walker[remove[k]] = walker[numberOfWalkers]; // move walkers at end of array to removed walker spots
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
