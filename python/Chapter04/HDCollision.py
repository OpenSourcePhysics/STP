"""
Created : Thu Sep  5 14:26:24 2019
Author: Jan Tobochnik 
 Description:
    functions needed for Hard Disk program
    """
import numpy as np
import math
from myFunctions import *


def HDinitializeAuxiliaryArrays(N,Lx,Ly,x,y,vx,vy):
    partner = np.zeros(N)
    collisionTime = np.zeros(N)
    timeOfLastCollision = np.zeros(N)
    for i in range(N):
        partner[i] = N-1
    collisionTime[N-1] = 100000
    for i in range (N):
        uplist(i,N,collisionTime,Lx,Ly,partner,x,y,vx,vy)
    dx = np.zeros(N)
    dy = np.zeros(N)
    dx2 = np.zeros(20)
    dy2 = np.zeros(20)
    ndxdy = np.zeros(20)
    tD = 0
    return collisionTime,partner,timeOfLastCollision,tD,ndxdy,dx,dy,dx2,dy2

def checkCollision(i,j,x,y,vx,vy,Lx,Ly,collisionTime,partner):
    for xCell  in range(-1,2):
        for yCell  in range(-1,2):
            dx = x[i]-x[j]+xCell*Lx
            dy = y[i]-y[j]+yCell*Ly
            dvx = vx[i]-vx[j]
            dvy = vy[i]-vy[j]
            bij = dx*dvx+dy*dvy
            if bij<0:
                r2 = dx*dx+dy*dy
                v2 = dvx*dvx+dvy*dvy
                discr = bij*bij-v2*(r2-1)
                if discr>0:
                    tij = (-bij-math.sqrt(discr))/v2
                    if tij<collisionTime[i]:
                        collisionTime[i] = tij
                        partner[i] = j

def getMinimumCollisionTime(N,collisionTime,partner):
    tij = 1000000
    for k in range(N):
        if collisionTime[k]<tij:
            tij = collisionTime[k]
            minimumCollisionI = k
    minimumCollisionJ = partner[minimumCollisionI]
    return tij, minimumCollisionI,minimumCollisionJ


def move(tij,x,y,vx,vy,N,collisionTime,Lx,Ly,tD,tmax,ndxdy,dx,dy,dx2,dy2):
    delt = tmax/20
    if(tD + tij <tmax):
        tD = tD +tij
        index = int(tD/delt)
        ndxdy[index]+=1
        for k in range(N):
            dx[k] += vx[k]*tij
            dy[k] += vy[k]*tij
            dx2[index] += dx[k]*dx[k]
            dy2[index] += dy[k]*dy[k]
    else:
        tD = 0;
        for k in range(N):
            dx[k] = 0
            dy[k] = 0
    for k  in range(N):
        collisionTime[k] = collisionTime[k]-tij
        x[k] += vx[k]*tij
        y[k] += vy[k]*tij
        x[k] = pbc(x[k],Lx)
        y[k] = pbc(y[k],Ly)
    return tD


def contact(i,j,x,y,vx,vy,Lx,Ly):
    i = int(i)
    j = int(j)
    dx = sep(x[i]-x[j],Lx)
    dy = sep(y[i]-y[j],Ly)
    dvx = vx[i]-vx[j]
    dvy = vy[i]-vy[j]
    factor = dx*dvx+dy*dvy
    delvx = -factor*dx
    delvy = -factor*dy
    vx[i] = vx[i]+delvx
    vx[j] = vx[j]-delvx
    vy[i] = vy[i]+delvy
    vy[j] = vy[j]-delvy
    return delvx*dx+delvy*dy #virial



def resetList(i,j,N,partner,collisionTime,Lx,Ly,x,y,vx,vy):
    for k in range(N):
        test = partner[k];
        if ((k==i)or(test==i)or(k==j)or(test==j)):
            uplist(k,N,collisionTime,Lx,Ly,partner,x,y,vx,vy)
    downlist(i,x,y,vx,vy,Lx,Ly,collisionTime,partner)
    downlist(j,x,y,vx,vy,Lx,Ly,collisionTime,partner)


def downlist(j,x,y,vx,vy,Lx,Ly,collisionTime,partner):
    j = int(j)
    if(j==0):
        return
    for  i in range(j):
        checkCollision(i,j,x,y,vx,vy,Lx,Ly,collisionTime,partner)


def uplist(i,N,collisionTime,Lx,Ly,partner,x,y,vx,vy):
    if(i==(N-1)):
        return
    collisionTime[i] = 1000000
    for j in range(i+1,N):
        checkCollision(i,j,x,y,vx,vy,Lx,Ly,collisionTime,partner)
        
        
def updateMFP(i,j,timeOfLastCollision,time,vx,vy,mfp):
    i = int(i)
    j = int(j)
    ti = time-timeOfLastCollision[i]
    tj = time-timeOfLastCollision[j]
    dx = vx[i]*ti
    dy = vy[i]*ti
    mfp += math.sqrt(dx*dx+dy*dy)
    dx = vx[j]*tj
    dy = vy[j]*tj
    mfp += math.sqrt(dx*dx+dy*dy)
    timeOfLastCollision[i] = time
    timeOfLastCollision[j] = time
    return mfp
  
 
def HDstep(N,x,y,vx,vy,Lx,Ly,collisionTime,partner,timeOfLastCollision,time,mfp,tD,tmax,ndxdy,dx,dy,dx2,dy2):
    dt,minimumCollisionI,minimumCollisionJ = getMinimumCollisionTime(N,collisionTime,partner)
    tD = move(dt,x,y,vx,vy,N,collisionTime,Lx,Ly,tD,tmax,ndxdy,dx,dy,dx2,dy2)
    virial = contact(minimumCollisionI, minimumCollisionJ,x,y,vx,vy,Lx,Ly)
    resetList(minimumCollisionI, minimumCollisionJ,N,partner,collisionTime,Lx,Ly,x,y,vx,vy)
    mfp = updateMFP(minimumCollisionI,minimumCollisionJ,timeOfLastCollision,time,vx,vy,mfp)
    return virial,dt,mfp,tD
    
    