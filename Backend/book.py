def getEncodedImage(image_file):
  with open(image_file, 'r') as file:
    return file.read()

class Book :
    def __init__(self, id, title, description, cover_file):
        self.id = id
        self.title = title
        self.description = description
        self.cover = cover_file

    def getBookInfo(self):
        return {'id': self.id, 'title':self.title, 'description':self.description, 'cover':getEncodedImage(self.cover)}