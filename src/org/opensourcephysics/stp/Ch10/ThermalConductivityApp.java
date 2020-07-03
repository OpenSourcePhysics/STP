/*

 * Open Source Physics software is free software as described near the bottom of this code file.

 *

 * For additional information and documentation on Open Source Physics please see:

 * <http://www.opensourcephysics.org/>

 */

package org.opensourcephysics.stp.Ch10;
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
 * LJfluidApp simulates a three-dimensional system of interacting particles
 * via the Lennard-Jones potential.
 * calculates thermal conductivity using Florian Muller-Plathe, JCP 106, p. 6082 (1997).
 *
 * @author Jan Tobochnik, Wolfgang Christian, Harvey Gould
 * @version 1.0 revised 03/28/05, 3/29/05, 8/14/08
 */ 
public class ThermalConductivityApp extends AbstractSimulation {
  ThermalConductivity md = new ThermalConductivity();
  PlotFrame pressureData = new PlotFrame("time", "PV/NkT", "Mean pressure");
  PlotFrame temperatureData = new PlotFrame("time", "temperature", "Temperature");
  PlotFrame slabTemperature = new PlotFrame("z (distance from cold slab)", "temperature", "Temperature in each slab");
  PlotFrame slabDensity = new PlotFrame("z (distance from cold slab)", "density", "Number density of each slab");
  double[] Tslab, rhoslab;
  int nTslab[];
  double dTdz;
  boolean equil = true;
  
 
  /**
   * Initializes the model by reading the number of particles.
   */
  public void initialize() {
    md.n = control.getInt("Enter even k where number of particles = 3*k*k*k");
    int n = md.n;
    md.N = 3*n*n*n;
    Tslab = new double[3*n];
    rhoslab = new double[3*n];
    nTslab = new int[3*n];
    md.desiredTemperature = control.getDouble("desired temperature");
    md.rho = control.getDouble("number density");
    md.a = 1.0/Math.pow(md.rho,1.0/3.0);
    md.Lx = n*md.a;
    md.Ly = n*md.a;
    md.Lz = 3*n*md.a;
    md.dt = control.getDouble("time step");
    md.initialize();
    equil = true;
  }
  
  /**

   * Resets the LJ model to its default state.

   */
  public void reset() {
    control.setValue("Enter even k where number of particles = 3*k*k*k", 10);
    control.setValue("desired temperature", 0.7);
    control.setValue("time step", 0.007);
    control.setValue("number density", 0.849);
    control.setAdjustableValue("time steps between swapping hot and cold particles" , 15);
    enableStepsPerDisplay(true);
    super.setStepsPerDisplay(10);  // draw configurations every 10 steps    displayPanel.setSquareAspect(true); // so particles will appear as circular disks
  }

  /**
   * Does a simulation step and appends data to the views.
   */
  public void doStep() {
	md.stepAdd = control.getInt("time steps between swapping hot and cold particles");
	md.step(equil);
	double temperature = 0;
    for(int i = 0; i<md.N; i++) {
       double T = md.state[6*i+1]*md.state[6*i+1]+md.state[6*i+3]*md.state[6*i+3]+md.state[6*i+5]*md.state[6*i+5];
       temperature += T;
       int index = (int)(md.state[6*i+4]/(md.a));
       Tslab[index] += T/3.0;
       rhoslab[index]++;
       nTslab[index]++;
    }
    temperature /= 3*md.N;
    temperatureData.append(0, md.t, temperature);
    pressureData.append(0, md.t, md.getMeanPressure());
    slabTemperature.clearData();
    slabDensity.clearData();
    double zsum = 0;
    double z2sum = 0;
    double ysum = 0;
    double zysum = 0;
    double nsum = 0;
    for(int islab = 1; islab < 3*md.n;islab++){
    	double z = islab*md.a;
    	if(islab > 3*md.n/2)
    		z = md.n*3*md.a - z; 		
    	if(islab != 3*md.n/2){
    		double y = Tslab[islab]/nTslab[islab]; 
      		slabTemperature.append(0,z,y);
      		slabDensity.append(0,z,rhoslab[islab]/(md.steps*md.a*md.Lx*md.Ly));
      		zsum += z;
    		z2sum += z*z;
    		ysum += y;
    		zysum += z*y;
    		nsum++;
    	}
    }
    dTdz = ((zysum/nsum)- (zsum/nsum)*(ysum/nsum))/((z2sum/nsum) - (zsum/nsum)*(zsum/nsum));
 }

  /**

   * Prints the LJ model's data after the simulation has stopped.

   */
  public void stop() {
	control.println("Density = "+decimalFormat.format(md.rho));
	control.println("length scale: 1/density^(1/3) = "+decimalFormat.format(md.a));
    control.println("Number of time steps = "+md.steps);
    control.println("Time step dt = "+decimalFormat.format(md.dt));
    control.println("<T>= "+decimalFormat.format(md.getMeanTemperature()));
    control.println("<E> = "+decimalFormat.format(md.getMeanEnergy()));
 //   control.println("Heat capacity = "+decimalFormat.format(md.getHeatCapacity()));
    control.println("<PA/NkT> = "+decimalFormat.format(md.getMeanPressure()));
    control.println("<dT/dz> = "+decimalFormat.format(dTdz) + " transfer = "+decimalFormat.format(md.transfer));
    control.println("conductivity = "+decimalFormat.format(md.transfer/(2*md.dt*md.steps*md.Lx*md.Ly*dTdz)));
     }

  /**

   * Reads adjustable parameters before the program starts running.

   */
  public void startRunning() {
   }

 

  /**

   * Resets the LJ model and the data graphs.

   *

   * This method is invoked using a custom button.

   */
  public void resetData() {
    md.resetAverages();
    Tslab = new double[3*md.n];
    rhoslab = new double[3*md.n];
    nTslab = new int[3*md.n];
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
  }
  
  public void stopEquilibration(){
	  equil = false;
	  resetData();
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
        LennardJonesWRApp app = new LennardJonesWRApp();
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

  */

  public static void main(String[] args) {
    ThermalConductivityApp app = new ThermalConductivityApp();
    SimulationControl control = SimulationControl.createApp(app, args);
    control.addButton("resetData", "Reset Data");
    control.addButton("stopEquilibration", "Stop Equilibration");
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
