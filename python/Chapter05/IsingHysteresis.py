"""
Created : Thu Aug 30 2019
Author: Jan Tobochnik 
Description: 2D Ising Model Hysteresis Loop
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *


#input parameters
L = int(input0('Length',20)) #length of the lattice 
N = L*L  #number of spins
H = 1.0   #initial magnetic field
dH = -float(input0('dH',0.01)) 
mcs = int(input0('mcs per field value',10))  #number of spin flip attempts per spin
T = float(input0('Temperature',2.269)) # temperature
nplot = int(input0('time between plots',10))
showLattice = input0('show lattice y/n','n') #runs faster if lattice not shown

# set up plots
plt.close('all')
fig, axs = plt.subplots(1, 2, figsize=(10, 4))
for ax, interp in zip(axs, ['lattice', 'magnetization']):
    ax.set_title(interp.capitalize())
    if interp == 'lattice':
        for i in range(L + 1):   #cell boundary for each spin
            ax.axhline(i, lw=0.5)
            ax.axvline(i, lw=0.5)
    else:
        ax.grid(True)  #add grid for plot
        ax.set_xlabel('H')
        ax.set_ylabel('M')
        ax.set_xlim(-1,1)
        ax.set_ylim(-1,1)
plt.tight_layout()  #improve appearance of layout
my_cmap = ListedColormap(['r', 'g', 'b'])  #spin colors

#start off with all spins up
spins = np.ones((L,L)) #all spins up
M = N
nmcs = 0

#start simulation
while H <= 1.0: 
    nmcs += 1 
    Mcum = 0          
    for n in range(mcs*N):
        k=int(N*rnd())
        j = int(k/L)
        i = k % L
        dE = 2*spins[i,j]*(H+spins[i,(j+1)%L]+spins[i,(j-1+L)%L] +spins[(i+1)%L,j]+spins[(i-1+L)%L,j])
        if((dE<=0) or (rnd()<math.exp(-dE/T))) :
                spins[i,j]=-spins[i,j]
                M += 2*spins[i,j]
        Mcum += M
    if nmcs % nplot == 0:  #plot data
        plt.pause(0.001)   #needed to see visulaize continuously
        for ax, interp in zip(axs, ['lattice', 'magnetization']):
            if interp == 'lattice':
                if showLattice == 'y':
                    ax.imshow(spins, interpolation='none', cmap=my_cmap,extent=[0, L, 0, L],zorder=0)
            elif interp == 'magnetization':
                if dH < 0:
                    ax.plot(H,Mcum/(mcs*N*N),'bo')
                else:
                    ax.plot(H,Mcum/(mcs*N*N),'ro')
    if abs(H) > 1:
         dH = -dH
    H += dH                 


