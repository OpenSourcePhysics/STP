"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description: Calculation of P(Ea) for two Einstein solids in thermal contact 
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    plt.close()
    plt.xlabel('Ea')
    plt.ylabel('P(Ea)')
    plt.title('Probability')
    runAgain = 'y'
    colors = ['bo', 'go', 'ro', 'mo', 'yo', 'ko']  # for plots
    c = 0
    while runAgain == 'y':
        point = colors[c]
        Ea0 = int(input0('Initial Ea',10))
        Eb0 = int(input0('Initial Eb',2))
        Na = int(input0('Na',4))
        Nb = int(input0('Na',4))
        Etotal = Ea0 + Eb0 
        Pa =np.zeros(Etotal+1)
        Pb =np.zeros(Etotal+1)
        numberOfStates = 0
        max = 0
        maxE = -1
        hotToCold = 0
        meanEa = 0
        for Ea in range(Etotal+1):
            Pa[Ea] = binom(Ea+Na-1, Ea)
            Pb[Ea] = binom(Etotal-Ea+Nb-1, Etotal-Ea)
            numberOfStates += Pa[Ea]*Pb[Ea]
        for Ea in range(Etotal+1):
            prob = Pa[Ea]*Pb[Ea]/numberOfStates
            if prob> max:
                max = prob
                maxE = Ea
            if Ea < Ea0:
                hotToCold += prob
            meanEa += prob*Ea
            plt.plot(Ea,prob,point)
        plt.pause(0.01)
        printi('Initial \u03a9a',binom(Ea0+Na-1, Ea0) )
        printi('Initial \u03a9b',binom(Eb0+Nb-1, Eb0) )
        printi('Most probably Ea',maxE )
        print3f('<Ea>',meanEa )
        printf('Probability of a to b', hotToCold,4)
        c += 1
        c = c % 6
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

main()