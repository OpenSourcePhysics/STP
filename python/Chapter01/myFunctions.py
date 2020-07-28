"""
Created : Thu Aug  8 09:28:36 2019
Author: Jan Tobochnik 
 Description:
    """
import numpy as np
import matplotlib.pyplot as plt
import math

def input0(string,default):
    defaultString = str(default)
    userInput = input(string+' (default = ' + defaultString +'): ')
    if userInput == '':
        return default
    else:
        return userInput
    

def print2f(name,value):
    print(name + ' = {:.2f}'.format(value))
    
def print3f2(name1,value1,name2,value2):
    print(name1 + ' = {:.3f}'.format(value1)+ name2 + ' = {:.3f}'.format(value2))
    
def print3f(name,value):
    print(name + ' = {:.3f}'.format(value))
   
def printi(name,value):
    print(name + ' = {:.0f}'.format(value))

def printf(name,value,digits):
    d = ' = {:.'+str(digits)+'f}'
    print(name + d.format(value))
    
def sep(ds,L):
    if abs(ds) < L/2:
        return ds
    elif ds > 0:
        if ds > L: print(ds)
        return ds - L
    else:
        if ds < -L: print(ds)
        return ds + L
    
def pbc(s,L):
    if (s > 0) and (s < L):
        return s
    elif s < 0:
        return s+L
    else:
        return s-L
    
def rdfSetup(lx,ly):
    L = lx/2
    if lx > ly: L = ly/2
    nbins = int(L/0.1)
    g =  np.zeros((2,nbins))
    for i in range(int(np.size(g)/2)):
        g[1,i] = i*0.1
    return g

def rdfUpdate(x,y,g,N,lx,ly):
    size = np.size(g)/2
    for i in range (N-1):
        for j in range(i+1,N):
            dx = sep(x[i]-x[j],lx)
            dy = sep(y[i]-y[j],ly)
            r = math.sqrt(dx*dx+dy*dy)
            index = int(r/0.1)
            if index < size:
                g[0,index] += 1
    return g

def rdfNormalize(g,lx,ly,N,Nupdates):
    gn = np.copy(g)
    for index in range(int(np.size(g)/2)):
        r = index/10
        shellArea = math.pi*((r+0.1)**2 - r*r)
        rho = N/(lx*ly)
        gn[0,index] /= shellArea*rho*Nupdates*N*0.5
        
    return gn


