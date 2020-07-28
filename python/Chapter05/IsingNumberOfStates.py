"""
Created : Sept. 9  2019
Author: Jan Tobochnik 
Description: 2D Ising Model using Wang-Landau algorithm
    """
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from numpy.random import random as rnd
import math
from myFunctions import *


#input parameters
L = int(input0('L',16)) #length of the lattice 
N = L*L  #number of spins
T = float(input0('T for (P(E) plot',2.5)) 
beta = 1/T
nset = int(input0('number of sets of mcs',10))
mcs = int(100000/N)
if mcs == 0: mcs =1

# set up plots
plt.close('all')
data = {}
fig, axs = plt.subplots(1, 4, figsize=(16, 4))
for ax, interp in zip(axs, ['Histogram', 'Density of States', 'Specific Heat','Energy Distribution']):
    ax.set_title(interp)
    ax.grid(True)
    data[interp], = ax.plot(0,0)
    if interp == 'Histogram':
        ax.set_xlabel('E')
        ax.set_ylabel('H(E)') 
        datah, = ax.plot(0,0)
    elif interp == 'Density of States':
        ax.set_xlabel('E')
        ax.set_ylabel('ln g(E)')
        datad, = ax.plot(0,0)
    elif interp == 'Specific Heat':
        ax.set_xlabel('T')
        ax.set_ylabel('C(T)')
        datahc, = ax.plot(0,0)
    elif interp == 'Energy Distribution':
        ax.set_xlabel('E')
        ax.set_ylabel('P(E)')
        datae, = ax.plot(0,0)

plt.tight_layout()  #improve appearance of layout

#start off with randomly oriented spins
spins = np.ones((L,L)) #all spins up
for i in range(L):
    for j in range(L):
        if rnd() < 0.5: 
            spins[i,j] = -1  #randomly flip spins to down

#calculate energy 
E = 0  #enerrgy 
for i in range(L):
    for j in range(L):
        E -= spins[i,j]*(spins[i,(j+1)%L]+spins[(i+1)%L,j])
E += 2*N  #translate energy by 2*N to facilitate array access

#initialize variables
f = math.exp(1)
iterations = 0
lng = np.zeros(N + 1)
H = np.zeros(N+1)
Eaxis = np.arange(-2*N,2*N + 1,4)

def isFlat(N,H):
    netH = 0
    numEnergies = 0
    for e in range(N+1):
      if(H[e]>0):
        netH += H[e]
        numEnergies += 1
    netHAvept8 = 0.8*netH/numEnergies
    for e in range(N+1):
      if((0<H[e]) and (H[e]<netHAvept8)):
        return False
    return True

def logZ(N, lng, beta):
    #m = max {e^(g - beta E)}
    m = 0
    for e in range(N+1):
        mm = lng[e] - beta*(e - N/2)*4
        if mm > m:
            m = mm
    #s = Sum {e^(g - beta E)} * e^(-m)
    # => s = Z * e^(-m)
    # => log s = log Z - m
    s = 0;
    for e in range(N+1):
        s += math.exp(lng[e] - beta*(e - N/2)*4-m)
    return math.log(s)+m

def heatCapacity(N,lng,beta):
    lnZ = logZ(N, lng, beta)
    E_avg = 0
    E2_avg = 0
    for e in range(N+1):
      if(lng[e] !=0):
          E = (e - N/2)*4
          E_avg += E*math.exp(lng[e]-beta*E-lnZ)
          E2_avg += E*E*math.exp(lng[e]-beta*E-lnZ)
    return (E2_avg-E_avg*E_avg)*beta*beta 

#start simulation
nmcs = 0  #MC time 
keepRunning = 'c'
while keepRunning == 'c':
    for iset in range(nset):
        for imcs in range(mcs):
            nmcs += 1
            for n in range(N):
                k=int(N*rnd())
                j = int(k/L)
                i = k % L
                dE = 2*spins[i,j]*(spins[i,(j+1)%L]+spins[i,(j-1+L)%L] +spins[(i+1)%L,j]+spins[(i-1+L)%L,j])
                dlng = lng[int(E/4)] - lng[int((E+dE)/4)]
                if dlng > 0 or rnd() < math.exp(dlng)  :
                    spins[i,j]=-spins[i,j]
                    E +=  dE
                lng[int(E/4)] += math.log(f)
                H[int(E/4)] += 1
        if(isFlat(N,H) and f>1.0001):
            f = math.sqrt(f)
            printi("iterations",iterations)
            printf("f",f,4)
            iterations += 1
            for e in range(N+1):
                H[e] = 0
        for ax, interp in zip(axs, ['Histogram', 'Density of States', 'Specific Heat','Energy Distribution']):
            lnge0 = []
            E0axis = []
            Hplot = []
            Pplot = []
            z = 0
            maxarg = 0
            for e in range(N+1):
                if lng[e] > 0:
                    arg = lng[e]-lng[0]-beta*(e - N/2)*4
                    if arg > maxarg: maxarg = arg
            lngmax = 0                
            for e in range(N+1):
                if lng[e] > 0:
                    arg = lng[e]-lng[0]-beta*(e - N/2)*4 - maxarg
                    z += math.exp(arg)
                    dlng = lng[e] - lng[0]
                    if dlng > lngmax: lngmax = dlng
                    lnge0.append(dlng)
                    E0axis.append(Eaxis[e])
                    Hplot.append(H[e])
            pmax = 0
            for e in range(N+1):
                if lng[e] > 0:
                    p = math.exp(lng[e]-lng[0]-beta*(e - N/2)*4 - maxarg)/z
                    if p > pmax: pmax = p
                    Pplot.append(p)
            if interp == 'Histogram':
                datah.remove()
                datah, = ax.plot(E0axis,Hplot,'r.')
            elif interp == 'Density of States':
                datad.remove()
                ax.set_ylim(0,1.1*lngmax)
                datad, =ax.plot(E0axis,lnge0,'b.')
            elif interp == 'Specific Heat':
                datahc.remove()
                Cplot = []
                Taxis = []
                cmax = 0
                T = 0.5
                while T < 6:
                    Taxis.append(T)
                    c = heatCapacity(N, lng, 1/T)/N
                    if c > cmax: cmax = c
                    Cplot.append(c);
                    T += 0.1
                T = 1.9
                while T < 2.7:
                    Taxis.append(T)
                    c = heatCapacity(N, lng, 1/T)/N
                    if c > cmax: cmax = c
                    Cplot.append(c);
                    T += 0.02
                ax.set_ylim(0,1.1*cmax)
                datahc, =ax.plot(Taxis,Cplot,'r.')
            elif interp == 'Energy Distribution':
                datae.remove()
                ax.set_ylim(0,1.1*pmax)
                datae, =ax.plot(E0axis,Pplot,'r.')
        printi('nmcs',nmcs)
        plt.pause(0.001)   #needed to see visualize continuously
    keepRunning = input('Continue/Stop (c/s/)')
    if keepRunning == 'c':
        nset = int(input0('number of sets of mcs',10))
        T = float(input0('T for (P(E) plot',2.5)) # temperature
        beta = 1/T
   

