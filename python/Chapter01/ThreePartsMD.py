"""
Created : Thu September 4, 2019
Author: Jan Tobochnik 
 Description: Simulation of particles in a divided box 
 using molecular dynamics with Lennard-Jones forces
 3 partitions
    """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd
from myFunctions import *
import math

#initialize
N = int(input0("N", 75))
runTime = float(input0("run time (dt = 0.01)", 10))
plotTime = float(input0("plot time (dt = 0.01)", 0.1))

#set up plots
L = 10*math.sqrt(N)

plt.close()
fig, axs = plt.subplots(1, 2, figsize=(10, 5))
for axp, interp in zip(axs, ['Lennard-Jones Particles', 'nLeft,nCenter,nRight']):
    axp.set_title(interp)    
    if interp == 'Lennard-Jones Particles':
        dataR, = axp.plot(0,0)   #data for plots
        dataL, = axp.plot(0,0)   #data for plots
        dataC, = axp.plot(0,0)   #data for plots
        axp.axvline(L/3, lw=1)
        axp.axvline(2*L/3, lw=1)
        axp.set_xlim(0,L) 
        axp.set_ylim(0,L) 
    else:
        axp.grid(True)
        dataL, = axp.plot(0,0)   #data for nLeft,nRight
plt.tight_layout()  #improve appearance of layout
plt.pause(0.01)   #show initial configuration

#initialize
x = np.zeros(N)
y = np.zeros(N)
for n in range(N):
    while True:
        x[n] = L/3 + rnd()*L/3    
        y[n] = rnd()*L
        i = 0
        overlap = False
        while  (i < n):
            dx = sep(x[n] -x[i],L)
            dy = sep(y[n] -y[i],L)
            if (dx*dx + dy*dy) < 1:
                overlap = True
                break
            i += 1
        if not overlap:
            break

vx = np.zeros(N)
vy = np.zeros(N)    
tpi = 2.0*math.pi
temperature = 5
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
    
ax,ay,virial = accelerationLJ(x,y,N,L,L,L*L)

dt = 0.01
nSteps = int(runTime/dt)
nPlot = int(plotTime/dt)

#start simulation
keepRunning = 'c'
t = 0
while keepRunning == 'c':
    for iSteps in range(nSteps):
        t += dt
        verlet(x,y,vx,vy,ax,ay,dt,N,L,L,L*L)
        nLeft = 0
        nCenter = 0
        for i in range(N):
            if x[i] < L/3: 
                nLeft += 1
            elif x[i] < 2*L/3:
                nCenter += 1
        nRight = N - nLeft - nCenter
        if iSteps %nPlot == 0:
            xL = []
            xR = []
            xC = []
            yL = []
            yR = []
            yC = []
            for i in range(N):
                if x[i] < L/3: 
                    xL.append(x[i])
                    yL.append(y[i])
                elif x[i] < 2*L/3:
                    xC.append(x[i])
                    yC.append(y[i])
                else:
                    xR.append(x[i])
                    yR.append(y[i])

            for axp, interp in zip(axs, ['Lennard-Jones Particles', 'nLeft,nCenter,nRight']):
                if interp == 'Lennard-Jones Particles':
                    dataR.remove()  #remove old data
                    dataL.remove()  #remove old data
                    dataC.remove()  #remove old data
                    dataL, = axp.plot(xL,yL,'ro')
                    dataR, = axp.plot(xR,yR,'go')
                    dataC, = axp.plot(xC,yC,'bo')
                else:
                    axp.plot(t,nLeft,'ro')
                    axp.plot(t,nRight,'go')
                    axp.plot(t,nCenter,'bo')
            plt.pause(.01) #needed to visualize data continuously
    keepRunning = input('Continue/Stop/Reverse Velocities (c/s/r/)')
    if keepRunning == 'r':
        for i in range(N):
            vx[i] = -vx[i]
            vy[i] = -vy[i]
        keepRunning = 'c'
