package com.kaayakarpam.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/adminDashboardStats")
public class AdminDashBoardStats extends HttpServlet{
          
          protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
              
              PrintWriter writer = response.getWriter();
              response.setContentType("application/json");
              HttpSession session = request.getSession(false);
              
              if (session == null || session.getAttribute("user_id") == null) {
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  writer.write("{\"error\": \"No active session. Please login again.\"}");
                  return;
              }

             try (Connection connection = DatabaseConnection.getConnection()) {
                   int totalHospitals = findTotalHospitals(connection);
                   int overAllPatientCount = findTotalInpatients(connection);
                   int overAllPendingRequests = findTotalPendingRequests(connection);
                   int overAllScanCentreCount = findTotalScanCentres(connection);
                   int overAllAmbulanceCount  = findTotalAmbulances(connection);
                   
                    writer.write("{\"hospitals\": " + totalHospitals +
                         ", \"patients\": " + overAllPatientCount +
                         ", \"scanCentres\": " + overAllScanCentreCount +
                         ", \"ambulances\": " + overAllAmbulanceCount +
                         ", \"requests\": " + overAllPendingRequests + "}"); 
                   
             }
            catch (Exception e) {
                  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
            }
       }
       
       private int findTotalHospitals(Connection connection) throws SQLException {
    System.out.println("DEBUG: Executing query: " + SQLQueries.TOTAL_HOSPITALS);
    
    try (PreparedStatement totalHospitalStatement = connection.prepareStatement(SQLQueries.TOTAL_HOSPITALS);
         ResultSet totalHospitalResultSet = totalHospitalStatement.executeQuery()) {
        
        int count = totalHospitalResultSet.next() ? totalHospitalResultSet.getInt(1) : 0;
        System.out.println("DEBUG: Query returned: " + count + " hospitals");
        return count;
    }
}
        
        private int findTotalInpatients(Connection connection)throws SQLException{
              try(PreparedStatement totalInPatientStatement = connection.prepareStatement(SQLQueries.OVERALL_INPATIENT_COUNT);
                     ResultSet totalInPatientResultSet = totalInPatientStatement.executeQuery()){
                   return totalInPatientResultSet.next() ? totalInPatientResultSet.getInt(1) : 0;     
              }       
        }
        
        private int findTotalPendingRequests(Connection connection)throws SQLException{
               try(PreparedStatement totalPendingRequestStatement = connection.prepareStatement(SQLQueries.OVERALL_PENDING_REQUESTS);
                       ResultSet totalPendingRequestResultSet = totalPendingRequestStatement.executeQuery()){
                 return totalPendingRequestResultSet.next() ? totalPendingRequestResultSet.getInt(1) : 0; 
              }   
        }
        
        private int findTotalScanCentres(Connection connection)throws SQLException{
                try(PreparedStatement totalScanCentreStatement = connection.prepareStatement(SQLQueries.OVERALL_SCANCENTRE_COUNT);
                      ResultSet totalScanCentreResultSet = totalScanCentreStatement.executeQuery()){
                  return  totalScanCentreResultSet.next() ? totalScanCentreResultSet.getInt(1) : 0;   
                }      
        }
        
        private int findTotalAmbulances(Connection connection)throws SQLException{
                try(PreparedStatement totalAmbulanceStatement = connection.prepareStatement(SQLQueries.OVERALL_AMBULANCE_COUNT);
                       ResultSet totalAmbulanceResultSet = totalAmbulanceStatement.executeQuery()){
                   return totalAmbulanceResultSet.next() ? totalAmbulanceResultSet.getInt(1) : 0;      
               }        
        }
}
