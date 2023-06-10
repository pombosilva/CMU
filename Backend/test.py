with open('res/libraries.txt','r') as file:
    for line in file:
        line = line.strip()  # Remove leading/trailing whitespaces and newlines
        data = line.split(";")  # Split the line into components

        id_number = data[0]
        manual_title = data[1]
        author = data[2]
        filename = data[3]
        is_available = data[4]

        # Do something with the variables
        print("ID:", id_number)
        print("Manual Title:", manual_title)
        print("Author:", author)
        print("Filename:", filename)
        print("Availability:", is_available)
        print()