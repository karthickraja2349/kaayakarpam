package com.kaayakarpam.admin.controller;

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

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/ambulanceDelete")
public class DeleteAmbulance extends HttpServlet{

       protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
       
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
                HttpSession session = request.getSession(false);
                
                 Connection connection = null;
                 
                  if (session == null || session.getAttribute("user_id") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        response.sendRedirect("kaayakarpam/index.html");
                        return;
                  }      
                  
                  int hospitalId =Integer.parseInt(request.getParameter("hospitalId"));
                   try{
                        connection = DatabaseConnection.getConnection();
                        PreparedStatement viewAmbulanceStatement = connection.prepareStatement(SQLQueries.VIEW_AMBULANCES);
                        viewAmbulanceStatement.setInt(1, hospitalId);
                        ResultSet viewAmbulanceResultSet = viewAmbulanceStatement.executeQuery();
                        
                        JSONArray requestArray = new JSONArray();
                        while(viewAmbulanceResultSet.next()){
                               JSONObject requestObj = new JSONObject();
                               requestObj.put("vehicleNumber", viewAmbulanceResultSet.getString("vehicle_number"));
                               requestObj.put("currentLocation", viewAmbulanceResultSet.getString("current_location"));
                              requestArray.put(requestObj); 
                        }
                        writer.write(requestArray.toString());
                    }
                     catch(Exception e){
                      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                      writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                  }
                  finally{
                       try{
                           if(connection != null)
                                connection.close();
                       }
                       catch(Exception e){
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                       }
                  }     
         }
         
          protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException{
                
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
                HttpSession session = request.getSession(false);
                
                 Connection connection = null;
                 
                  if (session == null || session.getAttribute("user_id") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        response.sendRedirect("kaayakarpam/index.html");
                        return;
                  }
                  
                  try{
                        connection = DatabaseConnection.getConnection();
                        
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        BufferedReader reader = request.getReader();
                        while ((line = reader.readLine()) != null) 
                            requestBody.append(line);
            
                        JSONObject jsonData = new JSONObject(requestBody.toString());
                        
                        int hospitalId = jsonData.getInt("hospitalId");
                        String ambulanceNumber = jsonData.getString("ambulanceNumber");
                        
                        PreparedStatement deleteAmbulanceStatement = connection.prepareStatement(SQLQueries.DELETE_AMBULANCE);
                        deleteAmbulanceStatement.setString(1, ambulanceNumber);
                        int rowsAffected = deleteAmbulanceStatement.executeUpdate();
                        if(rowsAffected > 0){
                            writer.write("{\"success\": \"Ambulance Deleted Successfully.\"}");
                        }
                        else
                             writer.write("{\"failure\": \"Sorry , Try Again Later.\"}");  
                  }
                  catch(Exception e){
                      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                      writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                  }
                  finally{
                       try{
                           if(connection != null)
                                connection.close();
                       }
                       catch(Exception e){
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                       }
                  }        
       }       
}
