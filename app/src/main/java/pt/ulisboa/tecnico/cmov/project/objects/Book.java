package pt.ulisboa.tecnico.cmov.project.objects;

public class Book {
    private final int id;
    private String title;
    private String description;
    private String cover;


    public Book(int id, String title, String description, String cover) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
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

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }


    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + "'"+
                '}';
    }

    public String toJson()
    {
        return  "{\"id\":"+ this.getId() +",\"title\":"+this.getTitle()+",\"description\":"+this.getDescription()+",\"cover\":"+this.getCover()+"}";
    }
}
