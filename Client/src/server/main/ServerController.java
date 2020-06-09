package server.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import server.entity.Member;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    @FXML
    TextFlow txtConsole;

    @FXML
    ScrollPane txtScrollPane;

    @FXML
    Button btnStart;

    @FXML
    Button btnStop;

    @FXML
    Button btnManageUser;

    @FXML
    ListView<String> lstMember = new ListView<>();

    @FXML
    TableView<Member> tblMemberList;

    @FXML
    TextField txtPort;

    @FXML
    TableColumn<Member, String> tblColName;

    @FXML
    TableColumn<Member, String> tblColIP;

    @FXML
    TableColumn<Member, Integer> tblColPort;

    public ObservableList<Member> listObvMember = FXCollections.observableArrayList();

    private Connection ssc;

    public ServerController() {
    }

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert txtConsole != null;
        assert btnStop != null;
        assert lstMember != null;
        assert txtPort != null;

        txtPort.setText("5000");
        btnStop.setDisable(true);

        //lstMember.setItems(listObvMember);
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tblColIP.setCellValueFactory(new PropertyValueFactory<>("IPAddress"));
        tblColPort.setCellValueFactory(new PropertyValueFactory<>("port"));
        tblMemberList.setItems(listObvMember);

    }

    @FXML
    private void handleButtonActionStart() throws Exception {
        ssc.startServer();
        btnStart.setDisable(true);
        btnStop.setDisable(false);
        txtPort.setDisable(true);
    }

    @FXML
    private void handleButtonActionStop() {
        btnStart.setDisable(true);
        btnStop.setDisable(true);
        //if(ssc.isStart())
        ssc.stopServer();
        addStringConsole("Server has stopped", ServerConstants.SYSTEM_MESSAGE);
    }

    @FXML
    private void handleButtonActionManageUser() {

    }

    public void addStringConsole(String str, int id) {
        Text name;

        Text message;

        final var s = str.substring(str.indexOf(">") + 1) + "\n";
        if (id == ServerConstants.BROADCAST_MESSAGE) {
            name = new Text(str.substring(0, str.indexOf(">") + 1));
            message = new Text(s);

            name.setFill(Color.DARKORANGE);
        } else if (id == ServerConstants.SYSTEM_MESSAGE) {
            name = new Text("");
            message = new Text(str + "\n");

        } else if (id == ServerConstants.PRIVATE_MESSAGE) {
            name = new Text(str.substring(0, str.indexOf(">") + 1));
            message = new Text(s);

            name.setFill(Color.DARKGREY);

        } else if (id == ServerConstants.ERROR_MESSAGE) {
            name = new Text("");
            message = new Text(str + "\n");

            name.setFill(Color.RED);
            message.setFill(Color.RED);

        } else {
            name = new Text("");
            message = new Text(str + "\n");
        }

        Platform.runLater(() -> {
            txtConsole.getChildren().addAll(name, message);
            txtScrollPane.setVvalue(1.0);
        });

        System.out.println(str);
    }

    public void setServerSocketConnection(Connection ssc) {
        this.ssc = ssc;
    }
}