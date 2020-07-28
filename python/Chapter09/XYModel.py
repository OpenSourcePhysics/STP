"""
Created : Thu Aug 22 08:59:02 2019, revised 9/23/19
Author: Jan Tobochnik 
Description: 2D XY or Planar Model
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
    L = int(input0('L',20)) #length of the lattice 
    N = L*L  #number of spins
    H = float(input0('H',0)) #external magnetic field
    T = float(input0('T',1.0)) # temperature
    dtheta = 2*float(input0('max dtheta',0.5)) # max change in angle 
    initconf = input0('initial configuration:random, aligned (r/a)','r')
    nplot = int(input0('time between plots',10))
    twopi = 2*math.pi
    # set up plots
    plt.close('all')
    fig, axs = plt.subplots(1, 3, figsize=(15, 4))
    for ax, interp in zip(axs, ['vortices', 'energy per spin', 'magnetization']):
        ax.set_title(interp.capitalize())
        if interp == 'vortices':
            for i in range(L + 1):   #cell boundary for each spin
                ax.axhline(i, lw=0.5)
                ax.axvline(i, lw=0.5)
        else:
            ax.grid(True)  #add grid for plots
            ax.set_xlabel('time (mcs)')
    plt.tight_layout()  #improve appearance of layout
    my_cmap = ListedColormap([ 'y', 'w', 'b'])  #svortex colors
    
    #start off with randomly oriented spins
    spins = np.zeros((L,L)) #all spins up
    if(initconf == 'r'):
        for i in range(L):
            for j in range(L):
                spins[i,j] = rnd()*twopi  #randomly orient spins 
    #calculate energy and magnetization 
    E = 0  #enerrgy 
    M = 0  #magnetization 
    for i in range(L):
        for j in range(L):
            E -= math.cos(spins[i,j] - spins[i,(j+1)%L]) + math.cos(spins[i,j] - spins[(i+1)%L,j]) + H*math.cos(spins[i,j])
            M += math.cos(spins[i,j]) 
    # set up plots
    plt.close('all')
    fig, axs = plt.subplots(1, 3, figsize=(15, 4))
    plt.tight_layout()  #improve appearance of layout
    my_cmap = ListedColormap([ 'y', 'w', 'b'])  #svortex colors
    for ax, interp in zip(axs, ['vortices', 'energy per spin', 'correlation function']):
        ax.set_title(interp.capitalize())
        if interp == 'vortices':
            for i in range(L + 1):   #cell boundary for each spin
                ax.axhline(i, lw=0.5)
                ax.axvline(i, lw=0.5)
        elif interp == 'energy per spin':
            ax.grid(True)  #add grid for plots
            ax.set_xlabel('time (mcs)')
        else:
            ax.set_xlabel('r')
            ax.set_xlim(0,L/2) 
            ax.set_ylim(-0.2,1.0)
            dataC, = ax.plot(0,0)
   #variables to accumulate data for averaging
   
    def reset(cum):
        cum['E'] = 0  
        cum['M'] = 0 
        cum['E2'] = 0 
        cum['M2'] = 0 
        cum['|M|'] = 0 
        cum['accept'] = 0 
        cum['n'] = 0
        return cum  
    cum = {}   #set up dictionary (hash table)
    cum = reset(cum)
    correlation = np.zeros(L)
    norm = np.zeros(L)
 
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
                theta = spins[i,j] + (rnd()-0.5)*dtheta
                dM = math.cos(theta) - math.cos(spins[i,j])  
                dE = -H*dM
                dE += math.cos(spins[i,j] - spins[i,(j+1)%L]) - math.cos(theta - spins[i,(j+1)%L])
                dE += math.cos(spins[i,j] - spins[i,(j-1+L)%L]) - math.cos(theta - spins[i,(j-1+L)%L])
                dE += math.cos(spins[i,j] - spins[(i+1)%L,j]) - math.cos(theta - spins[(i+1)%L,j])
                dE += math.cos(spins[i,j] - spins[(i-1+L)%L,j]) - math.cos(theta - spins[(i-1+L)%L,j])
                if((dE<=0) or (rnd()<math.exp(-dE/T))) :
                    spins[i,j]= theta
                    cum['accept'] += 1
                    E = E + dE
                    M = M + dM               
                cum['E'] += E
                cum['E2'] += E*E
                cum['M'] += M
                cum['|M|'] +=  abs(M)
                cum['M2'] += M*M
                cum['n'] += 1  
            if nmcs % 10 == 0:
                norm,correlation = correlationFunction(spins,L,correlation,norm)
            if nmcs % nplot == 0:  #plot data
                vortices = np.zeros((L,L))
                for x in range(L):
                    for y in range(L):
                        vortices[x][y] = identifyVortex(x,y,spins,L)
                cOfr = np.zeros(int(L/2))
                plotr = np.zeros(int(L/2))
                for r in range(int(L/2)):
                    cOfr[r] = correlation[r]/norm[r]
                    plotr[r] = r;
                plt.pause(0.001)   #needed to see visulaize continuously
                for ax, interp in zip(axs, ['vortices', 'energy per spin', 'correlation function']):
                    if interp == 'vortices':
                        ax.imshow(vortices, interpolation='none', cmap=my_cmap,extent=[0, L, 0, L],zorder=0)
                    elif interp == 'energy per spin':
                        ax.plot(nmcs,cum['E']/(cum['n']*N),'b.')
                    elif interp == 'correlation function':
                        dataC.remove()
                        dataC, = ax.plot(plotr,cOfr,'go')
        printi('Number of MC steps used in average',int(cum['n']/N))
        print3f('acceptance ratio',cum['accept']/cum['n'])
        print3f('average energy',cum['E']/(N*cum['n']))
        print3f('Specific Heat',((cum['E2']/cum['n'])-(cum['E']/cum['n'])**2)/(N*T*T))
        print3f('average magnetization',cum['M']/(N*cum["n"]))
        print3f('Susceptibility',((cum['M2']/cum['n'])-(cum['|M|']/cum['n'])**2)/(N*T))
        keepRunning = input('Continue/Stop/Reset Averages/New Parameters (c/s/r/n)')
        if keepRunning == 'r':  #reset averages
            cum = reset(cum)
            for r in range(L):
                correlation[r] = 0 
                norm[r] = 0
            keepRunning = 'c' 
            mcs = int(input0('mcs',mcs))  #number of spin flip attempts per spin
            nplot = int(input0('time between plots',nplot))
        elif keepRunning == 'n':
            cum = reset(cum)
            for r in range(L):
                correlation[r] = 0 
                norm[r] = 0
            keepRunning = 'c' 
            mcs = int(input0('mcs',mcs))  #number of spin flip attempts per spin
            H = float(input0('H',H)) #external magnetic field
            T = float(input0('T',T)) #temperature
            nplot = int(input0('time between plots',nplot))  

def identifyVortex(x,y,spins,L): # all angles between 0 and 2*pi
    delta = 0
    angles = np.zeros(4)
    angles[0] = spins[x][y]
    angles[1] = spins[x][(y+1)%L]
    angles[2] = spins[(x+1)%L][(y+1)%L]
    angles[3] = spins[(x+1)%L][y]
    for i in range(3):
        temp = angles[i+1]-angles[i]
        if((temp<=math.pi) and (temp>-math.pi)):
            delta += temp;
        elif(temp>math.pi):
            delta += (temp-2*math.pi);
        else:
            delta += (temp+2*math.pi);
    if(delta>math.pi):
        return 1
    elif(delta<-math.pi):
        return -1
    else: 
        return 0
    
def correlationFunction(spins,L,correlation,norm):
    half = L/2;
    for  x1 in range(L):
        for  y1 in range(L):
            for  x2 in range(L):
                for  y2 in range(L):
                    dx = abs(x2-x1)
                    if(dx > half): 
                        dx -= L
                    dy = abs(y2-y1)
                    if(dy > half):
                        dy -= L
                    r = int(math.sqrt(0.2 + dx*dx+dy*dy))
                    correlation[r] += math.cos(spins[x1][y1]-spins[x2][y2])
                    norm[r] += 1
    return norm,correlation
  
main()