package edu.gwu.cs6431.multichat.core.server.service;

import edu.gwu.cs6431.multichat.core.server.session.Session;

import java.util.LinkedList;
import java.util.List;

public class SessionService {

    private static SessionService ourInstance = new SessionService();

    public static SessionService getInstance() {
        return ourInstance;
    }

    private SessionService() {
        this.sessions = new LinkedList<>();
    }

    private List<Session> sessions;

    private int sessionCounter;

    public void addSession(Session session) {
        synchronized (this) {
            session.setId(this.sessionCounter);
            this.sessionCounter++;
        }
        this.sessions.add(session);
    }

    public void removeSession(Session session) {
        this.sessions.remove(session);
    }

    public List<Session> listSession() {
        return this.sessions;
    }

    public Session findSessionById(int id) {
        return this.sessions.stream().filter(session -> id == session.getId()).findAny().orElse(null);
    }

}
