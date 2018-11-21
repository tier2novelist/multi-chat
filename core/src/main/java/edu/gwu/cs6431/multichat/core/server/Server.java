package edu.gwu.cs6431.multichat.core.server;

import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.server.session.SessionListener;

public interface Server extends SessionListener {
    String IP_ADDR = ProtocolProps.SERVER_IP_ADDR;
    int PORT = ProtocolProps.SERVER_PORT;

    void start();
    void stop();
}
