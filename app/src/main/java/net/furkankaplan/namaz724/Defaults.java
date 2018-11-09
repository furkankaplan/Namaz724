package net.furkankaplan.namaz724;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Defaults extends Activity{

    private static SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor;


    public void setupPreferences(Context context) {

        sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPrefs.edit();

    }

    public SharedPreferences getSharedPrefs() {

        return sharedPrefs;

    }

    public SharedPreferences.Editor getEditor() {

        return editor;

    }

    public void applyEditor() {

        getEditor().apply();

    }

    public  void clearEditor() {
        getEditor().remove("TIME_COUNTRY");
        getEditor().remove("TIME_ADMINAREA");
        getEditor().remove("TIME_SUBADMINAREA");
        getEditor().remove("TIME_LIST");
        getEditor().apply();
    }

    //
    // SharedPreferences içerisinde aşağıdaki verileri tutuyorum
    //

    public String getTimeList() {

        return getSharedPrefs().getString("TIME_LIST", null);

    }

    public void setTimeList(String timeList) {

         getEditor().putString("TIME_LIST", timeList);
         applyEditor();

    }

    public String getAdminArea() {

        return getSharedPrefs().getString("TIME_ADMINAREA", null);

    }

    public void setAdminArea(String adminArea) {

        getEditor().putString("TIME_ADMINAREA", adminArea);
        applyEditor();

    }


    public String getSubAdminArea() {

        return getSharedPrefs().getString("TIME_SUBADMINAREA", null);

    }

    public void setSubAdminArea(String subAdminArea) {

        getEditor().putString("TIME_SUBADMINAREA", subAdminArea);
        applyEditor();
    }

    public String getCountry() {

        return getSharedPrefs().getString("TIME_COUNTRY", null);

    }

    public void setCountry(String country) {

        getEditor().putString("TIME_COUNTRY", country);
        applyEditor();

    }




}
