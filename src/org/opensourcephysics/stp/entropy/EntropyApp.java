/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.entropy;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.InteractiveMouseHandler;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.stp.util.MyMath;

/**
 * put your documentation comment here
 * @author Hui Wang
 * @author Joshua Gould
 * @author Peter Sibley
 * @created January 28, 2002
 */
public class EntropyApp extends AbstractCalculation implements InteractiveMouseHandler {
  boolean showTable = false;
  int E, Na, Nb;
  int cid = 0;
  final static int MAXIMUM_NUMERATOR = 20;
  //TableFrame tableFrame = new TableFrame("table");
  PlotFrame plotFrame = new PlotFrame("E_a", "Entropy", "Entropy");

  /**
   * Constructor EntropyApp
   */
  public EntropyApp() {
    plotFrame.setMarkerColor(0, Color.red);
    plotFrame.setMarkerColor(1, Color.blue);
    plotFrame.setMarkerColor(2, Color.GREEN);
    //tableFrame.setColumnNames(0, "S(E_a)");
    //tableFrame.setColumnNames(0, "Ln of omega_a");
    //tableFrame.setColumnNames(0, "Ln of omega_b");
  }

  public void addPoints() {
    for(int i = 0; i<=E; i++) {
      int Ea = i;
      int Eb = E-Ea;
      double log_omegaA = 0;
      double log_omegaB = 0;
      if((Ea+Na-1)>MAXIMUM_NUMERATOR) {
        log_omegaA = MyMath.stirling(Ea+Na-1)-MyMath.stirling(Ea)-MyMath.stirling(Na-1);
      } else {
        log_omegaA = Math.log(EntropyApp.factorial(Ea+Na-1, Ea, Na-1));
      }
      if((Eb+Nb-1)>MAXIMUM_NUMERATOR) {
        log_omegaB = MyMath.stirling(Eb+Nb-1)-MyMath.stirling(Eb)-MyMath.stirling(Nb-1);
      } else {
        log_omegaB = Math.log(EntropyApp.factorial(Eb+Nb-1, Eb, Nb-1));
      }
      double S_of_Ea = log_omegaA+log_omegaB;
      //tableFrame.appendRow(new double[]{S_of_Ea, log_omegaA, log_omegaB});
      plotFrame.append(0, Ea, S_of_Ea);
      plotFrame.append(1, Ea, log_omegaA);
      plotFrame.append(2, Ea, log_omegaB);
    }
    //  plotFrame.setPreferredMinMaxX(0, E);
    plotFrame.repaint();
  }

  public void getStotal() {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(5);
    Dataset d = plotFrame.getDataset(0);
    double sum = 0.0;
    double sm = d.getYMax();
    for(int i = 0; i<d.getRowCount(); i++) {
      sum += Math.exp(d.getValidYPoints()[i]-sm);
    }
    control.clearMessages();
    control.println("Relative error due to keeping only the largest term\n in the total entropy: "
                    +nf.format(Math.log(sum)/sm));
  }

  public static double factorial(int numerator, int denominator1, int denominator2) {
    if(numerator<Math.max(denominator1, denominator2)) {
      throw new IllegalArgumentException("A must be greater than Math.max(B,C)");
    }
    int maxDenominator = 0;
    int minDenominator = 0;
    if(denominator1>denominator2) {
      maxDenominator = denominator1;
      minDenominator = denominator2;
    } else {
      maxDenominator = denominator2;
      minDenominator = denominator1;
    }
    double total = numerator;
    for(int i = numerator-1; i>maxDenominator; i--) {
      total *= i;
    }
    return total/(MyMath.factorial(minDenominator));
  }

  public void resetCalculation() {
    control.clearMessages();
    plotFrame.clearData();
    plotFrame.repaint();
    //tableFrame.clearData();
    //tableFrame.refreshTable();
    E = 200;
    Na = 50;
    Nb = 50;
    control.setValue("E", E);
    control.setValue("Na", Na);
    control.setValue("Nb", Nb);
    control.println("Blue: S(E_a)");
    control.println("Green: S(E_b)");
    control.println("Red: S_total(E_a)");
    plotFrame.setInteractiveMouseHandler(this);
  }

  public void handleMouseAction(InteractivePanel panel, MouseEvent evt) {
    if(panel.getMouseAction()==InteractivePanel.MOUSE_PRESSED) {
      double s = MouseOnCurve(panel.getMouseX(), panel.getMouseY());
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(3);
      if(s!=Double.MIN_VALUE) {
        String str = "";
        switch(cid) {
           case 0 :
             str = "T = ";
             break;
           case 1 :
             str = "Ta = ";
             plotFrame.setMessage(str+nf.format(s));
             break;
           case 2 :
             str = "Tb = ";
             plotFrame.setMessage(str+nf.format(s));
             break;
        }
      }
    }
    if(panel.getMouseAction()==InteractivePanel.MOUSE_RELEASED) {
      plotFrame.setMessage("");
    }
  }

  public double MouseOnCurve(double x0, double y0) {
    for(int j = 0; j<3; j++) {
      Dataset d = plotFrame.getDataset(j);
      double[][] c = d.getPoints();
      for(int i = 0; i<c.length; i++) {
        double x = c[i][0];
        double y = c[i][1];
        double dx = x-x0;
        double dy = y-y0;
        if((dx*dx+dy*dy<10)&&(i>0)) {
          double slope = c[i][1]-c[i-1][1];
          slope /= c[i][0]-c[i-1][0];
          cid = j;
          if(j==2) {
            slope = -slope;
          }
          return 1.0/slope;
        }
      }
    }
    return Double.MIN_VALUE;
  }

  public void calculate() {
    E = control.getInt("E");
    Na = control.getInt("Na");
    Nb = control.getInt("Nb");
    addPoints();
    getStotal();
    //tableFrame.refreshTable();
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new EntropyApp(), args);
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
