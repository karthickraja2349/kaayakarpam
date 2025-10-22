package com.kaayakarpam.staff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/scanCentre")
public class ScanCentreView extends HttpServlet{
          
          protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
                   
                   PrintWriter writer = response.getWriter();
                   response.setContentType("application/json");
                   
                   HttpSession session  = request.getSession(false);
                   if(session == null || session.getAttribute("user_id") == null){
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          writer.write("{\"error\": \"No active session. Please login again.\"}");
                          return;
                   }
                   
                   int userId = (Integer)session.getAttribute("user_id");
                   int hospitalId = 0;
                   
                   Connection connection = null;
                   try{
                       connection = DatabaseConnection.getConnection();
                       hospitalId = getHospitalId(connection, userId);
                       
                       PreparedStatement scanCentreStatement = connection.prepareStatement(SQLQueries.GET_SCAN_CENTRES_OF_HOSPITAL);
                       scanCentreStatement.setInt(1, hospitalId);
                       
                       ResultSet scanCentreResultSet = scanCentreStatement.executeQuery();
                       JSONArray requestArray = new JSONArray();
                       
                       while(scanCentreResultSet.next()){
                       /*
                             int scanCenterId = scanCentreResultSet.getInt("scan_center_id");
                            String scanTypeName = scanCentreResultSet.getString("scan_type_name");
                            int totalBooking = scanCentreResultSet.getInt("total_booking_per_day");
                            
                            System.out.println("Scan Center ID: " + scanCenterId + 
                                              ", Type: " + scanTypeName + 
                                              ", Bookings: " + totalBooking);
                            
                            */
                             JSONObject requestObj = new JSONObject();
                             requestObj.put("scan_centre_id", scanCentreResultSet.getInt("scan_center_id"));
                             requestObj.put("scan_type_name", scanCentreResultSet.getString("scan_type_name"));
                             requestObj.put("total_booking_per_day", scanCentreResultSet.getInt("total_booking_per_day"));
                             requestArray.put(requestObj);
                       }
                       writer.write(requestArray.toString());
                   }
                   catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                   }
                   finally{
                          try{
                               if(connection != null)
                                   connection.close();
                          }
                          catch(Exception e){
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                          }
                   }
          }
          
          protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException{
                 
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
                        
                        int scanCentreId = jsonData.getInt("scan_centre_id");
                        int totalBookingPerDay = jsonData.getInt("total_booking_per_day");
                        
                        PreparedStatement updateScanCentreStatement = connection.prepareStatement(SQLQueries.UPDATE_SCAN_CENTRE);
                        updateScanCentreStatement.setInt(1, totalBookingPerDay);
                        updateScanCentreStatement.setInt(2, scanCentreId);
                        int rowsAffected = updateScanCentreStatement.executeUpdate();
                        
                        if(rowsAffected > 0)
                               writer.write("{\"status\":\"success\",\"message\":\"Scan Centre Updated Successfully\"}");
                        else
                              writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
                   }
                   catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                   }
                   finally{
                        try{
                             if(connection !=null)
                                 connection.close();
                        }
                        catch(Exception e){
                             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                             writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                        }
                   }
          }
          
          private int getHospitalId(Connection connection , int userId)throws SQLException{
               int hospitalId = 0;
               try(PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID)){
                        hospitalIdStatement.setInt(1, userId);
                        try(ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery()){
                                 if(hospitalIdResultSet.next())
                                     hospitalId = hospitalIdResultSet.getInt(1);
                        }
               }
               return hospitalId;
          }
}
