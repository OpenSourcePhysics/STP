"""
Created : Created June 16, 2020
Author: Jan Tobochnik 
Description: Sums over single particle states of a Bose gas
    """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd
import math
from myFunctions import *


#input parameters
T0 = float(input0('T0',2.0))  #minimum temperature 
Tmax = float(input0('Tmax',150.0))  #maximum temperature 
dT = float(input0('dT',5.0))  #change in temperature 
N = int(input0('N',500)) #number of particles
arraySize = int(1 + (Tmax-T0)/dT)
print("This may take a couple minutes")

# set up plots
plt.close('all')
fig, axs = plt.subplots(1, 3, figsize=(15, 4))
for ax, interp in zip(axs, ['Chemical Potential', 'Specific Heat', 'Occupancy of single particle states']):
    ax.set_title(interp.capitalize())
    if interp == 'Chemical Potential':
        ax.set_xlabel('kT')
        ax.set_ylabel('\u03bc*')
    elif interp == 'Specific Heat':
        ax.set_xlabel('kT')
        ax.set_ylabel('c')
    else:
        ax.set_xlabel('kT')
        ax.set_ylabel('N0,N1,Nex')
plt.tight_layout()  #improve appearance of layout

N0 = np.zeros(arraySize)
N1 = np.zeros(arraySize)
Nex = np.zeros(arraySize)
kT = np.zeros(arraySize)
cp = np.zeros(arraySize)
sh = np.zeros(arraySize)
T = T0
mu = 0
lndelta = math.log(1E8)
dN = 0.0001
E1 = 0
for i in range(arraySize):
    nmax = int(math.sqrt(T*lndelta + mu))
    muplus = 3
    muminus = -3*T;
    Ntrial = 0;
    E = 0;
    while(abs(N-Ntrial) > dN):
        mu = 0.5*(muplus+muminus)
        Ntrial = 0
        E = 0
        for nx in range(nmax):
            for ny in range(nmax):
                for nz in range(nmax):
                    ep = (nx+1)**2 + (ny+1)**2 + (nz+1)**2
                    nbar = 1.0/(math.exp((ep-mu)/T) -1)
                    Ntrial += nbar;
                    E += ep*nbar;
        if(Ntrial > N):
            muplus = mu
        else:
            muminus = mu
    cp[i] = mu
    kT[i] = T
    N0[i] = 1.0/(math.exp((3-mu)/T) -1)
    N1[i] = 3.0/(math.exp((6-mu)/T) -1)   #3 states
    Nex[i] = N - N0[i]
    if(E1 != 0):
        sh[i] = (E - E1)/dT 
    E1 = E
    T += dT
for ax, interp in zip(axs, ['Chemical Potential', 'Specific Heat', 'Occupancy of single particle states']):
    if interp == 'Chemical Potential':
        ax.plot(kT,cp,'b.')
    elif interp == 'Specific Heat':
        ax.plot(kT,sh,'b.')
    else:
        ax.plot(kT,N0,'r.')
        ax.plot(kT,N1,'b.')
        ax.plot(kT,Nex,'g.')
print("Now computing Tc")
mu = 3
Tplus = N
Tminus = 0
Ntrial = 0
while(abs(N-Ntrial) > dN):
    T = 0.5*(Tplus+Tminus)
    Ntrial = 0
    for nx in range(nmax):
        for ny in range(nmax):
            for nz in range(nmax):
                ep = (nx+1)**2 + (ny+1)**2 + (nz+1)**2
                if(ep > mu):
                    nbar = 1.0/(math.exp((ep-mu)/T) -1)
                    Ntrial += nbar;
    if(Ntrial > N):
        Tplus = T
    else:
        Tminus = T
print3f('Tc',T) 