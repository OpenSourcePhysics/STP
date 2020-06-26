/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/**
 * TODO - check results. Original version was not working, converted to AbstractSimulation
 *
 *
 *
 * Approach to equilibrium. Starts all particles in a central part of simulation cell and
 * keeps track of how many particles are in the left third, center third and right third of cell.
 * User can also reverse velocities to see particles return to their original state for short enough
 * times. Particles interact with Lennard Jones forces. Density is low enough for a gas.
 * @author Jan Tobochnik
 * @author Peter Sibley
 */
package org.opensourcephysics.stp.approach;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.stp.approach.LJgas;

public class LJgasApp extends AbstractSimulation {
  org.opensourcephysics.stp.approach.LJgas gas;
  PlotFrame plotFrame = new PlotFrame("time", "n1, n2, n3", "Number of particles in each cell");
  DisplayFrame displayFrame = new DisplayFrame("Particle positions");
  DrawingPanel displayPanel;
 
  /**
   * Constructor LJgasApp
   */
  public LJgasApp() {
    gas = new LJgas();
    displayPanel=displayFrame.getDrawingPanel();
    plotFrame.setMarkerColor(0, Color.blue);
    plotFrame.setMarkerColor(1, Color.black);
    plotFrame.setMarkerColor(2, Color.red);
    plotFrame.setXYColumnNames(0,"t","left");
    plotFrame.setXYColumnNames(1,"t","center");
    plotFrame.setXYColumnNames(2,"t","right");
  }

  public void initialize() {
	displayPanel.addDrawable(gas);
	displayPanel.setPreferredMinMax(0, 10, 0, 10);
	displayPanel.setAutoscaleX(true);
	displayPanel.setAutoscaleY(true);
    gas.numberOfParticles = control.getInt("N");
    gas.initialize();
    displayPanel.setPreferredMinMax(-0.2*gas.cellLength, 1.2*gas.cellLength, -0.2*gas.cellLength, 1.2*gas.cellLength);
   }
  
 

  public void doStep() {
    for(int i = 0; i<10; i++) {
      gas.step(); // advance the solution of the ODE by one step
    }
    //double norm = 1.0/gas.steps;
    plotFrame.append(0, gas.t, gas.n0); // blue, left
    plotFrame.append(1, gas.t, gas.n1); // black middle
    plotFrame.append(2, gas.t, gas.n2); // red, right
    gas.zeroAverages();
  }

  public void reset() {
    //gas.initialize();
    control.setValue("N", 270);
    initialize();
  }

  public void reverse() {
    gas.reverse();
    gas.zeroAverages();
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
        LJgasWRApp app = new LJgasWRApp();
        LJgasWRAppControl c = new LJgasWRAppControl(app, app.displayFrame, null);
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
	    addChildFrame(displayFrame);
	  }

  public static void main(String[] args) {
	LJgasApp app =new LJgasApp();
    SimulationControl control = SimulationControl.createApp(app, args);
    control.addButton("reverse", "Reverse");
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
