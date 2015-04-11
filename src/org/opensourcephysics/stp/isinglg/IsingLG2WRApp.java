/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.isinglg;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsSimulationControl;

public class IsingLG2WRApp extends IsingLG2App {
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
        IsingLG2App app = new IsingLG2App();
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
  
  public void stopRunning() {
	  
  }


  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final IsingLG2WRApp app = new IsingLG2WRApp();
    new IsingLG2Control(app, app.displayFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for IsingLG2.
 * @author Wolfgang Christian
 */
class IsingLG2Control extends EjsSimulationControl {
  IsingLG2Control(Simulation model, DrawingFrame frame, String[] args) {
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
    add("NumberField","parent=temperaturePanel;variable=temperature;tooltip=Temperature;format=0.000;size=40,23;position=center;action=model.startRunning()");
    
    add("Panel", "name=muLPanel;parent=paramPanel;position=east;layout=flow,0,0,0;horizontalAlignment=left");
    add("Label", "position=west; parent=muLPanel;text=  \u03bcL = ;tooltip=Left chemical potential.;horizontalAlignment=right;");
    add("NumberField","parent=muLPanel;variable=left chemical potential;tooltip=Left chemical potential.;format=0.000;size=40,23;position=center;action=model.startRunning()");

    add("Panel", "name=muRPanel;parent=paramPanel;position=east;layout=flow,0,0,0;horizontalAlignment=left");
    add("Label", "position=west; parent=muRPanel;text=  \u03bcR = ;tooltip=Right chemical potential.;horizontalAlignment=right;");
    add("NumberField","parent=muRPanel;variable=right chemical potential;tooltip=Right chemical potential.;format=0.000;size=40,23;position=center;action=model.startRunning()");

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
