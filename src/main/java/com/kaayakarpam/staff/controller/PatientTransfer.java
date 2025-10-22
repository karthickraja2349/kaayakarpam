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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/patientTransfer")
public class PatientTransfer extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");

        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writer.write("{\"error\": \"No active session. Please login again.\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        int hospitalId = 0;
        double latitude = 0;
        double longitude = 0;
        Connection connection = null;
        try {
             connection = DatabaseConnection.getConnection();
            
            // Get hospital ID for the staff member
            PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
            hospitalIdStatement.setInt(1, userId);
            ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery();
            if (hospitalIdResultSet.next()) {
                hospitalId = hospitalIdResultSet.getInt(1);
            }

            // Get hospital location
            PreparedStatement hospitalLocationStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_GEO_LOCATION);
            hospitalLocationStatement.setInt(1, hospitalId);
            ResultSet hospitalLocationResultSet = hospitalLocationStatement.executeQuery();
            if (hospitalLocationResultSet.next()) {
                latitude = hospitalLocationResultSet.getDouble("latitude");
                longitude = hospitalLocationResultSet.getDouble("longitude");
            }

            JSONArray nearbyHospitals = new JSONArray();
            
            // Find nearby hospitals with available beds
            PreparedStatement nearByHospitalStatement = connection.prepareStatement(SQLQueries.FIND_NEARBY_HOSPITALS);
            nearByHospitalStatement.setDouble(1, latitude);
            nearByHospitalStatement.setDouble(2, longitude);
            nearByHospitalStatement.setDouble(3, latitude);
            nearByHospitalStatement.setInt(4, hospitalId);
            ResultSet nearByHospitalResultSet = nearByHospitalStatement.executeQuery();

            // Process each nearby hospital
            while (nearByHospitalResultSet.next()) {
                int nearbyHospitalId = nearByHospitalResultSet.getInt("hospital_id");
                
                // Get hospital details
                PreparedStatement hospitalDetailStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_BASIC_DETAILS);
                hospitalDetailStatement.setInt(1, nearbyHospitalId);
                ResultSet hospitalDetailResultSet = hospitalDetailStatement.executeQuery();
                
                if (hospitalDetailResultSet.next()) {
                    JSONObject hospital = new JSONObject();
                    hospital.put("hospitalId", nearbyHospitalId);
                    hospital.put("hospitalName", hospitalDetailResultSet.getString("hospital_name"));
                    hospital.put("hospitalAddress", hospitalDetailResultSet.getString("address"));
                    hospital.put("hospitalPhoneNumber", hospitalDetailResultSet.getString("phone_number"));
                    nearbyHospitals.put(hospital);
                }
                hospitalDetailResultSet.close();
                hospitalDetailStatement.close();
            }
            
            writer.write(nearbyHospitals.toString());
        } 
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
        finally{
             try{
                if(connection != null)
                   connection.close();
             }
             catch(Exception e){
                 e.getMessage();
             }
        }
    }

      protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException{
                HttpSession session = request.getSession(false);
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
               
                if (session == null || session.getAttribute("user_id") == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    writer.write("{\"error\": \"No active session. Please login again.\"}");
                    return;
                }
                
                int userId = (Integer) session.getAttribute("user_id");
                int hospitalId = 0;
                int ambulanceRequestId = 0;
                Connection connection = null;
                try{
                        connection = DatabaseConnection.getConnection();
                        
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        BufferedReader reader = request.getReader();
                        while((line = reader.readLine()) !=null)
                             requestBody.append(line);
                       
                       JSONObject jsonData = new JSONObject(requestBody.toString());
                       int requestId = jsonData.getInt("requestId");
                       int destinationHospitalId = jsonData.getInt("destinationHospitalId");
                      String reason = jsonData.getString("reason");
                      String patientCondition = jsonData.optString("patientCondition", "");
                      
                      PreparedStatement isBedAllocatedStatement = connection.prepareStatement(SQLQueries.IS_BED_ALLOCATED);
                      isBedAllocatedStatement.setInt(1, requestId);
                      ResultSet isBedAllocatedResultSet = isBedAllocatedStatement.executeQuery();
                      if(isBedAllocatedResultSet.next()){
                            int value = isBedAllocatedResultSet.getInt(1);
                            if(value == 1){
                              writer.write("{\"error\": \"Sorry , Already Bed was Allocated in your Hospital.\"}");
                              return;
                           }  
                      }
                      
                      PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
                      hospitalIdStatement.setInt(1, userId);
                      ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery();
                      if (hospitalIdResultSet.next()) {
                          hospitalId = hospitalIdResultSet.getInt(1);
                      }
                      
                      PreparedStatement ambulanceRequestIdStatement = connection.prepareStatement(SQLQueries.GET_AMBULANCE_REQUEST_ID);
                      ambulanceRequestIdStatement.setInt(1, requestId);
                      ResultSet ambulanceRequestIdResultSet = ambulanceRequestIdStatement.executeQuery();
                      if(ambulanceRequestIdResultSet.next())
                                ambulanceRequestId = ambulanceRequestIdResultSet.getInt(1);
                      
                      PreparedStatement transferStatement = connection.prepareStatement(SQLQueries.HANDOVER_PATIENT_REQUEST);
                      transferStatement.setInt(1, destinationHospitalId);
                      transferStatement.setInt(2, hospitalId);
                      transferStatement.setInt(3, requestId);
                      transferStatement.executeUpdate();
                      
                      PreparedStatement UpdateStatement = connection.prepareStatement(SQLQueries.ALTER_HOSPITAL_ID_DUE_TO_TRANSFER);
                      UpdateStatement.setInt(1, destinationHospitalId);
                      UpdateStatement.setInt(2, ambulanceRequestId);
                      UpdateStatement.executeUpdate();
                     
                     writer.write("{\"success\": \"Transfer initiated successfully\"}");
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
}

/*
{requestId: 18, destinationHospitalId: "1", reason: "dsx", patientCondition: "fcdx"}
destinationHospitalId
: 
"1"
patientCondition
: 
"fcdx"
reason
: 
"dsx"
requestId
: 
18
*/

