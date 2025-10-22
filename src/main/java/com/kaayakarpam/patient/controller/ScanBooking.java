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
/*
@WebServlet("/bookScan")
public class ScanBooking extends HttpServlet{
          
          protected void doPost(HttpServletRequest request , HttpServletResponse response)throws IOException{
                
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
                     
                     StringBuilder requestBody = new StringBuilder();
                     BufferedReader reader = request.getReader();
                     String line = "";
                     while((line = reader.readLine()) !=null)
                          requestBody.append(line);
                          
                    JSONObject jsonData = new JSONObject(requestBody.toString());
                    
                    int hospitalId = jsonData.getInt("hospitalId");
                    String scanDateStr = jsonData.getString("scanDate");
                    Timestamp bookedDate = Timestamp.valueOf(scanDateStr + " 00:00:00");
                    int scanTypeId = jsonData.getInt("scanTypeId");
                    
                    PreparedStatement checkScanBookingStatement = connection.prepareStatement(SQLQueries.CHECK_SCAN_BOOKING);
                    checkScanBookingStatement.setInt(1, userId);
                    checkScanBookingStatement.setInt(2, hospitalId);
                    checkScanBookingStatement.setTimestamp(3, bookedDate);
                    
                    ResultSet checkScanBookingResultSet = checkScanBookingStatement.executeQuery();
                    if(checkScanBookingResultSet.next()){
                           int count = checkScanBookingResultSet.getInt(1);
                           if(count != 0){
                              writer.write("{\"status\":\"failure\",\"message\":\"Sorry, You already had an Scan appointment in this hospital on that day\"}");
                              return;
                            }
                    }
                     
                    PreparedStatement bookScanStatement = connection.prepareStatement(SQLQueries.BOOK_SCAN);
                    bookScanStatement.setInt(1, userId);
                    bookScanStatement.setInt(2, hospitalId);
                    bookScanStatement.setTimestamp(3, bookedDate);
                    bookScanStatement.setInt(4, scanTypeId);

                    int rowsAffected = bookScanStatement.executeUpdate();
                    
                    if(rowsAffected > 0)
                          writer.write("{\"status\":\"success\",\"message\":\"Scan Centre Booked Successfully\"}");
                    else
                           writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
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
*/
@WebServlet("/bookScan")
public class ScanBooking extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // Start transaction
            
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line = "";
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            int hospitalId = jsonData.getInt("hospitalId");
            String scanDateStr = jsonData.getString("scanDate");
            Timestamp bookedDate = Timestamp.valueOf(scanDateStr + " 00:00:00");
            int scanTypeId = jsonData.getInt("scanTypeId");
            
            PreparedStatement hospitalCheckStatement = connection.prepareStatement(SQLQueries.IS_HOSPITAL_PRESENT);
            hospitalCheckStatement.setInt(1, hospitalId);
            ResultSet hospitalCheckResultSet = hospitalCheckStatement.executeQuery();
            if(hospitalCheckResultSet.next()){
                   writer.write("{\"status\":\"failure\",\"message\":\"Sorry , No Hospital Available\"}");
                 return;
            }
            
            PreparedStatement checkUserBooking = connection.prepareStatement(SQLQueries.CHECK_SCAN_BOOKING);
                /*"SELECT COUNT(*) FROM scan_bookings " +
                "WHERE patient_id = ? AND hospital_id = ? AND booked_date = ?"
            );*/
            checkUserBooking.setInt(1, userId);
            checkUserBooking.setInt(2, hospitalId);
            checkUserBooking.setTimestamp(3, bookedDate);
            
            ResultSet userBookingResult = checkUserBooking.executeQuery();
            if (userBookingResult.next() && userBookingResult.getInt(1) > 0) {
                writer.write("{\"status\":\"failure\",\"message\":\"You already have a scan appointment in this hospital on that day\"}");
                connection.rollback();
                return;
            }
            
            // Check availability with row locking
            PreparedStatement checkAvailability = connection.prepareStatement(SQLQueries.CHECK_SLOT_AVAILABILITY);
            
            checkAvailability.setTimestamp(1, bookedDate);
            checkAvailability.setInt(2, hospitalId);
            checkAvailability.setInt(3, scanTypeId);
            
            ResultSet availabilityResult = checkAvailability.executeQuery();
            
            if (!availabilityResult.next()) {
                writer.write("{\"status\":\"failure\",\"message\":\"Scan type not available in this hospital\"}");
                connection.rollback();
                return;
            }
            
            int bookedCount = availabilityResult.getInt("booked_count");
            int totalCapacity = availabilityResult.getInt("total_booking_per_day");
            
            if (bookedCount >= totalCapacity) {
                writer.write("{\"status\":\"failure\",\"message\":\"No available slots for this scan type on the selected date\"}");
                connection.rollback();
                return;
            }
            
            // Insert the booking
            PreparedStatement bookScanStatement = connection.prepareStatement(SQLQueries.BOOK_SCAN);
            bookScanStatement.setInt(1, userId);
            bookScanStatement.setInt(2, hospitalId);
            bookScanStatement.setTimestamp(3, bookedDate);
            bookScanStatement.setInt(4, scanTypeId);
            
            int rowsAffected = bookScanStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit(); // Commit transaction
                writer.write("{\"status\":\"success\",\"message\":\"Scan Centre Booked Successfully\"}");
            } else {
                connection.rollback();
                writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
            }
            
        } catch (Exception e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                // Log rollback error
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Reset auto-commit
                    connection.close();
                }
            } catch (Exception e) {
                // Log closing error
            }
        }
    }
}
