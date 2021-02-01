"""
Created : Tues Dec  31  2019
Author: Jan Tobochnik 
 Description: functions used for Conductivity and Viscosity programs
    """
import numpy as np
import matplotlib.pyplot as plt
import math
from numpy.random import random as rnd
from myFunctions import *

def accelerationLJ3d(x,y,z,N,Lx,Ly,Lz,nn,PEaccumulator):
    ax = np.zeros(N)
    ay = np.zeros(N)
    az = np.zeros(N)
    virial = 0
    for i in range(N-1):
        nnIndex = 0
        while nn[i][nnIndex] != -1:
            j = int(nn[i][nnIndex])
            dx = sep(x[i]-x[j],Lx)
            dy = sep(y[i]-y[j],Ly)
            dz = sep(z[i]-z[j],Lz)
            r2 = dx*dx+dy*dy+dz*dz;
            if(r2<9):
                r2i = 1.0/r2
                r6i = r2i*r2i*r2i
                fOverR = 48.0*r6i*(r6i-0.5)*r2i
                PEaccumulator += 4.0*(r6i*r6i-r6i)
                fx = fOverR*dx
                fy = fOverR*dy
                fz = fOverR*dz
                ax[i] += fx
                ay[i] += fy
                az[i] += fz
                ax[j] -= fx
                ay[j] -= fy
                az[j] -= fz
                virial += fx*dx+fy*dy+fz*dz
            nnIndex += 1
    return ax,ay,az,virial,PEaccumulator
      

def verlet3d(x,y,z,vx,vy,vz,ax,ay,az,dt,N,Lx,Ly,Lz,nn,PEaccumulator):
    dt2half = 0.5*dt*dt
    dthalf = 0.5*dt
#    for i in range(N):
    x += vx*dt+ax*dt2half
    y += vy*dt+ay*dt2half
    z += vz*dt+az*dt2half
    for i in range(N):
        x[i] = pbc(x[i],Lx)
        y[i] = pbc(y[i],Ly)
        z[i] = pbc(z[i],Lz)
    vx += ax*dthalf
    vy += ay*dthalf
    vz += az*dthalf
    ax,ay,az,virial,PEaccumulator = accelerationLJ3d(x,y,z,N,Lx,Ly,Lz,nn,PEaccumulator)        
    #for i in range(N):
    vx += ax*dthalf
    vy += ay*dthalf
    vz += az*dthalf
    return  x,y,z,vx,vy,vz,ax,ay,az,virial,PEaccumulator 


def setVelocities(N,vx,vy,vz,desiredTemperature):
    vxSum = 0.0
    vySum = 0.0
    vzSum = 0.0
    for i in range(N):          # assign random initial velocities
        vx[i] = rnd()-0.5
        vy[i] = rnd()-0.5
        vz[i] = rnd()-0.5
        vxSum += vx[i]
        vySum += vy[i]
        vzSum += vz[i]
    # zero center of mass momentum
    vxcm = vxSum/N  # center of mass momentum (velocity)
    vycm = vySum/N
    vzcm = vzSum/N
    for i in range(N): 
        vx[i] -= vxcm;
        vy[i] -= vycm;
        vz[i] -= vzcm;
    vx,vy,vz = scaleVelocities(N,vx,vy,vz,desiredTemperature)
    return vx,vy,vz

def scaleVelocities(N,vx,vy,vz,desiredTemperature):
    v2sum = 0
    for i in range(N): 
        v2sum += vx[i]*vx[i]+vy[i]*vy[i]+vz[i]*vz[i]
    scale = math.sqrt(3*N*desiredTemperature/v2sum)
    for i in range(N): 
        vx[i] *=scale;
        vy[i] *=scale;
        vz[i] *=scale;
    return vx,vy,vz

def setNNList(N,x,y,z,Lx,Ly,Lz,nn):
    for i in range(N-1):
        nn[i][0] = -1
        nnIndex = 0
        for j in range(i+1,N):
            dx = sep(x[j]-x[i],Lx)
            dy = sep(y[j]-y[i],Ly)
            dz = sep(z[j]-z[i],Lz)
            r2 = dx*dx+dy*dy+dz*dz
            if r2 < 16:
                nn[i][nnIndex] = j
                nnIndex += 1
                nn[i][nnIndex] = -1
    return nn


def setPositions(N,x,y,z,Lx,Ly,Lz,a,slab,nSlab):
    xs = a/2
    ys = a/2
    zs = a/2
    for i in range(N):
        x[i] = xs   
        y[i] = ys
        z[i] = zs
        xs +=a
        if xs > Lx:
        	xs = a/2
        	ys += a
        	if ys > Ly:
        		ys = a/2;
        		zs += a;
        slabNumber = int(x[i]/a)
        slab[slabNumber][int(nSlab[slabNumber])] = i
        nSlab[slabNumber] += 1
    return x,y,z,slab,nSlab


def checkSlab(i,zSave,z,a,slab,nSlab):
    slabold = int(zSave[i]/(a))
    slabnew = int(z[i]/(a))
    if(slabnew != slabold):
        slab[slabnew][int(nSlab[slabnew])] = i
        nSlab[slabnew]+=1
        ns = 0
        notfound = True;
        while (notfound and ns < nSlab[slabold]):
            if(slab[slabold][ns] == i ):
                nSlab[slabold] -= 1
                slab[slabold][ns]  = slab[slabold][int(nSlab[slabold])]
                notfound = False
            ns+= 1
    return slab,nSlab

def swapHotandCold(n,vx,vy,vz,slab,nSlab,transfer):
    hot = 0
    cold = 1E10
    ihot = 0
    icold = 0
    for j in range(int(nSlab[0])):
        i = int(slab[0][j])
        v2 = vx[i]*vx[i] + vy[i]*vy[i] + vz[i]*vz[i]
        if(v2 > hot):
            hot = v2
            ihot = i
    for j in range(int(nSlab[int(3*n/2)])):
        i = int(slab[int(3*n/2)][j])
        v2 = vx[i]*vx[i] + vy[i]*vy[i] + vz[i]*vz[i]
        if(v2 < cold):
            cold = v2
            icold = i
    vxh = vx[ihot]
    vyh = vy[ihot]
    vzh = vz[ihot]
    vx[ihot]= vx[icold]
    vy[ihot]= vy[icold]
    vz[ihot]= vz[icold]
    vx[icold] = vxh
    vy[icold] = vyh
    vz[icold] = vzh
    transfer += 0.5*(hot-cold)
    return vx,vy,vz,transfer

def swapMomenta(n,vx,vy,vz,slab,nSlab,transfer):
    phigh = -1E10
    plow = 1E10
    ihigh = 0
    ilow = 0
    for j in range(int(nSlab[0])):
        i = int(slab[0][j])
        vxi = vx[i]
        if(vxi > phigh):
            phigh = vxi
            ihigh = i
    for j in range(int(nSlab[int(3*n/2)])):
        i = int(slab[int(3*n/2)][j])
        vxi = vx[i]
        if(vxi < plow):
            plow = vxi
            ilow= i
    vxh = vx[ihigh]
    vx[ihigh]= vx[ilow]
    vx[ilow] = vxh
    transfer += 0.5*(phigh-plow)
    return vx,vy,vz,transfer
    