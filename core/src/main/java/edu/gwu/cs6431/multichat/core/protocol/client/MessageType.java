package edu.gwu.cs6431.multichat.core.protocol.client;

public enum MessageType {

    CHAT("chat"), QUERY("query"), FETCH("fetch"), NICKNAME("nickname"), BYE("bye");

    String value;

    MessageType(String val) {
        this.value = val;
    }
}
