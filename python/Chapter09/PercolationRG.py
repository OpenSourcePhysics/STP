"""
Created : Thu Aug 22 08:59:02 2019
Author: Jan Tobochnik 
Description: RG illustration for percolation model
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *

cmap12 = ListedColormap(['w', 'b'])  #site colors
cmap1 = ListedColormap(['b'])  #site color for filled in lattice
runAgain = 'a'
L = 64  #first default value
p = 0.5927 #first default value
while runAgain == 'a':
#input parameters
    L = int(input0('L (multiple of 2)',L)) #length of the lattice 
    N = 1 + int(math.log(L,2))  #number of lattices
    p = float(input0('p',p)) # temperature
    latticeName = []
    for n in range(N):
        l = int(L/(2**n))
        latticeName.append('l = ' +str(l))
    
    # set up plots
    plt.close('all')
    l = L
    fig, axs = plt.subplots(1, N, figsize=(15, 4))
    for ax, interp in zip(axs, latticeName):
        ax.set_title(interp)    
        for i in range(l + 1):   #cell boundary for each site
                ax.axhline(i, lw=0.5)
                ax.axvline(i, lw=0.5)
        l = int(l/2)
    plt.tight_layout()  #improve appearance of layout
    lattice = np.zeros((L,L)) #all sites empty
    for i in range(L):
        for j in range(L):
            if rnd() < p: 
                lattice[i,j] = 1  #randomly occupy sites
    
    #start renormalizing
    l = L
    for ax, interp in zip(axs, latticeName):
        product = 0
        if l != L:  #renormalize
            latticeB = np.zeros((l,l)) 
            product = 1
            for ib in range(l):
                for jb in range(l):
                    #verticle spanning rule, first index is y, second is x for visualization
                    leftCellProduct = lattice[2*ib,2*jb]*lattice[2*ib+1,2*jb]
                    rightCellProduct = lattice[2*ib,2*jb+1]*lattice[2*ib+1,2*jb+1]
                    if (leftCellProduct==1) or (rightCellProduct == 1):
                        latticeB[ib,jb] = 1
                    product *= latticeB[ib,jb]
            lattice = latticeB
        if product == 1:
            cmapUse = cmap1
        else:
            cmapUse = cmap12
        ax.imshow(lattice, interpolation='none', cmap=cmapUse,extent=[0, l, 0, l])
        l = int(l/2)       
    plt.pause(0.01)   # show lattices
    runAgain = input('Another lattice/Stop (a/s)')
    
