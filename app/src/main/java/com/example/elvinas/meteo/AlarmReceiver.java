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
    double rain, windSpeed, humidity, temperature;
    @Override
    public void onReceive(Context context, Intent intent) {
        sharedpreferences = context.getSharedPreferences(SettingsPREFERENCES, Context.MODE_MULTI_PROCESS);
        if(sharedpreferences.contains("lookingStationId")){
            stationId = sharedpreferences.getString("lookingStationId", "");
            new CheckStationInfo(context).execute();
        }
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

    public boolean checkIfBeforeWillBeNotified(){
        SharedPreferences.Editor ed = sharedpreferences.edit();
        boolean notify = false;
        if(sharedpreferences.contains("willBeNotified")){
            notify = sharedpreferences.getBoolean("willBeNotified", true);
            ed.putBoolean("willBeNotified", notify);
            ed.commit();
            return notify;
        }
        return notify;
    }

    public void setNotified(boolean status){
        SharedPreferences.Editor ed = sharedpreferences.edit();
        ed.putBoolean("willBeNotified", status);
        ed.commit();
    }

    public boolean getNotified(){
        return sharedpreferences.getBoolean("willBeNotified", true);
    }

    public boolean compareAndCheckNotify(String operator, double var1, double var2){
        boolean result;
        if(operator.contains("More")){
            result = var1 < var2;
        } else if(operator.contains("Less")){
            result = var1 > var2;
        } else {
            result = var1 == var2;
        }
        if(result && !getNotified()){
            setNotified(true);
        } else if (result && getNotified()) {
            setNotified(false);
        } else if(!result && getNotified()){
            setNotified(false);
        } else if(!result && !getNotified()){
            setNotified(false);
        }
        return result;
    }

    public void notifyUser(Context context, double rain){
        String msg;
        boolean notify = false;

        if(sharedpreferences.contains(constants.rainSpinnerValue) && sharedpreferences.contains(constants.rainSettedValue) && sharedpreferences.contains(constants.rainCheck)){
            if(sharedpreferences.getBoolean(constants.rainCheck, true)){
                notify = compareAndCheckNotify(sharedpreferences.getString(constants.rainSpinnerValue, ""), Double.parseDouble(sharedpreferences.getString(constants.rainSettedValue,"")), rain);
            }
        }

        if(!notify && sharedpreferences.contains(constants.winSpeedSpinerValue) && sharedpreferences.contains(constants.windSpeedSettedValue) && sharedpreferences.contains(constants.windSpeedCheck)){
            if(sharedpreferences.getBoolean(constants.windSpeedCheck, true)){
                notify = compareAndCheckNotify(sharedpreferences.getString(constants.winSpeedSpinerValue, ""), Double.parseDouble(sharedpreferences.getString(constants.windSpeedSettedValue,"")), windSpeed);
            }
        }

        if(!notify && sharedpreferences.contains(constants.humiditySpinerValue) && sharedpreferences.contains(constants.humiditySettedValue) && sharedpreferences.contains(constants.humidityCheck)){
            if(sharedpreferences.getBoolean(constants.humidityCheck, true)){
                notify = compareAndCheckNotify(sharedpreferences.getString(constants.humiditySpinerValue, ""), Double.parseDouble(sharedpreferences.getString(constants.humiditySettedValue,"")), humidity);
            }
        }

        if(!notify && sharedpreferences.contains(constants.temperatureSpinerValue) && sharedpreferences.contains(constants.temperatureSettedValue) && sharedpreferences.contains(constants.temperatureCheck)){
            if(sharedpreferences.getBoolean(constants.temperatureCheck, true)){
                notify = compareAndCheckNotify(sharedpreferences.getString(constants.temperatureSpinerValue, ""), Double.parseDouble(sharedpreferences.getString(constants.temperatureSettedValue,"")), temperature);
            }
        }

        if(notify && getNotified()) {
            msg = "Hey! Check your selected station last information!";
            showNotification(context, msg);
        }
        if(notify)
            if(!getNotified())
                setNotified(true);
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

            JSONObject json = jParser.getJSONFromUrl(constants.BASE_URL + "/get/lastStationInformation/" + stationId);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONObject c = json.getJSONObject("information");
                Log.d("c", c.toString());
                rain = c.getDouble("rain");
                windSpeed = c.getDouble("wind_speed");
                humidity = c.getDouble("humidity");
                temperature = c.getDouble("temperature");
                notifyUser(this.context, rain);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
