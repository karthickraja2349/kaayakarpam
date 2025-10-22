package com.kaayakarpam.staff.controller;

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

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/ambulanceAdder")
public class AmbulanceAdder extends HttpServlet{
       
        protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException{
               
                 PrintWriter writer  = response.getWriter();
                 response.setContentType("application/json");
                 
                 Connection connection = null;
                 try{
                     connection = DatabaseConnection.getConnection();
                     
                     StringBuilder requestBody = new StringBuilder();
                     BufferedReader reader = request.getReader();
                     String line = "";
                     
                     while((line = reader.readLine()) != null)
                           requestBody.append(line);
                     
                     JSONObject jsonData = new JSONObject(requestBody.toString());
                     
                     int hospitalId = (Integer)jsonData.getInt("hospitalId");
                     String ambulanceNumber = jsonData.getString("ambulanceNumber");
                     String ambulanceLocation = jsonData.getString("ambulanceLocation");
                     double latitude = Double.parseDouble(jsonData.getString("latitude"));
                     double longitude = Double.parseDouble(jsonData.getString("longitude"));
                     
                     PreparedStatement checkHospitalStatement = connection.prepareStatement(SQLQueries.IS_HOSPITAL_DELETED);
                     checkHospitalStatement.setInt(1, hospitalId);
                     ResultSet checkHospitalResultSet = checkHospitalStatement.executeQuery();
                     
                     if(checkHospitalResultSet.next()){
                           writer.write("{\"status\":\"failure\",\"message\":\"Sorry, Hospital Was Already Deleted.You can't able to Add Ambulance to the deleted Hospital.\"}");
                           return;
                     }
                     
                     PreparedStatement checkAmbulanceStatement = connection.prepareStatement(SQLQueries.CHECK_AMBULANCE);
                     checkAmbulanceStatement.setString(1, ambulanceNumber);
                     ResultSet checkAmbulanceResultSet = checkAmbulanceStatement.executeQuery();
                     if(checkAmbulanceResultSet.next()){
                           int isDeleted = checkAmbulanceResultSet.getInt(1);
                           if(isDeleted == 1){
                                  updateAmbulance(connection, hospitalId, ambulanceNumber, ambulanceLocation, latitude, longitude);
                                  writer.write("{\"status\":\"success\",\"message\":\"Ambulance Updated Successfully\"}");
                                  return;
                           }
                     }
                     
                     PreparedStatement addAmbulanceStatement = connection.prepareStatement(SQLQueries.ADD_AMBULANCE);
                     addAmbulanceStatement.setInt(1, hospitalId);
                     addAmbulanceStatement.setString(2, ambulanceNumber);
                     addAmbulanceStatement.setString(3, ambulanceLocation);
                     addAmbulanceStatement.setDouble(4, latitude);
                     addAmbulanceStatement.setDouble(5, longitude);
                     
                     int rowsAffected = addAmbulanceStatement.executeUpdate();
                     
                     if(rowsAffected > 0)
                           writer.write("{\"status\":\"success\",\"message\":\"Ambulance Added Successfully\"}");
                     else
                           writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
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
        
        private void updateAmbulance(Connection connection,int hospitalId, String ambulanceNumber, String ambulanceLocation, double latitude, double longitude)throws Exception{
              try(PreparedStatement updateAmbulanceStatement = connection.prepareStatement(SQLQueries.UPDATE_AMBULANCE)){
                       updateAmbulanceStatement.setInt(1, hospitalId);
                       updateAmbulanceStatement.setString(2, ambulanceLocation);
                       updateAmbulanceStatement.setDouble(3, latitude);
                       updateAmbulanceStatement.setDouble(4, longitude);
                       updateAmbulanceStatement.setString(5, ambulanceNumber);
                       updateAmbulanceStatement.executeUpdate();
              }
        }
}
