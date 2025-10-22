/*
package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroy the session
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
*/

package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Invalidate 8080 session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 2. Delete SSO cookie from browser (8080 side)
        Cookie ssoCookie = new Cookie("SSO_TOKEN", "");
        ssoCookie.setPath("/");
        ssoCookie.setDomain("localhost");  // Must match how it was set
        ssoCookie.setMaxAge(0);
        response.addCookie(ssoCookie);
        
        // 3. Also delete JSESSIONID cookie from 8080
        Cookie jsessionCookie = new Cookie("JSESSIONID", "");
        jsessionCookie.setPath("/");
        jsessionCookie.setDomain("localhost");
        jsessionCookie.setMaxAge(0);
        response.addCookie(jsessionCookie);
        
        // 4. Call 9090 logout to clean up there too
        call9090Logout();
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect(request.getContextPath() + "/index.html?message=logout_success");
    }
    
    private void call9090Logout() {
        try {
            URL url = new URL("http://localhost:9090/logout9090");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(2000); // 2 second timeout
            conn.setReadTimeout(2000);
            
            // Make the request
            int responseCode = conn.getResponseCode();
            System.out.println("9090 logout response: " + responseCode);
            
        } catch (Exception e) {
            System.out.println("9090 logout call failed: " + e.getMessage());
            // Continue anyway - we cleaned up 8080 side
        }
    }
}
