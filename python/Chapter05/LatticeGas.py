"""
Created : Thu Aug 29 2019
Author: Jan Tobochnik 
Description: 2D Lattice Gas
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *

def main():
    #input parameters
    mcs = int(input0('mcs',400))  #number of spin flip attempts per spin
    L = int(input0('Length',32)) #length of the lattice
    Nsites = L*L
    N = int(input0('Number of particles',512))  #number of spins
    T = float(input0('Temperature',0.567)) # temperature
    g = float(input0('Gravitatioanl Field',0)) #external gravitational field
    nplot = int(input0('time between plots',20))
    
    # set up plots
    plt.close('all')
    fig, axs = plt.subplots(1, 2, figsize=(10, 4))
    for ax, interp in zip(axs, ['particle configuration', 'energy per particle']):
        ax.set_title(interp.capitalize())
        if interp == 'particle configuration':
            for i in range(L + 1):   #cell boundary for each spin
                ax.axhline(i, lw=0.5)
                ax.axvline(i, lw=0.5)
        else:
            ax.grid(True)  #add grid for plots
            ax.set_xlabel('time (mcs)')
    plt.tight_layout()  #improve appearance of layout
    my_cmap = ListedColormap(['w', 'g'])  #lattice cell colors
    
    #start off with all particles att the bottom
    lattice = np.zeros((L,L)) #all sites empty 
    drawLattice = np.zeros((L,L))
    xp = np.zeros(N)
    yp = np.zeros(N)
    for n in range(N):
        i = n % L      
        j = int(n/L)   
        lattice[i,j] = 1 
        xp[n] = i
        yp[n] = j
    #calculate energy  
    E = 0  
    for i in range(L):
        for j in range(0,L):
            E -= lattice[i,j]*(-g*j+lattice[i,(j+1)%L]+lattice[(i+1)%L,j])
             
    #variables to accumulate data for averaging
    def reset(cum):
        cum['E'] = 0  
        cum['E2'] = 0 
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
            for moves in range(N):
                k=int(N*rnd())
                i1 = int(xp[k])
                j1 = int(yp[k])
                neighbor = rnd()
                i2 = int(nnx(neighbor,i1,L))
                j2 = int(nny(neighbor,j1,L))
                if lattice[i2,j2] == 0 and ((g == 0) or border(j1,j2,L)):  
                    dE = dEcalc(i1,j1,i2,j2,lattice,g,L)  
                    if((dE<=0) or (rnd()<math.exp(-dE/T))) :
                        lattice[i1,j1] = 0
                        lattice[i2,j2] = 1
                        yp[k] = j2
                        xp[k] = i2
                        cum['accept'] += 1
                        E +=  dE
                cum['E'] += E
                cum['E2'] += E*E
                cum['n'] += 1       
            if nmcs % nplot == 0:  #plot data
                for i in range(L):
                    for j in range(L):
                        drawLattice[L-1-i,j] = lattice[j,i]
                for ax, interp in zip(axs, ['particle configuration', 'energy per particle']):
                    if interp == 'particle configuration':
                        ax.imshow(drawLattice, interpolation='none', cmap=my_cmap,extent=[0, L, 0, L],zorder=0)
                    else:
                        ax.plot(nmcs,cum['E']/(cum['n']*N),'b.')
                plt.pause(0.001)   #needed to  visualize continuously
        printi('Number of MC steps used in average',int(cum['n']/N))
        print3f('acceptance ratio',cum['accept']/cum['n'])
        print3f('average energy per particle',cum['E']/(N*cum['n']))
        print3f('Specific Heat',((cum['E2']/cum['n'])-(cum['E']/cum['n'])**2)/(N*T*T))
        keepRunning = input('Continue/Stop/Reset Averages/New Parameters (c/s/r/n)')
        if keepRunning == 'r':  #reset averages
            reset(cum)
            keepRunning = 'c' 
    
        elif keepRunning == 'n':
            reset(cum)
            keepRunning = 'c'        
            gnew = float(input0('Gravitatioanl Field',g)) #external magnetic field
            T = float(input0('Temperature',T)) #temperature
            for n in range(N):
                E += (gnew-g)*yp[n]  #change in energy due to change in g
            g = gnew
   
def nnx(neighbor,i1,L):
    if neighbor < 0.25:
        return (i1 + 1) % L
    elif neighbor < 0.5:
        return (i1 +L -1) % L
    else:
        return i1
    
def nny(neighbor,j1,L):
    if neighbor < 0.5:
        return j1
    elif neighbor < 0.75:
        return (j1 + 1) % L
    else:
        return (j1 +L -1) % L

def dEcalc(i1,j1,i2,j2,lattice,g,L):
    dE = (j2-j1)*g
    dE += 1 - (lattice[(i2-1+L) %L,j2]+ lattice[(i2+1) %L,j2] + lattice[i2,(j2-1+L) % L]+ lattice[i2,(j2+1) % L]) 
    dE += (lattice[(i1-1+L) %L,j1]+ lattice[(i1+1) %L,j1] + lattice[i1,(j1-1+L) % L]+ lattice[i1,(j1+1) % L])
    if (g != 0):
        if  j1 ==0:
            if j2 == 1:
                dE -= lattice[i1,(j1-1+L)%L]
            else:
                dE += lattice[i2,(j1-1+L)%L]- lattice[i1,(j1-1+L)%L]
        elif j1 == L-1:
            if j2 == L-2:
                dE -= lattice[i1,(j1+1)%L]
            else:
                dE += lattice[i2,(j1+1)%L]- lattice[i1,(j1+1)%L]
    return dE 

def border(j1,j2,L):
    if j1 == 0 and  j2 == L-1:
        return False
    elif j1 == L-1 and j2 == 0:
        return False
    else:
        return True
main()