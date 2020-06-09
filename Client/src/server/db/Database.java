package server.db;

import java.sql.*;

public class Database {
    public ResultSet rs;
    private Connection con;

    private static final Database ourInstance = new Database();

    public static Database getInstance() {
        return ourInstance;
    }

    private Database() { //establishes a connection
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "STUDENT", "student1");
            System.out.println("Connected to Users Database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet setResultSet(String query) { //will execute any query
        try {
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public Connection getConnection() {
        return con;
    }
}