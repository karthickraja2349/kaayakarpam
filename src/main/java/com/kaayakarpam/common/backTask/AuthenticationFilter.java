package com.kaayakarpam.common.backTask;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // This intercepts EVERY SINGLE request to your app
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
          res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);
        String requestPath = req.getRequestURI(); 
        String contextPath = req.getContextPath(); 

 
        if (isPublicResource(requestPath, contextPath)) {
    
            chain.doFilter(request, response);
            return;
        }

 
        HttpSession session = req.getSession(false); 


        if (session == null || session.getAttribute("user_id") == null) {
          //  res.sendRedirect(contextPath + "/index.html?message=login_required");
           showSessionExpiredPage(req, res, contextPath);
            return; 
        }

       
        chain.doFilter(request, response);
    }

    /**
     * This method defines the ONLY exceptions to the rule.
     * Only the login page itself and the login processing servlet are public.
     */
    private boolean isPublicResource(String path, String contextPath) {
        
        boolean isRootPath = path.equals(contextPath + "/") || path.equals(contextPath);
        boolean isLoginPage = path.equals(contextPath + "/index.html");
        boolean isPolicyPage = path.equals(contextPath + "/static/html/policy.html");
        boolean isAboutUsPage = path.equals(contextPath + "/static/html/aboutUs.html");
        boolean isRegister = path.equals(contextPath + "/register");
        boolean isForgotPasswordVerify = path.equals(contextPath + "/forgetPassword/verify");
       boolean isForgotPasswordReset = path.equals(contextPath + "/forgetPassword/reset");
        boolean isForgotPassword = path.startsWith(contextPath + "/forgetPassword");
    
       
        boolean isLoginServlet = path.equals(contextPath + "/manualLogin");
       
        
        // boolean isStaticResource = path.startsWith(contextPath + "/css/") || path.startsWith(contextPath + "/js/");
        boolean isSession =  path.equals(contextPath + "/checkSessionStatus");
         boolean isAmbulancePage = path.equals(contextPath + "/bookAmbulance.html");
        boolean isAmbulanceRequest = path.equals(contextPath +"/ambulanceRequest");
        boolean isHospitalRetriver = path.equals(contextPath + "/hospitalRetriver");

      
        return isRootPath || isLoginPage || isPolicyPage || isAboutUsPage || isLoginServlet || isSession || isAmbulancePage || isAmbulanceRequest || isRegister || isForgotPasswordVerify || isForgotPasswordReset || isForgotPassword || isHospitalRetriver; 
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
    
    private void showSessionExpiredPage(HttpServletRequest req, HttpServletResponse res, String contextPath) throws IOException {
    // Serve the session message HTML page
    String htmlContent = "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
            "<title>Session Expired - Kaayakarpam</title>" +
            "<style>" +
                "body {" +
                    "font-family: Arial, sans-serif;" +
                    "background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
                    "height: 100vh;" +
                    "display: flex;" +
                    "justify-content: center;" +
                    "align-items: center;" +
                    "margin: 0;" +
                "}" +
                ".message-container {" +
                    "background: white;" +
                    "padding: 30px;" +
                    "border-radius: 10px;" +
                    "box-shadow: 0 10px 30px rgba(0,0,0,0.2);" +
                    "text-align: center;" +
                    "max-width: 400px;" +
                "}" +
                ".icon {" +
                    "font-size: 48px;" +
                    "color: #ff4757;" +
                    "margin-bottom: 20px;" +
                "}" +
                "h2 {" +
                    "color: #333;" +
                    "margin-bottom: 15px;" +
                "}" +
                "p {" +
                    "color: #666;" +
                    "margin-bottom: 25px;" +
                    "line-height: 1.5;" +
                "}" +
                ".btn {" +
                    "background: #667eea;" +
                    "color: white;" +
                    "padding: 12px 24px;" +
                    "text-decoration: none;" +
                    "border-radius: 5px;" +
                    "font-weight: bold;" +
                    "transition: background 0.3s;" +
                "}" +
                ".btn:hover {" +
                    "background: #5a67d8;" +
                "}" +
            "</style>" +
        "</head>" +
        "<body>" +
            "<div class=\"message-container\">" +
                "<div class=\"icon\">⏰</div>" +
                "<h2>Session Expired</h2>" +
                "<p>Your session has expired due to inactivity. Please login again to continue using our services.</p>" +
                "<a href=\"" + contextPath + "/index.html\" class=\"btn\">Login Again</a>" +
            "</div>" +
            "<script>" +
                "setTimeout(function() {" +
                    "window.location.href = '" + contextPath + "/index.html';" +
                "}, 5000);" +
            "</script>" +
        "</body>" +
        "</html>";

    res.setContentType("text/html");
    res.getWriter().write(htmlContent);
}
}

/*
javac -cp "/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.servlet-api.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/json-20240303.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/mysql-connector-9.0.0.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.mail-2.0.1.jar:/home/karthick-ts476/Server/apache-tomcat-10.1.44/lib/jakarta.activation-2.0.1.jar"     -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes       com/kaayakarpam/common/db/DatabaseConnection.java com/kaayakarpam/common/db/SQLQueries.java  com/kaayakarpam/auth/controller/RegisterController.java com/kaayakarpam/common/security/PasswordEncrypter.java com/kaayakarpam/auth/controller/ManualLoginController.java com/kaayakarpam/common/service/ProfileService.java com/kaayakarpam/common/service/serviceImplementation/ManualProfileService.java com/kaayakarpam/common/web/AmbulanceRequest.java com/kaayakarpam/common/service/DistanceMeasureable.java com/kaayakarpam/common/service/DurationAnalyser.java com/kaayakarpam/common/service/serviceImplementation/DistanceCalculator.java com/kaayakarpam/common/service/serviceImplementation/DurationCalculator.java com/kaayakarpam/staff/controller/PatientRequests.java com/kaayakarpam/staff/controller/BedAllocation.java com/kaayakarpam/admin/controller/HospitalDelete.java com/kaayakarpam/common/backTask/SessionExpiryListener.java com/kaayakarpam/common/backTask/SessionExpiryFilter.java com/kaayakarpam/common/backTask/AuthenticationFilter.java com/kaayakarpam/auth/controller/ForgetPassword.java 


*/


