package net.furkankaplan.namaz724.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.network.model.Time;
import net.furkankaplan.namaz724.service.MainService;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class FetchData {

    private static final String TAG = "FetchData";
    private Context context;

    // @timelist SharedPreferences'te tutulan tüm vakitleri tutuyor.
    List<Time> timeList = new ArrayList<>();

    private static final String GUNES = "gunes";
    private static final String OGLE = "ogle";
    private static final String IKINDI = "ikindi";
    private static final String AKSAM = "aksam";
    private static final String YATSI = "yatsi";
    private static final String TARIH = "tarih";

    public FetchData( Context context, MainActivity activity ) {

        this.context = context;

        try {

            if (context != null) {
                if (!isServiceWorking()) {

                    //Toast.makeText(context, "SERVICE STARTED", Toast.LENGTH_SHORT).show();
                    context.startService(new Intent(context, MainService.class));

                }
            }

            Log.e(TAG, " çalışmaya devam ediyor  ");


            String sharedPrefTimeList = Defaults.getTimeList();

            JSONArray jsonArray = new JSONArray(sharedPrefTimeList);

            int length = jsonArray.length();
            for ( int i = 0; i < length; i++ ) {

                JSONObject jsonObj = jsonArray.getJSONObject(i);

                timeList.add(new Time(
                        jsonObj.getString(GUNES),
                        jsonObj.getString(OGLE),
                        jsonObj.getString(IKINDI),
                        jsonObj.getString(AKSAM),
                        jsonObj.getString(YATSI),
                        jsonObj.getString(TARIH)
                ));
            }

            new ParsData(context, activity, timeList, false);




        } catch (ParseException | JSONException e) {
            Log.e(TAG, e.toString());
        }

    }

    private boolean isServiceWorking() {

        ActivityManager serviceManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : serviceManager.getRunningServices(Integer.MAX_VALUE) ) {

            if ( context.getPackageName().equals(service.service.getPackageName()) ) {

                return true;

            }

        }

        return false;

    }

}
