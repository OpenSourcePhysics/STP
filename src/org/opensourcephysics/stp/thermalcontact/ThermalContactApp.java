/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.thermalcontact;
import java.awt.Color;
import java.text.NumberFormat;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.frames.*;
import org.opensourcephysics.stp.util.DoubleArray;

public class ThermalContactApp extends AbstractSimulation {
  PlotFrame kinFrame;
  PlotFrame potFrame;
  DisplayFrame boardFrame = new DisplayFrame("particle display");
  ParticleBoard board1, board2;
  LJSimulation sim1, sim2;
  double time;
  NumberFormat numberformat = NumberFormat.getInstance();

  public void initialize() {
    boardFrame.removeDrawable(board1);
    boardFrame.removeDrawable(board2);
    double Lx = control.getDouble("L");
    double Ly = (Math.sqrt(3)/2)*Lx;
    int Na = control.getInt("number of red particles");
    int Nb = control.getInt("number of green particles");
    double dt = control.getDouble("\u2206t");
    time = 0;
    sim1 = new LJSimulation(Na, 0, Lx, Ly, dt);
    sim2 = new LJSimulation(0, Nb, Lx, Ly, dt);
    board1 = new ParticleBoard(Na, 0, Lx, Ly, 0.5);
    board1.pos_x = sim1.x;
    board1.pos_y = sim1.y;
    board2 = new ParticleBoard(0, Nb, Lx, Ly, 0.5);
    board2.pos_x = sim2.x;
    board2.pos_y = sim2.y;
    board2.Bx = Lx+0.2;
    boardFrame.addDrawable(board1);
    boardFrame.addDrawable(board2);
    boardFrame.repaint();
    kinFrame.clearData();
    kinFrame.setPreferredMinMaxY(0, 10);
    kinFrame.setAutoscaleY(true);
    kinFrame.repaint();
    potFrame.clearData();
    potFrame.repaint();
  }

  public void reset() {
    double Lx = 12;
    control.setValue("L", Lx);
    control.setValue("number of red particles", 81);
    control.setValue("number of green particles", 64);
    control.setValue("\u2206t", 0.01);
    control.clearMessages();
    // control.setValue("\u03C3 (red-red)", 1);
    // control.setValue("\u03C3 (green-green)", 1.2);
    // control.setValue("\u03C3 (wall)", 1);
    // control.setValue("\u025B (red-red)", 1);
    // control.setValue("\u025B (green-green)", 1.5);
    boardFrame.removeDrawable(board1);
    boardFrame.removeDrawable(board2);
    board1 = new ParticleBoard(0, 0, Lx, Lx*Math.sqrt(3)/2, 0.5);
    board2 = new ParticleBoard(0, 0, Lx, Lx*Math.sqrt(3)/2, 0.5);
    board2.Bx = Lx+0.2;
    boardFrame.addDrawable(board1);
    boardFrame.addDrawable(board2);
    boardFrame.repaint();
    kinFrame.clearData();
    kinFrame.repaint();
    potFrame.clearData();
    potFrame.repaint();
    enableStepsPerDisplay(true);
  }

  public void doStep() {
    double siga = 1;   // control.getDouble("\u03C3 (red-red)");
    double sigb = 1.2; // control.getDouble("\u03C3 (green-green)");
    double sigw = 1;   // control.getDouble("\u03C3 (wall)");
    double epsa = 1;   // control.getDouble("\u025B (red-red)");
    double epsb = 1.5; // control.getDouble("\u025B (green-green)");
    double dt = control.getDouble("\u2206t");
    int N = sim1.N;
    sim1.setInteractionCoefficients(siga, sigb, sigw, epsa, epsb);
    sim1.setTimeStep(dt);
    if(sim2!=null) {
      N = Math.max(N, sim2.N);
      sim2.setInteractionCoefficients(siga, sigb, sigw, epsa, epsb);
      sim2.setTimeStep(dt);
    }
    for(int i = 0; i<1e5/(N*N); i++) {
      sim1.step();
      if(sim2!=null) {
        sim2.step();
      }
      time += dt;
    }
    double energy;
    if(sim2!=null) {
      kinFrame.append(0, time, sim1.Ka/sim1.Na);
      kinFrame.append(1, time, sim2.Kb/sim2.Nb);
      potFrame.append(0, time, sim1.Va/sim1.Na);
      potFrame.append(1, time, sim2.Vb/sim2.Nb);
      energy = sim1.Ka+sim2.Kb+sim1.Va+sim2.Vb;
    } else {
      kinFrame.append(0, time, sim1.Ka/sim1.Na);
      kinFrame.append(1, time, sim1.Kb/sim1.Nb);
      potFrame.append(0, time, sim1.Va/sim1.Na);
      potFrame.append(1, time, sim1.Vb/sim1.Nb);
      energy = sim1.Ka+sim1.Kb+sim1.Va+sim1.Vb;
    }
    control.clearMessages();
    control.println("Time = "+ControlUtils.f2(time));
    control.println("Total energy = "+ControlUtils.f2(energy));
  }

  public void connectBoxes() {
    if(sim2==null) {
      return;
    }
    double dt = control.getDouble("\u2206t");
    double siga = 1;   // control.getDouble("\u03C3 (red-red)");
    double sigb = 1.2; // control.getDouble("\u03C3 (green-green)");
    int Na = board1.Na;
    int Nb = board2.Nb;
    double Lx = board1.Lx+board2.Lx;
    double Ly = board1.Ly;
    // concatenate both boxes into one
    LJSimulation sim = new LJSimulation(Na, Nb, Lx, Ly, dt);
    sim.x = DoubleArray.concat(sim1.x, sim2.x);
    sim.y = DoubleArray.concat(sim1.y, sim2.y);
    sim.vx = DoubleArray.concat(sim1.vx, sim2.vx);
    sim.vy = DoubleArray.concat(sim1.vy, sim2.vy);
    // shift particle systems to be separated by sigma_ab
    double max1 = DoubleArray.max(sim1.x);
    double min2 = DoubleArray.min(sim2.x);
    for(int i = 0; i<Na; i++) {
      sim.x[i] += board1.Lx-max1-(siga+sigb)/4;
    }
    for(int i = Na; i<Na+Nb; i++) {
      sim.x[i] += board1.Lx-min2+(siga+sigb)/4;
    }
    ParticleBoard board = new ParticleBoard(Na, Nb, Lx, Ly, 0.5);
    board.pos_x = sim.x;
    board.pos_y = sim.y;
    boardFrame.removeDrawable(board1);
    boardFrame.removeDrawable(board2);
    boardFrame.addDrawable(board);
    boardFrame.repaint();
    sim1 = sim;
    sim2 = null;
    board1 = board;
    board2 = null;
  }

  /**
   * Constructor ThermalContactApp
   */
  public ThermalContactApp() {
    boardFrame.setSize(640, 360);
    kinFrame = new PlotFrame("Time", "Kinetic Energy / Particle", "Kinetic Energy Per Particle");
    kinFrame.limitAutoscaleX(0, 0.1);
    kinFrame.setMarkerColor(0, Color.red);
    kinFrame.setMarkerColor(1, Color.green);
    potFrame = new PlotFrame("Time", "Potential Energy / Particle", "Potential Energy Per Particle");
    potFrame.limitAutoscaleX(0, 0.1);
    potFrame.setMarkerColor(0, Color.red);
    potFrame.setMarkerColor(1, Color.green);
    numberformat.setMaximumIntegerDigits(4);
    numberformat.setMinimumIntegerDigits(4);
  }

  public static void main(String[] args) {
    SimulationControl control = SimulationControl.createApp(new ThermalContactApp(), args);
    control.addButton("connectBoxes", "Contact");
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
