import book as BK


def getEncodedImage(image_file):
    with open(image_file, 'r') as file:
        return file.read()


class Library:
    def __init__(self, id, name, lat, lng, fav, cover):
        self.id = id
        self.name = name
        self.lat = lat
        self.lng = lng
        self.fav = fav
        self.cover = cover
        self.registered_books = []
        self.distance = -1



    def getRegisteredBooksAsJson(self):
        booksAsJson = []

        for rb in self.registered_books:
            booksAsJson.append(rb.getBookInfo())

        return booksAsJson


    def getMarkerInfo(self):
        return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lng': self.lng, 'fav': self.fav, 'cover': getEncodedImage(self.cover)}

    def getMarkerInfoWithDistance(self):
        return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lng': self.lng, 'fav': self.fav, 'distance': self.distance, 'cover': getEncodedImage(self.cover)}
    

    def toJson(self):
        return {"id": self.id, "name": self.name, "lat": self.lat, "lng": self.lng, "fav": self.fav, "cover":getEncodedImage(self.cover), "registeredBooks" : self.getRegisteredBooksAsJson(), "distance": self.distance}

    def getLibraryImage(self):
        return getEncodedImage(self.cover)

    def isBookPresent(self, barcode):
        for book in self.registered_books:
            if book.id == barcode:
                return True
        return False


    def getLibraryBooks(self):
        result = []
        for book in self.registered_books:
            result.append(book.getBookInfo())
        return result

    def addBook(self, book):
        if book not in self.registered_books: 
            print("Added new book")
            self.registered_books.append(book)
            return
        print("Book already exists in this library")

    def setDistance(self, dst):
        self.distance = dst

    def __str__(self):
        return f"Library(id={self.id}, name={self.name}, " \
               f"latitude={self.lat}, longitude={self.lng}, favorite={self.fav}, file={self.cover})"

    def getLibInfoToStore(self):
        return str(self.id)+";"+str(self.name)+";"+str(self.lat)+";"+str(self.lng)+";"+str(self.fav)+";"+str(self.cover)
