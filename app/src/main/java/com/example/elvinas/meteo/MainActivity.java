package com.example.elvinas.meteo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elvinas.meteo.Interfaces.AsyncResponse;
import com.example.elvinas.meteo.services.MeteoService;
import com.example.elvinas.meteo.services.NotifyService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Calendar;
import java.util.jar.Manifest;


public class MainActivity extends Activity  {
    SharedPreferences sharedpreferences;
    public static final String SettingsPREFERENCES = "SettingsPrefs";
    TextView temp, hum, light,pres,windDir, windSpeed, dRain, date;
    ImageButton refreshButton;
    private static final String info = "information";
    private static final String temperature = "temperature";
    private static final String humidity = "humidity";
    private static final String light_level = "light_level";
    private static final String pressure = "pressure";
    private static final String wind_direction = "wind_direction";
    private static final String wind_speed = "wind_speed";
    private static final String rain = "rain";
    private static final String created_at = "created_at";
    JSONObject stationInfo = null;
    private PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        addListenerOnButton();

        sharedpreferences = getSharedPreferences(SettingsPREFERENCES, Context.MODE_PRIVATE);

        if(sharedpreferences.getBoolean("notify", true)){
            new NotifyService(MainActivity.this).start();
        }
        new JSONParse().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(MainActivity.this, MenuActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addListenerOnButton() {

        refreshButton = (ImageButton) findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new JSONParse().execute();
            }

        });
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
            temp = (TextView)findViewById(R.id.temperatureValue);
            hum = (TextView)findViewById(R.id.humidityValue);
            light = (TextView)findViewById(R.id.lightLevelValue);
            pres = (TextView)findViewById(R.id.pressureValue);
            windDir = (TextView)findViewById(R.id.windDirectionValue);
            windSpeed = (TextView)findViewById(R.id.windSpeedValue);
            dRain = (TextView)findViewById(R.id.rainValue);
            date = (TextView)findViewById(R.id.updatedAtValue);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Updating information ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl("http://158.129.18.217:8000/api/v1/get/lastStationInformation/3RkTSJ");
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            try {
                JSONObject c = json.getJSONObject(info);
                temp.setText(Integer.toString((int) Math.round(Double.parseDouble(c.getString(temperature)))));
                hum.setText(c.getString(humidity));
                light.setText(c.getString(light_level));
                pres.setText(c.getString(pressure));
                windDir.setText(c.getString(wind_direction));
                windSpeed.setText(c.getString(wind_speed));
                dRain.setText(c.getString(rain));
                date.setText(c.getString(created_at));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
