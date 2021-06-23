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
import io.socket.emitter.Emitter;

public class SocketConnection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection() {
        System.out.println("Start connection");
        try {
            // ws://seechange-chat.the-circle.designone.nl:80
            System.out.println("in try");
            clientSocket = IO.socket("http://localhost:3000");
           
            clientSocket.on("message", onNewMessage);
            clientSocket.on("streamUsers", onStreamUsers);
            clientSocket.connect();

            System.out.println(clientSocket.id());

            clientSocket.emit("joinstream", "");
        } catch (URISyntaxException e) {
            System.out.println("dikke error neef");
            //e.printStackTrace();
        }
    }

    public void sendMessage(String msg) throws IOException {
        System.out.println(msg);

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        clientSocket.emit("chatMessage", msg);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String username;
//                    String message;
//                    try {
//                        username = data.getString("username");
//                        message = data.getString("message");
//                    } catch (JSONException e) {
//                        return;
//                    }
//
//                    // add the message to view
//                    addMessage(username, message);
//                }
//            });
        }
    };

    private Emitter.Listener onStreamUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String username;
//                    String message;
//                    try {
//                        username = data.getString("username");
//                        message = data.getString("message");
//                    } catch (JSONException e) {
//                        return;
//                    }
//
//                    // add the message to view
//                    addMessage(username, message);
//                }
//            });
        }
    };

    public void disconnect() throws IOException {
        clientSocket.emit("disconnectUserFromStream", "");
    }

    public void stopConnection() throws IOException {
//            in.close();
//            out.close();
        clientSocket.close();
    }
}