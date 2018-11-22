package edu.gwu.cs6431.multichat.core.server.session;


import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;

public interface SessionListener {
    void onSessionStart(Session session);
    void onSessionFinish(Session session);
    void onMessageReceived(ClientMessage message);
}
