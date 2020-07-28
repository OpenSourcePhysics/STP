"""
Created : Fri Aug  23  2019
Author: Jan Tobochnik 
Description:  Computes chemical potential from quantum ideal gas integrals
    """
import numpy as np
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    plt.close('all')
    fig, axs = plt.subplots(1, 2, figsize=(10, 4))
    for ax, interp in zip(axs, ['chemical potential', 'energy per particle']):
        ax.set_title(interp.capitalize())
        ax.grid(True)  #add grid for plots
        if interp == 'chemical potential':
            ax.set_xlabel('T')
            ax.set_ylabel('\u03bc')
        else:
            ax.set_xlabel('T')
            ax.set_ylabel('E/N')
    plt.tight_layout()  #improve appearance of layout  
      
    n = 2000  #number of points to compute integrals
    statistics = input0('statistics: bose or fermi','bose')
    newT = 'y'
    while newT == 'y':
        newMu = 'n'
        newT = input0('T or type d to leave program', 2.0)
        if newT != 'd':
            T = float(newT)
            while newMu != 'y':
                newMu = input0('mu(negative for bosons) or if done type y', -2.0)
                if newMu != 'y':
                    mu = float(newMu)
                    if statistics == 'bose':
                        mu = -abs(mu)
                    rhs = simpsonQM(T,mu,n,statistics)
                    print3f2('rhs = ',rhs,"  mu", mu)
                else:
                    newT = 'y'
                    for ax, interp in zip(axs, ['chemical potential', 'energy per particle']):
                        if interp == 'chemical potential':
                            ax.plot(T,mu,'b.')
                        else:
                            ax.plot(T, calculateE(T,mu,n,statistics),'b.')
                    plt.pause(0.01)  #needed to visualize data continuously 
             
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
        if mu == 0:
            return 0.77*(T**2.5)
        c = -1
        a = 0.432
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