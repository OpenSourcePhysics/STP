/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/**
 *  computes the area of a function using monte carlo estimation
 *
 * @author    Joshua Gould
 */
package org.opensourcephysics.stp.estimation;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.*;
import org.opensourcephysics.controls.*;
import java.util.Random;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class MonteCarloEstimationApp extends AbstractCalculation {
  Random rng = new Random();
  int n; // number of trials
  long seed;
  Function function = new MyFunction();
  PlotFrame plotFrame = new PlotFrame("x", "y", "Monte Carlo Estimation hits / misses");
  FunctionDrawer functionDrawer;
  double ymax = 1, a = 0, b = 1;

  /**
   * Constructor MonteCarloEstimationApp
   */
  public MonteCarloEstimationApp() {
    functionDrawer = new FunctionDrawer(function);
    functionDrawer.initialize(0, 1, 1000, false);
    plotFrame.setMarkerColor(0, Color.blue);
    plotFrame.setMarkerColor(1, Color.red);
    plotFrame.addDrawable(functionDrawer);
  }

  public void calculate() {
    plotFrame.clearData();
    n = control.getInt("n");
    try{
    seed = Long.parseLong(control.getString("seed"));
    }catch(Exception ex){
    	control.setValue("seed", String.valueOf(System.currentTimeMillis()));
    	seed = Long.parseLong(control.getString("seed"));	
    }
    rng.setSeed(seed);
    long hits = 0;
    double x;
    double y;
    for(int i = 0; i<n; i++) {
      x = rng.nextDouble()*(b-a); // nextDouble returns a random
      // double between 0 (inclusive) and
      // 1 (exclusive)
      y = rng.nextDouble()*ymax;
      if(y<=function.evaluate(x)) {
        hits++;
        plotFrame.append(0, x, y);
      } else {
        plotFrame.append(1, x, y);
      }
    }
    displayArea(hits);
  }

  public void displayArea(long hits) {
    double estimatedArea = (hits*(b-a)*ymax)/n;
    control.println("estimated area = "+estimatedArea);
  }

  public void resetCalculation() {
    control.setValue("n", 1000);
    control.setValue("seed", String.valueOf(System.currentTimeMillis()));
    plotFrame.clearData();
    control.clearMessages();
    calculate();
  }

  /**
   * Switch to the WRApp user interface.
   */
  public void switchGUI() {
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
        MonteCarloEstimationWRApp app = new MonteCarloEstimationWRApp();
        MonteCarloEstimationControl c = new MonteCarloEstimationControl(app, app.plotFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
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
  }

  public static void main(String[] args) {
	MonteCarloEstimationApp app=new MonteCarloEstimationApp();
    CalculationControl.createApp(app, args);
    app.customize();
  }

  private static class MyFunction implements Function {
    public double evaluate(double x) {
      return 1-x*x<0?0:Math.sqrt(1-x*x);
    }

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
