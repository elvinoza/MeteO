package com.example.elvinas.meteo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elvinas.meteo.Constants.MainConstants;
import com.example.elvinas.meteo.services.NotifyService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by elvinas on 15.4.10.
 */
public class MenuActivity extends Activity {

    SharedPreferences sharedpreferences;
    MainConstants constants = new MainConstants();
    Spinner rainSp, windSpeedSp, humiditySp, temperatureSp;
    Button saveBtn;
    EditText stationId, rainValue, windSpeedValue, humidityValue, temperatureValue;
    CheckBox notify, rainCheck, windSpeedCheck, humidityCheck, temperatureCheck;
    boolean isExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        sharedpreferences = getSharedPreferences(constants.SettingsPREFERENCES, Context.MODE_PRIVATE);
        getByIds();
        setSpinners();
        setDataFromPreferences();



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckExistStation st = new CheckExistStation();
                st.execute();

            }
        });
    }

    public void showDialog(String title, String msg, int infoType){
        new AlertDialog.Builder(MenuActivity.this)
                            .setTitle(title)
                            .setMessage(msg)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(infoType)
                            .show();
    }

    public void setSpinners(){
        ArrayAdapter<String> spAd = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, getResources().getStringArray(R.array.spinner_values));
        rainSp.setAdapter(spAd);
        windSpeedSp.setAdapter(spAd);
        humiditySp.setAdapter(spAd);
        temperatureSp.setAdapter(spAd);
    }

    public void getByIds(){
        stationId = (EditText) findViewById(R.id.stationId);
        notify = (CheckBox) findViewById(R.id.notifyCheckBox);
        rainSp = (Spinner) findViewById(R.id.rainSpinner);
        windSpeedSp = (Spinner) findViewById(R.id.windSpeedSpinner);
        humiditySp = (Spinner) findViewById(R.id.humiditySpinner);
        temperatureSp = (Spinner) findViewById(R.id.temperatureSpinner);
        saveBtn = (Button) findViewById(R.id.saveId);
        rainCheck = (CheckBox) findViewById(R.id.rainCheckBox);
        windSpeedCheck = (CheckBox) findViewById(R.id.windSpeedCheckBox);
        humidityCheck = (CheckBox) findViewById(R.id.humidityCheckBox);
        temperatureCheck = (CheckBox) findViewById(R.id.temperatureCheckBox);
        rainValue = (EditText) findViewById(R.id.rainSetedValue);
        windSpeedValue = (EditText) findViewById(R.id.windSpeedSetedValue);
        humidityValue = (EditText) findViewById(R.id.humiditySetedValue);
        temperatureValue = (EditText) findViewById(R.id.temperatureSetedValue);
    }

    public void saveSettings(String st){
        boolean can = true;
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(constants.lookingStationId, st);
        stopStartService();
        if(rainCheck.isChecked() && rainValue.getText().toString().isEmpty()){
            can = false;
            showDialog("Error", "Can't rain value be empty if it checked!", android.R.drawable.ic_dialog_alert);
        }
        editor.putString("rainSettedValue", rainValue.getText().toString());
        editor.putBoolean("notify", notify.isChecked());
        editor.putBoolean("rainCheck", rainCheck.isChecked());
        editor.putString("rainSettedSpiner", rainSp.getSelectedItem().toString());

        if(windSpeedCheck.isChecked() && windSpeedValue.getText().toString().isEmpty()){
            can = false;
            showDialog("Error", "Can't wind speed value be empty if it checked!", android.R.drawable.ic_dialog_alert);
        }

        if(humidityCheck.isChecked() && humidityValue.getText().toString().isEmpty()){
            can = false;
            showDialog("Error", "Can't humidity value be empty if it checked!", android.R.drawable.ic_dialog_alert);
        }

        if(temperatureCheck.isChecked() && temperatureValue.getText().toString().isEmpty()){
            can = false;
            showDialog("Error", "Can't temperature value be empty if it checked!", android.R.drawable.ic_dialog_alert);
        }

        editor.putString(constants.windSpeedSettedValue, windSpeedValue.getText().toString());
        editor.putBoolean(constants.windSpeedCheck, windSpeedCheck.isChecked());
        editor.putString(constants.winSpeedSpinerValue, windSpeedSp.getSelectedItem().toString());

        editor.putString(constants.temperatureSettedValue, temperatureValue.getText().toString());
        editor.putBoolean(constants.temperatureCheck, temperatureCheck.isChecked());
        editor.putString(constants.temperatureSpinerValue, temperatureSp.getSelectedItem().toString());

        editor.putString(constants.humiditySettedValue, humidityValue.getText().toString());
        editor.putBoolean(constants.humidityCheck, humidityCheck.isChecked());
        editor.putString(constants.humiditySpinerValue, humiditySp.getSelectedItem().toString());

        if(can)
            editor.commit();
    }

    public void stopStartService(){
        if(!notify.isChecked()){
            new NotifyService(MenuActivity.this).stop();
        } else {
            new NotifyService(MenuActivity.this).start();
        }
    }

    public void setDataFromPreferences(){
        if(sharedpreferences.contains(constants.lookingStationId)){
            stationId.setText(sharedpreferences.getString(constants.lookingStationId, ""));
        }
        if(sharedpreferences.contains("notify")){
            notify.setChecked(sharedpreferences.getBoolean("notify",true));
        }

        setOptionsFromPreferences(rainCheck, constants.rainCheck, rainValue, constants.rainSettedValue, rainSp, constants.rainSpinnerValue);
        setOptionsFromPreferences(windSpeedCheck, constants.windSpeedCheck, windSpeedValue, constants.windSpeedSettedValue, windSpeedSp, constants.winSpeedSpinerValue);
        setOptionsFromPreferences(humidityCheck, constants.humidityCheck, humidityValue, constants.humiditySettedValue, humiditySp, constants.humiditySpinerValue);
        setOptionsFromPreferences(temperatureCheck, constants.temperatureCheck, temperatureValue, constants.temperatureSettedValue, temperatureSp, constants.temperatureSpinerValue);

    }

    public void setOptionsFromPreferences(CheckBox box, String check, EditText text, String value, Spinner spin, String spinner){
        if(sharedpreferences.contains(check)){
            box.setChecked(sharedpreferences.getBoolean(check,true));
        }
        if(sharedpreferences.contains(value)){
            text.setText(sharedpreferences.getString(value,""));
        }
        if(sharedpreferences.contains(spinner)){
            if(sharedpreferences.getString(spinner, "").contains("More"))
                spin.setSelection(0);
            else spin.setSelection(1);
        }
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
            //startActivity(new Intent(this, About.class));
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

    private class CheckExistStation extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL

            JSONObject json = jParser.getJSONFromUrl("http://158.129.18.217:8000/api/v1/app/checkStation/" + stationId.getText().toString());
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                isExist = json.getBoolean("exist");
                if(!isExist){
                    showDialog("Sorry..", "This stations hasn't found in the server.", android.R.drawable.ic_dialog_alert);
                } else {
                    showDialog("Successful", "This station successfully found.", android.R.drawable.ic_dialog_info);
                }
                saveSettings(stationId.getText().toString());
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
