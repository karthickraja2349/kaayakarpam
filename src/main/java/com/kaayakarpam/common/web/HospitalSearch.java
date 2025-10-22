package com.kaayakarpam.common.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/searchHospital")
@MultipartConfig
public class HospitalSearch extends HttpServlet{
        
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
                 
                 response.setContentType("application/json");
                 PrintWriter writer = response.getWriter();
                 
                 String name = request.getParameter("name");
                 String location = request.getParameter("location");
                 String freebeds = request.getParameter("freebeds");
                 String icu = request.getParameter("icu");
                 String ambulance = request.getParameter("ambulance");
                 
                 try{
                      StringBuilder query = new StringBuilder(SQLQueries.SEARCH_HOSPITAL);
                      boolean isName = name != null && !name.isEmpty();
                      boolean isLocation = location != null && !location.isEmpty();
                      boolean isFreeBeds = freebeds != null && !freebeds.isEmpty();
                      boolean isIcu = icu != null && !icu.isEmpty();
                      boolean isAmbulance = ambulance !=null && !ambulance.isEmpty();
                       if(isName)
                           query.append(" AND hospital_name LIKE  ?");
                       if(isLocation)
                           query.append(" AND location LIKE  ?");
                        if(isFreeBeds)
                            query.append(" AND free_general_beds >= ?");
                        if(isIcu)
                            query.append(" AND free_icu_beds >= ?");
                         if(isAmbulance)
                            query.append(" AND ambulance_count <= ?");
                            
                       PreparedStatement preparedStatement = DatabaseConnection.getPreparedStatement(query.toString());
                       int index = 1;
                       
                       if(isName)
                             preparedStatement.setString(index++, "%" +name+"%");
                       if(isLocation)
                            preparedStatement.setString(index++, "%" +location+"%");
                       if(isFreeBeds)
                            preparedStatement.setInt(index++, Integer.parseInt(freebeds));
                       if(isIcu)
                            preparedStatement.setInt(index++, Integer.parseInt(icu));
                       if(isAmbulance)
                            preparedStatement.setInt(index++, Integer.parseInt(ambulance));
                       
                       ResultSet resultSet = preparedStatement.executeQuery();
                       JSONArray hospitals = new JSONArray();

                        while (resultSet.next()) {
                            JSONObject hospital = new JSONObject();
                            
                            hospital.put("name", resultSet.getString("hospital_name"));
                            hospital.put("location", resultSet.getString("location"));
                            hospital.put("address", resultSet.getString("address"));
                            hospital.put("contact", resultSet.getString("phone_number"));
                            hospital.put("freebeds", resultSet.getInt("free_general_beds"));
                            hospital.put("icu", resultSet.getInt("free_icu_beds"));
                            hospital.put("ambulance", resultSet.getInt("ambulance_count"));
                            hospitals.put(hospital);
                        }
                        
                         writer.print(hospitals.toString());     
                 }
                catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writer.print("{\"error\":\"Something went wrong\"}");
                    writer.print(e.getMessage());
            }
        }
}
