package net.furkankaplan.namaz724.service;

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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;
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

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    List<Time> timeList = new ArrayList<>();


    DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    Handler handler = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPrefs.edit();

        handler = new Handler(Looper.getMainLooper());


        if (sharedPrefs.getString("TIME_LIST", null) != null) {

            Log.e("SERVICE", "TIME_LIST boş değil");

            try {

                this.promptData(null);

            } catch (ParseException | JSONException e) {
                Log.e("DEBUG", e.toString());
            }

        } else {

            Log.e("SERVICE", "TIME_LIST boş");
            // this.checkPromptInternetConnection(null);

        }


    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    private void promptData(List<Time> subAdminAreaResponse) throws ParseException, JSONException {


        if (sharedPrefs.getString("TIME_LIST", null) != null && subAdminAreaResponse == null) {

            String sharedPrefTimeList = sharedPrefs.getString("TIME_LIST", null);

            JSONArray jsonArray = new JSONArray(sharedPrefTimeList);

            int length = jsonArray.length();
            for ( int i = 0; i < length; i++ ) {

                JSONObject jsonObj = jsonArray.getJSONObject(i);
                timeList.add(new Time(
                        jsonObj.getString("gunes"),
                        jsonObj.getString("ogle"),
                        jsonObj.getString("ikindi"),
                        jsonObj.getString("aksam"),
                        jsonObj.getString("yatsi"),
                        jsonObj.getString("tarih")
                ));

            }

            Log.e("SERVICE", "SharedPreferences dolu, liste pars edildi.");


            this.promptDataCont(timeList, false);


            // Then, get the informations by using below codes.

        } else {

            // shared prefences is null, so must get the data from api.
            // takes parameter as a last presponse of the above request chain.

            if ( subAdminAreaResponse != null ) {

                Log.e("DEBUG", "İlk defa veri çekilecek, işlem başlıyor.");

                this.promptDataCont(subAdminAreaResponse, true);

            } else {

                Log.e("DEBUG", "İlk defa veri çekilecek fakat liste boş");

            }

        }


    }

    private void promptDataCont( List<Time> subAdminAreaResponse, boolean willBeSaved) throws ParseException {

        final int sizeOfTimes = subAdminAreaResponse.size();

        StringBuilder stringToSave = null;

        if ( willBeSaved ) {
            stringToSave = new StringBuilder("[");
        }

        for (int z = 0; z < sizeOfTimes; z++) {


            Time obj3 = subAdminAreaResponse.get(z);

            final String gunes = obj3.getGünes();
            final String ogle = obj3.getOgle();
            final String ikindi = obj3.getIkindi();
            final String aksam = obj3.getAksam();
            final String yatsi = obj3.getYatsi();
            final String tarih = obj3.getTarih();

            if ( willBeSaved ) {

                timeList.add(new Time(

                        gunes,

                        ogle,

                        ikindi,

                        aksam,

                        yatsi,

                        tarih
                ));

                String times = "{\n" +
                        "tarih:" + "\"" + tarih + "\"" +  ",\n" +
                        "gunes:" + "\"" + gunes + "\"" +  ",\n" +
                        "ogle:" + "\"" + ogle + "\"" +  ",\n" +
                        "ikindi:" + "\"" + ikindi + "\"" +  ",\n" +
                        "aksam:" + "\"" + aksam + "\"" +  ",\n" +
                        "yatsi:" + "\"" + yatsi + "\"" +
                        "}";

                stringToSave.append(times);
                if ( z != sizeOfTimes -1 ) {
                    stringToSave.append(",");
                } else {
                    stringToSave.append("]");
                }

            }

            SimpleDateFormat formatterForDate = new SimpleDateFormat("dd.MM.yyyy");
            Calendar calendar = Calendar.getInstance();
            Date todayDate = calendar.getTime();
            String todayString = formatterForDate.format(todayDate);

            // Date todayDate = Calendar.getInstance().getTime();
            // SimpleDateFormat formatterForDate = new SimpleDateFormat("dd.MM.yyyy");

            // final String todayString = formatterForDate.format(todayDate);

            if (tarih.equals(todayString)) {
                SimpleDateFormat formatterForHour = new SimpleDateFormat("HH:mm:ss");

                String hourString = formatterForHour.format(todayDate);
                Date gunesDate = sdf.parse(todayString +" "+ gunes+":00");
                Date ogleDate = sdf.parse(todayString +" "+ ogle+":00");
                Date ikindiDate = sdf.parse(todayString +" "+ ikindi+":00");
                Date aksamDate = sdf.parse(todayString +" "+ aksam+":00");
                Date yatsiDate = sdf.parse(todayString +" "+ yatsi+":00");
                Date nowDate = sdf.parse(todayString +" "+ hourString+":00");

                if ( nowDate.before(gunesDate) ) {

                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - gunesDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60 )  {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainService.this, "güneş vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.e("SERVICE_GUNES", "güneş vaktine 30 dk kaldı.");

                            }


                            if ( diffInMillies2 == 0 ) {

                                Log.e("SERVICE_GUNES", diffInMillies2 + "");
                                onCreate();
                                time.cancel();
                                time.purge();

                            } else {

                                Log.e("SERVICE_GUNES", diffInMillies2 + "");

                            }

                        }
                    }, delay, period);


                } else if ( nowDate.before(ogleDate)) {

                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - ogleDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60 )  {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainService.this, "öğle vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.e("SERVICE_OGLE", "öğle vaktine 30 dk kaldı.");

                            }

                            if ( diffInMillies2 == 0 ) {
                                Log.e("SERVICE_OGLE", diffInMillies2 + "");
                                onCreate();
                                time.cancel();
                                time.purge();
                            } else {

                                Log.e("SERVICE_OGLE", diffInMillies2 + "");

                            }


                        }
                    }, delay, period);


                } else if ( nowDate.before(ikindiDate)) {


                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - ikindiDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60 )  {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainService.this, "ikindi vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.e("SERVICE_IKINDI", "ikindi vaktine 30 dk kaldı.");

                            }


                            if ( diffInMillies2 == 0 ) {
                                Log.e("SERVICE_IKINDI", diffInMillies2 + "");
                                onCreate();
                                time.cancel();
                                time.purge();
                            }else {

                                Log.e("SERVICE_IKINDI", diffInMillies2 + "");

                            }

                        }
                    }, delay, period);


                } else if ( nowDate.before(aksamDate)) {

                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - aksamDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60 )  {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainService.this, "akşam vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.e("SERVICE_AKSAM", "akşam vaktine 30 dk kaldı.");

                            }

                            if ( diffInMillies2 == 0 ) {
                                Log.e("SERVICE_AKSAM", diffInMillies2 + "");
                                onCreate();
                                time.cancel();
                                time.purge();
                            } else {
                                Log.e("SERVICE_AKSAM", diffInMillies2 + "");

                            }

                        }
                    }, delay, period);


                } else if ( nowDate.before(yatsiDate)) {


                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - yatsiDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60) {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainService.this, "Yatsi vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Log.e("SERVICE_YATSI", "Yatsi vaktine 30 dk kaldı.");

                            }

                            if ( diffInMillies2 == 0 ) {
                                Log.e("SERVICE_YATSI", diffInMillies2 + "");
                                onCreate();

                                time.cancel();
                                time.purge();
                            } else {
                                Log.e("SERVICE_YATSI", diffInMillies2 + "");

                            }


                        }
                    }, delay, period);

                }  else {


                    Calendar calendarForTomorrow = Calendar.getInstance();
                    calendarForTomorrow.add(Calendar.DAY_OF_YEAR, 1);
                    Date  tomorrowDate = calendarForTomorrow.getTime();
                    String tomorrowString = formatterForDate.format(tomorrowDate);
                    Log.e("TARIH", tomorrowString +" "+ gunes+":00");
                    Date tomorrowGunesDate = sdf.parse(tomorrowString +" "+ gunes+":00");


                    int delay = 0;
                    int period = 1000;
                    final Timer time = new Timer();

                    time.scheduleAtFixedRate(new TimerTask() {

                        public void run() {


                            Calendar calendar2 = Calendar.getInstance();
                            Date  todayDate2 = calendar2.getTime();
                            String todayString2 = formatterForDate.format(todayDate2);
                            String hourString2 = formatterForHour.format(todayDate2);
                            Date nowDate2 = null;
                            try {
                                nowDate2 = sdf.parse(todayString2 +" "+ hourString2+":00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int diffInMillies2 =(int)(Math.abs(nowDate2.getTime() - tomorrowGunesDate.getTime()));

                            if ( diffInMillies2 == 1000 * 30 * 60)  {

                                Toast.makeText(MainService.this, "GÜNEŞ vaktine 30 dk kaldı.", Toast.LENGTH_SHORT).show();
                                Log.e("SERVICE_GUNES", "akşam vaktine 30 dk kaldı.");

                            }

                            if ( diffInMillies2 == 0 ) {
                                Log.e("SERVICE_GUNES", diffInMillies2 + "");
                                onCreate();
                                time.cancel();
                                time.purge();
                            } else {
                                Log.e("SERVICE_GUNES", diffInMillies2 + "");

                            }



                        }
                    }, delay, period);

                }

            }

        }

        if ( willBeSaved ) {

            Log.e("TEST", stringToSave.toString());
            editor.putString("TIME_LIST", stringToSave.toString());
            editor.apply();

        }

    }




}
