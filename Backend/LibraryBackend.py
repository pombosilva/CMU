#!/usr/bin/env python3

from email.mime import image
from mimetypes import init
from flask import Flask, render_template, jsonify, json, request
from flask_sock import Sock
from simple_websocket.ws import ConnectionClosed
from gevent import monkey
from gevent.pywsgi import WSGIServer
import library as LB
import book as BK

libraries = [LB.Library(1, "Lisbon", 38.713912, -9.133397, False, 'madrid.txt'),
             LB.Library(2, "Madrid", 40.416891, -3.703739, False, 'madrid.txt'),
             LB.Library(3, "Zaragoza", 41.657059, -0.875448, True, 'zaragoza.txt'),
             LB.Library(4, "Lagos", 6.476754, 3.368539, True, 'lagos.txt')]

libraries[0].addBook(BK.Book(45632, "Marco Polo", "Aventura para descobrir o amanha", 'espanha.txt', 123))
libraries[0].addBook(BK.Book(5346, "martim Manha", "Hoje tenho uma erecao", 'espanha.txt', 123))


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
    library_strings = []
    for library in libraries:
        library_strings.append(str(library))
    return library_strings


@app.route('/books', methods=['GET'])
def showAllBooks():
    global libraries
    list = []
    for library in libraries:
        l = library.getLibraryBooks()
        for book in l:
            list.append(book)
    return jsonify(list)


@app.route('/libraryBooks/<int:libraryId>', methods=['GET'])
def showLibraryBooks(libraryId):
    global libraries
    library_books = None
    for library in libraries:
        if library.id == libraryId:
            library_books = library.getLibraryBooks()
            break
    return jsonify(library_books)


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
    updateFavLibrary(data)
    return "True"


@sockets.route('/ws')
def ws(ws):
    global websocket_connections, libraries
    websocket_connections.append(ws)

    while True:
        ws.send(json.dumps([library.getLibraryInfo() for library in libraries]))
        ws.receive()


monkey.patch_all()
WSGIServer(('0.0.0.0', 5000), app).serve_forever()
