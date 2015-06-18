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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.mobile.myweather.factory.FactoryFlag;
import com.mobile.myweather.factory.FactoryWeather;
import com.mobile.myweather.list.CustomList;
import com.mobile.myweather.parser.Api;
import com.mobile.myweather.parser.RestClient;
import com.mobile.myweather.parser.WeatherResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import retrofit.RestAdapter;


public class ShowWeather extends ActionBarActivity {

    private static String LOG_TAG=ShowWeather.class.getSimpleName();
    private static String mCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);

        Intent intent = getIntent();
        mCountry = intent.getExtras().getString("country");

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
        getMenuInflater().inflate(R.menu.menu_show_weather, menu);
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

        private ArrayAdapter<String>  mForecastAdapter;//remove

        //swipe layout
        /**
         * The {@link android.support.v4.widget.SwipeRefreshLayout} that detects swipe gestures and
         * triggers callbacks in the app.
         */
        private SwipeRefreshLayout mSwipeRefreshLayout;

        /**
         * The {@link android.widget.ListView} that displays the content that should be refreshed.
         */
        private ListView mListView;

        /**
         * The {@link android.widget.ListAdapter} used to populate the {@link android.widget.ListView}
         * defined in the previous statement.
         */
        private ArrayAdapter<String> mListAdapter;

        MediaPlayer mPlayer;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_show_weather, container, false);


            //swipe
            // Retrieve the SwipeRefreshLayout and ListView instances
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);

            // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids

            mSwipeRefreshLayout.setColorScheme(
                    R.color.blue, R.color.purple,
                    R.color.green, R.color.orange);

            // Retrieve the ListView
            mListView = (ListView) rootView.findViewById(android.R.id.list);

            return rootView;
        }

        //swipe
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);




            String[] data = {
                    "    Loading...",
                    "Loading...",
                    "Loading...",
                    "Loading...",
                    "Loading...",
                    "Loading...",
                    "Loading...",
                    "Loading..."
            } ;
            Integer[] imageId = {
                    R.drawable.thermometer,
                    R.drawable.i04d,
                    R.drawable.uk,
                    R.drawable.wind_flag,
                    R.drawable.pressure,
                    R.drawable.humidity,
                    R.drawable.sunrise,
                    R.drawable.moon

            };
            CustomList adapter = new CustomList(getActivity(), data, imageId);

            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                   // Toast.makeText( getActivity(), "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                    Toast.makeText( getActivity(), "You Clicked "+parent.getItemAtPosition(2).toString() , Toast.LENGTH_SHORT).show();
                }
            });


            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                    initiateRefresh();
                }
            });
            initiateRefresh();

        }


        private void initiateRefresh() {
            Log.i(LOG_TAG, "initiateRefresh");

            /**
             * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
             */
            new FetchWeatherTask().execute();
        }

        private class FetchWeatherTask extends AsyncTask<Void, Void, WeatherResponse> {

            RestAdapter restAdapter;
            @Override
            protected void onPreExecute() {
                restAdapter = new RestAdapter.Builder()
                        .setEndpoint(RestClient.ROOT)
                        .build();
            }


            @Override
            protected WeatherResponse doInBackground(Void... params) {
                WeatherResponse weather;
                try {
                    Api methods = restAdapter.create(Api.class);
                     weather = methods.getWeather(mCountry);
                }catch (Exception ex){
                    weather=null;
                }

                return weather;
            }

            @Override
            protected void onPostExecute(WeatherResponse weatherResponse) {

                if (weatherResponse!=null  && weatherResponse.getCod() ==200) {

                    ArrayList<String> result = new ArrayList<String>();

                    result.add("  " + String.valueOf(Double.valueOf(weatherResponse.getMain().getTemp() - 273.15).intValue()) + "° C");
                    result.add(weatherResponse.getWeather().get(0).getMain());
                    result.add(weatherResponse.getName());

                    DecimalFormat df = new DecimalFormat("#.##");

                    result.add(String.valueOf(df.format(weatherResponse.getWind().getSpeed() * 3.6) + " km/h"));
                    result.add(String.valueOf(weatherResponse.getMain().getPressure() + " hPa"));
                    result.add(String.valueOf(weatherResponse.getMain().getHumidity() + " %"));

                    TimeZone zone=TimeZone.getDefault();

                    long time = Long.valueOf(weatherResponse.getSys().getSunrise()) * (long) 1000;
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone(zone.getID()));

                    result.add(format.format(date) + " " + zone.getDisplayName(false, TimeZone.SHORT, Locale.getDefault()));

                    time = Long.valueOf(weatherResponse.getSys().getSunset()) * (long) 1000;
                    date = new Date(time);

                    result.add(format.format(date) +" "+ zone.getDisplayName(false, TimeZone.SHORT, Locale.getDefault()));
                    result.add(weatherResponse.getWeather().get(0).getIcon());
                    result.add(weatherResponse.getSys().getCountry());


                    String[] data = {
                            result.get(0),
                            result.get(1),
                            result.get(2),
                            result.get(3),
                            result.get(4),
                            result.get(5),
                            result.get(6),
                            result.get(7)
                    };
                    //Update icons
                    Integer[] imageId = {
                            R.drawable.thermometer,
                            FactoryWeather.getFlag(result.get(8), getResources(), getActivity()),
                            FactoryFlag.getFlag(result.get(9), getResources(), getActivity()),
                            R.drawable.wind_flag,
                            R.drawable.pressure,
                            R.drawable.humidity,
                            R.drawable.sunrise,
                            R.drawable.moon

                    };

                    //Sound
                    if (result.get(1).toLowerCase().contains("rain")) {
                        mPlayer = MediaPlayer.create(getActivity(), R.raw.rain);
                        mPlayer.start();

                    } else if (result.get(1).toLowerCase().contains("clear")) {
                        mPlayer = MediaPlayer.create(getActivity(), R.raw.clear_sky);
                        mPlayer.start();
                    }

                    CustomList adapter = new CustomList(getActivity(), data, imageId);

                    mListView.setAdapter(adapter);





                    // Stop the refreshing indicator
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                //it was a error
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.alert_title))
                            .setMessage(mCountry+" : "+getResources().getString(R.string.alert_message))
                            .setCancelable(false)
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(getActivity(), MainActivity.class);
                                    startActivity(i);
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setIcon(R.drawable.warning);
                    alert.show();
                }



            }
        }
        @Override
        public void onDestroy() {

            if (mPlayer != null){
                mPlayer.stop();
            }
            super.onDestroy();

        }
    }
}
