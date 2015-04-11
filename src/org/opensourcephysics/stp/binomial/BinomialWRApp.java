/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.binomial;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import org.opensourcephysics.controls.Calculation;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsCalculationControl;

/**
 * Ejs front end for BinomialApp.
 *
 * @author Wolfgang Christian
 */
public class BinomialWRApp extends BinomialApp {
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
        BinomialApp app = new BinomialApp();
        CalculationControl c = CalculationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        app.resetCalculation();
        System.gc();
        OSPRuntime.disableAllDrawing = false;
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  public void setParameter() {
    calculate();
    GUIUtils.repaintOSPFrames();
  }

  /**
   * Starts the program and loads an optional arg[0] XML data file.
   * @param args String[]
   */
  public static void main(String[] args) {
    final BinomialWRApp app = new BinomialWRApp();
    new BinomialControl(app, app.plotFrame, args);
    app.customize();
    app.calculate();
    GUIUtils.repaintOSPFrames();
  }

}

/**
 * A custom user interface for BinomialWRApp.
 * @author Wolfgang Christian
 */
class BinomialControl extends EjsCalculationControl {
  BinomialControl(Calculation model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=nPanel;text=  N = ;tooltip=Number of trials.;horizontalAlignment=right");
    add("NumberField",
        "parent=nPanel;tooltip=Number of trials.;variable=N;format=000;size=45,23;action=model.setParameter()");
    add("Panel", "name=dimensionPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=dimensionPanel;text=  p = ;tooltip=Probability;horizontalAlignment=right");
    add("NumberField",
        "parent=dimensionPanel;tooltip=Probability;variable=p;format=0.00;size=45,23;action=model.setParameter()");
    getControl("calculateButton").setProperty("visible", "false");
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
