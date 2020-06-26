/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.demon.mdDemon;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;

/**
 * LJParticlesLoader implements the ObjectLoader interface to load and store data.
 *
 * @author Wolfgang Christian, Jan Tobochnik, Harvey Gould
 * @version 1.0 revised 03/28/05
 */
public class LJParticlesDemonLoader implements XML.ObjectLoader {
  /**
   * Creates a LJParticlesApp object.
   *
   * @param control the xml control
   * @return a new object
   */
  public Object createObject(XMLControl element) {
    return new LJParticlesDemonApp();
  }

  /**
   * Saves data from the LJParticlesApp model into the control.
   *
   * @param element XMLControl
   * @param obj Object
   */
  public void saveObject(XMLControl control, Object obj) {
    LJParticlesDemonApp model = (LJParticlesDemonApp) obj;
    control.setValue("initial_configuration", model.md.initialConfiguration);
    control.setValue("state", model.md.state);
  }

  /**
   * Loads data from the control into the LJParticlesApp model.
   *
   * @param element XMLControl
   * @param obj Object
   * @return Object
   */
  public Object loadObject(XMLControl control, Object obj) {
    // GUI has been loaded with the saved values; now restore the LJ state
    LJParticlesDemonApp model = (LJParticlesDemonApp) obj;
    model.initialize(); // reads values from the GUI into the LJ model
    model.md.initialConfiguration = control.getString("initial_configuration");
    model.md.state = (double[]) control.getObject("state");
    if(model.md.state!=null) {
      int N = (model.md.state.length-1)/4;
      model.md.ax = new double[N];
      model.md.ay = new double[N];
      model.md.computeAcceleration();
      model.md.resetAverages();
    }
    GUIUtils.clearDrawingFrameData(false); // clears old data from the plot frames
    return obj;
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
