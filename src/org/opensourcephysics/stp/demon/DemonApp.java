/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.frames.HistogramFrame;
import org.opensourcephysics.frames.PlotFrame;

// The demon algorithm applied to an ideal gas
public class DemonApp extends AbstractSimulation {
  int N, dimensions, acceptedMoves = 0, mcs = 0;
  double[][] v; // (N*dimensions) particle velocities
  double systemEnergy, demonEnergy, systemEnergyAccumulator = 0, demonEnergyAccumulator = 0, delta, exponent;
  JButton logButton;
  NumberFormat nf;
  PlotFrame histogramFrame = new PlotFrame("Ed", "ln P(Ed)", "Demon Energy Distribution");;
  HistogramFrame vhistogramFrame = new HistogramFrame("Vx", "Histogram", "Histogram of vx");;
  double [] histogram;
  double[] offsetVelocity(double[] vel) {
    double[] vnew = new double[dimensions];
    for(int d = 0; d<dimensions; d++) {
      vnew[d] = vel[d]+(2.0*Math.random()-1.0)*delta;
    }
    return vnew;
  }

  double getSpeed(double[] vel) {
    double speed = 0;
    for(int d = 0; d<dimensions; d++) {
      speed += vel[d]*vel[d];
    }
    return Math.sqrt(speed);
  }

  public void doStep() {
    for(int i = 0; i<10000/N; i++) {
      for(int j = 0; j<N; ++j) {
        int particleIndex = (int) (Math.random()*N); // choose
        // particle at
        // random
        double[] v_old = v[particleIndex];
        double[] v_new = offsetVelocity(v_old);
        double dE = (Math.pow(getSpeed(v_new), exponent)-Math.pow(getSpeed(v_old), exponent));
        if(dE<=demonEnergy) {
          v[particleIndex] = v_new;
          acceptedMoves++;
          systemEnergy += dE;
          demonEnergy -= dE;
        }
        systemEnergyAccumulator += systemEnergy;
        demonEnergyAccumulator += demonEnergy;
        histogram[(int)demonEnergy]++;
        vhistogramFrame.append(v[j][0]);
      }
      histogramFrame.clearData();
      mcs++;
      for(int e = 0; e < (int)systemEnergy+1; e++){
    	  if(histogram[e] > 0) histogramFrame.append(0,e,Math.log(1.0*histogram[e]/(mcs*N)));
      }
     
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
    dimensions = Math.min(control.getInt("dimension"), 3);
    control.setValue("dimension", dimensions);
    systemEnergy = control.getDouble("system energy");
    delta = 0.1; // control.getDouble("Delta");
    exponent = control.getDouble("momentum exponent");
    v = new double[N][dimensions];
    double v0 = Math.pow(systemEnergy/N, 1.0/exponent);
    for(int i = 0; i<N; i++) {
      v[i][0] = v0;
      vhistogramFrame.append(v0);
    }
    histogram = new double[(int)systemEnergy+1];
    demonEnergy = 0;
    mcs = 0;
    systemEnergyAccumulator = 0;
    demonEnergyAccumulator = 0;
    acceptedMoves = 0;
    control.clearMessages();
   }

  public void reset() {
    control.setValue("N", 40);
    control.setValue("dimension", 3);
    control.setValue("system energy", 40);
    control.setValue("momentum exponent", "2");
    control.clearMessages();
    histogramFrame.clearData();
    vhistogramFrame.clearData();
    enableStepsPerDisplay(true);
  }

  public void zeroAverages() {
    mcs = 0;
    systemEnergyAccumulator = 0;
    demonEnergyAccumulator = 0;
    acceptedMoves = 0;
    histogramFrame.clearData();
    histogramFrame.repaint();
    control.clearMessages();
    stopRunning();
  }

  /**
   * Constructor DemonApp
   */
  public DemonApp() {
    vhistogramFrame.setBinWidth(0.1);
    vhistogramFrame.setPreferredMinMaxX(-5, 5);
    histogramFrame.setAutoscaleY(true);
    histogramFrame.setAutoscaleX(true);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
  }

  /**
   * Switch to the WRApp user interface.
   */
  public void switchGUI() {
	stopSimulation();
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
        DemonWRApp app = new DemonWRApp();
        DemonWRAppControl c = new DemonWRAppControl(app, app.histogramFrame, null);
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
    addChildFrame(vhistogramFrame);
  }

  public static void main(String[] args) {
    DemonApp app = new DemonApp();
    SimulationControl control = SimulationControl.createApp(app, args);
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
