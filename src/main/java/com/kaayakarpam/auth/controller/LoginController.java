package com.kaayakarpam.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

import  java.net.URLEncoder;

import com.kaayakarpam.auth.service.OAuthService;
import com.kaayakarpam.auth.service.OAuthServiceFactory;



@WebServlet("/login")
public class LoginController extends HttpServlet{
        
      //  private final AuthService authService = new AuthServiceImpl();
        
       protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String oAuthProvider = request.getParameter("provider");
        if (oAuthProvider != null) {
            // Store the provider in session
            HttpSession session = request.getSession();
            session.setAttribute("oauth_provider", oAuthProvider);

            OAuthService oAuthService = OAuthServiceFactory.getService(oAuthProvider);
            if (oAuthService != null) {
                response.sendRedirect(oAuthService.getAuthorizationUrl());
                return;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown OAuth provider");
                return;
            }
        }
        response.sendRedirect("index.html");
    }
        
  
        
}
/*
javac -cp ".:WEB-INF/lib/json-20240303.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.servlet-api.jar"     *.java -d WEB-INF/classes
javac -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes com/kaayakarpam/auth/controller/LoginController.java

folder structure is
auth/
 ├─ controller/      
 │    ├─ LoginController.java
 │    ├─ OAuthCallbackController.java
 │    ├─ ProfileController.java
 │    └─ RegisterController.java
 ├─ service/         (business logic)
 │    ├─ AuthService.java
 │    ├─ OAuthService.java
 │    └─ impl/
 │         ├─ ManualAuthService.java
 │         └─ GoogleOAuthService.java
 ├─ dao/             (DB access)
 │    ├─ UserDAO.java
 │    └─ impl/
 │         └─ UserDAOImplementation.java
 └─ model/
      └─ User.java

front end
html 
login ->  if user choose signin with google    -> call LoginController doGet()
           ->  if user manually enter data in the form -> call LoginController doPost() 
           
doGet()
      there are lot of ways for signin (now only google) but for future
      OuthService is an interface which was implemented by GoogleOAuthService class and overrides the callback url
      the callback url points to OAuthCallbackController class
      but i cannot know what do in what class(does not know how to segregate) for maintaining solid principles
 
doPost()
    it directly comes to ManualAuthService which implements the AuthService
    and the html send emailId, and password (which was hashed by pbekeyspec algorithm) and store it in db
  
      
      
         [Login Page]
          /        \
Manual Login        OAuth Login (Google)
   |                     |
POST /login           GET /login?provider=google
   |                     |
LoginController.doPost   LoginController.doGet
   |                     |
ManualAuthService       GoogleOAuthService.getAuthorizationUrl()
   |                     |
User authenticated      Redirect to Google login page
   |                     |
Store User in session    Google login page -> user logs in
   |                     |
Redirect /profile       Google redirects /callback?code=XYZ
                         |
                     OAuthCallbackController.doGet
                         |
                GoogleOAuthService.handleCallback(code)
                         |
                 Exchange code -> Access Token
                         |
                 Fetch user info -> User object
                         |
                 Store User in session
                         |
                   Redirect /profile
                         |
                   ProfileController.doGet
                         |
                  Show user profile
*/
