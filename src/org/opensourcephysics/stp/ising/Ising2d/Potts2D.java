/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.Ising2d;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display2d.CellLattice;

public class Potts2D implements Drawable {
  public int[][] spin;
  public int[] state;
  public int maxState;
  public int L;
  public int N;                // Number of sites
  public int q=8;                // q states Potts model
  public double criticalTemperature = 1.0/Math.log(1.0+Math.sqrt(1.0*q));
  public double J = 1.0;       // Interaction strength
  public double T;             // Temperature
  public double H;             // External magnetic field
  public double E,order;       // System energy and order parameter
  public double E_acc;         // E accumulator
  public double E2_acc;        // E^2 accumulator
  public int M;                // System magnetization
  public double M_acc;         // M accumulator
  public double absM_acc;      // |M| acumulator
  public double M2_acc;        // M^2 accumulator
  public int mcs;              // Number of MC moves per spin
  public int acceptedMoves;    // Used to determine acceptance ratio
  private CellLattice lattice; // Used only for drawing

  public void initialize(int _L, double _T, double _H) {
    L = _L;
    N = L*L;
    T = _T;
    H = _H;
    state = new int[q];
    lattice = new CellLattice(L, L); // only used for drawing
    lattice.setIndexedColor(0, Color.red);
    lattice.setIndexedColor(1, Color.green);
    lattice.setIndexedColor(2, Color.blue);
    lattice.setIndexedColor(3, Color.yellow);
    lattice.setIndexedColor(4, Color.magenta);
    lattice.setIndexedColor(5, Color.black);
    lattice.setIndexedColor(6, Color.white);
    lattice.setIndexedColor(7, Color.orange);
    // all spins up
    spin = new int[L][L];
    for(int i = 0; i<L; ++i) {
      for(int j = 0; j<L; ++j) {
        spin[i][j] = 0;
      }
    }
    state[0] = N;
    maxState = 0;
    M = N; 
    E = -2*J*N-H*M;
    resetData();
  }

  public void setTemperature(double _T) {
    T = _T;
  }

  public void setExternalField(double _H) {
    E += H*M-_H*M;
    H = _H;
  }

  public double specificHeat() {
    int mcs = (this.mcs==0)
              ? 1
              : this.mcs;
    double E2_avg = E2_acc/mcs;
    double E_avg = E_acc/mcs;
    return(E2_avg-E_avg*E_avg)/(T*T*N);
  }

  public double susceptibility() {
    int mcs = (this.mcs==0)
              ? 1
              : this.mcs;
    double M2_avg = M2_acc/mcs;
    double M_avg = M_acc/mcs;
    return(M2_avg-M_avg*M_avg)/(T);
  }

  public void resetData() {
    mcs = 0;
    E_acc = 0;
    E2_acc = 0;
    absM_acc = 0;
    M_acc = 0;
    M2_acc = 0;
    acceptedMoves = 0;
  }

  public void doOneMCStep() {
    for(int k = 0; k<N; ++k) {
      int i = (int) (Math.random()*L);
      int j = (int) (Math.random()*L);
      int trialSpin = (spin[i][j] + 1 + (int)(Math.random()*(q+1))) % q;
      int Eold = 0;
      if(spin[i][j] == spin[(i+1)%L][j]) Eold--;
      if(spin[i][j] == spin[(i-1+L)%L][j]) Eold--;
      if(spin[i][j] == spin[i][(j+1)%L]) Eold--;
      if(spin[i][j] == spin[i][(j-1+L)%L]) Eold--;
      int Enew = 0;
      if(trialSpin == spin[(i+1)%L][j]) Enew--;
      if(trialSpin == spin[(i-1+L)%L][j]) Enew--;
      if(trialSpin == spin[i][(j+1)%L]) Enew--;
      if(trialSpin == spin[i][(j-1+L)%L]) Enew--;
      double dE = Enew - Eold;
      if((dE<=0)||(Math.random()<Math.exp(-dE/T))) {
    	state[spin[i][j]]--;
    	state[trialSpin]++;
        spin[i][j] = trialSpin;
        acceptedMoves++;
        E += dE;
         M = state[0];
         for(int qq = 1; qq < q; qq++)
        	   if(state[qq] > M){
        		   M = state[qq];
        	   }
      }
    }
    accumulate_EM();
    mcs++;
  }

  public void doOneWolffStep(double bondProbability) {
    HashMap<Integer, Integer> wolffCluster = growWolffCluster(bondProbability);
    int size = wolffCluster.size();
    for(int i = 0; i<size; i++) {
      int x = (Integer) wolffCluster.get(i)%L;
      int y = (Integer) wolffCluster.get(i)/L;
      int spinDirection = spin[x][y];
      spin[x][y] *= -1;;
      double dE = 2*spinDirection
                  *(this.J
                    *(this.spin[(x-1+L)%L][y]+this.spin[(x+1+L)%L][y]+this.spin[x][(y-1+L)%L]+this.spin[x][(y+1+L)%L]));
      this.M += -2*spinDirection;
      this.E += dE;
    }
    accumulate_EM();
    mcs++;
  }

  public HashMap<Integer, Integer> growWolffCluster(double bondProbability) {
    Random r = new Random();
    HashMap<Integer, Integer> wolffSpins = new HashMap<Integer, Integer>(100);
    boolean visit[] = new boolean[this.N];
    int stackSize = 0;
    int stackPointer = 0;
    int firstSpinX = r.nextInt(this.L);
    int firstSpinY = r.nextInt(this.L);
    int firstSpin = firstSpinX+this.L*firstSpinY;
    int direction = spin[firstSpinX][firstSpinY];
    wolffSpins.put(stackPointer, firstSpin);
    stackSize++;
    visit[firstSpin] = true;
    while(stackPointer<stackSize) {
      int currentSpin = wolffSpins.get(stackPointer);
      ArrayList<Integer> neighborList = new ArrayList<Integer>();
      int currentX = currentSpin%L;
      int currentY = currentSpin/L;
      neighborList.add(currentX+((currentY-1+L)%L)*L);
      neighborList.add(currentX+((currentY+1+L)%L)*L);
      neighborList.add((currentX-1+L)%L+currentY*L);
      neighborList.add((currentX+1+L)%L+currentY*L);
      visit[currentSpin] = true;
      int numAdded = 0;
      for(int nextSpin : neighborList) {
        boolean visited = visit[nextSpin];
        boolean inSameDir = (spin[nextSpin%this.L][nextSpin/this.L]==direction);
        boolean occupyBond = (r.nextDouble()<bondProbability);
        if(!visited) {
          if(inSameDir&&occupyBond) {
            if(!wolffSpins.containsValue(nextSpin)) {
              wolffSpins.put(stackSize, nextSpin);
              stackSize++;
            }
            numAdded++;
          }
        }
      }
      stackPointer++;
    }
    return wolffSpins;
  }

  public void accumulate_EM() {
    E_acc += E;
    E2_acc += E*E;
    order = ((q*1.0*M/N)-1)/(q-1);
     M_acc += order;
    //absM_acc += Math.abs(M);
    M2_acc += order*order;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(lattice==null) {
      return;
    }
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        lattice.setValue(i, j, (byte) (spin[i][j]));
      }
    }
    lattice.draw(panel, g);
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
