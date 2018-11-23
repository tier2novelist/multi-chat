package edu.gwu.cs6431.multichat.core.server.session;


import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;

public interface SessionListener {
    void onSessionOpened(Session session);
    void onSessionClosed(Session session);
    void onMessageReceived(Session session, ClientMessage message);
}
