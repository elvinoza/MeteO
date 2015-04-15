package com.example.elvinas.meteo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.elvinas.meteo.Constants.MainConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by elvinas on 15.4.11.
 */
public class AlarmReceiver extends BroadcastReceiver {
    SharedPreferences sharedpreferences;
    MainConstants constants;
    public static final String SettingsPREFERENCES = "SettingsPrefs";
    String stationId;
    double rain;
    @Override
    public void onReceive(Context context, Intent intent) {
        sharedpreferences = context.getSharedPreferences(SettingsPREFERENCES, Context.MODE_PRIVATE);
        if(sharedpreferences.contains("lookingStationId")){
            stationId = sharedpreferences.getString("lookingStationId", "");
            Log.d("stationid", stationId);
            new CheckStationInfo(context).execute();
        }
        //Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
    }

    public void showNotification(Context context, String msg){
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        Notification.Builder myNotification = new Notification.Builder(context)
                .setContentTitle("Station")
                .setContentText(msg)
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker("Notification!");
        myNotification.setContentIntent(contentIntent);
        myNotification.setDefaults(Notification.DEFAULT_SOUND);
        myNotification.setAutoCancel(true);

        NotificationManager nfM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nfM.notify(1,myNotification.build());
    }

    public void notifyUser(Context context, double rain){
        String msg;
        boolean notify = false;

        if(sharedpreferences.contains(constants.rainSpinnerValue) && sharedpreferences.contains(constants.rainSettedValue)){
            if(sharedpreferences.getString(constants.rainSpinnerValue, "").contains("More")) {
                if(rain >= Double.parseDouble(sharedpreferences.getString(constants.rainSettedValue,""))){
                    notify = true;
                }
            }
            else {
                if(rain < Double.parseDouble(sharedpreferences.getString(constants.rainSettedValue,""))){
                    notify = true;
                }
            }
        }
        msg = "Hey! Check your selected station last information!";
        if(notify)
            showNotification(context, msg);
    }

    private class CheckStationInfo extends AsyncTask<String, String, JSONObject> {

        private Context context;

        CheckStationInfo(Context cn){
            this.context = cn;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            JSONObject json = jParser.getJSONFromUrl("http://158.129.18.217:8000/api/v1/get/lastStationInformation/" + stationId);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONObject c = json.getJSONObject("information");
                Log.d("c", c.toString());
                rain = c.getDouble("rain");
                notifyUser(this.context, rain);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
