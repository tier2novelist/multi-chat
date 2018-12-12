package edu.gwu.cs6431.multichat.core.client;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.HeaderField;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.core.protocol.server.ServerMessage;
import edu.gwu.cs6431.multichat.core.server.Server;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient implements Client {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean listening;
    private Thread readWorker;
    private int messageCounter;

    private EventListener eventListener;

    private ExecutorService pool = Executors.newCachedThreadPool();


    public ChatClient(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void start() {
        try {
            this.socket = new Socket(Server.IP_ADDR, Server.PORT);
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            this.listen();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void stop() {
        this.listening = false;
        try {
            this.dis.close();
            this.dos.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void chat(String text) {
        if(StringUtils.isEmpty(text)) {
            return;
        }
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.CHAT);
        message.setContentType(ProtocolProps.TEXT_CONTENT);
        message.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(text));
        message.setContentLength(message.getPayload().length);
        this.send(message);
    }

    @Override
    public void chat(File file) throws IOException {
        if(file == null || !file.isFile()) {
            return;
        }
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.CHAT);
        message.setContentType(FilenameUtils.getExtension(file.getName()));
        message.setPayload(FileUtils.readFileToByteArray(file));
        message.setContentLength(message.getPayload().length);
        this.send(message);
    }

    @Override
    public void fetch(int fileId) {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.FETCH);
        message.setContentType(ProtocolProps.TEXT_CONTENT);
        message.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(String.valueOf(fileId)));
        message.setContentLength(message.getPayload().length);
        this.send(message);
    }

    @Override
    public void nickname(String nickname) {
        if(StringUtils.isEmpty(nickname)) {
            return;
        }
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.NICKNAME);
        message.setContentType(ProtocolProps.TEXT_CONTENT);
        message.setPayload(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(nickname));
        message.setContentLength(message.getPayload().length);
        this.send(message);
    }

    @Override
    public void bye() {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.BYE);
        this.send(message);
    }

    @Override
    public void query() {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.QUERY);
        this.send(message);
    }

    private void send(ClientMessage message) {
        synchronized (this) {
            message.setId(messageCounter);
            this.messageCounter++;
        }

        this.eventListener.beforeClientMessageSent(message);

        Runnable task = () -> {
            StringBuilder sb = new StringBuilder();
            byte[] payload = null;

            Field[] fields = ClientMessage.class.getDeclaredFields();
            for(Field field : fields) {
                if(field.isAnnotationPresent(HeaderField.class)) {
                    HeaderField headerField = field.getAnnotation(HeaderField.class);
                    try {
                        PropertyDescriptor prop = new PropertyDescriptor(field.getName(), ClientMessage.class);
                        Object fieldValue = prop.getReadMethod().invoke(message);
                        if(fieldValue != null) {
                            switch (field.getType().getSimpleName()) {
                                case "MessageType":
                                    sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, ((MessageType) fieldValue).name()));
                                    sb.append(ProtocolProps.LINE_SEPARATOR);
                                    break;

                                    default:
                                        sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, fieldValue));
                                        sb.append(ProtocolProps.LINE_SEPARATOR);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(field.isAnnotationPresent(Payload.class)) {
                    try {
                        PropertyDescriptor prop = new PropertyDescriptor(field.getName(), ClientMessage.class);
                        payload = (byte[]) prop.getReadMethod().invoke(message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                sb.append(ProtocolProps.LINE_SEPARATOR);
                this.dos.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(sb.toString()));
                if(payload != null) {
                    this.dos.write(payload);
                }
                this.dos.flush();
                this.eventListener.onClientMessageSent(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        this.pool.submit(task);
    }

    private void listen() {
        this.listening = true;
        this.readWorker = new Thread(() -> {
            while(this.listening) {
                try {
                    Map<String, String> headers = new HashMap<>();
                    String line;
                    while (StringUtils.isNotEmpty(line = this.dis.readLine())) {
                        String[] parts = line.split(ProtocolProps.HEADER_FIELD_SEPARATOR);
                        headers.put(parts[0], parts[1]);
                    }

                    if(headers.isEmpty()) {
                        continue;
                    }

                    ServerMessage serverMessage;
                    if(headers.containsKey("Status")) {
                        serverMessage = new ResponseMessage();
                    } else {
                        serverMessage = new RelayMessage();
                    }

                    Field[] fields = serverMessage.getClass().getDeclaredFields();
                    for(Field field : fields) {
                        if(field.isAnnotationPresent(edu.gwu.cs6431.multichat.core.protocol.server.HeaderField.class)) {
                            edu.gwu.cs6431.multichat.core.protocol.server.HeaderField headerField = field.getAnnotation(edu.gwu.cs6431.multichat.core.protocol.server.HeaderField.class);
                            PropertyDescriptor prop = new PropertyDescriptor(field.getName(), serverMessage.getClass());
                            String fieldValue = headers.get(headerField.name());
                            if(fieldValue == null && headerField.required()) {
                                // TODO required field missing, throw Exception
                            } else if(fieldValue != null) {
                                switch (field.getType().getSimpleName()) {
                                    case "Integer":
                                        prop.getWriteMethod().invoke(serverMessage, Integer.parseInt(fieldValue));
                                        break;
                                    case "String":
                                        prop.getWriteMethod().invoke(serverMessage, fieldValue);
                                        break;
                                    case "MessageType":
                                        prop.getWriteMethod().invoke(serverMessage, MessageType.valueOf(fieldValue));
                                        break;
                                    case "ResponseStatus":
                                        prop.getWriteMethod().invoke(serverMessage, ResponseStatus.valueOf(fieldValue));
                                        break;
                                }
                            }
                        }
                    }

                    if(serverMessage instanceof ResponseMessage) {
                        Integer contentLength = ((ResponseMessage) serverMessage).getContentLength();
                        if(contentLength != null && contentLength > 0) {
                            byte[] payloadBuffer = new byte[contentLength];
                            dis.readFully(payloadBuffer,0, payloadBuffer.length);
                            ((ResponseMessage) serverMessage).setPayload(payloadBuffer);
                        }

                        this.eventListener.onResponseMessageReceived((ResponseMessage) serverMessage);

                    } else if(serverMessage instanceof RelayMessage) {
                        if(StringUtils.equals(ProtocolProps.TEXT_CONTENT, ((RelayMessage) serverMessage).getContentType())) {
                            Integer contentLength = ((RelayMessage) serverMessage).getContentLength();
                            if(contentLength != null && contentLength > 0) {
                                byte[] payloadBuffer = new byte[contentLength];
                                dis.readFully(payloadBuffer,0, payloadBuffer.length);
                                ((RelayMessage) serverMessage).setPayload(payloadBuffer);
                            }
                        } else {
                          // Do not read for payload when sharing file, send a FETCH message to retrieve the payload
                        }
                        this.eventListener.onRelayMessageReceived((RelayMessage) serverMessage);

                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                    this.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        this.readWorker.start();
    }
}
