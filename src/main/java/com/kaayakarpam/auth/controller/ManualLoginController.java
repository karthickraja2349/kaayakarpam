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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.annotation.WebServlet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

import com.kaayakarpam.common.security.PasswordEncrypter;

@WebServlet("/manualLogin")
public class ManualLoginController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

        String role = request.getParameter("role");
        String email = request.getParameter("mailId");
        String password = request.getParameter("password");

        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // Prevent caching
        response.setHeader("Pragma", "no-cache"); // Older browsers
        response.setDateHeader("Expires", 0);     // Expired immediately
        PrintWriter writer = response.getWriter();

        try {
            String query;
            switch (role) {
                case "admin":
                    query = SQLQueries.VERIFY_ADMIN;
                    break;
                case "patient":
                    query = SQLQueries.VERIFY_PATIENT;
                    break;
                case "staff":
                    query = SQLQueries.VERIFY_STAFF;
                    break;
                default:
                    writer.write("invalid_role");
                    return;
            }

            PreparedStatement preparedStatement = DatabaseConnection.getPreparedStatement(query);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String storedHashPassword = resultSet.getString("password_hash");
                String saltedPassword = resultSet.getString("password_salt");
                String userEmail = resultSet.getString("email");

                // Hash entered password with stored salt
                byte[] saltBytes = PasswordEncrypter.decodeSalt(saltedPassword);
                String hashedPassword = PasswordEncrypter.hashPassword(password, saltBytes);

                if (hashedPassword.equals(storedHashPassword)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("role", role);
                    session.setAttribute("user_id", userId);
                    session.setAttribute("email", userEmail);
                    session.setMaxInactiveInterval(30 * 60); // 30 min
                   //writer.write("success," + userId);
                   
                   String token = JwtUtil.generateToken(userId, role, userEmail, 30);
                   session.setAttribute("authToken", token);
                   Cookie ssoCookie = new Cookie("SSO_TOKEN", token);
                    ssoCookie.setHttpOnly(true);
                    ssoCookie.setPath("/");               //  root path
                    ssoCookie.setDomain("localhost"); 
                    ssoCookie.setMaxAge(30 * 60);        // 30 minutes
                    // ssoCookie.setSecure(true);
                    response.addCookie(ssoCookie);
                    
                   if(role.equals("admin")){
                       response.sendRedirect("src/main/java/com/kaayakarpam/admin/jsp/adminView.jsp");
                       //response.sendRedirect(request.getContextPath() + "/admin/adminView.jsp");
                   }
                   else if(role.equals("patient")){
                     //    response.sendRedirect("src/main/java/com/kaayakarpam/patient/jsp/patientView.jsp");
                        // response.sendRedirect("http://localhost:9090/patientView.jsp");
                        // Set cookie with proper domain first
                   /*   Cookie ssoCookie = new Cookie("SSO_TOKEN", token);
                      ssoCookie.setHttpOnly(true);
                      ssoCookie.setPath("/");
                      ssoCookie.setDomain("localhost");
                      ssoCookie.setMaxAge(30 * 60);
                      response.addCookie(ssoCookie);*/
                   }
                   else if(role.equals("staff")){
                        response.sendRedirect("src/main/java/com/kaayakarpam/staff/jsp/staffView.jsp");
                   }
                } 
                else {
                    writer.write("invalid_password");
                }
            } 
            else {
                writer.write("not_found");
            }

        } 
        catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Login failed due to server error: " + e.getMessage());
        }
    }
}
