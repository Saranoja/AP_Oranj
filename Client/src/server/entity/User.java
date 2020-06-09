package server.entity;

import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty username;
    private final SimpleStringProperty password;

    public User(String username, String password) {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    public String getUserName() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

}
