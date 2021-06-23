package com.pedro.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class NetworkUtils {
    public static final String TAG = NetworkUtils.class.getSimpleName();
    public static final String URL = "";

    public static URL buildUrl() {
        URL url = null;
        try {
            url = new URL(URL);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL " + url);
        return url;
    }
    public static String sendGET(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        StringBuffer response = new StringBuffer();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        Log.d(NetworkUtils.class.getSimpleName(), "GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            Log.d(NetworkUtils.class.getSimpleName(), response.toString());

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } else
            return "GET request not worked";

        Log.v(TAG, "GET " + url);
        return response.toString();
    }



}
