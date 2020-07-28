
"""
Created on September 9, 2019
@author: Jan Tobochnik
2D site percolation using the Newman-Ziff algorithm 
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
from PercolationClusters import *


def main():
#input parameters
    L = int(input0('Lattice size',32))   #Lattice size
    N = L*L
    pDisplay = float(input0('display lattice at this value of p',0.5927))
    nLat = int(input0('Number of trials',10))
 
#set up plots 
    plt.close('all')
    fig, axs = plt.subplots(2,3, figsize=(15, 10))
    axs[0,0].set_title('Newman-Ziff cluster algorithm')
    for i in range(L + 1):   #cell boundary for each spin
        axs[0,0].axhline(i, lw=0.5)
        axs[0,0].axvline(i, lw=0.5)
    datac, = axs[0,1].plot(0,0)
    axs[0,1].set_title('Mean cluster size')
    axs[0,1].grid(True)
    axs[0,1].set_xlabel('p') 
    axs[0,1].set_ylabel('Mean cluster size')
    axs[0,1].set_xlim(0,1)
    datapi, = axs[0,2].plot(0,0)
    axs[0,2].set_title('P_\u221e')
    axs[0,2].grid(True)
    axs[0,2].set_xlabel('p') 
    axs[0,2].set_ylabel('P_\u221e')
    axs[0,2].set_xlim(0,1)
    axs[0,2].set_ylim(0,1)
    dataps, = axs[1,0].plot(0,0)
    axs[1,0].set_title('P_span')
    axs[1,0].grid(True)
    axs[1,0].set_xlabel('p') 
    axs[1,0].set_ylabel('Pspan')
    axs[1,0].set_xlim(0,1)
    axs[1,0].set_ylim(0,1)
    datad, = axs[1,1].plot(0,0)
    axs[1,1].set_title('Cluster size distribution')
    axs[1,1].grid(True)
    axs[1,1].set_xlabel('ln s') 
    axs[1,1].set_ylabel('ln ns')
    plt.tight_layout()  #improve appearance of layout

#set up arrays
    displayLattice = np.zeros((L,L))
    pAxis = np.arange(0,1,1/N)
    meanClusterSize = np.zeros(N)
    P_infinity = np.zeros(N)
    P_span = np.zeros(N)
    numClustersAcum = np.zeros(N)
    numberOfTrials = 0;
    numClusters,secondClusterMoment,spanningClusterSize,order,parent,touchesLeft,touchesRght = initialize(L,N)
    
#start simulation
    keepRunning = 'c'
    while keepRunning == 'c':
        for iLat in range(nLat):
             numberOfTrials += 1
             numSitesOccupied, secondClusterMoment, spanningClusterSize = newLattice(numClusters,parent,touchesLeft,touchesRght,order,L,N)
             for i in range(N):
                 numSitesOccupied,secondClusterMoment,spanningClusterSize = addRandomSite(numSitesOccupied,numClusters,secondClusterMoment,spanningClusterSize,parent,order,touchesLeft,touchesRght,L,N)
                 meanClusterSize[i] += getMeanClusterSize(spanningClusterSize,secondClusterMoment,numSitesOccupied)
                 P_infinity[i] += spanningClusterSize/numSitesOccupied
                 if spanningClusterSize != 0: P_span[i] += 1
                 if int(pDisplay*N) == i:
                     for j in range(N):
                         numClustersAcum[j] += numClusters[j]
                         for x in range(L):
                             for y in range(L):
                                 n = y + x*L   #python plots x vertically
                                 displayLattice[x,y] = getClusterSize(n,parent) # large clusters have unique sizes
                     axs[0,0].imshow(displayLattice, interpolation='none',extent=[0, L, 0, L],zorder=0)
             plot1 =  meanClusterSize/numberOfTrials
             plot2 =  P_infinity/numberOfTrials
             plot3 =  P_span/numberOfTrials
             plot4y = []
             plot4x = []
             for n in range(1,N):
                 if numClustersAcum[n] > 0:
                     plot4x.append(math.log(n))
                     plot4y.append(math.log(numClustersAcum[n]/numberOfTrials))
                     
             datac.remove()
             datac, = axs[0,1].plot(pAxis,plot1,'r.')  #add new data
             datapi.remove()
             datapi, = axs[0,2].plot(pAxis,plot2,'r.')  #add new data
             dataps.remove()
             dataps, = axs[1,0].plot(pAxis,plot3,'r.')  #add new data
             datad.remove()
             datad, = axs[1,1].plot(plot4x,plot4y,'r.')  #add new data
             plt.pause(.000001) #needed to visualize data continuously
             printi('number of trials',numberOfTrials)
        keepRunning = input('Continue/Stop/(c/s)')
        if keepRunning == 'c':
            nLat = int(input0('Number of lattices',10))
            pDisplay = float(input0('display lattice at this value of p',0.5927))

     
main()