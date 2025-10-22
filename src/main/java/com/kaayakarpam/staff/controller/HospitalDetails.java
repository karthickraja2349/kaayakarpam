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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/hospitalDetails")
public class HospitalDetails extends HttpServlet{
         
         protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
                 
                 PrintWriter writer = response.getWriter();
                 response.setContentType("application/json");
                 
                 HttpSession session  = request.getSession(false);
                 if(session == null || session.getAttribute("user_id") == null){
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        writer.write("{\"error\": \"No active session. Please login again.\"}");
                        return;
                 }
                 
                 int userId = (Integer)session.getAttribute("user_id");
                 int hospitalId = 0;
                 
                 Connection connection = null;
                 try{
                     connection = DatabaseConnection.getConnection();
                     PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID);
                     hospitalIdStatement.setInt(1,userId);
                     ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery();
                     
                     if(hospitalIdResultSet.next())
                          hospitalId = hospitalIdResultSet.getInt(1);
                     else{
                          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                          writer.write("{\"error\": \"  No Hospital was Found\"}");
                     }
                     hospitalIdStatement.close();
                     hospitalIdResultSet.close();
                     
                     PreparedStatement hospitalDetailsStatement  = connection.prepareStatement(SQLQueries.GET_HOSPITAL_DETAILS);
                     hospitalDetailsStatement.setInt(1, hospitalId);
                     ResultSet hospitalResultSet = hospitalDetailsStatement.executeQuery();
                     
                     JSONArray requestArray = new JSONArray();
                     while(hospitalResultSet.next()){
                            JSONObject requestObj = new JSONObject();
                            requestObj.put("hospitalName", hospitalResultSet.getString("hospital_name"));
                            requestObj.put("hospitalAddress", hospitalResultSet.getString("address"));
                            requestObj.put("hospitalPhoneNumber", hospitalResultSet.getString("phone_number"));
                            requestObj.put("ceo", hospitalResultSet.getString("ceo_name"));
                            requestObj.put("totalBeds", hospitalResultSet.getInt("total_general_beds"));
                            requestObj.put("freeBeds", hospitalResultSet.getInt("free_general_beds"));
                            requestObj.put("icu", hospitalResultSet.getInt("total_icu_beds"));
                            requestObj.put("labCount", hospitalResultSet.getInt("lab_count"));
                            requestObj.put("ambulanceCount", hospitalResultSet.getInt("ambulance_count"));
                            
                            requestArray.put(requestObj);
                     }
                     writer.write(requestArray.toString());
                 }
                 catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                 }
                 finally{
                       try{
                              if(connection == null)
                                   connection.close();
                       }
                       catch(Exception e){
                              response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                              writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                       }
                 }
         }
}
