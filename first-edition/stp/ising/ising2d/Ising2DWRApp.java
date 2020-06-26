/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsSimulationControl;

/**
 * A graphical user interface for Ising2DApp.
 *
 * @author Wolfgang Christian
 */
public class Ising2DWRApp extends Ising2DApp {
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
        Ising2DApp app = new Ising2DApp();
        SimulationControl c = SimulationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        c.addButton("zeroAverages", "Zero averages");
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.addWindowListener(listeners[i]);
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
  
  public void doStep(){
	  super.doStep();
	  stopRunning();
  }
  
  public void initialize() {
	  super.initialize();
	  GUIUtils.repaintOSPFrames();
  }

  public void stopRunning() {
    if(control==null) {
      return;
    }
    double norm = (ising.mcs==0)
      ? 1
      : 1.0/(ising.mcs*ising.N);
    ((EjsSimulationControl) control).getControl("meanE").setProperty("value", nf.format(ising.E_acc*norm));
    ((EjsSimulationControl) control).getControl("meanM").setProperty("value", nf.format(ising.M_acc*norm));
    ((EjsSimulationControl) control).getControl("C").setProperty("value", nf.format(ising.specificHeat()));
    ((EjsSimulationControl) control).getControl("susceptibility").setProperty("value", nf.format(ising.susceptibility()));
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final Ising2DWRApp app = new Ising2DWRApp();
    new Ising2DControl(app, app.displayFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for IsingLG2.
 * @author Wolfgang Christian
 */
class Ising2DControl extends EjsSimulationControl {
  Ising2DControl(Simulation model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
	getControl("buttonPanel").setProperty("layout", "flow,0,0,0");
    add("Button", "parent=buttonPanel; text=Zero;tooltip=Zero averages;action=model.zeroAverages()");
    add("Panel", "name=paramPanel;parent=controlPanel;position=center;layout=flow,0,0,0");
    add("Panel", "name=temperaturePanel;parent=paramPanel;position=east;layout=flow,0,0,0;horizontalAlignment=left");
    add("Label", "position=west; parent=temperaturePanel;text=  T = ;tooltip=Temperature;horizontalAlignment=right;");
    add("NumberField","parent=temperaturePanel;variable=Temperature;tooltip=Temperature;format=0.000;size=40,23;position=center;action=model.startRunning()");
    add("Panel", "name=fieldPanel;parent=paramPanel;position=center;layout=flow,0,0,0");
    add("Label", "position=west; parent=fieldPanel;text=  H = ;tooltip=External field.;horizontalAlignment=right");
    add("NumberField", "parent=fieldPanel;tooltip=External field.;variable=External field;format=0.000;size=45,23;action=model.startRunning()");
    add("Panel", "name=lengthPanel;parent=paramPanel;position=center;layout=flow,0,0,0");
    add("Label", "position=west; parent=lengthPanel;text=  L = ;tooltip=Lattice size.;horizontalAlignment=right");
    add("NumberField", "parent=lengthPanel;tooltip=Lattice size.;variable=Length;format=00;size=45,23;action=model.initialize()");
    
    JPanel panel = (JPanel) add("Panel","name=outPanel;parent=controlFrame;layout=flow,0,0,0;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <E> =;tooltip=Mean energy.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Mean energy.;size=65,23;editable=false;enabled=false;name=meanE");
    add("Label", "parent=outPanel;text=  <M> =;tooltip=Mean magnetization.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Mean magnetization.;size=65,23;editable=false;enabled=false;name=meanM");
    add("Label", "parent=outPanel;text=  C =;tooltip=Specific heat.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Specific heat.;size=65,23;editable=false;enabled=false;name=C");
    add("Label", "parent=outPanel;text=  \u03a7 =;tooltip=Susceptibility.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Susceptibility.;size=65,23;editable=false;enabled=false;name=susceptibility");
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
