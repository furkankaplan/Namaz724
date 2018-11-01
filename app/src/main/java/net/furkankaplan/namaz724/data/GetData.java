package net.furkankaplan.namaz724.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import net.furkankaplan.namaz724.BuildConfig;
import net.furkankaplan.namaz724.Data;
import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.R;
import net.furkankaplan.namaz724.gps.LocationActivity;
import net.furkankaplan.namaz724.gps.model.DefaultLocation;
import net.furkankaplan.namaz724.network.RetrofitRxJava;
import net.furkankaplan.namaz724.network.model.City;
import net.furkankaplan.namaz724.network.model.SubAdminArea;

import org.json.JSONArray;
import org.json.JSONException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetData extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "GetData";


    /**
     * Code used in requesting runtime permissions.
     */
    GoogleApiClient mGoogleApiClient;

    Context context;
    MainActivity activity;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    public GetData(MainActivity activity) {
        this.activity = activity;
    }

    public GetData(Context context, MainActivity activity) {

        this.context = context;
        this.activity = activity;

        // Aşağıdaki kod bloğu ve connect fonksiyonu locationChecker fonksiyonu için yaratılıyor.
        mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        this.checkPromptInternetConnection(null);

    }

    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean checkPromptInternetConnection(DialogInterface dialog) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

            this.locationChecker(mGoogleApiClient, activity);
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                            this.locationChecker(mGoogleApiClient, activity);

                        } else if (!checkPermissions()) {
                            requestPermissions();
                        }

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /**
     * Prompt user to enable GPS and Location Services
     * @param mGoogleApiClient
     * @param activity
     */
    public void locationChecker(GoogleApiClient mGoogleApiClient, final MainActivity activity) {


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

                        new LocationActivity(context, activity).execute();
                        // requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult( activity , 1000 );
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("ERR", "ERR");

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


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale( activity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {

            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity,
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
                this.locationChecker(mGoogleApiClient, activity);

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


    public void getTimeList(DefaultLocation defaultLocation) {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (defaultLocation != null) {


            JSONArray jsonArray = null; // it contains the json of the countries.
            try {
                jsonArray = new JSONArray(Data.country);

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
                                                                    if (obj2.getSubAdminAreaName().equals(defaultLocation.getSubAdminArea().toUpperCase()) || obj2.getSubAdminAreaName().equals(defaultLocation.getCity().toUpperCase())) {

                                                                        k = sizeOfSubAdminAreas - 1;

                                                                        final String subAdminAreaID = obj2.getSubAdminAreaID();

                                                                        Disposable subscribe = new RetrofitRxJava().getAPI()
                                                                                .getTimes(subAdminAreaID)
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(times -> new ParsData(activity, activity, times, true));

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
