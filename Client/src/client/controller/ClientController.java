package client.controller;

import client.entity.Member;
import client.main.Connection;
import client.main.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML
    public Button btnSend;

    @FXML
    public Button btnExit;

    @FXML
    public Button btnSendFile;

    @FXML
    public Label txtName;

    @FXML
    public TextFlow txtConsoleClient;

    @FXML
    public ComboBox<String> comboBoxMember;

    @FXML
    public ScrollPane txtScrollPaneClient;

    @FXML
    public TextField txtSend;

    @FXML
    public TableView<Member> tblMemberList;

    @FXML
    public TableColumn<Member, String> tblColName;

    @FXML
    public TableColumn<Member, String> tblColIP;

    @FXML
    public TableColumn<Member, Integer> tblColPort;

    @FXML
    public TableColumn<Member, Boolean> tblColBlock;

    public ObservableList<Member> membersList = FXCollections.observableArrayList();
    public ObservableList<String> namesList = FXCollections.observableArrayList();
    private Connection connection;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert btnSend != null;

        txtSend.requestFocus();
        txtSend.setOnAction(event -> {
            try {
                performSend(txtSend.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Set the members list
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tblColIP.setCellValueFactory(new PropertyValueFactory<>("IPAddress"));
        tblColPort.setCellValueFactory(new PropertyValueFactory<>("port"));

        // Set the block's check box
        // Value of isBlocked is automatically updated based on check box value
        tblColBlock.setCellValueFactory(new PropertyValueFactory<>("isBlocked"));
        tblColBlock.setCellValueFactory(param -> {
            //System.out.println("Value is : "+param.getValue().isBlockedProperty());
            return param.getValue().isBlockedProperty();
        });
        tblColBlock.setCellFactory(CheckBoxTableCell.forTableColumn(tblColBlock));
        tblColBlock.setEditable(true);
        tblMemberList.setItems(membersList);

        // Set the "Send to" combo box
        namesList.add("All members");
        comboBoxMember.setItems(namesList);
        comboBoxMember.setValue("All members");

    }

    @FXML
    private void handleButtonActionSend() throws Exception {
        performSend(txtSend.getText());
    }

    public void performSend(String message) throws IOException {
        if (!message.trim().equals("")) {
            if (comboBoxMember.getValue() == null || comboBoxMember.getValue().equals("All members")) {
                // Send broadcast message
                this.connection.sendMessage(message);
            } else {
                // Send private message
                String sender = txtName.getText();
                String receiver = comboBoxMember.getValue();
                this.connection.sendMessage(Constants.PRIVATE_MESSAGE, comboBoxMember.getValue() + "|" + message);
                if (!sender.equals(receiver)) {
                    consolePrint("From " + sender + " to " + receiver + "> " + message, Constants.PRIVATE_MESSAGE);
                }
            }
        }
        txtSend.clear();
        txtSend.requestFocus();
    }

    @FXML
    private void handleButtonActionExit() throws Exception {
        connection.stopClient();

        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }

    public void consolePrint(String str, int id) {

        Text name;
        Text message;

        final var substring = str.substring(str.indexOf(">") + 1);
        if (id == Constants.BROADCAST_MESSAGE) {
            name = new Text(str.substring(0, str.indexOf(">") + 1));
            message = new Text(substring + "\n");

            name.setFill(Color.DARKORANGE);

        } else if (id == Constants.SYSTEM_MESSAGE) {
            name = new Text("");
            message = new Text(str + "\n");

        } else if (id == Constants.PRIVATE_MESSAGE) {
            name = new Text(str.substring(0, str.indexOf(">") + 1));
            message = new Text(substring + "\n");

            name.setFill(Color.DARKGREY);

        } else if (id == Constants.ERROR_MESSAGE) {
            name = new Text("");
            message = new Text(str + "\n");

            name.setFill(Color.RED);
            message.setFill(Color.RED);

        } else {
            name = new Text("");
            message = new Text(str + "\n");
        }

        Platform.runLater(() -> {
            txtConsoleClient.getChildren().addAll(name, message);
            txtScrollPaneClient.setVvalue(1.0);
        });

        System.out.println(str);

    }

    public void setConnection(Connection csc) {
        this.connection = csc;
    }

}
