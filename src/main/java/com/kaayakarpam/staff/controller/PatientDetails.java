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

import org.json.JSONObject;
import org.json.JSONArray;
@WebServlet("/inPatientDetails")
public class PatientDetails extends HttpServlet{
           
           protected void doGet(HttpServletRequest request , HttpServletResponse response)throws IOException{
                   HttpSession session = request.getSession(false);
                   PrintWriter writer = response.getWriter();
                  response.setContentType("application/json");
                
                   if (session == null || session.getAttribute("user_id") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        return;
                  }
                
                int user_id =(Integer) session.getAttribute("user_id");
                
                try{
                        Connection connection = DatabaseConnection.getConnection();
                        int hospitalId = getHospitalId(user_id);
                        PreparedStatement getPatientStatement = connection.prepareStatement(SQLQueries.GET_IN_PATIENT_DETAILS);
                        getPatientStatement.setInt(1, hospitalId);
                        ResultSet patientResultSet = getPatientStatement.executeQuery();
                        
                        JSONArray patientsArray = new JSONArray();
                        while (patientResultSet.next()) {
                                JSONObject patient = new JSONObject();
                                patient.put("patient_id", patientResultSet.getInt("inpatient_id"));
                                patient.put("patient_name", patientResultSet.getString("patient_name"));
                                patient.put("aadhar_number", patientResultSet.getString("patient_aadhar_number"));
                                patient.put("condition", patientResultSet.getString("patient_condition"));
                                patient.put("reason_for_admittance", patientResultSet.getString("reason_for_admittance"));
                                patient.put("admission_date", patientResultSet.getTimestamp("admission_date"));
                                patient.put("room_no", patientResultSet.getString("roomNo"));
                                patient.put("discharge_date", patientResultSet.getDate("discharge_date"));
                        
                                patientsArray.put(patient);
                        }
                      writer.write(patientsArray.toString());
                      getPatientStatement.close();
                      patientResultSet.close();  
                }
                catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writer.write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
                }
       }    
           
           private int getHospitalId(int staffId)throws Exception{
                int hospitalId = 0;
                 PreparedStatement findHospitalIdStatement = DatabaseConnection.getPreparedStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
                 findHospitalIdStatement.setInt(1, staffId);
                 ResultSet hospitalIdResultSet = findHospitalIdStatement.executeQuery();
                if (hospitalIdResultSet.next()) {
                            hospitalId = hospitalIdResultSet.getInt(1);
                }
                findHospitalIdStatement.close();
                hospitalIdResultSet.close();
                return hospitalId;
           }
}


/*
a2FydGhpY2stdHM0NzY%3D.fdZx7SUx7d3SRohKHvs1GuEEcx422DQJqbUvZeh6DsK9jPF59WA6jJET9Pkgc4Zorn5CTuC46OPAwmlVKVo9NlOMPS7HDbzXaXyClik3QlXX3ReEqW7d4od2lQBwdZLs.3Z7k7FAoj8IVxARzpHwW9Dgh7Riubi7O7LwBCWiLZkkycbkXlYkK1Jd5fCc4NNET66MvrUVv9R7TjMGOrMy3Gw%3D%3D

a2FydGhpY2stdHM0NzY%3D.fdZx7SUx7d3SRohKHvs1GuEEcx422DQJqbUvZeh6DsK9jPF59WA6jJET9Pkgc4Zorn5CTuC46OPAwmlVKVo9NlOMPS7HDbzXaXyClik3QlXX3ReEqW7d4od2lQBwdZLs.3Z7k7FAoj8IVxARzpHwW9Dgh7Riubi7O7LwBCWiLZkkycbkXlYkK1Jd5fCc4NNET66MvrUVv9R7TjMGOrMy3Gw%3D%3D
*/
