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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

public class WeatherMapActivity extends ActionBarActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /*
    https://developer.android.com/training/location/retrieve-current.html
    http://www.coders-hub.com/2015/02/advance-android-google-map-2-tutorial-with-example.html#.VQ8c0_msWSo
     */
    GoogleApiClient mGoogleApiClient;
    static Location mLastLocation;


    static Double  mLatitude = 0.0;
    static  Double mLongitude=0.0;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
           // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
           // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
           Toast.makeText(getApplicationContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_SHORT).show();
            mLatitude=mLastLocation.getLatitude();
            mLongitude=mLastLocation.getLongitude();
        }
        else{
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map_activity);

        Intent intent = getIntent();
        mLatitude = intent.getExtras().getDouble("latitude");
        mLongitude=intent.getExtras().getDouble("longitude");

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

            switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) )
            {
                case ConnectionResult.SUCCESS:

                    mapView = (MapView) v.findViewById(R.id.map);
                    mapView.onCreate(savedInstanceState);
                    // Gets to GoogleMap from the MapView and does initialization stuff
                    if(mapView!=null)
                    {
                        map = mapView.getMap();

                        map.setMyLocationEnabled(true);

                        map.getUiSettings().setMyLocationButtonEnabled(true); //false to disable
                        map.getUiSettings().setZoomControlsEnabled(true); //false to disable
                        map.getUiSettings().setCompassEnabled(false); //false to disable
                        //)
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude,mLongitude), 13);
                        map.animateCamera(cameraUpdate);

                    }
                    break;
                case ConnectionResult.SERVICE_MISSING:
                    Toast.makeText(getActivity(), "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    Toast.makeText(getActivity(), "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                    break;
                default: Toast.makeText(getActivity(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()), Toast.LENGTH_SHORT).show();
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
    }
}
