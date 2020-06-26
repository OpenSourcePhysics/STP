/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.cointoss;
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
 * Ejs front end for MultipleCoinTossApp.
 *
 * @author Wolfgang Christian
 */
public class MultipleCoinTossWRApp extends MultipleCoinTossApp {
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
        MultipleCoinTossApp app = new MultipleCoinTossApp();
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
	    double avg = (totalFlips>0)
	                 ? sumHeads/totalFlips
	                 : 0;
	    double avg2 = (totalFlips>0)
	                  ? sum2Heads/totalFlips
	                  : 0;
	    ((EjsSimulationControl) control).getControl("meanH").setProperty("value", nf.format(avg));
	    ((EjsSimulationControl) control).getControl("meanHH").setProperty("value", nf.format(avg2));
	    double sigma = Math.sqrt(avg2-avg*avg);
	    if(totalFlips>0) {
	      ((EjsSimulationControl) control).getControl("sigma").setProperty("value", nf.format(sigma));
	    } else {
	      ((EjsSimulationControl) control).getControl("sigma").setProperty("value", "undefined");
	    }
	    ((EjsSimulationControl) control).getControl("nFlips").setProperty("value", ""+totalFlips);
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
    final MultipleCoinTossWRApp app = new MultipleCoinTossWRApp();
    new MultipleCoinTossWRAppControl(app, app.histogramFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for MultipleCoinTossWRApp.
 * @author Wolfgang Christian
 */
class MultipleCoinTossWRAppControl extends EjsSimulationControl {
  MultipleCoinTossWRAppControl(MultipleCoinTossWRApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=nPanel;text=  N = ;tooltip=Number of coins to flip.;horizontalAlignment=right");
    add("NumberField",
      "parent=nPanel;tooltip=Number of coins to flip.;variable=coins to flip;format=000;size=45,23;action=model.setParameter()");
    add("Panel", "name=dimensionPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=dimensionPanel;text=  p = ;tooltip=Probability;horizontalAlignment=right");
    add("NumberField",
      "parent=dimensionPanel;tooltip=Probability;variable=probability;format=0.00;size=45,23;action=model.setParameter()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <H>=;tooltip=Mean number of heads;horizontalAlignment=right");
    add("TextField","parent=outPanel;tooltip=Mean number of heads.;size=60,23;editable=false;enabled=false;name=meanH");
    add("Label", "parent=outPanel;text=  <H\u00b2>=;tooltip=Mean number of heads squared;horizontalAlignment=right");
    add("TextField","parent=outPanel;tooltip=Mean number of heads squared.;size=60,23;editable=false;enabled=false;name=meanHH");
    add("Label", "parent=outPanel;text=  \u03c3=;tooltip=Variance of heads.;horizontalAlignment=right");
    add("TextField","parent=outPanel;tooltip=Variance of heads.;size=60,23;editable=false;enabled=false;name=sigma");
    add("Label", "parent=outPanel;text=  Trials=;tooltip=Number of flips.;horizontalAlignment=right");
    add("TextField","parent=outPanel;tooltip=Number of flips.;size=40,23;editable=false;enabled=false;name=nFlips");
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
