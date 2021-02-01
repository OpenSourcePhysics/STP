"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description: Calculation of the Second Virial Coefficient for the Lenarrd-Jones Potential 
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    plt.close()
    plt.xlabel('T')
    plt.ylabel('B_2')
    plt.title('Second Virial Coefficient')
    runAgain = 'y'
    while runAgain == 'y':
        Tmin = float(input0('Tmin',1.0))
        Tmax = float(input0('Tmax',25))
        numberOfPoints = 400
        dT = (Tmax-Tmin)/numberOfPoints   
        T = Tmin
        #set axes limits
        ymax = simpson(Tmax)
        ymin = simpson(Tmin)
        if ymax > 0:
            ymax = 1.1*ymax
        else:
            ymax = 0.9*ymax
        if ymin > 0:
            ymin = 0.9*ymin
        else:
            ymin = 1.1*ymin
 
        plt.ylim(ymin,ymax)
        plt.xlim(Tmin,Tmax)
        while T <= Tmax:
            B2 = simpson(T)
            plt.plot(T,B2,'bo')
            T += dT
        plt.pause(0.01)
        runAgain = input('Continue? (y/n)')
                             

def simpson(T):
    n = 2000
    ymax = 10
    dy = ymax/(2*n)
    y = 0.000001
    sum = evaluate(y,T)
    for i in range(0,n-1):
        y = y+dy
        sum = sum + 4*evaluate(y,T)
        y = y+dy
        sum = sum + 2*evaluate(y,T)
    sum = sum +evaluate(ymax,T)
    return sum*dy/3


def evaluate(x,T):
    r2 = x*x;
    oneOverR2 = 1.0/r2;
    oneOverR6 = oneOverR2*oneOverR2*oneOverR2
    z = math.exp(-(1/T)*4.0*(oneOverR6*oneOverR6-oneOverR6))
    return r2*2*math.pi*(1-z)

main()