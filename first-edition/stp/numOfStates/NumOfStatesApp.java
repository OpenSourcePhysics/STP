/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.numOfStates;
import org.opensourcephysics.controls.AbstractCalculation;
import org.opensourcephysics.controls.CalculationControl;
import org.opensourcephysics.frames.PlotFrame;

public class NumOfStatesApp extends AbstractCalculation {
  int dim = 2;
  double R = 10;
  PlotFrame plotFrame = new PlotFrame("R", "NumOfStates", "Number Of States");

  /**
   * put your documentation comment here
   *
   * @author Hui Wang
   * @created Oct 24, 2006
   */
  public void resetCalculation() {
    plotFrame.setConnected(1, true);
    plotFrame.setMarkerSize(1, 0);
    plotFrame.clearData();
    plotFrame.repaint();
    control.setValue("dimension", dim);
    control.setValue("R", R);
  }

  public void calculate() {
    dim = control.getInt("dimension");
    R = control.getDouble("R");
    for(int r = 0; r<=R; r++) {
      if(dim==2) {
        plotFrame.append(0, r, countNumOfStates2(r));
        plotFrame.append(1, r, 1.0/4*Math.PI*r*r);
      } else if(dim==3) {
        plotFrame.append(0, r, countNumOfStates3(r));
        plotFrame.append(1, r, 1.0/6*Math.PI*r*r*r);
      } else if(dim==1) {
        plotFrame.append(0, r, countNumOfStates1(r));
        plotFrame.append(1, r, r);
      }
    }
  }

  public int countNumOfStates3(int r) {
    int r2 = r*r;
    int counter = 0;
    int n2;
    for(int nx = 0; nx<=r; nx++) {
      for(int ny = 0; ny<=r; ny++) {
        for(int nz = 0; nz<=r; nz++) {
          n2 = nx*nx+ny*ny+nz*nz;
          if(n2<r2) {
            counter++;
          }
        }
      }
    }
    return counter;
  }

  public int countNumOfStates2(int r) {
    int r2 = r*r;
    int counter = 0;
    int n2;
    for(int nx = 0; nx<=r; nx++) {
      for(int ny = 0; ny<=r; ny++) {
        n2 = nx*nx+ny*ny;
        if(n2<r2) {
          counter++;
        }
      }
    }
    return counter;
  }

  public int countNumOfStates1(int r) {
    int r2 = r*r;
    int counter = 0;
    int n2;
    for(int nx = 0; nx<=r; nx++) {
      n2 = nx*nx;
      if(n2<r2) {
        counter++;
      }
    }
    return counter;
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new NumOfStatesApp(), args);
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
