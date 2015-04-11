/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.harddisk;
import java.awt.*;
import java.util.Random;
import org.opensourcephysics.display.*;

/**
 * Simulates hard disks.
 *
 * @author Jan Tobochnik
 * @author Joshua Gould
 * @author Peter Sibley
 *
 */
public class HD implements Drawable {
  double[] velocityX;
  double[] velocityY;
  double[] positionX;
  double[] positionY;
  double[] collisionTime;
  double[] timeOfLastCollision;
  double mfpAccumulator;
  int[] partner;
  int N;
  double Lx;
  double Ly;
  public String initialConfiguration;
  static double TIME_BIG = 1000000;
  int minimumCollisionI;
  int minimumCollisionJ;
  double virial;
  double virialAccumulator;
  double time, tenCollisionTime, tenCollisionVirial;
  double velocityMax;
  double area;
  double totalKineticEnergyAccumulator;
  double totalKineticEnergySquaredAccumulator;
  int collisions;
  double rho;
  double radius = .5; // radius of disks used for drawing;
  Random random;
  int steps;
  Histogram velocityHistogram = new Histogram();

  /**
   * Constructor HD
   */
  public HD() {
    random = new Random(System.currentTimeMillis());
    area = 1;
    setLx(10);
    setLy(10);
    setNumberOfDisks(10);
    setVelocityMax(2);
  }

  public double getTime() {
    return time;
  }

  public int getSteps() {
    return steps;
  }

  public double getCollisions() {
    return collisions;
  }

  public double getMeanPressure() {
    double meanVirial;
    meanVirial = virialAccumulator/time;
    return 1.0+0.5*meanVirial/(N*getMeanTemperature()); // quantity
    // PV/NkT
  }

  public double getInstantanousPressure() {
    return 1.0+0.5*tenCollisionVirial/(tenCollisionTime*getInstaneousKineticEnergy()); // quantity
    // PV/NkT
  }

  public double getHeatCapacity() {
    double meanTemperature = getMeanTemperature();
    double meanTemperatureSquared = totalKineticEnergySquaredAccumulator/steps;
    double sigma2 = meanTemperatureSquared-meanTemperature*meanTemperature;
    // heat capacity related to fluctuations of temperature
    double denom = sigma2/(N*meanTemperature*meanTemperature)-1.0;
    return N/denom;
  }

  public void initialize() {
    if(initialConfiguration.equals("crystal")) {
      setCrystalPositions();
    } else {
      setRandomPositions();
    }
    adjustMomentum();
    initializeAuxiliaryArrays();
    clearData();
    velocityHistogram.setBinWidth(2*velocityMax/N); // assuming
    velocityHistogram.setBinOffset(velocityMax/N);
    for(int k = 0; k<N; k++) {
      positionX[k] = pbc(positionX[k], Lx);
      positionY[k] = pbc(positionY[k], Ly);
    }
  }

  public void setRandomPositions() {
    // particles placed at random, but not closer than rMinimumSquared
    boolean overlap;
    for(int i = 0; i<N; ++i) {
      do {
        overlap = false;
        positionX[i] = Lx*Math.random();
        positionY[i] = Ly*Math.random();
        int j = 0;
        while((j<i)&&!overlap) {
          double dx = seperation(positionX[i]-positionX[j], Lx);
          double dy = seperation(positionY[i]-positionY[j], Ly);
          if(dx*dx+dy*dy<1) {
            overlap = true;
          }
          j++;
        }
      } while(overlap);
    }
    for(int i = 0; i<N; i++) {
      double th = 2*Math.PI*random.nextDouble();
      velocityX[i] = velocityMax*Math.cos(th);
      velocityY[i] = velocityMax*Math.sin(th);
      timeOfLastCollision[i] = 0;
    }
  }

  public void setCrystalPositions() {
    // place particles on triangular lattice
    double dnx = Math.sqrt(N);
    int ns = (int) dnx;
    if(dnx-ns>0.001) {
      ns++;
    }
    double ax = Lx/ns;
    double ay = Ly/ns;
    int i = 0;
    int iy = 0;
    while(i<N) {
      for(int ix = 0; ix<ns; ++ix) {
        if(i<N) {
          positionY[i] = ay*(iy+0.5);
          if(iy%2==0) {
            positionX[i] = ax*(ix+0.25);
          } else {
            positionX[i] = ax*(ix+0.75);
          }
          i++;
        }
      }
      iy++;
    }
    for(int i1 = 0; i1<N; i1++) {
      double th = 2*Math.PI*random.nextDouble();
      velocityX[i1] = velocityMax*Math.cos(th);
      velocityY[i1] = velocityMax*Math.sin(th);
      timeOfLastCollision[i1] = 0;
    }
  }

  public void step() {
    tenCollisionTime = 0;
    tenCollisionVirial = 0;
    for(int j = 0; j<10; j++) { // do 10 collisions
      double dt = getMinimumCollisionTime();
      move(dt);
      time += dt;
      tenCollisionTime += dt;
      collisions++;
      updateMFP(minimumCollisionI, minimumCollisionJ);
      contact(minimumCollisionI, minimumCollisionJ);
      // System.out.println(minimumCollisionI + " " + minimumCollisionJ + " "
      // + collisionTime[minimumCollisionI]); //DEBUG
      virialAccumulator += virial;
      tenCollisionVirial += virial;
      totalKineticEnergyAccumulator += getInstaneousKineticEnergy();
      totalKineticEnergySquaredAccumulator += getInstaneousKineticEnergy()*getInstaneousKineticEnergy();
      resetList(minimumCollisionI, minimumCollisionJ);
      checkOverlap();
      for(int i = 0; i<N; i++) {
        appendVelocityPoint(i);
      }
      steps++;
    }
    // System.out.println(toString() ); //DEBUG
  }

  public void updateMFP(int i, int j) {
    double ti = time-timeOfLastCollision[i];
    double tj = time-timeOfLastCollision[j];
    double dx, dy;
    dx = velocityX[i]*ti;
    dy = velocityY[i]*ti;
    mfpAccumulator += Math.sqrt(dx*dx+dy*dy);
    dx = velocityX[j]*tj;
    dy = velocityY[j]*tj;
    mfpAccumulator += Math.sqrt(dx*dx+dy*dy);
    timeOfLastCollision[i] = time;
    timeOfLastCollision[j] = time;
  }

  public double getMFP() {
    return mfpAccumulator/2.0/collisions;
  }

  public double getMFT() {
    return N*time/collisions;
  }

  public double getMeanTemperature() {
    return totalKineticEnergyAccumulator/(N*steps);
  }

  public double getInstantaneousTemperature() {
    return getInstaneousKineticEnergy()/N;
  }

  public void clearData() {
    time = 0;
    virialAccumulator = 0;
    collisions = 0;
    totalKineticEnergyAccumulator = 0;
    totalKineticEnergySquaredAccumulator = 0;
    steps = 0;
    mfpAccumulator = 0;
    velocityHistogram.clear();
  }

  public void zeroAverages() {
    this.velocityHistogram.clear();
    this.mfpAccumulator = 0;
    this.collisions = 0;
    this.totalKineticEnergyAccumulator = 0;
    this.totalKineticEnergySquaredAccumulator = 0;
    this.virialAccumulator = 0;
  }

  public void appendVelocityPoint(int i) {
    velocityHistogram.append(velocityX[i]);
    // velocityHistogram.append(vy[i]);
  }

  public double getMeanKineticEnergy() {
    return totalKineticEnergyAccumulator/steps;
  }

  public double getTemperature() {
    return getInstaneousKineticEnergy()/N;
  }

  public void initializeAuxiliaryArrays() {
    for(int i = 0; i<N; i++) {
      partner[i] = N-1;
    }
    collisionTime[N-1] = TIME_BIG;
    for(int i = 0; i<N; i++) {
      uplist(i);
    }
  }

  public void setLx(double _Lx) {
    Lx = _Lx;
    area = Lx*Ly;
    rho = N/area;
  }

  public void setLy(double _Ly) {
    Ly = _Ly;
    area = Lx*Ly;
    rho = N/area;
  }

  public void setNumberOfDisks(int N) {
    this.N = N;
    velocityX = new double[N];
    velocityY = new double[N];
    positionX = new double[N];
    positionY = new double[N];
    collisionTime = new double[N];
    timeOfLastCollision = new double[N];
    partner = new int[N];
    rho = N/area;
  }

  //input is the temperature, and 1/2v*v = T
  public void setVelocityMax(double T) {
    velocityMax = Math.sqrt(2.0*T);
  }

  public void setLatticeSpacing() {
    double nx = Math.floor(Math.sqrt(N));
    double ny = nx;
    double ax = Lx/nx;
    double ay = Ly/ny;
    int i = 0;
    double pi = Math.PI;
    for(int x = 0; x<nx; x++) {
      for(int y = 0; y<ny; y++) {
        positionX[i] = (x-.5)*ax;
        positionY[i] = (y-.5)*ay;
        double th = 2*pi*random.nextDouble();
        velocityX[i] = velocityMax*Math.cos(th);
        velocityY[i] = velocityMax*Math.sin(th);
        timeOfLastCollision[i] = 0;
        i++;
      }
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i<N; i++) {
      buffer.append(i+" x "+positionX[i]+" y "+positionY[i]+" vx "+velocityX[i]+" vy "+velocityY[i]+"\n");
    }
    return buffer.toString();
  }

  /**
   * Computes and returns the total Kinetic Energy of the system
   */
  public double getInstaneousKineticEnergy() {
    double kineticEnergy = 0;
    for(int i = 0; i<N; i++) {
      kineticEnergy += velocityX[i]*velocityX[i]+velocityY[i]*velocityY[i];
    }
    kineticEnergy = kineticEnergy/2.0;
    return kineticEnergy;
  }

  public void adjustMomentum() {
    double velocityXsum = 0;
    double velocityYsum = 0;
    double T0 = getTemperature();
    for(int i = 0; i<N; i++) {
      velocityXsum += velocityX[i];
      velocityYsum += velocityY[i];
    }
    double velocityXmean = velocityXsum/N;
    double velocityYmean = velocityYsum/N;
    for(int i = 0; i<N; i++) {
      velocityX[i] = velocityX[i]-velocityXmean;
      velocityY[i] = velocityY[i]-velocityYmean;
    }
    //restore the temperature
    double a = T0/getTemperature();
    a = Math.sqrt(a);
    for(int i = 0; i<N; i++) {
      velocityX[i] *= a;
      velocityY[i] *= a;
    }
  }

  public Histogram getVelocityHistogram() {
    return velocityHistogram;
  }

  public void checkCollision(int i, int j) {
    for(int xCell = -1; xCell<=1; xCell++) {
      for(int yCell = -1; yCell<=1; yCell++) {
        double dx = positionX[i]-positionX[j]+xCell*Lx;
        double dy = positionY[i]-positionY[j]+yCell*Ly;
        double dvx = velocityX[i]-velocityX[j];
        double dvy = velocityY[i]-velocityY[j];
        double bij = dx*dvx+dy*dvy;
        if(bij<0) {
          double r2 = dx*dx+dy*dy;
          double v2 = dvx*dvx+dvy*dvy;
          double discr = bij*bij-v2*(r2-1);
          if(discr>0) {
            double tij = (-bij-Math.sqrt(discr))/v2;
            if(tij<collisionTime[i]) {
              collisionTime[i] = tij;
              partner[i] = j;
            }
          }
        }
      }
    }
  }

  public double getMinimumCollisionTime() {
    double tij = TIME_BIG;
    for(int k = 0; k<N; k++) {
      if(collisionTime[k]<tij) {
        tij = collisionTime[k];
        minimumCollisionI = k;
      }
    }
    minimumCollisionJ = partner[minimumCollisionI];
    return tij;
  }

  public void move(double tij) {
    for(int k = 0; k<N; k++) {
      collisionTime[k] = collisionTime[k]-tij;
      positionX[k] += velocityX[k]*tij;
      positionY[k] += velocityY[k]*tij;
      positionX[k] = pbc(positionX[k], Lx);
      positionY[k] = pbc(positionY[k], Ly);
    }
  }

  public double pbc(double x, double L) {
    if(x>=L) {
      return x-L;
    } else if(x<0) {
      return x+L;
    } else {
      return x;
    }
  }

  public void contact(int i, int j) {
    double dx = seperation(positionX[i]-positionX[j], Lx);
    double dy = seperation(positionY[i]-positionY[j], Ly);
    double dvx = velocityX[i]-velocityX[j];
    double dvy = velocityY[i]-velocityY[j];
    double factor = dx*dvx+dy*dvy;
    double delvx = -factor*dx;
    double delvy = -factor*dy;
    velocityX[i] = velocityX[i]+delvx;
    velocityX[j] = velocityX[j]-delvx;
    velocityY[i] = velocityY[i]+delvy;
    velocityY[j] = velocityY[j]-delvy;
    virial = delvx*dx+delvy*dy;
  }

  public double seperation(double ds, double L) {
    if(ds>.5*L) {
      return ds-L;
    } else if(ds<-.5*L) {
      return ds+L;
    } else {
      return ds;
    }
  }

  public void resetList(int i, int j) {
    for(int k = 0; k<N; k++) {
      int test = partner[k];
      if((k==i)||(test==i)||(k==j)||(test==j)) {
        uplist(k);
      }
    }
    downlist(i);
    downlist(j);
  }

  public void downlist(int j) {
    if(j==0) {
      return;
    }
    for(int i = 0; i<j; i++) {
      checkCollision(i, j);
    }
  }

  public void uplist(int i) {
    if(i==(N-1)) {
      return;
    }
    collisionTime[i] = TIME_BIG;
    for(int j = i+1; j<N; j++) {
      checkCollision(i, j);
    }
  }

  public void checkOverlap() {
    double tol = .00001;
    for(int i = 0; i<N-1; i++) {
      for(int j = i+1; j<N; j++) {
        double dx = seperation(positionX[i]-positionX[j], Lx);
        double dy = seperation(positionY[i]-positionY[j], Ly);
        double r2 = dx*dx+dy*dy;
        if(r2<1) {
          double r = Math.sqrt(r2);
          double dist = 1-r;
          if(dist>tol) {
            System.err.println("particles "+i+"  "+j+" overlap");
          }
        }
      }
    }
  }

  public void draw(DrawingPanel myWorld, Graphics g) {
    if(positionX==null) {
      return;
    }
    int pxRadius = Math.abs(myWorld.xToPix(radius)-myWorld.xToPix(0));
    int pyRadius = Math.abs(myWorld.yToPix(radius)-myWorld.yToPix(0));
    g.setColor(Color.blue);
    int xpix = myWorld.xToPix(positionX[0])-pxRadius;
    int ypix = myWorld.yToPix(positionY[0])-pyRadius;
    g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    g.setColor(Color.red);
    for(int i = 1; i<N; i++) {
      xpix = myWorld.xToPix(positionX[i])-pxRadius;
      ypix = myWorld.yToPix(positionY[i])-pyRadius;
      g.fillOval(xpix, ypix, 2*pxRadius, 2*pyRadius);
    }
    g.setColor(Color.black);
    xpix = myWorld.xToPix(0);
    ypix = myWorld.yToPix(Ly); //bug fix by WC
    int lx = myWorld.xToPix(Lx)-myWorld.xToPix(0);
    int ly = myWorld.yToPix(0)-myWorld.yToPix(Ly);//bug fix by WC
    g.drawRect(xpix, ypix, lx, ly); 
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
