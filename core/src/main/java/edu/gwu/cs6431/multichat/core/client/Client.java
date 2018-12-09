package edu.gwu.cs6431.multichat.core.client;

import java.io.File;

public interface Client {
    void start();
    void stop();
    void chat(String text);
    void chat(File file);
    void fetch(int fileId);
    void nickname(String nickname);
    void bye();
    void query();
}
