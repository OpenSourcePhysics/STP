/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.extremeValue;
import java.awt.Color;
import java.util.Arrays;
import java.util.Random;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.HistogramDataset;
import org.opensourcephysics.frames.PlotFrame;

public class ExtremeValueApp extends AbstractCalculation {
  Random random;
  PlotFrame distributions = new PlotFrame("x", "H(x)", "Histogram");
  Dataset gaussian = new HistogramDataset(0, 1, 0.001);
  Dataset LEV = new HistogramDataset(0, 1, 0.001);
  Dataset GEV = new HistogramDataset(0, 1, 0.001);
  int numberOfSamples;
  int numberOfTrials;

  /**
   * Constructor ExtremeValueApp
   */
  public ExtremeValueApp() {
    this.random = new Random(System.currentTimeMillis());
    LEV.setMarkerColor(Color.red);
    GEV.setMarkerColor(Color.green);
    gaussian.setMarkerColor(Color.blue);
  }

  public void calculate() {
    this.numberOfSamples = control.getInt("number of samples");
    this.numberOfTrials = control.getInt("number of trials");
    double sample[] = new double[numberOfSamples];
    double sum = 0;
    clearData();
    for(int i = 0; i<this.numberOfTrials; i++) {
      for(int j = 0; j<this.numberOfSamples; j++) {
        sample[j] = random.nextDouble();
        sum += sample[j];
      }
      Arrays.sort(sample);
      LEV.append(sample[0], 1);
      GEV.append(sample[numberOfSamples-1], 1);
      sum = sum/numberOfSamples;
      gaussian.append(sum, 1);
    }
    //      distributions.append(0, LEV.getXPoints(), LEV.getYPoints());
    //      distributions.append(1, GEV.getXPoints(), GEV.getYPoints());
    distributions.addDrawable(LEV);
    distributions.addDrawable(GEV);
    distributions.addDrawable(gaussian);
  }

  public void reset() {
    control.setValue("number of samples", 10);
    control.setValue("number of trials", 1000000);
    control.clearMessages();
    clearData();
  }

  public void clearData() {
    LEV.clear();
    GEV.clear();
    gaussian.clear();
    distributions.clearData();
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new ExtremeValueApp(), args);
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
