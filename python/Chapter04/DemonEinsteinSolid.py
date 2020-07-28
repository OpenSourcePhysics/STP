"""
Created :  Aug 23 2019
Author: Jan Tobochnik 
Description:  Demon Algorithm applied to the Einstein solid
    """
import numpy as np
import matplotlib.pyplot as plt
import random
import math
from numpy.random import random as rnd
from myFunctions import *

#input parameters
mcs = int(input0('mcs',1000)) 
N = int(input0('N',40))   #number of particles
E = int(input0('E',120))   #system energy
nplot = int(input0('time between plots',10))

Ed = 0  # initial demon energy
microstate = np.zeros(N) 
microstate[0] = E  #give all the energy to one oscillator
pBins = E
prob = np.zeros(pBins)  
# set up plots
plt.close('all')
plt.title('Demon Distribution')
plt.grid(True)  #add grid for plots
data, = plt.plot(0,0,'bo') #new data
plt.ylabel('ln p')
plt.xlabel('E')
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
            if rnd() > 0.5:
                dE = 1
            else:
                dE = -1   
            if (Ed >= dE) and (microstate[i] + dE >= 0) :   # accept change
                microstate[i] += dE
                E += dE
                Ed -= dE
                cum['accept'] += 1
            prob[Ed] += 1
            cum['n'] += 1
            cum['E'] += E
            cum['Ed'] += Ed
        if nmcs % nplot == 0:  #plot data    
            data.remove()  #remove old data
            eAxis = []
            lnPAxis = []
            for e in range(pBins):
                if prob[e] > 0:
                    eAxis.append(e)
                    lnPAxis.append(math.log(prob[e]/cum['n']))
            data, = plt.plot(eAxis,lnPAxis,'bo')    
            plt.pause(.000001) 
    printi('Number of MC steps used in average',int(cum['n']/N))
    print3f('acceptance ratio',cum['accept']/cum['n'])
    print3f('average energy',cum['E']/(cum['n']))
    print3f('average demon energy',cum['Ed']/(cum['n']))
    keepRunning = input('Continue/Stop/Reset Axerages? (c/s/r)')
    if keepRunning == 'r':  #reset averages
        reset(cum)
        keepRunning = 'c' 
