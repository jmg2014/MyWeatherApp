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
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.location.LocationManager;
import android.location.Criteria;
import android.location.Location;
import android.widget.Toast;
import android.speech.RecognizerIntent;

import java.util.Locale;
import java.util.ArrayList;



public class MainActivity extends ActionBarActivity {

    protected static final int REQUEST_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {


        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }



    }


    /** Called when the user clicks the Default button */
    public void showWeather(View view) {

        //Parser.getData();
        Intent intent = new Intent(this, ShowWeather.class);

        startActivity(intent);
    }

    /** Called when the user clicks the Default button */
    public void showLocation(View view) {

        LocationManager locationManager;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, String.valueOf(location.getLatitude())+" , "+String.valueOf(location.getLongitude()), duration);
        toast.show();

    }
    /** Called when the user clicks Voice button */
    public void speech(View view) {

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault().toString());

        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> information = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


            String command=information.get(0);
            Toast.makeText(this, command,Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, ShowWeather.class);
            i.putExtra("country", command);
            startActivity(i);


            //Result code for various error.
        }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
            Toast.makeText(this, "Audio Error",Toast.LENGTH_LONG).show();
        }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
            Toast.makeText(this, "Client Error",Toast.LENGTH_LONG).show();
        }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
            Toast.makeText(this,"Network Error",Toast.LENGTH_LONG).show();
        }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
            Toast.makeText(this, "No Match",Toast.LENGTH_LONG).show();
        }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
            Toast.makeText(this, "Server Error",Toast.LENGTH_LONG).show();
        }
    }
}
