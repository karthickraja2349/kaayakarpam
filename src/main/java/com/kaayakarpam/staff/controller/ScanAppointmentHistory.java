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

@WebServlet("/scanAppointmentHistory")
public class ScanAppointmentHistory extends HttpServlet{

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
                        
                        PreparedStatement scanAppointmentHistoryStatement = connection.prepareStatement(SQLQueries.SCAN_APPOINTMENT_HISTORY);
                        scanAppointmentHistoryStatement.setInt(1, hospitalId);
                        ResultSet scanAppointmentResultSet = scanAppointmentHistoryStatement.executeQuery();
                        
                        JSONArray requestArray = new JSONArray();
                        while(scanAppointmentResultSet.next()){
                               JSONObject requestObj = new JSONObject();
                               requestObj.put("bookedId", scanAppointmentResultSet.getInt("scan_booking_id"));
                               requestObj.put("patientName", scanAppointmentResultSet.getString("patient_name"));
                               requestObj.put("patientAge", scanAppointmentResultSet.getInt("age"));
                               requestObj.put("patientGender", scanAppointmentResultSet.getString("gender"));
                               requestObj.put("patientMobileNumber", scanAppointmentResultSet.getString("mobile_number"));
                               requestObj.put("scanType", scanAppointmentResultSet.getString("scan_type_name"));
                               requestObj.put("bookedDate", scanAppointmentResultSet.getDate("booked_date").toString());
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
