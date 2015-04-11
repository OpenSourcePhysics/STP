/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.einsteinsolid;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.opensourcephysics.controls.Calculation;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsCalculationControl;

public class EinsteinSolidWRApp extends EinsteinSolidApp {
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
        EinsteinSolidApp app = new EinsteinSolidApp();
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
  
  void outputResult(double maxE, double meanEa, double hottocoldprob) {
		if(control==null) return;
		((EjsCalculationControl) control).getControl("omegaA").setProperty("value", numberFormat.format(binom(Ea+Na-1, Ea)));
		((EjsCalculationControl) control).getControl("omegaB").setProperty("value", numberFormat.format(binom(Eb+Nb-1, Eb)));
		((EjsCalculationControl) control).getControl("pAB").setProperty("value", numberFormat.format(hottocoldprob));
	  }

  /**
   * Starts the program and loads an optional arg[0] XML data file.
   * @param args String[]
   */
  public static void main(String[] args) {
    final EinsteinSolidWRApp app = new EinsteinSolidWRApp();
    new EinsteinSolidControl(app, app.plotFrame, args);
    app.customize();
    app.calculate();
    GUIUtils.repaintOSPFrames();
  }

}

/**
 * A custom user interface for EinsteinSolidWRApp.
 * @author Wolfgang Christian
 */
class EinsteinSolidControl extends EjsCalculationControl {
  EinsteinSolidControl(Calculation model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
	getControl("buttonPanel").setProperty("layout", "flow,0,0,0");
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow,0,0,0");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "position=west; parent=nPanel;text=  Na = ;tooltip=Number a.;horizontalAlignment=right");
    add("NumberField", "parent=nPanel;tooltip=Number a.;variable=Na;format=000;size=45,24;action=model.setParameter()");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "position=west; parent=nPanel;text=  Nb = ;tooltip=Number b.;horizontalAlignment=right");
    add("NumberField", "parent=nPanel;tooltip=Number b.;variable=Nb;format=000;size=45,24;action=model.setParameter()");
    
    add("Panel", "name=eaPanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "position=west; parent=eaPanel;text=  Ea = ;tooltip=Mean Ea.;horizontalAlignment=right");
    add("NumberField", "parent=eaPanel;tooltip=Mean Ea.;variable=initial Ea;format=0.00;size=45,24;action=model.setParameter()");
    
    add("Panel", "name=ebPanel;parent=parameterPanel;layout=flow,0,0,0");
    add("Label", "position=west; parent=ebPanel;text=  Eb = ;tooltip=mean Eb.;horizontalAlignment=right");
    add("NumberField", "parent=ebPanel;tooltip=Mean Eb.;variable=initial Eb;format=0.00;size=45,24;action=model.setParameter()");
    getControl("calculateButton").setProperty("visible", "false");
    
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow,0,0,0;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text= \u03a9a = =;tooltip=Omega a.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Omega a.;size=65,23;editable=false;enabled=false;name=omegaA");
    add("Label", "parent=outPanel;text= \u03a9b =;tooltip=Omega b.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Omega b.;size=65,23;editable=false;enabled=false;name=omegaB");
    add("Label", "parent=outPanel;text= Pab =;tooltip=Probability that eneryg flows form a to b.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Probability that eneryg flows form a to b.;size=65,23;editable=false;enabled=false;name=pAB");  
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
