package net.furkankaplan.namaz724.gps;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.furkankaplan.namaz724.MainActivity;
import net.furkankaplan.namaz724.gps.model.DefaultLocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AsyncTask<Void, Void, Location> implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private Context context;
    private static final int INTERVAL = 0;
    private static final int FASTEST_INTERVAL = 0;
    private static final int PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static final String TAG = "LocationActivity";

    private static Location mCurrentLocation = null;
    GoogleApiClient mGoogleApiClient = null;
    LocationRequest mLocationRequest;


    String mLastUpdateTime;

    ProgressDialog progressDialog;

    private MainActivity mainActivity;

    public LocationActivity() {
    }

    public LocationActivity(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Location doInBackground(Void... voids) {

        Log.e(TAG,"Do in Background Running "+context);

        GoogleAPI(context);

        if (mCurrentLocation != null){
            Log.e(TAG,"Current Location Is Not Null "+mCurrentLocation);
            return mCurrentLocation;
        }
        return null;

    }


    @Override
    protected void onPostExecute(Location location) {
        super.onPostExecute(location);

        this.mCurrentLocation = location;
        this.updateUI();
    }

    private void updateUI() {
        Log.e(TAG, "UI update initiated .............");

        if (mCurrentLocation != null) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());

            Log.e("At Time: ",
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider());


            this.getGEO(mCurrentLocation);

            progressDialog.dismiss();
            if ( mGoogleApiClient.isConnected() ) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            }

        } else {
            Log.e(TAG, "location is null ...............");
        }
    }


    public void getGEO(Location location) {

        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        if ( location != null ){

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {

                        String city = adr.getAdminArea();
                        String country = adr.getCountryName();
                        String subAdminArea = adr.getSubAdminArea();



                        Log.e("RESULT_COUNTRY", country);
                        Log.e("RESULT_CITY", city);
                        Log.e("RESULT_SUBADMINAREA", subAdminArea);
                        Log.e("RESULT_LOCATION", location.toString());

                        mainActivity.takeItBack(
                                new DefaultLocation(
                                        country,
                                        city,
                                        subAdminArea,
                                        location
                                ));

                    }
                }
            }
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            // String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            // String city = addresses.get(0).getLocality();
            // String state = addresses.get(0).getAdminArea();
            // String country = addresses.get(0).getCountryName();
            // String postalCode = addresses.get(0).getPostalCode();
            // String knownName = addresses.get(0).getFeatureName();
            // tring subAdminArea = addresses.get(0).getSubAdminArea();
            // addresses.get(0).getAddressLine(0)


        } else {

            Log.e("RETURNED", "NULL FROM LOCATION");

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "ON CONNECTED CALLED GOOGLE API CONNECTED ");
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("SUSPENDED", i +"");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG,"On Location Changed");
        mCurrentLocation=location;
        updateUI();

    }
    private void requestLocationUpdate(int Interval,int Fastest,int Priority) {
        Log.e(TAG, "Request Location Update Calling");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Interval);
        mLocationRequest.setFastestInterval(Fastest);
        mLocationRequest.setPriority(Priority);
    }

    public void GoogleAPI(Context context){
        requestLocationUpdate(INTERVAL,FASTEST_INTERVAL,PRIORITY);
        Log.e(TAG,"Location Api Client Value "+ mGoogleApiClient);
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(context) // THIS LINE THROW NPE
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }

    }

}

