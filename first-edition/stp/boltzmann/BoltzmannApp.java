/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

// Note - the "bin offset" function is no longer available, but it doesn't seem to matter
// as the data are relatively continuous (no spikes).
package org.opensourcephysics.stp.boltzmann;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Random;
import javax.swing.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.HistogramFrame;

/**
 * example of Metropolis algorithm for a particle in one dimension
 *
 * @author Peter Sibley
 * @author Joshua Gould
 * @author Wolfgang Christian
 */
public class BoltzmannApp extends AbstractCalculation {
  Random random;
  HistogramFrame energyFrame = new HistogramFrame("E", "H(E)", "H(E)");
  HistogramFrame velocityFrame = new HistogramFrame("v", "H(v)", "H(v)");
  // kinetic energy, maximum change in velocity
  double energyAccumulator, velocityAccumulator, beta, velocity, temperature, energy, delta;
  // number of MC steps, number of bins
  int mcs, numberOfBins, nequil, accept = 0;
  NumberFormat nf;

  /**
   * Constructor BoltzmannApp
   */
  public BoltzmannApp() {
    velocityFrame.setBinWidth(0.1);
    // /velocityFrame.setBinOffset(0.05);
    energyFrame.setBinWidth(0.1);
    // /energyFrame.setBinOffset(0.05);
    random = new Random(System.currentTimeMillis());
    energyFrame.setAutoscaleX(true);
    energyFrame.setPreferredMinMaxX(0, 50);
    // energyFrame.setPreferredMinMaxY(0, 10);
    energyFrame.setAutoscaleY(true);
    velocityFrame.setPreferredMinMaxX(-10, 10);
    velocityFrame.setPreferredMinMaxY(0, 10);
    velocityFrame.setAutoscaleX(true);
    velocityFrame.setAutoscaleY(true);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(4);
    velocityFrame.setXYColumnNames("velocity", "counts", "Velocity Histogram");
    energyFrame.setXYColumnNames("energy", "counts", "Energy Histogram");
  }

  public void calculate() {
    clearData();
    temperature = control.getDouble("Temperature");
    beta = 1.0/temperature;
    mcs = control.getInt("Number of MC steps");
    nequil = mcs/10;
    velocity = control.getDouble("Initial Speed");
    energy = 0.5*velocity*velocity;
    delta = control.getDouble("delta");
    for(int time = 1; time<=mcs; time++) { // equibrate system
      metropolis();
    }
    accept = 0;
    for(int time = 1; time<=mcs; time++) {
      metropolis();
      update(); // update data after each trial
    }
    outputResult();
    // energyFrame.repaint();
    // velocityFrame.repaint();
  }
  
  void outputResult(){
	    control.println(" running ... ");
	    double acceptanceProbability = (double) accept/mcs;
	    control.println("acceptance probability = "+nf.format(acceptanceProbability));
	    control.println("mean velocity = "+nf.format((double) velocityAccumulator/mcs));
	    double meanEnergy = (double) energyAccumulator/mcs;
	    control.println("<E> = "+nf.format(meanEnergy));
  }
  
  void outputAfterReset(){
	    control.println("acceptance probability = 0");
	    control.println("mean velocity = 0");
	    control.println("<E> = 0");
	    control.println();
}

  public void reset() {
    control.setValue("Temperature", 1.0);
    control.setValue("Number of MC steps", 100000);
    control.setValue("Initial Speed", 1.0);
    control.setValue("delta", 4.0);
    // control.setValue("Maximum change in velocity", 4.0);
    control.clearMessages();
    clearData();
    outputAfterReset();
  }

  public void clearData() {
    velocityFrame.clearData();
    energyFrame.clearData();
    energyAccumulator = 0;
    velocityAccumulator = 0;
    accept = 0;
    velocityFrame.repaint();
    energyFrame.repaint();
  }

  public void metropolis() {
    double dv = (2.0*random.nextDouble()-1.0)*delta;                 // trial velocity change
    double velocityTrial = velocity+dv;
    double dE = 0.5*(velocityTrial*velocityTrial-velocity*velocity); // trial
    // energy
    // change
    if(dE>0) {
      if(Math.exp(-beta*dE)<random.nextDouble()) {
        return; // change not accepted
      }
    }
    velocity = velocityTrial;
    energy += dE;
    accept++;
  }

  public void update() {
    energyAccumulator += energy;
    velocityAccumulator += velocity;
    velocityFrame.append(velocity);
    energyFrame.append(energy);
  }

  /**
   * Switch to the WRApp user interface.
   */
  public void switchGUI() {
    Runnable runner = new Runnable() {
      public synchronized void run() {
        OSPRuntime.disableAllDrawing = true;
        OSPFrame mainFrame = getMainFrame();
        XMLControlElement xml = new XMLControlElement(getOSPApp());
        WindowListener[] listeners = mainFrame.getWindowListeners();
        int closeOperation = mainFrame.getDefaultCloseOperation();
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setKeepHidden(true);
        mainFrame.dispose();
        BoltzmannWRApp app = new BoltzmannWRApp();
        BoltzmannControl c = new BoltzmannControl(app, app.energyFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        System.gc();
        OSPRuntime.disableAllDrawing = false;
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  void customize() {
    OSPFrame f = getMainFrame();
    if((f==null)||!f.isDisplayable()) {
      return;
    }
    JMenu menu = f.getMenu("Display");
    JMenuItem item = new JMenuItem("Switch GUI");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchGUI();
      }

    });
    menu.add(item);
    addChildFrame(energyFrame);
    addChildFrame(velocityFrame);
  }

  public static void main(String args[]) {
    BoltzmannApp app = new BoltzmannApp();
    CalculationControl.createApp(app, args);
    app.customize();
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
