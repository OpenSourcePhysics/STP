/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.binomial;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.stp.util.MyMath;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;

/**
 * put your documentation comment here
 *
 * @author Hui Wang
 * @created Oct 18, 2006
 */
public class BinomialApp extends AbstractCalculation {
  int N;
  double p;
  final static int MAXIMUM_NUMERATOR = 20;
  final static double tol = 1e-100;
  int counter = 0;
  double[] nbar = new double[100];
  PlotFrame plotFrame = new PlotFrame("n", "P", "P(n) versus n");
  PlotFrame plotFrameNorm = new PlotFrame("n/<n>", "P", "P(n) versus n/<n>");
  DatasetManager data = new DatasetManager();

  /**
   * Constructor BinomialApp
   */
  public BinomialApp() {
    plotFrame.setAutoscaleY(true);
    plotFrame.setAutoscaleX(true);
    plotFrameNorm.setAutoscaleY(true);
    plotFrameNorm.setAutoscaleX(true);
  }

  public void addPoints() {
    double log_N = 0;
    double log_n1 = 0, log_n2 = 0;
    boolean stirling = false;
    stirling = N>MAXIMUM_NUMERATOR;
    for(int i = 0; i<=N; i++) {
      if(stirling) {
        log_N = MyMath.stirling(N);
        log_n1 = MyMath.stirling(i);
        log_n2 = MyMath.stirling(N-i);
      } else { // use double to avoid integer overflow
        log_N = Math.log(MyMath.factorial((double) N));
        log_n1 = Math.log(MyMath.factorial((double) i));
        log_n2 = Math.log(MyMath.factorial((double) (N-i)));
      }
      double w = log_N-log_n1-log_n2+i*Math.log(p)+(N-i)*Math.log(1-p);
      double prob = Math.exp(w);
      if(prob>tol) {
        data.append(counter, i, prob);
      }
    }
  }

  public void resetCalculation() {
    counter = 0;
    data.removeDatasets();
    plotFrame.removeDatasets();
    plotFrameNorm.removeDatasets();
    N = 60;
    p = 0.5;
    control.setValue("N", N);
    control.setValue("p", p);
  }

  public void calculate() {
    N = control.getInt("N");
    p = control.getDouble("p");
    addPoints();
    nbar[counter] = N*p;
    plotFrame.clearData();
    plotFrameNorm.clearData();
    //draw all the datasets
    for(int i = 0; i<=counter; i++) {
      double[] x = data.getXPoints(i);
      double[] y = data.getYPoints(i);
      for(int j = 0; j<x.length; j++) {
        plotFrame.append(i, x[j], y[j]);
        plotFrameNorm.append(i, x[j]/nbar[i], y[j]);
      }
    }
    plotFrame.setXYColumnNames(counter, "n", "P_"+counter+"[n]");
    plotFrameNorm.setXYColumnNames(counter, "n_norm", "P_"+counter+"[n]");
    counter++;
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
        BinomialWRApp app = new BinomialWRApp();
        BinomialControl c = new BinomialControl(app, app.plotFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        app.resetCalculation();
        app.calculate();
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
    addChildFrame(plotFrameNorm);
    addChildFrame(plotFrame);
  }

  public static void main(String[] args) {
    BinomialApp app = new BinomialApp();
    CalculationControl.createApp(app, args);
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
