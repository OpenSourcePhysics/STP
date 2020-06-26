/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.percolation;
import java.awt.event.*;
import org.opensourcephysics.controls.Control;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.frames.DisplayFrame;

public class PercolationMouseListener extends MouseAdapter {
  DrawingPanel drawingPanel;
  PercolationLattice lattice;
  Control control;

  /**
   * Constructor PercolationMouseListener
   * @param frame
   * @param lattice
   */
  public PercolationMouseListener(DisplayFrame frame, PercolationLattice lattice) {
    drawingPanel = frame.getDrawingPanel();
    this.lattice = lattice;
  }

  /**
   * Constructor PercolationMouseListener
   * @param panel
   * @param lattice
   */
  public PercolationMouseListener(DrawingPanel panel, PercolationLattice lattice) {
    drawingPanel = panel;
    this.lattice = lattice;
  }

  public void setControl(Control c) {
    control = c;
  }

  public void mouseClicked(MouseEvent e) {
    System.out.println("mouse clicked");
    int xClicked = (int) drawingPanel.pixToX(e.getX());
    int yClicked = (int) drawingPanel.pixToY(e.getY());
    int selectedClusterSize = 0;
    if((xClicked<=lattice.L)&&(yClicked<=lattice.L)&&(yClicked>=1)&&(xClicked>=1)) {
      lattice.selectedCluster = lattice.rsite[xClicked][yClicked];
      if(lattice.selectedCluster==HoshenKopelman.EMPTY) {
        return;
      }
      //          lattice.selectedColor = GUIUtils.randomColor();
      for(int y = 1; y<=lattice.L; y++) {
        // find all with this clusterNumber
        for(int x = 1; x<=lattice.L; x++) {
          if(lattice.rsite[x][y]==lattice.selectedCluster) {
            selectedClusterSize++;
          }
        }
      }
    }
    control.clearMessages();
    control.println("Selected cluster size = "+selectedClusterSize);
    drawingPanel.repaint();
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
