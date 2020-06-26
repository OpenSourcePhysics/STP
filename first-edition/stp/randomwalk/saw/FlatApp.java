/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.saw;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;

/**
 * Simulates random walkers in one dimension
 *
 *  @author Jan Tobochnik, Wolfgang Christian,  Harvey Gould
 *  @version 1.0  revised 04/21/05
 */
public class FlatApp extends AbstractSimulation {
  Flat walkers = new Flat();
  PlotFrame xyFrame = new PlotFrame("time", "", "<x>,<y>");
  PlotFrame x2y2Frame = new PlotFrame("ln time", "x^2, y^2", "ln <x^2>,ln <y^2>");
  PlotFrame r2Frame = new PlotFrame("ln time", "r^2", "ln <r^2>");
  DatasetManager datasets;
  DrawingPanel drawingPanel;
  int step, nstep, maxStep; // time step

  /**
   *   Sets column names for data table
   */
  public FlatApp() {
    xyFrame.setXYColumnNames(0, "t", "<x>");
    xyFrame.setXYColumnNames(1, "t", "<y>");
    x2y2Frame.setXYColumnNames(0, "ln t", "ln <x^2>");
    x2y2Frame.setXYColumnNames(1, "ln t", "ln <y^2>");
    x2y2Frame.setAutoscaleX(true);
    x2y2Frame.setAutoscaleY(true);
    x2y2Frame.limitAutoscaleX(0, 10);
    x2y2Frame.limitAutoscaleY(0, 10);
    xyFrame.setAutoscaleX(true);
    xyFrame.setAutoscaleY(true);
    xyFrame.limitAutoscaleX(0, 10);
    xyFrame.limitAutoscaleY(0, 10);
    datasets=r2Frame.getDatasetManager();
    datasets.setXYColumnNames(0, "ln t", "ln <r^2>");
    drawingPanel=r2Frame.getDrawingPanel();
    drawingPanel.setAutoscaleX(true);
    drawingPanel.setAutoscaleY(true);
    drawingPanel.limitAutoscaleX(0, 10);
    drawingPanel.limitAutoscaleY(0, 10);
  }

  /**
   *   Gets parameters and initializes model
   */
  public void initialize() {
    walkers.numberOfWalkers = control.getInt("number of initial walkers");
    maxStep = control.getInt("number of steps");
    OneWalker.s0 = (short) (maxStep/2);
    //walkers.L = control.getInt("Half lattice Size");
    walkers.initialize();
    step = 0;
    nstep = 2;
    drawingPanel.setMessage("time = "+step);
  }

  /**
   * Does one walker at a time
   */
  public void doStep() {
    if(nstep<=maxStep) {
      int step0 = step;
      for(int i = step0; i<nstep; i++) {
        walkers.step();
        step++;
      }
      nstep *= 2;
      double norm = walkers.norm;
      double xbar = walkers.xAccum/norm;
      double x2bar = walkers.xSquaredAccum/norm;
      double ybar = walkers.yAccum/norm;
      double y2bar = walkers.ySquaredAccum/norm;
      xyFrame.append(0, step, xbar);
      xyFrame.append(1, step, ybar);
      x2y2Frame.append(0, Math.log(step), Math.log(x2bar-xbar*xbar));
      x2y2Frame.append(1, Math.log(step), Math.log(y2bar-ybar*ybar));
      datasets.append(0, Math.log(step), Math.log(x2bar-xbar*xbar+y2bar-ybar*ybar));
      xyFrame.setMessage("# walkers = "+walkers.numberOfWalkers);
      drawingPanel.setMessage("time = "+step);
    }
  }

  /**
   *  Resets to default values
   */
  public void reset() {
    control.setValue("number of initial walkers", 100);
    control.setValue("number of steps", 1024);
    //control.setAdjustableValue("steps per display", 100);
    //control.setValue("Half lattice Size", 100);
    //enableStepsPerDisplay(true);
    xyFrame.clearData();
    datasets.clear();
    x2y2Frame.clearData();
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
        FlatWRApp app = new FlatWRApp();
        FlatAppControl c = new FlatAppControl(app, app.r2Frame, null);
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
    addChildFrame(xyFrame);
    addChildFrame(x2y2Frame);
    addChildFrame(r2Frame);
  }

  /**
   * Starts the Java application.
   * @param args  command line parameters
   */
  public static void main(String[] args) {
FlatApp app=new FlatApp();
    SimulationControl.createApp(app);
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
