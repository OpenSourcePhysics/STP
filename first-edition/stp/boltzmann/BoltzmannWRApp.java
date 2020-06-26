/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.boltzmann;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.ejs.control.*;

/**
 * Ejs front end for BoltzmannApp.
 *
 * @author Wolfgang Christian
 */
public class BoltzmannWRApp extends BoltzmannApp {
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
        BoltzmannApp app = new BoltzmannApp();
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
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  void outputResult() {
	if(control==null) return;
    double meanEnergy = (double) energyAccumulator/mcs;
	((EjsCalculationControl) control).getControl("meanV").setProperty("value", nf.format((double) velocityAccumulator/mcs));
	((EjsCalculationControl) control).getControl("meanE").setProperty("value", nf.format(meanEnergy));
  }

  void outputAfterReset() {
	if(control==null) return;
	((EjsCalculationControl) control).getControl("meanV").setProperty("value", "0");
	((EjsCalculationControl) control).getControl("meanE").setProperty("value", "0");
  }

  /**
   * Starts the program and loads an optional arg[0] XML data file.
   * @param args String[]
   */
  public static void main(String[] args) {
    final BoltzmannWRApp app = new BoltzmannWRApp();
    new BoltzmannControl(app, app.energyFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for BoltzmannApp.
 * @author Wolfgang Christian
 */
class BoltzmannControl extends EjsCalculationControl {
  BoltzmannControl(Calculation model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
	getControl("buttonPanel").setProperty("layout", "flow,0,0,0");
	add("Panel", "name=parameterPanel;parent=controlPanel;position=east;layout=flow,0,0,0");
	
    add("Panel", "name=temperaturePanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "parent=temperaturePanel;text=  T =;tooltip=Temperature;horizontalAlignment=right");
    add("NumberField","parent=temperaturePanel;variable=Temperature;tooltip=Temperature;format=0.000;size=40,24;action=control.calculate()");
    
    add("Panel", "name=deltaPanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "parent=deltaPanel;text=  \u03b4 =;tooltip=delta;horizontalAlignment=right");
    add("NumberField","parent=deltaPanel;variable=delta;tooltip=delta;format=0.000;size=40,24;action=control.calculate()");
    
    add("Panel", "name=v0Panel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "parent=v0Panel;text=  v0 =;tooltip=Initial speed.;horizontalAlignment=right");
    add("NumberField","parent=v0Panel;variable=Initial Speed;tooltip=Initial speed.;format=0.000;size=40,24;action=control.calculate()");
    
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow,0,0,0;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <v> =;tooltip=Mean velocity.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Mean velocity.;size=65,23;editable=false;enabled=false;name=meanV");
    add("Label", "parent=outPanel;text=  <E> =;tooltip=Mean energy.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Mean energy.;size=65,23;editable=false;enabled=false;name=meanE");
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
