/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.qmc;

/**
 * Monte Carlo simulation of ideal quantum gases
 *
 * @author Jan Tobochnik
 * @version 1.0  11/16/06
 */
public class QMC {
  public int[] x, y, z, occupation, occupationAccum, state;
  public double[] energy, densityOfStates;
  public int dimension, maxEnergy;
  public double p, p2, dE;
  public int kmax, np, arraySize, occupiedIndexMax;
  public double beta; // 1/temperature
  public int mcs, accept;
  public double energyAccum, energySqAccum;

  public void initial(int dimension, int kmax, double p, int np, int statistics, double dE) {
    this.dimension = dimension;
    this.kmax = kmax;
    this.p = p;
    this.np = np;
    this.dE = dE;
    p2 = p/2;
    maxEnergy = (int) Math.pow(kmax, p);
    densityOfStates = new double[(int) ((maxEnergy+dE)/dE)];
    arraySize = findArraySize();
    x = new int[arraySize];
    y = new int[arraySize];
    z = new int[arraySize];
    occupation = new int[arraySize];
    energy = new double[arraySize];
    state = new int[np];
    orderLevels();
    if(statistics==1) {
      for(int i = 0; i<np; i++) {
        occupation[i] = 1; // put one particle in each of the lowest np states
        state[i] = i;
      }
    } else {
      occupation[0] = np;
      for(int i = 0; i<np; i++) {
        state[i] = 0;      // put all particles in ground state
      }
    }
    mcs = 0;
    occupationAccum = new int[arraySize];
    occupiedIndexMax = np;
    energyAccum = energySqAccum = 0;
  }

  public void resetData() {
    occupationAccum = new int[arraySize];
    mcs = 0;
    energyAccum = energySqAccum = 0;
  }

  public void stepFD() {
    // one MC step per particle
    int trialState = 0;
    for(int j = 0; j<np; j++) {
      int i = (int) (Math.random()*np);
      if(((Math.random()>0.5)&&(state[i]<arraySize-2))||(state[i]==0)) {
        trialState = state[i]+1;
      } else {
        trialState = state[i]-1;
      }
      if(occupation[trialState]==0) {
        double dE = energy[trialState]-energy[state[i]];
        if((dE<0)||(Math.exp(-dE*beta)>=Math.random())) {
          occupation[state[i]] = 0;
          occupation[trialState] = 1;
          state[i] = trialState;
          accept++;
          if(trialState>occupiedIndexMax) {
            occupiedIndexMax = trialState;
          }
        }
      }
    }
    double ener = 0;
    for(int i = 0; i<=occupiedIndexMax; i++) {
      occupationAccum[i] += occupation[i];
      ener += energy[i]*occupation[i];
    }
    energyAccum += ener;
    energySqAccum += ener*ener;
    mcs++;
  }

  public void stepBE() {
    // one MC step per particle
    int trialState = 0;
    for(int j = 0; j<np; j++) {
      boolean ok = false;
      int i = (int) (Math.random()*np);
      if(((Math.random()>0.5)&&(state[i]<arraySize-2))||(state[i]==0)) {
        trialState = state[i]+1;
        if((i==np-1)||(state[i+1]>=trialState)) {
          ok = true;
        }
      } else {
        trialState = state[i]-1;
        if((i==0)||(state[i-1]<=trialState)) {
          ok = true;
        }
      }
      if(ok) {
        double dE = energy[trialState]-energy[state[i]];
        if((dE<0)||(Math.exp(-dE*beta)>=Math.random())) {
          occupation[state[i]]--;
          occupation[trialState]++;
          state[i] = trialState;
          accept++;
          if(trialState>occupiedIndexMax) {
            occupiedIndexMax = trialState;
          }
        }
      }
    }
    double ener = 0;
    for(int i = 0; i<=occupiedIndexMax; i++) {
      occupationAccum[i] += occupation[i];
      ener += energy[i]*occupation[i];
    }
    energyAccum += ener;
    energySqAccum += ener*ener;
    mcs++;
  }

  public void stepMB() {
    // one MC step per particle
    int trialState = 0;
    for(int j = 0; j<np; j++) {
      int i = (int) (Math.random()*np);
      if(((Math.random()>0.5)&&(state[i]<arraySize-2))||(state[i]==0)) {
        trialState = state[i]+1;
      } else {
        trialState = state[i]-1;
      }
      double dE = energy[trialState]-energy[state[i]];
      if((dE<0)||(Math.exp(-dE*beta)>=Math.random())) {
        occupation[state[i]]--;
        occupation[trialState]++;
        state[i] = trialState;
        accept++;
        if(trialState>occupiedIndexMax) {
          occupiedIndexMax = trialState;
        }
      }
    }
    double ener = 0;
    for(int i = 0; i<=occupiedIndexMax; i++) {
      occupationAccum[i] += occupation[i];
      ener += energy[i]*occupation[i];
    }
    energyAccum += ener;
    energySqAccum += ener*ener;
    mcs++;
  }

  public int findArraySize() {
    int size = 0;
    int kmax2 = kmax*kmax;
    switch(dimension) {
       case 1 :
         size = 2*kmax;
       case 2 :
         for(int kx = -kmax; kx<=kmax; kx++) {
           for(int ky = -kmax; ky<=kmax; ky++) {
             if(kx*kx+ky*ky<=kmax2) {
               size++;
             }
           }
         }
       default :
         for(int kx = -kmax; kx<=kmax; kx++) {
           for(int ky = -kmax; ky<=kmax; ky++) {
             for(int kz = -kmax; kz<=kmax; kz++) {
               if(kx*kx+ky*ky+kz*kz<=kmax2) {
                 size++;
               }
             }
           }
         }
    }
    return size+1;
  }

  public void orderLevels() {
    int i = 0;
    int kmax2 = kmax*kmax;
    densityOfStates[0] = 1;
    switch(dimension) {
       case 1 :
         for(int kx = 1; kx<=kmax; kx++) {
           if(Math.random()<0.5) {
             i++;
             energy[i] = Math.pow(kx*kx, p2); // k^p
             x[i] = kx;
             i++;
             energy[i] = energy[i-1];
             x[i] = -kx;
             densityOfStates[(int) (energy[i]/dE)] += 2;
           } else {
             i++;
             energy[i] = Math.pow(kx*kx, p2); // k^p
             x[i] = -kx;
             i++;
             energy[i] = energy[i-1];
             x[i] = kx;
             densityOfStates[(int) (energy[i]/dE)] += 2;
           }
         }
         break;
       case 2 :
         for(int kx = -kmax; kx<=kmax; kx++) {
           for(int ky = -kmax; ky<=kmax; ky++) {
             if((kx*kx+ky*ky<=kmax2)&&((kx!=0)||(ky!=0))) {
               i++;
               insert(i, kx*kx+ky*ky, kx, ky, 0);
             }
           }
         }
         break;
       default :
         for(int kx = -kmax; kx<=kmax; kx++) {
           for(int ky = -kmax; ky<=kmax; ky++) {
             for(int kz = -kmax; kz<=kmax; kz++) {
               if((kx*kx+ky*ky+kz*kz<=kmax2)&&((kx!=0)||(ky!=0)||(kz!=0))) {
                 i++;
                 insert(i, kx*kx+ky*ky+kz*kz, kx, ky, kz);
               }
             }
           }
         }
         break;
    }
  }

  public void insert(int i, int k2, int kx, int ky, int kz) {
    double insertEnergy = Math.pow(k2, p2); // k^p
    int insertionLocation = binarySearch(i, insertEnergy);
    for(int j = i; j>insertionLocation; j--) {
      energy[j] = energy[j-1];
      x[j] = x[j-1];
      y[j] = y[j-1];
      z[j] = z[j-1];
    }
    energy[insertionLocation] = insertEnergy;
    densityOfStates[(int) (insertEnergy/dE)]++;
    x[insertionLocation] = kx;
    y[insertionLocation] = ky;
    z[insertionLocation] = kz;
  }

  public int binarySearch(int i, double insertEnergy) {
    int firstLocation = 0;
    int lastLocation = i;
    int middleLocation = (firstLocation+lastLocation)/2;
    // determine which half of list new number is in
    while(lastLocation-firstLocation>1) {
      if(insertEnergy<energy[middleLocation]) {
        lastLocation = middleLocation;
      } else {
        firstLocation = middleLocation;
      }
      middleLocation = (firstLocation+lastLocation)/2;
    }
    return lastLocation;
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
