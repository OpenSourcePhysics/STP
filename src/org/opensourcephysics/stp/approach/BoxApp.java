/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.approach;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.PlotFrame;

/**
 *  simulation of the particles in a box problem
 *
 * @author     jgould
 * @created    December 26, 2002
 * @authors    Joshua Gould , Peter Sibley
 * modified    Natali Gulbahce, Oct 2002, Jan 28 2003
 */
public class BoxApp extends AbstractSimulation {
  PlotFrame plotFrame = new PlotFrame("time", "n", "");
  DatasetManager datasets;

  /**
   * These are the variables used by the simulation.
   */
  int N = 64;       // total number of particles
  public int nleft; // number on left
  int time;
  int zeroedTime;
  int nleftAccumulator;
  int nleftSquaredAccumulator;
  NumberFormat numberFormatTwoDigits = NumberFormat.getInstance();

  /**
   * Constructor BoxApp
   */
  public BoxApp() {
	plotFrame.setTitle("Approach to Equilibrium.");
	plotFrame.setMarkerColor(0,Color.RED);
    numberFormatTwoDigits.setMaximumFractionDigits(2);
    datasets=plotFrame.getDatasetManager();
  }

  public void reset() {
    control.setValue("N", 64);
    plotFrame.setPreferredMinMaxX(0, 100);
    plotFrame.setPreferredMinMaxY(0, N);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    plotFrame.repaint();
    zeroAverages();
  }

  public void initialize() {
    N = control.getInt("N");
    time = 0;
    zeroedTime = 0;
    nleft = N; // all particles initially on left side
    nleftAccumulator = 0;
    nleftSquaredAccumulator = 0;
  }

  // move particle through hole
  public void moveParticle() {
    // generate random number and move particle
    double r = Math.random();
    double ratio = (double) nleft/N;
    if(r<=ratio) {
      nleft--;
    } else {
      nleft++;
    }
    time++;
    zeroedTime++;
  }

  public void stopRunning() {
    control.clearMessages();
    control.println("<n> = "+numberFormatTwoDigits.format((double) nleftAccumulator/zeroedTime));
    control.println("<n\u00b2> = "+numberFormatTwoDigits.format((double) nleftSquaredAccumulator/zeroedTime));
    control.println("\u03c3\u00b2 = "
      +numberFormatTwoDigits.format((double) nleftSquaredAccumulator/zeroedTime-((double) nleftAccumulator*(double) nleftAccumulator/zeroedTime/zeroedTime)));
  }

  public void zeroAverages() {
    nleftAccumulator = 0;
    nleftSquaredAccumulator = 0;
    zeroedTime = 0;
    datasets.clear();
    control.println("<n> = 0");
    control.println("<n\u00b2> = 0");
    control.println("\u03c3\u00b2 = 0");
  }

  public void doStep() {
    plotFrame.append(0, time, nleft);
    moveParticle();
    nleftAccumulator += nleft;
    nleftSquaredAccumulator += nleft*nleft;
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
        BoxWRApp app = new BoxWRApp();
        BoxWRAppControl c = new BoxWRAppControl(app, app.plotFrame, null);
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
    addChildFrame(plotFrame);
  }

  public static void main(String[] args) {
    BoxApp app = new BoxApp();
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
