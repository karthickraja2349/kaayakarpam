package com.kaayakarpam.staff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONArray;
import org.json.JSONObject;
/*
@WebServlet("/patientRequest")
public class PatientRequests extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\": \"No active session. Please login again.\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        int userId = (Integer) session.getAttribute("user_id");
        
        if (!"staff".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\": \"Access denied. Staff role required.\"}");
            return;
        }
        
        try {
            Connection connection = DatabaseConnection.getConnection();
            int hospitalId = 0;
            PreparedStatement findHospitalIdStatement =connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
            findHospitalIdStatement.setInt(1, userId);
            ResultSet hospitalIdResultSet = findHospitalIdStatement.executeQuery();
            
            if (hospitalIdResultSet.next()) {
                hospitalId = hospitalIdResultSet.getInt(1);
            }
            findHospitalIdStatement.close();
            hospitalIdResultSet.close();
            
            if (hospitalId == 0) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Hospital not found for this staff member.\"}");
                return;
            }
            
            PreparedStatement getTemporaryRequestStatement = connection.prepareStatement(SQLQueries.GET_TEMPORARY_REQUESTS);
            getTemporaryRequestStatement.setInt(1, hospitalId);
            
            ResultSet temporaryRequestsResultSet = getTemporaryRequestStatement.executeQuery();
            
            JSONArray requestsArray = new JSONArray();
            
            while (temporaryRequestsResultSet.next()) {
                JSONObject requestObj = new JSONObject();
                requestObj.put("temporary_request_id", temporaryRequestsResultSet.getInt("temporary_request_id"));
                requestObj.put("ambulance_request_id", temporaryRequestsResultSet.getInt("ambulance_request_id"));
                requestObj.put("request_type", temporaryRequestsResultSet.getString("request_type"));
                requestObj.put("isBedAllocated", temporaryRequestsResultSet.getBoolean("isBedAllocated"));
                requestObj.put("request_time", temporaryRequestsResultSet.getTimestamp("request_time").toString());
                
                requestsArray.put(requestObj);
            }
            
            temporaryRequestsResultSet.close();
            getTemporaryRequestStatement.close();
            
            out.write(requestsArray.toString());
            
        } 
        catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } 
        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}
*/
@WebServlet("/patientRequest")
public class PatientRequests extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\": \"No active session. Please login again.\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        int userId = (Integer) session.getAttribute("user_id");
        
        if (!"staff".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write("{\"error\": \"Access denied. Staff role required.\"}");
            return;
        }
        
        try {
            Connection connection = DatabaseConnection.getConnection();
            int hospitalId = 0;
            PreparedStatement findHospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
            findHospitalIdStatement.setInt(1, userId);
            ResultSet hospitalIdResultSet = findHospitalIdStatement.executeQuery();
            
            if (hospitalIdResultSet.next()) {
                hospitalId = hospitalIdResultSet.getInt(1);
            }
            findHospitalIdStatement.close();
            hospitalIdResultSet.close();
            
            if (hospitalId == 0) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Hospital not found for this staff member.\"}");
                return;
            }
            
            // Get both transfer requests AND regular ambulance requests
            PreparedStatement getTemporaryRequestStatement = connection.prepareStatement(
                "SELECT ptr.temporary_request_id, ptr.ambulance_request_id, " +
                "COALESCE(ar.request_type, 'TRANSFER') AS request_type, " +
                "ptr.isBedAllocated, ptr.request_time, " +
                "COALESCE(ptr.transferred_from, 0) AS source_hospital_id, " +
                "COALESCE(ptr.transferred_to, 0) AS destination_hospital_id, " +
                "COALESCE(ptm.patient_name, 'Unknown') AS patient_name, " +
                "COALESCE(ptm.patient_aadhar, 'Unknown') AS patient_aadhar, " +
                "COALESCE(ptm.patient_condition, 'Unknown') AS patient_condition, " +
                "CASE WHEN ptr.ambulance_request_id IS NULL THEN 'TRANSFER' ELSE 'AMBULANCE' END AS request_category " +
                "FROM patient_temporary_requests ptr " +
                "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                "LEFT JOIN patient_transfer_mapping ptm ON ptr.temporary_request_id = ptm.transfer_request_id " +
                "WHERE ptr.isBedAllocated = 0 " +
                "AND (ptr.transferred_to = ? OR ar.hospital_id = ?)");
            
            getTemporaryRequestStatement.setInt(1, hospitalId);
            getTemporaryRequestStatement.setInt(2, hospitalId);
            ResultSet temporaryRequestsResultSet = getTemporaryRequestStatement.executeQuery();
            
            JSONArray requestsArray = new JSONArray();
            
            while (temporaryRequestsResultSet.next()) {
                JSONObject requestObj = new JSONObject();
                requestObj.put("temporary_request_id", temporaryRequestsResultSet.getInt("temporary_request_id"));
                requestObj.put("ambulance_request_id", temporaryRequestsResultSet.getInt("ambulance_request_id"));
                requestObj.put("request_type", temporaryRequestsResultSet.getString("request_type"));
                requestObj.put("isBedAllocated", temporaryRequestsResultSet.getBoolean("isBedAllocated"));
                requestObj.put("request_time", temporaryRequestsResultSet.getTimestamp("request_time").toString());
                requestObj.put("source_hospital_id", temporaryRequestsResultSet.getInt("source_hospital_id"));
                requestObj.put("destination_hospital_id", temporaryRequestsResultSet.getInt("destination_hospital_id"));
                requestObj.put("patient_name", temporaryRequestsResultSet.getString("patient_name"));
                requestObj.put("patient_aadhar", temporaryRequestsResultSet.getString("patient_aadhar"));
                requestObj.put("patient_condition", temporaryRequestsResultSet.getString("patient_condition"));
                requestObj.put("request_category", temporaryRequestsResultSet.getString("request_category"));
                
                requestsArray.put(requestObj);
            }
            
            temporaryRequestsResultSet.close();
            getTemporaryRequestStatement.close();
            
            out.write(requestsArray.toString());
            
        } 
        catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } 
        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}
/*
retrive ambulance_request_id as request_id from patient_temporary_request;
by using request_id retrive a.hospital_id from ambulance_request a;
show patients releated to staff hospital_id = a.hospital_id;

SELECT 
    ar.request_type,
    ptr.isBedAllocated,
    ptr.request_time
FROM patient_temporary_requests ptr
JOIN ambulance_request ar 
    ON ptr.ambulance_request_id = ar.request_id;

the staff will see  , temporary_request_id, request_type, isBedAllocated, request_time in the same  page , then at the last i need a textbox label as Are you want to allocate Bed. placeholder enter temporary_request_id after that control pass to /bedAllocation
*/

