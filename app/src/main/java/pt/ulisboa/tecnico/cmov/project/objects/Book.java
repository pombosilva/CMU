package pt.ulisboa.tecnico.cmov.project.objects;

import android.util.Log;

import com.google.gson.Gson;

import pt.ulisboa.tecnico.cmov.project.R;

public class Book {
    private final long id;
    private String title;
    private String description;
    private String cover;
    private final boolean fav;


    public static final int unloadedBookCover = R.drawable.unloaded_book;

    public Book(long id, String title, String description, String cover, boolean fav) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.fav = fav;
    }

    public long getId() {
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

    public boolean isFav() {
        return fav;
    }


    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }

    public String toJson()
    {
        Gson gson = new Gson();
        Log.d("RegisterBook", "Livro Jsonificado = " + gson.toJson(this));
        return gson.toJson(this);

    }
}
