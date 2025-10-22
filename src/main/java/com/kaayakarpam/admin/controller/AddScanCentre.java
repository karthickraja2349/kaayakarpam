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

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/scanCentreAdd")
public class AddScanCentre extends HttpServlet{
         
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
                
                 PrintWriter writer = response.getWriter();
                 response.setContentType("application/json");
                 
                 Connection connection = null;
                 try{
                        connection = DatabaseConnection.getConnection();
                        
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        BufferedReader reader = request.getReader();
                        while ((line = reader.readLine()) != null) 
                            requestBody.append(line);
            
                        JSONObject jsonData = new JSONObject(requestBody.toString());
                        
                        int hospitalId = jsonData.getInt("hospitalId");
                        int scanTypeId = jsonData.getInt("scanTypeId");
                        
                        PreparedStatement checkHospitalStatement = connection.prepareStatement(SQLQueries.IS_HOSPITAL_DELETED);
                        checkHospitalStatement.setInt(1, hospitalId);
                        ResultSet checkHospitalResultSet = checkHospitalStatement.executeQuery();
                        if(checkHospitalResultSet.next()){
                             writer.write("{\"status\":\"failure\",\"message\":\"Sorry, This Hospital was already Deleted, You can't able to add Scan Centres to the deleted Hospitals\"}");
                              return;
                        }
                        
                        PreparedStatement verifyStatement = connection.prepareStatement(SQLQueries.CHECK_SCAN_CENTRE_IN_HOSPITAL);
                        verifyStatement.setInt(1, hospitalId);
                        verifyStatement.setInt(2, scanTypeId);
                        ResultSet verifyResultSet = verifyStatement.executeQuery();
                        if(verifyResultSet.next()){
                             int count = verifyResultSet.getInt(1);
                             if(count !=0){
                                    writer.write("{\"status\":\"failure\",\"message\":\"Sorry, ScanCentre was already present in the Hospital\"}");
                                    return;
                             }
                        }
                        
                        PreparedStatement scanCentreStatement = connection.prepareStatement(SQLQueries.ADD_SCAN_CENTRE);
                        scanCentreStatement.setInt(1, scanTypeId);
                        scanCentreStatement.setInt(2, hospitalId);
                        int rowsAffected = scanCentreStatement.executeUpdate();
                        
                     if (rowsAffected > 0) {
                          writer.write("{\"status\":\"success\",\"message\":\"Scan Centre Added Successfully\"}");
                      } 
                      else {
                          writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
                      }

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
