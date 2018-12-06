package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.Payload;

public class ResponseMessage implements ServerMessage {

    @HeaderField(name = "Status", inherited = false)
    private ResponseStatus status;

    @HeaderField(name = "Message-Type")
    private String type;

    @HeaderField(name = "Message-ID")
    private Integer id;

    @HeaderField(name = "Content-Type", inherited = false)
    private String contentType;

    @HeaderField(name = "Content-Length", inherited = false)
    private Integer contentLength;

    @Payload
    private byte[] payload;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
