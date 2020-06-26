/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.sensitive;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.DisplayFrame;

public class LJgasApp extends AbstractSimulation {
  LJgas gas;
  DisplayFrame displayFrame = new DisplayFrame("");
  NumberFormat numberformat = NumberFormat.getInstance();

  /**
   * Constructor LJgasApp
   */
  public LJgasApp() {
    gas = new LJgas();
    displayFrame.addDrawable(gas);
    displayFrame.setPreferredMinMax(-0.2*gas.cellLength, 1.2*gas.cellLength, -0.2*gas.cellLength, 1.2*gas.cellLength);
    numberformat.setMaximumIntegerDigits(5);
    numberformat.setMinimumIntegerDigits(1);
    numberformat.setMinimumFractionDigits(1);
  }

  public void initialize() {
    //gas.numberOfParticles = control.getInt("N");
    gas.cellLength = 11.0; //control.getDouble("cell length");
    gas.initialize();
  }

  public void doStep() {
    for(int i = 0; i<20; i++) {
      gas.step(); // advance the solution of the ODE by one step
    }
    displayFrame.setMessage("time = "+numberformat.format(gas.t));
    displayFrame.render();
  }

  public void reset() {
    gas.initialize();
    //control.setValue("N", gas.numberOfParticles);
    //control.setValue("cell length", gas.cellLength);
    control.setValue("perturbation strength", 1.00001);
    displayFrame.setPreferredMinMax(-0.2*gas.cellLength, 1.2*gas.cellLength, -0.2*gas.cellLength, 1.2*gas.cellLength);
    displayFrame.render();
  }

  public void perturb() {
    double a = control.getDouble("perturbation strength");
    gas.perturb(a);
    gas.zeroAverages();
  }

  public void reverse() {
    gas.reverse();
    gas.zeroAverages();
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new LJgasApp(), args);
    control.addButton("perturb", "Perturb");
    control.addButton("reverse", "Reverse");
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
