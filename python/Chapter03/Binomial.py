"""
Created : Fri Aug 25, 2019
Author: Jan Tobochnik 
 Description: Plot Binomial distribution
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

plt.close('all')
fig, axs = plt.subplots(1, 2, figsize=(10, 4))
for ax, interp in zip(axs, ['P(n)', 'P(n)/<n>']):
    ax.set_title(interp)
    ax.grid(True)  #add grid for plots
    if interp == 'tanh(m)':
        ax.set_xlabel('n')
        ax.set_ylabel('P')
    else:
        ax.set_xlabel('n/<n>')
        ax.set_ylabel('P)')
plt.tight_layout()  #improve appearance of layout  
     
runAgain = 'a'
while runAgain == 'a':
    N = int(input0('N',60))
    p = float(input0('p',0.5))
    x = []
    y = []
    logN = math.log(math.factorial(N))
    logp = math.log(p)
    logp1 = math.log(1-p)
    for n in range(N):
        logn = math.log(math.factorial(n))
        logn1 = math.log(math.factorial(N-n))
        prob = math.exp(logN-logn-logn1+n*logp+(N-n)*logp1)
        x.append(n)
        y.append(prob)
    
    for ax, interp in zip(axs, ['P(n)', 'P(n)/<n>']):
        if interp == 'P(n)':
            data1, = ax.plot(x,y,'o')
        else:
            nbar = N*p
            for n in range(N):
                x[n] = x[n]/nbar
            data2, = ax.plot(x,y,'o')
    plt.pause(0.001)
    runAgain = input('Another/Stop (a/s)')

