package com.example.elvinas.meteo.services;

import android.os.AsyncTask;

import com.example.elvinas.meteo.Interfaces.AsyncResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by elvinas on 15.4.10.
 */
public class MeteoService extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;

    @Override
    protected void onPostExecute(String result){
        delegate.processFinish(result);
    }

    @Override
    protected String doInBackground(String... string) {
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet get = new HttpGet("http://158.129.18.217:8000/api/v1/get/lastStationInformation/3RkTSJ");
        // Depends on your web service
        get.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();

        } catch (Exception e) {
            result = e.toString();
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
        return result;
    }
}
