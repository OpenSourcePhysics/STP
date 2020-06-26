/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.util;
public class Rdf {
  public double bin = 0.1;
  public double[] x, y;
  public double Lx, Ly;
  public double rmax;
  public int N, nbins;
  public boolean pbc = true;
  public int ncorr = 0;
  double[] gr;
  public double[] rx, ngr; // normalized gr

  /**
   * Constructor Rdf
   */
  public Rdf() {}

  /**
   * Constructor Rdf
   * @param lx
   * @param ly
   */
  public Rdf(double lx, double ly) {
    initialize(lx, ly, 0.1);
  }

  public void initialize(double lx, double ly, double _bin) {
    Lx = lx;
    Ly = ly;
    rmax = (Lx>Ly)
           ? 0.5*Lx
           : 0.5*Ly;
    bin = _bin;
    nbins = (int) Math.ceil(rmax/bin);
    gr = new double[nbins];
    ngr = new double[nbins];
    rx = new double[nbins];
    for(int i = 0; i<nbins; i++) {
      rx[i] = i*bin;
      gr[i] = 0.0;
    }
  }

  public void reset() {
    for(int i = 0; i<nbins; i++) {
      rx[i] = i*bin;
      gr[i] = 0.0;
    }
    ncorr = 0;
  }

  public void append(double[] x, double[] y) {
    N = x.length;
    ncorr++;
    for(int i = 0; i<N; i++) {
      for(int j = i+1; j<N; j++) {
        double dx = x[i]-x[j];
        double dy = y[i]-y[j];
        if(pbc) {
          dx = separation(dx, Lx);
          dy = separation(dy, Ly);
        }
        double dr = dx*dx+dy*dy;
        dr = Math.sqrt(dr);
        int nbin = (int) ((dr+0.0000001)/bin);
        if(nbin<nbins) {
          //if(nbin == 9)System.out.println(dr);
          gr[nbin]++;
        }
      }
    }
  }

  public void normalize() {
    double area;
    double pi = Math.PI;
    int i = 0;
    int imax = (int) (rmax/bin);
    double rho = N/Lx/Ly;
    double norm = 0.5*rho*ncorr*N;
    while(i<imax) {
      double r = i*bin;
      area = pi*((r+bin)*(r+bin)-r*r);
      area *= norm;
      ngr[i] = gr[i]/area;
      i++;
    }
  }

  public void setBinSize(double _bin) {
    bin = _bin;
    nbins = (int) (rmax/bin)+1;
  }

  public void setPbc(boolean p) {
    pbc = p;
  }

  public double separation(double dx, double lx) {
    if(dx>0.5*lx) {
      return dx-lx;
    }
    if(dx<-0.5*lx) {
      return dx+lx;
    }
    return dx;
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
