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
# The following runs had predation cycles and are worth more analysis
# 'echo_pop_50_genome_1_mutation_001_9.csv'
# 'echo_pop_50_genome_1_mutation_001_10.csv' 
# 'echo_pop_50_genome_1_mutation_0001_3.csv'
# 'echo_pop_50_genome_1_mutation_0001_6.csv'
# 'echo_pop_50_genome_1_mutation_0001_7.csv'
# 'echo_pop_50_genome_5_mutation_001_9.csv'


# may need to redo 0001_1, it had some weird data errors in it, running part 2 now
# to see if something similar happens. -- rerun part 1, something was wrong
df = pd.read_csv('echo_pop_50_genome_5_mutation_0001_10.csv') 
#df = pd.read_csv('testing.csv')


# Now we need to find out how many unique ids we have per timestep
# this should give us the total population per time step that we can then
# plot to see how populations changed.  Similarly, we can then plot the 
# unique genomes(?).  The id is unique, but the tags could match, especially
# with asexual reproduction.  We do have mutations, so later, we may
# want to track family lineages, but one thing at a time.

ids = pd.unique(df.tag)
time = pd.unique(df.timestep)

# I don't believe the next lines are true. I think we have double++ counting over time
# as the parent doesn't change between timesteps.  We would have to build sets of 
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
graph1 = nx.DiGraph()
graph1.add_edges_from(edgeSet)
nx.draw(graph1, with_labels=False, font_weight='bold')
plt.show()


# Another point would be to trace family lineages back through time to see if 
# sets of lineages dominate. Obviously, with high mutation rates we would argue
# that individual genotypes are species derived from our orignal 50 family
# lineages, but it would be interesting to see how long those original lines
# lasted.

# One final point is to trace trading over time.  This could (hopefully)
# show ant - caterpillar - fly triangles where ant -> fly -> caterpillar and
# caterpillar supplies ant with a trade commodity.  We alreay have double
# predation webs like ant -> fly -> caterpillar
tradePartners = df.loc[df['tradepartners'] != 'NONE']  
elementPartners = {}
for row in tradePartners.iterrows():
    tradeSet = set(row[1]['tradepartners'].split('_'))
    tempSet = {}
    for trader in tradeSet:
        tList = [trader,row[1]['tag']]
        tList.sort()
        tempSet.update({(tList[0],tList[1])})
    elementPartners.update(tempSet)

# Now the hard part, we have who trades with who, but now we have to cross reference
# those trades with predation... wonder if we can add the two together to a digraph
# in some manner to highlight the the different edges... that would be nice but probably
# harder to do?  Maybe build the map of trades first
tradeSet = []
for key in elementPartners.keys():
    tradeSet.append((key,elementPartners[key]))
tradeSet = set(tradeSet)

graph2 = nx.DiGraph()
graph2.add_edges_from(tradeSet)
nx.draw(graph2, with_labels=False, font_weight='bold')
plt.show()

# Graphs don't exactly show cycles well.  They show the complex webs of predation
# but the cycles may not be easy to find.
print("Predator - Prey Cycles:")
print(nx.recursive_simple_cycles(graph1))
print("Trade cycles:")
print(nx.recursive_simple_cycles(graph2))


# Wwe will iterate through the predator edge set list grabbing the victim
# we will then iterate though the trade set to see if the victim trades
# with a predator of the first predator - so far, this hasn't worked
# but I haven't tested a lot of data sets and there may be something
# else I need to consider here -- aka, this ain't perfect yet.
for p1 in edgeSet:
    victim = p1[1]
    for v1 in tradeSet:
        tPartner = []
        if victim == v1[0]:
            tPartner = v1[1]
        elif victim == v1[1]:
            tPartner = v1[0]
        if tPartner != []:
            for p2 in edgeSet:
                if tPartner == p2[1] and p1 == p2[0]:
                    print('Eureka!')
            


# Let's grab all the shortest paths in the predator graph
path = dict(nx.all_pairs_shortest_path(graph1,1))
# we have a dictionary of all the paths from each node to all other nodes
# It is implied that a -> b in the predator graph means a eats b.  We
# would need to check the trade edge set to see if there exists a 'c' such that
# b trades with c, or c trades with b (doesn't matter), AND c -> a. to create
# the an-catepillar-fly triangle that Holland theorized could come about in his
# echo world model
#keys = path.keys() #  get the keys for the predator paths

