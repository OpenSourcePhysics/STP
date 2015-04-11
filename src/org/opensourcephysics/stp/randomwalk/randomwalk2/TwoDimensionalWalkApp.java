/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.randomwalk2;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.*;

/**
 * random walk in two dimensions Input Parameters: nwalkers, number of walkers
 * pleft, probability of step to the left pright, probability of step to the
 * right pdown, probability of of step to the down
 *
 * @author Joshua Gould
 * @author Macneil Shonle
 * @author Peter Sibley
 * @author Wolfgang Christian
 */
public class TwoDimensionalWalkApp extends AbstractSimulation implements Drawable {
  int nwalkers, xmax, ymax, xmin, ymin, time = 0;
  int xpositions[], ypositions[];
  double xbar, ybar, x2, y2, r2;
  DisplayFrame displayFrame = new DisplayFrame("Walkers");
  DrawingPanel displayPanel;
  HistogramFrame histogramFrame = new HistogramFrame("r", "H(r)", "H(r) versus r");
  Random random;
  double pRight, pLeft, pDown; // probability of walker moving right, left,
  NumberFormat numberFormat;

  // down

  /**
   * Constructor TwoDimensionalWalkApp
   */
  public TwoDimensionalWalkApp() {
    numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(2);
    displayFrame.addDrawable(this);
    // /displayFrame.setGutters(10, 10, 10, 10);
    displayPanel = displayFrame.getDrawingPanel();
    displayPanel.setPreferredMinMax(-100, 100, -100, 100);
    displayPanel.setAutoscaleX(false);
    displayPanel.setAutoscaleY(false);
    random = new Random();
    histogramFrame.setBinWidth(.01);
    histogramFrame.setAutoscaleX(true);
    histogramFrame.setAutoscaleY(true);
    histogramFrame.limitAutoscaleX(0, 10);
    histogramFrame.limitAutoscaleY(0, 10);
  }

  public boolean isMeasured() {
    return true;
  }

  public double getXMax() {
    return xmax;
  }

  public double getYMax() {
    return ymax;
  }

  public double getXMin() {
    return xmin;
  }

  public double getYMin() {
    return ymin;
  }

  public void initialize() {
    nwalkers = control.getInt("number of walkers");
    pLeft = control.getDouble("p left");
    pRight = control.getDouble("p right");
    pDown = control.getDouble("p down");
    double pSum = pLeft+pRight+pDown;
    if(pSum>1) { // normalization
      pLeft = pLeft/pSum;
      pRight = pRight/pSum;
      pDown = pDown/pSum;
    }
    xbar = ybar = x2 = y2 = r2 = 0.0;
    xpositions = new int[nwalkers];
    ypositions = new int[nwalkers];
    random.setSeed(System.currentTimeMillis());
    time = 0;
    displayPanel.setMessage("time = "+time);
  }

  public void reset() {
    xmax = 0;
    ymax = 0;
    xmin = 0;
    ymin = 0;
    pRight = 0.25;
    pDown = 0.25;
    pLeft = 0.25;
    control.setValue("number of walkers", 1000);
    control.setValue("p left", pLeft);
    control.setValue("p right", pRight);
    control.setValue("p down", pDown);
    histogramFrame.clearData();
    time = 0;
    displayPanel.setMessage("time = "+time);
    enableStepsPerDisplay(true);
  }

  public void draw(DrawingPanel drawingPanel, Graphics g) {
    int size = 4; // size of a walker in pixels
    g.setColor(Color.red);
    for(int i = 0; i<nwalkers; i++) {
      double x = xpositions[i];
      double y = ypositions[i];
      int px = drawingPanel.xToPix(x)-size/2;
      int py = drawingPanel.yToPix(y)-size/2;
      g.fillRect(px, py, size, size); // walkers represented as rectangles
      // with a width of 4 pixels
    }
  }

  // Move each walker left, right, up, or down
  public void move() {
    time++;
    if(time%10==1)histogramFrame.clearData();
    for(int i = 0; i<nwalkers; i++) {
      double r = random.nextDouble();
      if(r<=pRight) {
        xpositions[i] = xpositions[i]+1;
      } else if(r<pRight+pLeft) {
        xpositions[i] = xpositions[i]-1;
      } else if(r<pRight+pLeft+pDown) {
        ypositions[i] = ypositions[i]-1;
      } else {
        ypositions[i] = ypositions[i]+1;
      }
      if(time%10==1)histogramFrame.append(Math.sqrt(xpositions[i]*xpositions[i]+ypositions[i]*ypositions[i]));
      xmax = Math.max(xpositions[i], xmax);
      ymax = Math.max(ypositions[i], ymax);
      xmin = Math.min(xpositions[i], xmin);
      ymin = Math.min(ypositions[i], ymin);
    }
  }

  public void getAverages() {
    xbar = ybar = x2 = y2 = r2 = 0.0;
    for(int i = 0; i<nwalkers; i++) {
      xbar += xpositions[i];
      ybar += ypositions[i];
      x2 += xpositions[i]*xpositions[i];
      y2 += ypositions[i]*ypositions[i];
      r2 += xpositions[i]*xpositions[i]+ypositions[i]*ypositions[i];
    }
    xbar /= nwalkers;
    ybar /= nwalkers;
    x2 /= nwalkers;
    y2 /= nwalkers;
    r2 /= nwalkers;
  }

  public void stopRunning() {
    control.clearMessages();
    control.println("time = "+time);
    control.println("<x> = "+ControlUtils.f2(xbar)+"\t\t<y> = "+ControlUtils.f2(ybar));
    control.println("<x\u00b2> = "+ControlUtils.f2(x2)+"\t\t<y\u00b2> = "+ControlUtils.f2(y2));
    control.println("<r\u00b2> = "+ControlUtils.f2(r2));
  }

  protected void doStep() {
    move();
    getAverages();
    displayPanel.setMessage("time = "+time);
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
        TwoDimensionalWalkWRApp app = new TwoDimensionalWalkWRApp();
        TwoDimensionalWalkWRAppControl c = new TwoDimensionalWalkWRAppControl(app, app.displayFrame, null);
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
    addChildFrame(histogramFrame);
  }

  public static void main(String[] args) {
    TwoDimensionalWalkApp app = new TwoDimensionalWalkApp();
    SimulationControl.createApp(app, args);
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
