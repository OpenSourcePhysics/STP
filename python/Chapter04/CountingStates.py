
"""
Created on thursday August 22, 2019
@author: Jan Tobochnik
compares actual number of states with formula good for large energy or r
"""

import matplotlib.pyplot as plt
from myFunctions import *
import math


#start calculation
runAgain = 'y'
D = int(input0('dimension D = 1,2, or 3)',2))   #dimension of space
while runAgain == 'y': 
    if D < 4:
        R = int(input0('maximum R',10))
        number = np.zeros(R+1)
        func = np.zeros(R+1)
        rValues = np.arange(R+1)
        data1, = plt.plot(0,0)
        data2, = plt.plot(0,0)
        for r in range(R+1):
            number[r] = 0
            r2 = r*r 
            if D == 1:
                for nx in range(r+1):
                    if nx*nx < r2:
                        number[r] += 1
                func[r] = r
            elif D == 2:
                for nx in range(r+1):
                    for ny in range(r+1):
                        if nx*nx +ny*ny < r2:
                            number[r] += 1
                func[r] = math.pi*r2/4
            elif D == 3:
                for nx in range(r+1):
                    for ny in range(r+1):
                        for nz in range(r+1):
                            if nx*nx +ny*ny +nz*nz< r2:
                                number[r] += 1
                func[r] = (1.0/6.0)*math.pi*r*r*r            
            
        plt.close('all')
        data1.remove()
        data2.remove()
        plt.xlim(0,R)
        plt.xlabel('r')
        plt.title('NumberOfStates')
        data1, = plt.plot(rValues,number,'bo')  #plots points
        data2, = plt.plot(rValues,func)  #plots curve
        plt.pause(0.1)
 
    runAgain = input('Again? (y/n)')
    if runAgain == 'y':
        D = int(input0('dimension D = 1,2, or 3)',2))   #dimension of space
