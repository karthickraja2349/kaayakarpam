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
import java.sql.Timestamp;
import java.sql.Statement;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/hospitalDelete")
public class HospitalDelete extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        Connection connection = null;
        double latitude = 0;
        double longitude = 0;
        
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            int hospitalId = jsonData.getInt("hospitalId");
            String hospitalName = jsonData.getString("hospitalName");
            
            //check hospital
          PreparedStatement checkHospitalStatement = connection.prepareStatement(SQLQueries.CHECK_HOSPITAL);
          checkHospitalStatement.setInt(1, hospitalId);
          checkHospitalStatement.setString(2, hospitalName);
          ResultSet checkHospitalResultSet = checkHospitalStatement.executeQuery();

          if (checkHospitalResultSet.next()) {
              int count = checkHospitalResultSet.getInt(1);
              if (count != 1) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  writer.write("{\"error\": \"Hospital ID and Name mismatch\"}");
                  return;
              }
          }
            
            // Verify hospital exists and get location
            PreparedStatement hospitalDetailStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_GEO_LOCATION);
            hospitalDetailStatement.setInt(1, hospitalId);
            ResultSet hospitalDetailResultSet = hospitalDetailStatement.executeQuery();
            
            if (hospitalDetailResultSet.next()) {
                latitude = hospitalDetailResultSet.getDouble("latitude");
                longitude = hospitalDetailResultSet.getDouble("longitude");
            } 
            else {
                writer.write("{\"error\": \"Hospital not found with ID: " + hospitalId + "\"}");
                return;
            }
            
            // Get all inpatients from the hospital to be deleted
            PreparedStatement inPatientStatement = connection.prepareStatement(SQLQueries.GET_INPATIENT_DETAILS);
            inPatientStatement.setInt(1, hospitalId);
            ResultSet ipPatientResultSet = inPatientStatement.executeQuery();
            
            List<Patient> criticalPatients = new ArrayList<>();
            List<Patient> normalPatients = new ArrayList<>();
            
            while (ipPatientResultSet.next()) {
                String condition = ipPatientResultSet.getString("patient_condition");
                Patient p = new Patient(
                    ipPatientResultSet.getInt("inpatient_id"),
                    ipPatientResultSet.getString("patient_name"),
                    ipPatientResultSet.getString("patient_aadhar_number"),
                    condition,
                    ipPatientResultSet.getTimestamp("booked_date"),
                    hospitalId
                );
                
                if ("critical".equalsIgnoreCase(condition)) {
                    criticalPatients.add(p);
                } else {
                    normalPatients.add(p);
                }
            }
            
            // If there are patients, find nearby hospitals and create transfer requests
            if (!criticalPatients.isEmpty() || !normalPatients.isEmpty()) {
                // Get nearby hospitals with available beds
                PreparedStatement nearByHospitalStatement = connection.prepareStatement(SQLQueries.GET_NEARBY_HOSPITALS);
                nearByHospitalStatement.setDouble(1, latitude);
                nearByHospitalStatement.setDouble(2, longitude);
                nearByHospitalStatement.setDouble(3, latitude);
                nearByHospitalStatement.setInt(4, hospitalId);
                
                ResultSet nearByHospitalResultSet = nearByHospitalStatement.executeQuery();
                List<Hospital> hospitals = new ArrayList<>();
                
                while (nearByHospitalResultSet.next()) {
                    hospitals.add(new Hospital(
                        nearByHospitalResultSet.getInt("hospital_id"),
                        nearByHospitalResultSet.getString("hospital_name"),
                        nearByHospitalResultSet.getInt("free_general_beds"),
                        nearByHospitalResultSet.getDouble("distance_km")
                    ));
                }
                
                // Create transfer requests for patients
                boolean allTransferRequestsCreated = createTransferRequests(connection, criticalPatients, normalPatients, hospitals, hospitalId);
                
                if (!allTransferRequestsCreated) {
                    connection.rollback();
                    writer.write("{\"error\": \"Could not create transfer requests for all patients. Hospital deletion cancelled.\"}");
                    return;
                }
            }
            
            // Mark hospital as deleted
            PreparedStatement deleteHospitalStatement = connection.prepareStatement(SQLQueries.INSERT_DELETED_HOSPITALS);
            deleteHospitalStatement.setInt(1, hospitalId);
            deleteHospitalStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            int rowsAffected = deleteHospitalStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit();
                
                // Prepare response
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", "Hospital deleted successfully");
                responseJson.put("patientsTransferred", criticalPatients.size() + normalPatients.size());
                responseJson.put("message", "Transfer requests created. Hospital staff will allocate beds to transferred patients.");
                
                writer.write(responseJson.toString());
            } else {
                connection.rollback();
                writer.write("{\"error\": \"Failed to delete hospital\"}");
            }
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }
    }
    
    private boolean createTransferRequests(Connection connection, List<Patient> criticalPatients, 
                                        List<Patient> normalPatients, List<Hospital> hospitals, 
                                        int sourceHospitalId) throws SQLException {
        
        // Sort hospitals by distance (nearest first)
        hospitals.sort((h1, h2) -> Double.compare(h1.getDistance(), h2.getDistance()));
        
        // Combine and prioritize patients (critical first)
        List<Patient> allPatients = new ArrayList<>();
        allPatients.addAll(criticalPatients);
        allPatients.addAll(normalPatients);
        
        // Create transfer requests for all patients
        for (Patient patient : allPatients) {
            boolean requestCreated = false;
            
            // Try to find a suitable hospital for this patient
            for (Hospital hospital : hospitals) {
                // Create transfer request (hospital staff will later approve and allocate bed)
                int transferRequestId = createTransferRequest(connection, sourceHospitalId, hospital.getHospitalId());
                
                if (transferRequestId != -1) {
                    // Map patient to this transfer request
                    createPatientTransferMapping(connection, patient, transferRequestId);
                    requestCreated = true;
                    break;
                }
            }
            
            if (!requestCreated) {
                return false; // Could not create transfer request for a patient
            }
        }
        
        return true;
    }
    
    private int createTransferRequest(Connection connection, int fromHospitalId, int toHospitalId) throws SQLException {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                SQLQueries.CREATE_TRANSFER_REQUEST, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, fromHospitalId);
            stmt.setInt(2, toHospitalId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating transfer request: " + e.getMessage());
        }
        return -1;
    }
    
    private void createPatientTransferMapping(Connection connection, Patient patient, int transferRequestId) throws SQLException {
        try {
            // Create patient_transfer_mapping table if it doesn't exist
            PreparedStatement createTableStmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS patient_transfer_mapping (" +
                "mapping_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id INT NOT NULL, " +
                "transfer_request_id INT NOT NULL, " +
                "patient_name VARCHAR(255), " +
                "patient_aadhar VARCHAR(20), " +
                "patient_condition VARCHAR(50), " +
                "mapping_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (transfer_request_id) REFERENCES patient_temporary_requests(temporary_request_id))");
            createTableStmt.execute();
            
            // Insert mapping
            PreparedStatement stmt = connection.prepareStatement(SQLQueries.CREATE_PATIENT_TRANSFER_MAPPING);
            stmt.setInt(1, patient.getPatientId());
            stmt.setInt(2, transferRequestId);
            stmt.setString(3, patient.getName());
            stmt.setString(4, patient.getAadharNumber());
            stmt.setString(5, patient.getCondition());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating patient transfer mapping: " + e.getMessage());
            throw e;
        }
    }
    
    // Helper classes
    private class Patient {
        private int patientId;
        private String name;
        private String aadharNumber;
        private String condition;
        private Timestamp admittedDate;
        private int originalHospitalId;
        
        public Patient(int patientId, String name, String aadharNumber, String condition, 
                      Timestamp admittedDate, int originalHospitalId) {
            this.patientId = patientId;
            this.name = name;
            this.aadharNumber = aadharNumber;
            this.condition = condition;
            this.admittedDate = admittedDate;
            this.originalHospitalId = originalHospitalId;
        }
        
        public int getPatientId() { return patientId; }
        public String getName() { return name; }
        public String getAadharNumber() { return aadharNumber; }
        public String getCondition() { return condition; }
    }
    
    private class Hospital {
        private int hospitalId;
        private String name;
        private int freeBeds;
        private double distance;
        
        public Hospital(int hospitalId, String name, int freeBeds, double distance) {
            this.hospitalId = hospitalId;
            this.name = name;
            this.freeBeds = freeBeds;
            this.distance = distance;
        }
        
        public int getHospitalId() { return hospitalId; }
        public double getDistance() { return distance; }
    }
}
/*
@WebServlet("/hospitalDelete")
public class HospitalDelete extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        
        Connection connection = null;
        double latitude = 0;
        double longitude = 0;
        
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonData = new JSONObject(requestBody.toString());
            int hospitalId = jsonData.getInt("hospitalId");
            String hospitalName = jsonData.getString("hospitalName");
            
            // Verify hospital exists and get location
            PreparedStatement hospitalDetailStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_GEO_LOCATION);
            hospitalDetailStatement.setInt(1, hospitalId);
            ResultSet hospitalDetailResultSet = hospitalDetailStatement.executeQuery();
            
            if (hospitalDetailResultSet.next()) {
                latitude = hospitalDetailResultSet.getDouble("latitude");
                longitude = hospitalDetailResultSet.getDouble("longitude");
            } else {
                writer.write("{\"error\": \"Hospital not found with ID: " + hospitalId + "\"}");
                return;
            }
            
            // Get all inpatients from the hospital to be deleted
            PreparedStatement inPatientStatement = connection.prepareStatement(SQLQueries.GET_INPATIENT_DETAILS);
            inPatientStatement.setInt(1, hospitalId);
            ResultSet ipPatientResultSet = inPatientStatement.executeQuery();
            
            List<Patient> criticalPatients = new ArrayList<>();
            List<Patient> normalPatients = new ArrayList<>();
            
            while (ipPatientResultSet.next()) {
                String condition = ipPatientResultSet.getString("patient_condition");
                Patient p = new Patient(
                    ipPatientResultSet.getInt("inpatient_id"),
                    ipPatientResultSet.getString("patient_name"),
                    ipPatientResultSet.getString("patient_aadhar_number"),
                    condition,
                    ipPatientResultSet.getTimestamp("booked_date"),
                    hospitalId  // Set the original hospital ID
                );
                
                if ("critical".equalsIgnoreCase(condition)) {
                    criticalPatients.add(p);
                } else {
                    normalPatients.add(p);
                }
            }
            
            // If there are patients, find nearby hospitals and transfer them
            if (!criticalPatients.isEmpty() || !normalPatients.isEmpty()) {
                // Get nearby hospitals with available beds
                PreparedStatement nearByHospitalStatement = connection.prepareStatement(SQLQueries.GET_NEARBY_HOSPITALS);
                nearByHospitalStatement.setDouble(1, latitude);
                nearByHospitalStatement.setDouble(2, longitude);
                nearByHospitalStatement.setDouble(3, latitude);
                nearByHospitalStatement.setInt(4, hospitalId);
                
                ResultSet nearByHospitalResultSet = nearByHospitalStatement.executeQuery();
                List<Hospital> hospitals = new ArrayList<>();
                
                while (nearByHospitalResultSet.next()) {
                    hospitals.add(new Hospital(
                        nearByHospitalResultSet.getInt("hospital_id"),
                        nearByHospitalResultSet.getString("hospital_name"),
                        nearByHospitalResultSet.getInt("free_general_beds"),
                        nearByHospitalResultSet.getDouble("distance_km")
                    ));
                }
                
                // Transfer patients to nearby hospitals
                boolean allPatientsTransferred = transferPatients(connection, criticalPatients, normalPatients, hospitals, hospitalId);
                
                if (!allPatientsTransferred) {
                    connection.rollback();
                    writer.write("{\"error\": \"Could not transfer all patients. Hospital deletion cancelled.\"}");
                    return;
                }
            }
            
            // Mark hospital as deleted
            PreparedStatement deleteHospitalStatement = connection.prepareStatement(SQLQueries.INSERT_DELETED_HOSPITALS);
            deleteHospitalStatement.setInt(1, hospitalId);
            deleteHospitalStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            int rowsAffected = deleteHospitalStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                connection.commit();
                
                // Prepare response
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", "Hospital deleted successfully");
                responseJson.put("patientsTransferred", criticalPatients.size() + normalPatients.size());
                
                writer.write(responseJson.toString());
            } else {
                connection.rollback();
                writer.write("{\"error\": \"Failed to delete hospital\"}");
            }
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }
    }
    
    private boolean transferPatients(Connection connection, List<Patient> criticalPatients, 
                                   List<Patient> normalPatients, List<Hospital> hospitals, int hospitalId) throws SQLException {
        
        // Sort hospitals by distance (nearest first)
        hospitals.sort((h1, h2) -> Double.compare(h1.getDistance(), h2.getDistance()));
        
        // First transfer critical patients to nearest hospitals with available beds
        for (Patient patient : criticalPatients) {
            boolean transferred = false;
            
            for (Hospital hospital : hospitals) {
                if (hospital.getFreeBeds() > 0) {
                    // Update patient record to new hospital
                    PreparedStatement updatePatientStmt = connection.prepareStatement(
                        "UPDATE inpatient_details SET hospital_id = ? WHERE inpatient_id = ?");
                    updatePatientStmt.setInt(1, hospital.getHospitalId());
                    updatePatientStmt.setInt(2, patient.getPatientId());
                    updatePatientStmt.executeUpdate();
                    
                    // Update hospital bed count
                    PreparedStatement updateBedStmt = connection.prepareStatement(
                        "UPDATE hospital_facilities SET free_general_beds = free_general_beds - 1 WHERE hospital_id = ?");
                    updateBedStmt.setInt(1, hospital.getHospitalId());
                    updateBedStmt.executeUpdate();
                    
                    // Also update the source hospital to free up the bed
                    PreparedStatement freeSourceBedStmt = connection.prepareStatement(
                        "UPDATE hospital_facilities SET free_general_beds = free_general_beds + 1 WHERE hospital_id = ?");
                    freeSourceBedStmt.setInt(1, hospitalId);
                    freeSourceBedStmt.executeUpdate();
                    
                    hospital.setFreeBeds(hospital.getFreeBeds() - 1);
                    transferred = true;
                    break;
                }
            }
            
            if (!transferred) {
                return false; // Could not transfer a critical patient
            }
        }
        
        // Then transfer normal patients
        for (Patient patient : normalPatients) {
            boolean transferred = false;
            
            for (Hospital hospital : hospitals) {
                if (hospital.getFreeBeds() > 0) {
                    // Update patient record to new hospital
                    PreparedStatement updatePatientStmt = connection.prepareStatement(
                        "UPDATE inpatient_details SET hospital_id = ? WHERE inpatient_id = ?");
                    updatePatientStmt.setInt(1, hospital.getHospitalId());
                    updatePatientStmt.setInt(2, patient.getPatientId());
                    updatePatientStmt.executeUpdate();
                    
                    // Update hospital bed count
                    PreparedStatement updateBedStmt = connection.prepareStatement(
                        "UPDATE hospital_facilities SET free_general_beds = free_general_beds - 1 WHERE hospital_id = ?");
                    updateBedStmt.setInt(1, hospital.getHospitalId());
                    updateBedStmt.executeUpdate();
                    
                    // Also update the source hospital to free up the bed
                    PreparedStatement freeSourceBedStmt = connection.prepareStatement(
                        "UPDATE hospital_facilities SET free_general_beds = free_general_beds + 1 WHERE hospital_id = ?");
                    freeSourceBedStmt.setInt(1, hospitalId);
                    freeSourceBedStmt.executeUpdate();
                    
                    hospital.setFreeBeds(hospital.getFreeBeds() - 1);
                    transferred = true;
                    break;
                }
            }
            
            if (!transferred) {
                return false; // Could not transfer a normal patient
            }
        }
        
        return true;
    }
    
    // Helper classes for patient and hospital data
    private class Patient {
        private int patientId;
        private String name;
        private String aadharNumber;
        private String condition;
        private Timestamp admittedDate;
        private int originalHospitalId;
        
        public Patient(int patientId, String name, String aadharNumber, String condition, 
                      Timestamp admittedDate, int originalHospitalId) {
            this.patientId = patientId;
            this.name = name;
            this.aadharNumber = aadharNumber;
            this.condition = condition;
            this.admittedDate = admittedDate;
            this.originalHospitalId = originalHospitalId;
        }
        
        // Getters
        public int getPatientId() { return patientId; }
        public int getOriginalHospitalId() { return originalHospitalId; }
    }
    
    private class Hospital {
        private int hospitalId;
        private String name;
        private int freeBeds;
        private double distance;
        
        public Hospital(int hospitalId, String name, int freeBeds, double distance) {
            this.hospitalId = hospitalId;
            this.name = name;
            this.freeBeds = freeBeds;
            this.distance = distance;
        }
        
        // Getters and setters
        public int getHospitalId() { return hospitalId; }
        public int getFreeBeds() { return freeBeds; }
        public void setFreeBeds(int freeBeds) { this.freeBeds = freeBeds; }
        public double getDistance() { return distance; }
    }
}

*/
/*SELECT h.hospital_id , h.hospital_name, ROUND(6371 * ACOS (  
               COS(RADIANS(?)) * COS(RADIANS(h.latitude)) *
               COS(RADIANS(h.longitude) - RADIANS(?)) +
              SIN(RADIANS(?)) * SIN(RADIANS(h.latitude)) 
               ),2 ) AS distance_km 
              FROM hospital h JOIN hospital_facilities hf 
              ON h.hospital_id = hf.hospital_id 
              WHERE hf.free_general_beds > ?
              ORDER BY distance_km ASC LIMIT ;
              
if an admin wants to delete hospital, first take the inpatients list 
          select * from inpatient_details where hospital_id = ?;
 then fetch the nearby hospitals with available beds greater than required count of patients
 then transfer the patients to this page
           <div class="header">
        <h1>Patient Requests Management</h1>
        <p>Hospital Staff Dashboard</p>
        <button class="logout-btn" onclick="logout()">Logout</button>
    </div>
    
    <div class="container">
        <div id="error-message" class="error-message"></div>
        <div id="success-message" class="success-message"></div>
        
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Pending Patient Requests</h2>
                <button class="refresh-btn" onclick="loadRequests()">Refresh</button>
            </div>
            
            <div id="requests-table">
                <table>
                    <thead>
                        <tr>
                            <th>Request ID</th>
                            <th>Request Type</th>
                            <th>Bed Allocated</th>
                            <th>Request Time</th>
                            <th>Time Waiting</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody id="requests-body">
                        <!-- JavaScript will populate this -->
                    </tbody>
                </table>
            </div>
            
            <div id="no-requests" class="no-requests" style="display: none;">
                <p>No pending patient requests at this time.</p>
            </div>
        </div>
        
        <!-- Transfer Section -->
        <div id="transfer-section" class="card transfer-section">
            <div class="card-header">
                <h2 class="card-title">Patient Transfer - Request #<span id="transfer-request-id"></span></h2>
                <button class="transfer-cancel-btn" onclick="cancelTransfer()">Cancel Transfer</button>
            </div>
            
            <div id="transfer-data">
                <h3>Nearby Hospitals</h3>
                <p>Select a hospital to transfer this patient to:</p>
                
                <div id="hospitals-container" class="hospitals-grid">
                    <!-- Hospitals will be populated here -->
                </div>
            </div>
            
            <div class="transfer-form">
                <h3>Initiate Transfer to: <span id="selected-hospital-name"></span></h3>
                <input type="hidden" id="selected-hospital-id">
                
                <form id="transfer-form" onsubmit="submitTransfer(event)">
                    <div class="form-group">
                        <label class="form-label" for="transfer-reason">Transfer Reason:</label>
                        <textarea id="transfer-reason" class="form-input" required placeholder="Enter reason for transfer" rows="3"></textarea>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="patient-condition">Patient Condition Notes:</label>
                        <textarea id="patient-condition" class="form-input" placeholder="Any additional notes about patient condition" rows="3"></textarea>
                    </div>
                    <button type="submit" class="transfer-submit-btn">Confirm Transfer</button>
                    <button type="button" class="transfer-cancel-btn" onclick="cancelHospitalSelection()">Change Hospital</button>
                </form>
            </div>
        </div>
    </div>
   </div>
           async function loadRequests() {   
            try {
                showMessage('Loading requests...', 'success');
                const response = await fetch('/kaayakarpam/patientRequest', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                
                if (response.ok) {
                    const requests = await response.json();
                    requestsData = requests; // Store the data
                    displayRequests(requests);
                } 
                else {
                    showError('Failed to load patient requests');
                }
            } catch (error) {
                showError('Network error: ' + error.message);
            }
        }
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
   
   note : dont give css
   note : if the deleted hospital had 10 patients, the first nearby hospital has only 5 beds , then tranfer the 5 critical patients to it,and the remaining to the next hospital. if all are critical then no edge cases are there(leave it as per flow)
   
   Cannot delete or update a parent row: a foreign key constraint fails (`kaayakarpam`.`ambulance_request`, CONSTRAINT `ambulance_request_ibfk_2` FOREIGN KEY (`ambulance_id`) REFERENCES `ambulance` (`ambulance_id`))
  */            

              
              
