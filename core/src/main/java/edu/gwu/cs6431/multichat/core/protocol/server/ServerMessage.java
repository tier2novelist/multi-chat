package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;

public interface ServerMessage {
    default ServerMessage from(ClientMessage clientMessage) {
//        clientMessage.getContentType()
        // chat
        // file sharing
        return null;
    }
}
