
"""
Created on thursday August 22, 2019
@author: Jan Tobochnik
Illustration of central limit theorem
P(y) normalized so that integral P(y)dy = 1
"""

import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
import math


#input parameters
ntrials = int(input0('number of trials',100000))   #number of trials
n = int(input0('n',12))  #number of data points summed for each value of S
distribution = input0('distribution:uniform/exponential/lorentz (u/e/l)','u')   
nPlot = int(input0('nPlot',500))  #number of trials between plots. Make larger to speed up


#initialize arrays for histogram
histogram=[]
xAxis=[]
ds = 0.03/(math.sqrt(n))
if distribution == 'u':
    smin = 0
    smax = 1
elif distribution == 'e':
    smin = 0
    smax = 5
else:
    smin = -15
    smax = 15
s = smin
while s < smax:
    histogram.append(0)
    xAxis.append(s)
    s += ds 

#set up axes for plot
plt.close()
plt.xlabel('y = S/n')
plt.ylabel('H(y)')
plt.xlim(smin,smax)
data, = plt.plot(0,0)


xcum = 0
x2cum = 0
ycum = 0
y2cum = 0
trials = 0

#start simulation
keepRunning = 'y'
while keepRunning == 'y':
    for itrial in range(ntrials): 
        S = 0  
        for i in range(n):
            if distribution == 'u':
                x = rnd()
            elif distribution == 'e':
                x = -math.log(1.0 - rnd())
            else:
                x = math.tan(math.pi*rnd())
            xcum += x
            x2cum += x*x
            S += x
        ycum += S/n
        y2cum += (S/n)**2
        index = int((-smin+S/n)/ds)
        if index >= 0 and index  < len(histogram):
            histogram[index] += 1
        trials += 1
        if itrial % nPlot == 0:
            data.remove()  #remove old data
            yAxis = []
            ymax = 0
            for i in range(len(histogram)):
                yAxis.append(histogram[i]/(ds*trials))
                if yAxis[i] > ymax:
                    ymax = yAxis[i]
            plt.ylim(0,1.1*ymax)   #rescale y axis
            data, = plt.plot(xAxis,yAxis,'b') #new data
            plt.pause(.000001) #needed to visualize data continuously    
    printi('total number of trials',trials)
    print3f('<x>',xcum/(n*trials))
    print3f('variance of x',x2cum/(n*trials) - (xcum/(n*trials))**2)
    print3f('<y>',ycum/trials)
    printf('sample variance of y = S/n',y2cum/trials - (ycum/trials)**2,4)
    keepRunning = input('Continue? (y/n)')
