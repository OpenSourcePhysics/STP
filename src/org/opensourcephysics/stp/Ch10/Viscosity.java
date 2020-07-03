/*

l * Open Source Physics software is free software as described near the bottom of this code file.


 *

 * For additional information and documentation on Open Source Physics please see:

 * <http://www.opensourcephysics.org/>

 */

package org.opensourcephysics.stp.Ch10;
import java.awt.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.numerics.*;
import javax.swing.JFrame;


/**

 *Evolves a three-dimensional system of interacting particles

 * via the Lennard-Jones potential using a Verlet ODESolver.

 * calculates viscosity using the ideas in Florian Muller-Plathe, JCP 106, p. 6082 (1997).

 * @author Jan Tobochnik

 * @version 1.0 12/25/19

 */
public class Viscosity implements  ODE {
  public double state[];
  public double ax[], ay[],az[],zSave[];
  public int slab [][];
  public int nSlab[];
  public int N,n;       // N number of particles, N = 3*n*n*n
  public double Lx, Ly,Lz,a;
  public double rho = N/(Lx*Ly*Lz);
  public double desiredTemperature;
  public int steps = 0;
  public int stepAdd;  // number of steps between swapping hot and cold particle. 
  public double dt = 0.01;
  public double t;
  public double totalPotentialEnergyAccumulator;
  public double totalKineticEnergyAccumulator, totalKineticEnergySquaredAccumulator;
  public double virialAccumulator;
  public double radius = 0.5; // radius of particles on screen
  Verlet odeSolver = new Verlet(this);
  public int nstep;
  public int nn[][];
  public double transfer;
 
  public void initialize() {
    t = 0;
    rho = N/(Lx*Ly*Lz);
    state = new double[1+6*N];
    ax = new double[N];
    ay = new double[N];
    az = new double[N];
    nn = new int[N][300];
    zSave = new double[N];
    slab = new int[3*n][6*n*n];
    nSlab = new int[3*n];
    resetAverages();
    setPositions();
    setNNList();
    setVelocities();
    computeAcceleration();
    odeSolver.setStepSize(dt);
   }

  // end break
  // start break
  // setRandomPositions
  public void setPositions() { // particles placed at random, but not closer than rMinimumSquared
    double x = a/2;
    double y = a/2;
    double z = a/2;
    for(int i = 0; i<N; ++i) {
        state[6*i] = x;   // x
        state[6*i+2] = y; // y
        state[6*i+4] = z; // z
        x +=a;
        if(x > Lx) {
        	x = a/2;
        	y += a;
        	if(y > Ly){
        		y = a/2;
        		z += a;
        	}
        }
        int slabNumber = (int) (state[6*i+4]/a);
        slab[slabNumber][nSlab[slabNumber]] = i;
        nSlab[slabNumber]++;
     }
   
  }


  // end break
  // start break
  // setVelocities
  public void setVelocities() {
    double vxSum = 0.0;
    double vySum = 0.0;
    double vzSum = 0.0;
    for(int i = 0; i<N; ++i) {          // assign random initial velocities
      state[6*i+1] = Math.random()-0.5; // vx
      state[6*i+3] = Math.random()-0.5; // vy
      state[6*i+5] = Math.random()-0.5; // vy
      vxSum += state[6*i+1];
      vySum += state[6*i+3];
      vzSum += state[6*i+5];
         }
    // zero center of mass momentum
    double vxcm = vxSum/N; // center of mass momentum (velocity)
    double vycm = vySum/N;
    double vzcm = vzSum/N;
    double v2sum = 0;
    for(int i = 0; i<N; ++i) {
      state[6*i+1] -= vxcm;
      state[6*i+3] -= vycm;
      state[6*i+5] -= vzcm;
      v2sum += state[6*i+1]*state[6*i+1]+state[6*i+3]*state[6*i+3]+state[6*i+5]*state[6*i+5];
    }
    double scale = Math.sqrt(3*N*desiredTemperature/v2sum);
    for(int i = 0; i<N; ++i) {
        state[6*i+1] *=scale;
        state[6*i+3] *=scale;
        state[6*i+5] *=scale;
      }
  }
  
  public void scaleVelocities(){
	  double v2sum = 0;
	   for(int i = 0; i<N; ++i) {
		      v2sum += state[6*i+1]*state[6*i+1]+state[6*i+3]*state[6*i+3]+state[6*i+5]*state[6*i+5];
	   }
	   double scale = Math.sqrt(3*N*desiredTemperature/v2sum);
	   for(int i = 0; i<N; ++i) {
		   state[6*i+1] *=scale;
		   state[6*i+3] *=scale;
		   state[6*i+5] *=scale;
		}
  }
public void setNNList(){
    for(int i = 0; i<N-1; i++) {
    	nn[i][0] = -1;
        int nnIndex = 0;
	      for(int j = i+1; j<N; j++) {
		        double dx = pbcSeparation(state[6*i]-state[6*j], Lx);
		        double dy = pbcSeparation(state[6*i+2]-state[6*j+2], Ly);
		        double dz = pbcSeparation(state[6*i+4]-state[6*j+4], Lz);
		        double r2 = dx*dx+dy*dy+dz*dz;
		        if(r2 < 16) {
		        	nn[i][nnIndex] = j;
		        	nnIndex++;
		        	nn[i][nnIndex] = -1;
		        }
		  }
    }
}
  // end break
  // start break
  // averages
  public double getMeanTemperature() {
    return (2.0/3.0)*totalKineticEnergyAccumulator/(N*steps);
  }

  public double getMeanEnergy() {
    return totalKineticEnergyAccumulator/steps+totalPotentialEnergyAccumulator/steps;
  }

  public double getMeanPressure() {
    double meanVirial;
    meanVirial = virialAccumulator/steps;
    return 1.0+meanVirial/(3*N*getMeanTemperature()); // quantity PV/NkT
  }

 

 
  public double getHeatCapacity() {
    double meanTemperature = getMeanTemperature();
    double meanTemperatureSquared = (4.0/9.0)*totalKineticEnergySquaredAccumulator/(N*N*steps);
    // heat capacity related to fluctuations of temperature
    double sigma2 = meanTemperatureSquared- meanTemperature*meanTemperature;
    double denom = 1.0-(2.0/3.0)*N*sigma2/(meanTemperature*meanTemperature);
    //System.out.println("denom = " + denom + " " + sigma2);
    return 1.5*N/denom;
  }

  public void resetAverages() {
    steps = 0;
    virialAccumulator = 0;
    totalPotentialEnergyAccumulator = 0;
    totalKineticEnergyAccumulator = 0;
    totalKineticEnergySquaredAccumulator = 0;
    transfer = 0;
  }

  public void computeAcceleration() {
	    for(int i = 0; i<N; i++) {
	      ax[i] = 0;
	      ay[i] = 0;
	      az[i] = 0;
	         }
	    for(int i = 0; i<N-1; i++) {
//	      for(int j = i+1; j<N; j++) {
	    	int nnIndex = 0;
	    	while(nn[i][nnIndex] != -1){
	    		int j = nn[i][nnIndex];
	            double dx = pbcSeparation(state[6*i]-state[6*j], Lx);
	            double dy = pbcSeparation(state[6*i+2]-state[6*j+2], Ly);
	            double dz = pbcSeparation(state[6*i+4]-state[6*j+4], Lz);
	            double r2 = dx*dx+dy*dy+dz*dz;
	            //if(r2 > 16)System.out.println("neighbor error " + r2);
	            if(r2 < 9){
	            	double oneOverR2 = 1.0/r2;
			        double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
			        double fOverR = 48.0*oneOverR6*(oneOverR6-0.5)*oneOverR2;
			        double fx = fOverR*dx; // force in x-direction
			        double fy = fOverR*dy; // force in y-direction
			        double fz = fOverR*dz; // force in z-direction
			        ax[i] += fx;           // use Newton's third law
			        ay[i] += fy;
			        az[i] += fz;
			        ax[j] -= fx;
			        ay[j] -= fy;
			        az[j] -= fz;
			        totalPotentialEnergyAccumulator += 4.0*(oneOverR6*oneOverR6-oneOverR6);
			        virialAccumulator += dx*fx+dy*fy +dz*fz;
	            }
	            nnIndex++;
	      }
	    }
	  }

  

  // end break
  // start break
  // pbcSeparation
  private double pbcSeparation(double ds, double L) {
    if(ds>0) {
      while(ds>0.5*L) {
        ds -= L;
      }
    } else {
      while(ds<-0.5*L) {
        ds += L;
      }
    }
    return ds;
  }

  // end break
  // start break
  // pbcPosition
  private double pbcPosition(double s, double L) {
    if(s>0) {
      while(s>L) {
        s -= L;
      }
    } else {
      while(s<0) {
        s += L;
      }
    }
    return s;
  }

  // end break
  // start break
  // odeMethods
  public void getRate(double[] state, double[] rate) {
    // getRate is called twice for each call to step.
    // accelerations computed for every other call to getRate because
    // new velocity is computed from previous and current acceleration.
    // Previous acceleration is saved in step method of Verlet.
    if(odeSolver.getRateCounter()==1) {
      computeAcceleration();
    }
    for(int i = 0; i<N; i++) {
      rate[6*i] = state[6*i+1];   // rates for positions are velocities
      rate[6*i+2] = state[6*i+3]; // vy
      rate[6*i+4] = state[6*i+5]; // vy
      rate[6*i+1] = ax[i];        // rate for velocity is acceleration
      rate[6*i+3] = ay[i];
      rate[6*i+5] = az[i];
         }
    rate[6*N] = 1; // dt/dt = 1
  }

  public double[] getState() {
    return state;
  }

  public void step(boolean equil) {
	    for(int i = 0; i<N; i++) zSave[i] = state[6*i+4];
	    odeSolver.step();
	    double totalKineticEnergy = 0;
	    for(int i = 0; i<N; i++) {
	      totalKineticEnergy += (state[6*i+1]*state[6*i+1]+state[6*i+3]*state[6*i+3]+state[6*i+5]*state[6*i+5]);
	      state[6*i] = pbcPosition(state[6*i], Lx);
	      state[6*i+2] = pbcPosition(state[6*i+2], Ly);
	      state[6*i+4] = pbcPosition(state[6*i+4], Lz);
	      checkSlab(i);
	    }
	    totalKineticEnergy *= 0.5;
	    steps++;
	    totalKineticEnergyAccumulator += totalKineticEnergy;
	    totalKineticEnergySquaredAccumulator += totalKineticEnergy*totalKineticEnergy;
	    t += dt;
	    if(steps % stepAdd == 0 && !equil) swapMomenta();
	    if(steps % 12 == 0) setNNList();
	    if((steps % 10 == 0) && equil)scaleVelocities();
  }
  
  public void swapMomenta() {
	  double phigh = -1E10;
	  double plow = 1E10;
	  int ihigh = 0;
	  int ilow = 0;
	  for(int j = 0; j < nSlab[0]; j++){
		  int i = slab[0][j];
		  double vx = state[6*i+1];
		  if(vx > phigh){
			  phigh = vx;
			  ihigh = i;
		  }
	  }
	  for(int j = 0; j < nSlab[3*n/2]; j++){
		  int i = slab[3*n/2][j];
		  double vx = state[6*i+1];
		  if(vx < plow){
				  plow = vx;
				  ilow = i;
			  }	  
	  }
//	  if(hot < cold) System.out.println(hot + " " + cold + " " + state[6*icold+1] + " " + state[6*icold+3] + " " + state[6*icold+5]);
	  double vx = state[6*ihigh+1];
	  state[6*ihigh + 1] = state[6*ilow +1];
	  state[6*ilow +1] = vx;  
	  transfer += 0.1*(phigh -plow);
  }
  public void checkSlab(int i) {
	  int slabold = (int)(zSave[i]/(a));
	  int slabnew = (int)(state[6*i+4]/(a));
	  if(slabnew != slabold){
		  slab[slabnew][nSlab[slabnew]] = i;
		  nSlab[slabnew]++;
		  int ns = 0;
		  boolean notfound = true;
		  while (notfound && ns < nSlab[slabold]){
			 if(slab[slabold][ns] == i ) {
				 nSlab[slabold]--;
				 slab[slabold][ns]  = slab[slabold][nSlab[slabold]];
				 notfound = false;
			 }
			 ns++;
		  }
	  }
	  
  }
 
  // end break

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
