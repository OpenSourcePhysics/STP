/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.einsteinsolid;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;

/**
 * @author Natali Gulbahce
 * @created Oct 22, 2002 modified Dec 27, 2002 by Joshua Gould modified
 *          dataset.setXYColumnNames(0,"E_a", "P(E_a)") Jan 29, 2003
 */
public class EinsteinSolid extends AbstractCalculation {
  int Ea, Eb, Na, Nb;
  PlotFrame plotFrame = new PlotFrame("Ea", "P(Ea)", "Probability");
  TableFrame tableFrame = new TableFrame("Table");
  NumberFormat numberFormat;

  /**
   * Constructor EinsteinSolid
   */
  public EinsteinSolid() {
    numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(4);
    plotFrame.setXYColumnNames(0, "E_a", "P");
    tableFrame.setColumnNames(0, "E_a");
    tableFrame.setColumnNames(1, "P");
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
    plotFrame.repaint();
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
      plotFrame.append(0, i, prob);
      String[] row = new String[2];
      row[0] = numberFormat.format(i);
      row[1] = numberFormat.format(prob);
      tableFrame.appendRow(row);
    }
    control.println("Most probable Ea = "+numberFormat.format(maxE));
    control.println("Mean Ea = "+numberFormat.format(meanEa));
    control.println("Hot to cold probability = "+numberFormat.format(hottocoldprob));
  }

  public void reset() {
    plotFrame.clearData();
    tableFrame.clearData();
    control.clearMessages();
    // datasetIndex = -1;
    tableFrame.refreshTable();
    plotFrame.repaint();
    Ea = 10;
    Eb = 2;
    Na = 4;
    Nb = 4;
    control.setValue("Ea", Ea);
    control.setValue("Eb", Eb);
    control.setValue("Na", Na);
    control.setValue("Nb", Nb);
  }

  public void calculate() {
    Ea = control.getInt("Ea");
    Eb = control.getInt("Eb");
    Na = control.getInt("Na");
    Nb = control.getInt("Nb");
    addPoints();
    tableFrame.refreshTable();
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new EinsteinSolid());
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
