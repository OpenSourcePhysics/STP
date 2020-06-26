/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.approach;
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

public class BoxWRApp extends BoxApp {

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
        BoxApp app = new BoxApp();
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

  public void setParameter() {
    plotFrame.clearData();
    initialize();
    GUIUtils.repaintOSPFrames();
  }
  
  public void doStep(){
	  super.doStep();
	  stopRunning();
  }

  public void stopRunning() {
    if(control==null) {
      return;
    }
    String nStr = numberFormatTwoDigits.format((double) nleftAccumulator/zeroedTime);
    String nStr2 = numberFormatTwoDigits.format((double) nleftSquaredAccumulator/zeroedTime);
    String sigmaStr = numberFormatTwoDigits.format((double) nleftSquaredAccumulator/zeroedTime -((double) nleftAccumulator*(double) nleftAccumulator/zeroedTime/zeroedTime));
    ((EjsSimulationControl)control).getControl("avgNumber").setProperty("value", nStr);
    ((EjsSimulationControl)control).getControl("avgNumber2").setProperty("value", nStr2);
    ((EjsSimulationControl)control).getControl("sigma").setProperty("value", sigmaStr);
  }

  public void zeroAverages() {
    nleftAccumulator = 0;
    nleftSquaredAccumulator = 0;
    zeroedTime = 0;
    datasets.clear();
    GUIUtils.repaintOSPFrames();
    if(control==null) {
        return;
    }
    ((EjsSimulationControl)control).getControl("avgNumber").setProperty("value", "");
    ((EjsSimulationControl)control).getControl("avgNumber2").setProperty("value", "");
    ((EjsSimulationControl)control).getControl("sigma").setProperty("value", "");
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final BoxWRApp app = new BoxWRApp();
    new BoxWRAppControl(app, app.plotFrame, args);
    app.customize();
  }

}

/**
 * A custom user interface for BoxWRApp.
 * @author Wolfgang Christian
 */
class BoxWRAppControl extends EjsSimulationControl {
  BoxWRAppControl(BoxWRApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Button", "parent=buttonPanel; text=Zero;tooltip=Zero the averages.;action=model.zeroAverages()");
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "position=west; parent=nPanel;text=  N = ;tooltip=Number of particles.;horizontalAlignment=right");
    add("NumberField",
        "parent=nPanel;tooltip=Number of particles.;variable=N;format=000;size=45,23;action=model.setParameter()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <n>=;tooltip=Mean of the number of particles;horizontalAlignment=right");
    add("TextField",
      "parent=outPanel;tooltip=Mean of the number of particles.;format=0.00;size=65,23;editable=false;enabled=false;name=avgNumber");
    add("Label",
        "parent=outPanel;text=  <n\u00b2>=;tooltip=Mean of the number of particles squared.;horizontalAlignment=right");
    add("TextField",
      "parent=outPanel;tooltip=Mean of the number of particles squared.;format=0.00;size=65,23;editable=false;enabled=false;name=avgNumber2");
    add("Label",
      "parent=outPanel;text= \u03c3\u00b2=;tooltip=Stand deviation squared.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Standard deviation squared.;format=0.00;size=65,23;editable=false;enabled=false;name=sigma");
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
