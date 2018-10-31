package net.furkankaplan.namaz724.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubAdminArea {

    @SerializedName("IlceID")
    @Expose
    public String subAdminAreaID;

    @SerializedName("IlceAdiEn")
    @Expose
    public String subAdminAreaEN;

    @SerializedName("IlceAdi")
    @Expose
    public String subAdminAreaName;

    public SubAdminArea(String subAdminAreaID, String subAdminAreaEN, String subAdminAreaName) {
        this.subAdminAreaID = subAdminAreaID;
        this.subAdminAreaEN = subAdminAreaEN;
        this.subAdminAreaName = subAdminAreaName;
    }

    public String getSubAdminAreaID() {
        return subAdminAreaID;
    }

    public void setSubAdminAreaID(String subAdminAreaID) {
        this.subAdminAreaID = subAdminAreaID;
    }

    public String getSubAdminAreaEN() {
        return subAdminAreaEN;
    }

    public void setSubAdminAreaEN(String subAdminAreaEN) {
        this.subAdminAreaEN = subAdminAreaEN;
    }

    public String getSubAdminAreaName() {
        return subAdminAreaName;
    }

    public void setSubAdminAreaName(String subAdminAreaName) {
        this.subAdminAreaName = subAdminAreaName;
    }
}
