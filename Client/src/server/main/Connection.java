package server.main;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Platform;

public class Connection {
	
	public ServerController serverController;
	public UserManager userManager;
	public ServerSocketThread serverSocketThread = new ServerSocketThread();
	public ArrayList<ClientHandler> clientHandlers;
	private boolean flag = true;
	
	public Connection(ServerController serverController, UserManager userManager) {
		this.serverController = serverController;
		this.userManager = userManager;
	}
	
	public void startServer() {
		serverSocketThread.start();
	}
	
	public boolean isStart() {
		return !serverSocketThread.server.isClosed();
	}
	
	public void stopServer() {
		try {
			for (ClientHandler client : clientHandlers) {
				client.dos.write(ServerConstants.SERVER_STOP);
				client.dos.writeUTF("");
				serverController.txtConsole.setDisable(true);
			}	
			Platform.runLater(() -> serverController.listObvMember.clear());
			serverSocketThread.server.close();
		} catch (Exception e) {
			serverController.addStringConsole("Some problem occurred while closing the server", ServerConstants.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally {
			flag = false;
		}
	}
	
	private class ServerSocketThread extends Thread {
		private ServerSocket server;
		
		public void run() {
			try {
	            server = new ServerSocket(Integer.parseInt(serverController.txtPort.getText()));
	            serverController.addStringConsole("Server setup", ServerConstants.SYSTEM_MESSAGE);
	            serverController.addStringConsole("Waiting for client connections on port: "+ serverController.txtPort.getText(), ServerConstants.SYSTEM_MESSAGE);
	            
	            clientHandlers = new ArrayList<>();
	            
	            while(flag)
	            {
	                Socket serverSocket = server.accept();
	                
	                ClientHandler newClientHandler = new ClientHandler(serverSocket,clientHandlers, serverController, userManager);
	                newClientHandler.start();
	                clientHandlers.add(newClientHandler);
	            }
	        } catch (IOException e){
	            e.printStackTrace();
	        }
		}
	}
}
