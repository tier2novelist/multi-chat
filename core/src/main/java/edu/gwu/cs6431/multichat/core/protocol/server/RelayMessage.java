package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;


public class RelayMessage implements ServerMessage {

    @HeaderField(name = "Message-Type")
    private String type;

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
    private byte[] payload;

    @Override
    public RelayMessage generateFrom(ClientMessage clientMessage) {

        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
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
