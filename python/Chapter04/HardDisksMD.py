
"""
Created on September 3, 2019
@author: Jan Tobochnik
 MD simulation of Hard disk particles in 2D, 
output is g(r), temperature, pressure, velocity histogram, mean squared displacement
and collision time histogram
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
from HDCollision import *

def main():
#input parameters
    N = int(input0('N',64))   #number of particles
    initconf = input0('initial configuration:fluid(random), rectagular, hexangonal (f/r/h)','h')
    Lx = float(input0('Lx  (Ly = sqrt(3)Lx/2))',20))
    dt = float(input0('time step',0.01))
    T = float(input0('Initial Kinetic Energy per particle',1.0))
    Ly = Lx*math.sqrt(3)/2
    nSteps = int(input0("number of collisions", 1000))
    nPlot = int(input0("number of collisions per plot ", 20))
    #for x2 and y2 calculation
    x2plot = np.zeros(20)
    y2plot = np.zeros(20)
    ndxdy = np.zeros(20)
    dx = np.zeros(20)
    dy = np.zeros(20)
    dx2 = np.zeros(20)
    dy2 = np.zeros(20)
    tdxdy = np.zeros(20)
    tmax = 2*Lx/T
    tD = 0
    delt = tmax/20
    for i in range(20):
        tdxdy[i] = (i+0.5)*delt
    
    #for velocity histogram
    scalev = 10
    binWidth = math.sqrt(T)/scalev
    histogram=[]
    xAxis=[]
    nBins = 50
    for i in range(2*nBins+1):
        histogram.append(0)
        xAxis.append((i-nBins)/scalev)
        
 #for collision time histogram
    binwidthC = 0.01
    histogramCol=[]
    xAxisCol=[]
    nBinsC = int(1/binwidthC)
    for i in range(nBinsC):
        histogramCol.append(0)
        xAxisCol.append(i/100)
        
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
    vx = np.zeros(N)
    vy = np.zeros(N)    
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
    KE = 0
    for i in range(N):
        vx[i] -= vxcm
        vy[i] -= vycm 
        KE += vx[i]*vx[i] + vy[i]*vy[i]
    #reset velocities to correct temperature
    KE *= 0.5/N
    scale = math.sqrt(T/KE)        
  #  for i in range(N):
    vx *= scale
    vy *=  scale
        
    #set up plots and initial variables
    a = 280/Lx   #disk size in plot
    plt.close()
    fig, axs = plt.subplots(2,3, figsize=(15, 10*math.sqrt(3)/2))
    def reset(cum,axs,data):
        cum['KE'] = 0  
        cum['virial'] = 0 
        cum['t'] = 0
        cum['nCol'] = 0
        cum['mfp'] = 0
        axs[0,0].clear()
        axs[0,0].set_title('Hard Disks')
        axs[0,0].grid(True)
        axs[0,0].set_xlim(0,Lx) 
        axs[0,0].set_ylim(0,Ly)
        data['D'], = axs[0,0].plot(0,0)
        axs[0,1].clear()
        axs[0,1].set_title('Mean Pressure and Temperature')
        axs[0,1].grid(True)
        axs[0,1].set_xlabel('t') 
        axs[0,1].set_ylabel('T and PA/NkT')
        axs[0,2].clear()
        axs[0,2].set_title('Mean Square Displacement')
        axs[0,2].grid(True)
        axs[0,2].set_xlabel('t') 
        axs[0,2].set_ylabel('x2 and y2')
        data['x2'], = axs[0,0].plot(0,0)
        data['y2'], = axs[0,0].plot(0,0)
        #axs[0,2].set_ylim(0,2*T)
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
        axs[1,1].set_xlabel('vx') 
        axs[1,1].set_ylabel('H(vx)')
        data['h'], = axs[1,1].plot(0,0)
        axs[1,2].clear()
        axs[1,2].set_title('Collision time histogram')
        axs[1,2].grid(True)
        axs[1,2].set_xlabel('t = collision time') 
        axs[1,2].set_ylabel('H(t)')
        data['c'], = axs[1,1].plot(0,0)
 
        plt.tight_layout()  #improve appearance of layout
        return cum,axs,data 
    cum = {}   #set up dictionary (hash table)
    data = {}
    cum,axs,data = reset(cum,axs,data)
    g = rdfSetup(Lx,Ly)
    Nupdates = 0
    collisionTime,partner,timeOfLastCollision,tD,ndxdy,dx,dy,dx2,dy2 = HDinitializeAuxiliaryArrays(N,Lx,Ly,x,y,vx,vy)
    #start simulation
    keepRunning = 'c'
    while keepRunning == 'c':
        for iStep in range(nSteps):
            totalKE = 0
            for n in range(N):
                totalKE += vx[n]*vx[n] + vy[n]*vy[n]
                index = int(scalev*vx[n])+nBins
                if index == nBins:
                    histogram[index] += 0.5
                elif index < 2*nBins + 1 and index >= 0: 
                    histogram[index] += 1
            totalKE *= 0.5
            virial,dt,cum['mfp'],tD  = HDstep(N,x,y,vx,vy,Lx,Ly,collisionTime,partner,timeOfLastCollision,cum['t'],cum['mfp'],tD,tmax,ndxdy,dx,dy,dx2,dy2) 
            #print(dt)
            cum['t'] += dt
            cum['KE'] += totalKE*dt
            cum['virial'] += virial
            g = rdfUpdate(x,y,g,N,Lx,Ly) 
            Nupdates += 1
            cum['nCol'] += 1
            indexC = int(dt*100)
          #  printi("......",indexC)
            if indexC < nBinsC: histogramCol[indexC] += 1
            if iStep % nPlot == 0:
                 meanT = cum['KE']/(cum['t']*N)
                 P = 1 + 0.5*cum['virial']/(meanT*N*cum['t'])
                 data['D'].remove()
                 data['D'], = axs[0,0].plot(x,y,'bo',markersize = a)  #add new data
                 axs[0,1].plot(cum['t'],P,'bo')  #add new data
                 data['h'].remove()
                 data['h'], = axs[1,1].plot(xAxis,histogram, 'bo') 
                 data['c'].remove()
                 data['c'], = axs[1,2].plot(xAxisCol,histogramCol, 'bo')                  
                   
                 Temp = 0
                 for n in range(N):
                     Temp += vx[n]*vx[n] + vy[n]*vy[n]
                 Temp *= 0.5/N
                 axs[0,1].plot(cum['t'],Temp,'ro')  #add new data
                 for i in range(20):
                     x2plot[i] = dx2[i]/(ndxdy[i]*N)
                     y2plot[i] = dy2[i]/(ndxdy[i]*N)
                 data['x2'].remove()
                 data['y2'].remove()
                 data['x2'], = axs[0,2].plot(tdxdy,x2plot,'go')
                 data['y2'], = axs[0,2].plot(tdxdy,y2plot,'ro')
  
                 gn = rdfNormalize(g,Lx,Ly,N,Nupdates)
                 data['g'].remove()
                 data['g'], = axs[1,0].plot(gn[1],gn[0],'go')
                 plt.pause(.000001) #needed to visualize data continuously
        meanT = cum['KE']/(cum['t']*N)
        print3f('density = N/A',N/(Lx*Ly))
        print3f('mean temperature',meanT)
        print3f('PA/NkT ', 1 + 0.5*cum['virial']/(meanT*N*cum['t']))
        print3f('Mean collision time',N*cum['t']/cum['nCol'])
        print3f('Mean free path',0.5*cum['mfp']/cum['nCol'])
        x2t = 0;
        t2 = 0;
        y2t = 0;
        for i in range(20):
            if(ndxdy[i] > 0):
                t = (i+0.5)*delt
                t2 += t*t
                x2t += t*dx2[i]/(ndxdy[i]*N)
                y2t += t*dy2[i]/(ndxdy[i]*N)
        print3f("Diffusion constant D from x2",0.5*x2t/t2);
        print3f("Diffusion constant D from y2",0.5*y2t/t2);
        keepRunning = input('Continue/Stop/Reset/(c/s/r)')
        if keepRunning == 'r':
            g = rdfSetup(Lx,Ly)
            Nupdates = 0
            cum,axs,data= reset(cum,axs,data)
            keepRunning = 'c'
        if keepRunning != 's':
            nSteps = int(input0("number of collisions", nSteps))
            nPlot = int(input0("number of collisions per plot ", nPlot))
          
 
    
main()