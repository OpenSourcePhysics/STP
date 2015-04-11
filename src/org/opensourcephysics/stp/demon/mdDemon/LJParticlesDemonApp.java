/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon.mdDemon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;

/**
 * LJParticlesApp simulates a two-dimensional system of interacting particles
 * via the Lennard-Jones potential.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05, 3/29/05
 */
public class LJParticlesDemonApp extends AbstractSimulation {
  LJParticlesDemon md = new LJParticlesDemon();
  PlotFrame pressureData = new PlotFrame("time", "PA/NkT", "Mean pressure");
  PlotFrame demonPlot = new PlotFrame("E", "ln (P(E))", "Demon energy distribution");
  PlotFrame temperatureData = new PlotFrame("time", "temperature", "Kinetic temperature");
  HistogramFrame xVelocityHistogram = new HistogramFrame("vx", "H(vx)", "Velocity histogram");
  DisplayFrame display = new DisplayFrame("x", "y", "Lennard-Jones system");
  DrawingPanel drawingPanel;

  /**
   * Constructor LJParticlesDemonApp
   */
  public LJParticlesDemonApp() {
    drawingPanel = display.getDrawingPanel();
  }

  /**
   * Initializes the model by reading the number of particles.
   */
  public void initialize() {
	if(control==null) return;
    String number = control.getString("number of particles");
    if(number.equals("64")) {
      md.N = 64;
    } else if(number.equals("144")) {
      md.N = 144;
    } else {
      md.N = 256;
    }
    md.initialKineticEnergy = control.getDouble("initial kinetic energy per particle");
    md.dv = 2.0*control.getDouble("maximum change in velocity component");
    md.L = control.getDouble("L");
    md.initialConfiguration = control.getString("initial configuration");
    md.dt = control.getDouble("\u0394t");
    md.initialize();
    drawingPanel.addDrawable(md);
    drawingPanel.setPreferredMinMax(0, md.L, 0, md.L); // assumes vmax = 2*initalTemp and bin width = Vmax/N
    xVelocityHistogram.setBinWidth(2*md.initialKineticEnergy/md.N);
    xVelocityHistogram.setPreferredMinMaxY(0, 10);
    xVelocityHistogram.setAutoscaleY(true);
    demonPlot.setPreferredMinMaxX(0, 10);
    demonPlot.setPreferredMinMaxY(0, 10);
    demonPlot.setAutoscaleX(true);
    demonPlot.setAutoscaleY(true);
    temperatureData.setPreferredMinMaxX(0, 10);
    temperatureData.setPreferredMinMaxY(0, 10);
    temperatureData.setAutoscaleX(true);
    temperatureData.setAutoscaleY(true);
    pressureData.setAutoscaleX(true);
    pressureData.setAutoscaleY(true);
    pressureData.limitAutoscaleX(0, 10);
    pressureData.limitAutoscaleY(0, 10);
  }

  /**
   * Does a simulation step and appends data to the views.
   */
  public void doStep() {
    md.step(xVelocityHistogram);
    pressureData.append(0, md.t, md.getMeanPressure());
    temperatureData.append(0, md.t, md.getMeanTemperature());
    if(md.steps%10==1) {
      demonPlot.clearData();
      for(int i = 0; i<100; i++) {
        if(md.demonP[i]>0) {
          demonPlot.append(0, i, Math.log(md.demonP[i]));
        }
      }
    }
  }

  /**
   * Prints the LJ model's data after the simulation has stopped.
   */
  public void stopRunning() {
    if((control==null)||(md==null)) {
      return;
    }
    control.println("Density = "+decimalFormat.format(md.rho));
    control.println("Number of time steps = "+md.steps);
    //control.println("Time step dt = "+decimalFormat.format(md.dt));
    control.println("<Kinetic Temperature>= "+decimalFormat.format(md.getMeanTemperature()));
    //control.println("<E> = "+decimalFormat.format(md.getMeanEnergy()));
    //control.println("Heat capacity = "+decimalFormat.format(md.getHeatCapacity()));
    //control.println("<PA/NkT> = "+decimalFormat.format(md.getMeanPressure()));
    control.println("Mean Demon Energy = "+decimalFormat.format(md.demonEnergyAccumulator/md.steps));
    demonPlot.clearData();
    for(int i = 0; i<100; i++) {
      if(md.demonP[i]>0) {
        demonPlot.append(0, i, Math.log(md.demonP[i]));
      }
    }
    //demonPlot.render();
  }

  /**
   * Reads adjustable parameters before the program starts running.
   */
  public void startRunning() {
    md.dt = control.getDouble("\u0394t");
    double L = control.getDouble("L");
    if((L!=md.L)) {
      md.L = L;
      md.computeAcceleration();
      drawingPanel.setPreferredMinMax(0, L, 0, L);
      resetData();
    }
  }

  /**
   * Resets the LJ model to its default state.
   */
  public void reset() {
    OSPCombo combo = new OSPCombo(new String[] {"64", "144", "256"}, 0); // second argument is default
    control.setValue("number of particles", combo);
    control.setAdjustableValue("L", 30);
    control.setValue("initial kinetic energy per particle", 1.0);
    control.setAdjustableValue("\u0394t", 0.01);
    OSPCombo combo2 = new OSPCombo(new String[] {"rectangular", "triangular"}, 0); // second argument is default
    control.setValue("initial configuration", combo2);
    control.setValue("maximum change in velocity component", 0.2);
    enableStepsPerDisplay(true);
    super.setStepsPerDisplay(10);       // draw configurations every 10 steps
    drawingPanel.setSquareAspect(true); // so particles will appear as circular disks
    delayTime = 0;
    resetData();
  }

  /**
   * Resets the LJ model and the data graphs.
   *
   * This method is invoked using a custom button.
   */
  public void resetData() {
    md.resetAverages();
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
  }

  /**
   * Switch to the WRApp user interface.
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
        LJParticlesDemonWRApp app = new LJParticlesDemonWRApp();
        LJParticlesDemonAppControl c = new LJParticlesDemonAppControl(app, app.display, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        c.resetSimulation();
        System.gc();
        OSPRuntime.disableAllDrawing = false;
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  void customize() {
    OSPFrame f = getMainFrame();
    if((f==null)||!f.isDisplayable()) {
      return;
    }
    JMenu menu = f.getMenu("Display");
    JMenuItem item = new JMenuItem("Switch GUI");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchGUI();
      }

    });
    menu.add(item);
    addChildFrame(display);
    addChildFrame(pressureData);
    addChildFrame(demonPlot);
    addChildFrame(temperatureData);
    addChildFrame(xVelocityHistogram);
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this program.
   *
   * LJParticle data can now be saved using the Save menu item in the control.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new LJParticlesDemonLoader();
  }

  /**
   * Starts the Java application.
   * @param args  command line parameters
   */
  public static void main(String[] args) {
    LJParticlesDemonApp app = new LJParticlesDemonApp();
    SimulationControl control = SimulationControl.createApp(app);
    control.addButton("resetData", "Zero Averages");
    app.customize();
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
