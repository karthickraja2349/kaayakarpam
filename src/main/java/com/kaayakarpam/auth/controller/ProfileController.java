package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;

import com.kaayakarpam.auth.model.User;

@WebServlet("/profile")
public class ProfileController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");
        response.setContentType("text/plain");
        response.getWriter().println("Name: " + user.getName());
        response.getWriter().println("Email: " + user.getEmail());
        response.getWriter().println("Phone: " + (user.getPhone() != null ? user.getPhone() : "Not provided"));
    }
}

