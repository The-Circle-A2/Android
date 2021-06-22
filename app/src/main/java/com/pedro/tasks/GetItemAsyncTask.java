package com.pedro.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.pedro.rtpstreamer.defaultexample.StreamActivity;
import com.pedro.model.Message;
import com.pedro.utils.NetworkUtils;
import com.pedro.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GetItemAsyncTask extends AsyncTask<Void, Void, ArrayList<Message>> {
    private JSONArray array;

    @Override
    protected ArrayList<Message> doInBackground(Void... str) {
        ArrayList<Message> messages = new ArrayList<>();

        URL url = NetworkUtils.buildUrl();
        String response = null;

        try {
            response = NetworkUtils.sendGET(url);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            if(response != null) {
                JSONObject json = new JSONObject(response);
                array = json.getJSONArray("messages");
            } else
                throw new RuntimeException("Response is null.");

            for (int i=0; i < array.length(); i++) {
                JSONObject jArray = array.getJSONObject(i);
                JSONObject messageJson = jArray.getJSONObject("attributes");

                int id = 0;
                String userName = "";
                String text = "";
                String image = "";

                if (messageJson.has("id")) {
                    id = messageJson.getInt("id");
                }

                if (messageJson.has("userName")) {
                    userName = messageJson.getString("userName");
                }

                if (messageJson.has("text")) {
                    text = messageJson.getString("text");
                }

                if (messageJson.has("image")) {
                    image = messageJson.getString("image");
                }

                SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = null;

                if(messageJson.has("date") && !messageJson.isNull("date")){
                    String inputDate = messageJson.getString("date");
                    try {
                        Date inputDateRaw = new Date(Long.parseLong(inputDate));
                        String dateRaw = newFormat.format(inputDateRaw);
                        date = newFormat.parse(dateRaw);
                    } catch (ParseException e){
                        e.printStackTrace();
                    }
                }

                Message message = new Message(id, userName, text, date, image);
                messages.add(message);
            }
        } catch(JSONException e){
            e.printStackTrace();
            return messages;
        }
        return messages;
    }
}
