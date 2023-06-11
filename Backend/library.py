import book as BK


def getEncodedImage(image_file):
    with open(image_file, 'r') as file:
        return file.read()


class Library:
    def __init__(self, id, name, lat, lng, fav, image_file):
        self.id = id
        self.name = name
        self.lat = lat
        self.lng = lng
        self.fav = fav
        self.image_file = image_file
        self.registered_books = []
        self.distance = -1



    def getRegisteredBooksAsJson(self):
        booksAsJson = []

        for rb in self.registered_books:
            booksAsJson.append(rb.getBookWithoutImage())

        return booksAsJson


    def getMarkerInfo(self):
        return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lng': self.lng, 'fav': self.fav}

    def getMarkerInfoWithDistance(self):
        return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lng': self.lng, 'fav': self.fav, 'distance': self.distance}
    

    def toJson(self):
        return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lng': self.lng, 'fav': self.fav, 'encodedFile':self.image_file, 'registeredBooks' : self.getRegisteredBooksAsJson(),'distance': self.distance}

    def getLibraryImage(self):
        return getEncodedImage(self.image_file)

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
               f"latitude={self.lat}, longitude={self.lng}, favorite={self.fav}, file={self.image_file})"
