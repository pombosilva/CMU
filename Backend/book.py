def getEncodedImage(image_file):
    print()
    print(image_file)
    print()


    with open(image_file, 'r') as file:
        return file.read()


class Book:
    def __init__(self, id, title, description, cover, fav):
        self.id = id # id e o barcode
        self.title = title
        self.description = description
        self.cover = cover
        self.fav = fav


    def getBookInfo(self):
        return {'id': self.id, 'title': self.title, 'description': self.description, 'cover': getEncodedImage(self.cover), 'fav': self.fav}

    def getBookWithoutImage(self):
        return {'id': self.id, 'title': self.title, 'description': self.description, 'cover': None , 'fav': self.fav}

    def getBookImage(self):
        return getEncodedImage(self.cover)


    def __str__(self):
        return f"Book(id={self.id}, title={self.title}, description={self.description}, cover={self.cover}, fav={self.fav})"



    def getBookInfoToStore(self):
        return str(self.id)+";"+str(self.title)+";"+str(self.description)+";"+str(self.cover)+";"+str(self.fav)
    