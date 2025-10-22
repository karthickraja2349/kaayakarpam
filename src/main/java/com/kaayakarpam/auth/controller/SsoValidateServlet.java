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

@WebServlet("/validate")
public class SsoValidateServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getParameter("token");
        resp.setContentType("application/json");
        
        try {
            if (token != null && JwtUtil.validateToken(token)) {
                // Return user info as JSON
                int userId = JwtUtil.getUserId(token);
                String role = JwtUtil.getRole(token);
                String email = JwtUtil.getEmail(token);
                
                String json = String.format("{\"userId\":%d,\"role\":\"%s\",\"email\":\"%s\"}",
                                          userId, role, email);
                resp.getWriter().write(json);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
