/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.approach;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsSimulationControl;

public class LJgas2boxWRApp extends LJgas2boxApp {
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
        LJgas2boxApp app = new LJgas2boxApp();
        SimulationControl c = SimulationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        c.addButton("reverse", "Reverse");
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
  
  void messages(){
	  
  }

  public void setParameter() {
    initialize();
    GUIUtils.repaintOSPFrames();
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final LJgas2boxWRApp app = new LJgas2boxWRApp();
    new LJgas2boxWRAppControl(app, app.displayFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for BoxWRApp.
 * @author Wolfgang Christian
 */
class LJgas2boxWRAppControl extends EjsSimulationControl {
	LJgas2boxWRAppControl(LJgas2boxApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
	// removed in order to simplify the user interface
    // add("Button", "parent=buttonPanel; text=Reverse;tooltip=Reverse the velocities.;action=model.reverse()");
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=nPanel;text=  N = ;tooltip=Number of particles.;horizontalAlignment=right");
    add("NumberField",
        "parent=nPanel;tooltip=Number of particles.;variable=N;format=000;size=45,23;action=model.setParameter()");
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
