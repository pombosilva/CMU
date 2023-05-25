package pt.ulisboa.tecnico.cmov.project.objects;

public class Marker {

    private String name;
    private double lat;
    private double lng;
    private boolean fav;

    public Marker(String name, double lat, double lng, boolean fav) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
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
}
