package edu.gwu.cs6431.multichat.core.client;

import edu.gwu.cs6431.multichat.core.protocol.Payload;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.HeaderField;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;
import edu.gwu.cs6431.multichat.core.server.Server;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient implements Client {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean listening;
    private Thread readWorker;
    private int messageCounter;

    private ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void start() {
        try {
            this.socket = new Socket(Server.IP_ADDR, Server.PORT);
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            listen();
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
        message.setContentType("text");
        message.setPayload(text.getBytes());
        message.setContentLength(message.getPayload().length);
        this.send(message);
    }

    @Override
    public void chat(File file) {
        if(file == null || !file.isFile()) {
            return;
        }
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.CHAT);
//        message.setContentType();
//        message.setPayload();
        message.setContentLength(message.getPayload().length);
    }

    @Override
    public File fetch(int fileId) {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.FETCH);
        return null;
    }

    @Override
    public void nickname(String nickname) {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.NICKNAME);
    }

    @Override
    public void bye() {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.BYE);
    }

    @Override
    public void query() {
        ClientMessage message = new ClientMessage();
        message.setType(MessageType.QUERY);
    }

    private void send(ClientMessage message) {
        synchronized (this) {
            message.setId(messageCounter);
            this.messageCounter++;
        }
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
                                    sb.append(headerField.name() + " " + ((MessageType) fieldValue).name());
                                    sb.append(System.lineSeparator());
                                    break;

                                    default:
                                        sb.append(headerField.name() + " " + fieldValue);
                                        sb.append(System.lineSeparator());

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
                sb.append(System.lineSeparator());
                this.dos.writeBytes(sb.toString());
                if(payload != null) {
                    this.dos.write(payload);
                }
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
                    String line;
                    while (StringUtils.isNotEmpty(line = this.dis.readLine())) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        this.readWorker.start();
    }

    public static void main(String[] args) {
        try {
            Client client = new ChatClient();
            client.start();

            String msg = "How are you?";
            client.chat(msg);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
