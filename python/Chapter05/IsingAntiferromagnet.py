"""
Created : Thu Aug 22 08:59:02 2019
Author: Jan Tobochnik 
Description: 2D Ising Antiferromagnetic model on a square lattice
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *


#input parameters
mcs = int(input0('mcs',400))  #number of spin flip attempts per spin
L = int(input0('L',20)) #length of the lattice 
N = L*L  #number of spins
H = float(input0('H',0)) #external magnetic field
T = float(input0('T',2.269)) # temperature
nplot = int(input0('time between plots',10))

# set up plots
plt.close('all')
fig, axs = plt.subplots(1, 3, figsize=(15, 4))
for ax, interp in zip(axs, ['lattice', 'energy per spin', 'magnetization and staggered magnetization']):
    ax.set_title(interp.capitalize())
    if interp == 'lattice':
        for i in range(L + 1):   #cell boundary for each spin
            ax.axhline(i, lw=0.5)
            ax.axvline(i, lw=0.5)
    else:
        ax.grid(True)  #add grid for plots
        ax.set_xlabel('time(mcs)')
plt.tight_layout()  #improve appearance of layout
my_cmap = ListedColormap(['r', 'b', 'b'])  #spin colors

#start off with randomly oriented spins
spins = np.ones((L,L)) #all spins up
for i in range(L):
    for j in range(L):
        if rnd() < 0.5: 
            spins[i,j] = -1  #randomly flip spins to down
#calculate energy and magnetization 
E = 0  #enerrgy 
M = 0  #magnetization 
Ms = 0 #staggered magnetization
for i in range(L):
    for j in range(L):
        E += spins[i,j]*(H+spins[i,(j+1)%L]+spins[(i+1)%L,j])
        M += spins[i,j]
        if (i+j)%2==0:
            Ms+= spins[i,j]
        else:
            Ms-= spins[i,j]
#variables to accumulate data for averaging
def reset(cum):
    cum['E'] = 0  
    cum['M'] = 0 
    cum['E2'] = 0 
    cum['M2'] = 0 
    cum['Ms'] = 0 
    cum['Ms2'] = 0 
    cum['accept'] = 0 
    cum['n'] = 0
    return cum   
cum = {}   #set up dictionary (hash table)
reset(cum)

#start simulation
nmcs = 0  #MC time 
keepRunning = 'c'
while keepRunning == 'c':            
    for imcs in range(0,mcs):
        nmcs += 1
        for n in range(N):
            k=int(N*rnd())
            j = int(k/L)
            i = k % L
            dE = 2*spins[i,j]*(H-spins[i,(j+1)%L]-spins[i,(j-1+L)%L] -spins[(i+1)%L,j]-spins[(i-1+L)%L,j])
            if((dE<=0) or (rnd()<math.exp(-dE/T))) :
                spins[i,j]=-spins[i,j]
                cum['accept'] += 1
                E += dE
                M += 2*spins[i,j]
                if (i+j)%2==0:
                    Ms+= spins[i,j]
                else:
                    Ms-= spins[i,j]               
            cum['E'] += E
            cum['E2'] += E*E
            cum['M'] += M
            cum['Ms'] +=  Ms
            cum['Ms2'] +=  Ms*Ms
            cum['M2'] += M*M
            cum['n'] += 1       
        if nmcs % nplot == 0:  #plot data
            plt.pause(0.001)   #needed to see visulaize continuously
            for ax, interp in zip(axs, ['lattice', 'energy per spin', 'magnetization']):
                if interp == 'lattice':
                    ax.imshow(spins, interpolation='none', cmap=my_cmap,extent=[0, L, 0, L],zorder=0)
                elif interp == 'energy per spin':
                    ax.plot(nmcs,cum['E']/(cum['n']*N),'b.')
                else:
                    ax.plot(nmcs,cum['M']/(cum['n']*N),'b.')
                    ax.plot(nmcs,cum['Ms']/(cum['n']*N),'r.')
    printi('Number of MC steps used in average',int(cum['n']/N))
    print3f('acceptance ratio',cum['accept']/cum['n'])
    print3f('average energy',cum['E']/(N*cum['n']))
    print3f('Specific Heat',((cum['E2']/cum['n'])-(cum['E']/cum['n'])**2)/(N*T*T))
    print3f('average magnetization',cum['M']/(N*cum["n"]))
    print3f('Susceptibility',cum['M2']/(cum['n']*N*T))
    print3f('average stagggered magnetization',cum['Ms']/(N*cum["n"]))
    print3f('Staggered Susceptibility',((cum['Ms2']/cum['n'])-(cum['Ms']/cum['n'])**2)/(N*T))
    keepRunning = input('Continue/Stop/Reset Averages/New Parameters (c/s/r/n)')
    if keepRunning == 'r':  #reset averages
        reset(cum)
        keepRunning = 'c' 

    elif keepRunning == 'n':
        reset(cum)
        keepRunning = 'c'        
        H = float(input0('H',H)) #external magnetic field
        T = float(input0('T',T)) #temperature
   

