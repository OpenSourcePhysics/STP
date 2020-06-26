/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon.mdDemon;
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
 * LJParticlesDemonWRApp adds a custom user interface to LJParticlesDemonApp.
 * @author Wolfgang Christian
 */
public class LJParticlesDemonWRApp extends LJParticlesDemonApp {
  /**
   * Constructor LJParticlesDemonWRApp
   */
  public LJParticlesDemonWRApp() {
    pressureData.setKeepHidden(true);
    pressureData.dispose();
    xVelocityHistogram.setKeepHidden(true);
    xVelocityHistogram.dispose();
  }

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
        LJParticlesDemonApp app = new LJParticlesDemonApp();
        SimulationControl c = SimulationControl.createApp(app);
        c.setDefaultCloseOperation(closeOperation);
        c.addButton("resetData", "Reset Data");
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
    if((control==null)||(md==null)) {
      return;
    }
    ((EjsSimulationControl) control).getControl("TNumber").setProperty("value",ControlUtils.f4(md.getMeanTemperature()));
    ((EjsSimulationControl) control).getControl("edNumber").setProperty("value",ControlUtils.f4(md.getMeanEnergy()));
    demonPlot.clearData();
    for(int i = 0; i<100; i++) {
      if((md.demonP!=null)&&(md.demonP[i]>0)) {
        demonPlot.append(0, i, Math.log(md.demonP[i]));
      }
    }
    //demonPlot.render();
  }

  public void setParameter() {
    resetData();
    initialize();
    stopRunning();
    GUIUtils.repaintOSPFrames();
  }

  /**
   * Starts the program.
   *
   * @param args input parameters such as an xml data file
   */
  public static void main(String[] args) {
    final LJParticlesDemonWRApp app = new LJParticlesDemonWRApp();
    new LJParticlesDemonAppControl(app, app.display, args);
    app.customize();
  }

}

/**
 * A custom user interface for DemonWRApp.
 * @author Wolfgang Christian
 */
class LJParticlesDemonAppControl extends EjsSimulationControl {
  LJParticlesDemonAppControl(LJParticlesDemonWRApp model, DrawingFrame frame, String[] args) {
    super(model, frame, args);
  }

  /**
   * Customize the EjsSimulationControl.
   */
  protected void customize() {
    add("Button", "parent=buttonPanel; text=Reset Data;tooltip=Resets the data.;action=model.resetData()");
    add("Panel", "name=parameterPanel;parent=controlPanel;position=center;layout=flow");
    add("Panel", "name=nPanel;parent=parameterPanel;layout=flow");
    add("Label", "parent=nPanel;text= N=;tooltip=Number of particles.;horizontalAlignment=right");
    add("ComboBox", "parent=nPanel;action=setParameter();tooltip=Number of particles.;variable=number of particles").setProperty("options","64;144;256");
    add("Panel", "name=LPanel;parent=parameterPanel;layout=flow");
    add("Label", "parent=LPanel;text= L=;tooltip=Cell size.;horizontalAlignment=right");
    add("NumberField","parent=LPanel;tooltip=Cell size.;variable=L;format=000;size=45,23;action=model.setParameter()");
    
    JPanel panel = (JPanel) add("Panel", "name=outPanel;parent=controlFrame;layout=flow;position=north").getComponent();
    panel.setBorder(new EtchedBorder());
    add("Label", "parent=outPanel;text=  <T>=;tooltip=Mean temperature.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Mean temperature.;format=0.00;size=55,23;editable=false;enabled=false;name=TNumber");
    add("Label", "parent=outPanel;text=  <Ed>=;tooltip=Demon energy.;horizontalAlignment=right");
    add("TextField",
        "parent=outPanel;tooltip=Demon energy;format=0.00;size=55,23;editable=false;enabled=false;name=edNumber");
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
