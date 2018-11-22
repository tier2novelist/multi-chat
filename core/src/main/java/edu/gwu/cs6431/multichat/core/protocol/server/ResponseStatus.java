package edu.gwu.cs6431.multichat.core.protocol.server;

public enum ResponseStatus {

    OK("Ok"), ERROR("Error");

    String value;

    ResponseStatus(String val) {
        this.value = val;
    }
}
