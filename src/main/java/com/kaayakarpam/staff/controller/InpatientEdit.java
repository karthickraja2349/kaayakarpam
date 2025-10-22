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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONObject;

@WebServlet("/inpatientEdit")
public class InpatientEdit extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();

        try {
                  // Read the request body
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            
            // Parse the JSON data
            JSONObject jsonData = new JSONObject(requestBody.toString());
            
            // Get patientId from JSON
            Integer patientId = jsonData.has("patientId") ? jsonData.getInt("patientId") : null;
            if (patientId == null) {
                response.getWriter().write("Patient ID is required!");
                return;
            }

            String patientName = jsonData.has("patientName") ? jsonData.getString("patientName") : null;
            String patientAadharNumber = jsonData.has("patientAadharNumber") ? jsonData.getString("patientAadharNumber") : null;
            String patientCondition = jsonData.has("patientCondition") ? jsonData.getString("patientCondition") : null;
            String reason = jsonData.has("reason") ? jsonData.getString("reason") : null;
            String roomNumber = jsonData.has("roomNumber") ? jsonData.getString("roomNumber") : null;
            String dischargeDate = jsonData.has("dischargeDate") ? jsonData.getString("dischargeDate") : null;

            StringBuilder sql = new StringBuilder("UPDATE inpatient_details SET ");
            List<Object> params = new ArrayList<>();

            if (patientName != null && !patientName.isEmpty()) {
                sql.append("patient_name = ?, ");
                params.add(patientName);
            }
            if (patientAadharNumber != null && !patientAadharNumber.isEmpty()) {
                sql.append("patient_aadhar_number = ?, ");
                params.add(patientAadharNumber);
            }
            if (patientCondition != null && !patientCondition.isEmpty()) {
                sql.append("patient_condition = ?, ");
                params.add(patientCondition);
            }
            if (reason != null && !reason.isEmpty()) {
                sql.append("reason = ?, ");
                params.add(reason);
            }
            if (roomNumber != null && !roomNumber.isEmpty()) {
                sql.append("roomNo = ?, ");
                params.add(roomNumber);
            }
            if (dischargeDate != null && !dischargeDate.isEmpty()) {
                sql.append("discharge_date = ?, ");
                params.add(dischargeDate);
            }

            if (params.isEmpty()) {
                writer.write("Nothing to update.");
                return;
            }

            // remove last comma+space
            sql.setLength(sql.length() - 2);

            sql.append(" WHERE inpatient_id = ?");
            params.add(patientId);

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement updateStatement = connection.prepareStatement(sql.toString())) {

                for (int i = 0; i < params.size(); i++) {
                    updateStatement.setObject(i + 1, params.get(i));
                }

                int rows = updateStatement.executeUpdate();
                if (rows > 0) {
                    writer.write("Patient details updated successfully!");
                } else {
                    writer.write("No patient found with given id!");
                }
            }

        } catch (Exception e) {
            writer.write("Error: " + e.getMessage());
        }
    }
}

