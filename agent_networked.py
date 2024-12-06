#!/usr/bin/env python
# ------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ------------------------------------------------------------------------
 
import time
import sys
import os
import stomp
import json

user = os.getenv("ACTIVEMQ_USER") or "admin"
password = os.getenv("ACTIVEMQ_PASSWORD") or "password"
host = os.getenv("ACTIVEMQ_HOST") or "localhost"
port = os.getenv("ACTIVEMQ_PORT") or 61613
destination = sys.argv[1:2] or ["/state/"]
destination = destination[0]

actionChannel = sys.argv[1:2] or ["/action/"]
actionChannel = actionChannel[0]
print(actionChannel)

activeConn = stomp.Connection(host_and_ports = [(host, port)])
#activeConn.start()
activeConn.connect(login=user,passcode=password)

class MyListener(object):
  
  def __init__(self, conn, actionConn):
    self.conn = conn
    self.count = 0
    self.start = time.time()
    self.actionConn = actionConn
  
  def on_error(self, headers, message):
    print('received an error %s' % message)

  def on_message(self, message):
    if message == "SHUTDOWN":
    
      diff = time.time() - self.start
      print("Received %s in %f seconds" % (self.count, diff))
      
      conn.disconnect()
      sys.exit(0)
      
    else:
      print(message)

      jsonMsg = {'leftWheelVelocity':0.9, 'rightWheelVelocity':0.5, 'pickup':'false', 'drop':'false'}
      stringMsg = json.dumps(jsonMsg)  # Convert json object to text
 
      jsonObj = json.loads(stringMsg)   # Parse text to json object

      self.actionConn.send(body=stringMsg, destination="/action/", persistent='false')

      
      if self.count==0:
        self.start = time.time()
        
      self.count += 1
      if self.count % 1000 == 0:
         print("Received %s messages." % self.count)

conn = stomp.Connection(host_and_ports = [(host, port)])
conn.set_listener('', MyListener(conn,activeConn))
conn.connect(login=user,passcode=password)
conn.subscribe(destination=destination, id=1, ack='auto')
print("Waiting for messages...")
while 1: 
  time.sleep(10) 
