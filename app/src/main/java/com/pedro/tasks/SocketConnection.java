package com.pedro.tasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketConnection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;


    public void startConnection(String ip, int port) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("http://localhost/", 3000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public String sendMessage(String msg) throws IOException {
            JSONObject object = new JSONObject();
            try {
                object.put("_id", "?");
                object.put("name", "message username");
                object.put("date", "message.time");
                object.put("message", "message.message");
                object.put("info", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(object);
            out.println(msg);
            String resp = in.readLine();
            return resp;
        }

        public void stopConnection() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }
    }
