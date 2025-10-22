package com.kaayakarpam.patient.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/patientDashboardStats")
public class PatientDashBoardStats extends HttpServlet{
       
       protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
               
               PrintWriter writer = response.getWriter();
              response.setContentType("application/json");
              HttpSession session = request.getSession(false);
              
              if (session == null || session.getAttribute("user_id") == null) {
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  writer.write("{\"error\": \"No active session. Please login again.\"}");
                  return;
              } 
              
              int userId = (Integer) session.getAttribute("user_id");
              
              try(Connection connection = DatabaseConnection.getConnection()){
                     int upcomingAppointments = getUpcomingAppointments(connection, userId);
                     
                      writer.write("{\"upComingAppointments\": " + upcomingAppointments + "}");
              }
              catch(Exception e){
                  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
              }
       }
       
       private int getUpcomingAppointments(Connection connection, int userId) throws SQLException{
              try (PreparedStatement totalUpcomingBookingStatement = connection.prepareStatement(SQLQueries.TOTAL_UPCOMING_BOOKINGS)) {
                   totalUpcomingBookingStatement.setInt(1, userId);
                  try (ResultSet totalUpcomingBookingResultSet = totalUpcomingBookingStatement.executeQuery()) {
                      return totalUpcomingBookingResultSet.next() ? totalUpcomingBookingResultSet.getInt(1) : 0;
                  }
              }
       }
}
