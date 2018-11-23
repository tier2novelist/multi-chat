package edu.gwu.cs6431.multichat.core.server;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.server.service.SessionService;
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
    public void onSessionOpened(Session session) {
        SessionService.getInstance().addSession(session);
    }

    @Override
    public void onSessionClosed(Session session) {
        SessionService.getInstance().removeSession(session);
    }

    @Override
    public void onMessageReceived(Session session, ClientMessage message) {
        switch (message.getType()) {
            case "chat":
                if(message.getTo() != null) {
                    // private chat
                } else {
                    // public chat
                }
                break;
            case "query":
//                session.write();
                break;
            case "fetch":
                // file id
                message.getPayload();
//                session.write();
                break;
            case "nickname":
                session.setNickname(new String(message.getPayload()));
                break;
            case "bye":
                session.close();
                break;
        }
    }

    public static void main(String[] args) {
        Server server = new ChatServer();
        server.start();
    }
}
