
"""
Created on September 3, 2019
@author: Jan Tobochnik
Metropolis MD simulation of Lennard-Jones particles in 2D, 
output is g(r), temperature, pressure, velocity histogram, velocity autocorrelation
function, and mean square displacement, 
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
from LJ2d import *

def main():
#input parameters
    N = int(input0('N',64))   #number of particles
    initconf = input0('initial configuration:fluid(random), rectagular, hexangonal (f/r/h)','h')
    Lx = float(input0('Lx  (Ly = sqrt(3)Lx/2))',20))
    dt = float(input0('time step',0.01))
    T = float(input0('Initial Kinetic Energy per particlee',1.0))
    Ly = Lx*math.sqrt(3)/2
    runTime = float(input0("run time (dt = 0.01)", 10))
    plotTime = float(input0("plot time (dt = 0.01)", 0.1))
    tmaxr2 = float(input0("max time for x^2 and y^2", 5))
    tmaxvaf = float(input0("max time for vaf", 5))
    nSteps = int(runTime/dt)
    nPlot = int(plotTime/dt) 
    #for x2 and y2 plots
    nStepsr2 = int(tmaxr2/dt)
    x2sum = np.zeros(nStepsr2)
    y2sum = np.zeros(nStepsr2)
    x2ave = np.zeros(nStepsr2)
    y2ave = np.zeros(nStepsr2)
    tr2 = np.zeros(nStepsr2)
    for i in range(nStepsr2):
        tr2[i] = i*dt
        
    dx = np.zeros(N)
    dy = np.zeros(N)
    #for velocity autocorrelation plots
    nStepsvaf = int(tmaxvaf/dt)
    cxsum = np.zeros(nStepsvaf)
    cysum = np.zeros(nStepsvaf)
    cxave = np.zeros(nStepsvaf)
    cyave = np.zeros(nStepsvaf)
    tvaf = np.zeros(nStepsvaf)
    for i in range(nStepsvaf):
        tvaf[i] = i*dt
 
     
    #for velocity histogram
    binWidth = math.sqrt(T)/100
    nBins = int(3/binWidth)
    histogram=np.zeros(2*nBins+1)
 
    x = np.zeros(N)
    y = np.zeros(N)
    nx = int(0.01 + math.sqrt(N))
    if nx*nx < N:
        nx += 1
    if initconf == 'f':
        for n in range(N):
            x[n] = rnd()*Lx
            y[n] = rnd()*Ly
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
    vx = np.zeros(N)
    vy = np.zeros(N) 
    vx0 = np.zeros(N)
    vy0 = np.zeros(N)
    tpi = 2.0*math.pi
    temperature = T
    for i in range(N):
        r1 = rnd()
        r2 = rnd()*tpi;
        vx[i] = math.sqrt(-2*temperature*math.log(r1))*math.cos(r2) #sets random initial velocities
        vy[i] = math.sqrt(-2*temperature*math.log(r1))*math.sin(r2)
    #zero Momentum
    vxsum = 0.0;
    vysum = 0.0;
    for i in range(N):
        vxsum += vx[i]
        vysum += vy[i]
    vxcm = vxsum/N
    vycm = vysum/N
    for i in range(N):
        vx[i] -= vxcm
        vy[i] -= vycm
        vx0[i] = vx[i]
        vy0[i] = vy[i]          
    #set up plots and initial variables
    a = 280/Lx   #disk size in plot
    plt.close()
    fig, axs = plt.subplots(3,3, figsize=(15, 10*math.sqrt(3)/2))
    ax,ay,virial = accelerationLJ(x,y,N,Lx,Ly,Lx*Ly)
    def reset(cum,axs,x2sum,y2sum,nStepsr2):
        cum['KE'] = 0  
        cum['KE2'] = 0 
        cum['virial'] = 0 
        cum['n'] = 0
        cum['nr2'] = 0
        cum['nvaf'] = 0
        cum['r2sum'] = 0
        cum['vafsum'] = 0
        x2sum = np.zeros(nStepsr2)
        y2sum = np.zeros(nStepsr2)
        cxsum = np.zeros(nStepsvaf)
        cysum = np.zeros(nStepsvaf)
        axs[0,0].clear()
        axs[0,0].set_title('Lennard-Jones')
        axs[0,0].grid(True)
        axs[0,0].set_xlim(0,Lx) 
        axs[0,0].set_ylim(0,Ly)
        data['D'], = axs[0,0].plot(0,0)
        axs[0,1].clear()
        axs[0,1].set_title('Pressure')
        axs[0,1].grid(True)
        axs[0,1].set_xlabel('t') 
        axs[0,1].set_ylabel('PA/NkT')
        axs[0,2].clear()
        axs[0,2].set_title('Temperature')
        axs[0,2].grid(True)
        axs[0,2].set_xlabel('t') 
        axs[0,2].set_ylabel('T')
        axs[1,0].clear()
        axs[1,0].set_title('Radial distribution function')
        axs[1,0].grid(True)
        axs[1,0].set_xlabel('r') 
        axs[1,0].set_ylabel('g(r)')
        axs[1,0].set_xlim(0,Ly/2)
        data['g'], = axs[1,0].plot(0,0)
        axs[1,1].clear()
        axs[1,1].set_title('Velocity histogram')
        axs[1,1].grid(True)
        axs[1 ,1].set_xlabel('vx') 
        axs[1,1].set_ylabel('H(vx)')
        data['h'], = axs[1,1].plot(0,0)
        axs[1,2].clear()
        axs[1,2].set_title('Mean Square Displacement')
        axs[1,2].grid(True)
        axs[1,2].set_xlabel('t') 
        axs[1,2].set_ylabel('x^2 and y^2')
        data['rx'], = axs[1,2].plot(0,0)
        data['ry'], = axs[1,2].plot(0,0)
        axs[2,1].clear()
        axs[2,1].set_title('Velocity AutoCorrelation Function')
        axs[2,1].grid(True)
        axs[2,1].set_xlabel('t') 
        axs[2,1].set_ylabel('C(t)')
        data['cx'], = axs[2,1].plot(0,0)
        data['cy'], = axs[2,1].plot(0,0)
        plt.tight_layout()  #improve appearance of layout
        return cum,axs,data,x2sum,y2sum  
    cum = {}   #set up dictionary (hash table)
    data = {}
    cum,axs,data,x2sum,y2sum, = reset(cum,axs,x2sum,y2sum,nStepsr2)
    g = rdfSetup(Lx,Ly)
    Nupdates = 0
    t = 0
    scale = 1
    
    #start simulation
    keepRunning = 'c'
    while keepRunning == 'c':
        for iStep in range(nSteps):
            cum['n'] += 1 
            t += dt
            x,y,dx,dy,vx,vy,ax,ay,virial,cum,x2sum,y2sum  = verlet(x,y,dx,dy,vx,vy,ax,ay,dt,N,Lx,Ly,Lx*Ly,cum,x2sum,y2sum) 
            totalKE = 0
            for n in range(N):
                totalKE += vx[n]*vx[n] + vy[n]*vy[n]
                vx[n]*= scale
                vy[n]*= scale
                cxsum[int(cum['nvaf'])] += vx0[n]*vx[n]
                cysum[int(cum['nvaf'])] += vy0[n]*vy[n]
                index = int(100*vx[n])+nBins
                if index >= 0 and index <= 2*nBins+1:
                    if index == nBins:
                        histogram[index] += 0.5
                    elif index < 2*nBins + 1: 
                        histogram[index] += 1
            totalKE *= 0.5
            cum['KE'] += totalKE
            cum['KE2'] += totalKE*totalKE
            cum['virial'] += virial
            g = rdfUpdate(x,y,g,N,Lx,Ly) 
            Nupdates += 1
            cum['nvaf'] += 1
            if cum['n'] % nPlot == 0:
                 meanT = cum['KE']/(cum['n']*N)
                 T = totalKE/(N)
                 meanP = 1 + cum['virial']/(2*meanT*N*cum['n'])
                 P = 1 + virial/(2*T*N)
                 data['D'].remove()
                 data['D'], = axs[0,0].plot(x,y,'bo',markersize = a)  #add new data
                 axs[0,1].plot(t,P,'ro',ms=4)  #add new data
                 axs[0,1].plot(t,meanP,'go',ms=4)  #add new data
                 data['h'].remove()
                 xAxis = []
                 yAxis = []
                 for i in range(2*nBins+1):
                     if histogram[i] > 0:
                         xAxis.append((i-nBins)/100.0)
                         yAxis.append(histogram[i])
                 data['h'], = axs[1,1].plot(xAxis,yAxis, 'bo',ms=4)                  
                 axs[0,2].plot(t,T,'ro',ms=4)  #add new data
                 axs[0,2].plot(t,meanT,'go',ms=4)  #add new data
                 gn = rdfNormalize(g,Lx,Ly,N,Nupdates)
                 data['g'].remove()
                 data['g'], = axs[1,0].plot(gn[1],gn[0],'go',ms=4)
                 plt.pause(.000001) #needed to visualize data continuously
            if cum['n'] % nStepsr2 == 0:
                 cum['r2sum'] += 1
                 x2ave = x2sum/(N*cum['r2sum'])
                 y2ave = y2sum/(N*cum['r2sum'])
                 dx = 0
                 dy = 0
                 data['rx'].remove()
                 data['ry'].remove()
                 data['rx'], = axs[1,2].plot(tr2,x2ave,'ro',ms=4)
                 data['ry'], = axs[1,2].plot(tr2,y2ave,'go', ms=4)
                 cum['nr2'] = 0
            if cum['n'] % nStepsvaf == 0:
                 cum['vafsum'] += 1
                 cxave = cxsum/(N*cum['vafsum'])
                 cyave = cysum/(N*cum['vafsum'])
                 for i in range(N):
                     vx0[i] = vx[i]
                     vy0[i] = vy[i]
                 data['cx'].remove()
                 data['cy'].remove()
                 data['cx'], = axs[2,1].plot(tvaf,cxave,'ro',ms=4)
                 data['cy'], = axs[2,1].plot(tvaf,cyave,'go', ms=4)
                 cum['nvaf'] = 0
        sigma2 = cum['KE2']/cum['n'] - meanT*meanT*N*N
        print3f('density = N/A',N/(Lx*Ly))
        print3f('mean temperature',meanT)
        print3f('PA/NkT', meanP)
        print3f('Heat Capacity',N/(1 - sigma2/(N*T*T)))
        keepRunning = input('Continue/Stop/Reset/change Lx/Veclocity rescale (c/s/r/cl/vr)')
        if keepRunning == 'r':
            g = rdfSetup(Lx,Ly)
            Nupdates = 0
            cum,axs,data,x2sum,y2sum = reset(cum,axs,x2sum,y2sum,nStepsr2)
            t = 0
            keepRunning = 'c'
        elif keepRunning == 'cl':
            LxNew = float(input0('Lx  (Ly = sqrt(3)Lx/2))',Lx))
            a,x,y,Lx,Ly = changeSize(x,y,Lx,Ly,N,LxNew)
            cum,axs,data,x2sum,y2sum = reset(cum,axs,x2sum,y2sum,nStepsr2)
            t = 0
            Nupdates = 0
            g = rdfSetup(Lx,Ly)
            keepRunning = 'c'
        elif keepRunning == 'vr':
            scale = float(input0('Velocity scaling',0.999))
            for n in range(N):
                vx[i] *=scale
                vy[i] *=scale
            cum,axs,data,x2sum,y2sum = reset(cum,axs,x2sum,y2sum,nStepsr2)
            t = 0
            Nupdates = 0
            g = rdfSetup(Lx,Ly)
            keepRunning = 'c'
        if keepRunning != 's':
            runTime = float(input0("run time (dt = 0.01)", 10))
            plotTime = float(input0("plot time (dt = 0.01)", 0.1))
            nSteps = int(runTime/dt)
            nPlot = int(plotTime/dt) 
           
 
    
             
def changeSize(x,y,Lx,Ly,N,LxNew):
    scale = LxNew/Lx
    Lx = LxNew
    Ly = Ly*scale
    for i in range(N):
        x[i] *= scale
        y[i] *= scale
    a = 280/Lx
    return a,x,y,Lx,Ly 
    
main()