/*
package com.kaayakarpam.common.db;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection{

        private static Connection connection;
        
        private static final String DB_URL = "jdbc:mysql://localhost:3331/success_test";
        private static final String DB_USERNAME = "main";
        private static final String DB_PASSWORD = "zoho";
         
        private DatabaseConnection(){
        
        }
        
        public static Connection getConnection(){
             if (connection == null) {
                  try {
                      Class.forName("com.mysql.cj.jdbc.Driver");
                      connection = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
                     System.out.println("jdbc was connected successfully");
                 } 
                 catch (SQLException | ClassNotFoundException e) {
                    System.out.println("Failed to register JDBC driver: " + e.getMessage());
                    e.printStackTrace();
                }
           }
           return connection;
        }
        
           public static void closeConnection(){
               if(connection!=null){
                  try{
                    connection.close();                                 
                    connection = null;                              
                 }
                 catch(SQLException e){
                   System.out.println("Failed to close connection");
                 }
             }
        }
    
         public static PreparedStatement getPreparedStatement(String query) {
                PreparedStatement statement = null;
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    statement = connection.prepareStatement(query);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                return statement;
          }
   
           public static void main(String[]args){
           //       getConnection();
                   try {
            
           // Connection dbConnection = DatabaseConnection.getConnection();
            Connection connection = getConnection();

            // First, update
          String updateQuery = "UPDATE celebration SET message = 'yes Success' WHERE id = 1";
          PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
          updateStmt.executeUpdate();
          updateStmt.close();

          // Then, select to verify
          String selectQuery = "SELECT message FROM celebration WHERE id = 1";
          PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
          ResultSet resultSet = selectStmt.executeQuery();

          while (resultSet.next()) {
              System.out.println(resultSet.getString("message"));
          }

          resultSet.close();
          selectStmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
           }
}




javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-java.jar"     -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes     com/kaayakarpam/common/db/DatabaseConnection.java 

karthick-ts476@karthick-ts476:~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/src/main/java$ java -cp ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-java-9.0.0.jar \
com.kaayakarpam.common.db.DatabaseConnection
jdbc was connected successfully

*/

 package com.kaayakarpam.common.db;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DatabaseConnection {

    private static DataSource dataSource; 

    static {
        try {
           // DataSource from Tomcat's JNDI context
            InitialContext context = new InitialContext();
            // The name "java:comp/env/" is the standard JNDI context for Tomcat components
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/MyClusterDB");
            System.out.println("SUCCESS: HikariCP DataSource found via JNDI.");

        } catch (NamingException e) {
            System.err.println("FAILED to find JNDI DataSource: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError("Database JNDI Resource not configured. Check Tomcat's context.xml.");
        }
    }

    private DatabaseConnection() {
       
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
        
    public static PreparedStatement getPreparedStatement(String query) throws SQLException {
        Connection connection = getConnection();
        return connection.prepareStatement(query);
    }
    
    public static PreparedStatement getPreparedStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return getConnection().prepareStatement(sql, autoGeneratedKeys);
}
   
    
    public static void main(String[] args) {
        System.out.println("Testing DatabaseConnection...");
        
        try (Connection connection = getConnection()) {
            System.out.println("SUCCESS: Got a connection from the HikariCP pool!");

          /* 
            String updateQuery = "UPDATE celebration SET message = 'HikariCP Success!' WHERE id = 1";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                int rowsUpdated = updateStmt.executeUpdate();
                System.out.println("Rows updated: " + rowsUpdated);
            }

 
            String selectQuery = "SELECT message FROM celebration WHERE id = 1";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStmt.executeQuery()) {

                while (resultSet.next()) {
                    System.out.println("Message from DB: " + resultSet.getString("message"));
                }
            }
            */
            
        } catch (SQLException e) {
            System.err.println("Database error occurred:");
            e.printStackTrace();
        } catch (ExceptionInInitializerError e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
        
        System.out.println("Test finished.");
    }
}

/*
javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-java.jar:\      
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/HikariCP-5.0.1.jar:\
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/slf4j-api-2.0.7.jar:\
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/tomcat-juli.jar" -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes com/kaayakarpam/common/db/DatabaseConnection.java

//run command not works in terminal it needs tomcat environment to execute

java -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes:\
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-java.jar:\
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/HikariCP-5.0.1.jar:\
/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/slf4j-api-2.0.7.jar:\                                                                                                             /home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/tomcat-juli.jar" com.kaayakarpam.common.db.DatabaseConnection


*/
