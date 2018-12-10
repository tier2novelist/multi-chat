package edu.gwu.cs6431.multichat.core.client;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;

public interface EventListener {
    void onResponseMessageReceived(ResponseMessage message);
    void onRelayMessageReceived(RelayMessage message);
    void onClientMessageSent(ClientMessage message);
    void beforeClientMessageSent(ClientMessage message);
}
