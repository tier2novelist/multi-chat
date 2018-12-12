package edu.gwu.cs6431.multichat.core.server.service;

import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.core.server.exception.SessionNotExistException;
import edu.gwu.cs6431.multichat.core.server.session.Session;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageService {
    private static MessageService ourInstance = new MessageService();

    public static MessageService getInstance() {
        return ourInstance;
    }

    private MessageService() {
        shareFileList = new ArrayList<>();
    }

    private List<ClientMessage> shareFileList;

    public void forward(Session source, ClientMessage message) throws SessionNotExistException {
        RelayMessage relayMessage = new RelayMessage();
        relayMessage.setFrom(source.getId());
        relayMessage.setNickname(source.getNickname());
        relayMessage.from(message);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);
        responseMessage.setStatus(ResponseStatus.OK);
        responseMessage.setContentType(ProtocolProps.TEXT_CONTENT);
        responseMessage.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8("OK"));
        responseMessage.setContentLength(responseMessage.getPayload().length);

        Session target = SessionService.getInstance().findSessionById(relayMessage.getTo());
        if(target == null) {
            SessionNotExistException sessionNotExistException = new SessionNotExistException(relayMessage.getTo());
            responseMessage.setStatus(ResponseStatus.ERROR);
            responseMessage.setContentType(ProtocolProps.TEXT_CONTENT);
            responseMessage.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(sessionNotExistException.getMessage()));
            responseMessage.setContentLength(responseMessage.getPayload().length);
            source.write(responseMessage);
            throw sessionNotExistException;
        } else {
            if(!StringUtils.equals(ProtocolProps.TEXT_CONTENT, message.getContentType())) {
                synchronized (this) {
                    this.shareFileList.add(message);
                    relayMessage.setFileId(shareFileList.size() - 1);
                }
            } else {
                relayMessage.setPayload(message.getPayload());
            }
            target.write(relayMessage);
            source.write(responseMessage);
        }
    }

    public void broadcast(Session source, ClientMessage message) {
        RelayMessage relayMessage = new RelayMessage();
        relayMessage.setFrom(source.getId());
        relayMessage.setNickname(source.getNickname());
        relayMessage.from(message);
        if(!StringUtils.equals(ProtocolProps.TEXT_CONTENT, message.getContentType())) {
            synchronized (this) {
                this.shareFileList.add(message);
                relayMessage.setFileId(shareFileList.size() - 1);
            }
        } else {
            relayMessage.setPayload(message.getPayload());
        }

        List<Session> sessions = SessionService.getInstance().listSession();
        Iterator<Session> iterator = sessions.iterator();
        while(iterator.hasNext()) {
            Session current = iterator.next();
            if(current != source) {
                current.write(relayMessage);
            }
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);
        responseMessage.setStatus(ResponseStatus.OK);
        responseMessage.setContentType(ProtocolProps.TEXT_CONTENT);
        responseMessage.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8("OK"));
        responseMessage.setContentLength(responseMessage.getPayload().length);

        source.write(responseMessage);
    }

    public void query(Session source, ClientMessage message) {
        List<Session> sessions = SessionService.getInstance().listSession();
        StringBuilder sb = new StringBuilder();
        Iterator<Session> iterator = sessions.iterator();
        while(iterator.hasNext()) {
            Session current = iterator.next();
            if(current != source) {
                sb.append(current.getId());
                sb.append(ProtocolProps.LINE_SEPARATOR);
            }
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);
        responseMessage.setStatus(ResponseStatus.OK);
        responseMessage.setContentType(ProtocolProps.TEXT_CONTENT);
        responseMessage.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(sb.toString()));
        responseMessage.setContentLength(responseMessage.getPayload().length);

        source.write(responseMessage);
    }

    public void fetch(Session source, ClientMessage message) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);

        // get file id from payload
        int fileId = Integer.parseInt(org.apache.commons.codec.binary.StringUtils.newStringUtf8(message.getPayload()));
        if(fileId < shareFileList.size()) {
            ClientMessage stagedMessage = shareFileList.get(fileId);
            responseMessage.setContentType(stagedMessage.getContentType());
            responseMessage.setContentLength(stagedMessage.getContentLength());
            responseMessage.setPayload(stagedMessage.getPayload());
            responseMessage.setStatus(ResponseStatus.OK);
        } else {
            // invalid file id
            responseMessage.setStatus(ResponseStatus.ERROR);
        }
        source.write(responseMessage);
    }

    public void nickname(Session source, ClientMessage message) {
        source.setNickname(org.apache.commons.codec.binary.StringUtils.newStringUtf8(message.getPayload()));

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);
        responseMessage.setStatus(ResponseStatus.OK);
        responseMessage.setContentType(message.getContentType());
        responseMessage.setPayload(message.getPayload());
        responseMessage.setContentLength(responseMessage.getPayload().length);

        source.write(responseMessage);
    }

    public void bye(Session source, ClientMessage message) {
        source.close();
    }
}
