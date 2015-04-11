/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.carnot;

/**
 * A Carnot cycle.
 *
 * @author Hui Wang
 * @version 1.0
 * @created 02/21/2007
 */
import java.awt.Color;
import java.awt.event.MouseEvent;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.InteractiveMouseHandler;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.frames.*;

/**
 * put your documentation comment here
 * @author Hui Wang
 * @created January 23, 2007
 */
public class CarnotApp extends AbstractCalculation implements InteractiveMouseHandler {
  double dr2 = 4.0;
  Vertex[] V;
  double v0, v1, v2, v3, p0, p1, p2, p3;
  double g = 4.0/3.0;
  double dv = 0.01;
  PlotFrame plotFrame = new PlotFrame("V", "P", "P-V Diagram");

  /**
   * Constructor CarnotApp
   */
  public CarnotApp() {
    V = new Vertex[4];
    for(int i = 0; i<4; i++) {
      V[i] = new Vertex(1, 1);
    }
    plotFrame.setMarkerColor(0, Color.red);
    plotFrame.setMarkerColor(1, Color.red);
    plotFrame.setMarkerColor(2, Color.GREEN);
    plotFrame.setMarkerColor(3, Color.GREEN);
  }

  public void calculate() {
    drawCurves();
  }

  public void drawCurves() {
    plotFrame.clearData();
    addCurve(V[0], V[1], g, 0);
    addCurve(V[2], V[3], g, 1);
    addCurve(V[0], V[2], 1, 2);
    addCurve(V[1], V[3], 1, 3);
    plotFrame.render();
  }

  public void addCurve(Vertex V1, Vertex V2, double a, int id) {
    double v = V1.v;
    double p, ddv;
    double C = V1.getProd(a);
    ddv = dv;
    if(V2.v<V1.v) {
      ddv = -dv;
    }
    while(Math.abs(v-V2.v)>dv) {
      p = C*Math.pow(v, -a);
      plotFrame.append(id, v, p);
      v += ddv;
    }
  }

  public void resetCalculation() {
    control.clearMessages();
    plotFrame.clearData();
    p0 = 5.0;
    v0 = 2.0;
    p3 = 2;
    v3 = 3.0;
    V[0].set(p0, v0);
    V[3].set(p3, v3);
    update(1, 2);
    plotFrame.repaint();
    plotFrame.setInteractiveMouseHandler(this);
  }

  public void handleMouseAction(InteractivePanel panel, MouseEvent evt) {
    int id = -1;
    if(panel.getMouseAction()==InteractivePanel.MOUSE_PRESSED) {
      //do nothing                
    }
    if(panel.getMouseAction()==InteractivePanel.MOUSE_DRAGGED) {
      double x = panel.getMouseX();
      double y = panel.getMouseY();
      id = MouseOnCurve(x, y);
      System.out.println(id+"\t"+x+"\t"+y);
      if((x>0)&&(y>0)) {
        drawCurve(id, x, y);
      }
    }
    if(panel.getMouseAction()==InteractivePanel.MOUSE_RELEASED) {
      plotFrame.setMessage("");
    }
  }

  public int MouseOnCurve(double x0, double y0) {
    for(int j = 0; j<4; j++) {
      double x = V[j].v;
      double y = V[j].p;
      double dx = x-x0;
      double dy = y-y0;
      if(dx*dx+dy*dy<dr2) {
        return j;
      }
    }
    return -1;
  }

  public void drawCurve(int id, double v0, double p0) {
    if(!check(p0, v0)) {
      return;
    }
    if((id==0)||(id==3)) {
      Vertex vo = new Vertex(V[id].p, V[id].v);
      V[id].set(p0, v0);
      if(!update(1, 2)) {
        V[id].set(vo);
      }
    } else if((id==1)||(id==2)) {
      Vertex vo = new Vertex(V[id].p, V[id].v);
      V[id].set(p0, v0);
      if(!update(0, 3)) {
        V[id].set(vo);
      }
    }
    drawCurves();
  }

  public boolean check(Vertex ver) {
    if((ver.v>10)||(ver.p>10)) {
      return false;
    }
    return true;
  }

  public boolean check(double _p, double _v) {
    if((_p>20)||(_v>20)) {
      return false;
    }
    return true;
  }

  public boolean update(int i, int j) {
    int i0 = 0, j0 = 0;
    if((i==1)&&(j==2)) {
      i0 = 0;
      j0 = 3;
    }
    if((i==0)&&(j==3)) {
      i0 = 1;
      j0 = 2;
    }
    v1 = Math.pow(V[i0].getProd(g)/V[j0].getProd(), 1.0/(g-1));
    p1 = V[j0].getProd()/v1;
    v2 = Math.pow(V[j0].getProd(g)/V[i0].getProd(), 1.0/(g-1));
    p2 = V[i0].getProd()/v2;
    if(!check(p1, v1)) {
      return false;
    }
    if(!check(p2, v2)) {
      return false;
    }
    V[i].set(p1, v1);
    V[j].set(p2, v2);
    return true;
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new CarnotApp(), args);
  }

}

class Vertex {
  double p, v;

  Vertex(Vertex v0) {
    p = v0.p;
    v = v0.v;
  }

  Vertex(double _p, double _v) {
    p = _p;
    v = _v;
  }

  void set(double _p, double _v) {
    p = _p;
    v = _v;
  }

  void setp(double _p) {
    p = _p;
  }

  void setv(double _v) {
    v = _v;
  }

  void set(Vertex v0) {
    v = v0.v;
    p = v0.p;
  }

  double getProd() {
    return p*v;
  }

  double getProd(double g) {
    return p*Math.pow(v, g);
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
