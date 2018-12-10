package edu.gwu.cs6431.multichat.core.server.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Thrown by server when trying to reach a disconnected client
 */
public class ClientLostException extends Exception {

    private int sessionId;

    public ClientLostException(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getMessage() {
        return StringUtils.join("Client of session ", this.sessionId, " is lost");
    }
}
