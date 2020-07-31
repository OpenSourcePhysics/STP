package org.opensourcephysics.stp.Ch06;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.*;

import java.awt.Color;
import java.text.NumberFormat;

/**
 * BoseSumApp class - computes N0, Nex, mu, and C as a function of T
 *
 *  @author Jan Tobochnik
 *  @version 1.0   based on Price and Swendsen AJP paper
 *     2/6/20
 */
public class BoseGasSumApp extends AbstractCalculation{
	PlotFrame muFrame = new PlotFrame("kT", "\u03bc*", "Temperature Dependence of the Chemical Potential");	//mu
	PlotFrame nFrame = new PlotFrame("kT", "N0, N1 Nex", "Occupancy of Single Particle States");    
	PlotFrame eFrame = new PlotFrame("kT", "C/N", "Specific Heat");    
	double b = 1.0;
	double T = 1.0;
	double mu = -1.0;
	double x_low = 0.0;
	double x_high = 1e3;
	double lndelta = Math.log(1E8);
	int npoints = 0;
	double[] mua, Ta, Ea;
	double tolerance = 0.001;
	int nmaxTrials = 1000;
	NumberFormat nf = NumberFormat.getInstance();
	 NumberFormat numberFormatTwoDigits = NumberFormat.getInstance();
	  NumberFormat numberFormatFourDigits = NumberFormat.getInstance();

	 public BoseGasSumApp(){
	  OSPRuntime.setAppClass(this);
	  }

   public void reset() {
	   control.setValue("T0", 2.0); 
	   control.setValue("TMax", 150.0); 
	   control.setValue("dT", 2.0); 
	   control.setValue("N",500);
	  // control.setValue("mu at T0",1);
   }
  
   public void calcN(){
	   double mu = control.getDouble("mu at T0");
	   double T = control.getDouble("T0");
	   double nmax = (int)(Math.sqrt(T*lndelta + mu));
       double N = 0;
	   for(int nx = 1; nx < nmax;nx++)
		   for(int ny = 1; ny < nmax;ny++)
			   for(int nz = 1; nz < nmax;nz++){
				   double ep = nx*nx + ny*ny+nz*nz;
				   double nbar = 1.0/(Math.exp((ep-mu)/T) -1);
				   N += nbar;
			    }
       control.println("mu = " + mu + " T = " + " N = " + decimalFormat.format(N));
   }
   public void calculate() {
	   nFrame.clearData();
	   muFrame.clearData();
	   int N = control.getInt("N");
	   control.println("Tc = " + decimalFormat.format(calculateTc(N)));
	   double dN = 0.0001;
	   double mu = 0;
	   double dT = control.getDouble("dT");
	   double E1 = 0;
	   for(double T = control.getDouble("T0");T <control.getDouble("TMax")+0.01; T += dT){
		   double nmax = (int)(Math.sqrt(T*lndelta + mu));
		   double muplus  = 3;
		   double muminus = -3*T;
		   double Ntrial = 0;
		   double E = 0;
		   while(Math.abs(N-Ntrial) > dN){
			   mu = 0.5*(muplus+muminus);
			   Ntrial = 0;
			   E = 0;
			   for(int nx = 1; nx < nmax;nx++)
				   for(int ny = 1; ny < nmax;ny++)
					   for(int nz = 1; nz < nmax;nz++){
						   double ep = nx*nx + ny*ny+nz*nz;
						   double nbar = 1.0/(Math.exp((ep-mu)/T) -1);
						   Ntrial += nbar;
						   E += ep*nbar;
					    }
			   if(Ntrial > N) muplus = mu; else muminus = mu;
		   }
		   muFrame.append(0,T,mu);
		   double N0 = 1.0/(Math.exp((3-mu)/T) -1);
		   double N1 = 3.0/(Math.exp((6-mu)/T) -1);
		   nFrame.append(0,T,N0);
		   nFrame.append(1,T,N-N0);
		   nFrame.append(2,T,N1);
		   if(E1 != 0){
			   double C = (E - E1)/dT;
			   eFrame.append(0, T-0.5*dT, C/N);
		   }
		   E1 = E;
			   
			   
	   }  
   }
   
   public double calculateTc(int N){
	   double lndelta = Math.log(1E8);
	   double dN = 0.0001;
	   double mu = 3;
		   double nmax = (int)(Math.sqrt(N*lndelta + mu));
		   double Tplus  = N;
		   double Tminus = 0;
		   double Ntrial = 0;
		   while(Math.abs(N-Ntrial) > dN){
			   T = 0.5*(Tplus+Tminus);
			   Ntrial = 0;
			   for(int nx = 1; nx < nmax;nx++)
				   for(int ny = 1; ny < nmax;ny++)
					   for(int nz = 1; nz < nmax;nz++){
						   if(nx*ny*nz > 1){
						   double ep = nx*nx + ny*ny+nz*nz;
						   double nbar = 1.0/(Math.exp((ep-mu)/T) -1);
						   Ntrial += nbar;
						   }
						   //System.out.println(Ntrial + " " + T);
					    }
			   if(Ntrial > N) Tplus = T; else Tminus = T ;
		   }
		   return T;
   }
   
   
   public static void main(String[] args) {
	   CalculationControl control = CalculationControl.createApp(new BoseGasSumApp(),args);
//	   control.addButton("calcN", "calculateN");
   }
}


