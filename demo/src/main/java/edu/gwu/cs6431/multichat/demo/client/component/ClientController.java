package edu.gwu.cs6431.multichat.demo.client.component;

import edu.gwu.cs6431.multichat.core.client.ChatClient;
import edu.gwu.cs6431.multichat.core.client.Client;
import edu.gwu.cs6431.multichat.core.client.EventListener;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.demo.client.component.vo.ChatMessageVO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class ClientController implements EventListener {

    private Client client;

    @FXML
    private VBox chatMessageVBox;

    @FXML
    private Button shareFileButton;

    @FXML
    private Button sendTextMessageButton;

    @FXML
    private Button setNicknameButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button byeButton;

    @FXML
    private TextField textMessageField;

    @FXML
    private TextField nicknameField;

    private FileChooser fileChooser = new FileChooser();

    {
        this.client = new ChatClient(this);
        this.client.start();
    }

    @FXML
    void handleShareFileButtonClick(MouseEvent mouseEvent) {
        File file = fileChooser.showOpenDialog(this.shareFileButton.getScene().getWindow());
        client.chat(file);
    }

    @FXML
    void handleSendTextButtonClick(MouseEvent mouseEvent) {
        client.chat(textMessageField.getText());
        textMessageField.clear();
    }

    @FXML
    void handleSetNicknameButtonClick(MouseEvent mouseEvent) {
        client.nickname(this.nicknameField.getText());
    }

    @FXML
    void handleRefreshButtonClick(MouseEvent mouseEvent) {
        client.query();
    }

    @FXML
    void handleByeButtonClick(MouseEvent mouseEvent) {
        client.bye();
        // TODO show dialog, then client.close()
    }

    @Override
    public void onResponseMessageReceived(ResponseMessage message) {
        if(ResponseStatus.OK.equals(message.getStatus())) {
            switch (message.getType()) {
                case CHAT:
                    break;
                case QUERY:
                    System.out.println(new String(message.getPayload()));
                    break;
                case FETCH:
                    break;
                case NICKNAME:
                    break;
            }

        } else if(ResponseStatus.ERROR.equals(message.getStatus())) {

        }
    }

    @Override
    public void onRelayMessageReceived(RelayMessage message) {
        if(MessageType.CHAT.equals(message.getType())) {
            Platform.runLater(() -> chatMessageVBox.getChildren().add(new ChatMessageVO(message)));
        }
    }

    @Override
    public void onClientMessageSent(ClientMessage message) {
        if(MessageType.CHAT.equals(message.getType())) {
            Platform.runLater(() -> chatMessageVBox.getChildren().add(new ChatMessageVO(message)));
        }
    }
}
