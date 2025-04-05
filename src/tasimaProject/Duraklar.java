package tasimaProject;

public class Duraklar {
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lon;
    private boolean sonDurak;
    private Vehicle vehicle;


    public Duraklar(String id, String name, String type, double lat, double lon, boolean sonDurak, Vehicle vehicle) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.sonDurak = sonDurak;
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle(){return vehicle;}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean getSonDurak() {
        return sonDurak;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setSonDurak(boolean sonDurak) {
        this.sonDurak = sonDurak;
    }

}