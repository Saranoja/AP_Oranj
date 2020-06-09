package server.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import server.db.Database;
import server.entity.User;
import server.security.Encrypt;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserManager implements Initializable {

    @FXML
    Button btnAddUser;

    @FXML
    Button btnRemoveUser;

    @FXML
    Label txtMessage;

    @FXML
    TextField txtUserName;

    @FXML
    TextField txtPassword;

    @FXML
    Button btnBackServer;

    @FXML
    TableView<User> tblUser;

    @FXML
    TableColumn<User, String> tblColUserName;

    @FXML
    TableColumn<User, String> tblColPassword;

    public ObservableList<User> listUser = FXCollections.observableArrayList();
    public List<User> listU = new ArrayList<>();
    //public File userFile = new File("Users.xml");
    public Database db = Database.getInstance();

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        //get listUser object from file
        try {
            listU.clear();
            ResultSet rs = db.setResultSet("SELECT USERNAME, PASSWORD FROM ORANJ_USERS");
            while (rs.next()) {
                User user = new User(rs.getString(1), rs.getString(2));
                listU.add(user);
                listUser = FXCollections.observableArrayList(listU);
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }

        txtMessage.setText("");
        tblColUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        tblColPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        tblUser.setItems(listUser);

    }

    @FXML
    private void handleButtonActionAdd() throws Exception {
        if (!txtUserName.getText().trim().equals("") && !txtPassword.getText().trim().equals("")) {
            if (!isDuplicateName(txtUserName.getText().trim())) {
                //do add the User to listUser
                User user = new User(txtUserName.getText().trim(), Encrypt.encrypt(txtPassword.getText().trim()));
                listUser.add(user);

                //write listUser to file
                try {

                    ResultSet rs = db.setResultSet("INSERT INTO ORANJ_USERS(USERNAME, PASSWORD) VALUES('" + txtUserName.getText().trim()
                            + "','" + Encrypt.encrypt(txtPassword.getText().trim()) + "')");

                    txtMessage.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                txtMessage.setText("This username is already registered");
            }
        }

        txtUserName.clear();
        txtPassword.clear();
    }

    @FXML
    private void handleButtonActionRemove() {
        //do remove the User from listUser
        listUser.remove(tblUser.getSelectionModel().getSelectedIndex());

        //write listUser to file
        try {
            String username = listUser.get(tblUser.getSelectionModel().getSelectedIndex() % 10).getUserName();
            //System.out.println("Index: " + tblUser.getSelectionModel().getSelectedIndex()%10);
            //System.out.println("User to delete: " + username);
            ResultSet rs = db.setResultSet("DELETE FROM ORANJ_USERS WHERE USERNAME = '" + username + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isLoginSuccessful(String username, String password) {
        //System.out.println("Username = "+username+" , password = "+password);
        ArrayList<User> users = new ArrayList<>(listUser);
        boolean flag = false;
        for (User user : users) {
            if (user.getUserName().equals(username) && user.getPassword().equals(password)) {
                flag = true;
                //break;
            }
        }
        return flag;
    }

    public boolean isDuplicateName(String username) {
        ArrayList<User> users = new ArrayList<>(listUser);
        boolean flag = false;
        for (User user : users) {
            if (user.getUserName().equals(username)) {
                flag = true;
                //break;
            }
        }
        return flag;
    }

    @FXML
    private void handleButtonActionBackServer() {

    }

}