package com.kaayakarpam.admin.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.kaayakarpam.common.db.DatabaseConnection;

import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

@WebServlet("/hospitalUpdate")
public class HospitalUpdate extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        Connection connection = null;

        try {
            
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) 
                requestBody.append(line);
            
            JSONObject jsonData = new JSONObject(requestBody.toString());

            int hospitalId = jsonData.getInt("hospitalId");

            
            StringBuilder sql = new StringBuilder("UPDATE hospital SET ");
            StringBuilder facilitiesSql = new StringBuilder("UPDATE hospital_facilities SET ");

            boolean hasHospitalUpdate = false;
            boolean hasFacilityUpdate = false;

            
            List<Object> hospitalParams = new ArrayList();
            List<Object> facilityParams = new ArrayList();

            
                       
            if (jsonData.has("hospitalName") && !jsonData.optString("hospitalName").isEmpty()) {
                sql.append("hospital_name = ?, ");
                hospitalParams.add(jsonData.getString("hospitalName"));
                hasHospitalUpdate = true;
            }

            if (jsonData.has("hospitalAddress") && !jsonData.optString("hospitalAddress").isEmpty()) {
                sql.append("address = ?, ");
                hospitalParams.add(jsonData.getString("hospitalAddress"));
                hasHospitalUpdate = true;
            }

            if (jsonData.has("latitude") && jsonData.has("longitude") 
                    && !jsonData.optString("latitude").isEmpty() && !jsonData.optString("longitude").isEmpty()) {
                sql.append("latitude = ?, longitude = ?, ");
                hospitalParams.add(Double.parseDouble(jsonData.getString("latitude")));
                hospitalParams.add(Double.parseDouble(jsonData.getString("longitude")));
                hasHospitalUpdate = true;
            }

            if (jsonData.has("hospitalPhoneNumber") && !jsonData.optString("hospitalPhoneNumber").isEmpty()) {
                sql.append("phone_number = ?, ");
                hospitalParams.add(jsonData.getString("hospitalPhoneNumber"));
                hasHospitalUpdate = true;
            }


            
           if (jsonData.has("totalBeds") && !jsonData.optString("totalBeds").isEmpty()) {
              facilitiesSql.append("total_general_beds = ?, ");
              facilityParams.add(Integer.parseInt(jsonData.getString("totalBeds")));
              hasFacilityUpdate = true;
          }

            if (jsonData.has("freeBeds") && !jsonData.optString("freeBeds").isEmpty()) {
                facilitiesSql.append("free_general_beds = ?, ");
                facilityParams.add(Integer.parseInt(jsonData.getString("freeBeds")));
                hasFacilityUpdate = true;
            }

            if (jsonData.has("labCount") && !jsonData.optString("labCount").isEmpty()) {
                facilitiesSql.append("lab_count = ?, ");
                facilityParams.add(Integer.parseInt(jsonData.getString("labCount")));
                hasFacilityUpdate = true;
            }

            if (jsonData.has("icuCount") && !jsonData.optString("icuCount").isEmpty()) {
                facilitiesSql.append("total_icu_beds = ?, ");
                facilityParams.add(Integer.parseInt(jsonData.getString("icuCount")));
                hasFacilityUpdate = true;
            }

            if (jsonData.has("ambulanceCount") && !jsonData.optString("ambulanceCount").isEmpty()) {
                facilitiesSql.append("ambulance_count = ?, ");
                facilityParams.add(Integer.parseInt(jsonData.getString("ambulanceCount")));
                hasFacilityUpdate = true;
            }

            connection = DatabaseConnection.getConnection();

            int rowsAffected = 0;

           
            if (hasHospitalUpdate) {
                String finalSql = sql.substring(0, sql.length() - 2) + " WHERE hospital_id = ?";
                hospitalParams.add(hospitalId);

                try (PreparedStatement hospitalUpdateStatement = connection.prepareStatement(finalSql)) {
                    for (int i = 0; i < hospitalParams.size(); i++) {
                        hospitalUpdateStatement.setObject(i + 1, hospitalParams.get(i));
                    }
                    rowsAffected += hospitalUpdateStatement.executeUpdate();
                }
            }

            
            if (hasFacilityUpdate) {
                String finalSql = facilitiesSql.substring(0, facilitiesSql.length() - 2) + " WHERE hospital_id = ?";
                facilityParams.add(hospitalId);

                try (PreparedStatement hospitalFacilityUpdateStatement = connection.prepareStatement(finalSql)) {
                    for (int i = 0; i < facilityParams.size(); i++) {
                        hospitalFacilityUpdateStatement.setObject(i + 1, facilityParams.get(i));
                    }
                    rowsAffected += hospitalFacilityUpdateStatement.executeUpdate();
                }
            }

            if (rowsAffected > 0) {
                writer.write("{\"success\":\"Hospital updated successfully\"}");
            } 
            else {
                writer.write("{\"error\":\"No fields updated\"}");
            }
        } 
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"error\": \"" + e.getMessage() + "\"}");
        } 
        finally {
            try {
                if (connection != null) 
                   connection.close();
            }
            catch (Exception e) {
                writer.write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
