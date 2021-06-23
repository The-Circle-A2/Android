package com.pedro.tasks;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketConnection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;


    public void startConnection(String ip, int port) {
        //Socket clientSocket = null;
        try {
            // ws://seechange-chat.the-circle.designone.nl:80
            clientSocket = IO.socket("http://localhost:3000");
            clientSocket.connect();
            clientSocket.emit("joinstream", "");
            //out = new PrintWriter(clientSocket.getOutputStream(), true);
            //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

        public void sendMessage(String msg) throws IOException {
            System.out.println(msg);

            if (TextUtils.isEmpty(msg)) {
                return;
            }

            clientSocket.emit("chatMessage", msg);
//            JSONObject object = new JSONObject();
//            try {
//                object.put("_id", "?");
//                object.put("name", "message username");
//                object.put("date", "message.time");
//                object.put("message", "message.message");
//                object.put("info", false);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(object);
//            out.println(msg);
//            String resp = in.readLine();
//            return resp;
        }

        public void stopConnection() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }
    }
