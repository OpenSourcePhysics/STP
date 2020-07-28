
"""
Created on thursday August 22, 2019
@author: Jan Tobochnik
1D random walkers with continuous step size. 
"""

import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *


#input parameters
nWalk = int(input0('nWalk',2000))   #number of walkers
nSteps = int(input0('nSteps',16))  #number of steps
p = float(input0('p',0.5))   #probability of stepping to the right
nPlot = int(input0('nPlot',20))  #number of walks between plots. Make larger to speed up
binWidth = float(input0('bin width', 0.5))  # bin width for histogram

#set up axes for plot
plt.close()
plt.xlim(-nSteps,nSteps) #set x axis limit
plt.xlabel('x')
plt.ylabel('H(x)')

#initialize arrays for histogram
histogram=[]
xAxis=[]
nBins = int(nSteps/binWidth)
step = -nSteps
for x in range(2*nBins+1):
    histogram.append(0)
    xAxis.append(step)
    step = step + binWidth 
data, = plt.plot(xAxis,histogram,'bo')   #data for plots
xcum = 0
x2cum = 0
trials = 0

#start simulation
keepRunning = 'y'
while keepRunning == 'y':
    for i in range(nWalk): 
        pos = 0  # start new walker
        for t in range(nSteps):
            if rnd()<p:
                pos=pos+rnd() 
            else:
                pos=pos-rnd()
        index = int((pos+nSteps)/binWidth)
        histogram[index] = histogram[index]+1
        xcum += pos
        x2cum += pos*pos
        trials += 1
        if i % nPlot == 0:
            data.remove()  #remove old data
            data, = plt.plot(xAxis,histogram,'bo') #add new data
            plt.pause(.000001) #needed to visualize data continuously    
    printi('total number of walkers',trials)
    print3f('<x>',xcum/trials)
    print3f('<x^2>',x2cum/trials)
    keepRunning = input('Continue? (y/n)')
