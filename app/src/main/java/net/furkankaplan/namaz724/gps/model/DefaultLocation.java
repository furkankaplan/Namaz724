package net.furkankaplan.namaz724.gps.model;

import android.location.Location;

public class DefaultLocation {

    private String country;
    private String city;
    private String subAdminArea;
    private Location location;

    public DefaultLocation(String country, String city, String subAdminArea, Location location) {
        this.country = country;
        this.city = city;
        this.subAdminArea = subAdminArea;
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getSubAdminArea() {
        return subAdminArea;
    }

    public Location getLocation() {
        return location;
    }
}
