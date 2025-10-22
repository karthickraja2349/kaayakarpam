package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import com.kaayakarpam.common.security.PasswordEncrypter;

import org.json.JSONObject;

import java.util.Properties;
import java.util.Random;

import jakarta.mail.Session;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;

@WebServlet("/forgetPassword/*")
public class ForgetPassword extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        if ("/verify".equals(pathInfo)) {
            handleVerification(request, response);
        } 
        else if ("/reset".equals(pathInfo)) {
            handlePasswordReset(request, response);
        } 
        else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("{\"error\": \"Invalid endpoint\"}");
        }
    }
/*    
    private void handleVerification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        Connection connection = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            String role = jsonData.getString("role");
            String emailId = jsonData.getString("mailId");
            String mobileNumber = jsonData.getString("mobileNumber");
            
            // Verify user exists
            if (!verifyUser(connection, role, emailId, mobileNumber)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.write("{\"error\": \"User not found. Please verify your email and mobile number.\"}");
                return;
            }
            
            // Generate and send OTP
            String otp = generateOTP();
            int userId = getUserIdByRole(connection, role, emailId);
            
            // Insert OTP into database
            insertOTP(connection, role.toUpperCase(), userId, otp);
            
            // Send OTP via email
            sendOTPEmail(emailId, otp);
            
            writer.write("{\"success\": \"OTP sent successfully\"}");
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */
    
    private void handleVerification(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter writer = response.getWriter();
    response.setContentType("application/json");
    Connection connection = null;
    
    try {
        connection = DatabaseConnection.getConnection();
        
        StringBuilder requestBody = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        
        JSONObject jsonData = new JSONObject(requestBody.toString());
        String role = jsonData.getString("role");
        String emailId = jsonData.getString("mailId");
        String mobileNumber = jsonData.getString("mobileNumber");
        
        // Verify user exists
        if (!verifyUser(connection, role, emailId, mobileNumber)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write("{\"error\": \"User not found. Please verify your email and mobile number.\"}");
            writer.flush();
            return;
        }
        
        // Generate and send OTP
        String otp = generateOTP();
        int userId = getUserIdByRole(connection, role, emailId);
        
        // Insert OTP into database
        insertOTP(connection, role.toUpperCase(), userId, otp);
        
        // Send immediate response FIRST
        writer.write("{\"success\": \"OTP sent successfully\"}");
        writer.flush();
        
        // Send email in BACKGROUND thread (async)
        new Thread(() -> {
            try {
                sendOTPEmail(emailId, otp);
                System.out.println("DEBUG: Email sent successfully to: " + emailId);
            } catch (Exception e) {
                System.out.println("DEBUG: Failed to send email to " + emailId + ": " + e.getMessage());
                // Log the error but don't affect user experience
            }
        }).start();
        
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        writer.flush();
        e.printStackTrace();
    } finally {
        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    private void handlePasswordReset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        Connection connection = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            String role = jsonData.getString("role");
            String emailId = jsonData.getString("mailId");
            String mobileNumber = jsonData.getString("mobileNumber");
            String otp = jsonData.getString("otp");
            String newPassword = jsonData.getString("newPassword");
            
            // Verify OTP
            int userId = getUserIdByRole(connection, role, emailId);
            if (!verifyOTP(connection, userId, role.toUpperCase(), otp)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writer.write("{\"error\": \"Invalid OTP or OTP expired. Please try again.\"}");
                return;
            }
            
            // Reset password
            if (resetPassword(connection, role, userId, newPassword)) {
                // Delete used OTP
                deleteOTP(connection, userId, role.toUpperCase());
                writer.write("{\"success\": \"Password reset successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"error\": \"Password reset failed. Please try again.\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean verifyUser(Connection connection, String role, String email, String mobile) throws Exception {
        String query;
        switch (role) {
            case "admin":
                query = SQLQueries.VERIFY_ADMIN_FOR_FORGET_PASSWORD;
                break;
            case "patient":
                query = SQLQueries.VERIFY_PATIENT_FOR_FORGET_PASSWORD;
                break;
            case "staff":
                query = SQLQueries.VERIFY_STAFF_FOR_FORGET_PASSWORD;
                break;
            default:
                throw new Exception("Invalid role");
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, mobile);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private boolean verifyOTP(Connection connection, int userId, String role, String otp) throws Exception {
        String query = SQLQueries.VERIFY_OTP;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, role);
            stmt.setString(3, otp);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp generatedAt = rs.getTimestamp("generated_at");
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    
                    // Check if OTP is expired (8 minutes)
                    long diffInMillis = currentTime.getTime() - generatedAt.getTime();
                    long diffInMinutes = diffInMillis / (60 * 1000);
                    
                    if (diffInMinutes > 8) {
                        // Delete expired OTP
                        deleteOTP(connection, userId, role);
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }
    }
    
    private void sendOTPEmail(String email, String otp) throws Exception {
        final String userName = "karthickraja.k@zsgs.in";
        final String password = "82Karthi&kalai";
        
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.zoho.com");
        properties.put("mail.smtp.port", "587");
        
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Reset OTP - Kaayakarpam");
            message.setText("Your OTP for password reset is: " + otp + 
                          "\nThis OTP is valid for 8 minutes." +
                          "\n\nIf you didn't request this, please ignore this email.");
            
            Transport.send(message);
        } catch (MessagingException e) {
            throw new Exception("Failed to send email: " + e.getMessage());
        }
    }
    
    private void deleteOTP(Connection connection, int userId, String role) throws Exception {
        String query = SQLQueries.DELETE_OTP;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, role);
            stmt.executeUpdate();
        }
    }
    
    private String generateOTP() {
        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000);
        return String.valueOf(otpNumber);
    }
    
        private int getUserIdByRole(Connection connection, String role, String mailId)throws Exception{
            int userId = 0;
            String query ="";
            switch(role){
                   case "admin" :
                        query = SQLQueries.GET_ADMIN_ID_BY_MAILID;
                        break;
                   case "staff" :
                        query = SQLQueries.GET_STAFF_ID_BY_MAILID;
                        break;
                    case "patient" :
                         query = SQLQueries.GET_PATIENT_ID_BY_MAILID;
            }
            try(PreparedStatement userIdStatement = connection.prepareStatement(query)){
                    userIdStatement.setString(1, mailId);
                    try(ResultSet userIdResultSet =  userIdStatement.executeQuery()){
                          if(userIdResultSet.next())
                                userId = userIdResultSet.getInt(1);
                    }
            }
            return userId;
    }
    
    private boolean resetPassword(Connection connection, String role, int userId, String password) throws Exception {
        byte[] salt = PasswordEncrypter.generateSalt();
        String encodedSalt = PasswordEncrypter.encodeSalt(salt);
        String hashedPassword = PasswordEncrypter.hashPassword(password, salt);
        
        boolean isSuccess = false;
        String query;
        
        switch (role) {
            case "admin":
                query = SQLQueries.ADMIN_PASSWORD_RESET;
                break;
            case "staff":
                query = SQLQueries.STAFF_PASSWORD_RESET;
                break;
            case "patient":
                query = SQLQueries.PATIENT_PASSWORD_RESET;
                break;
            default:
                throw new Exception("Invalid role for password reset");
        }
        
        try (PreparedStatement updateStatement = connection.prepareStatement(query)) {
            updateStatement.setString(1, hashedPassword);
            updateStatement.setString(2, encodedSalt);
            updateStatement.setInt(3, userId);
            int rowsAffected = updateStatement.executeUpdate();
            isSuccess = rowsAffected > 0;
        }
        
        return isSuccess;
    }
    
      private void insertOTP(Connection connection, String role, int userId , String otp) throws Exception{
          try(PreparedStatement insertOTPStatement = connection.prepareStatement(SQLQueries.INSERT_OTP)){
                insertOTPStatement.setInt(1, userId);
                insertOTPStatement.setString(2, role);
                insertOTPStatement.setString(3, otp);
                insertOTPStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                insertOTPStatement.executeUpdate();
          }
    }

}
/* 
@WebServlet("/forgetPassword")
public class ForgetPassword extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
            
            PrintWriter writer = response.getWriter();
            response.setContentType("application/json");
            
            Connection connection = null;
            try{
                  connection = DatabaseConnection.getConnection();
                  
                  StringBuilder requestBody = new StringBuilder();
                  String line;
                  BufferedReader reader = request.getReader();
                  
                  while( (line = reader.readLine()) != null)
                            requestBody.append(line);
                  
                  JSONObject jsonData = new JSONObject(requestBody.toString());
                  
                  String mailId = jsonData.getString("mailId");
                  String role = jsonData.getString("role");
                  int userId = getUserIdByRole(connection, role.toLowerCase(), mailId);
                  String otp = generateOTP();
                  response.sendRedirect("/kaayakarpam/mail?otp = ${otp}");
                 // writer.write("{\"OTP\": \"Your OTP was " + otp.\"}");
                  insertOTP(connection, role.toUpperCase(), userId, otp);
                  return;
            }
            catch(Exception e){
                   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                   writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
            }
            finally{
                  try{
                       if(connection !=null)
                             connection.close();
                  }
                  catch(Exception e){
                        e.printStackTrace();
                  }
            }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            
 
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            String role = jsonData.getString("role");
            String emailId = jsonData.getString("mailId");
            String mobileNumber = jsonData.getString("mobileNumber");
            String newPassword = jsonData.getString("newPassword");
            
            String query;
            switch (role) {
                case "admin":
                    query = SQLQueries.VERIFY_ADMIN_FOR_FORGET_PASSWORD;
                    break;
                case "patient":
                    query = SQLQueries.VERIFY_PATIENT_FOR_FORGET_PASSWORD;
                    break;
                case "staff":
                    query = SQLQueries.VERIFY_STAFF_FOR_FORGET_PASSWORD;
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writer.write("{\"error\": \"Invalid role specified\"}");
                    return;
            }
            
            PreparedStatement verifyStatement = connection.prepareStatement(query);
            verifyStatement.setString(1, emailId);
            verifyStatement.setString(2, mobileNumber);
            ResultSet verifyResultSet = verifyStatement.executeQuery();
            
            if (verifyResultSet.next()) {
                int userId = verifyResultSet.getInt("id");
                if (resetPassword(connection, role, userId, newPassword)) {
                    writer.write("{\"success\": \"Your password was updated successfully. You can now login with your new password.\"}");
                } 
                else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writer.write("{\"error\": \"Password reset failed. Please try again later.\"}");
                }
            } 
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.write("{\"error\": \"User not found. Please verify your email and mobile number.\"}");
            }
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
          //  e.printStackTrace();
        }
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private int getUserIdByRole(Connection connection, String role, String mailId)throws Exception{
            int userId = 0;
            String query ;
            switch(role){
                   case "admin" :
                        query = SQLQueries.GET_ADMIN_ID_BY_MAILID;
                        break;
                   case "staff" :
                        query = SQLQueries.GET_STAFF_ID_BY_MAILID;
                        break;
                    case "patient" :
                         query = SQLQueries.GET_PATIENT_ID_BY_MAILID;
            }
            try(PreparedStatement userIdStatement = connection.prepareStatement(query)){
                    userIdStatement.setString(1, mailId);
                    try(ResultSet userIdResultSet =  userIdStatement.executeQuery()){
                          if(userIdResultSet.next())
                                userId = userIdResultSet.getInt(1);
                    }
            }
            return userId;
    }
    
    private boolean resetPassword(Connection connection, String role, int userId, String password) throws Exception {
        byte[] salt = PasswordEncrypter.generateSalt();
        String encodedSalt = PasswordEncrypter.encodeSalt(salt);
        String hashedPassword = PasswordEncrypter.hashPassword(password, salt);
        
        boolean isSuccess = false;
        String query;
        
        switch (role) {
            case "admin":
                query = SQLQueries.ADMIN_PASSWORD_RESET;
                break;
            case "staff":
                query = SQLQueries.STAFF_PASSWORD_RESET;
                break;
            case "patient":
                query = SQLQueries.PATIENT_PASSWORD_RESET;
                break;
            default:
                throw new Exception("Invalid role for password reset");
        }
        
        try (PreparedStatement updateStatement = connection.prepareStatement(query)) {
            updateStatement.setString(1, hashedPassword);
            updateStatement.setString(2, encodedSalt);
            updateStatement.setInt(3, userId);
            int rowsAffected = updateStatement.executeUpdate();
            isSuccess = rowsAffected > 0;
        }
        
        return isSuccess;
    }
    
    private void insertOTP(Connection connection, String role, int userId , String otp) throws Exception{
          try(PreparedStatement insertOTPStatement = connection.prepareStatement(SQLQueries.INSERT_OTP)){
                insertOTPStatement.setInt(1, userId);
                insertOTPStatement.setString(2, role);
                insertOTPStatement.setString(3, otp);
                insertOTPStatement.setTimestamp(new Timestamp(System.currentTimeInMillis()));
                insertOTPStatement.executeUpdate();
          }
    }
    
    private String generateOTP() throws Exception{
       String otp = 
       return otp;
    }
}
*/

/*
javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.servlet-api.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/json-20240303.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-9.0.0.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.mail-2.0.1.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.activation-2.0.1.jar"     -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes       com/kaayakarpam/common/db/DatabaseConnection.java com/kaayakarpam/common/db/SQLQueries.java  com/kaayakarpam/auth/controller/RegisterController.java com/kaayakarpam/common/security/PasswordEncrypter.java com/kaayakarpam/auth/controller/ManualLoginController.java com/kaayakarpam/common/service/ProfileService.java com/kaayakarpam/common/service/serviceImplementation/ManualProfileService.java com/kaayakarpam/common/web/AmbulanceRequest.java com/kaayakarpam/common/service/DistanceMeasureable.java com/kaayakarpam/common/service/DurationAnalyser.java com/kaayakarpam/common/service/serviceImplementation/DistanceCalculator.java com/kaayakarpam/common/service/serviceImplementation/DurationCalculator.java com/kaayakarpam/staff/controller/PatientRequests.java com/kaayakarpam/staff/controller/BedAllocation.java com/kaayakarpam/admin/controller/HospitalDelete.java com/kaayakarpam/common/backTask/SessionExpiryListener.java com/kaayakarpam/common/backTask/SessionExpiryFilter.java com/kaayakarpam/common/backTask/AuthenticationFilter.java 
com/kaayakarpam/auth/controller/ForgetPassword.java
*/
