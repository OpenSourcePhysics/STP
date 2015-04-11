/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.ising.meanfieldequation;

import java.text.DecimalFormat;

import org.opensourcephysics.controls.*;

import org.opensourcephysics.frames.PlotFrame;

import org.opensourcephysics.numerics.*;

import java.awt.Color;

/**
 * 
 * MeanFieldApp class.
 * 
 * 
 * 
 * @author Hui Wang
 * 
 * @version 1.0 revised 11/12/06
 * revised by Jan Tobochnik 10/3/08
 * 
 */

public class MeanFieldApp extends AbstractCalculation implements Function {
	PlotFrame plotFrame = new PlotFrame("m", "tanh(m)", "");
	PlotFrame energyFrame = new PlotFrame("m", "f(m)", "Free energy");
	double beta = 1.0;
	double Jq = 4.0;
	double b = 1.0;
	double mxlimit, pxlimit; // plus minus xlimits
	double xtol = 5.0e-1;
	double[] root; // three roots
	boolean negative = false;
	DecimalFormat numberFormat = (DecimalFormat) DecimalFormat.getInstance();
	MeanFieldApp() {
		root = new double[3];
		plotFrame.setConnected(true);
		plotFrame.setMarkerColor(1,Color.RED);
		plotFrame.setMarkerColor(0,Color.BLUE);
		energyFrame.setMarkerColor(0,Color.BLUE);
		energyFrame.setConnected(true);
		numberFormat.setMinimumFractionDigits(3);
	}

	public void reset() {
		control.setValue("T", 1.0);
		control.setValue("q", 4.0);
		control.setValue("H", 0.0);
	}

	public void calculate() {
		beta = 1.0/control.getDouble("T");
		Jq = control.getDouble("q");
		//b = Math.abs(control.getDouble("B"));
		b = control.getDouble("H");
		pxlimit = 5.0;
		mxlimit = -5.0 - xtol / 2;
		double tol = 1.0e-4;
		root[0] = root[1] = root[2] = Double.NaN; // mark all roots as
		// undefined
		double x = mxlimit;
		while (x < pxlimit) {
			findRoots(x, x + xtol, tol);
			x += xtol;
		}
		control.clearMessages();
		if (countRoots() == 1) {
			control.println("Root = " + numberFormat.format(root[0]));
		} else if (countRoots() > 1) {
			mxlimit = root[0] - 1;
			pxlimit = root[2] + 1;
			for (int i = 0; i < 3; i++) {
				if (!Double.isNaN(root[i])) {
					control.println("Root " + i + " = "
							+ numberFormat.format(root[i]));
				}
			}
		} else {
			control.println("No roots found");
		}
		plot();
	}

	public boolean findRoots(double mx, double px, double tol) {
		double r0 = Root.bisection(this, mx, px, tol);
		if (Double.isNaN(r0)) {
			return false; // No roots at all
		}
		storeRoots(r0);
		return true;
	}

	public void storeRoots(double r) {
		int i = 0;
		while ((i < 3) && !Double.isNaN(root[i])) {
			i++;
		}
		if (i == 3) {
			System.out.println("More than three roots found!");
			return;
		}
		root[i] = r;
	}

	public int countRoots() {
		int counter = 0;
		for (int i = 0; i < 3; i++) {
			if (!Double.isNaN(root[i])) {
				counter++;
			}
		}
		return counter;
	}

	public void plot() {
			double x = mxlimit;
			while (x < pxlimit) {
				plotFrame.append(0, x, mftanh(x));
				plotFrame.append(1, x, x);
				energyFrame.append(0, x, freeenergy(x));
				x += 0.02;
			}
		plotFrame.render();
	}

	public double freeenergy(double x) {
		return 0.5 * Jq * x * x - 1.0 / beta
				* Math.log(2 * cosh(beta * (Jq * x + b)));
	}

	public double evaluate(double x) {
		return mftanh(x) - x;
	}

	public double mftanh(double x) {
		return tanh(beta * (Jq * x + b));
	}

	public double tanh(double x) {
		double ex, mex;
		ex = Math.exp(x);
		mex = 1 / ex;
		return (ex - mex) / (ex + mex);
	}

	public double cosh(double x) {
		double ex, mex;
		ex = Math.exp(x);
		mex = 1 / ex;
		return (ex + mex) / 2;
	}

	public static void main(String[] args) {
		CalculationControl.createApp(new MeanFieldApp(), args);
	}

}

/*
 * 
 * Open Source Physics software is free software; you can redistribute
 * 
 * it and/or modify it under the terms of the GNU General Public License (GPL)
 * as
 * 
 * published by the Free Software Foundation; either version 2 of the License,
 * 
 * or(at your option) any later version.
 * 
 * 
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * 
 * or any subpackage (subdirectory) of this package must must also be be
 * released
 * 
 * under the GNU GPL license.
 * 
 * 
 * 
 * This software is distributed in the hope that it will be useful,
 * 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * 
 * GNU General Public License for more details.
 * 
 * 
 * 
 * You should have received a copy of the GNU General Public License
 * 
 * along with this; if not, write to the Free Software
 * 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * 
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 * 
 * 
 * 
 * Copyright (c) 2007 The Open Source Physics project
 * 
 * http://www.opensourcephysics.org
 * 
 */

