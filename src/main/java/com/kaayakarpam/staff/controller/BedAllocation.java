package com.kaayakarpam.staff.controller;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.Statement;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONObject;
/*
@WebServlet("/bedAllocation")
public class BedAllocation extends HttpServlet{

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session = request.getSession(false);
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writer.write("{\"message\": \"No active session. Please login again.\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        int hospitalId = 0;
        
        try {
            Connection connection = DatabaseConnection.getConnection();
            
            // Read JSON data
            StringBuilder jsonBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) 
                jsonBuffer.append(line);

            String jsonData = jsonBuffer.toString();
            JSONObject jsonObject = new JSONObject(jsonData);
            int request_id = jsonObject.getInt("temporary_request_id");
            
            // Get hospital ID
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
                writer.write("{\"message\": \"Hospital not found for this staff member\"}");
                return;
            }
            
            // Check bed availability
            PreparedStatement freeBedState = connection.prepareStatement(SQLQueries.IS_BED_FREE);
            freeBedState.setInt(1, hospitalId);
            ResultSet freeBedSet = freeBedState.executeQuery();
            if (freeBedSet.next()) {
                int freeBed = freeBedSet.getInt(1);
                if (freeBed <= 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.write("{\"message\": \"There is No Bed Available\"}");
                    return;
                }
            }
            
            // Allocate bed
            PreparedStatement allocateBedStatement = connection.prepareStatement(SQLQueries.ALLOCATE_BED);
            allocateBedStatement.setInt(1, userId);
            allocateBedStatement.setInt(2, request_id);
            int rowsaffected = allocateBedStatement.executeUpdate();
            
            if (rowsaffected == 1) {
                // Get patient details for inpatient record
                String patient_condition = "";
                Timestamp patient_register_time = null;
                PreparedStatement getPatientDetailStatement = connection.prepareStatement(SQLQueries.GET_PATIENT_DETAILS);  
                getPatientDetailStatement.setInt(1, request_id);
                ResultSet patientDetailResultSet = getPatientDetailStatement.executeQuery();
                
                if (patientDetailResultSet.next()) {
                    patient_condition = patientDetailResultSet.getString(1);
                    patient_register_time = patientDetailResultSet.getTimestamp(2);
                    
                    // NORMALIZE THE PATIENT CONDITION FOR ENUM
                    if (patient_condition != null) {
                        patient_condition = patient_condition.toUpperCase().trim();
                        if (patient_condition.contains("CRITICAL")) {
                            patient_condition = "CRITICAL";
                        } else {
                            patient_condition = "NORMAL"; // default
                        }
                    } else {
                        patient_condition = "NORMAL"; // default if null
                    }
                }
                getPatientDetailStatement.close();
                patientDetailResultSet.close();
                
                // Update inpatient details
                PreparedStatement inPatientUpdateStatement = connection.prepareStatement(SQLQueries.UPDATE_INPATIENT_DETAILS);
                inPatientUpdateStatement.setString(1, patient_condition);
                inPatientUpdateStatement.setTimestamp(2, patient_register_time);
                inPatientUpdateStatement.setInt(3, hospitalId);
                inPatientUpdateStatement.executeUpdate();
                
                // Update free beds count
                int availableFreeBeds = 0;
                PreparedStatement freeBedStatement = connection.prepareStatement(SQLQueries.GET_FREE_BEDS);
                freeBedStatement.setInt(1, hospitalId);
                ResultSet freeBedResultSet = freeBedStatement.executeQuery();
                if (freeBedResultSet.next()) {
                    availableFreeBeds = freeBedResultSet.getInt(1);
                }
                
                PreparedStatement alterFreeBedStatement = connection.prepareStatement(SQLQueries.ALTER_FREE_BEDS);
                alterFreeBedStatement.setInt(1, availableFreeBeds - 1);
                alterFreeBedStatement.setInt(2, hospitalId);
                alterFreeBedStatement.executeUpdate();
                
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write("{\"message\": \"Bed Allocated Successfully\"}");
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write("{\"message\": \"Sorry No request_id present\"}");
            }
            
        } catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
        }   
    }
}
*/

@WebServlet("/bedAllocation")
public class BedAllocation extends HttpServlet{

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session = request.getSession(false);
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writer.write("{\"message\": \"No active session. Please login again.\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        int hospitalId = 0;
        Connection connection = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); 
            
            // Read JSON data
            StringBuilder jsonBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) 
                jsonBuffer.append(line);

            String jsonData = jsonBuffer.toString();
            JSONObject jsonObject = new JSONObject(jsonData);
            int request_id = jsonObject.getInt("temporary_request_id");
            
            // Get hospital ID
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
                writer.write("{\"message\": \"Hospital not found for this staff member\"}");
                return;
            }
            
            // Check bed availability
            PreparedStatement freeBedState = connection.prepareStatement(SQLQueries.IS_BED_FREE);
            freeBedState.setInt(1, hospitalId);
            ResultSet freeBedSet = freeBedState.executeQuery();
            if (freeBedSet.next()) {
                int freeBed = freeBedSet.getInt(1);
                if (freeBed <= 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    writer.write("{\"message\": \"There is No Bed Available\"}");
                    return;
                }
            }
            
            // Check if this is a transfer request (has patient mapping) or regular ambulance request
            boolean isTransferRequest = false;
            int patientId = -1;
            String patientName = null;
            String patientAadhar = null;
            String patientCondition = null;
            
            // First, check if this is a transfer request
            PreparedStatement checkTransferStmt = connection.prepareStatement(
                "SELECT ptm.patient_id, ptm.patient_name, ptm.patient_aadhar, ptm.patient_condition " +
                "FROM patient_transfer_mapping ptm " +
                "WHERE ptm.transfer_request_id = ?");
            checkTransferStmt.setInt(1, request_id);
            ResultSet transferRs = checkTransferStmt.executeQuery();
            
            if (transferRs.next()) {
                // This is a transfer request
                isTransferRequest = true;
                patientId = transferRs.getInt("patient_id");
                patientName = transferRs.getString("patient_name");
                patientAadhar = transferRs.getString("patient_aadhar");
                patientCondition = transferRs.getString("patient_condition");
            }
            checkTransferStmt.close();
            
            // Allocate bed
            PreparedStatement allocateBedStatement = connection.prepareStatement(SQLQueries.ALLOCATE_BED);
            allocateBedStatement.setInt(1, userId);
            allocateBedStatement.setInt(2, request_id);
            int rowsaffected = allocateBedStatement.executeUpdate();
            
            if (rowsaffected == 1) {
                if (isTransferRequest) {
                    // Handle transfer patient - update existing patient record
                    PreparedStatement updatePatientStmt = connection.prepareStatement(
                        "UPDATE inpatient_details SET hospital_id = ?, booked_date = NOW() " +
                        "WHERE inpatient_id = ?");
                    updatePatientStmt.setInt(1, hospitalId);
                    updatePatientStmt.setInt(2, patientId);
                    updatePatientStmt.executeUpdate();
                    
                    // Also update patient details if they were incomplete
                    if (patientName != null || patientAadhar != null || patientCondition != null) {
                        PreparedStatement updatePatientDetailsStmt = connection.prepareStatement(
                            "UPDATE inpatient_details SET patient_name = COALESCE(?, patient_name), " +
                            "patient_aadhar_number = COALESCE(?, patient_aadhar_number), " +
                            "patient_condition = COALESCE(?, patient_condition) " +
                            "WHERE inpatient_id = ?");
                        updatePatientDetailsStmt.setString(1, patientName);
                        updatePatientDetailsStmt.setString(2, patientAadhar);
                        updatePatientDetailsStmt.setString(3, patientCondition);
                        updatePatientDetailsStmt.setInt(4, patientId);
                        updatePatientDetailsStmt.executeUpdate();
                    }
                } else {
                    // Handle regular ambulance request 
                    String patient_condition = "";
                    Timestamp patient_register_time = null;
                    PreparedStatement getPatientDetailStatement = connection.prepareStatement(SQLQueries.GET_PATIENT_DETAILS);  
                    getPatientDetailStatement.setInt(1, request_id);
                    ResultSet patientDetailResultSet = getPatientDetailStatement.executeQuery();
                    
                    if (patientDetailResultSet.next()) {
                        patient_condition = patientDetailResultSet.getString(1);
                        patient_register_time = patientDetailResultSet.getTimestamp(2);
                        
                        
                        if (patient_condition != null) {
                            patient_condition = patient_condition.toUpperCase().trim();
                            if (patient_condition.contains("CRITICAL")) {
                                patient_condition = "CRITICAL";
                            } 
                            else {
                                patient_condition = "NORMAL"; 
                            }
                        } 
                        else {
                            patient_condition = "NORMAL"; 
                        }
                    }
                    getPatientDetailStatement.close();
                    patientDetailResultSet.close();
                    
                    PreparedStatement updateTemporaryRequestStatement = connection.prepareStatement(SQLQueries.UPDATE_PROCESSED_STATUS);
                    updateTemporaryRequestStatement.setInt(1, request_id);
                    updateTemporaryRequestStatement.executeUpdate();
                    
                    // Update inpatient details
                    PreparedStatement inPatientUpdateStatement = connection.prepareStatement(SQLQueries.UPDATE_INPATIENT_DETAILS);
                    inPatientUpdateStatement.setString(1, patient_condition);
                   // inPatientUpdateStatement.setTimestamp(2, patient_register_time);
                    inPatientUpdateStatement.setInt(2, hospitalId);
                    inPatientUpdateStatement.executeUpdate();
                }
                
                // Update free beds count (common for both cases)
                int availableFreeBeds = 0;
                PreparedStatement freeBedStatement = connection.prepareStatement(SQLQueries.GET_FREE_BEDS);
                freeBedStatement.setInt(1, hospitalId);
                ResultSet freeBedResultSet = freeBedStatement.executeQuery();
                if (freeBedResultSet.next()) {
                    availableFreeBeds = freeBedResultSet.getInt(1);
                }
                
                PreparedStatement alterFreeBedStatement = connection.prepareStatement(SQLQueries.ALTER_FREE_BEDS);
                alterFreeBedStatement.setInt(1, availableFreeBeds - 1);
                alterFreeBedStatement.setInt(2, hospitalId);
                alterFreeBedStatement.executeUpdate();
                
                connection.commit(); // Commit
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write("{\"message\": \"Bed Allocated Successfully\"}");
                
            } 
            else {
                connection.rollback(); // Rollback if  allocation failed
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write("{\"message\": \"There is No Bed Available\"}");
            }
            
        } catch(Exception e) {
            try {
                if (connection != null) connection.rollback(); // Rollback on error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


/*
after bed allocation, move the patient to the inpatient_details .(inpatient_id, patient_name, patient_aadhar_number, patient_condition, reason, booked_date, discharge_date)

create table inpatient_details(inpatient_id int primary key auto_increment, patient_name varchar(50) default null, patient_aadhar_number varchar(12) default null, patient_condition enum('CRITICAL', 'NORMAL') , reason varchar(255), booked_date timestamp, discharge_date timestamp default null);

 SELECT ptr.ambulance_request_id, ptr.request_time, ar.request_type FROM patient_temporary_requests ptr JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id WHERE ar.request_id = 1
 
 
complete java code
html view
*/

