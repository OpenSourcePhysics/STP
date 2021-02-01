
"""
Created on thursday August 22, 2019
@author: Jan Tobochnik
2D random walkers
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *


#input parameters
nWalk = int(input0('nWalk',2000))   #number of walkers
nSteps = int(input0('nSteps',200))  #number of steps
pR = float(input0('pRight',0.25))   #probability of stepping to the right
pL = float(input0('pLeft',0.25))   #probability of stepping to the left
pU = float(input0('pUp',0.25))   #probability of stepping up
nPlot = int(input0('nPlot',2))  #number of walks between plots. Make larger to speed up
nBins = int(input0('maximum value of r plotted', 50))  

#set up plots
pos = np.zeros((2,nWalk))
histogram=[]
xAxis=[]
for x in range(nBins):
    histogram.append(0)
    xAxis.append(x)
plt.close()
fig, axs = plt.subplots(1, 3, figsize=(15, 4))
for ax, interp in zip(axs, ['walkers', 'x**2 and y**2', 'H(r)']):
    ax.set_title(interp.capitalize())
    ax.grid(True)  #add grid for plots
    if interp == 'H(r)':
        dataH, = ax.plot(xAxis,histogram,'bo')   #data for plots
        ax.set_xlabel('r')
        ax.set_ylabel('H(r)')
    elif interp =='walkers':
        dataW, = ax.plot(pos[0],pos[1],'bo')   #data for plots
        ax.set_xlim(-nSteps,nSteps) 
        ax.set_ylim(-nSteps,nSteps) 
    else:
        ax.set_xlabel('time') 
       
plt.tight_layout()  #improve appearance of layout

t = 0
#start simulation
keepRunning = 'y'
while keepRunning == 'y':
    for isteps in range(nSteps): 
        t  += 1
        xcum = 0
        x2cum = 0
        ycum = 0
        y2cum = 0
        for i in range(nWalk):
            rand = rnd()
            if rand<pR:
                pos[0,i] += 1 
            elif rand<(pR+pL):
                pos[0,i] -= 1 
            elif rand < (pR+pL+pU):
                pos[1,i] += 1
            else:
                pos[1,i] -= 1
            r = np.sqrt(pos[0,i]**2 + pos[1,i]**2)
            index = int(r)
            if r < nBins:
                histogram[index] += 1
            xcum += pos[0,i]
            x2cum += pos[0,i]**2
            ycum += pos[1,i]
            y2cum += pos[1,i]**2
        if t % nPlot == 0:
            for ax, interp in zip(axs, ['walkers', 'x**2 and y**2', 'H(r)']):
                if interp == 'H(r)':
                    dataH.remove()  #remove old data
                    dataH, = ax.plot(xAxis,histogram,'bo') #add new data
                elif interp == 'walkers':
                    dataW.remove()  #remove old data
                    dataW, = ax.plot(pos[0],pos[1],'bo') #add new data
                else:
                    ax.plot(t,x2cum/nWalk,'bo')
                    ax.plot(t,y2cum/nWalk,'ro')
            plt.pause(.000001) #needed to visualize data continuously    
    keepRunning = input('Continue? (y/n)')
