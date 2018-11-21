package edu.gwu.cs6431.multichat.demo.server;

import edu.gwu.cs6431.multichat.core.server.ChatServer;
import edu.gwu.cs6431.multichat.core.server.Server;

public class DemoServer {

    public DemoServer() {
        server = new ChatServer();
        server.start();
    }

    private Server server;

    public static void main(String[] args) {
        new DemoServer();
    }

}
