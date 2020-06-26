/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp;
import org.opensourcephysics.controls.*;
import java.io.BufferedInputStream;
import java.util.Vector;

public class CmdLineTestApp extends AbstractCalculation {
  public void reset() {
    control.setValue("arg 0", "java");
    control.setValue("arg 1", "-classpath");
    control.setValue("arg 2", "osp_stp.jar");
    control.setValue("arg 3", "org.opensourcephysics.stp.approach.LJgasApp");
    control.setValue("arg 4", "ljgas_data.xml");
  }

  public void calculate() {
    final Vector cmd = new Vector();
    String arg = control.getString("arg 0").trim();
    if(arg.length()>0) {
      cmd.add(arg);
    }
    arg = control.getString("arg 1").trim();
    if(arg.length()>0) {
      cmd.add(arg);
    }
    arg = control.getString("arg 2").trim();
    if(arg.length()>0) {
      cmd.add(arg);
    }
    arg = control.getString("arg 3").trim();
    if(arg.length()>0) {
      cmd.add(arg);
    }
    arg = control.getString("arg 4").trim();
    if(arg.length()>0) {
      cmd.add(arg);
    }
    OSPLog.fine(cmd.toString());
    String[] cmdarray = (String[]) cmd.toArray(new String[0]);
    try {
      Process proc = Runtime.getRuntime().exec(cmdarray);
      BufferedInputStream errStream = new BufferedInputStream(proc.getErrorStream());
      StringBuffer buff = new StringBuffer();
      while(true) {
        int datum = errStream.read();
        if(datum==-1) {
          break;
        }
        buff.append((char) datum);
      }
      errStream.close();
      String msg = buff.toString().trim();
      if(msg.length()>0) {
        OSPLog.info("error buffer: "+buff.toString());
      }
    } catch(Exception ex) {
      OSPLog.info(ex.toString());
    }
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new CmdLineTestApp(), args).addControlListener("changed");
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
