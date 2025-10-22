package com.kaayakarpam.patient.controller;

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
import java.sql.Timestamp;

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import java.sql.Date;

@WebServlet("/hospitalList")
public class HospitalList extends HttpServlet{

         protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
                    
                  PrintWriter writer = response.getWriter();
                  response.setContentType("application/json");
                  
                  Connection connection = null;
                  try{
                       connection = DatabaseConnection.getConnection();
                       
                       String scanTypeId = request.getParameter("scanTypeId");      
                       String scanDate = request.getParameter("scanDate");
                       Date sqlDate = Date.valueOf(scanDate);
                       
                       PreparedStatement scanTypeStatement = connection.prepareStatement(SQLQueries.GET_HOSPITALS_BY_SCAN_TYPE);
                       scanTypeStatement.setDate(1, sqlDate);
                       scanTypeStatement.setInt(2, Integer.parseInt(scanTypeId));
                       ResultSet scanTypeResultSet = scanTypeStatement.executeQuery();
                       
                       JSONArray requestArray = new JSONArray();
                       
                       while(scanTypeResultSet.next()){
                              JSONObject requestObj = new JSONObject();
                              requestObj.put("hospitalId", scanTypeResultSet.getInt("hospital_id"));
                              requestObj.put("hospitalName", scanTypeResultSet.getString("hospital_name"));
                              requestObj.put("hospitalPhoneNumber", scanTypeResultSet.getString("phone_number"));
                              requestObj.put("hospitalAddress", scanTypeResultSet.getString("address"));
                              requestArray.put(requestObj);
                       }
                       writer.write(requestArray.toString());
                       
                  }
                  catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                       writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                  }
                  finally{
                        try{
                             if(connection != null)
                                   connection.close();
                        }
                        catch(Exception e){
                             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                             writer.write("{\"error\":\"" + e.getMessage() + "\"}");
                        }
                  }
         }
}
