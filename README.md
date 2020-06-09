# AP_Oranj
Oranj is a little chat application written in JavaFX. It is based on the classic tcp client-server model.
Server side: Provides a UI, together with a control panel for users' manipulation. Makes use of an oracle database (jdbc) where all the credentials are stored. Sensitive data (passwords)
is encrypted via SHA-256. The server is concurrent and creates a thread for each logged client while adding it to the chat room.
Client side: Also provides a UI with a log in scene and a chat window. The user sends a message in xml format which will be processed by the server and, depending on its type, it will be
broadcasted or sent in private. 
