"""
Created : Thu September 4, 2019
Author: Jan Tobochnik 
 Description: Simulation of 11 particles with Lennard-Jones forces, initially
 zero net force on each particle;
 user adds small perturbation
 """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd
from myFunctions import *
import math

#initialize
N = 11
runTime = float(input0("run time (dt = 0.01)", 5))
plotTime = float(input0("plot time (dt = 0.01)", 0.1))

#set up plots
L = N

plt.close()
data, = plt.plot(0,0)   #data for plots
plt.xlim(0,L)
plt.ylim(0,L)


#initialize
x = np.zeros(N)
y = np.zeros(N)
for n in range(N):
    x[n] = L/2
    y[n] = .5 + n

vx = np.ones(N)  #initially all particles moving with same velocity
vy = np.zeros(N)    
    
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
        if iSteps & nPlot == 0:
            data.remove()
            data, = plt.plot(x,y,'bo',markersize = 20)
            plt.pause(.01) #needed to visualize data continuously
    keepRunning = input('Continue/Stop/Perturb (c/s/p/)')
    if keepRunning == 'p':
        perturbation = float(input0("perturbation strength", 1.00001))
        vx[6] *= perturbation
        keepRunning = 'c'
