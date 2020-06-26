/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising1d;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;

public class Ising1DApp extends AbstractSimulation {
  DisplayFrame displayFrame = new DisplayFrame("Spin Configuration");
  PlotFrame eFrame = new PlotFrame("time", "E", "Energy");
  PlotFrame mFrame = new PlotFrame("time", "M", "Magnetization");
  Ising1D ising;
  NumberFormat nf;

  /**
   * Constructor Ising1DApp
   */
  public Ising1DApp() {
    ising = new Ising1D();
    eFrame.setPreferredMinMaxX(0, 10);
    eFrame.setAutoscaleX(true);
    eFrame.setAutoscaleY(true);
    eFrame.setMarkerColor(0, Color.RED);
    mFrame.setPreferredMinMaxX(0, 10);
    mFrame.setAutoscaleX(true);
    mFrame.setAutoscaleY(true);
    mFrame.setMarkerColor(0, Color.BLUE);
    displayFrame.setSize(400, 100);
    displayFrame.addDrawable(ising);
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(4);
  }

  public void initialize() {
    ising.initialize(control.getInt("N"), control.getDouble("Temperature"), control.getDouble("External field"));
    displayFrame.setPreferredMinMax(-2, ising.N+2, -2, +3);
    control.clearMessages();
    stopRunning();
  }

  public void doStep() {
    ising.setTemperature(control.getDouble("Temperature"));
    ising.setExternalField(control.getDouble("External field"));
    ising.doOneMCStep();
    mFrame.append(0, ising.mcs, (double) ising.M/ising.N);
    eFrame.append(0, ising.mcs, (double) ising.E/ising.N);
  }

  public void stopRunning() {
    double norm = (ising.mcs==0)
                  ? 1
                  : 1.0/(ising.mcs*ising.N);
    control.println("mcs = "+ising.mcs);
    control.println("<E> = "+nf.format(ising.E_acc*norm));
    control.println("C = "+nf.format(ising.specificHeat()));
    control.println("<M> = "+nf.format(ising.M_acc*norm));
    control.println("Susceptibility \u03a7 = "+nf.format(ising.susceptibility()));
    control.println("Acceptance ratio = "+nf.format(ising.acceptedMoves*norm));
    control.println();
  }

  public void reset() {
    control.setValue("N", 64);
    control.setAdjustableValue("Temperature", 1);
    control.setAdjustableValue("External field", 0);
    enableStepsPerDisplay(true);
  }

  public void zeroAverages() {
    control.clearMessages();
    ising.resetData();
    eFrame.clearData();
    mFrame.clearData();
    GUIUtils.repaintOSPFrames();
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
        Ising1DWRApp app = new Ising1DWRApp();
        Ising1DControl c = new Ising1DControl(app, app.eFrame, null);
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
    addChildFrame(displayFrame);
    addChildFrame(eFrame);
    addChildFrame(mFrame);
  }

  public static void main(String[] args) {
    Ising1DApp app = new Ising1DApp();
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
