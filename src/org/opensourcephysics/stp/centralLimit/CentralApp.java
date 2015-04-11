/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.centralLimit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.AbstractSimulation;
import org.opensourcephysics.controls.ControlUtils;
import org.opensourcephysics.controls.OSPCombo;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Histogram;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PlottingPanel;

public class CentralApp extends AbstractSimulation {
  int n;
  int trials;
  double x_accum, x2_accum;
  double y_accum, y2_accum;
  double variancex;
  double lambda = 1.0;
  DrawingFrame frame;
  PlottingPanel panel;
  Histogram dist;
  String xDistribution;

  /**
   * Constructor CentralApp
   */
  public CentralApp() {
    panel = new PlottingPanel("y", "P(y)", "");
    frame = new DrawingFrame("P(y)", panel);
    frame.setVisible(true);
    dist = new Histogram();
    dist.setNormalizedToOne(true);
    panel.addDrawable(dist);
    panel.setAutoscaleX(true);
    panel.setAutoscaleY(true);
    panel.limitAutoscaleX(0,1);
    panel.limitAutoscaleY(0,.01);
  }

  public double sample_x_uniform() {
    // Generate a uniform distribution in [0, 1]
    return Math.random();
  }

  public double sample_x_exponential() {
    // Generate distribution f(x) = 1/lambda* e^(-x/lambda), for x > 0
    return -lambda*Math.log(1-Math.random());
  }

  public double sample_x_lorentz() {
    // Generate Lorentzian distribution with mean 0.5
    return Math.tan(Math.PI*sample_x_uniform());
  }

  public double sample_y() {
    double y = 0;
    double x;
    for(int i = 0; i<n; i++) {
      if(xDistribution.equals("uniform")) {
        x = sample_x_uniform();
        x_accum += x;
        x2_accum += x*x;
        y += x;
      } else if(xDistribution.equals("exponential")) {
        x = sample_x_exponential();
        x_accum += x;
        x2_accum += x*x;
        y += x;
      } else if(xDistribution.equals("Lorentz")) {
        x = sample_x_lorentz();
        x_accum += x;
        x2_accum += x*x;
        y += x;
      }
    }
    return y/n;
  }

  public void doStep() {
    for(int t = 0; t<100; t++) {
      double y = sample_y();
      y_accum += y;
      y2_accum += y*y;
      trials++;
      dist.append(y);
    }
    panel.setMessage("trials="+trials);
    panel.render();
  }

  public void initialize() {
    dist.clear();
    frame.repaint();
    x_accum = 0;
    x2_accum = 0;
    y_accum = 0;
    y2_accum = 0;
    trials = 0;
    n = control.getInt("N");
    dist.setBinWidth(0.03/Math.sqrt((double) n)); // variable bin width
    xDistribution = control.getString("distribution");
    control.clearMessages();
  }

  public void reset() {
    control.setValue("N", 12);
    //control.setValue("BinWidth", 0.01);
    OSPCombo combo = new OSPCombo(new String[] {"uniform", "exponential", "Lorentz"}, 0); // second argument is default
    control.setValue("distribution", combo);
    //control.setValue("(U)niform/(E)xponential/(L)orentz", "U");
    initialize();
    enableStepsPerDisplay(true);
  }

  public void stopRunning() {
    if(control==null) {
      return;
    }
    double x_avg = x_accum/(n*trials);
    double x2_avg = x2_accum/(n*trials);
    double variancex = x2_avg-x_avg*x_avg;
    double y_avg = y_accum/trials;
    double y2_avg = y2_accum/trials;
    double variancey = y2_avg-y_avg*y_avg;
    variancey = variancey*trials/(trials-1);
    control.println("trials = "+trials);
    if(xDistribution.equals("Lorentz")) {
      control.println("<x> = "+0.0);
    } else {
      control.println("<x> =" +  ControlUtils.f4(x_avg) + " variance of x = "+ControlUtils.f4(variancex));
    }
    control.println("<y> = "+ControlUtils.f2(y_avg));
    control.println("sample variance s\u00b2= "+ControlUtils.f4(variancey));
    control.println();
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
        CentralWRApp app = new CentralWRApp();
        CentralAppControl c = new CentralAppControl(app, app.frame, null);
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
    addChildFrame(frame);
  }

  public static void main(String[] args) {
    CentralApp app = new CentralApp();
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
