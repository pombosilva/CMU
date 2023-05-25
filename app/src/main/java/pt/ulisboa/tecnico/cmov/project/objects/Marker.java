package pt.ulisboa.tecnico.cmov.project.objects;

public class Marker {

    private int id;
    private String name;
    private double lat;
    private double lng;
    private boolean fav;
    private String encodedImage;

    public Marker(int id, String name, double lat, double lng, boolean fav, String encodedImage ) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fav = fav;
        this.encodedImage = encodedImage;
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

    public String getEncodedImage() {
        return encodedImage;
    }
}
