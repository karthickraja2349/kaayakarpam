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
import java.sql.Timestamp;

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@WebServlet("/upcomingScanAppointments")
public class UpcomingScanAppointmentView extends HttpServlet{

              protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
              
                   PrintWriter writer = response.getWriter();
                   response.setContentType("application/json");
                   
                   HttpSession session  = request.getSession(false);
                   if(session == null || session.getAttribute("user_id") == null){
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          response.sendRedirect("index.html");
                          writer.write("{\"error\": \"No active session. Please login again.\"}");
                          return;
                   }
                   
                   int userId = (Integer)session.getAttribute("user_id");
                   int hospitalId = 0;
                   
                   Connection connection = null;
                   try{
                        connection = DatabaseConnection.getConnection();
                        hospitalId = getHospitalId(connection, userId);
                        
                        PreparedStatement upcomingScanStatement = connection.prepareStatement(SQLQueries.VIEW_UPCOMING_SCAN_APPOINTMENTS);
                        upcomingScanStatement.setInt(1, hospitalId);
                        ResultSet upcomingScanResultSet = upcomingScanStatement.executeQuery();
                        
                        JSONArray requesArray = new JSONArray();
                        while(upcomingScanResultSet.next()){
                               JSONObject requestObj = new JSONObject();
                               requestObj.put("bookedId", upcomingScanResultSet.getInt("scan_booking_id"));
                               requestObj.put("patientName", upcomingScanResultSet.getString("patient_name"));
                               requestObj.put("patientAge", upcomingScanResultSet.getInt("age"));
                               requestObj.put("patientGender", upcomingScanResultSet.getString("gender"));
                               requestObj.put("patientMobileNumber", upcomingScanResultSet.getString("mobile_number"));
                               requestObj.put("scanType", upcomingScanResultSet.getString("scan_type_name"));
                               requestObj.put("bookedDate", upcomingScanResultSet.getDate("booked_date").toString());
                               requesArray.put(requestObj);
                        }
                        writer.write(requesArray.toString());
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
              
              protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
              
                    PrintWriter writer = response.getWriter();
                    response.setContentType("application/json");
                    
                    Connection connection = null;
                    try {
                        connection = DatabaseConnection.getConnection();
                        
                        StringBuilder requestBody = new StringBuilder();
                        String line = "";
                        BufferedReader reader = request.getReader();
                        while ((line = reader.readLine()) != null) {
                            requestBody.append(line);
                        }
                        
                        JSONObject requestData = new JSONObject(requestBody.toString());
                        
                        String action = requestData.getString("action");
                        int appointmentId = requestData.getInt("appointmentId"); 
                        
                        if (action.equalsIgnoreCase("reschedule")) {
                            String date = requestData.optString("newDate", null);
                            if (date == null || date.isEmpty()) {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                writer.write("{\"error\": \"Date parameter is required for reschedule\"}");
                                return;
                            }
                            reschedule(connection, appointmentId, date);
                            writer.write("{\"status\":\"success\",\"message\":\"Rescheduled Successfully\"}");
                        } 
                        else if (action.equalsIgnoreCase("cancel")) {
                           cancel(connection, appointmentId);
                            writer.write("{\"status\":\"success\",\"message\":\"Cancelled Successfully\"}");
                        } 
                        else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            writer.write("{\"error\": \"Invalid action specified\"}");
                        }
                    } 
                    catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        writer.write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                    } 
                    finally {
                        try {
                            if (connection != null) {
                                connection.close();
                            }
                        } 
                        catch (Exception e) {
                            System.err.println("Error closing connection: " + e.getMessage());
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
       /*   
        private void reschedule(Connection connection, int appointmentId, String date) throws Exception {
        
               int currentVersion = getAppointmentVersion(connection, appointmentId);
 
                try (PreparedStatement rescheduleStatement = connection.prepareStatement(SQLQueries.RESCHEDULE_SCAN_APPOINTMENT)) {
                    LocalDate localDate = LocalDate.parse(date);
                    LocalDateTime dateTime = localDate.atTime(LocalTime.of(0, 0, 0)); 
                    Timestamp timestamp = Timestamp.valueOf(dateTime);
                   
                    rescheduleStatement.setTimestamp(1, timestamp);
                    rescheduleStatement.setInt(2, appointmentId);
                    rescheduleStatement.setInt(3, currentVersion);
                    
                    int rowsAffected = rescheduleStatement.executeUpdate();
                    
                    if (rowsAffected == 0) {
                            if (isAppointmentCancelled(connection, appointmentId)) 
                                throw new Exception("Appointment was already cancelled by another user");
                            else 
                                throw new Exception("Appointment was modified by another user. Please refresh and try again.");  
                    }
                }
          }
          */
          
          private void reschedule(Connection connection, int appointmentId, String date) throws Exception {
          
                    int currentVersion = getAppointmentVersion(connection, appointmentId);
                    
                    int hospitalId = 0;
                    int scanType = 0;
                    
                    try (PreparedStatement detailStatement = connection.prepareStatement(SQLQueries.GET_BOOKING_DETAILS)) {
                        detailStatement.setInt(1, appointmentId);
                        ResultSet detailResultSet = detailStatement.executeQuery();
                        if (detailResultSet.next()) {
                            hospitalId = detailResultSet.getInt("hospital_id");
                            scanType = detailResultSet.getInt("scan_type");
                        } 
                        else {
                            throw new Exception("Appointment not found");
                        }
                    }
                    
                    int totalAllowedBookings = 0;
                    
                    try (PreparedStatement totalBookingStatement = connection.prepareStatement(SQLQueries.FIXED_SCAN_BOOKING_OF_THE_DAY)) {
                        totalBookingStatement.setInt(1, appointmentId);
                        ResultSet totalBookingResultSet = totalBookingStatement.executeQuery();
                        if (totalBookingResultSet.next()) {
                            totalAllowedBookings = totalBookingResultSet.getInt("total_booking_per_day");
                        } 
                        else {
                            throw new Exception("Scan centre configuration not found");
                        }
                    }
                    
                    int currentBookings = 0;
                    
                    try (PreparedStatement currentCountStatement = connection.prepareStatement(SQLQueries.COUNT_BOOKINGS_FOR_DATE)) {
                        currentCountStatement.setInt(1, hospitalId);
                        currentCountStatement.setInt(2, scanType);
                        currentCountStatement.setString(3, date); 
                        ResultSet currentCountResultSet = currentCountStatement.executeQuery();
                        if (currentCountResultSet.next()) {
                            currentBookings = currentCountResultSet.getInt("current_bookings");
                        }
                    }
                    
                   
                    if (currentBookings >= totalAllowedBookings) {
                        throw new Exception("No available slots for " + date + ". Maximum " + totalAllowedBookings + " bookings per day reached.");
                    }
                    
                 
                    try (PreparedStatement rescheduleStatement = connection.prepareStatement(SQLQueries.RESCHEDULE_SCAN_APPOINTMENT)) {
                        LocalDate localDate = LocalDate.parse(date);
                        LocalDateTime dateTime = localDate.atTime(LocalTime.of(0, 0, 0)); 
                        Timestamp timestamp = Timestamp.valueOf(dateTime);
                       
                        rescheduleStatement.setTimestamp(1, timestamp);
                        rescheduleStatement.setInt(2, appointmentId);
                        rescheduleStatement.setInt(3, currentVersion);
                        
                        int rowsAffected = rescheduleStatement.executeUpdate();
                        
                        if (rowsAffected == 0) {
                            if (isAppointmentCancelled(connection, appointmentId)) {
                                throw new Exception("Appointment was already cancelled by another user");
                            } 
                            else {
                                throw new Exception("Appointment was modified by another user. Please refresh and try again.");  
                            }
                        }
                    }
           }
                          
          private void  cancel(Connection connection, int appointmentId)throws Exception{
                   int currentVersion = getAppointmentVersion(connection, appointmentId);
                   
                  try(PreparedStatement cancelStatement = connection.prepareStatement(SQLQueries.CANCEL_SCAN_APPOINTMENT)){
                         cancelStatement.setInt(1, appointmentId);
                         cancelStatement.setInt(2, currentVersion); 
                         
                         int rowsAffected = cancelStatement.executeUpdate();
                         if (rowsAffected == 0) {
                              if (isAppointmentCancelled(connection, appointmentId)) 
                                  throw new Exception("Appointment was already cancelled by another user");
                              else 
                                  throw new Exception("Appointment was modified by another user. Please refresh and try again.");
                          }
                }
          }
          
          private int getAppointmentVersion(Connection connection, int appointmentId) throws SQLException {
          
                try (PreparedStatement checkversionStatement = connection.prepareStatement(SQLQueries.CHECK_VERSION)) {
                    checkversionStatement.setInt(1, appointmentId);
                    ResultSet  checkVersionResultSet = checkversionStatement.executeQuery();
                    if (checkVersionResultSet.next()) {
                        return checkVersionResultSet.getInt("version");
                    }
                    throw new SQLException("Appointment not found with ID: " + appointmentId);
                }
           }
          
          private boolean isAppointmentCancelled(Connection connection, int appointmentId) throws SQLException {
          
                  try (PreparedStatement cancellationCheckStatement = connection.prepareStatement(SQLQueries.IS_APPOINTMENT_CANCELLED)) {
                      cancellationCheckStatement.setInt(1, appointmentId);
                      ResultSet cancellationCheckResultSet = cancellationCheckStatement.executeQuery();
                      if (cancellationCheckResultSet.next()) {
                          if(cancellationCheckResultSet.getInt("isCancelled")==1)
                                 return true;
                      }
                      return false; 
                  }
          }
          
}
