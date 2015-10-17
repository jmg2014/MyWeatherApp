/*
 * Copyright 2015 Jorge Manrique
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobile.myweather.app;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.mobile.myweather.factory.FactoryWeather;
import com.mobile.myweather.parser.RestClient;
import com.mobile.myweather.parser.WeatherResponse;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeatherMapActivity extends ActionBarActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static String LOG_TAG=WeatherMapActivity.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;
    static Location mLastLocation;
    static MediaPlayer mPlayer;
    private static LocationRequest mLocationRequest;

    static Double mLatitude = 0.0;
    static Double mLongitude = 0.0;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest=LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1800000);//30 minutes
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            Toast.makeText(getApplicationContext(), String.valueOf(mLastLocation.getLatitude())
                    +" , "+String.valueOf(mLastLocation.getLongitude())
                    , Toast.LENGTH_SHORT).show();
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        } else {
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
       Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map_activity);

        Intent intent = getIntent();
        mLatitude = intent.getExtras().getDouble("latitude");
        mLongitude = intent.getExtras().getDouble("longitude");

        buildGoogleApiClient();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        android.support.v7.app.ActionBar actionBar;

        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#2f6699"));
        actionBar.setBackgroundDrawable(colorDrawable);

        new FetchWeatherTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_map_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;

        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        MapView mapView;
        GoogleMap map;


        public PlaceholderFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_weather_map, container, false);
            // Gets the MapView from the XML layout and creates it

            //setRetainInstance(true);

            MapsInitializer.initialize(getActivity());

            switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
                case ConnectionResult.SUCCESS:

                    mapView = (MapView) v.findViewById(R.id.map);
                    mapView.onCreate(savedInstanceState);
                    // Gets to GoogleMap from the MapView and does initialization stuff
                    if (mapView != null) {
                        map = mapView.getMap();

                        map.setMyLocationEnabled(true);

                        map.getUiSettings().setMyLocationButtonEnabled(true); //false to disable
                        map.getUiSettings().setZoomControlsEnabled(true); //false to disable
                        map.getUiSettings().setCompassEnabled(false); //false to disable

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 13);
                        map.animateCamera(cameraUpdate);

                    }
                    break;
                case ConnectionResult.SERVICE_MISSING:
                    Toast.makeText(getActivity(), "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    Toast.makeText(getActivity(), "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getActivity(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()), Toast.LENGTH_SHORT).show();
            }


            // Updates the location and zoom of the MapView

            return v;
        }

        @Override
        public void onResume() {
            mapView.onResume();
            super.onResume();
        }

        @Override
        public void onDestroy() {
            if (mPlayer!=null) {
                mPlayer.stop();
            }

            super.onDestroy();
            mapView.onDestroy();


        }

        @Override
        public void onPause() {
            super.onPause();
            mapView.onPause();
            map.setMyLocationEnabled(false);


        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mapView.onLowMemory();

        }
        @Override
        public void onStart(){
            super.onStart();
            mGoogleApiClient.connect();

        }
        @Override
        public void onStop(){

            super.onStop();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, List<String>> {
        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        @Override
        protected List<String> doInBackground(Void... params) {


            final ArrayList<String> result = new ArrayList<String>();
            // Sleep for a small amount of time to simulate a background-task
            try {


                RestClient.get().getWeatherLatLng(String.valueOf(mLatitude), String.valueOf(mLongitude),
                        getResources().getString(R.string.OpenWeatherApiKey),
                        new Callback<WeatherResponse>() {
                    @Override
                    public void success(WeatherResponse weatherResponse, Response response) {
                        // success!

                        if (weatherResponse.getCod() == 200) {

                            String temp = String.valueOf(Double.valueOf(weatherResponse.getMain().getTemp() - 273.15).intValue()) + "° C";
                            DecimalFormat df = new DecimalFormat("#.##");
                            String wind = String.valueOf(df.format(weatherResponse.getWind().getSpeed() * 3.6) + " km/h");
                            String icon = String.valueOf(weatherResponse.getWeather().get(0).getIcon());
                            String status=weatherResponse.getWeather().get(0).getMain();

                            result.add(temp);
                            result.add(wind);
                            result.add(icon);
                            result.add(status);


                            Log.i(LOG_TAG, weatherResponse.getSys().getCountry());
                        } else {

                            Log.i(LOG_TAG, "ERROR loading information");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                        Log.e(LOG_TAG, error.getMessage());
                    }
                });
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // Return all the values needed to show the information
            return result;
        }

        @Override
        protected void onPostExecute(List<String> result) {

            super.onPostExecute(result);

            //Show icons
            if (result != null && result.size() > 0) {
                // Tell the Fragment that the refresh has completed
                TextView temp=(TextView)findViewById(R.id.temp);
                temp.setText(result.get(0));
                temp.setVisibility(View.VISIBLE);

                TextView wind=(TextView)findViewById(R.id.wind);
                wind.setText(result.get(1));
                wind.setVisibility(View.VISIBLE);


                int identifier=FactoryWeather.getFlag(result.get(2), getResources(), WeatherMapActivity.this);
                ImageView icon=(ImageView)findViewById(R.id.weatherIcon);
                icon.setImageResource(identifier);
                icon.setVisibility(View.VISIBLE);

                ImageView temp_icon=(ImageView)findViewById(R.id.temp_map);
                temp_icon.setVisibility(View.VISIBLE);

                ImageView flag_icon=(ImageView)findViewById(R.id.flag_map);
                flag_icon.setVisibility(View.VISIBLE);

                //Sound
                AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                switch( audio.getRingerMode() ){
                    case AudioManager.RINGER_MODE_NORMAL:
                        if (result.get(3).toLowerCase().contains("rain")) {
                            mPlayer = MediaPlayer.create(WeatherMapActivity.this, R.raw.rain);
                            mPlayer.start();

                        }
                        else if (result.get(3).toLowerCase().contains("clear")) {
                            mPlayer = MediaPlayer.create(WeatherMapActivity.this, R.raw.clear_sky);
                            mPlayer.start();
                        }
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        break;
                }



            }
        }

    }
}
