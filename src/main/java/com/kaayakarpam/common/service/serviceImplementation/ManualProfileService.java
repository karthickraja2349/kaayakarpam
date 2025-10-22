package com.kaayakarpam.common.service.serviceImplementation;

import com.kaayakarpam.common.service.ProfileService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/manualProfile")
public class ManualProfileService extends HttpServlet implements ProfileService {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("<h3>No active session. Please login again.</h3>");
            return;
        }

        String role = (String) session.getAttribute("role");
        int userId = (Integer) session.getAttribute("user_id");

        try {
            String profile = viewProfile(role, userId);

            response.setContentType("text/html");

            String html = """
                  <head>
                    <title>User Profile</title>
                    <style>
                      body {font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 20px; }
                      .container { max-width: 600px; margin: auto; background: #fff; padding: 20px; border-radius: 12px;
                                   box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
                      h2 { text-align: center; color: #333; margin-bottom: 20px; }
                      pre { background: #f4f4f4;color : green;font-size: 20px; padding: 15px; border-radius: 8px; white-space: pre-wrap; }
                    </style>
                  </head>
                  <body>
                    <div class='container'>
                      <h2>User Profile</h2>
                      <pre>%s</pre>
                    </div>
                  </body>
                  </html>
                  """.formatted(profile);

          response.getWriter().write(html);
     } 
      catch (Exception e) {
          e.printStackTrace();
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          response.getWriter().write("<h3>Error retrieving profile: " + e.getMessage() + "</h3>");
      }
    }
        
          @Override
         public String viewProfile(String role, int userId) throws Exception{
               StringBuilder profile = new StringBuilder();
               
              switch (role) {
                   case "admin": {
                       PreparedStatement preparedStatement = DatabaseConnection.getPreparedStatement(SQLQueries.GET_ADMIN_BY_ID);
                       preparedStatement.setInt(1, userId);
                       ResultSet resultSet = preparedStatement.executeQuery();
                       if (resultSet.next()) {
                            profile.append("-----------------Admin Profile-----------------\n")
                                   .append("Name: ").append(resultSet.getString("name")).append("\n")
                                   .append("Email: ").append(resultSet.getString("email")).append("\n")
                                   .append("Mobile: ").append(resultSet.getString("mobile_number")).append("\n")
                                   .append("Age: ").append(resultSet.getInt("age")).append("\n")
                                   .append("Gender: ").append(resultSet.getString("gender")).append("\n")
                                   .append("Address: ").append(resultSet.getString("address"));
                       }
                      resultSet.close();
                      break;
                 }
                case "patient": {
                    PreparedStatement preparedStatement = DatabaseConnection.getPreparedStatement(SQLQueries.GET_PATIENT_BY_ID);
                    preparedStatement.setInt(1, userId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                          profile.append("-----------------User Profile-----------------\n")
                                 .append("Name: ").append(resultSet.getString("patient_name")).append("\n")
                                 .append("Email: ").append(resultSet.getString("email")).append("\n")
                                 .append("Age: ").append(resultSet.getInt("age")).append("\n")
                                 .append("Gender: ").append(resultSet.getString("gender")).append("\n")
                                 .append("Address: ").append(resultSet.getString("address")).append("\n")
                                 .append("Mobile: ").append(resultSet.getString("mobile_number"));
                    }
                    resultSet.close();
                    break;
                 }
                case "staff": {
                    PreparedStatement preparedStatement = DatabaseConnection.getPreparedStatement(SQLQueries.GET_STAFF_BY_ID);
                    preparedStatement.setInt(1, userId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                         profile.append("-----------------Staff Profile-----------------\n")
                                 .append("Name: ").append(resultSet.getString("staff_name")).append("\n")
                                 .append("Email: ").append(resultSet.getString("email")).append("\n")
                                 .append("Age: ").append(resultSet.getInt("age")).append("\n")
                                 .append("Gender: ").append(resultSet.getString("gender")).append("\n")
                                 .append("Mobile: ").append(resultSet.getString("mobile_number")).append("\n")
                                 .append("Address: ").append(resultSet.getString("address"));
                    }
                    resultSet.close();
                    break;
                }
                default:
                    throw new Exception("Unknown role: " + role);
              }
             return profile.toString();
         }
}


