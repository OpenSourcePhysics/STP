/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */
package org.opensourcephysics.stp.entropy;
import org.opensourcephysics.controls.AbstractCalculation;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.frames.PlotFrame;

/**
 * @author Jan Tobochnik
 * @created June 24, 2008
 * allows number of oscillators to vary, but energy is fixed
 */
public class EinsteinSolidChemicalPotentialApp extends AbstractCalculation {
  int Ea, Eb, Na, Nb, E, N;
  PlotFrame plotFrame = new PlotFrame("Na", "Total Number of States", "");

  /**
   * Constructor EinsteinSolidChemicalPotentialApp
   */
  public EinsteinSolidChemicalPotentialApp() {
    plotFrame.setXYColumnNames(0, "N_a", "omegaA");
    plotFrame.setXYColumnNames(1, "N_a", "log omegaA");
    plotFrame.setXYColumnNames(2, "N_a", "muA/kT");
    plotFrame.setXYColumnNames(3, "N_a", "Nb");
    plotFrame.setXYColumnNames(4, "N_a", "omegaB");
    plotFrame.setXYColumnNames(5, "N_a", "log omegaA");
    plotFrame.setXYColumnNames(6, "N_a", "muB/kT");
    plotFrame.setXYColumnNames(7, "N_a", "# states");
    plotFrame.showDataTable(true);
    for(int i = 0; i<8; i++) {
      if(i != 7) plotFrame.setMarkerSize(i, 0);
    }
    plotFrame.setMarkerSize(7, 2);
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
    //int E = Ea + Eb;
    //plotFrame.repaint();
    int totalStates = 0;
    for(int Na = 1; Na<N; ++Na) {
      int Nb = N-Na;
      double omegaA = binom(Ea+Na-1, Ea);
      double lnOmegaA = Math.log(omegaA);
      double omegaB = binom(Eb+Nb-1, Eb);
      double lnOmegaB = Math.log(omegaB);
      double states = omegaA*omegaB;
      plotFrame.append(0, Na, omegaA);
      plotFrame.append(1, Na, lnOmegaA);
      if((Na>1)&&(Na<N-1)) {
        double muA = -0.5*(Math.log(binom(Ea+Na, Ea))-Math.log(binom(Ea+Na-2, Ea)));
        plotFrame.append(2, Na, muA); // muA = mu_A/kT
      } else {
        plotFrame.append(2, Na, 0);
      }
      plotFrame.append(3, Na, Nb);
      plotFrame.append(4, Na, omegaB);
      plotFrame.append(5, Na, lnOmegaB);
      if((Nb>1)&&(Nb<N-1)) {
        double muB = -0.5*(Math.log(binom(Eb+Nb, Eb))-Math.log(binom(Eb+Nb-2, Eb)));
        plotFrame.append(6, Na, muB); // muB = mu_B/kT
      } else {
         plotFrame.append(6, Na, 0);
      }
      plotFrame.append(7, Na, states);
      totalStates += states;
    }
    System.out.println("total number of states = "+totalStates);
  }

  public void resetCalculation() {
    plotFrame.clearData();
    Ea = 8;
    Eb = 5;
    N = 10;
    control.setValue("Ea", Ea);
    control.setValue("Eb", Eb);
    control.setValue("N", N);
    control.clearMessages();
  }

  public void calculate() {
    plotFrame.clearData();
    Ea = control.getInt("Ea");
    Eb = control.getInt("Eb");
    N = control.getInt("N");
    E = Ea+Eb;
    addPoints();
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new EinsteinSolidChemicalPotentialApp(), args);
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
