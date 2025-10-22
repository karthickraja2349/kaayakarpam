package com.kaayakarpam.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/addHospital")
@MultipartConfig
public class HospitalAdder extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        
        String hospitalName = request.getParameter("hospitalName");
        String hospitalAddress = request.getParameter("hospitalAddress");
        String hospitalLocation = request.getParameter("hospitalLocation");
        String hospitalCeoName = request.getParameter("ceoName");
        String hospitalPhoneNumber = request.getParameter("phoneNumber");
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longitude");
        String totalGeneralBeds = request.getParameter("generalBeds");
        String totalIcuBeds = request.getParameter("icuBeds");
        String labCount = request.getParameter("labCount");
        String ambulanceCount = request.getParameter("ambulanceCount");

        Connection connection = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Check for existing hospital with same name and location
            PreparedStatement checkHospital = connection.prepareStatement(
                "SELECT hospital_id FROM hospital WHERE hospital_name = ? AND location = ?"
            );
            checkHospital.setString(1, hospitalName);
            checkHospital.setString(2, hospitalLocation);
            
            ResultSet existingHospital = checkHospital.executeQuery();
            if (existingHospital.next()) {
                connection.rollback();
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                writer.write("{\"status\":\"error\",\"message\":\"Hospital with this name already exists in this location\"}");
                return;
            }

            // Check for existing phone number
            PreparedStatement checkPhone = connection.prepareStatement(
                "SELECT hospital_id FROM hospital WHERE phone_number = ?"
            );
            checkPhone.setString(1, hospitalPhoneNumber);
            
            ResultSet existingPhone = checkPhone.executeQuery();
            if (existingPhone.next()) {
                connection.rollback();
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                writer.write("{\"status\":\"error\",\"message\":\"Phone number already registered with another hospital\"}");
                return;
            }

            // Insert hospital with RETURN_GENERATED_KEYS
            PreparedStatement insertHospital = connection.prepareStatement(
                "INSERT INTO hospital(hospital_name, address, ceo_name, location, phone_number, latitude, longitude) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            insertHospital.setString(1, hospitalName);
            insertHospital.setString(2, hospitalAddress);
            insertHospital.setString(3, hospitalCeoName);
            insertHospital.setString(4, hospitalLocation);
            insertHospital.setString(5, hospitalPhoneNumber);
            insertHospital.setString(6, latitude);
            insertHospital.setString(7, longitude);
            
            int hospitalRows = insertHospital.executeUpdate();
            
            if (hospitalRows == 0) {
                connection.rollback();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"status\":\"error\",\"message\":\"Failed to insert hospital\"}");
                return;
            }

            // Get generated hospital ID
            int generatedId = -1;
            ResultSet generatedKeys = insertHospital.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            }
            generatedKeys.close();

            if (generatedId == -1) {
                connection.rollback();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"status\":\"error\",\"message\":\"Failed to retrieve hospital ID\"}");
                return;
            }

            // Insert hospital facilities
            PreparedStatement insertFacilities = connection.prepareStatement(
                "INSERT INTO hospital_facilities(hospital_id, total_general_beds, free_general_beds, total_icu_beds, free_icu_beds, lab_count, ambulance_count) VALUES (?,?,?,?,?,?,?)"
            );
            
            int generalBeds = Integer.parseInt(totalGeneralBeds);
            int icuBeds = Integer.parseInt(totalIcuBeds);
            int labs = Integer.parseInt(labCount);
            int ambulances = Integer.parseInt(ambulanceCount);
            
            insertFacilities.setInt(1, generatedId);
            insertFacilities.setInt(2, generalBeds);
            insertFacilities.setInt(3, generalBeds); // free beds initially equal total beds
            insertFacilities.setInt(4, icuBeds);
            insertFacilities.setInt(5, icuBeds); // free ICU beds initially equal total ICU beds
            insertFacilities.setInt(6, labs);
            insertFacilities.setInt(7, ambulances);
            
            int facilitiesRows = insertFacilities.executeUpdate();
            
            if (facilitiesRows == 0) {
                connection.rollback();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"status\":\"error\",\"message\":\"Failed to insert hospital facilities\"}");
                return;
            }

            // Commit transaction
            connection.commit();
            
            writer.write("{\"status\":\"success\",\"message\":\"Hospital registered successfully\",\"hospitalId\":" + generatedId + "}");

        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException rollbackEx) {
                // Log rollback error
            }
      
            // Handle unique constraint violations
            if (e.getSQLState().equals("23000")) { // Integrity constraint violation
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                if (e.getMessage().contains("unique_hospital_name_location")) {
                    writer.write("{\"status\":\"error\",\"message\":\"Hospital with this name already exists in this location\"}");
                } 
                else if (e.getMessage().contains("unique_hospital_phone")) {
                    writer.write("{\"status\":\"error\",\"message\":\"Phone number already registered with another hospital\"}");
                } 
                else {
                    writer.write("{\"status\":\"error\",\"message\":\"Database constraint violation: " + e.getMessage() + "\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("{\"status\":\"error\",\"message\":\"Server error: " + e.getMessage() + "\"}");
            }
            
        } catch (NumberFormatException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException rollbackEx) {
                // Log rollback error
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("{\"status\":\"error\",\"message\":\"Invalid number format in facilities data\"}");
            
        } catch (Exception e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException rollbackEx) {
                // Log rollback error
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"status\":\"error\",\"message\":\"Unexpected error: " + e.getMessage() + "\"}");
            
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Reset auto-commit
                    connection.close();
                }
            } catch (SQLException e) {
                // Log connection closing error
            }
        }
    }
}

/*
javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.servlet-api.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/json-20240303.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-9.0.0.jar"     -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes      com/kaayakarpam/common/db/DatabaseConnection.java com/kaayakarpam/common/db/SQLQueries.java com/kaayakarpam/admin/controller/HospitalAdder.java


*/
