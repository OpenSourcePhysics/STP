/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.Ch04;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/*
 *
 * @author Jan Tobochnik  revised from Hui Wang version
 * @created June 16, 2020 
 */
public class MCEinsteinsolidApp extends AbstractSimulation {
  int N, acceptedMoves = 0, mcs = 0;
  int[] e;
  double systemEnergy, systemEnergyAccumulator = 0;
  double systemEnergyAccumulator2 = 0;
  double nCalc = 0;
  double[] Ea, Ca, Ta; // array for curves
  int npoints = 0;
  static final int nmaxTrials = 1000;
  static final int nequil = 100;
  double T;            // system temperature
  DecimalFormat decimalFormat = new DecimalFormat("#.###");
  Random rnd = new Random();
  PlotFrame plotFrame = new PlotFrame("mcs", "E", "E versus time");
  PlotFrame ETFrame = new PlotFrame("T", "<E/N>", "<E/N> .vs. T");
  PlotFrame CTFrame = new PlotFrame("T", "C", "Specific Heat .vs. T");
  
  /**
   * Constructor EinsteinsolidMCApp
   */
  public MCEinsteinsolidApp() {
  	OSPRuntime.setAppClass(this);
  }
 
  public void doStep() {
    T = control.getDouble("T");
    for(int i = 0; i < N;i++){
     	int particleIndex = (int) (Math.random()*N);
    	int de = 2*rnd.nextInt(2)-1; // get -1 or 1
    	if(((de==1)&&(rnd.nextDouble()<Math.exp(-de/T)))||((de==-1)&&(e[particleIndex]>0))) {
          acceptedMoves++;
          e[particleIndex] += de;
          systemEnergy += de;
    	}
        systemEnergyAccumulator += systemEnergy;
        systemEnergyAccumulator2 += systemEnergy*systemEnergy;
        nCalc++;
    }
    mcs++;
    plotFrame.append(0, mcs, systemEnergy/N);
  }

  public void stopRunning() {
    control.println("mcs = "+decimalFormat.format(mcs));
    control.println("<E> = "+decimalFormat.format(meanE()));
    control.println("C = "+decimalFormat.format(heatCapacity()));
    control.println("acceptance ratio = "+decimalFormat.format(acceptedMoves/nCalc));
    control.println();
  }

  public void initialize() {
    N = control.getInt("N");
    T = control.getDouble("T");
    e = new int[N];
    for(int i = 0; i<N; i++) {
      e[i] = 1;
    }
    systemEnergy = N;
    mcs = 0;
    acceptedMoves = 0;
    plotFrame.setPreferredMinMax(0, 10, 0, 10);
    plotFrame.setAutoscaleX(true);
    plotFrame.setAutoscaleY(true);
    ETFrame.setPreferredMinMaxX(0, 10);
    CTFrame.setPreferredMinMaxX(0, 10);
    ETFrame.setAutoscaleX(true);
    CTFrame.setAutoscaleX(true);
    ETFrame.setPreferredMinMaxY(0, 10);
    CTFrame.setPreferredMinMaxY(0, 10);
    ETFrame.setAutoscaleY(true);
    CTFrame.setAutoscaleY(true);
  }


  public void acceptResults() {
       ETFrame.append(0, T, meanE());
       CTFrame.append(0, T, heatCapacity());
       ETFrame.render();
       CTFrame.render();
       resetData();
  }
  
  public void reset(){
		control.setValue("N", 20);
		control.setAdjustableValue("T", 2.0);
	    enableStepsPerDisplay(true);
  }
  
  public void resetData(){
	    mcs = 0;
	    acceptedMoves = 0;
	    plotFrame.clearData();
	    plotFrame.repaint();  
	    systemEnergyAccumulator = 0;
	    systemEnergyAccumulator2 = 0;
	    nCalc = 0;
	    plotFrame.clearData();
  }

  public double meanE() {
    double norm = 1.0/(nCalc*N);
    return systemEnergyAccumulator*norm;
  }

  public double heatCapacity() {
    double e1 = systemEnergyAccumulator/nCalc;
    double e2 = e1*e1;
    return(systemEnergyAccumulator2/nCalc-e2)/(N*T*T);
  }

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
        EinsteinsolidMCWRApp app = new EinsteinsolidMCWRApp();
        EinsteinsolidMCControl c = new EinsteinsolidMCControl(app, app.plotFrame, null);
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
    addChildFrame(plotFrame);
    addChildFrame(ETFrame);
    addChildFrame(CTFrame);
  }
*/
  public static void main(String[] args) {
    MCEinsteinsolidApp application = new MCEinsteinsolidApp();
    SimulationControl control = SimulationControl.createApp(application, args);
    control.addButton("acceptResults", "Accept E and C");
    control.addButton("resetData", "Reset Data");    
  //  application.customize();
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
