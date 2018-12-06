package edu.gwu.cs6431.multichat.core.server.exception;

public class SessionNotExistException extends Exception {

    private int sessionId;

    public SessionNotExistException(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getMessage() {
        return "Session " + this.sessionId + " Not Exist";
    }
}
