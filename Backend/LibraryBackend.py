#!/usr/bin/env python3

from email.mime import image
from mimetypes import init
from flask import Flask, render_template, jsonify, json, request
from flask_sock import Sock
from simple_websocket.ws import ConnectionClosed
import library as LB
import book as BK
    
libraries = []
libraries.append( LB.Library(1,"Lisbon", 38.713912, -9.133397, False, 'espanha.txt') )
libraries.append( LB.Library(2,"Madrid", 40.416891, -3.703739, False,'espanha.txt') )
libraries.append( LB.Library(3,"Zaragoza", 41.657059,-0.875448, True,'espanha.txt') )
libraries.append( LB.Library(4,"Lagos", 6.476754, 3.368539, True, 'espanha.txt') )

libraries[0].addBook( BK.Book(45632, "Marco Polo", "Aventura para descobrir o amanha", 'espanha.txt') )
libraries[0].addBook( BK.Book(5346, "martim Manha", "Hoje tenho uma erecao", 'espanha.txt') )

# markers = []
# markers.append( lb.Library("Lisbon", 38.713912, -9.133397, False, 'espanha.txt').getLibraryInfo() )
# markers.append( lb.Library("Madrid", 40.416891, -3.703739, False,'espanha.txt').getLibraryInfo() )
# markers.append( lb.Library("Zaragoza", 41.657059,-0.875448, True,'espanha.txt').getLibraryInfo() )
# markers.append( lb.Library("Lagos", 6.476754, 3.368539, True, 'espanha.txt').getLibraryInfo() )


# def getLibraryMarkerInfo():
#   global libraries
#   result =[]
#   for library in libraries:
#     result.append


websocket_connections = []


# websocket_broadcast(state)


def websocket_broadcast(message):
  for ws in websocket_connections:
    try:
      ws.send(json.dumps(message))
    except ConnectionClosed:
      websocket_connections.remove(ws)


def get_library(library_id):
  global libraries
  for lib in libraries:
    if lib.id == library_id:
      return lib

def updateFavLibrary(library_id):
  global libraries
  for lib in libraries:
    if lib.id == library_id:
      lib.fav = not lib.fav


app = Flask(__name__)
sockets = Sock(app)


@app.route('/', methods=['GET'])
def index():
  return "Welcome to our project"


@app.route('/markers', methods=['GET'])
def get_state():
  global libraries
  return jsonify([library.getLibraryInfo() for library in libraries])
  # return jsonify(markers)

@app.route('/libraryExtras', methods=['GET'])
def getLibrayExtras():
  library_id = int(request.args.get("libraryId"))
  # print(library_id)
  lib = get_library(library_id)
  # print(lib)
  return jsonify(lib.getLibraryBooks())

@app.route('/favMarker', methods=['PUT'])
def put_state():
  global libraries
  data = json.loads(request.data)
  library_id = data["libraryId"]
  updateFavLibrary(library_id)

  # TODO: Nao sei o que dar return
  return "True"


@sockets.route('/ws')
def ws(ws):
  global websocket_connections, libraries
  websocket_connections.append(ws)

  while True:
    ws.send(json.dumps([library.getLibraryInfo() for library in libraries]))
    ws.receive()


from gevent import monkey
monkey.patch_all()
from gevent.pywsgi import WSGIServer
WSGIServer(('0.0.0.0', 5000), app).serve_forever()
