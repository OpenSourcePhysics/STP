/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

<<<<<<< HEAD
package org.opensourcephysics.stp.Chapter09;
=======
package org.opensourcephysics.stp.Ch09;
>>>>>>> 21587f732c3b26918396413e3ec3807a4442aef4
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;
import org.opensourcephysics.display.*;
import org.opensourcephysics.stp.util.Util;

/**
 * This class simulates the XY model on a square lattice with periodic boundary
 * conditions.
 *
 * @author Joshua Gould
 * @author Peter Sibley
 * @author Rongfeng Sun
 * @created December 26, 2002
 */
public class XYModel implements Measurable {
  public final static double J = 1;
  public final static double TWOPI = Math.PI*2;
  double beta;
  double temperature;
  double deltatemperature;
  int time;
  double energyAccumulator;
  double energySquaredAccumulator;
  int L;                    // linear dimension
  int numberOfSpins;
  int[] nnOffSetX;          // x offset
  int[] nnOffSetY;          // y offset
  double[] correlation, norm;
  // offsets and pbc() determine nearest neighbors
  double energy;            // current total energy of system
  double magnetizationX;    // current magnetization of system in x direction
  double magnetizationY;    // current magnetization of system in y direction
  double magnetizationXAccumulator;
  double magnetizationYAccumulator;
  double magnetizationSquaredAccumulator;
  double magnetizationAccumulator;
  double spin[][];          // angle of spins in radians
  int mcs;                  // MC steps per spin
  int nequil = 0;
  int acceptAccumulator;
  double dThetaMax;
  int cellSizeX, cellSizeY; // number of pixels for each arrow
  GeneralPath vectorpath;
  Random random;
  boolean vorticityDisplay = false;
  int vortexAccumulator;
  boolean keepVorticity = true;
  long seed;                // seed for random number generator
  String initialConfiguration;

  /**
   * Constructor XYModel
   */
  public XYModel() {
    random = new Random();
    setSeed(System.currentTimeMillis());
    setLinearDimension(10);
    setTemperature(2.0);
    setDThetaMax(TWOPI);
    nnOffSetX = new int[] {1, 0, -1, 0}; // define nearest neighbors
    nnOffSetY = new int[] {0, 1, 0, -1};
  }

  public void fillSpinsUp() {
    for(int i = 0; i<spin.length; i++) {
      for(int j = 0; j<spin[0].length; j++) {
        spin[i][j] = TWOPI; // FIX_ME
      }
    }
  }

  /**
   * set MC steps needed to reach equilibrium
   *
   * @param _nequil
   *            The new nequil value
   */
  public void setNequil(int _nequil) {
    nequil = _nequil;
  }

  /**
   * Switch on or off keeping track of vorticity and mean number of
   * vorticities. requires an additional pass over the lattice after each
   * metropolis step. default is off.
   *
   * @param _kv
   *            The new keepVorticity value
   * @see getMeanNumberOfVortices
   * @see getVoricity
   */
  public void setKeepVorticity(boolean _kv) {
    keepVorticity = _kv;
  }

  /**
   * set temperature of system and adjusts Boltzmann probabilities accordingly
   *
   * @param _T
   */
  public void setTemperature(double _T) {
    if((_T>=0)&&(_T<=0.000001)) {
      temperature = 0.0000001;
    } // something small != 0
      else {
      temperature = Math.max(0, _T);
    }
    beta = 1.0/temperature;
  }

  /**
   * Set maximum change in theta (in radians) in the Metropolis algorithm
   *
   * @param _dThetaMax
   */
  public void setDThetaMax(double _dThetaMax) {
    dThetaMax = _dThetaMax;
  }

  /**
   * Turn on and off the display of circles around vorticies.
   *
   * @param _v
   *            Description of the Parameter
   */
  public void showVortex(boolean _v) {
    vorticityDisplay = _v;
  }

  /**
   * Set MC steps per spin
   *
   * @param _mcs
   */
  public void setMcs(int _mcs) {
    mcs = _mcs;
  }

  /**
   * set seed used for random number generator
   *
   * @param _seed
   *            The new seed value
   */
  public void setSeed(long _seed) {
    seed = _seed;
    random.setSeed(seed);
  }

  /**
   * get MC steps per spin
   *
   * @return mcs
   */
  public int getMcs() {
    return mcs;
  }

  /**
   * Return true if the system time > MCS otherwise it returns false
   *
   * @return The completed value
   */
  public boolean isCompleted() {
    return(time>mcs);
  }

  /**
   * Set linear dimension of lattice, It also has the effect of clearing the
   * exisiting lattice.
   *
   * @param _L
   *            The new linearDimension value
   */
  public void setLinearDimension(int _L) {
    L = _L;
    spin = new double[L][L];
    numberOfSpins = L*L;
    correlation = new double[L];
    norm = new double[L];
  }

  /**
   * compute total energy of system
   *
   * @return The totalEnergy value
   */
  public double getTotalEnergy() {
    double sum = 0;
    for(int x = 0; x<L; x++) {
      for(int y = 0; y<L; y++) {
        sum += Math.cos(spin[x][y]-spin[Util.pbc(x+1, L)][y]);
        sum += Math.cos(spin[x][y]-spin[x][Util.pbc(y+1, L)]);
      }
    }
    return -J*sum;
  }
  
  /**
   * compute correlation function
   *
   * 
   */
  
  public void correlationFunction() {
	  int half = L/2;

	   for(int x1 = 0; x1<L; x1++) {
		      for(int y1 = 0; y1<L; y1++) {
		   	   for(int x2 = 0; x2<L; x2++) {
				      for(int y2 = 0; y2<L; y2++) {
				    	    int dx = Math.abs(x2-x1);
				    	    if(dx > half) dx -= L;
				    	    int dy = Math.abs(y2-y1);
				    	    if(dy > half) dy -= L;
				    	    int r = (int)Math.sqrt(0.2 + dx*dx+dy*dy);
		        correlation[r] += Math.cos(spin[x1][y1]-spin[x2][y2]);
		        norm[r]++;
		      }
		    }
		      }
	   }
 }

  /**
   * Set spin at x,y to theta making sure that theta is between 0 and two pi.
   * Used for trial rotations
   *
   * @param x
   *            The new spin value
   * @param y
   *            The new spin value
   * @param theta
   *            The new spin value
   */
  public void setSpin(int x, int y, double theta) {
    while(theta<0) {
      theta += TWOPI;
    }
    spin[x][y] = theta%TWOPI; // we want theta in [0,2PI)
  }

  /**
   * increment counters
   *
   * @param E
   *            Description of the Parameter
   * @param Mx
   *            Description of the Parameter
   * @param My
   *            Description of the Parameter
   */
  public void updateStatistics(double E, double Mx, double My) {
    energyAccumulator += E;
    energySquaredAccumulator += E*E;
    magnetizationSquaredAccumulator += Mx*Mx+My*My;
    magnetizationAccumulator += Math.sqrt(Mx*Mx+My*My);
    magnetizationXAccumulator += Mx;
    magnetizationYAccumulator += My;
  }

  /**
   * compute Boltzmann probability for given deltaE
   *
   * @param deltaEnergy
   *            Description of the Parameter
   * @return boltzmann probabaility
   */
  public double boltzmannProbability(double deltaEnergy) {
    return Math.exp(-beta*deltaEnergy);
  }

  /**
   * Compute change in energy due to changing spin[x][y] = newTheta
   *
   * @param newTheta
   * @param x
   *            Description of the Parameter
   * @param y
   *            Description of the Parameter
   * @return change in energy
   */
  public double deltaEnergy(int x, int y, double newTheta) {
    double sum = 0;
    double sumNew = 0;
    for(int i = 0; i<nnOffSetY.length; i++) {
      sum += Math.cos(spin[x][y]-spin[Util.pbc(x+nnOffSetX[i], L)][Util.pbc(y+nnOffSetY[i], L)]);
      sumNew += Math.cos(newTheta-spin[Util.pbc(x+nnOffSetX[i], L)][Util.pbc(y+nnOffSetY[i], L)]);
    }
    return -J*(sumNew-sum);
  }

  /**
   * Reset counters, set time to zero, start in a random spin configuration.
   */
  public void initialize() {
    if(initialConfiguration.equals("random")) {
      for(int i = 0; i<spin.length; i++) {
        for(int j = 0; j<spin[0].length; j++) {
          spin[i][j] = random.nextDouble()*TWOPI;
        }
      }
    } else {
      for(int i = 0; i<spin.length; i++) {
        for(int j = 0; j<spin[0].length; j++) {
          spin[i][j] = 0;
        }
      }
    }
    energy = getTotalEnergy();
    calculateMagnetization();
    for(int i = 0; i<nequil; i++) {
      metropolis();
    }
    clearData();
  }

  /** do one Metropolis step and update statistics */
  public void step() {
    metropolis();
    updateStatistics(energy, magnetizationX, magnetizationY);
    time++;
    if(keepVorticity) {
      vortexAccumulator += getTotalNumberOfVortices();
    }
  }

  /** do mcs steps */
  public void compute() {
    initialize();
    while(!isCompleted()) {
      step();
    }
  }

  /**
   * so one MC step per spin according to the Boltzmann Probability
   * e^(-beta*DeltaEnergy)
   */
  public void metropolis() {
    int x;
    int y;
    double dE;
    double dTheta;
    double theta;
    double thetaTrial;
    double dMx;
    double dMy;
    for(int i = 1; i<=numberOfSpins; i++) {
      x = random.nextInt(L); // pick a random location
      y = random.nextInt(L);
      theta = spin[x][y];
      dTheta = (2*random.nextDouble()-1.0)*dThetaMax;
      thetaTrial = theta+dTheta;
      dE = deltaEnergy(x, y, thetaTrial);
      if(random.nextDouble()<boltzmannProbability(dE)) {
        dMx = Math.cos(thetaTrial)-Math.cos(spin[x][y]);
        dMy = Math.sin(thetaTrial)-Math.sin(spin[x][y]);
        setSpin(x, y, thetaTrial);
        acceptAccumulator++;
        energy += dE;
        magnetizationX += dMx;
        magnetizationY += dMy;
      }
    }
  }

  /** reset counters and set time to zero. */
  public void clearData() {
    energyAccumulator = 0;
    energySquaredAccumulator = 0;
    acceptAccumulator = 0;
    if(keepVorticity) {
      vortexAccumulator = 0;
    }
    magnetizationXAccumulator = 0;
    magnetizationYAccumulator = 0;
    magnetizationSquaredAccumulator = 0;
    magnetizationAccumulator = 0;
    time = 0; // since resetting accumulators should reset time as well
    for(int i = 0; i < L; i++) correlation[i] = 0;
  }

  /**
   * @return temperature
   */
  public double getTemperature() {
    return temperature;
  }

  /**
   * @return seed used for random number generator
   */
  public double getSeed() {
    return seed;
  }

  /**
   * @return time counter of the system
   */
  public int getTime() {
    return time;
  }

  /**
   * @return acceptance probability
   */
  public double getAcceptanceProbability() {
    return acceptAccumulator/((double) time*numberOfSpins);
  }

  /**
   * @return mean energy
   */
  public double getMeanEnergy() {
    return energyAccumulator*1.0/((double) time);
  }

  /**
   * @return mean of square of total energy
   */
  public double getMeanEnergySquared() {
    return energySquaredAccumulator/(double) time;
  }

  public double getMeanMagnetizationX() {
    return magnetizationXAccumulator/(double)time;
  }

  public double getMeanMagnetizationY() {
    return magnetizationYAccumulator/(double)time;
  }

  /**
   * @return mean of square of magnetization
   */
  public double getMeanMagnetizationSquared() {
    return magnetizationSquaredAccumulator/(double) time;
  }
  
  /**
   * @return mean of magnetization
   */
  public double getMeanMagnetization() {
    return magnetizationAccumulator/(double) time;
  }

  /**
   * @return heat capacity of system
   */
  public double getHeatCapacity() {
    return(1.0/(getTemperature()*getTemperature()))*(getMeanEnergySquared()-getMeanEnergy()*getMeanEnergy());
  }

  /**
   * @return susceptibaility of system
   */
  public double getSusceptibility() {
	//return(1.0/(getTemperature()*getTemperature()))*(getMeanMagnetizationSquared()-0); // <M> = 0 for T > T_K
    return(getMeanMagnetizationSquared()-getMeanMagnetization()*getMeanMagnetization())/getTemperature(); // <M> = 0 for T > T_K
  }

  /**
   * get total number of vortices (both negative and positive)
   *
   * @return total number of vortices
   */
  public int getTotalNumberOfVortices() {
    int numberOfVortices = 0;
    for(int x = 0; x<L; x++) {
      for(int y = 0; y<L; y++) {
        numberOfVortices += Math.abs(identifyVortex(x, y));
      }
    }
    return numberOfVortices;
  }

  /**
   * @return mean number of vortices
   * @see setKeepVorticity
   */
  public double getMeanNumberOfVortices() {
    if(!keepVorticity) {
      return -1;
    }
    return(double) vortexAccumulator/(double) time;
  }

  /**
   * @return returns the vorticity
   * @see setKeepVorticity
   */
  public double getVorticity() {
    if(!keepVorticity) {
      return -1;
    }
    return getMeanNumberOfVortices()/(L*L);
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(spin==null) {
      return;
    }
    float arrowLength = Math.max(1, panel.getSize().width/(float) spin.length/2);
    arrowLength = Math.min(20, arrowLength);
    vectorpath = newFilledVectorPath(arrowLength);
    cellSizeX = 1+(panel.xToPix(L)-panel.xToPix(0))/L; // need to
    // center the
    // spin
    cellSizeY = 1+(panel.yToPix(L)-panel.yToPix(0))/L;
    int vorticity = 0;
    for(int i = 0; i<L; i++) {
      for(int j = 0; j<L; j++) {
        drawSpin(panel, g, i, j);
        if(vorticityDisplay) {
          vorticity = identifyVortex(i, j);
          if(vorticity==1) {
            drawBox(g, panel, Color.red, i, j);
          } else if(vorticity==-1) {
            drawBox(g, panel, Color.blue, i, j);
          }
        }
      }
    }
  }

  /**
   * stuff for measurable interface
   *
   * @return The xMin value
   */
  public double getXMin() {
    return 0;
  }

  public double getYMin() {
    return 0;
  }

  public double getXMax() {
    return L;
  }

  public double getYMax() {
    return L;
  }

  public boolean isMeasured() {
    return spin!=null;
  }

  public void setdeltatemperature(double deltatemperature1) {
    deltatemperature = deltatemperature1;
  }

  public double getdeltatemperature() {
    return deltatemperature;
  }

  /** compute system magnetzation and update the state of magnetization */
  protected void calculateMagnetization() {
    magnetizationX = 0;
    magnetizationY = 0;
    for(int x = 0; x<L; x++) {
      for(int y = 0; y<L; y++) {
        magnetizationX += Math.cos(spin[x][y]);
        magnetizationY += Math.sin(spin[x][y]);
      }
    }
  }

  /**
   * This method detects a vortex. If there is one with a counter clock wise
   * spin it returns +1 If it has a clock wise spin it retuns -1 If there is
   * no vortex it returns 0
   *
   * @param x
   *            Description of the Parameter
   * @param y
   *            Description of the Parameter
   * @return -1,0,1
   */
  protected int identifyVortex(int x, int y) { // all angles between 0 and 2*pi
    double delta = 0;
    double angles[] = new double[] { // these thetas form the angles around a
      // box
      spin[x][y], spin[x][Util.pbc(y+1, L)], spin[Util.pbc(x+1, L)][Util.pbc(y+1, L)], spin[Util.pbc(x+1, L)][y]
    };
    for(int i = 0; i<3; i++) {
      double temp = angles[i+1]-angles[i];
      if((temp<=Math.PI)&&(temp>-Math.PI)) {
        delta += temp;
      } else if(temp>Math.PI) {
        delta += (temp-2*Math.PI);
      } else {
        delta += (temp+2*Math.PI);
      }
    }
    if(delta>Math.PI) {
      return 1;
    } else if(delta<-Math.PI) {
      return -1;
    } else {
      return 0;
    }
  }

  /**
   * draws a single spin spin[x][y]
   *
   * @param panel
   *            Description of the Parameter
   * @param g
   *            Description of the Parameter
   * @param x
   *            Description of the Parameter
   * @param y
   *            Description of the Parameter
   */
  protected void drawSpin(DrawingPanel panel, Graphics g, int x, int y) {
    double theta = spin[x][y];
    double cosTheta = Math.cos(theta);
    double sinTheta = Math.sin(theta);
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(Color.black);
    // Rotation by Theta + Translation
    AffineTransform at = new AffineTransform(cosTheta, -sinTheta, sinTheta, cosTheta, panel.xToPix(x)+cellSizeX/2, // translation x
    // (but centered)
    panel.yToPix(y)+cellSizeY/2); // translation y
    Shape s = vectorpath.createTransformedShape(at);
    g2.fill(s);
    g2.draw(s);
  }

  /**
   * from vector field class in display2d
   *
   * @param size
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  static GeneralPath newFilledVectorPath(float size) {
    float head = 1+size/5;
    GeneralPath path = new GeneralPath();
    path.moveTo((float) -size/2, (float) 1);             // start drawing at the base
    path.lineTo((float) size/2-head, (float) 1);         // line to base tip of
    // the head
    path.lineTo((float) size/2-head, (float) 2*head/3);  // draw to
    // one side
    path.lineTo((float) size/2, (float) 0);              // up to the tip
    path.lineTo((float) size/2-head, (float) -2*head/3); // the other
    // side
    path.lineTo((float) size/2-head, (float) -1);        // back to base tip
    // of the head
    path.moveTo((float) -size/2, (float) -1);            // back to the base
    return path;
  }

  /**
   * This method draws a box of color c around a cluster of spins with corner
   * at spin[x][y] It requires cellSizeX to be set correctly.
   *
   * @param g
   *            Description of the Parameter
   * @param p
   *            Description of the Parameter
   * @param c
   *            Description of the Parameter
   * @param x
   *            Description of the Parameter
   * @param y
   *            Description of the Parameter
   */
  private void drawBox(Graphics g, DrawingPanel p, Color c, int x, int y) {
    g.setColor(c);
    int xpoints[] = new int[4];
    int ypoints[] = new int[4];
    if((Util.pbc(x+1, L)==x+1)&&(Util.pbc(y+1, L)==y+1)) {        // we want to make sure we are not on the edge
      xpoints[0] = p.xToPix(x)+cellSizeX/2;
      ypoints[0] = p.yToPix(y)+cellSizeY/2;
      xpoints[1] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[1] = p.yToPix(y)+cellSizeY/2;
      xpoints[2] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[2] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      xpoints[3] = p.xToPix(x)+cellSizeX/2;
      ypoints[3] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      g.drawPolygon(xpoints, ypoints, 4);
    } else if((Util.pbc(x+1, L)!=x+1)&&(Util.pbc(y+1, L)==y+1)) { // we are on a edge so we will draw two boxes
      xpoints[0] = p.xToPix(x)+cellSizeX/2;
      ypoints[0] = p.yToPix(y)+cellSizeY/2;
      xpoints[1] = p.xToPix(p.getXMax());
      ypoints[1] = p.yToPix(y)+cellSizeY/2;
      xpoints[2] = p.xToPix(p.getXMax());
      ypoints[2] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      xpoints[3] = p.xToPix(x)+cellSizeX/2;
      ypoints[3] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      g.drawPolygon(xpoints, ypoints, 4);
      xpoints[0] = p.xToPix(p.getXMin());
      ypoints[0] = p.yToPix(y)+cellSizeY/2;
      xpoints[1] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[1] = p.yToPix(y)+cellSizeY/2;
      xpoints[2] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[2] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      xpoints[3] = p.xToPix(p.getXMin());
      ypoints[3] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      g.drawPolygon(xpoints, ypoints, 4);
    } else if((Util.pbc(x+1, L)==x+1)&&(Util.pbc(y+1, L)!=y+1)) {
      xpoints[0] = p.xToPix(x)+cellSizeX/2;
      ypoints[0] = p.yToPix(y)+cellSizeY/2;
      xpoints[1] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[1] = p.yToPix(y)+cellSizeY/2;
      xpoints[2] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[2] = p.yToPix(p.getYMax());
      xpoints[3] = p.xToPix(x)+cellSizeX/2;
      ypoints[3] = p.yToPix(p.getYMax());
      g.drawPolygon(xpoints, ypoints, 4);
      xpoints[0] = p.xToPix(x)+cellSizeX/2;
      ypoints[0] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      xpoints[1] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[1] = p.yToPix(Util.pbc(y+1, L))+cellSizeY/2;
      xpoints[2] = p.xToPix(Util.pbc(x+1, L))+cellSizeX/2;
      ypoints[2] = p.yToPix(p.getYMin());
      xpoints[3] = p.xToPix(x)+cellSizeX/2;
      ypoints[3] = p.yToPix(p.getYMin());
      g.drawPolygon(xpoints, ypoints, 4);
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
