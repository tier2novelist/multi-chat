package edu.gwu.cs6431.multichat.demo.client.component.vo;

import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * View object of chat message
 */
public class ChatMessageVO extends Label {

    public ChatMessageVO(ClientMessage message) {
        if(ProtocolProps.TEXT_CONTENT.equals(message.getContentType())) {
            setText(new String(message.getPayload()));
        } else {
            setText("You shared a file");
        }
        setAlignment(Pos.CENTER_RIGHT);
    }

    public ChatMessageVO(RelayMessage message) {
        if(ProtocolProps.TEXT_CONTENT.equals(message.getContentType())) {
            setText(new String(message.getPayload()));
        }
        setAlignment(Pos.CENTER_LEFT);
    }
}
