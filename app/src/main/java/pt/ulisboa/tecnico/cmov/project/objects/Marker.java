package pt.ulisboa.tecnico.cmov.project.objects;

import java.util.Locale;

public class Marker {

    private final int id;
    private final String name;
    private final double lat;
    private final double lng;
    private final boolean fav;
    private final double distance;

    public Marker(int id, String name, double lat, double lng, boolean fav) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.distance = -1;
    }

    public Marker(int id, String name, double lat, double lng, boolean fav, double distance) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.distance = distance;
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
                '}';
    }
}
