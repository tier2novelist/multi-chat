package edu.gwu.cs6431.multichat.core.protocol;

import java.util.ResourceBundle;

public final class ProtocolProps {
    static {
        ResourceBundle rb = ResourceBundle.getBundle("protocol");
        SERVER_IP_ADDR = rb.getString("SERVER_IP_ADDR");
        SERVER_PORT = Integer.parseInt(rb.getString("SERVER_PORT"));

        TEXT_CONTENT = rb.getString("TEXT_CONTENT");
        HEADER_FIELD_SEPARATOR = rb.getString("HEADER_FIELD_SEPARATOR");
        LINE_SEPARATOR = rb.getString("LINE_SEPARATOR");
    }

    public static final String SERVER_IP_ADDR;
    public static final int SERVER_PORT;

    public static final String TEXT_CONTENT;
    public static final String HEADER_FIELD_SEPARATOR;
    public static final String LINE_SEPARATOR;
}
