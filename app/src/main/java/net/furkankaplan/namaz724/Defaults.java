package net.furkankaplan.namaz724;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Defaults {

    private static SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;

    public static void setupPreferences(Context context) {

        sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPrefs.edit();

    }

    public static SharedPreferences getSharedPrefs() {

        return sharedPrefs;

    }

    public static SharedPreferences.Editor getEditor() {

        return editor;

    }

    public static void applyEditor() {

        Defaults.getEditor().apply();

    }

    public static void clearEditor() {
        Defaults.getEditor().remove("TIME_COUNTRY");
        Defaults.getEditor().remove("TIME_ADMINAREA");
        Defaults.getEditor().remove("TIME_SUBADMINAREA");
        Defaults.getEditor().remove("TIME_LIST");
        Defaults.getEditor().apply();
    }

    //
    // SharedPreferences içerisinde aşağıdaki verileri tutuyorum
    //

    public static String getTimeList() {

        return Defaults.getSharedPrefs().getString("TIME_LIST", null);

    }

    public static void setTimeList(String timeList) {

         Defaults.getEditor().putString("TIME_LIST", timeList);
         Defaults.applyEditor();

    }

    public static String getAdminArea() {

        return Defaults.getSharedPrefs().getString("TIME_ADMINAREA", null);

    }

    public static void setAdminArea(String adminArea) {

        Defaults.getEditor().putString("TIME_ADMINAREA", adminArea);
        Defaults.applyEditor();

    }


    public static String getSubAdminArea() {

        return Defaults.getSharedPrefs().getString("TIME_SUBADMINAREA", null);

    }

    public static void setSubAdminArea(String subAdminArea) {

        Defaults.getEditor().putString("TIME_SUBADMINAREA", subAdminArea);
        Defaults.applyEditor();
    }

    public static String getCountry() {

        return Defaults.getSharedPrefs().getString("TIME_COUNTRY", null);

    }

    public static void setCountry(String country) {

        Defaults.getEditor().putString("TIME_COUNTRY", country);
        Defaults.applyEditor();

    }




}
