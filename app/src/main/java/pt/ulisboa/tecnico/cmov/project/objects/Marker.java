package pt.ulisboa.tecnico.cmov.project.objects;

public class Marker {

    private final int id;
    private final String name;
    private final double lat;
    private final double lng;
    private final boolean fav;

    public Marker(int id, String name, double lat, double lng, boolean fav) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
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
}
