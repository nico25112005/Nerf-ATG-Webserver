package net.nerfatg.Utils;

public class GPS {

    private final double longitude;
    private final double latitude;

    public GPS(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
