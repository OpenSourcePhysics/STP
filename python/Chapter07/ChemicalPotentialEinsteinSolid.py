"""
Created : Fri Aug 23 10:20:46 2019
Author: Jan Tobochnik 
 Description: Calculation of chemical potential for two Einstein solids in thermal contact 
    """
import numpy as np
import math
import matplotlib.pyplot as plt
from myFunctions import *

def main():
    columns = ('$N_A$','$\u03a9_A(N_A)$', '$ln \u03a9_A(N_A)$', '$\u03bc_A/kT$', '$N_B$', '$\u03a9_B(N_B)$','$ln \u03a9_B(N_B)$', '$\u03bc_B/kT$','$\u03a9_A \u03a9_B$' )  # for plots
    runAgain = 'y'
    # column headings
    while runAgain == 'y':
        data = []
        Ea = int(input0('Ea',8))
        Eb = int(input0('Eb',5))
        N = int(input0('N',10))
        NaList  =np.arange(1,N)
        totalStates = np.zeros(N-1)
        lnOmegaA = np.zeros(N-1)
        lnOmegaB = np.zeros(N-1)
        totalStates = np.zeros(N-1)
        for Na in range(1,N):
            row = []
            OmegaA = binom(Ea+Na-1, Ea)
            lnOmegaA[Na-1] = math.log(OmegaA)
            Nb = N - Na
            OmegaB = binom(Eb+Nb-1, Eb)
            lnOmegaB[Na-1] = math.log(OmegaB)
            row.append(str(Na))
            row.append('{:.0f}'.format(OmegaA))
            row.append('{:.2f}'.format(lnOmegaA[Na-1]))
            row.append(' ')  # calculate mu later
            row.append(str(Nb))
            row.append('{:.0f}'.format(OmegaB))
            row.append('{:.2f}'.format(lnOmegaB[Na-1]))
            row.append(' ')# calculate mu later
            row.append('{:.0f}'.format(OmegaA*OmegaB))
            totalStates[Na-1] = OmegaA*OmegaB
            data.append(row) 
        for Na in range (2,N-1):
            muA =  (lnOmegaA[Na-2] - lnOmegaA[Na])/2
            muB =  (lnOmegaB[Na-2] - lnOmegaB[Na])/2
            data[Na-1][3] = '{:.2f}'.format(muA)
            data[Na-1][7] = '{:.2f}'.format(muB)
            
        plt.close('all')
        fig, axs = plt.subplots(1, 2,figsize=(15, N/2.5))       
        for ax, interp in zip(axs, ['States', 'Table']):
            if interp == 'States':
                ax.set_xlabel('Na')
                ax.set_title('Total number of states')
                ax.plot(NaList,totalStates,'bo')
            else:
                ax.axis('tight')
                ax.axis('off')
                the_table = ax.table(cellText = data,colLabels=columns, loc='center',fontsize = 5)
                the_table.auto_set_font_size(False)
                the_table.set_fontsize(9)
                the_table.scale(1.3, 1.8)
        plt.pause(0.1)
        runAgain = input('Continue? (y/n)')

        
                             

def binom(N,n):
    product = 1.0
    i = N
    j = n
    while (i>=N-n+1) and (j>=1):
        product *= i
        product /= j
        j-= 1
        i-= 1
    return product

main()