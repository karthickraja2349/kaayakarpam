package com.kaayakarpam.common.backTask;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/hospitalRetriver")
public class HospitalRetriver extends HttpServlet{
        
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
              
               PrintWriter writer = response.getWriter();
               response.setContentType("application/json");
               
               Connection connection  = null;
               try{
                   connection = DatabaseConnection.getConnection();
                   
                   PreparedStatement hospitalRetriveStatement = connection.prepareStatement(SQLQueries.GET_HOSPITALS);
                   ResultSet hospitalRetriveResultSet = hospitalRetriveStatement.executeQuery();
                   
                   JSONArray requestArray = new JSONArray();
                   
                   while(hospitalRetriveResultSet.next()){
                          JSONObject requestObj = new JSONObject();
                          requestObj.put("hospitalName", hospitalRetriveResultSet.getString("hospital_name"));
                          requestObj.put("latitude", hospitalRetriveResultSet.getDouble("latitude"));
                          requestObj.put("longitude", hospitalRetriveResultSet.getDouble("longitude"));
                          requestObj.put("mobileNumber", hospitalRetriveResultSet.getString("phone_number"));
                          int hospitalId = hospitalRetriveResultSet.getInt("hospital_id");
                          if(isHospitalPresent(connection, hospitalId))
                                   requestObj.put("status", "Available");
                          else
                                  requestObj.put("status", "Service Not Available");
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
                         e.getMessage();
                    }
               }
        }
        
        private boolean isHospitalPresent(Connection connection, int hospitalId) throws Exception{
               boolean isPresence = true;
               try(PreparedStatement hospitalCheckStatement = connection.prepareStatement(SQLQueries.IS_HOSPITAL_DELETED)){
                      hospitalCheckStatement.setInt(1, hospitalId);
                      ResultSet hospitalCheckResultSet = hospitalCheckStatement.executeQuery();
                      if(hospitalCheckResultSet.next())
                           isPresence = false;
              }
              return isPresence;
        }
}
