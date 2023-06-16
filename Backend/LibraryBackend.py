#!/usr/bin/env python3
import threading
from email.mime import image
from mimetypes import init
from flask import Flask, render_template, jsonify, json, request
from flask_sock import Sock
from simple_websocket.ws import ConnectionClosed
from gevent import monkey
monkey.patch_all()
from gevent.pywsgi import WSGIServer
from geopy.distance import geodesic
import library as LB
import book as BK
import time
import ssl

booksFile = 'res/books.txt'
librariesFile = 'res/libraries.txt'

bookImagesFolder = 'res/BookPics/'
libraryImagesFolder = 'res/LibraryPics/'

toLoad = 5

books = []
libraries = []


with open('res/books.txt','r') as file:
    for line in file:
        line = line.strip()
        data = line.split(";")

        book_id = int(data[0])
        book_title = data[1]
        book_description = data[2]
        book_filename = bookImagesFolder + data[3]
        book_favourite = data[4].lower() == "true"

        books.append(BK.Book(book_id, book_title, book_description, book_filename, book_favourite))

with open('res/libraries.txt','r') as file:
    for line in file:
        line = line.strip()
        data = line.split(";")

        library_id = int(data[0])
        library_title = data[1]
        library_lat = float(data[2])
        library_lng = float(data[3])
        library_favourite = data[4].lower() == "true"
        library_filename = libraryImagesFolder + data[5]

        libraries.append(LB.Library(library_id, library_title, library_lat, library_lng, library_favourite, library_filename))


libraries[0].addBook(books[0])
libraries[0].addBook(books[2])
libraries[0].addBook(books[4])
libraries[0].addBook(books[6])
libraries[0].addBook(books[8])
libraries[0].addBook(books[9])
libraries[0].addBook(books[10])
libraries[0].addBook(books[11])
libraries[1].addBook(books[0])
libraries[1].addBook(books[3])

websocket_connections = []


def websocket_broadcast(message):
    for ws in websocket_connections:
        try:
            ws.send(json.dumps(message))
        except ConnectionClosed:
            websocket_connections.remove(ws)


def registerBookPersistently(newBook, encodedImage):
    with open(booksFile, "a") as file:
        file.write(newBook.getBookInfoToStore()+'\n')
        saveBookImageCoverPersistently(newBook.title, encodedImage)


def saveBookImageCoverPersistently(image_file, encodedImage):
    with open(bookImagesFolder + image_file + '.txt', "w") as file:
        file.write(encodedImage)

def saveLibImageCoverPersistently(image_file, encodedImage):
    with open(libraryImagesFolder + image_file + '.txt', "w") as file:
        file.write(encodedImage)

def registerLibPersistently(newLib, encodedImage):
    with open(librariesFile, "a") as file:
        file.write(newLib.getLibInfoToStore()+'\n')
        saveLibImageCoverPersistently(newLib.name, encodedImage)

def saveLibImagePersistently(image_file, encodedImage):
    with open(libraryImagesFolder + image_file + '.txt', "w") as file:
        file.write(encodedImage)

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

def updateFavBook(book_id):
    global books
    for book in books:
        if book.id == book_id:
            book.fav = not book.fav


app = Flask(__name__)
sockets = Sock(app)


@app.route('/', methods=['GET'])
def index():
    return [str(library) for library in libraries]


@app.route('/books', methods=['GET'])
def showAllBooks():
    global books, toLoad
    start_id = int(request.args.get("startId", 0))
    selected_books = books[start_id:start_id+toLoad]

    return jsonify([book.getBookInfo() for book in selected_books])


@app.route('/booksWithoutImage', methods=['GET'])
def booksWithoutImage():
    global books, toLoad
    start_id = int(request.args.get("startId", 0))
    selected_books = books[start_id:start_id+toLoad]
    return jsonify([book.getBookWithoutImage() for book in selected_books])


@app.route('/bookCover/<int:bookId>', methods=['GET'])
def getBookCover(bookId):
    global books
    for b in books:
        if b.id == bookId:
            return jsonify(b.getBookImage())


@app.route('/bookInLibrary', methods=['GET'])
def getLibrariesThatContainBook():
    global libraries
    bookId = int(request.args.get("bookId",-1))
    latitude = float(request.args.get("lat", 0))
    longitude = float(request.args.get("lng", 1))
    coords = (latitude,longitude)

    result = []
    for l in libraries:
        if l.isBookPresent(bookId):
            result.append(l)

    result2 = []
    for l in result:
        distance = geodesic(coords, (l.lat, l.lng)).kilometers
        l.setDistance(distance)
        result2.append(l.getMarkerInfoWithDistance())

    final = sorted(result2, key=lambda x: x["distance"])

    return jsonify(final)


@app.route('/libraryBooks', methods=['GET'])
def showLibraryBooks():
    global libraries, toLoad
    libraryId= int(request.args.get("libraryId", -1))
    start_id = int(request.args.get("startId", 0))
    print(start_id)
    library = next((l for l in libraries if l.id == libraryId), None)

    if library is None:
        return jsonify({"error": "Library not found"})

    selected_books = library.registered_books[start_id : start_id + 5]

    return jsonify([book.getBookInfo() for book in selected_books])


@app.route('/libraryBooksWithoutImage', methods=['GET'])
def showLibraryBooksWithoutImage():
    global libraries, toLoad
    libraryId= int(request.args.get("libraryId", -1))
    start_id = int(request.args.get("startId", 0))

    library = next((l for l in libraries if l.id == libraryId), None)

    if library is None:
        return jsonify({"error": "Library not found"})

    selected_books = library.registered_books[start_id : start_id + 5]

    return jsonify([book.getBookWithoutImage() for book in selected_books])


@app.route('/markers', methods=['GET'])
def get_markers():
    global libraries
    return jsonify([library.getMarkerInfo() for library in libraries])

@app.route('/nextLibId', methods=['GET'])
def nextLibId():
    return jsonify(len(libraries))


@app.route('/favMarker', methods=['PUT'])
def favMarker():
    global libraries
    data = json.loads(request.data)
    updateFavLibrary(data)
    return "True"

@app.route('/favBook', methods=['PUT'])
def favBook():
    global books
    data = json.loads(request.data)
    updateFavBook(data)
    return "True"


@app.route('/libraryExtras', methods=['GET'])
def getLibrayExtras():
    library_id = int(request.args.get("libraryId"))
    lib = get_library(library_id)
    return jsonify(lib.getLibraryBooks())


@app.route('/getBook', methods=['GET'])
def getBook():
    bookBarcode = int(request.args.get("bookId",-1))
    global books
    for b in books:
        if b.id == bookBarcode:
            return jsonify(b.getBookInfo())
    return jsonify(False)



@app.route('/bookExistence', methods=['GET'])
def bookExists():
    bookBarcode = int(request.args.get("bookId",-1))
    print("BookBarcode = " + str(bookBarcode))
    global books
    for b in books:
        if b.id == bookBarcode:
            return jsonify(True)
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


@app.route('/registerBook/<string:libraryId>', methods=['PUT'])
def registerBook(libraryId):

    data = json.loads(request.data)
    bookId = data['id']
    bookTitle = data['title']
    bookDescription = data['description']
    bookCover = data['cover']

    global books, libraries
    # newBook = BK.Book(bookId, bookTitle, bookDescription, createFile(bookCover, "BookPics/"+str(bookId)+".txt"), False)
    newBook = BK.Book(bookId, bookTitle, bookDescription, bookTitle +'.txt', False)

    registerBookPersistently(newBook, bookCover)

    newBook.cover = bookImagesFolder + newBook.cover

    libraries[int(libraryId)].addBook(newBook)
    books.append(newBook)


    return jsonify(True)

@app.route('/registerLib', methods=['PUT'])
def registerLib():

    data = json.loads(request.data)
    libId = data['id']
    libName = data['name']
    libLat = data['lat']
    libLng = data['lng']
    libImage = data['cover']

    global libraries

    print(libId, libName, libLat, libLng, False, libImage)

    newLib = LB.Library(libId, libName, libLat, libLng, False, libName +'.txt')

    registerLibPersistently(newLib, libImage)

    newLib.cover = libraryImagesFolder + newLib.cover

    libraries.append(newLib)

    return jsonify(True)

@app.route('/filteredBooks', methods=['GET'])
def filteredBooks():
    start_id = int(request.args.get("start_id",0))
    filtr = str(request.args.get("filter",""))

    global books

    selected_books = [b for b in books if filtr.lower() in b.title.lower()]

    # for b in selected_books:
    #     print(b.getBookWithoutImage())
    # print(str(selected_books))

    # selected_books = selected_books[start_id:start_id+toLoad]
    # for b in selected_books:
    #     print(b.getBookWithoutImage())
    # print(str(selected_books))

    return jsonify([book.getBookInfo() for book in selected_books])


@app.route('/getContentsWithinRadius', methods=['GET'])
def getContentsWithinRadius():
    global libraries


    latitude = float(request.args.get("lat", -1))
    longitude = float(request.args.get("lng", -1))
    coords = (latitude,longitude)

    print()
    print("lat " + str(latitude))
    print("lng " + str(longitude))
    print()


    if latitude == -1 or longitude == -1:
        return jsonify(False)

    radius = 10
    filteredLibraries = []

    for l in libraries:
        distance = geodesic(coords, (l.lat, l.lng)).kilometers
        if distance < radius :
            print("Vou adicionar uma library")
            l.setDistance(distance)
            filteredLibraries.append(l.toJson())

    
    return jsonify(filteredLibraries)

@app.route('/test', methods=['GET'])
def test():
    global libraries

    libs = []
    libs.append(libraries[1].toJson())
    libs.append(libraries[3].toJson())

    # print(libs)

    # print( jsonify(libraries[1].toJson()))
    return jsonify(libs)

@sockets.route('/ws')
def ws(ws):
    global websocket_connections, libraries
    websocket_connections.append(ws)

    threading.Thread(target=sendAvailableFavBooks, args=(ws,)).start()

    while True:
        ws.receive()

def sendAvailableFavBooks(ws):
    global libraries

    while True:
        global libraries
        result = []
        result2 = []
        for l in libraries:
            if l.fav is True:
                result.append(l.getMarkerInfo())
        for l in libraries:
            for b in l.registered_books:
                if b.fav is True:
                    result2.append(b.title + " is available at " + l.name)

        ws.send(result2)
        time.sleep(300)

monkey.patch_all()
#WSGIServer(('0.0.0.0', 5000), app).serve_forever()

# Configure SSL context
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
ssl_context.load_cert_chain(certfile='/etc/ssl/certs/cert.crt', keyfile='/etc/ssl/private/key.key')

# Create and run the server with SSL
http_server = WSGIServer(('0.0.0.0', 5000), app, ssl_context=ssl_context)
http_server.serve_forever()