package com.kaayakarpam.admin.controller;

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

@WebServlet("/scantypes")
public class ScanTypeRetriver extends HttpServlet{
         
         protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException{
                 
                 PrintWriter writer = response.getWriter();
                 response.setContentType("application/json");
                 
                 Connection connection = null;
                 try{
                       connection = DatabaseConnection.getConnection();
                       PreparedStatement scanTypeStatement = connection.prepareStatement(SQLQueries.GET_SCAN_TYPES);
                       ResultSet scanTypeResultSet = scanTypeStatement.executeQuery();
                       
                       JSONArray requestArray = new JSONArray();
                       while(scanTypeResultSet.next()){
                             JSONObject requestObj = new JSONObject();
                             requestObj.put("scanTypeId", scanTypeResultSet.getInt("scan_type_id"));
                             requestObj.put("scanTypeName", scanTypeResultSet.getString("scan_type_name"));
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
