package com.kaayakarpam.common.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import com.kaayakarpam.common.service.DistanceMeasureable;
import com.kaayakarpam.common.service.DurationAnalyser;

import com.kaayakarpam.common.service.serviceImplementation.DistanceCalculator;
import com.kaayakarpam.common.service.serviceImplementation.DurationCalculator;

import java.util.logging.Logger;

@WebServlet("/ambulanceRequest")
@MultipartConfig
public class AmbulanceRequest extends HttpServlet{

           private static final Logger logger = Logger.getLogger(AmbulanceRequest.class.getName());
           
           protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
                    
                      long startTime = System.currentTimeMillis();
                      String clientIP = request.getRemoteAddr();
                      String userAgent = request.getHeader("User-Agent");
                      
                      logger.info(String.format("🚨 REQUEST START - Thread: %s, IP: %s, Time: %s, Agent: %s", 
                          Thread.currentThread().getName(), 
                          clientIP,
                          new java.util.Date(startTime),
                          userAgent));
                          
                          
                  String location ="N/A";// request.getParameter("location");        
                   String latitude = request.getParameter("latitude");
                   String longitude = request.getParameter("longitude");
                   String emergencyType = request.getParameter("emergencyType");
                   int hospital_id = 0;
                   int ambulance_id = 0;
                   double distance_km = 0;
                   int request_id = 0;
                   
                   PrintWriter writer = response.getWriter();
                   response.setContentType("text/plain");
                   
                   Connection connection = null;
                   
                   try{
                    
                        connection = DatabaseConnection.getConnection();
                        connection.setAutoCommit(false);
                        
                        PreparedStatement findNearbyHospitalStatement = connection.prepareStatement(SQLQueries.FIND_NEARBY_HOSPITAL);
                        findNearbyHospitalStatement.setDouble(1, Double.parseDouble(latitude));
                        findNearbyHospitalStatement.setDouble(2 ,Double.parseDouble(longitude));
                        findNearbyHospitalStatement.setDouble(3, Double.parseDouble(latitude));
                        
                        ResultSet nearByHospitalResultSet = findNearbyHospitalStatement.executeQuery();
                        
                        if(nearByHospitalResultSet.next()){
                                 hospital_id = nearByHospitalResultSet.getInt(1);
                                 nearByHospitalResultSet.close();
                                 findNearbyHospitalStatement.close();
                        }
                        else{
                             writer.write("Sorry , There is no Hospital Available");
                             return;
                        }
                        
                         logger.info(String.format("🔍 Checking ambulance availability - Thread: %s, Time: %s", 
                          Thread.currentThread().getName(),
                          new java.util.Date()));
                                  
                        PreparedStatement findAvailableAmbulanceStatement = connection.prepareStatement(SQLQueries.FIND_AVAILABLE_AMBULANCE);
                        findAvailableAmbulanceStatement.setDouble(1, Double.parseDouble(latitude));
                        findAvailableAmbulanceStatement.setDouble(2, Double.parseDouble(longitude));
                        findAvailableAmbulanceStatement.setDouble(3, Double.parseDouble(latitude));
                        findAvailableAmbulanceStatement.setInt(4, hospital_id);
                        
                        ResultSet nearByAmbulanceResultSet = findAvailableAmbulanceStatement.executeQuery();
                        
                        if(nearByAmbulanceResultSet.next()){
                                 ambulance_id  = nearByAmbulanceResultSet.getInt(1);
                                 distance_km = nearByAmbulanceResultSet.getInt(2);
                                 findAvailableAmbulanceStatement.close();
                                 nearByAmbulanceResultSet.close();
                        }
                        else{
                              writer.write("Sorry , There is no Ambulance Available");
                              return;
                        }
                        
                        PreparedStatement updateAmbulanceStatement = connection.prepareStatement(SQLQueries.UPDATE_AMBULANCE_STATUS);
                        updateAmbulanceStatement.setInt(1, ambulance_id);
                        updateAmbulanceStatement.executeUpdate();
                        updateAmbulanceStatement.close();
                        
                        PreparedStatement  assignAmbulanceStatement = connection.prepareStatement(SQLQueries.ASSIGN_AMBULANCE, Statement.RETURN_GENERATED_KEYS);
                        assignAmbulanceStatement.setString(1, location);
                        assignAmbulanceStatement.setDouble(2, Double.parseDouble(latitude));
                        assignAmbulanceStatement.setDouble(3, Double.parseDouble(longitude));
                        assignAmbulanceStatement.setString(4, emergencyType.toUpperCase());
                        assignAmbulanceStatement.setInt(5, hospital_id);
                        assignAmbulanceStatement.setInt(6, ambulance_id);
                        assignAmbulanceStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                        
                        int rowsAffected = assignAmbulanceStatement.executeUpdate();
                        if (rowsAffected > 0) {
                              try (ResultSet rs = assignAmbulanceStatement.getGeneratedKeys()) {
                                  if (rs.next()) {
                                      request_id = rs.getInt(1);  
                                  }
                              }
                          }
                        logger.info(String.format("✅ Ambulance assigned - Thread: %s, Ambulance ID: %d, Time: %s", 
                                  Thread.currentThread().getName(),
                                  ambulance_id,
                                  new java.util.Date()));
                        assignAmbulanceStatement.close();
                    
                        connection.commit();
                        double estimatedMinutes = (distance_km / 40) * 60;
                         writer.write("Ambulance assigned! Estimated arrival time: " + Math.round(estimatedMinutes) + " minutes");
                         
                       // String duration =  calculate(Double.parseDouble(latitude), Double.parseDouble(longitude), hospital_id, ambulance_id);
                       double durationMinutes = calculate(Double.parseDouble(latitude), Double.parseDouble(longitude), hospital_id, ambulance_id);
                       
                       long durationMillis = (long) (durationMinutes * 60 * 1000);
                       
                        Timestamp requestTime = new Timestamp(System.currentTimeMillis() + durationMillis);
                        
                        PreparedStatement temporaryRegisterStatement = connection.prepareStatement(SQLQueries.TEMPORARY_REGISTRATION);
                        temporaryRegisterStatement.setInt(1, request_id);
                        temporaryRegisterStatement.setTimestamp(2, requestTime);
                        temporaryRegisterStatement.executeUpdate();
                        temporaryRegisterStatement.close();
                   }    
                   catch(Exception e){
                          try {
                                if(connection != null)
                                     connection.rollback();
                            } 
                            catch(SQLException ex) {
                                       logger.severe(String.format("❌ ERROR - Thread: %s, Error: %s, Time: %s", 
                              Thread.currentThread().getName(),
                              ex.getMessage(),
                              new java.util.Date()));
                                ex.printStackTrace();
                            }
                            writer.write("Error: " + e.getMessage());
                            e.printStackTrace();
                  }
                  finally {
                            try {
                                if(connection != null) {
                                    connection.setAutoCommit(true);
                                    connection.close();
                                }
                            } 
                            catch(SQLException e) {
                                e.printStackTrace();
                            }
                }
           }
           
        private double calculate(double latitude, double longitude, int hospital_id, int ambulance_id) throws Exception {
                DistanceMeasureable distancecalculator = new DistanceCalculator();
                DurationAnalyser durationCalculator = new DurationCalculator();
                
                Connection connection = null;   
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                
                try {
                    connection = DatabaseConnection.getConnection();
                    statement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_GEO_LOCATION);
                    statement.setInt(1, hospital_id);
                    
                    resultSet = statement.executeQuery();
                  
                    if (resultSet.next()) {
                        double hospitalLatitude = resultSet.getDouble(1);
                        double hospitalLongitude = resultSet.getDouble(2);
                        
                        double totalDistance = distancecalculator.calculateDistance(latitude, longitude, hospitalLatitude, hospitalLongitude);
                        double totalDuration = durationCalculator.calculateDuration(totalDistance, 45.0);
                        //return DurationCalculator.formatDuration(totalDuration);
                        return totalDuration;
                    } 
                    else {
                        throw new Exception("Hospital location data not found");
                    }
                } 
                catch(Exception e) {
                   throw new Exception(e.getMessage());
                } 
                finally {
                    try {
                        if (resultSet != null) resultSet.close();
                        if (statement != null) statement.close();
                        if (connection != null) connection.close();
                    } 
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
               }
        }
}

/*

javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.servlet-api.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/json-20240303.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-9.0.0.jar"     -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes       com/kaayakarpam/common/db/DatabaseConnection.java com/kaayakarpam/common/db/SQLQueries.java  com/kaayakarpam/auth/controller/RegisterController.java com/kaayakarpam/common/security/PasswordEncrypter.java com/kaayakarpam/auth/controller/ManualLoginController.java com/kaayakarpam/common/service/ProfileService.java com/kaayakarpam/common/service/serviceImplementation/ManualProfileService.java com/kaayakarpam/common/web/AmbulanceRequest.java com/kaayakarpam/common/service/DistanceMeasureable.java com/kaayakarpam/common/service/DurationAnalyser.java com/kaayakarpam/common/service/serviceImplementation/DistanceCalculator.java com/kaayakarpam/common/service/serviceImplementation/DurationCalculator.java com/kaayakarpam/staff/controller/PatientRequests.java com/kaayakarpam/staff/controller/BedAllocation.java com/kaayakarpam/admin/controller/HospitalDelete.java com/kaayakarpam/common/backTask/SessionExpiryListener.java com/kaayakarpam/common/backTask/SessionExpiryFilter.java com/kaayakarpam/common/backTask/AuthenticationFilter.java com/kaayakarpam/common/backTask/AmbulanceStatusWorker.java com/kaayakarpam/staff/controller/BedAllocation.java 
*/


