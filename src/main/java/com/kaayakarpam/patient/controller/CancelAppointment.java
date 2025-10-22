package com.kaayakarpam.patient.controller;

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

import org.json.JSONObject;
import org.json.JSONArray;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/cancelBooking")
public class CancelAppointment extends HttpServlet{
        
        protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException{
               
                PrintWriter writer = response.getWriter();
                response.setContentType("application/json");
                
                String  id = request.getParameter("id");
                int scanBookingId = Integer.parseInt(id);
                
                Connection connection = null;
                try{
                     connection = DatabaseConnection.getConnection();
                     PreparedStatement cancelBookingStatement = connection.prepareStatement(SQLQueries.CANCEL_BOOKING);
                     cancelBookingStatement.setInt(1, scanBookingId);
                     int rowsAffected = cancelBookingStatement.executeUpdate();
                     
                     if(rowsAffected > 0)
                         writer.write("{\"status\":\"success\",\"message\":\"Appointment cancelled Successfully\"}");
                     else
                        writer.write("{\"status\":\"failure\",\"message\":\"Sorry, please try again later\"}");
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
