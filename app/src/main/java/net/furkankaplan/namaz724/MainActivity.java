package net.furkankaplan.namaz724;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import net.furkankaplan.namaz724.data.Data;
import net.furkankaplan.namaz724.data.FetchData;
import net.furkankaplan.namaz724.data.GetData;
import net.furkankaplan.namaz724.network.RetrofitRxJava;
import net.furkankaplan.namaz724.network.model.City;
import net.furkankaplan.namaz724.network.model.SubAdminArea;
import net.furkankaplan.namaz724.network.model.Time;
import net.furkankaplan.namaz724.gps.LocationActivity;
import net.furkankaplan.namaz724.gps.model.DefaultLocation;
import net.furkankaplan.namaz724.service.MainService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends Activity {

    TextView cityTextView, subAdminAreaTextView, toWhichTimeTextView, remainTimeTextView, todayTextView;
    LinearLayout timeContainerLayout;

    // Loglarda bu etiket kullanılacak.
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // İleride kullanılmak üzere sharedPreferences ve editor özelliklerini kurdum.
        // Gerekli değişkenleri de bu class üzerinden static olarak çekeceğim.

//        Defaults.clearEditor();

        this.setupView();
        this.startToFetch();

    }

    private void setupView() {

        cityTextView = findViewById(R.id.city);
        subAdminAreaTextView = findViewById(R.id.subAdminArea);
        toWhichTimeTextView = findViewById(R.id.toWhichTime);
        remainTimeTextView = findViewById(R.id.remainTime);
        timeContainerLayout = findViewById(R.id.timeContainer);
        todayTextView = findViewById(R.id.today);

    }

    private void  startToFetch() {

        // Tekrar tekrar internet işlemi yapmamak adına, aylık namaz vakti verileri SharedPreferences'te tutulur.
        // Veri string olarak json formatında saklanır ki bu şekilde parse edilmesi kolay olur.
        // Eğer SharedPreferences'te veri yoksa API yardımıyla yeni veri çekilmesi gerekir.
        // Bu fonksiyon bunun kontrolünü ve yönlendirmesini yapar.
        // @if'e girerse veri zaten SharedPreferences'tedir der ve fetch işlemine başlar.
        // Fetch işleminden sonra SharePreferences'teki veriler artık listededir. Fonksiyon listeyi doldurup Parse işlemi için ParseData class'ını çağırır.
        // @else'e girerse veri ilk defa çekilecekir. Network işlemlerini başlatır.


        new Data(MainActivity.this, this);



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == 1000 ) {

            Log.e("REQUEST_CODE", "OK");

            new LocationActivity(this, MainActivity.this).execute();

        } else {

            Log.e("REQUEST_CODE", requestCode + "");
        }

    }


    public void refresh(View view) {

        new GetData(MainActivity.this, this);

    }
}
