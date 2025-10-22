package com.kaayakarpam.admin.controller;

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

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;



@WebServlet("/scanCentreDelete")
public class DeleteScanCentre extends HttpServlet{

       protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
                HttpSession session = request.getSession(false);
                
                 Connection connection = null;
                 
                  if (session == null || session.getAttribute("user_id") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        response.sendRedirect("kaayakarpam/index.html");
                        return;
                  }      
                  int hospitalId =Integer.parseInt(request.getParameter("hospitalId"));
                   try{
                        connection = DatabaseConnection.getConnection();
                        
                        PreparedStatement CheckHospitalStatement = connection.prepareStatement(SQLQueries.IS_HOSPITAL_DELETED);
                        CheckHospitalStatement.setInt(1, hospitalId);
                        ResultSet checkHospitalResultSet = CheckHospitalStatement.executeQuery();
                        if(checkHospitalResultSet.next()){
                              writer.write("{\"error\": \"Sorry, This Hospital is already Deleted, You can't able to delete the Scan centres of the Deleted Hospitals\"}");
                              return;
                        }
                        
                        PreparedStatement viewScanCentreStatement = connection.prepareStatement(SQLQueries.VIEW_SCAN_CENTRES);
                        viewScanCentreStatement.setInt(1, hospitalId);
                        ResultSet viewScanCentreResultSet = viewScanCentreStatement.executeQuery();
                        
                        JSONArray requestArray = new JSONArray();
                        while(viewScanCentreResultSet.next()){
                               JSONObject requestObj = new JSONObject();
                               requestObj.put("scanTypeName", viewScanCentreResultSet.getString("scan_type_name"));
                               requestObj.put("scanTypeId", viewScanCentreResultSet.getInt("scan_type_id"));
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
        
       protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter writer = response.getWriter();
    response.setContentType("application/json");
    HttpSession session = request.getSession(false);
    
    Connection connection = null;
    
    if (session == null || session.getAttribute("user_id") == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writer.write("{\"error\": \"No active session. Please login again.\"}");
        return;
    }
    
    try {
        connection = DatabaseConnection.getConnection();
        
        StringBuilder requestBody = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) 
            requestBody.append(line);
        
        JSONObject jsonData = new JSONObject(requestBody.toString());
        
        int hospitalId = jsonData.getInt("hospitalId");
        
      
        
        // Handle null scanTypeId gracefully
        int scanTypeId;
        try {
            scanTypeId = jsonData.getInt("scanTypeId");
        } catch (JSONException e) {
            // If scanTypeId is null or invalid, return appropriate error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("{\"error\": \"Invalid scanTypeId provided\"}");
            return;
        }
        
        // Check if the scan centre exists before attempting to delete
        PreparedStatement checkExistsStmt = connection.prepareStatement(
            "SELECT COUNT(*) FROM scan_centres WHERE hospital_id = ? AND scan_type_id = ?");
        checkExistsStmt.setInt(1, hospitalId);
        checkExistsStmt.setInt(2, scanTypeId);
        ResultSet rs = checkExistsStmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        
        if (count == 0) {
            writer.write("{\"error\": \"Scan Centre does not exist or has already been deleted\"}");
            return;
        }
        
        PreparedStatement deleteScanCentreStatement = connection.prepareStatement(SQLQueries.DELETE_SCAN_CENTRE);
        deleteScanCentreStatement.setInt(1, hospitalId);
        deleteScanCentreStatement.setInt(2, scanTypeId);
        int rowsAffected = deleteScanCentreStatement.executeUpdate();
        
        if(rowsAffected > 0){
            PreparedStatement updateScanBookingStatement = connection.prepareStatement(SQLQueries.UPDATE_SCAN_BOOKINGS);
            updateScanBookingStatement.setInt(1, scanTypeId);
            updateScanBookingStatement.setInt(2, hospitalId);
            updateScanBookingStatement.executeUpdate();
            writer.write("{\"success\": \"Scan Centre Deleted Successfully.\"}");
        } else {
            writer.write("{\"failure\": \"Failed to delete scan centre. Please try again.\"}");  
        }
    }
    catch(Exception e){
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writer.write("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
    }
    finally{
        try{
            if(connection != null)
                connection.close();
        }
        catch(Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }        
}
}


