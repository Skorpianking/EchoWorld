#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jul  7 10:55:58 2022

Python script that can generate a echoworld.json file.  These files will
be important in reproducing our results as well as testing various
hyperparamters such as mutation rates, genome lengths, etc.

@author: davidking
"""

'''
Notional template -- the key will be creating random tags which the program
will then break into individual tags (need to think about this as they can
                                      be random sized, 1-4, 1-1, 1-10, etc.)

tag = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition;
interActionTag
matingTag
offenseTag
defenseTag
tradingTag
combatCondition
tradeCondition
matingCondition


{
  "pixels_per_meter": 20,
  "agents": [
    {
      "interActionTag": "genome of length [1-x]",
      "matingTag": "genome of length [1-x]",
      "offenseTag": "genome of length [1-x]",
      "defenseTag": "genome of length [1-x]",
      "tradingTag": "genome of length [1]",
      "combatCondition": "genome of length [1-x]",
      "tradeCondition": "genome of length [1-x]",
      "matingCondition": "genome of length [1-x]",
      "position": [-5,-5],
    },{
      "interActionTag": "genome of length [1-x]",
      "matingTag": "genome of length [1-x]",
      "offenseTag": "genome of length [1-x]",
      "defenseTag": "genome of length [1-x]",
      "tradingTag": "genome of length [1]",
      "combatCondition": "genome of length [1-x]",
      "tradeCondition": "genome of length [1-x]",
      "matingCondition": "genome of length [1-x]",
      "position": [-5,-5],
    }
  ],
  "lights": [
    {
      "position": [18,15],
      "bound_key": 1
    }, {
      "position": [-18.0, -15.0]
    }
  ],
  "obstacles": [
    {
      "position": [-10,-10],
      "size": [2,4]
    }
  ]
}
'''

# should be a simple loop to create the agents that we can then write out to file
from random import choices, randint
import json

possibleTypes = ['A', 'B', 'C', 'D']
possibleLocations =[-25,25]

# silly function that builds our random strings.
def buildTag(length):
    retStr = ""
    for i in range(0,length):
        retStr += str(choices(list(possibleTypes), k=1)[0])
    return retStr

# secondary silly function to create a random position
def setPosition():
    retStr = "["
    retStr += str(randint(possibleLocations[0],possibleLocations[1])) # set x
    retStr += ","
    retStr += str(randint(possibleLocations[0],possibleLocations[1])) # set y
    retStr += "]"
    return retStr

#print(randint(1, 100))    # Pick a random number between 1 and 100.
tagStrArray = ["interActionTag", "matingTag", "offenseTag", "defenseTag", "tradingTag",
               "combatCondition", "tradeCondition", "matingCondition", "position"]

numAgents = 5
tagLength = 4
agent = "{\n"+"  \"pixels_per_meter\": 20,\n"+"  \"agents\": [\n"
for i in range(0,numAgents):
    agent += "\t{\n\t\""+tagStrArray[0]+"\": \"" + buildTag(randint(1,tagLength)) + "\",\n"
    agent += "\t\""+tagStrArray[1]+"\": \"" + buildTag(randint(1,tagLength)) + "\",\n"
    agent += "\t\""+tagStrArray[2]+"\": \"" + buildTag(randint(1,tagLength)) + "\",\n"
    agent += "\t\""+tagStrArray[3]+"\": \"" + buildTag(randint(1,tagLength)) + "\",\n"
    agent += "\t\""+tagStrArray[4]+"\": \"" + buildTag(randint(1,1)) + "\",\n"
    agent += "\t\""+tagStrArray[5]+"\": \"" + buildTag(randint(1,1)) + "\",\n"
    agent += "\t\""+tagStrArray[6]+"\": \"" + buildTag(randint(1,1)) + "\",\n"
    agent += "\t\""+tagStrArray[7]+"\": \"" + buildTag(randint(1,1)) + "\",\n"
    agent += "\t\""+tagStrArray[8]+"\": " + str(setPosition()) + ",\n\t}"
    if i+1 != numAgents:
        agent+= ","
agent += "],\n\t" + "\"lights\": [\n\t{\n\t " + "\t\"position\": [18,15],\n\t\t" + "\"bound_key\": 1\n" + "\t}, {\n\t\t"+"\"position\": [-18.0, -15.0]" + "\n\t}\n\t],\n\t"+"\"obstacles\": [\n\t{\t\n\t\t" +"\"position\": [-10,-10],\n\t\t" +  "\"size\": [2,4]\n\t}\n\t]\n}"

print(agent)

with open('echoworld_5agents_4lengthtags.json', 'w') as outfile:
    outfile.write(agent)


