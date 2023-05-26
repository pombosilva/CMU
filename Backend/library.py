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
        self.registered_books=[]

    def getMarker(self):
      return {'id':self.id, 'name': self.name, 'lat': self.lat, 'lnt': self.lng, 'fav' : self.fav}
    
    def getLibraryInfo(self):
      return {'id': self.id, 'name': self.name, 'lat': self.lat, 'lnt': self.lng, 'fav' : self.fav, 'encodedImage' : getEncodedImage(self.image_file)}
    
    def getLibraryBooks(self):
      result = []  
      for book in self.registered_books:
         result.append(book.getBookInfo())
      return result

    def addBook(self, book):
      self.registered_books.append(book)

    def __str__(self):
        return f"Library(id={self.id}, name={self.name}, " \
               f"latitude={self.lat}, longitude={self.lng}, favorite={self.fav}, file={self.image_file})"