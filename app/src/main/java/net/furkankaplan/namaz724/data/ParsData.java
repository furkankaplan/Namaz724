package net.furkankaplan.namaz724.data;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;
import net.furkankaplan.namaz724.network.model.Time;
import net.furkankaplan.namaz724.service.MainService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class ParsData {

    private static final String TAG = "ParsData";
    private Context context;
    private MainActivity activity;
    private Handler handler = new Handler(Looper.getMainLooper());

    private long diffInMillies = 0;
    private long diffInMilliesTemp = 0;

    private Date nowDate = null;
    private Date gunesDate = null;
    private Date ogleDate = null;
    private Date ikindiDate = null;
    private Date aksamDate = null;
    private Date yatsiDate  = null;

    private String gunes = null;
    private String ogle = null;
    private String ikindi = null;
    private String aksam = null;
    private String yatsi = null;
    private String tarih = null;

    String todayString = null;

    Time obj3;

    String times;

    // SharedPreferences doldurulmak üzere nesne tanımlanıyor. Eğer willBeSaved false ise hiç yaratılmayacak.
    StringBuilder stringToSave = null;

    // Bu constructer, pars işlemi içi 2 türlü kullanılıyor.
    // İlki SharedPreferences doludur ve veriler FetchData ile çekilip liste bu constructor'a willBeSaved = false olarak aktarılır.
    // Çünkü zaten bir liste vardır ve tekrar kaydetmeye gerek yoktur.
    // İkincisi SharedPreferences boştur ve API'den veri çekilmiştir. Çekilen veri GetData ile çekilip liste bu constructor'a willBeSaved = true olarak aktarılır.
    // Böylece bir sonraki kullanım için SharedPreferences doldurulur ve internet bağlantısına gerek duyulmaz.

    private static DateFormat formatterForDateHour = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static SimpleDateFormat formatterForDate = new SimpleDateFormat("dd.MM.yyyy");

    private final static String SECOND_STRING = ":00";

    ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    public ParsData(Context context, MainActivity activity, List<Time> fetchedlist, boolean willBeSaved, ProgressDialog progressDialog) throws ParseException {

        this.context = context;
        this.activity = activity;

        final int sizeOfTimes = fetchedlist.size();



        if ( willBeSaved ) {
            stringToSave = new StringBuilder("[");
        }

        for (int z = 0; z < sizeOfTimes; z++) {

            obj3 = fetchedlist.get(z);

            gunes = obj3.getGünes();
            ogle = obj3.getOgle();
            ikindi = obj3.getIkindi();
            aksam = obj3.getAksam();
            yatsi = obj3.getYatsi();
            tarih = obj3.getTarih();

            if ( willBeSaved ) {

                 times = "{\n" +
                        "tarih:" + "\"" + tarih + "\"" +  ",\n" +
                        "gunes:" + "\"" + gunes + "\"" +  ",\n" +
                        "ogle:" + "\"" + ogle + "\"" +  ",\n" +
                        "ikindi:" + "\"" + ikindi + "\"" +  ",\n" +
                        "aksam:" + "\"" + aksam + "\"" +  ",\n" +
                        "yatsi:" + "\"" + yatsi + "\"" +
                        "}";

                stringToSave.append(times);

                // JSON formatında objeler arasında virgül konulur ve bundan dolayı son objeden sonra virgül koyulmaz.
                // Bu yüzden son obje mi değil mi diye kontrol ediyorum.
                if ( z != sizeOfTimes -1 ) {
                    stringToSave.append(",");
                } else {
                    stringToSave.append("]");
                }

            }


            // For döngüsünde bugünün vakitlerine ulaşıp Parse etmek için bugünün tarihini buluyoruz.
            todayString = getTodayDateString();
            if (context != null && activity != null) {
                TextView todayTextView = ((Activity) context).findViewById(R.id.today);
                todayTextView.setText(todayString);
            }

            if (tarih.equals(todayString)) {

                gunesDate = formatterForDateHour.parse(todayString + " " + gunes + SECOND_STRING);
                ogleDate = formatterForDateHour.parse(todayString + " " + ogle + SECOND_STRING);
                ikindiDate = formatterForDateHour.parse(todayString + " " + ikindi + SECOND_STRING);
                aksamDate = formatterForDateHour.parse(todayString + " " + aksam + SECOND_STRING);
                yatsiDate = formatterForDateHour.parse(todayString + " " + yatsi + SECOND_STRING);
                nowDate = formatterForDateHour.parse(getTodayDateStringWithSecond());

                if ( nowDate.before(gunesDate) ) {


                    diffInMillies = (gunesDate.getTime() - nowDate.getTime());

                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");
                    }


                    runTimer(gunesDate, "Güneş");


                } else if ( nowDate.before(ogleDate)) {

                    diffInMillies =(ogleDate.getTime() - nowDate.getTime());

                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Öğle");
                    }
                    runTimer(ogleDate, "Öğle");


                } else if ( nowDate.before(ikindiDate)) {

                    diffInMillies = (ikindiDate.getTime() - nowDate.getTime());

                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "İkindi");
                    }
                    runTimer(ikindiDate, "İkindi");

                } else if ( nowDate.before(aksamDate)) {

                    diffInMillies =(aksamDate.getTime() - nowDate.getTime());
                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Akşam");
                    }
                    runTimer(aksamDate, "Akşam");

                } else if ( nowDate.before(yatsiDate)) {

                    diffInMillies =(yatsiDate.getTime() - nowDate.getTime());
                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Yatsı");
                    }

                    runTimer(yatsiDate, "Yatsı");

                }  else {

                    Calendar calendarForTomorrow = Calendar.getInstance();
                    calendarForTomorrow.add(Calendar.DAY_OF_YEAR, 1);
                    Date  tomorrowDate = calendarForTomorrow.getTime();
                    String tomorrowString = formatterForDate.format(tomorrowDate);


                    Date tomorrowGunesDate = formatterForDateHour.parse(tomorrowString + " " + fetchedlist.get(z+1).getGünes() + SECOND_STRING);

                    diffInMillies = (tomorrowGunesDate.getTime() - nowDate.getTime());
                    if (context != null && activity != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");
                    }
                    runTimer(tomorrowGunesDate, "Güneş");

                }

            } 

        }

        if ( willBeSaved ) {

            Defaults defaults = new Defaults();
            defaults.setupPreferences( context );
            defaults.setTimeList(stringToSave.toString());
            progressDialog.dismiss();
            if (context != null) {
                    //Toast.makeText(context, "SERVICE STARTED", Toast.LENGTH_SHORT).show();
                    context.startService(new Intent(context, MainService.class));

                    Log.e("SERVICE", "STARTED" );

            }

        }

    }


    private static Date getTodayDate() {
        return Calendar.getInstance().getTime();
    }

    private static String getTodayDateString() {
        return formatterForDate.format(getTodayDate());
    }

    private static String getTodayDateStringWithSecond() {
        return formatterForDateHour.format(getTodayDate());
    }

    private String getTimeRemainingString(long vdiffInMillies) {

        Long hour = TimeUnit.HOURS.convert(vdiffInMillies, TimeUnit.MILLISECONDS);
        vdiffInMillies= vdiffInMillies % (1000 * 60 * 60);
        Long minutes = TimeUnit.MINUTES.convert(vdiffInMillies, TimeUnit.MILLISECONDS);
        vdiffInMillies= vdiffInMillies % (1000 * 60);
        Long seconds = TimeUnit.SECONDS.convert(vdiffInMillies, TimeUnit.MILLISECONDS);

        StringBuilder buffer = new StringBuilder();
        if (hour > 0) {
            buffer.append(hour).append(" s  ");
        }
        if (minutes > 0) {
            buffer.append(minutes).append(" dk  ");
        }
        buffer.append(seconds).append(" sn  ");


        return buffer.toString();

    }


    private void promptTimeAndRemaining(String diff, String time) {

        TextView toWhichTimeTextView = ((Activity)context).findViewById(R.id.toWhichTime);
        toWhichTimeTextView.setText(time + " Vaktine");

        TextView remainTimeTextView = ((Activity)context).findViewById(R.id.remainTime);
        remainTimeTextView.setText(diff);

    }

    private void runTimer( Date vakitDate , String vakitName) {


        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                nowDate = null;
                try {
                    nowDate = formatterForDateHour.parse(getTodayDateStringWithSecond());
                } catch (ParseException e) {
                    Log.e("ERR", e.toString());
                }

                if (nowDate == null) {

                    if ( activity != null) { // Uygulama ekranı açık vaziyette.

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new FetchData(context, activity);

                            }
                        });

                        scheduledExecutor.shutdown();

                    } else { // Servis çalışıyor.


                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                new FetchData(context, null);

                            }
                        });

                        Log.e(TAG, "Date null geldi. işlemler durdurulup yeniden başlatılacak.");
                        scheduledExecutor.shutdown();

                    }

                } else {

                    diffInMillies = (vakitDate.getTime() - nowDate.getTime());

                    if (diffInMillies < 0 && activity != null) { // Uygulama ekranı açık vaziyette.


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new FetchData(context, activity);

                            }
                        });

                        scheduledExecutor.shutdown();
                        return;

                    } else if (diffInMillies < 0) { // Servis çalışıyor.


                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                new FetchData(context, null);

                            }
                        });

                        Log.e(TAG, "Vakit bitti, yeni zamana geçilecek.");

                        scheduledExecutor.shutdown();

                        // TODO push notification

                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

                        b.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setTicker(vakitName + " vakti girdi kardeşim.")
                                .setContentTitle("Namaz 7/24")
                                .setContentText(vakitName + " vakti girdi kardeşim.")
                                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                .setContentIntent(contentIntent)
                                .setContentInfo("Info");

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());

                        return;

                    }

                    if (activity != null && diffInMillies > 0) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                promptTimeAndRemaining(getTimeRemainingString(diffInMillies), vakitName);

                            }
                        });

                    } else {

                        // Log.e("TEST", nowDate.getTime() + " to " + vakitDate.getTime());

                        Log.e(TAG, diffInMillies + " to " + vakitName);

                    }

                }

            }
        }, 0, 1, TimeUnit.SECONDS);



    }


}
