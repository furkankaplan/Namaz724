package net.furkankaplan.namaz724.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;
import net.furkankaplan.namaz724.network.model.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ParsData {

    private static final String TAG = "ParsData";
    private Context context;
    private MainActivity activity;

    // Bu constructer, pars işlemi içi 2 türlü kullanılıyor.
    // İlki SharedPreferences doludur ve veriler FetchData ile çekilip liste bu constructor'a willBeSaved = false olarak aktarılır.
    // Çünkü zaten bir liste vardır ve tekrar kaydetmeye gerek yoktur.
    // İkincisi SharedPreferences boştur ve API'den veri çekilmiştir. Çekilen veri GetData ile çekilip liste bu constructor'a willBeSaved = true olarak aktarılır.
    // Böylece bir sonraki kullanım için SharedPreferences doldurulur ve internet bağlantısına gerek duyulmaz.

    private static DateFormat formatterForDateHour = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static SimpleDateFormat formatterForDate = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat formatterForHour = new SimpleDateFormat("HH:mm:ss");

    private final static String SECOND_STRING = ":00";

    public ParsData(Context context, MainActivity activity, List<Time> fetchedlist, boolean willBeSaved ) throws ParseException {

        this.context = context;
        this.activity = activity;

        final int sizeOfTimes = fetchedlist.size();

        // SharedPreferences doldurulmak üzere nesne tanımlanıyor. Eğer willBeSaved false ise hiç yaratılmayacak.
        StringBuilder stringToSave = null;

        if ( willBeSaved ) {
            stringToSave = new StringBuilder("[");
        }

        for (int z = 0; z < sizeOfTimes; z++) {


            Time obj3 = fetchedlist.get(z);

            final String gunes = obj3.getGünes();
            final String ogle = obj3.getOgle();
            final String ikindi = obj3.getIkindi();
            final String aksam = obj3.getAksam();
            final String yatsi = obj3.getYatsi();
            final String tarih = obj3.getTarih();

            if ( willBeSaved ) {

                String times = "{\n" +
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
            String todayString = getTodayDateString();
            if (context != null) {
                TextView todayTextView = ((Activity) context).findViewById(R.id.today);
                todayTextView.setText(todayString);
            }

            if (tarih.equals(todayString)) {

                Date gunesDate = formatterForDateHour.parse(todayString + " " + gunes + SECOND_STRING);
                Date ogleDate = formatterForDateHour.parse(todayString + " " + ogle + SECOND_STRING);
                Date ikindiDate = formatterForDateHour.parse(todayString + " " + ikindi + SECOND_STRING);
                Date aksamDate = formatterForDateHour.parse(todayString + " " + aksam + SECOND_STRING);
                Date yatsiDate = formatterForDateHour.parse(todayString + " " + yatsi + SECOND_STRING);
                Date nowDate = formatterForDateHour.parse(getTodayDateStringWithSecond());

                if ( nowDate.before(gunesDate) ) {


                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - gunesDate.getTime()));

                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");
                    }


                    runTimer(gunesDate, "Güneş");


                } else if ( nowDate.before(ogleDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - ogleDate.getTime()));

                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Öğle");
                    }
                    runTimer(ogleDate, "Öğle");


                } else if ( nowDate.before(ikindiDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - ikindiDate.getTime()));
                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "İkindi");
                    }
                    runTimer(ikindiDate, "İkindi");


                } else if ( nowDate.before(aksamDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - aksamDate.getTime()));
                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Akşam");
                    }
                    runTimer(aksamDate, "Akşam");

                } else if ( nowDate.before(yatsiDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - yatsiDate.getTime()));
                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Yatsı");
                    }

                    runTimer(yatsiDate, "Yatsı");

                }  else {

                    Calendar calendarForTomorrow = Calendar.getInstance();
                    calendarForTomorrow.add(Calendar.DAY_OF_YEAR, 1);
                    Date  tomorrowDate = calendarForTomorrow.getTime();
                    String tomorrowString = formatterForDate.format(tomorrowDate);


                    Date tomorrowGunesDate = formatterForDateHour.parse(tomorrowString + " " + fetchedlist.get(z+1).getGünes() + SECOND_STRING);

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - tomorrowGunesDate.getTime()));
                    if (context != null) {
                        this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");
                    }
                    runTimer(tomorrowGunesDate, "Güneş");

                }

            }

        }

        if ( willBeSaved ) {

            Defaults.setTimeList(stringToSave.toString());

            Log.e(TAG, stringToSave.toString());

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

        int delay = 0;
        int period = 1000;
        final Timer time = new Timer();

        time.scheduleAtFixedRate(new TimerTask() {

            public void run() {

                Date nowDate = getTodayDate();

                int diffInMillies =(int)(Math.abs(nowDate.getTime() - vakitDate.getTime()));

                if ( diffInMillies == 0 ) {

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new FetchData(context, activity);
                        }
                    });
                    time.cancel();
                    time.purge();
                }
                if (context != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            promptTimeAndRemaining(getTimeRemainingString(diffInMillies), vakitName);
                        }
                    });
                } else {
                    Log.e(TAG, diffInMillies + " to " + vakitName);
                }

            }
        }, delay, period);

    }


}
