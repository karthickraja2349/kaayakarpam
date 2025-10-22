package com.kaayakarpam.patient.controller;

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

@WebServlet("/patientHistory")
public class PatientHistory extends HttpServlet{
          
          protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
                   
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
                
                HttpSession session = request.getSession(false);
                
                  if (session == null || session.getAttribute("user_id") == null) {
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          writer.write("{\"error\":\"No active session. Please login again.\"}");
                          return;
                   }
                   
                   int userId = (Integer) session.getAttribute("user_id");
                   
                   Connection connection = null;
                   try{
                       connection = DatabaseConnection.getConnection();
                       PreparedStatement historyStatement = connection.prepareStatement(SQLQueries.PATIENT_BOOKING_HISTORY);
                       historyStatement.setInt(1, userId);
                       ResultSet historyResultSet = historyStatement.executeQuery();
                       
                       JSONArray requestArray = new JSONArray();
                       
                       while(historyResultSet.next()){
                              JSONObject requestObj = new JSONObject();
                              requestObj.put("hospitalName", historyResultSet.getString("hospital_name"));
                              requestObj.put("hospitalAddress", historyResultSet.getString("address"));
                              requestObj.put("hospitalPhoneNumber", historyResultSet.getString("phone_number"));
                              requestObj.put("bookedDate", historyResultSet.getString("booked_date"));
                              requestObj.put("scanType", historyResultSet.getString("scan_type_name"));
                              
                              if(historyResultSet.getInt("status") == 0)
                                    requestObj.put("status", "Success");
                               else
                                   requestObj.put("status", "Cancelled");
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
}
