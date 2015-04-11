/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.secondvirial;
import org.opensourcephysics.controls.AbstractCalculation;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.Function;
import org.opensourcephysics.numerics.Integral;

public class SecondVirialApp extends AbstractCalculation implements Function {
  PlotFrame pf = new PlotFrame("T", "B_2", "Second Virial Coefficient");
  double Tmin, Tmax, T;
  int numberOfPoints = 400;

  public static void main(String[] args) {
    CalculationControl.createApp(new SecondVirialApp(), args);
  }

  public void reset() {
    control.setValue("Tmin", 0.5);
    control.setValue("Tmax", 5);
    pf.clearData();
  }

  public void initialize() {
    Tmin = control.getDouble("Tmin");
    Tmax = control.getDouble("Tmax");
    pf.clearData();
  }

  public void calculate() {
    double x_low = 0.000001, x_high = 10000, tolerance = 0.0001;
    initialize();
    double deltaT = (Tmax-Tmin)/(double) numberOfPoints;
    for(double t = Tmin; t<Tmax; t += deltaT) {
      T = t;
      double B2 = Integral.simpson(this, x_low, x_high, 20, tolerance);
      pf.append(0, T, B2);
    }
  }

  public double evaluate(double x) {
    double r2 = x*x;
    double oneOverR2 = 1.0/r2;
    double oneOverR6 = oneOverR2*oneOverR2*oneOverR2;
    double z = Math.exp(-(1/T)*4.0*(oneOverR6*oneOverR6-oneOverR6));
    return 2*Math.PI*(1-z);
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
