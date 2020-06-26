/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon.extra;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.PlotFrame;

// The demon algorithm applied to an Einstein solid

/**
 * @author jantobochnik
 *
 */
public class EinsteinApp extends AbstractSimulation {
  boolean logScale;
  int[] E; 
  double [] histogram;
// Particle energies
  int N, systemEnergy, demonEnergy, mcs = 0, demonEnergyAccumulator = 0, acceptedMoves = 0; // number of MC moves per particle
  double systemEnergyAccumulator = 0;
  PlotFrame histogramFrame = new PlotFrame("Ed", "ln P(Ed)", "Demon Energy Distribution");;
    JButton logButton;
  NumberFormat nf;

  /**
   * Does the Monte Carlo steps.
   */
  public void doStep() {
    for(int i = 0; i<10000/N; i++) {
      for(int j = 0; j<N; ++j) {
        int particleIndex = (int) (Math.random()*N); // choose
        // particle at
        // random
        int dE = (Math.random()<0.5)
                 ? 1
                 : -1;
        if((demonEnergy-dE>=0)&&(E[particleIndex]+dE>=0)) {
          demonEnergy -= dE;
          E[particleIndex] += dE;
          systemEnergy += dE;
          acceptedMoves++;
        }
        systemEnergyAccumulator += systemEnergy;
        demonEnergyAccumulator += demonEnergy;
        histogram[(int)demonEnergy]++;
     }
      histogramFrame.clearData();
      mcs++;
      for(int e = 0; e < (int)systemEnergy+1; e++)
    	  if(histogram[e] > 0) histogramFrame.append(0,e,Math.log(1.0*histogram[e]/(mcs*N)));
    }
  }

  public void stopRunning() {
    double norm = (mcs*N>0)
                  ? 1.0/(mcs*N)
                  : 0;
    // control.clearMessages();
    control.println("mcs = "+nf.format(mcs));
    control.println("<Ed> = "+nf.format(demonEnergyAccumulator*norm));
    control.println("<E> = "+nf.format(systemEnergyAccumulator*norm));
    control.println("acceptance ratio = "+nf.format(acceptedMoves*norm));
    control.println();
  }

  public void initialize() {
    N = control.getInt("N");
    systemEnergy = control.getInt("system energy");
    histogram = new double[(int)systemEnergy+1];
    E = new int[N];
    for(int i = 0; i<N; i++) {
      E[i] += systemEnergy/N;
    }
    E[0] += systemEnergy%N; // Make sure we put all system energy in
    demonEnergy = 0;
    mcs = 0;
    systemEnergyAccumulator = 0;
    demonEnergyAccumulator = 0;
    acceptedMoves = 0;
    control.clearMessages();
    histogramFrame.clearData();
   }

  public void reset() {
    control.setValue("N", 40);
    control.setValue("system energy", 120);
    control.clearMessages();
    histogramFrame.clearData();
    enableStepsPerDisplay(true);
    initialize();
  }

  public void zeroAverages() {
    mcs = 0;
    systemEnergyAccumulator = 0;
    demonEnergyAccumulator = 0;
    acceptedMoves = 0;
    histogramFrame.clearData();
    histogramFrame.repaint();
    control.clearMessages();
  }

  /**
   * Constructor EinsteinApp
   */
  public EinsteinApp() {
    histogramFrame.setPreferredMinMax(0, 5, 0, 1);
    histogramFrame.setAutoscaleX(true);
    histogramFrame.setAutoscaleY(true);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
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
        EinsteinWRApp app = new EinsteinWRApp();
        EinsteinWRAppControl c = new EinsteinWRAppControl(app, app.histogramFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        c.resetSimulation();
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
    addChildFrame(histogramFrame);
  }

  public static void main(String[] args) {
    EinsteinApp app = new EinsteinApp();
    SimulationControl control = SimulationControl.createApp(app);
    control.addButton("zeroAverages", "Zero averages");
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
