package client.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

public class Login {
    @FXML
    Button btnLogin;

    @FXML
    Button btnExit;

    @FXML
    TextField txtUserName;

    @FXML
    PasswordField txtPassword;

    @FXML
    TextField txtPort;

    @FXML
    Label txtMessage;

    private Connection connection;
    private Scene chatScene;

    @FXML
    private void handleButtonActionLogin() {

    }

    @FXML
    private void handleButtonActionExit() {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }

    public void doLogin() {
        // if success, change scene to chat scene
        // if failure, show error, clear all inputs, set cursor to the input username

        String userName = txtUserName.getText().trim();
        String password = txtPassword.getText().trim();

        if(!userName.equals("") && !password.equals("")) {
            connection.setPort(Integer.parseInt(txtPort.getText()));
            txtMessage.setText("Loading...");

            try {
                connection.startClient();
                Platform.runLater(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        connection.sendCredentials(userName, password);
                    } catch (Exception e) {
                        txtMessage.setText("Cannot connect to the server - wrong credentials.\nPlease try again later.");
                        disableInput();
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                txtMessage.setText("Cannot connect to the server - internal error.\nPlease try again later.");
                disableInput();
                e.printStackTrace();
            }
        }
    }

    public void clearAndFocusText() {
        txtUserName.setText("");
        txtPassword.setText("");
        txtUserName.requestFocus();
    }

    public void disableInput() {
        txtUserName.setDisable(true);
        txtPassword.setDisable(true);
        txtPort.setDisable(true);
        btnLogin.setDisable(true);
    }

    public void showChatScene(String username) {
        // called if the login was successful
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.setTitle (username+"'s Chat Room");
        stage.setScene(chatScene);
        stage.show();
    }

    public void setConnection(Connection csc) {
        this.connection = csc;
    }

    public void setChatScene(Scene scene) {
        this.chatScene = scene;
    }
}
