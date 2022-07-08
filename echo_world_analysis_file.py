#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu May 26 14:09:06 2022

@author: davidking
"""

import pandas as pd
import matplotlib.pyplot as plt
import networkx as nx


# Now read the csv back in to ensure we now have floats instead of strings
df = pd.read_csv('echo_pop_50_genome_4_mutation_0001_1.csv')
#df = pd.read_csv('testing.csv')


# Now we need to find out how many unique ids we have per timestep
# this should give us the total population per time step that we can then
# plot to see how populations changed.  Similarly, we can then plot the 
# unique genomes(?).  The id is unique, but the tags could match, especially
# with asexual reproduction.  We do have mutations, so later, we may
# want to track family lineages, but one thing at a time.

ids = pd.unique(df.tag)
time = pd.unique(df.timestep)

occur = df.groupby(['parent']).size() # counts the number of times a parent occurs - shows success of genomes over time
occur = occur[:-1] # have to get rid of the [] parents, these were the original generation, last element
occur = occur.sort_values(ascending=True) # sorts the series from smallest occurance to largest

population = []
genomes = []
numUniqueGenomes = []

combats = []
trades = []
reproductions = []

for ts in time:
    ix = df.timestep == ts
    x = df.timestep[ix]
    gen = df.tag[ix]
    combats.append(sum(df.didIFight[ix]==True)/len(df.didIFight[ix]))
    trades.append(sum(df.didITrade[ix]==True)/len(df.didITrade[ix]))
    reproductions.append(sum(df.didIReproduce[ix]==True)/len(df.didIReproduce[ix]))
    population.append(len(x))
    genomes.append(gen.unique())
    numUniqueGenomes.append(len(gen.unique()))
    
timestep = len(time)
    
fig = plt.figure()
plt.plot(time,population)
plt.xlim([0,timestep])
plt.ylim([0,max(population)])
plt.xlabel("Time Step")
plt.ylabel("Population")
plt.show()

    
fig = plt.figure()
plt.plot(time,numUniqueGenomes)
plt.xlim([0,timestep])
plt.ylim([0,max(population)])
plt.xlabel("Time Step")
plt.ylabel("Unique Genomes")
plt.show()


fig = plt.figure()
plt.plot(time,combats, 'b')
plt.plot(time,trades, 'r')
plt.plot(time,reproductions, 'g')
plt.xlim([0,timestep])
plt.ylim([0,1])
plt.xlabel("Time Step")
plt.ylabel("Combat, Trade, Reproduction Over Time")
plt.legend(['Combat', 'Trade', 'Reproduction'])
plt.show()

# we can also look at the average size of tags, number of combats, trades etc.
avgOffense = 0
avgDefense = 0
avgMating = 0
avgTag = 0

for item in df['offense']:
    avgOffense += len(item)
avgOffense = avgOffense / len(df['offense'])

for item in df['defense']:
    avgDefense += len(item)
avgDefense = avgDefense / len(df['defense'])
    
for item in df['mating']:
    avgMating += len(item)
avgMating = avgMating / len(df['mating'])

for item in df['tag']:
    avgTag += len(item)
avgTag = avgTag / len(df['tag'])

# Builds predator and prey data
predatorPrey = df.loc[df['predators'] != 'NONE']

# Likely we will need to treat all of these as sets so we do not have repeats
#preySet = set(predatorPrey['tag'].tolist())
#predSet = set(predatorPrey['predators'].tolist())
elements = []
for row in predatorPrey.iterrows():
    elements.append((row[1]['predators'],row[1]['tag']))

# Unique tags only will be the nodes
# then create a directed edge list [predator,prey] -- ignore doubles
edgeSet = set(elements)

# then plot it

# -- test code -- #
graph = nx.DiGraph()
#graph.add_edges_from([("root", "a"), ("a", "b"), ("a", "e"), ("b", "c"), ("b", "d"), ("d", "e")])
graph.add_edges_from(edgeSet)
nx.draw(graph, with_labels=False, font_weight='bold')
plt.show()
