"""
Created : Created June 16, 2020
Author: Jan Tobochnik 
Description: Einstein Solid using Metropolis MC algorithm
    """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd
import math
from myFunctions import *


#input parameters
mcs = int(input0('mcs',10000))  #number of MC steps per particle
N = int(input0('N',20)) #number of oscillators 
E = 20 #initial number of energy quanta
T = float(input0('T',2)) # temperature
nplot = mcs/10

# set up plots
plt.close('all')
fig, axs = plt.subplots(1, 3, figsize=(15, 4))
for ax, interp in zip(axs, ['Energy per oscillator', 'Specific Heat', 'Energy vs time']):
    ax.set_title(interp.capitalize())
    if interp == 'Energy per oscillator':
        ax.set_xlabel('T')
        ax.set_ylabel('E/N')
    elif interp == 'Specific Heat':
        ax.set_xlabel('T')
        ax.set_ylabel('c')
    else:
        ax.grid(True)  #add grid for plots
        ax.set_xlabel('time (mcs)')
plt.tight_layout()  #improve appearance of layout
plotData = False

#start off with energy randomly distributed
oscillators = np.zeros(N) 
for q in range(E):  # add energy quanta to oscillators
    n = int(rnd()*N)
    oscillators[n] += 1

#variables to accumulate data for averaging
def reset(cum):
    cum['E'] = 0  
    cum['E2'] = 0 
    cum['accept'] = 0 
    cum['norm'] = 0
    return cum   
cum = {}   #set up dictionary (hash table)
cum = reset(cum)
c = 0
Tplot = 0
Eave = 0
#start simulation
nmcs = 0  #MC time 
keepRunning = 'c'
while keepRunning == 'c':            
    for imcs in range(0,mcs):
        nmcs += 1
        for i in range(N):
            n=int(N*rnd())
            dE = 1
            if(oscillators[n] > 0):
                if (rnd() > 0.5):
                    dE = -1                
            if((dE<0) or (rnd()<math.exp(-dE/T))):
                oscillators[n] += dE
                cum['accept'] += 1
                E = E + dE
            cum['E'] += E
            cum['E2'] += E*E
            cum['norm'] += 1       
        if nmcs % nplot == 0:  #plot data
            plt.pause(0.001)   #needed to see visulaize continuously
            for ax, interp in zip(axs, ['Energy per oscillator', 'Specific Heat', 'Energy vs time']):
                if interp == 'Energy vs time':
                    ax.plot(nmcs,E/N,'b.')
                elif interp == 'Specific Heat':
                        if plotData:
                            ax.plot(Tplot,c,'b.')
                else:
                        if plotData:
                            ax.plot(Tplot,Eave,'b.')
            plotData = False
    printi('Number of MC steps used in average',int(cum['norm']/N))
    print3f('acceptance ratio',cum['accept']/cum['norm'])
    print3f('average energy',cum['E']/(N*cum['norm']))
    print3f('Specific Heat',((cum['E2']/cum['norm'])-(cum['E']/cum['norm'])**2)/(N*T*T))
    keepRunning = input('Continue/Reset/Stop/Plot Data (c/r/s/p)')
    if keepRunning == 'p':
        c = (cum['E2']/cum['norm']- (cum['E']/cum['norm'])**2)/(N*T*T)
        Eave = cum['E']/(cum['norm']*N)
        Tplot = T
        plotData = True
        reset(cum)
        keepRunning = 'c' 
        mcs = int(input0('mcs',mcs))  #number of MC steps per particle
        T = float(input0('T',T)) #temperature
    elif keepRunning == 'r':
        reset(cum)
        mcs = int(input0('mcs',mcs))  #number of MC steps per particle
        keepRunning = 'c'
    elif keepRunning == 'c':
        mcs = int(input0('mcs',mcs))  #number of MC steps per particle
    nplot = mcs/10