"""
Created : Mon Sep  9  2019
Author: Jan Tobochnik 
 Description:implements the Newman-Ziff algorithm for identifying clusters
    """
import numpy as np
import matplotlib.pyplot as plt
from numpy.random import random as rnd

def initialize(L,N):
    numClusters = np.zeros(N+1)   # number of clusters of size s, n_s
  # secondClusterMoment stores sum{s^2 n_s}, where sum is over all clusters (not counting spanning cluster)
  # first cluster moment, sum{s n_s} equals  numSitesOccupied.
  # mean cluster size S is defined as S = secondClusterMoment/numSitesOccupied

    secondClusterMoment = 0
  # spanningClusterSize, number of sites in a spanning cluster; 0 if it doesn't exist
  # assume at most one spanning cluster

    spanningClusterSize = 0
  # order[n] gives index of nth occupied site; contains all numbers from
  # [1...N], but in random order. For example, order[0] = 3 means
  # we will occupy site 3 first. An alternative to using order array is
  # to choose sites at random until we find an unoccupied site.

    order = np.zeros(N)
  # parent[] array serves three purposes: stores cluster size when site
  # is root. Otherwise, it stores index of the site's "parent" or is
  # EMPTY. The root is found from an occupied site by recursively following the
  # parent array. Recursion terminates when we encounter a negative value in the
  # parent array, which indicates we have found the unique cluster root.
  # if (parent[s] >= 0) parent[s] is parent site index
  # if (0 > parent[s] > EMPTY) s is root of size -parent[s]
  # if (parent[s] == EMPTY) site s is empty (unoccupied)

    parent = np.zeros(N)
  # A spanning cluster touches both left and right boundaries of lattice.
  # As clusters are merged, we maintain this information in following arrays
  # at roots. For example, if root of a cluster is at site 7 and this cluster
  # touches the left side, then touchesLeft[7] == true.

    touchesLeft = np.full(N,False)
    touchesRght = np.full(N,False)
    return numClusters,secondClusterMoment,spanningClusterSize,order,parent,touchesLeft,touchesRght 
    
def newLattice(numClusters,parent,touchesLeft,touchesRght,order,L,N):
    setOccupationOrder(N,order); #choose order in which sites are occupied
    #initially all sites are empty, and there are no clusters
    numSitesOccupied = 0
    secondClusterMoment =0
    spanningClusterSize = 0
    for s in range(N):
        numClusters[s] = 0
        parent[s] = -1E10

    #initially left boundary touchesLeft, right boundary touchesRight
    for s in range(N):
        touchesLeft[s] = (s%L==0)
        touchesRght[s] = (s%L==L-1)
    return numSitesOccupied, secondClusterMoment, spanningClusterSize

def addRandomSite(numSitesOccupied,numClusters,secondClusterMoment,spanningClusterSize,parent,order,touchesLeft,touchesRght,L,N):
    #if all sites are occupied, we can't add anymore
    if numSitesOccupied==N:
        return

    #newSite is index of random site to be occupied
    newSite = order[numSitesOccupied]
    numSitesOccupied += 1
    #creates a new cluster containing only site newSite.
    numClusters[1] += 1
    secondClusterMoment += 1
    # store new cluster's size in parent[]; negative sign distinguishes
    # newSite as a root, with a size value. Positive values correspond
    # to non-root sites with index pointers.
    parent[int(newSite)] = -1
    #merge newSite with occupied neighbors.  root is index of merged cluster root at each step
    root = newSite
    for  j in range(4):
      #neighborSite is jth site neighboring newly added site newSite
      neighborSite = int(getNeighbor(newSite, j,L))
      if (neighborSite!=-1E10) and (parent[neighborSite]!=-1E10):
          root,secondClusterMoment,spanningClusterSize = mergeRoots(root, findRoot(neighborSite,parent),parent,numClusters,secondClusterMoment,spanningClusterSize,touchesLeft,touchesRght)
    return numSitesOccupied,secondClusterMoment,spanningClusterSize

def getClusterSize(s,parent):
    if parent[int(s)] == -1E10:
        return 0
    else:
        return -parent[int(findRoot(s,parent))] 

#returns S (mean cluster size); sites belonging to spanning cluster not counted in cluster moments
def getMeanClusterSize(spanningClusterSize,secondClusterMoment,numSitesOccupied):
    spanSize = spanningClusterSize
    #subtract sites in spanning cluster
    correctedSecondMoment = secondClusterMoment-spanSize*spanSize;
    correctedFirstMoment = numSitesOccupied-spanSize;
    if correctedFirstMoment>0: 
      return correctedSecondMoment/correctedFirstMoment
    else:
      return 0

def findRoot(s,parent):
    s = int(s)
    if(parent[s]<0):
        return s # root site (with size -parent[s])
    else:
      # first link parent[s] to the cluster's root to improve performance
      # (path compression); then return this value.
      parent[s] = findRoot(parent[s],parent)
    return parent[s]

#returns jth neighbor of site s; j can be 0 (left), 1 (right), 2 (down), or 3
#(above). If no neighbor exists because of boundary, return value EMPTY.
#change this method for periodic boundary conditions.
#compare this method to same method in PercolationApp
def getNeighbor(s,j,L):
    if j == 0:
        if s%L==0:
            return -1E10
        else:
            return s-1 #left
    elif j == 1:
        if s%L==L-1:
            return -1E10
        else:
            return s+1 #right
    elif j == 2:
        if int(s/L)==0:
            return -1E10
        else:
            return s-L #down
    elif j == 3:
        if int(s/L)==L-1:
            return -1E10
        else:
            return s+L #up
    else:
         return -1E10

  #fills order[] array with random permutation of site indices. First order[]
  #is set to the identity permutation. Then for values of i in {1...N-1}, swap
  #values of order[i] with order[r], where r is a random index in {i+1 ...N}.
def setOccupationOrder(N,order):
    for s in range(N):
        order[s] = s
    for s in range(N-1):
        r = s+ int(rnd()*(N-s))
        temp = order[s]
        order[s] = order[r]
        order[r] = temp

#merges two root sites into one to represent cluster merging.
#use heuristic that root of smaller cluster points to root of larger
#cluster to improve performance.
#parent[root] stores negative cluster size.
def mergeRoots(r1,r2,parent,numClusters,secondClusterMoment,spanningClusterSize,touchesLeft,touchesRght):
    #clusters are uniquely identified by their root sites. If they
    #are the same, clusters are already merged, and we need do nothing
    r1 = int(r1)
    r2 = int(r2)
    if(r1==r2):
        return r1,secondClusterMoment,spanningClusterSize
      #f r1 has smaller cluster size than r2, reverse (r1,r2) labels
    elif(-parent[r1]<-parent[r2]):
          return mergeRoots(r2,r1,parent,numClusters,secondClusterMoment,spanningClusterSize,touchesLeft,touchesRght)
    else:
          #(-parent[r1] > -parent[r2])
          #update cluster count, and second cluster moment to account for
          #loss of two small clusters and gain of one bigger cluster
          numClusters[-int(parent[r1])] -= 1
          numClusters[-int(parent[r2])] -= 1
          numClusters[-int(parent[r1])-int(parent[r2])] += 1
          secondClusterMoment += (parent[r1]+parent[r2])**2-(parent[r1])**2-(parent[r2])**2
      #cluster at r1 now includes sites of old cluster at r2
          parent[r1] += parent[r2]
      #make r1 new parent of r2
          parent[r2] = r1
      #if r2 touched left or right, then so does merged cluster r1
          touchesLeft[r1] = touchesLeft[r1] or touchesLeft[r2];
          touchesRght[r1] = touchesRght[r1] or touchesRght[r2];
      #if cluster at r1 spans lattice, then remember its size
          if(touchesLeft[r1] and touchesRght[r1]):
              spanningClusterSize = -parent[r1]
      # return new root site r1
          return r1,secondClusterMoment,spanningClusterSize
   

