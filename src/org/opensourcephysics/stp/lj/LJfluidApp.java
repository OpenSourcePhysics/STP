/*

 * Open Source Physics software is free software as described near the bottom of this code file.

 *

 * For additional information and documentation on Open Source Physics please see:

 * <http://www.opensourcephysics.org/>

 */

package org.opensourcephysics.stp.lj;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.stp.util.Rdf;

/**
 * LJfluidApp simulates a two-dimensional system of interacting particles
 * via the Lennard-Jones potential.
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05, 3/29/05, 8/14/08
 */
public class LJfluidApp extends AbstractSimulation {
  LJfluid md = new LJfluid();
  PlotFrame pressureData = new PlotFrame("time", "PA/NkT", "Mean pressure");
  PlotFrame temperatureData = new PlotFrame("time", "temperature", "Temperature");
  HistogramFrame xVelocityHistogram = new HistogramFrame("vx", "H(vx)", "Velocity histogram");
  DisplayFrame displayFrame= new DisplayFrame("x", "y", "Lennard-Jones system");
  DrawingPanel displayPanel;
  PlotFrame grFrame = new PlotFrame("r", "g(r)", "Radial distribution function");
  Rdf gr = new Rdf();
  public double x[], y[];
  
  public LJfluidApp(){
	  displayPanel=displayFrame.getDrawingPanel();
  }

  /**
   * Initializes the model by reading the number of particles.
   */
  public void initialize() {
    String number = control.getString("number of particles");
    if(number=="64") {
      md.N = 64;
      md.nx = 8;
      md.ny = 8;
    } else if(number=="144") {
      md.N = 144;
      md.nx = 12;
      md.ny = 12;
    } else {
      md.N = 256;
      md.nx = 16;
      md.ny = 16;
    }
    x = new double[md.N];
    y = new double[md.N];
    md.initialKineticEnergy = control.getDouble("initial KE per particle");
    md.Lx = control.getDouble("L");
    md.Ly = control.getDouble("L");
    md.initialConfiguration = "rectangular"; // control.getString("initial configuration");"
    md.dt = control.getDouble("\u0394t");
    md.initialize();
    displayPanel.addDrawable(md);
    displayPanel.setPreferredMinMax(0, md.Lx, 0, md.Ly); // assumes vmax = 2*initalTemp and bin width = Vmax/N
    xVelocityHistogram.setBinWidth(2*md.initialKineticEnergy/md.N);
    grFrame.setPreferredMinMaxX(0, 10);
    grFrame.setAutoscaleX(true);
    grFrame.setPreferredMinMaxY(0, 10);
    grFrame.setAutoscaleY(true);
    gr.initialize(md.Lx, md.Lx, 0.1);
  }

  /**
   * Does a simulation step and appends data to the views.
   */
  public void doStep() {
    md.quench(control.getDouble("velocity rescaling"));
    double temperature = 0;
    md.step(xVelocityHistogram);
    for(int i = 0; i<md.N; i++) {
      x[i] = md.state[4*i];
      y[i] = md.state[4*i+2];
      temperature += md.state[4*i+1]*md.state[4*i+1]+md.state[4*i+3]*md.state[4*i+3];
    }
    gr.append(x, y);
    temperature /= 2.0*md.N;
    temperatureData.append(0, md.t, temperature);
    pressureData.append(0, md.t, md.getMeanPressure());
    gr.normalize();
    grFrame.clearData();
    grFrame.append(0, gr.rx, gr.ngr);
  }

  /**

   * Prints the LJ model's data after the simulation has stopped.

   */
  public void stop() {
    control.println("Density = "+decimalFormat.format(md.rho));
    control.println("Number of time steps = "+md.steps);
    control.println("Time step dt = "+decimalFormat.format(md.dt));
    control.println("<T>= "+decimalFormat.format(md.getMeanTemperature()));
    control.println("<E> = "+decimalFormat.format(md.getMeanEnergy()));
    control.println("Heat capacity = "+decimalFormat.format(md.getHeatCapacity()));
    control.println("<PA/NkT> = "+decimalFormat.format(md.getMeanPressure()));
  }

  /**

   * Reads adjustable parameters before the program starts running.

   */
  public void startRunning() {
    md.dt = control.getDouble("\u0394t");
    double Lx = control.getDouble("L");
    double Ly = control.getDouble("L");
    if((Lx!=md.Lx)||(Ly!=md.Ly)) {
      md.Lx = Lx;
      md.Ly = Ly;
      md.computeAcceleration();
      displayPanel.setPreferredMinMax(0, Lx, 0, Ly);
      resetData();
    }
  }

  /**

   * Resets the LJ model to its default state.

   */
  public void reset() {
    OSPCombo combo = new OSPCombo(new String[] {"64", "144", "256"}, 0); // first argument is default
    control.setValue("number of particles", combo);
    control.setAdjustableValue("L", 20.0);
    control.setValue("initial KE per particle", 1.0);
    control.setAdjustableValue("\u0394t", 0.01);
    //control.setValue("initial configuration", "rectangular");
    control.setAdjustableValue("velocity rescaling", 1.0);
    enableStepsPerDisplay(true);
    super.setStepsPerDisplay(10);  // draw configurations every 10 steps
    displayPanel.setSquareAspect(true); // so particles will appear as circular disks
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

   * Returns an XML.ObjectLoader to save and load data for this program.

   *

   * LJParticle data can now be saved using the Save menu item in the control.

   *

   * @return the object loader

   */

  /**

   * Starts the Java application.

   * @param args  command line parameters

   */

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
        LJfluidWRApp app = new LJfluidWRApp();
        LJfluidControl c = new LJfluidControl(app, app.displayFrame, null);
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
    addChildFrame(displayFrame);
    addChildFrame(xVelocityHistogram);
    addChildFrame(grFrame);
    addChildFrame(pressureData);
    addChildFrame(temperatureData);
  }

  public static void main(String[] args) {
    LJfluidApp app = new LJfluidApp();
    SimulationControl control = SimulationControl.createApp(app, args);
    control.addButton("resetData", "Reset Data");
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
