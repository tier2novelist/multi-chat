package edu.gwu.cs6431.multichat.core.server;

import edu.gwu.cs6431.multichat.core.protocol.ClientMessage;
import edu.gwu.cs6431.multichat.core.server.session.Session;

import java.io.IOException;
import java.net.ServerSocket;


public class ChatServer implements Server {

    private ServerSocket serverSocket;
    private boolean listening;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            listen();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() {
        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void listen() {
        listening = true;

        while (listening) {
            try {
                new Session(serverSocket.accept(), this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onSessionStart(Session session) {

    }

    @Override
    public void onSessionFinish(Session session) {

    }

    @Override
    public void onMessageReceived(ClientMessage message) {

    }
}
