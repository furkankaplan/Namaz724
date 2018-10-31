package net.furkankaplan.namaz724.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Time {


    @SerializedName("Gunes")
    @Expose
    public String günes;

    @SerializedName("Ogle")
    @Expose
    public String ogle;

    @SerializedName("Ikindi")
    @Expose
    public String ikindi;

    @SerializedName("Aksam")
    @Expose
    public String aksam;


    @SerializedName("Yatsi")
    @Expose
    public String yatsi;

    @SerializedName("MiladiTarihKisa")
    @Expose
    public String tarih;

    public Time(String günes, String ogle, String ikindi, String aksam, String yatsi, String tarih) {
        this.günes = günes;
        this.ogle = ogle;
        this.ikindi = ikindi;
        this.aksam = aksam;
        this.yatsi = yatsi;
        this.tarih = tarih;
    }

    public String getGünes() {
        return günes;
    }

    public void setGünes(String günes) {
        this.günes = günes;
    }

    public String getOgle() {
        return ogle;
    }

    public void setOgle(String ogle) {
        this.ogle = ogle;
    }

    public String getIkindi() {
        return ikindi;
    }

    public void setIkindi(String ikindi) {
        this.ikindi = ikindi;
    }

    public String getAksam() {
        return aksam;
    }

    public void setAksam(String aksam) {
        this.aksam = aksam;
    }

    public String getYatsi() {
        return yatsi;
    }

    public void setYatsi(String yatsi) {
        this.yatsi = yatsi;
    }

    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }
}
