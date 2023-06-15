package pt.ulisboa.tecnico.cmov.project.objects;

import java.util.Locale;
import android.util.Log;
import com.google.gson.Gson;

public class Library {

    private final int id;
    private final String name;
    private final double lat;
    private final double lng;
    private final boolean fav;
    private final double distance;
    private final String image;

    public Library(int id, String name, double lat, double lng, boolean fav, String image) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.distance = -1;
        this.image = image;
    }

    public Library(int id, String name, double lat, double lng, boolean fav, double distance, String image) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.distance = distance;
        this.image = image;
    }

    public Library(int id, String name, double lat, double lng, boolean fav, double distance) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.distance = distance;
        this.image = null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isFav() {
        return fav;
    }

    public double getDistance(){return Double.parseDouble(
            String.format(Locale.UK,"%.2f", distance));}


    @Override
    public String toString() {
        return "Marker{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", fav=" + fav +
                ", distance='" + distance + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String toJson() {
        Gson gson = new Gson();
        Log.d("RegisterLib", "Lib Jsonificado = " + gson.toJson(this));
        return gson.toJson(this);

    }
}
