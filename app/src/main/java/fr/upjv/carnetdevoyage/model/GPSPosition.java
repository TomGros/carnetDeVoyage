package fr.upjv.carnetdevoyage.model;

public class GPSPosition {
    public double latitude;
    public double longitude;
    public long timestamp;

    public GPSPosition() {}

    public GPSPosition(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}

