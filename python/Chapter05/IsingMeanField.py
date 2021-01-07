"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description:
     Computes mean field functions for m and Helmholtz function
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

fig, axs = plt.subplots(1, 2, figsize=(10, 4))
for ax, interp in zip(axs, ['tanh(m)', 'Helmholtz function']):
    ax.set_title(interp)
    ax.grid(True)  #add grid for plots
    if interp == 'tanh(m)':
        ax.set_xlabel('m')
        ax.set_ylabel('tanh(m)')
    else:
        ax.set_xlabel('m')
        ax.set_ylabel('f(m)')
plt.tight_layout()  #improve appearance of layout  
      
T = float(input0('T',1.0))
q = float(input0('q',4))
H = float(input0('H',0.0))
dm = 0.01
  
for ax, interp in zip(axs, ['tanh(m)', 'Helmholtz function']):
    if interp == 'tanh(m)':
        x = []
        y = []
        m = -2
        while m < 2:
            x.append(m)
            y.append(math.tanh((q*m + H)/T))
            m += dm
        ax.plot(x,y)
        ax.plot(x,x)
        ax.text(-1,1.5,'T = ' + str(T))
        ax.text(-1,1.0,'q = ' + str(q))
        ax.text(-1,0.5,'H = ' + str(H))
    else:
        x = []
        y = []
        m = -2
        while m < 2:
            x.append(m)
            y.append(0.5*q*m*m - T*math.log(2*math.cosh((q*m + H)/T)))
            m += dm
        ax.plot(x,y)
        plt.pause(0.001)
input('Press return to close')
