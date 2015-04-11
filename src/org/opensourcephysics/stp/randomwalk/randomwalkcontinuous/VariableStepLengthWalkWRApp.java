/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.randomwalkcontinuous;
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
 * VariableStepLengthWalkWRApp adds a custom user interface to VariableStepLengthWalkApp.
 * @author Wolfgang Christian
 */
public class VariableStepLengthWalkWRApp extends VariableStepLengthWalkApp {
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
        VariableStepLengthWalkApp app = new VariableStepLengthWalkApp();
        SimulationControl c = SimulationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
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
    if(control==null || trials ==0) {
       ((EjsSimulationControl) control).getControl("trialsNumber").setProperty("value", "0");
        ((EjsSimulationControl) control).getControl("xNumber").setProperty("value", "");
        ((EjsSimulationControl) control).getControl("x2Number").setProperty("value","");
      return;
    }
    double xbar = xcum/trials;
    double x2bar = x2cum/trials;
    ((EjsSimulationControl) control).getControl("trialsNumber").setProperty("value", ""+trials);
    ((EjsSimulationControl) control).getControl("xNumber").setProperty("value", numberFormat.format(xbar));
    ((EjsSimulationControl) control).getControl("x2Number").setProperty("value", numberFormat.format(x2bar));
  }

  public void setParameter() {
    histogramFrame.clearData();
    initialize();
    GUIUtils.repaintOSPFrames();
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final VariableStepLengthWalkWRApp app = new VariableStepLengthWalkWRApp();
    new VariableStepLengthWalkAppControl(app, app.histogramFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for OneDimensionalWalkApp.
 * @author Wolfgang Christian
 */
class VariableStepLengthWalkAppControl extends EjsSimulationControl {
	VariableStepLengthWalkAppControl(VariableStepLengthWalkApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label",
      "position=west; parent=nPanel;text= N=;tooltip=Maximum number of steps in one trial.;horizontalAlignment=right");
    add("NumberField",
      "parent=nPanel;tooltip=Maximum number of steps in one trial.;variable=N;format=000;size=45,23;action=model.setParameter()");
    add("Panel", "name=pPanel;parent=parameterPanel;layout=flow");
    add("Label",
        "position=west; parent=pPanel;text=p=;tooltip=Probability of step to right.;horizontalAlignment=right;action=model.setParameter()");
    add("NumberField",
      "parent=pPanel;tooltip=Probability of step to right.;variable=p;format=0.00;size=45,23;action=model.setParameter()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=trials=;tooltip=Number of trials.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Number of trials.;size=45,23;editable=false;enabled=false;name=trialsNumber");
    add("Label", "parent=outPanel;text= <x>=;tooltip=Average position.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Average position;format=0.00;size=65,23;editable=false;enabled=false;name=xNumber");
    add("Label", "parent=outPanel;text=<x\u00b2>=;tooltip=Average position squared.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Average position squared.;format=0.00;size=65,23;editable=false;enabled=false;name=x2Number");
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
