package client.main;

import client.controller.ClientController;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.fxml.*;

import java.io.*;

public class Client extends Application {
    Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoaderClientLogin = new FXMLLoader(getClass().getResource("../res/ClientLogin.fxml"));
            Pane rootClientLogin = fxmlLoaderClientLogin.load();
            Login clientLogin = fxmlLoaderClientLogin.getController();

            Scene loginScene = new Scene(rootClientLogin);
            loginScene.getStylesheets().add(getClass().getResource("../res/application.css").toExternalForm());
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Client");
            primaryStage.show();

            FXMLLoader fxmlLoaderClient = new FXMLLoader(getClass().getResource("../res/Client.fxml"));
            Parent rootClient = fxmlLoaderClient.load();
            ClientController clientController = fxmlLoaderClient.getController();

            connection = new Connection(clientController, clientLogin);
            clientController.setConnection(connection);
            clientLogin.setConnection(connection);

            Scene scene = new Scene(rootClient);
            scene.getStylesheets().add(getClass().getResource("../res/application.css").toExternalForm());

            // Display the login scene
            clientLogin.setChatScene(scene);

            // Add listener for submission on login
            clientLogin.btnLogin.setOnAction(event -> clientLogin.doLogin());

            clientLogin.txtUserName.setOnAction(event -> clientLogin.doLogin());

            clientLogin.txtPassword.setOnAction(event -> clientLogin.doLogin());

            clientLogin.txtUserName.requestFocus();
            clientController.txtSend.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws IOException {
        connection.stopClient();
    }
}

