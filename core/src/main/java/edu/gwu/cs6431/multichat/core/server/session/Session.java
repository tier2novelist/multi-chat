package edu.gwu.cs6431.multichat.core.server.session;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.HeaderField;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.core.protocol.server.ServerMessage;
import edu.gwu.cs6431.multichat.core.server.exception.ClientLostException;
import org.apache.commons.lang3.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Session {

    private int id;
    private String nickname;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private SessionListener sessionListener;

    private Thread readWorker;
    private boolean alive;

    private static ExecutorService pool = Executors.newCachedThreadPool();

    public void write(ServerMessage serverMessage) {
        Runnable writeTask = () -> {

            StringBuilder sb = new StringBuilder();
            byte[] payload = null;

            Field[] fields = serverMessage.getClass().getDeclaredFields();
            for(Field field : fields) {
                if (field.isAnnotationPresent(edu.gwu.cs6431.multichat.core.protocol.server.HeaderField.class)) {
                    edu.gwu.cs6431.multichat.core.protocol.server.HeaderField headerField = field.getAnnotation(edu.gwu.cs6431.multichat.core.protocol.server.HeaderField.class);
                    try {
                        PropertyDescriptor prop = new PropertyDescriptor(field.getName(), serverMessage.getClass());
                        Object fieldValue = prop.getReadMethod().invoke(serverMessage);
                        if(fieldValue == null && headerField.required()) {
                            // TODO required field missing, throw Exception
                        } else if(fieldValue != null) {
                            switch (field.getType().getSimpleName()) {
                                case "String":
                                    sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, fieldValue));
                                    sb.append(System.lineSeparator());
                                    break;
                                case "Integer":
                                    sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, fieldValue));
                                    sb.append(System.lineSeparator());
                                    break;
                                case "MessageType":
                                    sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, ((MessageType) fieldValue).name()));
                                    sb.append(System.lineSeparator());
                                    break;
                                case "ResponseStatus":
                                    sb.append(StringUtils.join(headerField.name(), ProtocolProps.HEADER_FIELD_SEPARATOR, ((ResponseStatus) fieldValue).name()));
                                    sb.append(System.lineSeparator());
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(field.isAnnotationPresent(Payload.class)) {
                    try {
                        PropertyDescriptor prop = new PropertyDescriptor(field.getName(), serverMessage.getClass());
                        payload = (byte[]) prop.getReadMethod().invoke(serverMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                sb.append(System.lineSeparator());
                this.dos.writeBytes(sb.toString());
                if(payload != null) {
                    this.dos.write(payload);
                }
            } catch (SocketException e) {
                e.printStackTrace();
                this.sessionListener.onSessionError(this, new ClientLostException(this.id));
            } catch (IOException e) {
                e.printStackTrace();
            }


        };

        pool.submit(writeTask);
    }

    public void close() {
        this.alive = false;
        try {
            this.dis.close();
            this.dos.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.sessionListener.onSessionClosed(this);
        }
    }

    public Session(Socket socket, SessionListener sessionListener) {
        this.socket = socket;
        this.sessionListener = sessionListener;
        this.alive = true;

        try {
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.readWorker = new Thread(() -> {
            try {
                while(alive) {

                    Map<String, String> headers = new HashMap<>();
                    String line;
                    while(StringUtils.isNotEmpty(line = dis.readLine())) {
                        String[] parts = line.split(ProtocolProps.HEADER_FIELD_SEPARATOR);
                        headers.put(parts[0], parts[1]);
                    }

                    if(headers.isEmpty()) {
                       continue;
                    }

                    ClientMessage clientMessage = new ClientMessage();
                    Field[] fields = ClientMessage.class.getDeclaredFields();
                    for(Field field : fields) {
                        if(field.isAnnotationPresent(HeaderField.class)) {
                            HeaderField headerField = field.getAnnotation(HeaderField.class);
                            PropertyDescriptor prop = new PropertyDescriptor(field.getName(), ClientMessage.class);
                            String fieldValue = headers.get(headerField.name());
                            if(fieldValue == null && headerField.required()) {
                                // TODO required field missing, throw Exception
                            } else if(fieldValue != null) {
                                switch (field.getType().getSimpleName()) {
                                    case "Integer":
                                        prop.getWriteMethod().invoke(clientMessage, Integer.parseInt(fieldValue));
                                        break;
                                    case "String":
                                        prop.getWriteMethod().invoke(clientMessage, fieldValue);
                                        break;
                                    case "MessageType":
                                        prop.getWriteMethod().invoke(clientMessage, MessageType.valueOf(fieldValue));
                                        break;
                                }
                            }
                        }
                    }

                    if(clientMessage.getContentLength() != null && clientMessage.getContentLength() > 0) {
                        byte[] payloadBuffer = new byte[clientMessage.getContentLength()];
                        dis.read(payloadBuffer);
                        clientMessage.setPayload(payloadBuffer);
                    }

                    this.sessionListener.onMessageReceived(this, clientMessage);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (IntrospectionException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });

        this.readWorker.start();
        this.sessionListener.onSessionOpened(this);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
