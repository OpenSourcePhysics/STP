"""
Created : Thu Aug 29 2019
Author: Jan Tobochnik 
Description: Demon algorithm for 1D Lattice Gas in phase space with variable particle number
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *

def main():
    #input parameters
    mcs = int(input0('mcs',200))  #number of spin flip attempts per spin
    L = int(input0('Length',100)) #length of the lattice
    pmax = int(input0('Maximum momentum',10)) #length of the lattice
    N = int(input0('Initial number of particles',100))  #number of spins
    Etotal = int(input0('Energy',200))  #number of spins
    hc = int(input0('Hard core(set to 1 for hard core',0))  
    epsilon = int(input0('attractive well depth',0)) 
    Eindex = int(input0('E for P(E,N)',1))
    Nindex = int(input0('N for P(E,N)',1))
    nplot = int(input0('time between plots',10))
    
    # set up plots
    plt.close('all')
    fig, axs = plt.subplots(1, 3, figsize=(15, 4))
    for ax, interp in zip(axs, ['Lattice', 'ln P(E)','ln P(N)']):
        ax.set_title(interp)
        if interp == 'Lattice':
            for i in range(L + 1):   #cell boundary for each spin
                ax.axhline(i, lw=0.5)
                ax.axvline(i, lw=0.5)
            ax.set_ylabel('p+pmax')
        elif interp == 'ln P(E)':
            ax.grid(True)  #add grid for plots
            ax.set_xlabel('E')
            dataE, = ax.plot(0,0)
        else:
            ax.grid(True)  #add grid for plots
            ax.set_xlabel('N')
            dataN, = ax.plot(0,0)
    plt.tight_layout()  #improve appearance of layout
    my_cmap = ListedColormap(['w', 'g','r','b'])  #lattice cell colors
    
    #initialize 
    Lp = 2*pmax+1
    mcs  *=L*Lp
    nplot *= L*Lp
    phaseSpace = np.zeros((L,Lp))
    drawLattice = np.zeros((Lp,L))
    realSpace = np.zeros(L)
    Prob = np.zeros((Etotal+1,N+1))  #Demon probabilities
    infE = 100*Etotal  #infinity for hard core
    E = 0
    for n in range(N):
        tryAgain = True
        while tryAgain:
            x = int(L*rnd())
            p = int(Lp*rnd())
            E1 = (p-pmax)**2  #kinetic energy 
            E1 += hc*infE*realSpace[x]  #hard core energy
            E1 -= epsilon*(realSpace[(x+1)%L] + realSpace[(x-1+L)%L])
            if (E1 + E <= Etotal) and phaseSpace[x,p] != 1:
                tryAgain = False
                realSpace[x] += 1
                phaseSpace[x,p] = 1
                E += E1
    def reset(cum):
        cum['Ed'] = 0  
        cum['Nd'] = 0 
        cum['accept'] = 0 
        cum['n'] = 0
        return cum   
    cum = {}   #set up dictionary (hash table)
    reset(cum)
    
    #start simulation
    Ed = 0
    Nd = 0
    keepRunning = 'c'
    while keepRunning == 'c':            
        for imcs in range(mcs):
            x=int(L*rnd())
            p=int(Lp*rnd())
            E1 = (p-pmax)**2  #kinetic energy 
            E1 -= epsilon*(realSpace[(x+1)%L] + realSpace[(x-1+L)%L])              
            if (phaseSpace[x,p] == 0) and (E1 <Ed) and (Nd > 0):
                #add particle
                if (hc == 0)  or (realSpace[x] == 0):
                    E += E1
                    Ed -= E1
                    N += 1
                    Nd -= 1
                    cum['accept'] += 1
                    realSpace[x] += 1
                    phaseSpace[x,p] += 1
            elif (phaseSpace[x,p] == 1) and (-E1 < Ed):
                #remove particle
                E -= E1
                Ed += E1
                N -= 1
                Nd += 1
                cum['accept'] += 1
                realSpace[x] -= 1
                phaseSpace[x,p] -= 1
            Prob[int(Ed),int(Nd)] += 1
            cum['Ed'] += Ed
            cum['Nd'] += Nd
            cum['n'] += 1
            if cum['n'] % nplot == 0:  #plot data
                for x in range(L):
                    for p in range(Lp):
                        drawLattice[Lp-1-p,x] = phaseSpace[x,p]   #Needed to draw p on vertical axis
                lnPofE = []
                Eaxis = []
                lnPofN = []
                Naxis = []
                for e in range(Etotal):
                    if Prob[e,Nindex] > 0:
                        lnPofE.append(math.log(Prob[e,Nindex]/cum['n']))
                        Eaxis.append(e)
                for n in range(N):
                    if Prob[Eindex,n] > 0:
                        lnPofN.append(math.log(Prob[Eindex,n]/cum['n']))
                        Naxis.append(n)                      
                for ax, interp in zip(axs, ['Lattice', 'ln P(E)','ln P(N)']):
                    if interp == 'Lattice':
                        ax.imshow(drawLattice, interpolation='none', cmap=my_cmap,extent=[0, L, 0, Lp],zorder=0)
                    elif interp == 'ln P(E)':
                        dataE.remove()
                        dataE, = ax.plot(Eaxis,lnPofE,'bo')
                    else:
                        dataN.remove()
                        dataN, = ax.plot(Naxis,lnPofN,'bo')
                plt.pause(0.001)   #needed to  visualize continuously
        printi('Number of MC steps used in average',int(cum['n']))
        print3f('acceptance ratio',cum['accept']/cum['n'])
        print3f('Mean demon energy',cum['Ed']/(cum['n']))
        print3f('Mean demon particle number',cum['Nd']/(cum['n']))
        keepRunning = input('Continue/Stop/Reset Averages/Change E and N in P(e,N) (c/s/r/cl)')
        if keepRunning == 'r':  #reset averages
            reset(cum)
            Prob = np.zeros((Etotal+1,N+1))
            keepRunning = 'c' 
        elif keepRunning == 'cl':  #reset averages
            Eindex = int(input0('E for P(E,N)',1))
            Nindex = int(input0('N for P(E,N)',1))
            keepRunning = 'c' 
    
 
main()