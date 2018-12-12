package edu.gwu.cs6431.multichat.demo.client.component;

import edu.gwu.cs6431.multichat.core.client.ChatClient;
import edu.gwu.cs6431.multichat.core.client.Client;
import edu.gwu.cs6431.multichat.core.client.EventListener;
import edu.gwu.cs6431.multichat.core.protocol.ProtocolProps;
import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;
import edu.gwu.cs6431.multichat.core.protocol.client.MessageType;
import edu.gwu.cs6431.multichat.core.protocol.server.RelayMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseMessage;
import edu.gwu.cs6431.multichat.core.protocol.server.ResponseStatus;
import edu.gwu.cs6431.multichat.demo.client.component.vo.ChatMessageVO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public class ClientController implements EventListener {

    private Client client;

    @FXML
    private ChoiceBox userChoiceBox;

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

    private DirectoryChooser directoryChooser = new DirectoryChooser();

    {
        this.client = new ChatClient(this);
        this.client.start();
    }

    @FXML
    void handleShareFileButtonClick(MouseEvent mouseEvent) {
        File file = fileChooser.showOpenDialog(this.shareFileButton.getScene().getWindow());
        try {
            client.chat(file);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot read file!");
            alert.showAndWait();
        }
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bye");
        alert.setHeaderText(null);
        alert.setContentText("Shutting down...");
        alert.showAndWait();

        client.stop();
        Platform.exit();
    }

    @Override
    public void onResponseMessageReceived(ResponseMessage message) {
        if(ResponseStatus.OK.equals(message.getStatus())) {
            switch (message.getType()) {
                case CHAT:
                    break;
                case QUERY:
                    if(message.getContentLength() != null && message.getContentLength() > 0) {
                        String userList = new String(message.getPayload());
                        if(StringUtils.isNotEmpty(userList)) {
                            ObservableList<String> users = FXCollections.observableArrayList(userList.split(ProtocolProps.LINE_SEPARATOR));
                            Platform.runLater(() -> {
                                userChoiceBox.getItems().clear();
                                userChoiceBox.getItems().addAll(users);
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            userChoiceBox.getItems().clear();
                        });
                    }
                    break;
                case FETCH:

                    Platform.runLater(() -> {
                        File destFileDir = this.directoryChooser.showDialog(this.shareFileButton.getScene().getWindow());
                        if(destFileDir != null && destFileDir.isDirectory()) {
                            try {
                                String fileName = StringUtils.join(new Date(),".",message.getContentType());
                                File destFile = new File(destFileDir, fileName);
                                FileUtils.writeByteArrayToFile(destFile, message.getPayload());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Error");
                                alert.setHeaderText(null);
                                alert.setContentText("Cannot write file!");
                                alert.showAndWait();
                            }
                        }
                    });
                    break;
                case NICKNAME:
                    break;
            }

        } else if(ResponseStatus.ERROR.equals(message.getStatus())) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(new String(message.getPayload()));
                alert.showAndWait();
            });
        }
    }

    @Override
    public void onRelayMessageReceived(RelayMessage message) {
        if(MessageType.CHAT.equals(message.getType())) {
            Platform.runLater(() -> {
                chatMessageVBox.getChildren().add(new ChatMessageVO(message));

                if(!StringUtils.equals(ProtocolProps.TEXT_CONTENT, message.getContentType())) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Fetch File");
                    alert.setHeaderText(null);
                    alert.setContentText(StringUtils.join("File type: ", message.getContentType(), ProtocolProps.LINE_SEPARATOR, "File size: ", message.getContentLength(), " bytes"));

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        this.client.fetch(message.getFileId());
                    } else {
                        // do nothing if choose cancel
                    }
                }
            });
        }
    }

    @Override
    public void onClientMessageSent(ClientMessage message) {
        if(MessageType.CHAT.equals(message.getType())) {
            Platform.runLater(() -> chatMessageVBox.getChildren().add(new ChatMessageVO(message)));
        }
    }

    @Override
    public void beforeClientMessageSent(ClientMessage message) {
        if(MessageType.CHAT.equals(message.getType())) {
            String to = (String) userChoiceBox.getValue();
            if(StringUtils.isNotEmpty(to)) {
                message.setTo(Integer.parseInt(to));
            }
        }
    }
}
