package com.kaayakarpam.auth.controller; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/checkSessionStatus")
public class SessionCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false); 

    
        if (session != null && session.getAttribute("user_id") != null) {
            
            String role = (String) session.getAttribute("role");
            String email = (String) session.getAttribute("email");
            out.write("{" +
                    "\"isLoggedIn\": true," +
                    "\"role\": \"" + role + "\"," +
                    "\"email\": \"" + email + "\"" +
                    "}");
        } 
        else {
            out.write("{\"isLoggedIn\": false}");
        }
        out.close();
    }
}


