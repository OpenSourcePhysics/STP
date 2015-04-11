/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.oscillatornh;

/**
 * The Nose-Hoover constant temperature simulation of an harmonic oscillator.
 *
 * @author Hui Wang
 * @version 1.0
 * @created 12/21/2006
 */
import java.awt.Color;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.PlotFrame;

public class OscillatorApp extends AbstractSimulation {
  Oscillator osci = new Oscillator();
  PlotFrame phaseFrame = new PlotFrame("q", "p", "Nose-Hoover harmonic oscillator");
  PlotFrame energyFrame = new PlotFrame("time", "Energy", "Energy versus time");
  double mcsPerDisplay;
  
  public OscillatorApp() {
	  phaseFrame.setMarkerColor(0,Color.BLUE);
	  energyFrame.setMarkerColor(0,Color.RED);
  }

  public void doStep() {
    osci.step(0.05);
    phaseFrame.append(0, osci.x, osci.vx);
    energyFrame.append(0, osci.t, osci.Et);
  }

  public void initialize() {
    osci.x = control.getDouble("x");
    osci.vx = control.getDouble("vx");
    osci.Q = control.getDouble("Q");
    energyFrame.setAutoscaleX(true);
    this.delayTime = 0;
    energyFrame.setPreferredMinMax(0, 10, 0, 10);
    energyFrame.setAutoscaleX(true);
    energyFrame.setAutoscaleY(true);
  }

  public void reset() {
    control.setAdjustableValue("Q", 1.0);
    control.setAdjustableValue("x", 1.0);
    control.setAdjustableValue("vx", 1.0);
    energyFrame.clearData();
  }

  public static void main(String[] args) {
    SimulationControl.createApp(new OscillatorApp());
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
