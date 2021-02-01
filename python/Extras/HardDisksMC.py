
"""
Created on September 1, 2019
@author: Jan Tobochnik
Metropolis MC simulation of hard disks, output is g(r)
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *

def main():
#input parameters
    N = int(input0('N',64))   #number of particles
    mcs = int(input0('MC steps per particle',200))  #number of steps
    initconf = input0('initial configuration:fluid(random), rectagular, hexangonal (f/r/h)','h')
    Lx = float(input0('Lx  (Ly = sqrt(3)Lx/2))',20))
    ds = 2*float(input0('maximum step size',1.0))
    Ly = Lx*math.sqrt(3)/2
    nPlot = int(input0('nPlot',20))  #number of walks between plots. Make larger to speed up
    
    #set up initial configuration
    x = np.zeros(N)
    y = np.zeros(N)
    nx = int(0.01 + math.sqrt(N))
    if nx*nx < N:
        nx += 1
    keepRunning = 'c'
    if initconf == 'f':
        for n in range(N):
            attempts = 0
            if keepRunning == 's':
                break
            while True:
                attempts += 1
                if attempts > 20:
                    keepRunning = 's'
                    print('CANNOT CREATE RANDOM CONFIGURATION')
                    break
                x[n] = rnd()*Lx
                y[n] = rnd()*Ly
                i = 0
                overlap = False
                while  (i < n):
                    dx = sep(x[n] -x[i],Lx)
                    dy = sep(y[n] -y[i],Ly)
                    if (dx*dx + dy*dy) < 1:
                        overlap = True
                        break
                    i += 1
                if not overlap:
                    break
    elif initconf == 'r': 
        n = 0
        ax = Lx/nx
        ay = Ly/nx
        while n < N:
            i = n % nx
            j = int(n/nx)
            x[n] = (0.5+ i)*ax
            y[n] = (0.5+ j)*ay
            n += 1
    elif initconf == 'h': 
        n = 0
        ax = Lx/nx
        ay = Ly/nx
        while n < N:
            i = n % nx
            j = int(n/nx)
            if j%2 == 0:
                x[n] = (i+0.25)*ax
            else:
                x[n] = (i+0.75)*ax
            y[n] = (0.5+j)*ay
            n += 1
          
    #set up plots
    a = 280/Lx   #disk size in plot
    plt.close()
    fig, axs = plt.subplots(1, 2, figsize=(10, 5*math.sqrt(3)/2))
    for ax, interp in zip(axs, ['Hard disks', 'Radial distribution function']):
        ax.set_title(interp)
        ax.grid(True)  #add grid for plots
        if interp == 'Hard disks':
            dataD, = ax.plot(x,y,'bo',markersize = a)   #data for plots
            ax.set_xlim(0,Lx) 
            ax.set_ylim(0,Ly) 
        else:
            datag, = ax.plot(0,0)   #data for g(r)
            ax.set_xlim(0,Ly/2)
            ax.set_xlabel('r') 
            ax.set_ylabel('g(r)') 
    plt.tight_layout()  #improve appearance of layout
    plt.pause(0.01)   #show initial configuration

#start simulation
    nmcs = 0
    accept = 0
    g = rdfSetup(Lx,Ly)
    Nupdates = 0
    LxGoal = Lx  #used for compressing system
    while keepRunning == 'c':
        for imcs in range(mcs):
            nmcs += 1
            for n in range(N):
                i = int(rnd()*N)
                xnew = pbc(x[i] +(rnd() - 0.5)*ds,Lx) 
                ynew = pbc(y[i] +(rnd() - 0.5)*ds,Ly)
                overlap = False
                for j in range(N):
                    if i != j:
                        dx = sep(xnew-x[j],Lx)
                        dy = sep(ynew-y[j],Ly)
                        if(dx*dx + dy*dy) < 1:
                            overlap = True
                            break
                if not overlap:
                    accept+= 1
                    x[i] = xnew
                    y[i] = ynew
                    
            if LxGoal < Lx:
                a,x,y,Lx,Ly,axs = compress(x,y,Lx,Ly,N,LxGoal,axs)
            else:                
                g = rdfUpdate(x,y,g,N,Lx,Ly) 
                Nupdates += 1
            if nmcs % nPlot == 0:
                for ax, interp in zip(axs, ['Hard disks', 'Radial distribution function']):
                    if interp == 'Hard disks':
                        dataD.remove()  #remove old data
                        dataD, = ax.plot(x,y,'bo',markersize = a)  #add new data
                    else:
                        gn = rdfNormalize(g,Lx,Ly,N,Nupdates)
                        datag.remove()
                        datag, = ax.plot(gn[1],gn[0],'go')
                plt.pause(.000001) #needed to visualize data continuously
        print3f('acceptance ratio',accept/(N*nmcs))
        print3f('PA/NkT= ', 1 + 0.5*math.pi*N*gn[0,10]/(Lx*Ly))
        keepRunning = input('Continue/Stop/Reset/New parameters/change Lx (c/s/r/n/cl)')
        if keepRunning == 'r':
            g = rdfSetup(Lx,Ly)
            accept = 0
            Nupdates = 0
            nmcs = 0
            keepRunning = 'c'
        elif keepRunning == 'n':
            mcs = int(input0('MC steps per particle',mcs))
            nPlot = int(input0('nPlot',nPlot))
            ds = 2*float(input0('maximum step size',ds/2))
            keepRunning = 'c'
        elif keepRunning == 'cl':
            LxGoal = float(input0('Lx  (Ly = sqrt(3)Lx/2))',Lx))
            a,x,y,Lx,Ly,axs = compress(x,y,Lx,Ly,N,LxGoal,axs)
            accept = 0
            Nupdates = 0
            nmcs = 0
            g = rdfSetup(Lx,Ly)
            keepRunning = 'c'
             
def compress(x,y,Lx,Ly,N,LxGoal,axs):
    scale = 1
    if LxGoal < Lx:
        minsep = Lx
        for i in range(N-1):
            for j in range(i+1,N):
                dx = sep(x[j] -x[i],Lx)
                dy = sep(y[j] -y[i],Ly)
                r = math.sqrt(dx*dx + dy*dy)
                if r < minsep:
                    minsep = r 
        LxNew = Lx/minsep
        if LxNew < LxGoal:
            scale = Lx/LxGoal
        else: 
            scale = minsep
        Lx = Lx/scale
    else:
        scale = Lx/LxGoal 
        Lx = LxGoal
    Ly = Ly/scale
    for i in range(N):
        x[i] /= scale
        y[i] /= scale
    a = 280/Lx
    for ax, interp in zip(axs, ['Hard disks', 'Radial distribution function']):
        if interp == 'Hard disks':            
            ax.set_xlim(0,Lx) 
            ax.set_ylim(0,Ly) 
        else:           
            ax.set_xlim(0,Ly/2)
    return a,x,y,Lx,Ly,axs 
    
main()