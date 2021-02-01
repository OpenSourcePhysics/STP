
"""
Created on September 3, 2019
@author: Jan Tobochnik
MD simulation of Lennard-Jones particles in 3D, 
output is  temperature, pressure, density and temperature profile
calculates thermal conductivity using Florian Muller-Plathe, JCP 106, p. 6082 (1997).
"""

import numpy as np
import math
import matplotlib.pyplot as plt
from numpy.random import random as rnd 
from myFunctions import *
from LJ3d import *

def main():
#input parameters
    n = int(input0('Enter even k, number of particles = 3*k*k*k',4))   
    N = 3*n*n*n
    desiredTemperature = float(input0('desired temperature',0.7))
    rho = float(input0('number density',0.849))
    a = 1/rho**(1.0/3.0)
    dt = float(input0('time step',0.007))
    stepAdd = int(input0('time steps between swapping hot and cold particles',15))
    Lx = n*a
    Ly = n*a
    Lz = 3*n*a
    runTime = float(input0("run time (dt = 0.01)", 1))
    plotTime = float(input0("plot time (dt = 0.01)", 0.1))
    nSteps = int(runTime/dt)
    nPlot = int(plotTime/dt) 
    equil = True
    x = np.zeros(N)
    y = np.zeros(N)
    z = np.zeros(N)
    zSave = np.zeros(N)
    slab = np.zeros((3*n,6*n*n))
    nSlab = np.zeros(3*n)    
    Tslab = np.zeros(3*n) 
    rhoslab = np.zeros(3*n)    

    x,y,z,slab,nSlab = setPositions(N,x,y,z,Lx,Ly,Lz,a,slab,nSlab)
    vx = np.zeros(N)
    vy = np.zeros(N)    
    vz = np.zeros(N)    
    vx,vy,vz = setVelocities(N,vx,vy,vz,desiredTemperature) 
    nn = np.zeros((N,300))
    nn = setNNList(N,x,y,z,Lx,Ly,Lz,nn)        
    #set up plots and initial variables
    plt.close()
    fig, axs = plt.subplots(2,2, figsize=(10, 10))
    PEaccumulator = 0
    ax,ay,az,virial,PEaccumulator = accelerationLJ3d(x,y,z,N,Lx,Ly,Lz,nn,PEaccumulator)
    def reset(cum,axs):
        cum['KE'] = 0  
        cum['KE2'] = 0 
        cum['virial'] = 0 
        cum['n'] = 0
        cum['transfer'] = 0
        axs[0,0].clear()
        axs[0,0].set_title('Slab Temperature')
        axs[0,0].set_xlabel('z') 
        axs[0,0].set_ylabel('T(z)')
        axs[0,0].grid(True)
        axs[0,0].set_xlim(0,Lz/2) 
        axs[0,0].set_ylim(0.4,0.9) #Adjust as needed for better graphs
        dataST, = axs[0,0].plot(0,0)
        axs[0,1].clear()
        axs[0,1].set_title('Pressure')
        axs[0,1].grid(True)
        axs[0,1].set_xlabel('t') 
        axs[0,1].set_ylabel('PV/NkT')
        axs[1,1].clear()
        axs[1,1].set_title('Temperature')
        axs[1,1].grid(True)
        axs[1,1].set_xlabel('t') 
        axs[1,1].set_ylabel('T')
        axs[1,0].clear()
        axs[1,0].set_title('Slab density')
        axs[1,0].grid(True)
        axs[1,0].set_xlabel('z') 
        axs[1,0].set_ylabel('rho(z)')
        axs[1,0].set_xlim(0,Lz/2)
        axs[1,0].set_ylim(0.6,1.0)  #Adjust as needed for better graphs
        dataSD, = axs[1,0].plot(0,0)
        plt.tight_layout()  #improve appearance of layout
        return cum,axs,dataST,dataSD  
    cum = {}   #set up dictionary (hash table)
    cum,axs,dataST,dataSD = reset(cum,axs)
    t = 0
    
    #start simulation
    keepRunning = 'c'
    while keepRunning == 'c':
        for iStep in range(nSteps):
            cum['n'] += 1 
            t += dt
            for i in range(N):
                zSave[i] = z[i]
            x,y,z,vx,vy,vz,ax,ay,az,virial,PEaccumulator  = verlet3d(x,y,z,vx,vy,vz,ax,ay,az,dt,N,Lx,Ly,Lz,nn,PEaccumulator) 
            totalKE = 0
            for i in range(N):
                totalKE += vx[i]*vx[i] + vy[i]*vy[i] + vz[i]*vz[i]
                slab,nSlab = checkSlab(i,zSave,z,a,slab,nSlab)
                index = int(z[i]/a)
                Tslab[index] += (vx[i]*vx[i]+vy[i]*vy[i]+vz[i]*vz[i])/3.0
                rhoslab[index] += 1

            totalKE *= 0.5
            cum['KE'] += totalKE
            cum['KE2'] += totalKE*totalKE
            cum['virial'] += virial
            if (iStep % 12) == 0: nn = setNNList(N,x,y,z,Lx,Ly,Lz,nn)  
            if (iStep % 10) == 0 and equil: vx,vy,vz = scaleVelocities(N,vx,vy,vz,desiredTemperature) 
            if (iStep % stepAdd == 0 and not equil): vx,vy,vz,cum['transfer'] = swapHotandCold(n,vx,vy,vz,slab,nSlab,cum['transfer'])
            if cum['n'] % nPlot == 0:
                 meanT = (2.0/3.0)*cum['KE']/(cum['n']*N)
                 T = (2.0/3.0)*totalKE/(N)
                 meanP = 1 + cum['virial']/(3*N*meanT*cum['n'])
                 P = 1 + virial/(3*N*T)
                 axs[0,1].plot(t,P,'ro')  #add new data
                 axs[0,1].plot(t,meanP,'go')  #add new data
                 axs[1,1].plot(t,T,'ro')  #add new data
                 axs[1,1].plot(t,meanT,'go')  #add new data
                 Tslab,rhoslab,dataST,dataSD,dTdz = analyzeSlabs(N,n,a,z,vx,vy,vz,Tslab,rhoslab,axs,dataST,dataSD,Lx,Ly,cum)
                 plt.pause(.000001) #needed to visualize data continuously
        meanT = (2.0/3.0)*cum['KE']/(cum['n']*N)
        sigma2 = cum['KE2']/cum['n'] - meanT*meanT*N*N
        print3f('density = N/A',N/(Lx*Ly*Lz))
        print3f('mean temperature',meanT)
        print3f('PV/NkT ', 1 + cum['virial']/(3*N*meanT*cum['n']))
        print3f('dT/dz',dTdz)
        print3f('transfer',cum['transfer'])
        print3f('conductivity', cum['transfer']/(2*t*Lx*Ly*dTdz))
        keepRunning = input('Continue/Stop/Reset/Swap hot and cold (c/s/r/sw)')
        if keepRunning == 'r':
            cum,axs,dataST,dataSD = reset(cum,axs)
            t = 0
            Tslab = np.zeros(3*n) 
            rhoslab = np.zeros(3*n)    
            keepRunning = 'c'
        elif keepRunning == 'sw':
            cum,axs,dataST,dataSD = reset(cum,axs)
            Tslab = np.zeros(3*n) 
            rhoslab = np.zeros(3*n)    
            t = 0
            equil = False
            keepRunning = 'c'
        if keepRunning != 's':
            runTime = float(input0("run time (dt = 0.01)", 10))
            plotTime = float(input0("plot time (dt = 0.01)", 0.1))
            nSteps = int(runTime/dt)
            nPlot = int(plotTime/dt) 

def analyzeSlabs(N,n,a,z,vx,vy,vz,Tslab,rhoslab,axs,dataST,dataSD,Lx,Ly,cum): 
    dataST.remove()
    dataSD.remove()
    arraySize = 3*n-2
    yT = np.zeros(arraySize)
    yrho = np.zeros(arraySize)
    zaxis = np.zeros(arraySize)
    zsum = 0
    z2sum = 0
    ysum = 0
    zysum = 0
    nsum = 0
    index = 0
    for islab in range(3*n):
        if(index < arraySize):
            zaxis[index] = islab*a
            if(islab > 3*n/2):
                zaxis[index] = n*3*a - zaxis[index] 	
            if(islab != 3*n/2 and islab != 0): # don't include hot and cold slabs
                yT[index] = Tslab[islab]/rhoslab[islab] 
                yrho[index] = rhoslab[islab]/(cum['n']*a*Lx*Ly)
                zsum += zaxis[index]
                z2sum += zaxis[index]*zaxis[index]
                ysum += yT[index]
                zysum += zaxis[index]*yT[index]
                nsum += 1
                index += 1
    dataST, = axs[0,0].plot(zaxis,yT,'bo') 
    dataSD, = axs[1,0].plot(zaxis,yrho,'bo')
    dTdz = ((zysum/nsum)- (zsum/nsum)*(ysum/nsum))/((z2sum/nsum) - (zsum/nsum)*(zsum/nsum))
    return Tslab,rhoslab,dataST,dataSD,dTdz
    
main()