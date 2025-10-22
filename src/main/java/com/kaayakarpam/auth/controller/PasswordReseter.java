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


import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import com.kaayakarpam.common.security.PasswordEncrypter;

import org.json.JSONObject;

@WebServlet("/passwordReset")
public class PasswordReseter extends HttpServlet{
              
           protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException{
                     
                     HttpSession session = request.getSession(false);
                     PrintWriter writer = response.getWriter();
                     response.setContentType("application/json");
                     
                    if (session == null || session.getAttribute("user_id") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        response.sendRedirect("kaayakarpam/index.html");
                        return;
                   }
                   
                     
                     String role = (String)session.getAttribute("role");
   
                     
                     Connection connection = null;
                     try{
                         connection = DatabaseConnection.getConnection();
                         
                         StringBuilder requestBody = new StringBuilder();
                         String line;
                         BufferedReader reader = request.getReader();
                         while( (line = reader.readLine()) !=null)
                               requestBody.append(line);
                               
                        JSONObject jsonData = new JSONObject(requestBody.toString());
                        String emailId = jsonData.getString("mailId");
                        String password = jsonData.getString("password");
                       String newPassword = jsonData.getString("newPassword");
                        
                         
                         String query ;
                         switch(role){
                              case "admin":
                                  query = SQLQueries.VERIFY_ADMIN;
                                  break;
                              case "patient":
                                  query = SQLQueries.VERIFY_PATIENT;
                                  break;
                              case "staff":
                                  query = SQLQueries.VERIFY_STAFF;
                                  break;
                              default:
                                  writer.write("invalid_role");
                                  return; 
                         }
                         
                         PreparedStatement verifyStatement = connection.prepareStatement(query);
                         verifyStatement.setString(1, emailId);
                         
                         ResultSet verifyResultSet = verifyStatement.executeQuery();
                         
                          if (verifyResultSet.next()) {
                              int userId = verifyResultSet.getInt("id");
                              String storedHashPassword = verifyResultSet.getString("password_hash");
                              String saltedPassword = verifyResultSet.getString("password_salt");
                              String userEmail = verifyResultSet.getString("email");
                              
                              byte[] saltBytes = PasswordEncrypter.decodeSalt(saltedPassword);
                              String hashedPassword = PasswordEncrypter.hashPassword(password, saltBytes);
                              
                              if(hashedPassword.equals(storedHashPassword)){
                                     if(resetPassword(connection, role, userId, newPassword)){
                                        writer.write("{\"error\": \"Password Updated Successfully.\"}");
                                        return;
                                     }
                                     else{
                                          writer.write("{\"error\": \"Sorry Try Again Later.\"}");
                                     }
                              }
                              else{
                                  writer.write("{\"error\": \"Invalid Password.\"}");
                              }
                          }    
                          else{
                                writer.write("{\"error\": \"User Not Found.\"}");
                          }            
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
                                   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                   writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
                             }
                     }
           }
           
           private boolean resetPassword(Connection connection, String role, int userId, String password)throws Exception{
                   
                   byte[] salt = PasswordEncrypter.generateSalt();
                   String encodedSalt = PasswordEncrypter.encodeSalt(salt);
                   String hashedPassword = PasswordEncrypter.hashPassword(password, salt);
                   
                   boolean isSuccess = false;
                   
                   switch(role){
                        case "admin" :
                                try(PreparedStatement updateAdminStatement = connection.prepareStatement(SQLQueries.ADMIN_PASSWORD_RESET)){
                                      updateAdminStatement.setString(1, hashedPassword);
                                      updateAdminStatement.setString(2, encodedSalt);
                                      updateAdminStatement.setInt(3, userId);
                                      int rowsAffected = updateAdminStatement.executeUpdate();
                                      if(rowsAffected > 0)
                                           isSuccess = true;
                                  }         
                                break;
                        case "staff" :
                                try(PreparedStatement updateStaffStatement = connection.prepareStatement(SQLQueries.STAFF_PASSWORD_RESET)){
                                      updateStaffStatement.setString(1, hashedPassword);
                                      updateStaffStatement.setString(2, encodedSalt);
                                      updateStaffStatement.setInt(3, userId);
                                      int rowsAffected = updateStaffStatement.executeUpdate();
                                      if(rowsAffected > 0)
                                           isSuccess = true;
                                 }          
                                break;
                        case "patient" :
                                try(PreparedStatement updatePatientStatement = connection.prepareStatement(SQLQueries.PATIENT_PASSWORD_RESET)){
                                      updatePatientStatement.setString(1, hashedPassword);
                                      updatePatientStatement.setString(2, encodedSalt);
                                      updatePatientStatement.setInt(3, userId);
                                      int rowsAffected = updatePatientStatement.executeUpdate();
                                      if(rowsAffected > 0)
                                           isSuccess = true;
                                }           
                                break;
                   }
                   return isSuccess;
           }
}
