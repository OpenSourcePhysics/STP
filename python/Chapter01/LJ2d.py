#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jul  6 14:27:05 2020

@author: jant
"""

import numpy as np
import matplotlib.pyplot as plt
import math
from myFunctions import *

def accelerationLJ(x,y,N,Lx,Ly,cut2):
    ax = np.zeros(N)
    ay = np.zeros(N)
    virial = 0
    for i in range(N-1):
        for j in range(i+1,N):
            dx = sep(x[i]-x[j],Lx)
            dy = sep(y[i]-y[j],Ly)
            r2 = dx*dx+dy*dy;
            if(r2<cut2):
                r2i = 1.0/r2
                r6i = r2i*r2i*r2i
                fOverR = 48.0*r6i*(r6i-0.5)*r2i
                fx = fOverR*dx
                fy = fOverR*dy
                ax[i] += fx
                ay[i] += fy
                ax[j] -= fx
                ay[j] -= fy
                virial += dx*fx+dy*fy
    return ax,ay,virial
      
def verlet(x,y,dx,dy,vx,vy,ax,ay,dt,N,Lx,Ly,cut2,cum,x2sum,y2sum):
    dt2half = 0.5*dt*dt
    dthalf = 0.5*dt
#    for i in range(N):
    x += vx*dt+ax*dt2half
    y += vy*dt+ay*dt2half
    dx += vx*dt+ax*dt2half
    dy += vy*dt+ay*dt2half
    index = int(cum['nr2'])
    cum['nr2'] += 1
    for i in range(N):
        x[i] = pbc(x[i],Lx)
        y[i] = pbc(y[i],Ly)
        x2sum[index] += dx[i]*dx[i]
        y2sum[index] += dy[i]*dy[i]

    vx += ax*dthalf
    vy += ay*dthalf
    ax,ay,virial = accelerationLJ(x,y,N,Lx,Ly,cut2)        
    #for i in range(N):
    vx += ax*dthalf
    vy += ay*dthalf
    return  x,y,dx,dy,vx,vy,ax,ay,virial,cum,x2sum,y2sum  

