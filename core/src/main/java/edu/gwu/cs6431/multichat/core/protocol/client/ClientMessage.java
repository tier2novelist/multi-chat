package edu.gwu.cs6431.multichat.core.protocol.client;

import edu.gwu.cs6431.multichat.core.protocol.Payload;

import java.io.File;

public class ClientMessage {

    @HeaderField(name = "Message-Type")
    private MessageType type;

    @HeaderField(name = "Message-ID")
    private Integer id;

    @HeaderField(name = "To", required = false)
    private Integer to;

    @HeaderField(name = "Content-Type", required = false)
    private String contentType;

    @HeaderField(name = "Content-Length", required = false)
    private Integer contentLength;

    @Payload
    private String textPayload;

    @Payload(binary = true)
    private File filePayload;

    public ClientMessage() {

    }
}
