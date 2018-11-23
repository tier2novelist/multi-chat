package edu.gwu.cs6431.multichat.core.server.session;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.HeaderField;
import edu.gwu.cs6431.multichat.core.protocol.server.ServerMessage;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Session {

    private int id;
    private String nickname;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private SessionListener sessionListener;

    private Thread readWorker;
    private boolean alive;

    public void write(ServerMessage serverMessage) {
//        this.dos.write();
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
                    while(!"".equals(line = dis.readLine())) {
                        String[] parts = line.split(" ");
                        headers.put(parts[0], parts[1]);
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
                                }
                            }
                        }
                    }

                    if(clientMessage.getContentLength() != null && clientMessage.getContentLength() > 0) {
                        byte[] contentBuffer = new byte[clientMessage.getContentLength()];
                        dis.read(contentBuffer);
                        clientMessage.setPayload(contentBuffer);
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

        readWorker.start();

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
