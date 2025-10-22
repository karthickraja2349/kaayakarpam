package com.kaayakarpam.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet("/hospitalsView")
public class HospitalsView extends HttpServlet{
           
         protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
                  response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                  response.setHeader("Pragma", "no-cache");
                  response.setHeader("Expires", "0");
        
                 response.setContentType("application/json");
                 PrintWriter writer = response.getWriter();
                 
                 Connection connection = null;
                 
                 try{
                      connection = DatabaseConnection.getConnection();
                      PreparedStatement hospitalViewStatement = connection.prepareStatement(SQLQueries.VIEW_HOSPITAL);
                      ResultSet hospitalViewResultSet = hospitalViewStatement.executeQuery();
                      
                      
                      JSONArray requestArray = new JSONArray();
                      while(hospitalViewResultSet.next()){
                              JSONObject requestObj = new JSONObject();
                              requestObj.put("hospitalId", hospitalViewResultSet.getString("hospital_id"));
                              requestObj.put("hospitalName", hospitalViewResultSet.getString("hospital_name"));
                              requestObj.put("hospitalAddress", hospitalViewResultSet.getString("address"));
                              requestObj.put("hospitalPhoneNumber", hospitalViewResultSet.getString("phone_number"));
                              requestObj.put("totalBeds", hospitalViewResultSet.getString("total_general_beds"));
                              requestObj.put("freeBeds", hospitalViewResultSet.getString("free_general_beds"));
                              requestObj.put("labCount", hospitalViewResultSet.getString("lab_count"));
                              
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
                          if(connection !=null)
                              connection.close();
                      }
                      catch(Exception e){
                          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                          writer.write("{\"error\": \"" + e.getMessage() + "\"}");                      
                      }
                 }
         }
}
