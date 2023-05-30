def getEncodedImage(image_file):
    with open(image_file, 'r') as file:
        return file.read()


class Book:
    def __init__(self, id, title, description, cover):
        self.id = id # id e o barcode
        self.title = title
        self.description = description
        self.cover = cover

    def getBookInfo(self):
        return {'id': self.id, 'title': self.title, 'description': self.description,
                'cover': getEncodedImage(self.cover)}

    def __str__(self):
        return f"Book(id={self.id}, title={self.title}, description={self.description}, cover={self.cover})"
    