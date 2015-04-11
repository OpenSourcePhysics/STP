/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsSimulationControl;

/**
 * DemonWRApp adds a custom user interface to DemonApp.
 * @author Wolfgang Christian
 */
public class DemonWRApp extends DemonApp {
  /**
   * Switch to the App user interface.
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
        DemonApp app = new DemonApp();
        SimulationControl c = SimulationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        c.addButton("zeroAverages", "Zero averages");
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        c.setValue("model", null);
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

  public void doStep() {
    super.doStep();
    stopRunning();
  }

  public void stopRunning() {
    if(control==null) {
      return;
    }
    double norm = (mcs*N>0)
                  ? 1.0/(mcs*N)
                  : 0;
    String edStr = nf.format(demonEnergyAccumulator*norm);
    String eStr = nf.format(systemEnergyAccumulator*norm);
    String rStr = nf.format(acceptedMoves*norm);
    ((EjsSimulationControl) control).getControl("mcsNumber").setProperty("value", ""+mcs);
    ((EjsSimulationControl) control).getControl("edNumber").setProperty("value", edStr);
    ((EjsSimulationControl) control).getControl("eNumber").setProperty("value", eStr);
    ((EjsSimulationControl) control).getControl("ratio").setProperty("value", rStr);
  }

  public void zeroAverages() {
    mcs = 0;
    systemEnergyAccumulator = 0;
    demonEnergyAccumulator = 0;
    acceptedMoves = 0;
    histogramFrame.clearData();
    histogramFrame.repaint();
    if(control==null) {
      return;
    }
    ((EjsSimulationControl) control).getControl("mcsNumber").setProperty("value", "");
    ((EjsSimulationControl) control).getControl("edNumber").setProperty("value", "");
    ((EjsSimulationControl) control).getControl("eNumber").setProperty("value", "");
    ((EjsSimulationControl) control).getControl("ratio").setProperty("value", "");
  }

  public void setParameter() {
    zeroAverages();
    histogramFrame.clearData();
    vhistogramFrame.clearData();
    initialize();
    GUIUtils.repaintOSPFrames();
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final DemonWRApp app = new DemonWRApp();
    new DemonWRAppControl(app, app.histogramFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for DemonWRApp.
 * @author Wolfgang Christian
 */
class DemonWRAppControl extends EjsSimulationControl {
  DemonWRAppControl(DemonWRApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Button", "parent=buttonPanel; text=Zero;tooltip=Zero averages;action=model.zeroAverages()");
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=nPanel;text= N=;tooltip=Number of particles.;horizontalAlignment=right");
    add("NumberField",
        "parent=nPanel;tooltip=Number of particles.;variable=N;format=000;size=45,23;action=model.setParameter()");
    add("Panel", "name=ePanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=ePanel;text=E=;tooltip=Energy.;horizontalAlignment=right");
    add("NumberField",
        "parent=ePanel;tooltip=Energy.;variable=system energy;format=0.00;size=45,23;action=model.setParameter()");
    add("Panel", "name=dimensionPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=dimensionPanel;text=D=;tooltip=Dimension;horizontalAlignment=right");
    add("NumberField",
        "parent=dimensionPanel;tooltip=Dimension;variable=dimension;format=0;size=25,23;action=model.setParameter()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=mcs=;tooltip=Monte Carlo steps.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Monte Carlo steps.;size=45,23;editable=false;enabled=false;name=mcsNumber");
    add("Label", "parent=outPanel;text=  <Ed>=;tooltip=Demon energy.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Demon energy;format=0.00;size=65,23;editable=false;enabled=false;name=edNumber");
    add("Label", "parent=outPanel;text=  <E>=;tooltip=Particle energy.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Particle energy.;format=0.00;size=65,23;editable=false;enabled=false;name=eNumber");
    add("Label", "parent=outPanel;text= r=;tooltip=Acceptance ratio.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Acceptance ratio.;format=0.00;size=65,23;editable=false;enabled=false;name=ratio");
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
