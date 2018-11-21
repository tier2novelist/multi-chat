package edu.gwu.cs6431.multichat.core.server.session;

import java.io.IOException;
import java.net.Socket;

public class Session {

    private int uid;
    private String nickname;
    private Socket socket;
    private SessionListener sessionListener;

    private Thread readWorker;

    public Session(Socket socket, SessionListener sessionListener) {
        this.socket = socket;
        this.sessionListener = sessionListener;
        // TODO generate from util
        this.uid = 0;

        readWorker = new Thread(() -> {
            try {
                this.socket.getInputStream();

                // TODO parse message
                sessionListener.onMessageReceived(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        readWorker.start();
    }
}
