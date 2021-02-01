"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description:
     Monte Carlo calculation of an integral
    """
import numpy as np
from numpy.random import random as rnd
import math
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    #set up plot
    plt.close()
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('Monte Carlo Estimation')
    plt.xlim(0,1)
    plt.ylim(0,1)

    n = 101
    dx = 1.0/(n-1)
    x = np.zeros(n)
    y = np.zeros(n)
    for i in range(n):
       x[i] = i*dx
       y[i] = evaluate(x[i])
    plt.plot(x,y)  #draw function on plot
    dataB, = plt.plot(0,0,'bo')  #data below curve colored blue
    dataR, = plt.plot(0,0,'ro')  #data above curve colored red
    
    #do Monte Carlo
    runAgain = 'y'
    while runAgain == 'y':
        dataB.remove()
        dataR.remove()
        integral = 0
        N = int(input0('number of points',100))
        xpos = rnd(N)  #N random points
        ypos = rnd(N)
        xpR = []
        ypR = []
        xpB = []
        ypB = []
        for i in range(N):
            if ypos[i] < evaluate(xpos[i]):  #random point below curve
                integral += 1
                xpB.append(xpos[i])
                ypB.append(ypos[i])
            else:
                xpR.append(xpos[i])
                ypR.append(ypos[i])
        dataB, = plt.plot(xpB,ypB,'bo')
        dataR, = plt.plot(xpR,ypR,'ro')
        plt.pause(0.01)
        print3f('integral',integral/N)
        runAgain = input('Continue? (y/n)')

def evaluate(x):
    return math.sqrt(1-x*x)

main()