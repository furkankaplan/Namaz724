package net.furkankaplan.namaz724.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import net.furkankaplan.namaz724.Defaults;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;

public class Data {

    MainActivity activity;
    Context context;

    public Data(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;

        if (Defaults.getTimeList() != null) {

            if (context != null) {

                TextView cityTextView = ((Activity)context).findViewById(R.id.city);
                cityTextView.setText(Defaults.getAdminArea());

                TextView subAdminAreaTextView = ((Activity)context).findViewById(R.id.subAdminArea);
                subAdminAreaTextView.setText(Defaults.getSubAdminArea());

            }

            new FetchData(context, activity);


        } else {

            new GetData(context, activity);

        }
    }
}
