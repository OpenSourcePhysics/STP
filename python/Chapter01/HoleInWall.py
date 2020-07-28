"""
Created : Thu Aug  8 10:23:00 2019
Author: Jan Tobochnik 
 Description: Simulation of particles in a divided box, dynamics
 is equivalent to randomly picking a particles and moving to other side
    """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd
from myFunctions import *

#initialize
N = int(input0("N = ", 64))
nSteps = int(input0("nSteps = ", 200))
nLeft = N
time = 0
nLeftSum = 0
nLeft2Sum = 0
timeSum = 0

#set up plots
plt.close('all')
plt.xlabel('time')
plt.ylabel('n')

#start simulation
keepRunning = 'c'
while keepRunning == 'c':
    for istep in range(nSteps):
        plt.plot(time,nLeft,'b.')
        if rnd() <= 1.0*nLeft/N:
            nLeft = nLeft - 1
        else:
            nLeft = nLeft + 1
        nLeftSum = nLeftSum + nLeft
        nLeft2Sum = nLeft2Sum + nLeft*nLeft
        time = time+1
        timeSum = timeSum+1
        plt.pause(0.001)
    printi('Number of steps in averages',timeSum)
    print2f('<nLeft>',nLeftSum/timeSum)
    print2f('<nLeft^2> ',nLeft2Sum/time)
    print2f('sigma^2',nLeft2Sum/timeSum - (nLeftSum/timeSum)**2)
    keepRunning = input('Continue/Stop/Reset Averages? (c/s/r)')
    if keepRunning == "r":
        nLeftSum = 0
        nLeft2Sum = 0
        timeSum = 0
        keepRunning = 'c'