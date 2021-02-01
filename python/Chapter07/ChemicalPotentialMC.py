
"""
Created on June 15, 2020
@author: Jan Tobochnik
Metropolis MC simulation of Lennard-Jones particles in 2D, output is chemical potential
using Widom insertion method
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *

def main():
#input parameters
    N = int(input0('N',100))   #number of particles
    mcs = int(input0('MC steps per particle',200))  #number of steps
    Lx = float(input0('L',20))
    Ly = Lx 
    T = float(input0('Temperature',1.0))
    ds = 2*float(input0('maximum step size',1.0))
    nPlot = int(input0('nPlot',20))  #number of walks between plots. Make larger to speed up
    
    #set up initial configuration
    x = np.zeros(N)
    y = np.zeros(N)
    nx = int(0.01 + math.sqrt(N))
    if nx*nx < N:
        nx += 1
    n = 0
    ax = Lx/nx
    ay = Ly/nx
    while n < N:
        i = n % nx
        j = int(n/nx)
        x[n] = (0.5+ i)*ax
        y[n] = (0.5+ j)*ay
        n += 1             
    #set up plots and initial variables
    a = 280/Lx   #disk size in plot
    plt.close()
    fig, axs = plt.subplots(1, 3, figsize=(15, 5*math.sqrt(3)/2))
    E,P = EandPCalcTotal(x,y,Lx,Ly,N,T)
    def reset(cum,axs):
        cum['E'] = 0  
        cum['E2'] = 0 
        cum['P'] = 0 
        cum['P2'] = 0 
        cum['accept'] = 0 
        cum['n'] = 0
        cum['cp'] = 0
        for ax, interp in zip(axs, ['Lennard-Jones', 'Mean excess chemical potential','Pressure']):
            ax.clear()
            ax.set_title(interp)
            ax.grid(True)  #add grid for plots
            if interp == 'Lennard-Jones':
                ax.set_xlim(0,Lx) 
                ax.set_ylim(0,Ly)
                dataD, = ax.plot(0,0)
            elif interp == 'Pressure':
                ax.set_xlabel('mcs') 
                ax.set_ylabel('PA/NkT') 
            else:
                ax.set_xlabel('mcs') 
                ax.set_ylabel('mu*') 
        plt.tight_layout()  #improve appearance of layout
        return cum,axs,dataD 
    cum = {}   #set up dictionary (hash table)
    cum,axs,dataD, = reset(cum,axs)
    Nupdates = 0
    
    #start simulation
    keepRunning = 'c'
    while keepRunning == 'c':
        for imcs in range(mcs):
            cum['n'] += 1 
            for n in range(N):
                i = int(rnd()*N)
                xnew = pbc(x[i] +(rnd() - 0.5)*ds,Lx) 
                ynew = pbc(y[i] +(rnd() - 0.5)*ds,Ly)
                Enew,Pnew = EandPCalc1(x,y,xnew,ynew,i,Lx,Ly,N,T)
                Eold,Pold = EandPCalc1(x,y,x[i],y[i],i,Lx,Ly,N,T)
                dE = Enew - Eold
                if (dE <= 0) or  (math.exp(-dE/T) > rnd()):
                    cum['accept'] += 1
                    x[i] = xnew
                    y[i] = ynew
                    E += dE
                    P += Pnew-Pold
                cum['E'] += E
                cum['E2'] += E*E
                cum['P'] += P
                cum['P2'] += P*P
                cum['cp'] += computeChemicalPotential(x,y,Lx,Ly,N,T)
            if cum['n'] % nPlot == 0:
                for ax, interp in zip(axs, ['Lennard-Jones', 'Mean excess chemical potential','Pressure']):
                    if interp == 'Lennard-Jones':
                        #dataD.remove()  #remove old data
                        dataD.remove()
                        dataD, = ax.plot(x,y,'bo',markersize = a)  #add new data
                    elif interp == 'Pressure':
                        ax.plot(cum['n'],cum['P']/(N*cum['n']),'bo')  #add new data
                    else:
                        ax.plot(cum['n'],-T*math.log(cum['cp']/(N*cum['n'])),'bo')
                plt.pause(.000001) #needed to visualize data continuously
        print3f('acceptance ratio',cum['accept']/(N*cum['n']))
        print3f('density = N/A',N/(Lx*Ly))
        print3f('PA/NkT= ', cum['P']/(N*cum['n']))
        print3f('average energy',cum['E']/(N*N*cum['n']))
        print3f('Specific Heat',(cum['E2']/(N*cum['n'])-(cum['E']/(N*cum['n']))**2)/(N*T*T))
        keepRunning = input('Continue/Stop/Reset/New parameters (c/s/r/n)')
        if keepRunning == 'r':
            cum,axs,dataD, = reset(cum,axs)
            keepRunning = 'c'
        elif keepRunning == 'n':
            mcs = int(input0('MC steps per particle',mcs))
            nPlot = int(input0('nPlot',nPlot))
            ds = 2*float(input0('maximum step size',ds/2))
            keepRunning = 'c'
            
def EandPCalc1(x,y,x1,y1,i,Lx,Ly,N,T):
    E = 0
    P = 0
    f = 6*N/(T*Lx*Ly)
    for j in range(N):
        if i != j:
            dx = sep(x1-x[j],Lx)
            dy = sep(y1-y[j],Ly)
            r2 = dx*dx+dy*dy
            r6i = 1/(r2*r2*r2)
            E += 4*r6i*(r6i - 1)
            P += f*r6i*(2*r6i - 1)
    return E,P

def EandPCalcTotal(x,y,Lx,Ly,N,T):
    E = 0
    P = 0
    f = 6*N/(T*Lx*Ly)
    for i in range(N-1):
        for j in range(i+1,N):
            dx = sep(x[i]-x[j],Lx)
            dy = sep(y[i]-y[j],Ly)
            r2 = dx*dx+dy*dy
            r6i = 1/(r2*r2*r2)
            E += 4*r6i*(r6i - 1)
            P += f*r6i*(2*r6i - 1)
    P += 1   #non-interaction contribution to pressure
    return E,P
 
def computeChemicalPotential(x,y,Lx,Ly,N,T):
    xc = Lx*rnd()
    yc = Ly*rnd()
    dU = 0
    for i in range(N):
        dx = sep(xc-x[i], Lx)
        dy = sep(yc-y[i], Ly)
        r2 = dx*dx+dy*dy
        oneOverR2 = 1.0/r2
        oneOverR6 = oneOverR2*oneOverR2*oneOverR2
        dU += 4.0*oneOverR6*(oneOverR6-1)
    return math.exp(-dU/T)
    
main()