/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.randomwalkcontinuous;
import java.text.NumberFormat;
import java.util.Random;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.HistogramFrame;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * A random walk in 1D with continuous step size
 *
 * @author Hui Wang
 * @author Wolfgang Christian
 */
public class VariableStepLengthWalkApp extends AbstractSimulation {
  int N;        // maximum number of steps in one trial
  double p;     // probability of step to right
  int trials;   // number of trials
  double xcum;  // accumulate values of x after N steps
  double x2cum; // accumulate values of x*x after N steps
  HistogramFrame histogramFrame = new HistogramFrame("x", "H(x)", "Histogram");
  DrawingPanel drawingPanel;
  NumberFormat numberFormat;
  Random random;

  /**
   * Constructor VariableStepLengthWalkApp
   */
  public VariableStepLengthWalkApp() {
    drawingPanel = histogramFrame.getDrawingPanel();
    numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(2);
    random = new Random();
    histogramFrame.setBinStyle((short) 1);
    histogramFrame.setDiscrete(false);
    //      set different colors for bin and edge
    histogramFrame.setBinColor(null, Color.red);
  }

  public void initialize() {
    N = control.getInt("N");
    p = control.getDouble("p");
    trials = 0;
    xcum = 0;
    x2cum = 0;
    //drawingPanel.setPreferredMinMax(x-N, x+N, 0, 10);
    histogramFrame.setBinWidth(control.getDouble("bin width"));
    drawingPanel.setAutoscaleX(true);
    drawingPanel.setAutoscaleY(true);
    drawingPanel.limitAutoscaleX(-N,N);
    drawingPanel.limitAutoscaleY(0, 10);
    random.setSeed(System.currentTimeMillis());
  }

  public void reset() {
    N = 16;
    p = 0.5;
    trials=0;
    control.setAdjustableValue("N", N);
    control.setAdjustableValue("p", p);
    control.setAdjustableValue("bin width", 0.5);
    histogramFrame.clearData();
    enableStepsPerDisplay(true);
  }

  public double sample_x_exponential() {
    // Generate distribution f(x) = 1 / e^x, for x > 0
    return -0.5*Math.log(1-Math.random());
  }

  public double sample_x_uniform() {
    // Generate uniform distribution
    return Math.random();
  }

  protected void doStep() {
	double x = 0;
    for(int steps = 0; steps<N; steps++) {
      if(random.nextDouble()<p) {
        x += sample_x_uniform();
      } else {
        x -= sample_x_uniform();
      }
    }
    trials++;
    histogramFrame.append(x);
    xcum += x;
    x2cum += x*x;
  }

  public void stopRunning() {
    control.clearMessages();
    if(trials==0) {
      return;
    }
    double xbar = xcum/trials;
    double x2bar = x2cum/trials;
    control.println("trials = "+trials);
    control.println("<x> = "+numberFormat.format(xbar));
    control.println("<x^2> = "+numberFormat.format(x2bar));
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
        VariableStepLengthWalkWRApp app = new VariableStepLengthWalkWRApp();
        VariableStepLengthWalkAppControl c = new VariableStepLengthWalkAppControl(app, app.histogramFrame, null);
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
    VariableStepLengthWalkApp app = new VariableStepLengthWalkApp();
    SimulationControl.createApp(app, args);
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
