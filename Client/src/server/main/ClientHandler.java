package server.main;

import javafx.application.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import server.entity.Member;

public class ClientHandler extends Thread {
    public Socket serverSocket;
    ArrayList<ClientHandler> clients;

    DataInputStream dis = null;
    DataOutputStream dos = null;
    private final ServerController serverController;
    private final UserManager userManager;

    private String clientName = null;
    private final String IPAddress;
    private final int port;

    ArrayList<String> blockedList;
    private final HashMap<Integer, Member> members = new HashMap<>();

    public ClientHandler(Socket serverSocket, ArrayList<ClientHandler> clients, ServerController serverController, UserManager userManager) {
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.serverController = serverController;
        this.userManager = userManager;
        this.IPAddress = serverSocket.getInetAddress().getHostAddress();
        this.port = serverSocket.getPort();

        serverController.addStringConsole("New client connected from IP: " + IPAddress + " and port: " + port, ServerConstants.SYSTEM_MESSAGE);

        try {
            dis = new DataInputStream(serverSocket.getInputStream());
            dos = new DataOutputStream(serverSocket.getOutputStream());
        } catch (IOException e) {
            serverController.addStringConsole("Cannot get DataStream ..", ServerConstants.ERROR_MESSAGE);
        }
    }

    private boolean isMemberLoggedOn(String username) {
        // check if the member is already in the chat room

        boolean flag = false;
        for (Member member : serverController.listObvMember) {
            if (member.getName().equals(username)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private String getBroadcastMessage(String xmlMessage) {
        // Parse the XML Broadcast message from client
        // Extract the message by regex

        String broadcastMessage = "";
        try {
            DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDoc = docReader.parse(new InputSource(new StringReader(xmlMessage)));

            Element root = xmlDoc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("TEXT");
            for (int i = 0; i < nodes.getLength(); i++) {
                broadcastMessage = ((Element) (nodes.item(i))).getAttribute("text");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broadcastMessage;
    }

    private ArrayList<String> getBlockedList(String xmlMessage) {
        // Parse the XML Broadcast message from client & extract the blocked users

        ArrayList<String> blockedList = new ArrayList<>();
        String name;
        try {
            DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDoc = docReader.parse(new InputSource(new StringReader(xmlMessage)));

            Element root = xmlDoc.getDocumentElement();
            NodeList certainNodes = root.getElementsByTagName("NAME");
            for (int i = 0; i < certainNodes.getLength(); i++) {
                name = ((Element) (certainNodes.item(i))).getAttribute("name");
                blockedList.add(name);
            }
        } catch (Exception ignored) {

        }
        return blockedList;
    }

    public void run() {
        // when a new client connects

        boolean flag = true;

        while (flag) {
            int message;
            String strFromClient;

            try {
                message = dis.read();
                strFromClient = dis.readUTF();

                // take action depending on the received message
                switch (message) {
                    case ServerConstants.BROADCAST_MESSAGE -> {
                        // Parse the XML broadcast message
                        // Send to all members except the blocked ones

                        String broadcastMessage = getBroadcastMessage(strFromClient);
                        blockedList = getBlockedList(strFromClient);

                        String broadMessageWithUser = clientName + "> " + broadcastMessage;
                        serverController.addStringConsole(broadMessageWithUser, ServerConstants.BROADCAST_MESSAGE);
                        for (ClientHandler client : clients) {
                            if (!blockedList.contains(client.clientName)) {
                                client.dos.write(ServerConstants.BROADCAST_MESSAGE);
                                client.dos.writeUTF(broadMessageWithUser);
                            }
                        }
                    }

                    case ServerConstants.LOG_IN -> {
                        // A new user is logging in

                        clientName = strFromClient.split("\\|")[0];
                        String clientPassword = strFromClient.split("\\|")[1];

                        if (userManager.isLoginSuccessful(clientName, clientPassword)) {
                            // Log in success

                            // Check if this user is currently in the chat room (double logged in user)
                            if (!isMemberLoggedOn(clientName)) {
                                // This new member is not in the chat room
                                // Log in as usual

                                // Act this new member that the log in process is successful
                                dos.write(ServerConstants.LOG_IN_SUCCESS);
                                dos.writeUTF(clientName);

                                // Log the console
                                String welcomeNewMember = clientName + " has joined the chat";
                                serverController.addStringConsole(welcomeNewMember, ServerConstants.SYSTEM_MESSAGE);

                                // Create and add this new member to the member list
                                Member member = new Member(clientName, IPAddress, port);
                                members.put(port, member);

                                Platform.runLater(() -> serverController.listObvMember.add(member));

                                // Ack all existing members that we have received a new member
                                for (ClientHandler client : clients) {
                                    client.dos.write(ServerConstants.SYSTEM_MESSAGE);
                                    client.dos.writeUTF(welcomeNewMember);

                                    if (client.clientName.equals(clientName)) {
                                        // Send all current members info to the new member
                                        // So the new member can have full member info in his friend list

                                        for (ClientHandler clientEach : clients) {
                                            client.dos.write(ServerConstants.NEW_MEMBER);
                                            client.dos.writeUTF(clientEach.clientName + "|" + clientEach.IPAddress + "|" + clientEach.port);
                                        }
                                    } else {
                                        // Send only the new member info to the those existing members
                                        // Those existing members already have friend list, so need only the new member info

                                        client.dos.write(ServerConstants.NEW_MEMBER);
                                        client.dos.writeUTF(clientName + "|" + IPAddress + "|" + port);
                                    }
                                }
                            } else {
                                // This new user can't log in because this user is already in the chat room

                                serverController.addStringConsole("Username " + clientName + " logged in failed (already logged on)", ServerConstants.ERROR_MESSAGE);
                                dos.write(ServerConstants.LOG_IN_FAIL);
                                dos.writeUTF("Username is already logged in");
                            }
                        } else {
                            // This new user sends wrong username or password

                            serverController.addStringConsole("Username " + clientName + " logged in failed", ServerConstants.ERROR_MESSAGE);
                            dos.write(ServerConstants.LOG_IN_FAIL);
                            dos.writeUTF("Invalid Username or Password");
                            //break;
                        }
                    }
                    case ServerConstants.LOG_OUT -> {
                        // This user log out or left the chat room

                        String logOutMessage = clientName + " has left the chat";
                        serverController.addStringConsole(logOutMessage, ServerConstants.SYSTEM_MESSAGE);

                        // Remove this member from the member list
                        int logOutPort = Integer.parseInt(strFromClient);
                        Member logOutMember = members.get(logOutPort);
                        Platform.runLater(() -> serverController.listObvMember.remove(logOutMember));
                        members.remove(logOutPort);

                        // Send message to all existing friends
                        for (ClientHandler client : clients) {
                            if (!client.clientName.equals(clientName)) {
                                client.dos.write(ServerConstants.SYSTEM_MESSAGE);
                                client.dos.writeUTF(logOutMessage);

                                //client.dos.write(Constants.SYSTEM_MESSAGE);
                                //client.dos.writeUTF("Member in chatroom: "+memberList.toString());

                                client.dos.write(ServerConstants.LOG_OUT);
                                client.dos.writeUTF(strFromClient);
                            }
                        }

                        // This thread does not need to run anymore
                        // Exit the while(flag) loop
                        flag = false;
                    }

                    case ServerConstants.PRIVATE_MESSAGE -> {
                        // Send private message

                        String targetUser = strFromClient.split("\\|")[0];
                        String privateMessage = strFromClient.split("\\|")[1];
                        privateMessage = "From " + clientName + " to " + targetUser + "> " + privateMessage;
                        serverController.addStringConsole(privateMessage, ServerConstants.PRIVATE_MESSAGE);
                        for (ClientHandler client : clients) {
                            if (client.clientName.equals(targetUser)) {
                                client.dos.write(ServerConstants.PRIVATE_MESSAGE);
                                client.dos.writeUTF(privateMessage);
                            }
                        }
                    }
                    default -> System.out.println("Unknown message.." + strFromClient);
                }
            } catch (IOException e) {
                serverController.addStringConsole("Error in communication with the server.", ServerConstants.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        try {
            clients.remove(this);
        } catch (Exception e) {
            serverController.addStringConsole("Cannot remove client", ServerConstants.ERROR_MESSAGE);
        }
    }
}
