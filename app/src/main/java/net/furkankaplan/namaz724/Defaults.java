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

    public static void commitPreferences() {

        editor.apply();

    }

    public static SharedPreferences getSharedPrefs() {

        return sharedPrefs;

    }

    public static SharedPreferences.Editor getEditor() {

        return editor;

    }

    private static void applyEditor() {

        getEditor().apply();

    }

    //
    // SharedPreferences içerisinde aşağıdaki verileri tutuyorum
    //

    public static String getTimeList() {

        return Defaults.getSharedPrefs().getString("TIME_LIST", null);

    }

    public static void setTimeList(String timeList) {

         Defaults.editor.putString("TIME_LIST", timeList);
         Defaults.applyEditor();

    }

    public static String getAdminArea() {

        return Defaults.getSharedPrefs().getString("TIME_ADMINAREA", null);

    }

    public static String getSubAdminArea() {

        return Defaults.getSharedPrefs().getString("TIME_SUBADMINAREA", null);

    }

    public static String getCountry() {

        return Defaults.getSharedPrefs().getString("TIME_COUNTRY", null);

    }




}
