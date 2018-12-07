package edu.gwu.cs6431.multichat.core.server.service;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.core.server.exception.SessionNotExistException;
import edu.gwu.cs6431.multichat.core.server.session.Session;

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

        Session target = SessionService.getInstance().findSessionById(relayMessage.getTo());
        if(target == null) {
            responseMessage.setStatus(ResponseStatus.ERROR);
            source.write(responseMessage);
            throw new SessionNotExistException(relayMessage.getTo());
        } else {
            if(!"text".equals(message.getContentType())) {
                synchronized (this) {
                    this.shareFileList.add(message);
                    relayMessage.setFileId(shareFileList.size() - 1);
                }
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
        if(!"text".equals(message.getContentType())) {
            synchronized (this) {
                this.shareFileList.add(message);
                relayMessage.setFileId(shareFileList.size() - 1);
            }
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
                sb.append(System.lineSeparator());
            }
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);
        responseMessage.setStatus(ResponseStatus.OK);
        responseMessage.setContentType("text");
        responseMessage.setPayload(sb.toString().getBytes());
        responseMessage.setContentLength(responseMessage.getPayload().length);

        source.write(responseMessage);
    }

    public void fetch(Session source, ClientMessage message) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.from(message);

        // get file id from payload
        int fileId = Integer.parseInt(new String(message.getPayload()));
        if(fileId < shareFileList.size()) {
            ClientMessage stagedMesssage = shareFileList.get(fileId);
            responseMessage.setContentType(stagedMesssage.getContentType());
            responseMessage.setContentLength(stagedMesssage.getContentLength());
            responseMessage.setPayload(stagedMesssage.getPayload());
            responseMessage.setStatus(ResponseStatus.OK);
        } else {
            // invalid file id
            responseMessage.setStatus(ResponseStatus.ERROR);
        }
        source.write(responseMessage);
    }

    public void nickname(Session source, ClientMessage message) {
        source.setNickname(new String(message.getPayload()));

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