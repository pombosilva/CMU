package pt.ulisboa.tecnico.cmov.project.objects;

public class Book {
    private final int id;
    private String title;
    private String description;
    private int cover;

    private final int barcode;

    public Book(int id, String title, String description, int cover, int barcode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.barcode = barcode;
    }



    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }

    public int getBarcode() {
        return barcode;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title +
                ", barcode=" + barcode +
                '}';
    }
}
