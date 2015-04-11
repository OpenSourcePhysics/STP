/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.qmc;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.display3d.simple3d.*;

/**
 * QMCApp
 *
 * @author Wolfgang Christian, Jan Tobochnik, Harvey Gould
 * @version 1.0  05/16/05
 */
public class QMCApp extends AbstractSimulation {
  Display3DFrame frame = new Display3DFrame("Phase Space");
  QMC qmc = new QMC();
  PlotFrame distribution = new PlotFrame("energy", "n(E)", "distribution");
  PlotFrame energyDistribution = new PlotFrame("energy", "E n(E) D(E)", "energy distribution");
  PlotFrame dos = new PlotFrame("energy", "D(E)", "Density of States (number of states per unit energy)");
  PlotFrame particleState = new PlotFrame("particle label", "energy", "energy level diagram");
  int statistics;
  ElementEllipsoid[] sites;
  double dE;

  /**
   * Initializes the simulation.
   */
  public void initialize() {
    distribution.setPreferredMinMax(0, 1, 0, 1);
    distribution.setAutoscaleX(true);
    distribution.setAutoscaleY(true);
    distribution.setConnected(1, true);
    energyDistribution.setPreferredMinMax(0, 1, 0, 1);
    energyDistribution.setAutoscaleX(true);
    energyDistribution.setAutoscaleY(true);
    double p = control.getDouble("momentum exponent p"); // 1 or 2 are physical
    int kmax = control.getInt("maximum k");
    int dimension = control.getInt("dimension");         // 1 2 or 3
    //statistics = control.getInt("statistics:FD=1,BE=2,MB=3");
    statistics = 1;
    String st = control.getString("statistics:FD, BE, MB");
    if(st.equalsIgnoreCase("FD")) {
      statistics = 1;
    } else if(st.equalsIgnoreCase("BE")) {
      statistics = 2;
    } else if(st.equalsIgnoreCase("MB")) {
      statistics = 3;
    }
    int np = control.getInt("number of particles");
    dE = control.getDouble("dE");
    qmc.beta = 1.0/control.getDouble("temperature");
    particleState.setPreferredMinMax(0, np, 0, 1);
    particleState.setAutoscaleX(true);
    particleState.setAutoscaleY(true);
    qmc.initial(dimension, kmax, p, np, statistics, dE);
    sites = new ElementEllipsoid[np];
    frame.getDrawingPanel3D().removeAllElements();
    for(int i = 0; i<np; i++) {
      sites[i] = new ElementEllipsoid();
      sites[i].setXYZ(0, 0, 0);
      sites[i].setSizeXYZ(.2, .2, .2);
      frame.addElement(sites[i]);
    }
    frame.setPreferredMinMax(-kmax, kmax, -kmax, kmax, -kmax, kmax);
    dos.clearData();
    for(int e = 0; e<(int) (qmc.maxEnergy/dE); e++) {
      dos.append(0, dE*e, qmc.densityOfStates[e]/dE);
    }
    dos.render();
  }

  /**
   * Resets the simulation.
   */
  public void reset() {
    control.setValue("momentum exponent p", 2);
    control.setValue("maximum k", 10);
    control.setValue("dimension", 3);
    //control.setValue("statistics:FD=1,BE=2,MB=3", 1);
    control.setValue("statistics:FD, BE, MB", "FD");
    control.setValue("number of particles", 100);
    control.setValue("temperature", 10);
    control.setAdjustableValue("dE", 4);
    control.setAdjustableValue("steps per display", 10);
    //enableStepsPerDisplay(true);
  }

  /**
   *
   * Does an animation step by moving the ball.
   *
   */
  protected void doStep() {
    int stepsPerDisplay = control.getInt("steps per display");
    for(int steps = 0; steps<stepsPerDisplay; steps++) {
      switch(statistics) {
         case 1 :
           qmc.stepFD();
           break;
         case 2 :
           qmc.stepBE();
           break;
         default :
           qmc.stepMB();
      }
    }
    displayData();
  }

  protected void displayData() {
    dE = control.getDouble("dE");
    frame.setMessage("mcs = "+qmc.mcs);
    distribution.clearData();
    energyDistribution.clearData();
    int degeneracy = 0;
    double occupancy = 0;
    double energy = 0;
    double energyTop = dE;
    double energyAccum = 0;
    for(int i = 0; i<qmc.occupiedIndexMax; i++) {
      if(qmc.occupationAccum[i]>0) {
        energy = qmc.energy[i];
        if(energy>energyTop) {
          energyDistribution.append(0, energyTop-.5*dE, energyAccum/dE);
          energyTop += dE;
          energyAccum = 0;
        }
        degeneracy++;
        double occ = (1.0*qmc.occupationAccum[i])/qmc.mcs;
        occupancy += occ;
        distribution.append(0, energy, occ);
        energyAccum += energy*occ;
        if(Math.abs(energy-qmc.energy[i+1])>0.001) {
          distribution.append(1, energy, occupancy/degeneracy);
          degeneracy = 0;
          occupancy = 0;
        }
      }
    }
    distribution.append(1, energy, occupancy/degeneracy);
    //distribution.append(2,energy,occupancy);
    double eAve = qmc.energyAccum/(qmc.mcs);
    double e2Ave = qmc.energySqAccum/(qmc.mcs);
    double specificHeat = qmc.beta*qmc.beta*(e2Ave-eAve*eAve)/qmc.np;
    distribution.setMessage("E/N = "+ControlUtils.f2(eAve/qmc.np)+" c="+ControlUtils.f2(specificHeat));
    particleState.clearData();
    for(int i = 0; i<qmc.np; i++) {
      particleState.append(0, i, qmc.energy[qmc.state[i]]);
    }
  }

  public void stop() {
    for(int i = 0; i<qmc.np; i++) {
      sites[i].setXYZ(qmc.x[qmc.state[i]], qmc.y[qmc.state[i]], qmc.z[qmc.state[i]]);
    }
    frame.render();
  }

  public void resetData() {
    qmc.resetData();
    distribution.clearData();
    particleState.clearData();
    distribution.render();
    particleState.render();
    control.clearMessages();
  }

  /**
   *
   * Starts the Java application.
   *
   * @param args  command line parameters
   *
   */
  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new QMCApp());
    control.addButton("resetData", "Reset Data");
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
