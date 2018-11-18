package net.furkankaplan.namaz724.data;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;


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
    }
}
