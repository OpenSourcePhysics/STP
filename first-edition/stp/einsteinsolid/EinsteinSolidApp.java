/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.einsteinsolid;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;

/**
 * @author Natali Gulbahce
 * @created Oct 22, 2002 modified Dec 27, 2002 by Joshua Gould modified
 *          dataset.setXYColumnNames(0,"E_a", "P") Jan 29, 2003
 */
public class EinsteinSolidApp extends AbstractCalculation {
  int Ea, Eb, Na, Nb;
  PlotFrame plotFrame = new PlotFrame("Ea", "P(Ea)", "Probability");
  NumberFormat numberFormat;
  int datasetCounter = 0;

  /**
   * Constructor EinsteinSolidApp
   */
  public EinsteinSolidApp() {
    numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(4);
    plotFrame.setAutoclear(false);
  }

  public double binom(int N, int n) {
    double product = 1.0;
    int i = N;
    int j = n;
    while((i>=N-n+1)&&(j>=1)) {
      product *= i;
      product /= j;
      j--;
      i--;
    }
    return product;
  }

  public void addPoints() {
    int E = Ea+Eb;
    double Pa[] = new double[E+1];
    double Pb[] = new double[E+1];
    double possibleStates = 0.0;
    for(int i = 0; i<=E; i++) {
      Pa[i] = binom(i+Na-1, i);
      Pb[i] = binom(E-i+Nb-1, E-i);
      possibleStates += Pa[i]*Pb[i];
    }
    plotFrame.setPreferredMinMaxX(0, E);
    double max = 0.0;
    int maxE = -1;
    double meanEa = 0.0;
    double hottocoldprob = 0.0;
    for(int i = 0; i<=E; ++i) {
      double prob = Pa[i]*Pb[i]/possibleStates;
      if(i<Ea) {
        hottocoldprob += prob;
      }
      if(prob>max) {
        max = prob;
        maxE = i;
      }
      meanEa += i*prob;
      plotFrame.append(datasetCounter, i, prob);
      String[] row = new String[2];
      row[0] = numberFormat.format(i);
      row[1] = numberFormat.format(prob);
    }
    outputResult( maxE, meanEa, hottocoldprob);
  }
  
  void outputResult(double maxE, double meanEa, double hottocoldprob) {
	    control.println("Initial \u03a9a = "+binom(Ea+Na-1, Ea)+"\t \u03a9b = "+binom(Eb+Nb-1, Eb));
	    control.println("Most probable Ea = "+numberFormat.format(maxE));
	    control.println("<Ea> = "+numberFormat.format(meanEa));
	    control.println("P a to b = "+numberFormat.format(hottocoldprob));
	    control.println();  
  }

  public void resetCalculation() {
    plotFrame.removeDatasets();
    datasetCounter = 0;
    Ea = 10;
    Eb = 2;
    Na = 4;
    Nb = 4;
    control.setValue("initial Ea", Ea);
    control.setValue("initial Eb", Eb);
    control.setValue("Na", Na);
    control.setValue("Nb", Nb);
    control.clearMessages();
  }

  public void calculate() {
    Ea = control.getInt("initial Ea");
    Eb = control.getInt("initial Eb");
    Na = control.getInt("Na");
    Nb = control.getInt("Nb");
    addPoints();
    plotFrame.setXYColumnNames(datasetCounter, "E_a", "P_"+datasetCounter);
    datasetCounter++;
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
        EinsteinSolidWRApp app = new EinsteinSolidWRApp();
        EinsteinSolidControl c = new EinsteinSolidControl(app, app.plotFrame, null);
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
    addChildFrame(plotFrame);
  }

  public static void main(String[] args) {
    EinsteinSolidApp app = new EinsteinSolidApp();
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
