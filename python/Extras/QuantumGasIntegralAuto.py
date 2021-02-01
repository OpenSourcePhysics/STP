"""
Created : Fri Aug  9 10:34:39 2019
Author: Jan Tobochnik 
 Description:
    """
import numpy as np
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    plt.close('all')
    n = 2000
    statistics = input0('statistics: bose or fermi','bose')
    mu1 = 0.7
    mu2 = 1.0
    T = 0.1
    if statistics == 'bose':
        T = 1.1
        mu1 = -1
        mu2 = -0.01
    dT = 0.05
    Tmax = 5
    I2 = 10
    I1 = 0
    dmu = 100
    while T < Tmax:    
        while (I2-I1) > 0.001:
            I1 = simpsonQM(T,mu1,n,statistics)
            I2 = simpsonQM(T,mu2,n,statistics)
            muMid = 0.5*(mu1+mu2)
            Imid = simpsonQM(T,muMid,n,statistics)
            if Imid > 1:
                mu2 = muMid
            else:
                mu1 = muMid
        mu = 0.5*(mu1+mu2)
        plt.figure(1)
        data1,=plt.plot(T,mu,'b.')
        plt.title('Chemical Potential')
        plt.xlabel('T')
        plt.ylabel('\u03bc')
        plt.draw()
        plt.pause(0.01)
        plt.figure(2)
        data2,=plt.plot(T, calculateE(T,mu,n,statistics),'b.')
        plt.title('Energy')
        plt.xlabel('T')
        plt.ylabel('E/N')
        plt.draw()
        plt.pause(0.01)
        T = T + dT
        mu2 = mu
        mu1 = mu-dmu
        I1 = simpsonQM(T,mu1,n,statistics)
        I2 = simpsonQM(T,mu2,n,statistics)
            

def simpsonQM(T,mu,n,statistics):
    c = 0
    a = 0
    if statistics == 'fermi':
        c = 1
        a = 1.5
    elif statistics == 'bose':
        c = -1
        a = 0.432
    ymax = 100*T
    dy = ymax/(2*n)
    y = 0
    sum = 0
    for i in range(0,n-1):
        y = y+dy
        sum = sum + 4*np.sqrt(y)/(np.exp((y-mu)/T)+c)
        y = y+dy
        sum = sum + 2*np.sqrt(y)/(np.exp((y-mu)/T)+c)
    sum = sum +np.sqrt(ymax)/(np.exp((ymax-mu)/T)+c)
    return a*sum*dy/3


def calculateE(T,mu,n,statistics):
    c = 0
    a = 0
    if statistics == 'fermi':
        c = 1
        a = 1.5
    elif statistics == 'bose':
        c = -1
        a = 0.432
        if mu == 0:
            return 0.77*T^(2.5)
    ymax = 100*T
    dy = ymax/(2*n)
    y = 0
    sum = 0
    for i in range(0,n-1):
        y = y+dy
        sum = sum + 4*np.sqrt(y*y*y)/(np.exp((y-mu)/T)+c)
        y = y+dy
        sum = sum + 2*np.sqrt(y*y*y)/(np.exp((y-mu)/T)+c)
    sum = sum +np.sqrt(ymax*ymax*ymax)/(np.exp((ymax-mu)/T)+c)
    return a*sum*dy/3

main()