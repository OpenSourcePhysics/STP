/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.centralLimit;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.controls.ControlUtils;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.control.EjsSimulationControl;

/**
 * CentralWRApp adds a custom user interface to CentralApp.
 * @author Wolfgang Christian
 */
public class CentralWRApp extends CentralApp {
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
        CentralApp app = new CentralApp();
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
    initialize();
    stopRunning();
    GUIUtils.repaintOSPFrames();
  }

  public void setDistribution() {
    if(control==null) {
      return;
    }
    //System.out.println("Option in combo is now "+control.getString("distribution"));
    initialize();
    stopRunning();
    GUIUtils.repaintOSPFrames();
  }

  public void doStep() {
    super.doStep();
    stopRunning();
  }

  public void stopRunning() {
    if(control==null) {
      return;
    }
    if(xDistribution.equals("l")) {
      ((EjsSimulationControl) control).getControl("meanX").setProperty("value", "0.0");
      ((EjsSimulationControl) control).getControl("devX2").setProperty("value", "");
    } else {
      ((EjsSimulationControl) control).getControl("meanX").setProperty("value", "0.5");
      ((EjsSimulationControl) control).getControl("devX2").setProperty("value", "");
    }
    if(trials<2) {
      ((EjsSimulationControl) control).getControl("meanY").setProperty("value", "");
      ((EjsSimulationControl) control).getControl("s2").setProperty("value", "");
      return;
    }
    double y_avg = y_accum/trials;
    double y2_avg = y2_accum/trials;
    double variancey = y2_avg-y_avg*y_avg;
    variancey = variancey*trials/(trials-1);
    if(!xDistribution.equals("Lorentz")) {
      ((EjsSimulationControl) control).getControl("devX2").setProperty("value", ControlUtils.f4(variancex));
    }  
    ((EjsSimulationControl) control).getControl("meanY").setProperty("value", ControlUtils.f3(y_avg));
    ((EjsSimulationControl) control).getControl("s2").setProperty("value", ControlUtils.f4(variancey));
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final CentralWRApp app = new CentralWRApp();
    new CentralAppControl(app, app.frame, args);
    app.customize();
  }

}

/**
 * A custom user interface for CentralWRApp.
 * @author Wolfgang Christian
 */
class CentralAppControl extends EjsSimulationControl {
  CentralAppControl(CentralApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Panel", "name=parameterPanel;parent=controlPanel;position=right;layout=flow");
    add("ComboBox", "parent=parameterPanel;action=setDistribution();variable=distribution").setProperty("options",
        "uniform;exponential;Lorentz");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "parent=nPanel;text= N=;tooltip=Number of particles.;horizontalAlignment=right");
    add("NumberField","parent=nPanel;tooltip=Number of particles.;variable=N;format=000;size=45,23;action=model.setParameter()");
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <x>=;tooltip=Mean of x;horizontalAlignment=right");
    add("TextField","parent=outPanel;tooltip=Mean of x.;format=0.00;size=55,23;editable=false;enabled=false;name=meanX");
    add("Label", "parent=outPanel;text=  <(\u03c3x)\u00b2>=;tooltip=Variance of x squared.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Variance of x squared.;format=0.00;size=55,23;editable=false;enabled=false;name=devX2");
    add("Label", "parent=outPanel;text=  <y>=;tooltip=Mean of y;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Mean of y.;format=0.00;size=55,23;editable=false;enabled=false;name=meanY");
    add("Label", "parent=outPanel;text= s\u00b2=;tooltip=Sample variance.;horizontalAlignment=right");
    add("TextField", "parent=outPanel;tooltip=Sample variance.;format=0.00;size=65,23;editable=false;enabled=false;name=s2");
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
