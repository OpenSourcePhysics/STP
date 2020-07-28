"""
Created :  Aug 25 2019
Author: Jan Tobochnik 
Description:  Multiple Coin toss simulation
    """
import numpy as np
import matplotlib.pyplot as plt
import random
import math
from numpy.random import random as rnd
from myFunctions import *

#input parameters
N = int(input0('number of coins',100))   #number of coins
p = float(input0('probability of heads',0.5))
ntrial = int(input0('number of trials',100))
nplot = int(input0('time between plots',10))
histogram = np.zeros(N+1)
x = [] 
for n in range(N+1):
    x.append(n)
 
# set up plots
plt.close('all')
plt.title('Probability of n heads for ' + str(N) +' coins flipped')
plt.grid(True)  #add grid for plots
data, = plt.plot(0,0,'bo') #new data
plt.ylabel('P(n)')
plt.xlabel('n')

#start simulation
ncum = 0
Hcum = 0
H2cum = 0
keepRunning = 'c'
while keepRunning == 'c':
    for itrial in range(ntrial):
        nheads = 0
        for n in range(N): 
            if rnd() > 0.5:
                nheads += 1
        histogram[nheads] += 1
        Hcum += nheads
        H2cum += nheads**2
        ncum += 1
        if itrial % nplot == 0:  #plot data 
             data.remove()
             data, = plt.plot(x,histogram,'bo')    
             plt.pause(.000001) 
    printi('Number of trials used in average',ncum)
    print3f('<H>',Hcum/ncum)
    print3f('<H*H>',H2cum/ncum)
    print3f('sigma',H2cum/ncum - (Hcum/ncum)**2)
    keepRunning = input('Continue/Stop (c/s)')
