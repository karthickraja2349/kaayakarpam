package com.kaayakarpam.auth.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import com.kaayakarpam.common.security.PasswordEncrypter;

@WebServlet("/register")
@MultipartConfig
public class RegisterController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("role");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();

        try {
            // Password Hash + Salt
            byte[] salt = PasswordEncrypter.generateSalt();
            String encodedSalt = PasswordEncrypter.encodeSalt(salt);
            String hashedPassword = PasswordEncrypter.hashPassword(password, salt);

            int generatedId = -1;

            switch (role) {
                case "admin": {
                    String name = request.getParameter("name");
                    String mobile = request.getParameter("mobile_number");
                    int age = Integer.parseInt(request.getParameter("age"));
                    String gender = request.getParameter("gender");
                    String address = request.getParameter("address");

                    // Insert into admin
                    PreparedStatement ps1 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_ADMIN,
                            java.sql.Statement.RETURN_GENERATED_KEYS);
                    ps1.setString(1, name);
                    ps1.setString(2, email);
                    ps1.setString(3, mobile);
                    ps1.setInt(4, age);
                    ps1.setString(5, gender);
                    ps1.setString(6, address);
                    ps1.executeUpdate();

                    ResultSet rs = ps1.getGeneratedKeys();
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                    rs.close();

                    // Insert credentials
                    PreparedStatement ps2 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_ADMIN_CRED);
                    ps2.setInt(1, generatedId);
                    ps2.setString(2, hashedPassword);
                    ps2.setString(3, encodedSalt);
                    ps2.executeUpdate();

                    break;
                }
                case "patient": {
                    String name = request.getParameter("name");
                    int age = Integer.parseInt(request.getParameter("age"));
                    String gender = request.getParameter("gender");
                    String address = request.getParameter("address");
                    String mobile = request.getParameter("mobile_number");

                    PreparedStatement ps1 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_PATIENT,
                            java.sql.Statement.RETURN_GENERATED_KEYS);
                    ps1.setString(1, name);
                    ps1.setInt(2, age);
                    ps1.setString(3, gender);
                    ps1.setString(4, address);
                    ps1.setString(5, mobile);
                    ps1.executeUpdate();

                    ResultSet rs = ps1.getGeneratedKeys();
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                    rs.close();

                    PreparedStatement ps2 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_PATIENT_CRED);
                    ps2.setInt(1, generatedId);
                    ps2.setString(2, email);
                    ps2.setString(3, hashedPassword);
                    ps2.setString(4, encodedSalt);
                    ps2.executeUpdate();

                    break;
                }
                case "staff": {
                    String name = request.getParameter("name");
                    int age = Integer.parseInt(request.getParameter("age"));
                    String gender = request.getParameter("gender");
                    int isOnDuty = 1; 
                    String mobile = request.getParameter("mobile_number");
                    String address = request.getParameter("address");
                    String hospitalPhoneNumber = request.getParameter("hospitalPhoneNumber");
                    
                    int hospitalId = getHospitalId(hospitalPhoneNumber);

                    PreparedStatement ps1 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_STAFF,
                            java.sql.Statement.RETURN_GENERATED_KEYS);
                    ps1.setInt(1, hospitalId);
                    ps1.setString(2, name);
                    ps1.setInt(3, age);
                    ps1.setString(4, gender);
                    ps1.setInt(5, isOnDuty);
                    ps1.setString(6, mobile);
                    ps1.setString(7, address);
                    ps1.executeUpdate();

                    ResultSet rs = ps1.getGeneratedKeys();
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                    rs.close();

                    PreparedStatement ps2 = DatabaseConnection.getPreparedStatement(SQLQueries.INSERT_STAFF_CRED);
                    ps2.setInt(1, generatedId);
                    ps2.setString(2, email);
                    ps2.setString(3, hashedPassword);
                    ps2.setString(4, encodedSalt);
                    ps2.executeUpdate();

                    break;
                }
                default:
                    writer.write("invalid_role");
                    return;
            }

            writer.write("success");

        }
        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            String errorMessage = e.getMessage();
            if (errorMessage.contains("for key 'admin.mobile_number'")) {
                errorMessage = "Mobile Number already exists";
            } 
            else if (errorMessage.contains("for key 'admin.email'")) {
                errorMessage = "Email already exists";
            }
            else if (errorMessage.contains("for key 'staff.email'")) {
                errorMessage = "Email already exists";
            }
            else if (errorMessage.contains("for key 'staff.mobile_number'")) {
                errorMessage =  "Mobile Number already exists";
            }
            else {
                errorMessage = "Please ensure with your details and Try Later!";
            }
            response.getWriter().write("Registration failed: " + e.getMessage());
      }
    }
    
    private int getHospitalId(String phoneNumber)throws Exception{
          int hospitalId  = 0;
           try{ 
              PreparedStatement getHospitalIdStatement = DatabaseConnection.getPreparedStatement(SQLQueries.GET_HOSPITAL_ID);
              getHospitalIdStatement.setString(1, phoneNumber);
              ResultSet hospitalIdResultSet = getHospitalIdStatement.executeQuery();
              if(hospitalIdResultSet.next())
                  hospitalId = hospitalIdResultSet.getInt(1);
           }
           catch(Exception e){
              e.printStackTrace();
           }
           return hospitalId;
    }
}

