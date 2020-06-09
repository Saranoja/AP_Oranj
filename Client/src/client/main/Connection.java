package client.main;

import client.controller.ClientController;
import client.entity.Member;
import javafx.application.*;
import server.security.Encrypt;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Connection {

    private final ClientController clientController;
    private final Login login;
    public ClientThread clientThread = new ClientThread();
    public Socket client;
    private int port = 5000;

    HashMap<Integer, Member> members = new HashMap<>();

    public Connection(ClientController clientController, Login login) {
        this.clientController = clientController;
        this.login = login;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void startClient() {
        if (!isRunning())
            clientThread.start();
    }

    public void stopClient() throws IOException {
        // When pressing the exit button
        if (isRunning()) {
            sendMessage(Constants.LOG_OUT, String.valueOf(client.getLocalPort()));
        }
        closeSocket();
    }

    public void closeSocket() {
        // Disable all input when no connection (e.g. the server stops)
        clientController.txtSend.setDisable(true);
        clientController.btnSend.setDisable(true);
        clientController.btnSendFile.setDisable(true);
        //client.close();
    }

    public boolean isRunning() {
        return client != null;
    }

    public String createXMLMessage(String message) {
        // The format of broadcast message
        // Including real message, and blocked users list

        StringBuilder xmlMessage = new StringBuilder("<MESSAGE>\n");
        xmlMessage.append("	<TEXT text ='").append(message).append("'/>\n");
        xmlMessage.append("	<BLOCK>\n");
        for (Member eachMember : members.values()) {
            if (eachMember.getName().equals(clientController.txtName.getText()))
                continue;
            if (eachMember.isBlocked())
                xmlMessage.append("		<NAME name ='").append(eachMember.getName()).append("'/>\n");
        }
        xmlMessage.append("	</BLOCK>\n");
        xmlMessage.append("</MESSAGE>\n");
        return xmlMessage.toString();
    }

    public void sendMessage(String textMessage) throws IOException {
        sendMessage(Constants.BROADCAST_MESSAGE, createXMLMessage(textMessage));
    }

    public void sendMessage(int message, String textMessage) throws IOException {
        clientThread.dos.write(message);
        clientThread.dos.writeUTF(textMessage);
    }

    public void sendCredentials(String username, String password) throws IOException, NoSuchAlgorithmException {
        sendMessage(Constants.LOG_IN, username + "|" + Encrypt.encrypt(password));
    }

    private class ClientThread extends Thread {
        DataOutputStream dos;
        DataInputStream dis;

        public void run() {
            try {
                client = new Socket("localhost", port);
                clientController.consolePrint("Chat Room starts on port: " + port, Constants.SYSTEM_MESSAGE);

                dos = new DataOutputStream(client.getOutputStream());
                dis = new DataInputStream(client.getInputStream());

                Thread serverHandler = new Thread(() -> {
                    while (true) {
                        int message;
                        String textMessage;
                        try {
                            message = dis.read();
                            textMessage = dis.readUTF();

                            // if i logged in successfully
                            // if my credentials were wrong
                            // if someone leaves the chatroom
                            // if the server stops, clear the friends list & disable all input and buttons
                            // check blocked users list and send message only to those who are not blocked
                            switch (message) {
                                case Constants.NEW_MEMBER -> {
                                    // when someone new joins the chat
                                    // create a new Member and add it to the map (friends list)

                                    // split the received data
                                    List<String> friendData = new ArrayList<>(Arrays.asList(textMessage.split("\\|")));
                                    Member member = new Member(friendData.get(0), friendData.get(1), Integer.parseInt(friendData.get(2)));
                                    members.put(Integer.parseInt(friendData.get(2)), member);

                                    // add the newcomer
                                    Platform.runLater(() -> {
                                        clientController.membersList.add(member);
                                        clientController.namesList.add(friendData.get(0));
                                    });
                                }
                                case Constants.LOG_IN_SUCCESS -> Platform.runLater(() -> {
                                        clientController.txtName.setText(textMessage);
                                        login.showChatScene(textMessage);
                                });
                                case Constants.LOG_IN_FAIL -> Platform.runLater(() -> {
                                    try {
                                        login.txtMessage.setText(textMessage);
                                        login.clearAndFocusText();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                case Constants.LOG_OUT -> {
                                    int logOutPort = Integer.parseInt(textMessage);
                                    Member logOutMember = members.get(logOutPort);
                                    Platform.runLater(() -> {
                                        clientController.membersList.remove(logOutMember);
                                        clientController.namesList.remove(logOutMember.getName());
                                    });
                                    members.remove(logOutPort);
                                }
                                case Constants.SERVER_STOP -> {
                                    clientController.consolePrint("The server stopped working", Constants.ERROR_MESSAGE);
                                    Platform.runLater(() -> {
                                        clientController.membersList.clear();
                                        clientController.namesList.clear();
                                    });
                                    closeSocket();
                                }
                                case Constants.BROADCAST_MESSAGE -> {
                                    List<String> listStr = new ArrayList<>(Arrays.asList(textMessage.split(">")));
                                    String sender = listStr.get(0);
                                    boolean flag = true;
                                    for (Member member : members.values()) {
                                        if (member.getName().equals(sender) && member.isBlocked()) {
                                            flag = false;
                                        }
                                    }
                                    if (flag)
                                        clientController.consolePrint(textMessage, Constants.BROADCAST_MESSAGE);
                                }
                                case Constants.SYSTEM_MESSAGE -> clientController.consolePrint(textMessage, Constants.SYSTEM_MESSAGE);
                                case Constants.PRIVATE_MESSAGE -> clientController.consolePrint(textMessage, Constants.PRIVATE_MESSAGE);
                                default -> clientController.consolePrint(textMessage + " with code " + message, Constants.SYSTEM_MESSAGE);
                            }

                        } catch (IOException e) {
                            System.out.println("Server down");
                            break;
                        }
                    }
                });
                serverHandler.start();

            } catch (IOException e) {
                clientController.consolePrint("Cannot connect to the server.", Constants.ERROR_MESSAGE);
                clientController.btnSend.setDisable(true);
                clientController.txtSend.setDisable(true);
                clientController.btnSendFile.setDisable(true);
            }
        }
    }
}
