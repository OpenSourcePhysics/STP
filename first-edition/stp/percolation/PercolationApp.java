/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

//package  edu.clarku.sip.percolation;
package org.opensourcephysics.stp.percolation;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.frames.DisplayFrame;
// import org.opensourcephysics.display2d.Lattice;
import java.awt.event.*;
import java.awt.*;
import java.util.Random;

/**
 * <b>Input Parameters</b> <br>
 * <code>L</code> Lattice size. <br>
 * <code>p</code> Probability of a site being occupied.
 * <p>
 * <b>Frames</b> <br>
 * <code>OSPFrame0</code> Lattice.
 *
 * @author jgould
 * @created January 23, 2002
 */
public class PercolationApp extends AbstractCalculation implements InteractiveMouseHandler, Drawable {
  int[][] rsite; // size of array = L + 1, lattice
  static final int OCCUPIED = 1, EMPTY = 0;
  double p;      // chance of being occupied
  DisplayFrame displayFrame = new DisplayFrame("");
  int[] np;
  int L;         // iterate from 1 to L (ignore np[0])
  final Color OCCUPIED_COLOR = Color.black, EMPTY_COLOR = Color.white;
  Color selectedColor;
  // no cluster number is selected, the selected cluster number
  int NONE = -99, selectedCluster = NONE;
  Random random;

  /**
   * Constructor PercolationApp
   */
  public PercolationApp() {
    random = new Random();
    // drawingFrame.setShowCoordinates(false);
    displayFrame.setSquareAspect(true);
    displayFrame.setInteractiveMouseHandler(this);
    displayFrame.addDrawable(this);
  }

  public void calculate() {
    random.setSeed(1239012312);
    L = control.getInt("L");
    p = control.getDouble("p");
    rsite = new int[L+1][L+1];
    selectedCluster = NONE;
    displayFrame.setPreferredMinMax(0, L+2, L+2, 0);
    fill();
    compute();
    displayFrame.repaint();
  }

  public void reset() {
    control.setValue("L", 20);
    control.setValue("p", 0.5);
    calculate();
  }

  public void compute() {
    np = new int[rsite.length*rsite.length];
    int ncluster = 0; // cluster number
    // int bottomy;
    // int topy;
    for(int y = 1; y<=L; y++) {
      for(int x = 1; x<=L; x++) {
        if(rsite[x][y]==OCCUPIED) {
          int down = y-1;
          int left = x-1;
          if(rsite[x][down]+rsite[left][y]==0) {
            ncluster++;              // new cluster
            rsite[x][y] = ncluster;
            np[ncluster] = ncluster; // proper label
          } else {
            neighbor(x, y);
          }
        }
      }
    }
    for(int y = 1; y<=L; y++) {
      // assign proper labels to cluster array
      for(int x = 1; x<=L; x++) {
        if(rsite[x][y]!=EMPTY) {
          rsite[x][y] = proper(rsite[x][y]);
        }
      }
    }
  }

  void neighbor(int x, int y) {
    int down = y-1;
    int left = x-1;
    if(rsite[x][down]*rsite[left][y]>0) { // both neighbors occupied
      labelMin(x, y, left, down);
    } else if(rsite[x][down]>0) {
      rsite[x][y] = rsite[x][down];
    } else {
      rsite[x][y] = rsite[left][y];
    }
  }

  void labelMin(int x, int y, int left, int down) {
    // both neighbors occupied, determine minimum cluster number
    if(rsite[left][y]==rsite[x][down]) {
      rsite[x][y] = rsite[left][y]; // both neighbors have the same
      // cluster label
    } else {
      // determine minimum cluster label
      int cl_left = proper(rsite[left][y]);
      int cl_down = proper(rsite[x][down]);
      int nMax = Math.max(cl_left, cl_down);
      int nMin = Math.min(cl_left, cl_down);
      rsite[x][y] = nMin;
      if(nMin!=nMax) {
        np[nMax] = nMin;
      }
    }
  }

  int proper(int label) {
    if(np[label]==label) {
      return label;
    } else {
      return proper(np[label]);
    }
  }

  public void fill() {
    for(int y = 1; y<=L; y++) {
      for(int x = 1; x<=L; x++) {
        double d = random.nextDouble();
        if(d<p) {
          rsite[x][y] = OCCUPIED;
        } else {
          rsite[x][y] = EMPTY;
        }
      }
    }
  }

  public void draw(DrawingPanel drawingPanel, Graphics g) {
    if(rsite==null) {
      return;
    }
    int cellSizeX = 1+(drawingPanel.xToPix(L+1)-drawingPanel.xToPix(1))/L;
    int cellSizeY = 1+(drawingPanel.yToPix(L+1)-drawingPanel.yToPix(1))/L;
    for(int i = 1; i<=L; i++) {
      for(int j = 1; j<=L; j++) {
        int state = rsite[i][j];
        if(state==selectedCluster) {
          g.setColor(selectedColor);
        } else if(state==EMPTY) {
          g.setColor(EMPTY_COLOR);
        } else {
          g.setColor(OCCUPIED_COLOR);
        }
        int px0 = drawingPanel.xToPix(i);
        int py0 = drawingPanel.yToPix(j);
        g.fillRect(px0, py0, cellSizeX, cellSizeY);
      }
    }
  }

  public static void main(String[] args) {
    CalculationControl.createApp(new PercolationApp(), args);
  }

  public void handleMouseAction(InteractivePanel panel, MouseEvent e) {
    if(panel.getMouseAction()!=InteractivePanel.MOUSE_PRESSED) {
      return;
    }
    int xClicked = (int) displayFrame.getDrawingPanel().pixToX(e.getX());
    int yClicked = (int) displayFrame.getDrawingPanel().pixToY(e.getY());
    int selectedClusterSize = 0;
    if((xClicked<=L)&&(yClicked<=L)&&(yClicked>=1)&&(xClicked>=1)) {
      selectedCluster = rsite[xClicked][yClicked];
      if(selectedCluster==EMPTY) {
        return;
      }
      selectedColor = new Color((int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255));
      for(int y = 1; y<=L; y++) {
        // find all with this clusterNumber
        for(int x = 1; x<=L; x++) {
          if(rsite[x][y]==selectedCluster) {
            selectedClusterSize++;
          }
        }
      }
    }
    control.clearMessages();
    control.println("Selected cluster size = "+selectedClusterSize);
    displayFrame.repaint();
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
