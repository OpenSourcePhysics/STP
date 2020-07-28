
"""
Created on thursday August 22, 2019
@author: Jan Tobochnik
Illustration of Multiplicative Process
"""

import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
import math


#input parameters
ntrials = int(input0('number of trials',100))   #number of trials
N = int(input0('N',4))  #number of points multiplied for each product
p = float(input0('probability of choosing x1',0.5))   
x1 = float(input0('x1',2.0))   
x2 = float(input0('x2',0.5))   
nPlot = int(input0('nPlot',10))  #number of trials between plots. Make larger to speed up

#set up axes for plot
plt.close()
plt.xlabel('ln(Product)')
plt.ylabel('Occurences')
plt.title('Histogram of ln(Product)')

pcum = 0
p2cum = 0
trials = 0
hist = np.zeros(N+1)  
pvalues = np.zeros(N+1)
prod = x1**N
n = 0
pvalues[n] = math.log(prod)
for i in range(N):
    n += 1
    prod = prod*x2/x1
    pvalues[n] = math.log(prod) 

#start simulation
keepRunning = 'y'
while keepRunning == 'y':
    for itrial in range(ntrials): 
        product = 1 
        for i in range(N):
            if rnd() < p:
                product *= x1
            else:
                product *= x2
        pcum += product
        p2cum += product*product
        trials += 1
        lnprod = math.log(product)
        for n in range(N+1):
            if abs(lnprod-pvalues[n]) < 0.001:
                hist[n] += 1
        if trials % nPlot == 0: 
            plt.bar(pvalues,hist)
            plt.pause(.000001) #needed to visualize data continuously    
    printi('total number of trials',trials)
    print3f('<product>',pcum/(trials))
    print3f('<product*product>',p2cum/(trials))
    print3f('sigma',math.sqrt(p2cum/(trials) - (pcum/(trials))**2))
    keepRunning = input('Continue? (y/n)')
