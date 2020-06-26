/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.ising2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.*;
import java.awt.Color;

public class Ising2DHysteresisApp extends AbstractSimulation {
	Ising2D ising;
	DisplayFrame displayFrame = new DisplayFrame("Spin Configuration");
	DrawingPanel displayPanel;
	PlotFrame plotFrame = new PlotFrame("H", "M", "Hysteresis Loop");
	NumberFormat nf;
	double bondProbability, H, dH = 0.01;
	int dir = 1, mcsPerH;
	boolean metropolis = true;

	/**
	 * Constructor Ising2DApp
	 */
	public Ising2DHysteresisApp() {
		ising = new Ising2D();
		plotFrame.setPreferredMinMax(-1, 1,-1,1);
		plotFrame.setAutoscaleX(false);
		plotFrame.setAutoscaleY(false);	
		plotFrame.setMarkerColor(0,Color.BLUE);
		plotFrame.setMarkerColor(1,Color.RED);
		displayFrame.addDrawable(ising);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		displayPanel = displayFrame.getDrawingPanel();
	}

	public void initialize() {
		ising.initialize(control.getInt("Length"), control
				.getDouble("Temperature"), H);
		this.bondProbability = bondProbability(ising.J, ising.T);
		displayPanel.setPreferredMinMax(-5, ising.L + 5, -5, ising.L + 5);
		control.clearMessages();
		stopRunning();
		H = control.getDouble("Initial H");
		dH = Math.abs(control.getDouble("dH"));
		if(H > 0) dir = -1; else dir = 1;
		mcsPerH = control.getInt("mcs per field value");
		ising.setTemperature(control.getDouble("Temperature"));
	}

	public double bondProbability(double J, double T) {
		return 1 - Math.exp(-2 * J / T);
	}

	public void doStep() {
		ising.setExternalField(H);
		ising.resetData();
		for (int i = 0; i < mcsPerH; i++)
			ising.doOneMCStep();
		if(dir < 0)
			plotFrame.append(0, H, (double) ising.M / ising.N);
		else
			plotFrame.append(1, H, (double) ising.M / ising.N);
		if (Math.abs(H) > 1){
			dir = -dir;
		}
		H += dH * dir;
	}


	public void reset() {
		control.setValue("Length", 32);
		control.setValue("Initial H", 1);
		control.setValue("dH", 0.01);
		control.setValue("mcs per field value", 10);
		control.setValue("Temperature", nf
				.format(1.8));
		// control.setAdjustableValue("External field", 0);
		// OSPCombo combo = new OSPCombo(new String[] {"Metropolis", "Wolff"},
		// 0); // second argument is default
		// control.setValue("Dynamics", combo);
		 enableStepsPerDisplay(true);
	}

	public void zeroAverages() {
		control.clearMessages();
		ising.resetData();
		plotFrame.clearData();
		plotFrame.repaint();
	}

	/**
	 * Switch to the WRApp user interface.
	 */
	public void switchGUI() {
		stopSimulation();
		Runnable runner = new Runnable() {
			public synchronized void run() {
				OSPRuntime.disableAllDrawing = true;
				OSPFrame mainFrame = getMainFrame();
				XMLControlElement xml = new XMLControlElement(getOSPApp());
				WindowListener[] listeners = mainFrame.getWindowListeners();
				int closeOperation = mainFrame.getDefaultCloseOperation();
				mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				mainFrame.setKeepHidden(true);
				mainFrame.dispose();
				Ising2DWRApp app = new Ising2DWRApp();
				Ising2DControl c = new Ising2DControl(app, app.displayFrame,
						null);
				c.getMainFrame().setDefaultCloseOperation(closeOperation);
				for (int i = 0, n = listeners.length; i < n; i++) {
					if (listeners[i].getClass().getName().equals(
							"org.opensourcephysics.tools.Launcher$FrameCloser")) {
						c.getMainFrame().addWindowListener(listeners[i]);
					}
				}
				c.loadXML(xml, true);
				app.customize();
				app.initialize();
				System.gc();
				OSPRuntime.disableAllDrawing = false;
				GUIUtils.showDrawingAndTableFrames();
			}

		};
		Thread t = new Thread(runner);
		t.start();
	}

	void customize() {
		OSPFrame f = getMainFrame();
		if ((f == null) || !f.isDisplayable()) {
			return;
		}
		JMenu menu = f.getMenu("Display");
		JMenuItem item = new JMenuItem("Switch GUI");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchGUI();
			}

		});
		menu.add(item);
		addChildFrame(displayFrame);
		addChildFrame(plotFrame);
	}

	public static void main(String[] args) {
		Ising2DHysteresisApp app = new Ising2DHysteresisApp();
		SimulationControl.createApp(app, args);
		app.customize();
	}

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2007 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
