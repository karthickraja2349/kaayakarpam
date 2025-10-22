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

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.kaayakarpam.common.db.DatabaseConnection;

@WebServlet("/editProfile")
@MultipartConfig
public class EditProfile extends HttpServlet {
   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                    
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            writer.print("please Login again");
            return;
        }
        
        int userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");
        
        String currentMobileNumber = request.getParameter("oldMobileNumber");  
        String name = request.getParameter("name");
        String mailId = request.getParameter("newMailId");
        String mobileNumber = request.getParameter("mobileNumber");
        String address = request.getParameter("address");
        
  

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Build dynamic SQL
            StringBuilder sql = new StringBuilder("UPDATE " + role + " SET ");
            boolean first = true;

            if (name != null && !name.trim().isEmpty()) {
                sql.append(first ? "" : ", ").append(getNameColumn(role) + " = ?");
                first = false;
            }
            if (mailId != null && !mailId.trim().isEmpty()) {
                sql.append(first ? "" : ", ").append("email = ?");
                first = false;
            }
            if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
                sql.append(first ? "" : ", ").append("mobile_number = ?");
                first = false;
            }
            if (address != null && !address.trim().isEmpty()) {
                sql.append(first ? "" : ", ").append("address = ?");
                first = false;
            }

            if (first) { // no optional parameters given
                writer.print("No fields to update!");
                return;
            }

            // Use only user_id in WHERE clause, not mobile number
            sql.append(" WHERE " + getUserIdColumn(role) + " = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = 1;

                if (name != null && !name.trim().isEmpty()) {
                    ps.setString(index++, name);
                }
                if (mailId != null && !mailId.trim().isEmpty()) {
                    ps.setString(index++, mailId);
                }
                if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
                    ps.setString(index++, mobileNumber);
                }
                if (address != null && !address.trim().isEmpty()) {
                    ps.setString(index++, address);
                }

                ps.setInt(index++, userId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    writer.print("Profile updated successfully!");
                } 
                else {
                    writer.print("No record found with the given user ID.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            writer.print("Error while updating profile: " + e.getMessage());
        }
    }
    
    private String getUserIdColumn(String role) {
        switch (role) {
            case "admin": return "admin_id";
            case "patient": return "patient_id";
            case "staff": return "staff_id";
            default: return "id";
        }
    }
    
    private String getNameColumn(String role) {
        switch (role) {
            case "admin": return "name";
            case "patient": return "patient_name";
            case "staff": return "staff_name";
            default: return "name";
        }
    }         
}


