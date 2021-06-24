package com.pedro.utils;

import android.app.Application;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;

import java.net.URISyntaxException;

public class SocketApplication extends Application{

    private Socket mSocket;
    {
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket("http://seechange-chat.the-circle.designone.nl:80", opts);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}