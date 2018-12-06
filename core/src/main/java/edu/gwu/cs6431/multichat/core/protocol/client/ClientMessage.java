package edu.gwu.cs6431.multichat.core.protocol.client;

import edu.gwu.cs6431.multichat.core.protocol.Payload;


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
    private byte[] payload;

    public ClientMessage() {

    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }
}
