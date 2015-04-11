/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.isinglg;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;

/**
 * IsingApp simulates a two-dimensional Ising model.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0  revised 07/05/05
 */
public class IsingLG2App extends AbstractSimulation {
  IsingLG2 ising = new IsingLG2();
  LatticeFrame displayFrame = new LatticeFrame("Ising Model");
  PlotFrame plotFrame = new PlotFrame("time", "nLeft and nRight", "Number of Particles");;

  /**
   * Constructor IsingLG2App
   */
  public IsingLG2App() {
    plotFrame.setXYColumnNames(0, "mcs", "nLeft");
    plotFrame.setXYColumnNames(1, "mcs", "nRight");
    plotFrame.setMarkerColor(0, Color.green);
    plotFrame.setPreferredMinMax(0, 10, 0, 10);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
  }

  public void initialize() {
    int L = control.getInt("L");
    ising.temperature = control.getDouble("temperature");
    ising.chemicalPotentialLeft = control.getDouble("left chemical potential");
    ising.chemicalPotentialRight = control.getDouble("right chemical potential");
    ising.nLeft = (int) (0.5*L*L*control.getDouble("density of particles on left"));
    ising.nRight = (int) (0.5*L*L*control.getDouble("density of particles on right"));
    ising.initialize(L, displayFrame);
    control.clearMessages();
    stopRunning();
    //   resetData();
  }

  public void doStep() {
    ising.doOneMCStep();
    plotFrame.append(0, ising.mcs, ising.nLeft);
    plotFrame.append(1, ising.mcs, ising.nRight);
  }

  public void stopRunning() {
    control.println("mcs = "+ising.mcs);
    control.println("Number of particles in the left = "+ising.nLeft);
    control.println("Number of particles in the right = "+ising.nRight);
    control.println();
  }

  public void startRunning() {
    ising.temperature = control.getDouble("temperature");
    ising.chemicalPotentialLeft = control.getDouble("left chemical potential");
    ising.chemicalPotentialRight = control.getDouble("right chemical potential");
    ising.setBoltzmannArrays();
  }

  public void reset() {
    control.setValue("L", 32);
    control.setAdjustableValue("temperature", 1.0);
    control.setAdjustableValue("left chemical potential", -1);
    control.setAdjustableValue("right chemical potential", -2);
    control.setValue("density of particles on right", 0.04);
    control.setValue("density of particles on left", 0.2);
    enableStepsPerDisplay(true); // allow user to speed up simulation
  }

  public void zeroAverages() {
    ising.resetData();
    plotFrame.clearData();
    GUIUtils.repaintOSPFrames();
    control.clearMessages();
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
        IsingLG2WRApp app = new IsingLG2WRApp();
        IsingLG2Control c = new IsingLG2Control(app, app.displayFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        app.initialize();
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
    addChildFrame(displayFrame);
    addChildFrame(plotFrame);
  }

  public static void main(String[] args) {
    IsingLG2App app = new IsingLG2App();
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
 *
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
