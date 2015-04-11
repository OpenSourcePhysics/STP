/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.estimation;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsCalculationControl;

/**
 * MonteCarloEstimationWRApp adds a custom user interface to MonteCarloEstimationApp.
 * @author Wolfgang Christian
 */
public class MonteCarloEstimationWRApp extends MonteCarloEstimationApp {
	DecimalFormat f10 = new DecimalFormat("#0.000000000000000");
	
  /**
   * Switch to the App user interface.
   */
  public void switchGUI() {
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
        MonteCarloEstimationApp app = new MonteCarloEstimationApp();
        CalculationControl c = CalculationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        c.setValue("model", null);
        app.customize();
        System.gc();
        OSPRuntime.disableAllDrawing = false;
        c.clearMessages();
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  public void calculate() {
    control.setValue("seed", String.valueOf(System.currentTimeMillis()));
    seed = Long.parseLong(control.getString("seed"));
    super.calculate();
  }

  public void displayArea(long hits) {
    if(control==null) {
      return;
    }
    double estimatedArea = (hits*(b-a)*ymax)/n;
    ((EjsCalculationControl) control).getControl("errorNumber").setProperty("value", f10.format(estimatedArea));
    //((EjsCalculationControl) control).getControl("errorNumber").setProperty("value", ""+estimatedArea);
  }

  /**
   * Starts the program and loads an optional arg[0] XML data file.
   * @param args String[]
   */
  public static void main(String[] args) {
    final MonteCarloEstimationWRApp app = new MonteCarloEstimationWRApp();
    new MonteCarloEstimationControl(app, app.plotFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for MonteCarloEstimationApp.
 * @author Wolfgang Christian
 */
class MonteCarloEstimationControl extends EjsCalculationControl {
  MonteCarloEstimationControl(MonteCarloEstimationApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Panel", "name=parameterPanel;parent=controlPanel;position=east;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;position=east;layout=flow");
    add("Label", "position=west; parent=nPanel;text= n=;tooltip=Number of trials.;horizontalAlignment=right");
    add("NumberField",
        "parent=nPanel;tooltip=Number of trials.;variable=n;format=0;size=45,23;action=control.calculate()");
    add("Panel", "name=seedPanel;parent=parameterPanel;position=east;layout=flow");
    add("Label", "position=west; parent=seedPanel;text= seed=;tooltip=Random number seed.;horizontalAlignment=right");
    add("TextField",
        "parent=seedPanel;tooltip=Random number seed.;variable=seed;size=145,23;action=control.calculate()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=Estimated area = ;tooltip=Estimated area.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Estimated area.;size=150,23;editable=false;enabled=false;name=errorNumber");
    //getControl("calculateButton").setProperty("visible", "false");
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
