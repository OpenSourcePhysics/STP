"""
Created : Mon Aug 12 09:39:18 2019
Author: Jan Tobochnik 
 Description:
     Ideal thermometer: uses demon algorithm with ideal gas particles
    """
import numpy as np
import matplotlib.pyplot as plt
import random
import math
from numpy.random import random as rnd
from myFunctions import *

#input parameters
mcs = int(input0('mcs',400)) 
N = int(input0('N',40))   #number of particles
D = int(input0('dimension',3))  #dimension of space
E = float(input0('E',40))   #system energy
exponent = float(input0('momentum exponent',2))
nplot = int(input0('time between plots',10))

Ed = 0  # initial demon energy
v = np.zeros((N,D)) 
vnew = np.zeros(D)
vsmax = 2*np.sqrt(2*E/(D*N))
dvs = vsmax
for i in range(N):
    for d in range(D):
        v[i,d] = vsmax*(rnd() - 0.5)
nBins = int(25*vsmax)
bin0 = int(nBins/2)
histogram = np.zeros(nBins)
vAxis = np.arange(-bin0/10,bin0/10,0.1)
pBins = 40
prob = np.zeros(pBins)  
# set up plots
plt.close('all')
fig, axs = plt.subplots(1, 2, figsize=(10, 4))
for ax, interp in zip(axs, ['Demon Distribution', 'Velocity histogram']):
    ax.set_title(interp)
    ax.grid(True)  #add grid for plots
    if interp == 'Velocity histogram':
        ax.set_xlim(-bin0/10,bin0/10)
        datap, = ax.plot(0,0,'bo') #new data
        ax.set_xlabel('vx,vy, etc')
    else:
        datav, = ax.plot(0,0,'bo') #new data
        ax.set_ylabel('ln p')
        ax.set_xlabel('E')
plt.tight_layout()  #improve appearance of layout
def reset(cum):
    cum['n'] = 0  
    cum['E'] = 0 
    cum['Ed'] = 0 
    cum['accept'] = 0 
    return cum   
cum = {}   #set up dictionary (hash table)
reset(cum)

#start simulation
nmcs = 0
keepRunning = 'c'
while keepRunning == 'c':
    for imcs in range(mcs):
        nmcs += 1
        for n in range(N): 
            i =  np.random.randint(0,N)
            dE = 0
            for  d in range(D):
                vnew[d] = v[i,d] + (rnd()-0.5)*dvs
                dE = dE + 0.5*(vnew[d]**exponent - v[i,d]**exponent)
            if (dE <= 0) or (Ed >= dE):   # accept change
                v[i] = vnew
                E += dE
                Ed -= dE
                cum['accept'] += 1
            if 20*Ed < pBins:
                prob[int(20*Ed)] += 1
            for d in range(D):
                index = int(10*v[i,d])+bin0
                if index == bin0:
                    histogram[index] += 0.5
                else:
                    histogram[index] += 1
            cum['n'] += 1
            cum['E'] += E
            cum['Ed'] += Ed
        if nmcs % nplot == 0:  #plot data    
            for ax, interp in zip(axs, ['Demon Distribution', 'Velocity histogram']):
                if interp == 'Velocity histogram':
                    datav.remove()  #remove old data
                    datav, = ax.plot(vAxis,histogram,'bo') #new data
                else:
                    datap.remove()  #remove old data
                    eAxis = []
                    lnPAxis = []
                    for e in range(pBins):
                        if prob[e] > 0:
                            eAxis.append(e/20.0)
                            lnPAxis.append(math.log(prob[e]/cum['n']))
                    datap, = ax.plot(eAxis,lnPAxis,'bo')    
            plt.pause(.000001) 
    printi('Number of MC steps used in average',int(cum['n']/N))
    print3f('acceptance ratio',cum['accept']/cum['n'])
    print3f('average energy',cum['E']/(cum['n']))
    print3f('average demon energy',cum['Ed']/(cum['n']))
    keepRunning = input('Continue/Stop/Reset Axerages? (c/s/r)')
    if keepRunning == 'r':  #reset averages
        reset(cum)
        keepRunning = 'c' 
