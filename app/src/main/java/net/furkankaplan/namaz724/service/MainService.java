package net.furkankaplan.namaz724.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;
import net.furkankaplan.namaz724.data.Data;
import net.furkankaplan.namaz724.network.model.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("ParsData", "SERVICES_START: onStartCommand");
        new Data(null, this);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("ParsData", "SERVICES_DESTROYED: onDestroy");

        /*
        Intent myIntent = new Intent(getApplicationContext(), MainService.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.SECOND, 10);

        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.e("ParsData", "SERVICES_START: onDestroy");
         */

    }




    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.e("ParsData", "SERVICES_TASK_REMOVED: onTaskRemoved");


     /*
        Intent myIntent = new Intent(getApplicationContext(), MainService.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.SECOND, 10);

        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.e("ParsData", "SERVICES_START: onTaskRemoved");
      */

    }


}
