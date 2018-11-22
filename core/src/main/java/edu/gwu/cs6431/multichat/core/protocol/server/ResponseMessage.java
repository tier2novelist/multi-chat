package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;

import java.io.File;

public class ResponseMessage implements ServerMessage {

    @HeaderField(name = "Status", inherited = false)
    private ResponseStatus status;

    @HeaderField(name = "Message-Type")
    private MessageType type;

    @HeaderField(name = "Message-ID")
    private Integer id;

    @HeaderField(name = "Content-Type", inherited = false)
    private String contentType;

    @HeaderField(name = "Content-Length", inherited = false)
    private Integer contentLength;

    @Payload
    private String textPayload;

    @Payload(binary = true)
    private File filePayload;

    @Override
    public ResponseMessage generateFrom(ClientMessage clientMessage) {
        return null;
    }
}
