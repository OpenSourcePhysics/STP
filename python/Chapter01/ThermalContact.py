
"""
Created on September 3, 2019
@author: Jan Tobochnik
MD simulation of Lennard-Jones particles in 2D, 
with two kinds of molecules with different sigma and epsilon. User can then
start thermal contact between the two to see kinetic energy per particle becoming the same for the two. 
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *

def main():
#input parameters
    Nyellow = int(input0('Number of yellow particles',81))  
    Nblue = int(input0('Number of blue particles',64))  
    Lx = float(input0('Lx  (Ly = sqrt(3)Lx/2))',12))
    dt = float(input0('time step',0.01))
    Ly = Lx*math.sqrt(3)/2
    runTime = float(input0("run time (dt = 0.01)", 5))
    plotTime = float(input0("plot time (dt = 0.01)", 1))
    nSteps = int(runTime/dt)
    nPlot = int(plotTime/dt) 
    N = Nyellow+Nblue


    x = np.zeros(N)
    y = np.zeros(N)
    x0 = np.zeros(N)
    y0 = np.zeros(N)
    nx = int(0.01 + math.sqrt(Nyellow))
    if nx*nx < Nyellow:
        nx += 1
    n = 0
    ax = Lx/(nx+1.5)
    ay = Ly/(nx+1)
    while n < Nyellow:
        i = n % nx
        j = int(n/nx)
        x[n] = (1 + i +0.5*(j%2))*ax
        y[n] = (1+j)*ay
        n += 1
    nx = int(0.01 + math.sqrt(Nblue))
    if nx*nx < Nblue:
        nx += 1
    n = 0
    ax = Lx/(nx+1.5)
    ay = Ly/(nx+1)
    while n < Nblue:
        i = n % nx
        j = int(n/nx)
        x[n+Nyellow] = Lx + (1 + i +0.5*(j%2))*ax
        y[n+Nyellow] = (1+j)*ay
        n += 1

    for i in range(N):
        x0[i] = x[i]
        y0[i] = y[i]
    
    vx = np.zeros(N)
    vy = np.zeros(N)    
    ax = np.zeros(N)
    ay = np.zeros(N)    
  
    #set up plots and initial variables
    aYellow = 80/Lx   #disk size in plot
    aBlue = 1.2*80/Lx   #disk size in plot
    plt.close()
    fig, axs = plt.subplots(1,3, figsize=(18, 3*math.sqrt(3)/2))
    axs[0].set_title('Two Solids')
    axs[0].set_xlim(0,2*Lx ) 
    axs[0].set_ylim(0,Ly)
    dataline = axs[0].axvline(Lx, lw=1)
    dataY, = axs[0].plot(0,0)
    dataB, = axs[0].plot(0,0)
    axs[1].set_title('Kinetic Energy per particle')
    axs[1].grid(True)
    axs[1].set_xlabel('t') 
    axs[1].set_ylabel('KE')
    axs[2].clear()
    axs[2].set_title('Potential Energy per particle')
    axs[2].grid(True)
    axs[2].set_xlabel('t') 
    axs[2].set_ylabel('PE')
    plt.tight_layout()  #improve appearance of layout
    contact = False

    #start simulation
    keepRunning = 'c'
    t = 0
    while keepRunning == 'c':
        for iStep in range(nSteps):
            PEYellow, PEBlue,KEYellow,KEBlue = mdMove(x,y,x0,y0,vx,vy,ax,ay,Nyellow,Nblue,contact,dt,Lx,Ly) 
            t += dt
            if iStep % nPlot == 0:
                 dataY.remove()
                 dataB.remove()
                 xYellow = []
                 yYellow = []
                 xBlue = []
                 yBlue = []
                 for n in range(Nyellow):
                     xYellow.append(x[n])
                     yYellow.append(y[n])
                 for n in range(Nyellow,N):
                     xBlue.append(x[n])
                     yBlue.append(y[n])
                 dataY, = axs[0].plot(xYellow,yYellow,'yo',markersize = aYellow)  #add new data
                 dataB, = axs[0].plot(xBlue,yBlue,'bo',markersize = aBlue)  #add new data
                 axs[1].plot(t,KEYellow,'yo')  #add new data
                 axs[1].plot(t,KEBlue,'bo')  #add new data
                 axs[2].plot(t,PEYellow,'yo')  #add new data
                 axs[2].plot(t,PEBlue,'bo')  #add new data
                 plt.pause(.000001) #needed to visualize data continuously
                 E = (PEYellow+KEYellow)*Nyellow + (PEBlue+KEBlue)*Nblue
                 print3f('Total energy',E)
        keepRunning = input('Continue/Start Thermal Contact/Stop(c/stc/s)')
        if keepRunning == 'stc':
            contact = True
            dataline.remove()
            keepRunning = 'c'
        if keepRunning != 's':
            runTime = float(input0("run time (dt = 0.01)", 20))
            plotTime = float(input0("plot time (dt = 0.01)", 1))
            nSteps = int(runTime/dt)
            nPlot = int(plotTime/dt) 
           
def mdMove(x,y,x0,y0,vx,vy,ax,ay,Nyellow,Nblue,contact,dt,Lx,Ly):
    N = int(Nyellow + Nblue)
    dt2half = 0.5*dt*dt
    dthalf = 0.5*dt
    for i in range(N):
        x[i] += vx[i]*dt+ax[i]*dt2half
        y[i] += vy[i]*dt+ay[i]*dt2half
        vx[i] += ax[i]*dthalf
        vy[i] += ay[i]*dthalf
    PEYellow,PEBlue,ax,ay = acceleration(x,y,x0,y0,Nyellow,Nblue,contact,Lx,Ly)        
    for i in range(N):
        vx[i] += ax[i]*dthalf
        vy[i] += ay[i]*dthalf
    KEYellow = 0
    for i in range(Nyellow):
        KEYellow += vx[i]*vx[i] + vy[i]*vy[i]
    KEBlue = 0
    for i in range(Nyellow,N):
        KEBlue += vx[i]*vx[i] + vy[i]*vy[i]
    return PEYellow,PEBlue,0.5*KEYellow/Nyellow,0.5*KEBlue/Nblue

def acceleration(x,y,x0,y0,Nyellow,Nblue,contact,Lx,Ly):
    N = Nyellow+Nblue
    ax = np.zeros(N)
    ay = np.zeros(N)
    PEYellow = 0
    PEBlue = 0
    epBB = 1.5
    epYY = 1
    epW = 1
    sigma2YY = 1
    sigma2BB = 1.2*1.2
    epYB = 1.25
    sigma2YB = 1.1*1.1
    sigma2W = 1
    for i in range(Nyellow):
        if x[i] < Lx/2:
            fx,fy,PE = force(x[i],y[i],0,y[i],epW,sigma2W)
        elif not contact:
            fx,fy,PE = force(x[i],y[i],Lx,y[i],epW,sigma2W)
        ax[i] = fx
        PEYellow += PE
        if y[i] < Ly/2:
            fx,fy,PE = force(x[i],y[i],x[i],0,epW,sigma2W)
        else:
            fx,fy,PE = force(x[i],y[i],x[i],Ly,epW,sigma2W)
        ay[i] = fy
        PEYellow += PE
    for i in range(Nyellow,N):
        if x[i] > 3*Lx/2:
            fx,fy,PE = force(x[i],y[i],2*Lx,y[i],epW,sigma2W)
        elif not contact:
            fx,fy,PE = force(x[i],y[i],Lx,y[i],epW,sigma2W)
        ax[i] = fx
        PEBlue += PE
        if y[i] < Ly/2:
            fx,fy,PE = force(x[i],y[i],x[i],0,epW,sigma2W)
        else:
            fx,fy,PE = force(x[i],y[i],x[i],Ly,epW,sigma2W)
        ay[i] = fy
        PEBlue += PE
    for i in range(Nyellow-1):
        for j in range(i+1,Nyellow):
            fx,fy,PE = force(x[i],y[i],x[j],y[j],epYY,sigma2YY)
            ax[i] += fx
            ay[i] += fy
            ax[j] -= fx
            ay[j] -= fy
            PEYellow+= PE
    for i in range(Nyellow,N-1):
        for j in range(i+1,N):
            fx,fy,PE = force(x[i],y[i],x[j],y[j],epBB,sigma2BB)
            ax[i] += fx
            ay[i] += fy
            ax[j] -= fx
            ay[j] -= fy
            PEBlue += PE
    if contact:
        for i in range(Nyellow):
            for j in range(Nyellow,N):
                fx,fy,PE = force(x[i],y[i],x[j],y[j],epYB,sigma2YB)
                ax[i] += fx
                ay[i] += fy
                ax[j] -= fx
                ay[j] -= fy
                PEBlue += 0.5*PE
                PEYellow += 0.5*PE
    return PEYellow/Nyellow,PEBlue/Nblue,ax,ay

def force(x1,y1,x2,y2,ep,sigma2):
    dx = x1-x2
    dy = y1-y2
    r2 = dx*dx+dy*dy;
    r2i = sigma2/r2
    r6i = r2i*r2i*r2i
    fOverR = 48.0*ep*r6i*(r6i-0.5)*r2i
    fx = fOverR*dx
    fy = fOverR*dy
    PE = 4*ep*r6i*(r6i-1)
    return fx,fy,PE
     
    
main()