"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description: Calculation of Entropy for two Einstein solids in thermal contact 
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    plt.close()
    runAgain = 'y'
    maxNumerator = 20
    while runAgain == 'y':
        E = int(input0('E',200))
        Na = int(input0('Na',50))
        Nb = int(input0('Na',50))
        Sa =np.zeros(E+1)
        Sb =np.zeros(E+1)
        Stotal =np.zeros(E+1)
        xAxis = np.arange(E+1)
        for i in range(E+1):
            Ea = i
            Eb = E-Ea
            if (Ea+Na-1)>maxNumerator:
                log_omegaA = stirling(Ea+Na-1)-stirling(Ea)-stirling(Na-1)
            else:
                log_omegaA = math.log(binom(Ea+Na-1,Ea))
            if (Eb+Nb-1)>maxNumerator:
                log_omegaB = stirling(Eb+Nb-1)-stirling(Eb)-stirling(Nb-1)
            else:
                log_omegaB = math.log(binom(Eb+Nb-1,Eb))
            Sa[i] = log_omegaA
            Sb[i] = log_omegaB
            Stotal[i] = log_omegaA + log_omegaB
        plt.clf()
        plt.xlabel('Ea')
        plt.ylabel('Entropy')
        plt.xlim(0,E)
        plt.title('Entropy')
        plt.plot(xAxis,Sa,'bo')
        plt.plot(xAxis,Sb,'go')
        plt.plot(xAxis,Stotal,'ro')
        plt.pause(0.01)
        runAgain = input('Continue? (y/n)')
                             

def binom(N,n):
    product = 1.0
    i = N
    j = n
    while (i>=N-n+1) and (j>=1):
        product *= i
        product /= j
        j-= 1
        i-= 1
    return product

def stirling(x):
    if x == 0:
        return 0
    else:
        return x*math.log(x) - x + 0.5*math.log(2*math.pi*x)

main()