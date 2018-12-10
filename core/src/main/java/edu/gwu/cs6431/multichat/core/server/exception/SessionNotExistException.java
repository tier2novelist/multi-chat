package edu.gwu.cs6431.multichat.core.server.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Thrown when trying to reach a non-existing session by id
 */
public class SessionNotExistException extends Exception {

    private int sessionId;

    public SessionNotExistException(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getMessage() {
        return StringUtils.join("Session ", this.sessionId, " Not Exist");
    }
}
