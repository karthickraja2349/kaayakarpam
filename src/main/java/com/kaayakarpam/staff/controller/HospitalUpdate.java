package com.kaayakarpam.staff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/hospitalUpdation")
@MultipartConfig
public class HospitalUpdate extends HttpServlet{
         
           protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                  PrintWriter writer = response.getWriter();
                  response.setContentType("application/json");
                  
                  HttpSession session = request.getSession(false);
                  if(session == null || session.getAttribute("user_id") == null){
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      writer.write("{\"error\": \"No active session. Please login again.\"}");
                      return;
                  }
                  
                  int userId = (Integer)session.getAttribute("user_id");
                  Connection connection = null;
                  try{
                      connection = DatabaseConnection.getConnection();
                      PreparedStatement superiorStaffStatement = connection.prepareStatement(SQLQueries.IS_SUPERIOR_STAFF);
                      superiorStaffStatement.setInt(1, userId);
                      ResultSet superiorStaffResultSet = superiorStaffStatement.executeQuery();
                      
                      if(superiorStaffResultSet.next()){
                           int superiorStaffStatus = superiorStaffResultSet.getInt(1);
                           if(superiorStaffStatus == 1){
                              PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
                              hospitalIdStatement.setInt(1, userId);
                              ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery();
                              
                              if(hospitalIdResultSet.next()){
                                  int hospitalId = hospitalIdResultSet.getInt(1);
                                  response.sendRedirect("src/main/java/com/kaayakarpam/staff/html/updateHospital.html?id=" + hospitalId);
                              } 
                              else {
                                  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                  writer.write("{\"error\": \"Hospital ID not found for this user.\"}");
                              }
                          } 
                          else {
                              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                              response.setContentType("text/html");
                               writer.write("<html><body><h3>You are not authorized to edit hospital status.</h3></body></html>");
                          }
                      } 
                      else {
                          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                          writer.write("{\"error\": \"User data not found.\"}");
                      }
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
                         writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
                      }
                  }
              }
}
