#!/usr/bin/env python3

from email.mime import image
from mimetypes import init
from flask import Flask, render_template, jsonify, json, request
from flask_sock import Sock
from simple_websocket.ws import ConnectionClosed


def getEncodedImage(image_file):
  with open(image_file, 'r') as file:
    return file.read()


class Library:
    def __init__(self, name, lat, lng, fav, image_file):
        self.name = name
        self.lat = lat
        self.lng = lng
        self.fav = fav
        self.image_file = image_file

    def getMarker(self):
      return {'name': self.name, 'lat': self.lat, 'lnt': self.lng, 'fav' : self.fav}
    
    def getLibraryInfo(self):
      return {'name': self.name, 'lat': self.lat, 'lnt': self.lng, 'fav' : self.fav, 'encodedImage' : getEncodedImage(self.image_file)}
    
libraries = []
libraries.append( Library("Lisbon", 38.713912, -9.133397, False, 'espanha.txt').getMarker() )
libraries.append( Library("Madrid", 40.416891, -3.703739, False,'espanha.txt').getMarker() )
libraries.append( Library("Zaragoza", 41.657059,-0.875448, True,'espanha.txt').getMarker() )
libraries.append( Library("Lagos", 6.476754, 3.368539, True, 'espanha.txt').getMarker() )

markers = []
markers.append( Library("Lisbon", 38.713912, -9.133397, False, 'espanha.txt').getLibraryInfo() )
markers.append( Library("Madrid", 40.416891, -3.703739, False,'espanha.txt').getLibraryInfo() )
markers.append( Library("Zaragoza", 41.657059,-0.875448, True,'espanha.txt').getLibraryInfo() )
markers.append( Library("Lagos", 6.476754, 3.368539, True, 'espanha.txt').getLibraryInfo() )



websocket_connections = []


# websocket_broadcast(state)


def websocket_broadcast(message):
  for ws in websocket_connections:
    try:
      ws.send(json.dumps(message))
    except ConnectionClosed:
      websocket_connections.remove(ws)


app = Flask(__name__)
sockets = Sock(app)


@app.route('/', methods=['GET'])
def index():
  return "Welcome to our project"


@app.route('/markers', methods=['GET'])
def get_state():
  # print("Recebi pedido de markers")
  global markers
  # m = jsonify(libraries)
  # print("Vou enviar -> " + str(m))
  return jsonify(markers)




# @app.route('/state', methods=['PUT'])
# def put_state():
#   global state
#   change_state(json.loads(request.data))
#   return jsonify(state)


@sockets.route('/ws')
def ws(ws):
  global websocket_connections
  websocket_connections.append(ws)

  while True:
    ws.send(json.dumps(markers))
    ws.receive()


from gevent import monkey
monkey.patch_all()
from gevent.pywsgi import WSGIServer
WSGIServer(('0.0.0.0', 5000), app).serve_forever()
