package net.furkankaplan.namaz724.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class City {

    @SerializedName("SehirID")
    @Expose
    public String cityID;

    @SerializedName("SehirAdiEn")
    @Expose
    public String cityEN;

    @SerializedName("SehirAdi")
    @Expose
    public String cityName;

    public City(String cityID, String cityEN, String cityName) {
        this.cityID = cityID;
        this.cityEN = cityEN;
        this.cityName = cityName;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getCityEN() {
        return cityEN;
    }

    public void setCityEN(String cityEN) {
        this.cityEN = cityEN;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
