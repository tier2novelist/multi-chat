package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;

public interface ServerMessage {
    ServerMessage generateFrom(ClientMessage clientMessage);
}
