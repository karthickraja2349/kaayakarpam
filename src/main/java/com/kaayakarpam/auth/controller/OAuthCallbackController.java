package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;

import com.kaayakarpam.auth.service.OAuthService;
import com.kaayakarpam.auth.service.OAuthServiceFactory;

import com.kaayakarpam.auth.model.User;

@WebServlet("/callback")
public class OAuthCallbackController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Retrieve the provider from session (not from request parameter)
        HttpSession session = request.getSession();
        String oAuthProvider = (String) session.getAttribute("oauth_provider");

        String code = request.getParameter("code");
        if (code == null) {
            response.getWriter().println("No code found in callback!");
            return;
        }

        if (oAuthProvider != null) {
            OAuthService oAuthService = OAuthServiceFactory.getService(oAuthProvider);
            if (oAuthService == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown OAuth provider");
                return;
            }
            try {
                User user = oAuthService.handleCallback(code);
                session.setAttribute("user", user);
                response.sendRedirect("profile");
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth error: " + e.getMessage());
            }
        } 
        else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth provider not found in session.");
        }
    }
}
