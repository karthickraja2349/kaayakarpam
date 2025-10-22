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

@WebServlet("/accountDelete")
public class AccountDeletion extends HttpServlet{
           
           protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException{
                       
                       PrintWriter writer  = response.getWriter();
                       response.setContentType("application/json");
                       
                       HttpSession session = request.getSession(false);
                       
                        if (session == null || session.getAttribute("user_id") == null) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            writer.write("{\"error\": \"No active session. Please login again.\"}");
                            return;
                        }
                        
                        int userId  = Integer.parseInt((String)session.getAttribute("user_id"));
                        
                        Connection connection = null;
                        try{
                             connection = DatabaseConnection.getConnection();
                             PreparedStatement deleteAccountStatement = connection.prepareStatement(SQLQueries.DELETE_ACCOUNT);
                             deleteAccountStatement.setInt(1, userId);
                             int rowsAffected = deleteAccountStatement.executeUpdate();
                             
                             if(rowsAffected > 0){
                               writer.write("{\"error\": \"Account Deleted Successfully, ThankYou.\"}");
                               response.sendRedirect("/kaayakarpam/index.html");
                             }
                             else
                                  writer.write("{\"error\": \"Sorry, Try Again Later.\"}");
                        }
                        catch(Exception e){
                               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                              writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
                        }
                        finally{
                             try{
                                 if(connection != null)
                                     connection.close();
                             }
                             catch(Exception e){
                                   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                  writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");  
                             }
                        }      
           }
}
