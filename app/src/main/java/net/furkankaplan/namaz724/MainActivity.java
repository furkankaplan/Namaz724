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


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    TextView cityTextView, subAdminAreaTextView, toWhichTimeTextView, remainTimeTextView, todayTextView;
    LinearLayout timeContainerLayout;

    String countries = Data.country;

    List<Time> timeList = new ArrayList<>();

    /**
     * Code used in requesting runtime permissions.
     */
    GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String TAG = MainActivity.class.getSimpleName();

    DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    private static boolean isAfterYatsi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPrefs.edit();

        this.setupView();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();



        this.startToFetch();

    }


    private void  startToFetch() {

        if (sharedPrefs.getString("TIME_LIST", null) != null) {

            Log.e("DEBUG", "TIME_LIST boş değil");

            try {


                if ( !isServiceWorking() ) {

                    Toast.makeText(this, "STARTED", Toast.LENGTH_SHORT).show();
                    startService(new Intent(this, MainService.class));

                }


                this.promptData(null);
                if (sharedPrefs.getString("TIME_ADMINAREA", null) != null) {
                    Log.e("DEBUG", "TIME_ADMINAREA boş değil");

                    this.cityTextView.setText(sharedPrefs.getString("TIME_ADMINAREA", null));
                } else {
                    Log.e("DEBUG", "TIME_ADMINAREA boş ");
                }
                if (sharedPrefs.getString("TIME_SUBADMINAREA", null) != null) {
                    this.subAdminAreaTextView.setText(sharedPrefs.getString("TIME_SUBADMINAREA", null));
                    Log.e("DEBUG", "TIME_SUBADMINAREA boş değil");

                } else {
                    Log.e("DEBUG", "TIME_SUBADMINAREA boş ");
                }

            } catch (ParseException | JSONException e) {
                Log.e("DEBUG", e.toString());
            }

        } else {

            Log.e("DEBUG", "TIME_LIST boş");

            this.checkPromptInternetConnection(null);

        }

    }

    private boolean isServiceWorking() {

        ActivityManager serviceManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : serviceManager.getRunningServices(Integer.MAX_VALUE) ) {

            if ( getApplication().getPackageName().equals(service.service.getPackageName()) ) {

                return true;

            }

        }

        return false;

    }


    private void logout() {

        editor.remove("TIME_COUNTRY");
        editor.remove("TIME_ADMINAREA");
        editor.remove("TIME_SUBADMINAREA");
        editor.remove("TIME_LIST");
        editor.apply();

    }

    /**
     * Step 3: Start the Location Monitor Service
     */


    private void setupView() {

        cityTextView = findViewById(R.id.city);
        subAdminAreaTextView = findViewById(R.id.subAdminArea);
        toWhichTimeTextView = findViewById(R.id.toWhichTime);
        remainTimeTextView = findViewById(R.id.remainTime);
        timeContainerLayout = findViewById(R.id.timeContainer);
        todayTextView = findViewById(R.id.today);

    }


    /**
     * Prompt user to enable GPS and Location Services
     * @param mGoogleApiClient
     * @param activity
     */
    public void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {

                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        Log.e("RETURED", "SUCCCSS");

                         new LocationActivity(MainActivity.this, MainActivity.this).execute();


                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        Log.e("RETURED", "RESOLUTION_REQUIRED");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.e("RETURED", "SETTINGS_CHANGE_UNAVAILABLE");

                        break;

                    default:

                        Log.e("DEFAULT", status.getStatus().toString());
                }
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == 1000 ) {

            Log.e("REQUEST_CODE", "OK");

            new LocationActivity(MainActivity.this, this).execute();

        } else {

            Log.e("REQUEST_CODE", requestCode + "");
        }

    }

    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean checkPromptInternetConnection(DialogInterface dialog) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }

        if (dialog != null) {
            dialog.dismiss();
        }

        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            Log.e("DEBUG", "check and permissson OK, başlagıç verisi çekilecek.");

            this.locationChecker(mGoogleApiClient, MainActivity.this);
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = "positive text";
        builder.setPositiveButton(positiveText,
                (dialog, which) -> {

                    //Block the Application Execution until user grants the permissions
                    if (checkPromptInternetConnection(dialog)) {

                        //Now make sure about location permission.
                        if (checkPermissions()) {

                            //Step 2: Start the Location Monitor Service
                            //Everything is there to start the service.
                            this.locationChecker(mGoogleApiClient, MainActivity.this);
                        } else if (!checkPermissions()) {
                            requestPermissions();
                        }

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {

            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {

        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                this.locationChecker(mGoogleApiClient, MainActivity.this);

            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }



    private void fillTheArea(DefaultLocation defaultLocation) {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (defaultLocation != null) {


            JSONArray jsonArray = null; // it contains the json of the countries.
            try {
                jsonArray = new JSONArray(countries);

                final int sizeOfCountries = jsonArray.length();

                for (int i = 0; i < sizeOfCountries; i++) {

                    String country = jsonArray.getJSONObject(i).getString("UlkeAdi");
                    String countryID = jsonArray.getJSONObject(i).getString("UlkeID");

                    if (country.equals(defaultLocation.getCountry().toUpperCase())) { // get the country and its other field which country is being matched with location country.

                        i = sizeOfCountries - 1;
                        Disposable a = new RetrofitRxJava().getAPI()
                                .getCities(countryID)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(countryResponse -> { // Find the states by using the country that's just found.

                                            final int sizeOfCities = countryResponse.size();
                                            for (int j = 0; j < sizeOfCities; j++) {

                                                City obj = countryResponse.get(j);
                                                if (obj.getCityName().equals(defaultLocation.getCity().toUpperCase())) {

                                                    j = sizeOfCities - 1;

                                                    final String cityID = obj.getCityID();

                                                    new RetrofitRxJava().getAPI()
                                                            .getSubAdminAreas(cityID)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(cityResponse -> {

                                                                final int sizeOfSubAdminAreas = cityResponse.size();

                                                                for (int k = 0; k < sizeOfSubAdminAreas; k++) {

                                                                    SubAdminArea obj2 = cityResponse.get(k);
                                                                    if (obj2.getSubAdminAreaName().equals(defaultLocation.getSubAdminArea().toUpperCase())) {

                                                                        k = sizeOfSubAdminAreas - 1;

                                                                        final String subAdminAreaID = obj2.getSubAdminAreaID();

                                                                        Disposable subscribe = new RetrofitRxJava().getAPI()
                                                                                .getTimes(subAdminAreaID)
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(this::promptData);

                                                                    }

                                                                }

                                                            });

                                                }

                                            }

                                        },
                                        error -> Log.e("messageNotifi", error.toString()),
                                        () -> {


                                        });

                    }

                }
            }catch (JSONException e) {
                Log.e("EER", e.toString());
            }
        }

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

            Log.e("DEBUG", "SharedPreferences dolu, liste pars edildi.");


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
            Date  todayDate = calendar.getTime();
            String todayString = formatterForDate.format(todayDate);

            todayTextView.setText(todayString);

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


                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - gunesDate.getTime()));
                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");

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

                            if ( diffInMillies2 == 0 ) {


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "Güneş");
                                }
                            });

                        }
                    }, delay, period);


                } else if ( nowDate.before(ogleDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - ogleDate.getTime()));

                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Öğle");


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

                            if ( diffInMillies2 == 0 ) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "Öğle");
                                }
                            });

                        }
                    }, delay, period);


                } else if ( nowDate.before(ikindiDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - ikindiDate.getTime()));

                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "İkindi");


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

                            if ( diffInMillies2 == 0 ) {
                                onCreate(new Bundle());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "İkindi");
                                }
                            });

                        }
                    }, delay, period);


                } else if ( nowDate.before(aksamDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - aksamDate.getTime()));

                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Akşam");


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

                            if ( diffInMillies2 == 0 ) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "Akşam");
                                }
                            });

                        }
                    }, delay, period);


                } else if ( nowDate.before(yatsiDate)) {

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - yatsiDate.getTime()));

                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Yatsı");


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

                            if ( diffInMillies2 == 0 ) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "Yatsı");
                                }
                            });

                        }
                    }, delay, period);

                }  else {

                    Calendar calendarForTomorrow = Calendar.getInstance();
                    calendarForTomorrow.add(Calendar.DAY_OF_YEAR, 1);
                    Date  tomorrowDate = calendarForTomorrow.getTime();
                    String tomorrowString = formatterForDate.format(tomorrowDate);

                    Date tomorrowGunesDate = sdf.parse(tomorrowString +" "+ subAdminAreaResponse.get(z+1).getGünes()+":00");

                    int diffInMillies =(int)(Math.abs(nowDate.getTime() - tomorrowGunesDate.getTime()));

                    this.promptTimeAndRemaining(getTimeRemainingString(diffInMillies), "Güneş");


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

                            if ( diffInMillies2 == 0 ) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startToFetch();
                                    }
                                });
                                time.cancel();
                                time.purge();

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    promptTimeAndRemaining(getTimeRemainingString(diffInMillies2), "Güneş2");
                                    todayTextView.setText(todayString2);
                                }
                            });

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

    public void promptTimeAndRemaining(String diff, String time) {

        toWhichTimeTextView.setText(time + " Vaktine");
        remainTimeTextView.setText(diff);
    }

    public String getTimeRemainingString(long vdiffInMillies) {

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
        @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("ERR", connectionResult.getErrorMessage());
    }

    public void takeItBack(DefaultLocation defaultLocation) {

        if (defaultLocation != null) {

            Log.e("DEBUG", "calback çalıştı, location çekildi.");

            this.fillTheArea(defaultLocation);
            cityTextView.setText(defaultLocation.getCity());
            subAdminAreaTextView.setText(defaultLocation.getSubAdminArea());
            editor.putString("TIME_COUNTRY", defaultLocation.getCountry());
            editor.putString("TIME_ADMINAREA", defaultLocation.getCity());
            editor.putString("TIME_SUBADMINAREA", defaultLocation.getSubAdminArea());
            editor.apply();


        } else {
            Toast.makeText(this, "location empty", Toast.LENGTH_SHORT).show();
        }

    }

}
