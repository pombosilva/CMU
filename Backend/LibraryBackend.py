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

books = [BK.Book(1234567, "Biblia", "palavra de deus", "BookPics/bible.txt"),
         BK.Book(1234, "Harry poter", "feiticos", "BookPics/harry.txt"),
         BK.Book(98, "Ben 10", "bue fixe", 'BookPics/ben.txt'),
         BK.Book(43292, "Geronimo Stilton", "Rolemodel", "BookPics/g_ronimo.txt"),
         BK.Book(12345678, "Manual de portugues 8ano", "Camoes glorioso", "BookPics/manual.txt")]

libraries = [LB.Library(0, "Lisbon", 38.713912, -9.133397, False, 'LibraryPics/madrid.txt'),
             LB.Library(1, "Madrid", 40.416891, -3.703739, False, 'LibraryPics/madrid.txt'),
             LB.Library(2, "Zaragoza", 41.657059, -0.875448, True, 'LibraryPics/zaragoza.txt'),
             LB.Library(3, "Lagos", 6.476754, 3.368539, True, 'LibraryPics/lagos.txt')]

libraries[0].addBook(books[0])
libraries[0].addBook(books[1])
libraries[1].addBook(books[2])

websocket_connections = []


def websocket_broadcast(message):
    for ws in websocket_connections:
        try:
            ws.send(json.dumps(message))
        except ConnectionClosed:
            websocket_connections.remove(ws)


# def addBookToLibrary(atributos do livro, id da livraria):
# criar o objecto livro e coloca lo no array dos books
# dar append desse livro ao array de livro da livraria

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
    return [str(library) for library in libraries]


@app.route('/books', methods=['GET'])
def showAllBooks():
    global books
    return jsonify([book.getBookInfo() for book in books])


@app.route('/libraryBooks/<int:libraryId>', methods=['GET'])
def showLibraryBooks(libraryId):
    global libraries
    library = next((l for l in libraries if l.id == libraryId), None)
    return jsonify([book.getBookInfo() for book in library.registered_books])


@app.route('/markers', methods=['GET'])
def get_state():
    global libraries
    return jsonify([library.getMarkerInfo() for library in libraries])


@app.route('/libraryExtras/<int:libraryId>', methods=['GET'])
def getLibraryImage(libraryId):
    global libraries
    for l in libraries:
        if l.id == libraryId:
            return jsonify(l.getLibraryImage())


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


@app.route('/bookExistence/<string:bookBarcode>', methods=['GET'])
def bookExists(bookBarcode):
    bookBarcode = int(bookBarcode)
    global books
    for b in books:
        if b.id == bookBarcode:
            return jsonify(True)
    return jsonify(False)


@app.route('/getBook/<string:bookBarcode>', methods=['GET'])
def getBook(bookBarcode):
    bookBarcode = int(bookBarcode)
    global books
    for b in books:
        if b.id == bookBarcode:
            return jsonify(b.getBookInfo())
    return jsonify(False)


@app.route('/checkBookIn', methods=['PUT'])
def checkBookIn():
    data = json.loads(request.data)
    bookId = data['bookId']
    libraryId = data['libraryId']
    global books, libraries
    for b in books:
        if b.id == bookId:
            libraries[libraryId].addBook(b)
            return "True"


@app.route('/checkBookOut', methods=['PUT'])
def checkBookOut():
    data = json.loads(request.data)
    bookId = data['bookId']
    libraryId = data['libraryId']
    global books, libraries
    for b in books:
        if b.id == bookId:
            libraries[libraryId].registered_books.remove(b)
            # Esta linha existe porque nao sei se devemos retirar o livro completamente da db quando lhe damos checkout ou nao
            # books.remove(b)
            return "True"


@app.route('/registerBook', methods=['PUT'])
def registerBook():
    data = json.loads(request.data)
    bookId = data['bookId']
    bookTitle = data['bookTitle']
    bookDescription = data['bookDescription']
    bookCover = data['bookCover']

    print(bookId)
    print(bookTitle)
    print(bookDescription)
    # global books, libraries
    # for b in books:
    #     if b.id == bookId:
    #         libraries[libraryId].addBook(b)
    #         return "True"
    return jsonify(True)


@sockets.route('/ws')
def ws(ws):
    global websocket_connections, libraries
    websocket_connections.append(ws)

    while True:
        ws.send(json.dumps([library.getMarkerInfo() for library in libraries]))
        ws.receive()


monkey.patch_all()
WSGIServer(('0.0.0.0', 5000), app).serve_forever()
