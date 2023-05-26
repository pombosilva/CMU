def getEncodedImage(image_file):
    with open(image_file, 'r') as file:
        return file.read()


class Book:
    def __init__(self, id, title, description, cover_file, barcode):
        self.id = id
        self.title = title
        self.description = description
        self.cover = cover_file
        self.barcode = barcode

    def getBookInfo(self):
        return {'id': self.id, 'title': self.title, 'description': self.description,
                'cover': getEncodedImage(self.cover), 'barcode': self.barcode}

    def __str__(self):
        return f"Book(id={self.id}, title={self.title}, description={self.description}, cover={self.cover}, " \
               f"barcode={self.barcode})"
