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

@WebServlet("/upcomingBookings")
public class UpcomingAppointments extends HttpServlet{
          
          protected void doGet(HttpServletRequest request , HttpServletResponse response)throws IOException{
                
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
                        PreparedStatement upcomingBookingStatement = connection.prepareStatement(SQLQueries.UPCOMING_BOOKINGS);
                        upcomingBookingStatement.setInt(1, userId);
                        ResultSet upcomingBookingResultSet = upcomingBookingStatement.executeQuery();
                        
                        JSONArray requestArray = new JSONArray();
                       
                      while(upcomingBookingResultSet.next()){
                            JSONObject requestObj = new JSONObject();
                            requestObj.put("scan_booking_id", upcomingBookingResultSet.getInt("scan_booking_id"));
                            requestObj.put("hospital_name", upcomingBookingResultSet.getString("hospital_name"));
                            requestObj.put("scan_type_name", upcomingBookingResultSet.getString("scan_type_name"));
                            requestObj.put("booked_date", upcomingBookingResultSet.getTimestamp("booked_date"));
                            requestObj.put("address", upcomingBookingResultSet.getString("address"));
                            requestObj.put("phone_number", upcomingBookingResultSet.getString("phone_number"));
                            
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
                              if(connection !=null)
                                  connection.close();
                         }
                         catch(Exception e){
                             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                             writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                         }
                   }
                   
         }
}         
