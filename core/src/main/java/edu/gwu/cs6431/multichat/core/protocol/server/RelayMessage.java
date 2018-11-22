package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;

import java.io.File;

public class RelayMessage implements ServerMessage {

    @HeaderField(name = "Message-Type")
    private MessageType type;

    @HeaderField(name = "To", required = false)
    private Integer to;

    @HeaderField(name = "From", inherited = false)
    private Integer from;

    @HeaderField(name = "Sender-Nickname", required = false, inherited = false)
    private String nickname;

    @HeaderField(name = "File-ID", required = false, inherited = false)
    private Integer fileId;

    @HeaderField(name = "Content-Type")
    private String contentType;

    @HeaderField(name = "Content-Length")
    private Integer contentLength;

    @Payload
    private String textPayload;

    @Payload(binary = true)
    private File filePayload;


    @Override
    public RelayMessage generateFrom(ClientMessage clientMessage) {
        return null;
    }
}
