package models;

public class Coordinate {
    double longitude;
    double latitude;

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
}
