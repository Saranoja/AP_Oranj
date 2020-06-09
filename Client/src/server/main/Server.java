package server.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server extends Application {
    Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../res/Server.fxml"));
            Parent root = fxmlLoader.load();
            ServerController svc = fxmlLoader.getController();


            FXMLLoader fxmlLoaderUser = new FXMLLoader(getClass().getResource("../res/Users.fxml"));
            Parent rootUser = fxmlLoaderUser.load();
            UserManager usc = fxmlLoaderUser.getController();
            Scene sceneUser = new Scene(rootUser);

            svc.addStringConsole("Server has started ..", ServerConstants.SYSTEM_MESSAGE);

            connection = new Connection(svc, usc);
            svc.setServerSocketConnection(connection);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../res/application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Chat Server");
            primaryStage.show();

            // Toggle between Chat Server scene and Manage Users scene
            svc.btnManageUser.setOnAction(t -> {
                primaryStage.setScene(sceneUser);
                primaryStage.setTitle("Manage Users");
                primaryStage.show();
            });

            usc.btnBackServer.setOnAction(t -> {
                primaryStage.setScene(scene);
                primaryStage.setTitle("Chat Server");
                primaryStage.show();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        connection.stopServer();
    }
}
