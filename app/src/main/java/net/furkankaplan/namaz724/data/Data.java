package net.furkankaplan.namaz724.data;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;

import java.io.File;
import java.io.IOException;


public class Data extends Activity{

    MainActivity activity;
    Context context;

    public Data(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;

        Defaults defaults = new Defaults();
        defaults.setupPreferences( context );

        if (defaults.getTimeList() != null) {

            if (context != null && activity != null) {

                TextView cityTextView = ((Activity)context).findViewById(R.id.city);
                cityTextView.setText(defaults.getAdminArea());

                TextView subAdminAreaTextView = ((Activity)context).findViewById(R.id.subAdminArea);
                subAdminAreaTextView.setText(defaults.getSubAdminArea());

            } else {

                Log.e("DATA", defaults.getTimeList());

            }

            new FetchData(context, activity);


        } else {

            new GetData(context, activity);

        }

        /*
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/Namaz724LogcatFolder" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
        }
         */

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
}
