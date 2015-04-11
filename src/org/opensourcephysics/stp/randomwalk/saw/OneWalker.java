/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.randomwalk.saw;

/**
 * SAW Random Walk simulation in 2D  using flat histogram method
 *
 *  @author Jan Tobochnik
 *  @07/17/06
 */
public class OneWalker {
  double weight;
  static short s0 = 512; // use short to save memory
  int xVisited[] = new int[2*s0+1];
  int yVisited[] = new int[2*s0+1];
  int moveX[] = new int[4];
  int moveY[] = new int[4];
  boolean occupied[] = new boolean[4];
  int goodMoveX[] = new int[3];
  int goodMoveY[] = new int[3];
  int x, y;
  int m, t;

  /**
   * Initializes walker array
   */
  public void copyWalker(OneWalker toBeCloned, double weight) {
    int arraySize = toBeCloned.xVisited.length;
    xVisited = new int[arraySize];
    yVisited = new int[arraySize];
    System.arraycopy(toBeCloned.xVisited, 0, xVisited, 0, arraySize);
    System.arraycopy(toBeCloned.yVisited, 0, yVisited, 0, arraySize);
    this.weight = weight;
    x = toBeCloned.x;
    y = toBeCloned.y;
    t = toBeCloned.t;
  }

  public void newWalker(Flat f) {
    x = s0;
    y = s0;
    weight = 1;
    xVisited[0] = x;
    yVisited[0] = y;
    double r = Math.random();
    if(r<0.25) {
      x++;
    } else if(r<0.5) {
      x--;
    } else if(r<0.75) {
      y++;
    } else {
      y--;
    }
    xVisited[1] = x;
    yVisited[1] = y;
    f.xAccum += (x-s0);
    f.xSquaredAccum += (x-s0)*(x-s0);
    f.yAccum += (y-s0);
    f.ySquaredAccum += (y-s0)*(y-s0);
    f.norm += 1;
    t = 2;
  }

  /**
   *  Does self avoiding walk for one walker using Rosenbluth and Rosenbluth method
   */
  public void step(Flat f) {
    if(t<2*s0+1 && t<xVisited.length) {
      m = 0;
      for(int nn = 0; nn<4; nn++) {
        occupied[nn] = false;
        moveX[nn] = x;
        moveY[nn] = y;
      }
      moveX[0]++;
      moveX[1]--;
      moveY[2]++;
      moveY[3]--;
      for(int t1 = 0; t1<t; t1++) {
        for(int nn = 0; nn<4; nn++) {
          if((xVisited[t1]==moveX[nn])&&(yVisited[t1]==moveY[nn])) {
            occupied[nn] = true;
          }
        }
      }
      for(int nn = 0; nn<4; nn++) {
        if(!occupied[nn]&& m<3) {
          goodMoveX[m] = moveX[nn];
          goodMoveY[m] = moveY[nn];
          m++;
        }
      }
      if(m==0) {
        weight = 0;
        return;
      } else {
        int j = (int) (Math.random()*m); // pick possible move
        weight = weight*(m/3.0);
        x = goodMoveX[j];
        y = goodMoveY[j];
        xVisited[t] = x;
        yVisited[t] = y;
      }
      t++;
      f.xAccum += (x-s0)*weight;
      f.xSquaredAccum += (x-s0)*(x-s0)*weight;
      f.yAccum += (y-s0)*weight;
      f.ySquaredAccum += (y-s0)*(y-s0)*weight;
      f.norm += weight;
    }
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
