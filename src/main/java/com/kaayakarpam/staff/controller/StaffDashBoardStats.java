package com.kaayakarpam.staff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

@WebServlet("/staffDashboardStats")
public class StaffDashBoardStats extends HttpServlet {

          @Override
          protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
              response.setContentType("application/json");
              PrintWriter writer = response.getWriter();

              HttpSession session = request.getSession(false);
              if (session == null || session.getAttribute("user_id") == null) {
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  writer.write("{\"error\": \"No active session. Please login again.\"}");
                  return;
              }

              int userId = (Integer) session.getAttribute("user_id");

              try (Connection connection = DatabaseConnection.getConnection()) {
                  int totalInpatients = findTotalInPatients(connection, userId);
                  int totalPendingRequests = findTotalPendingRequests(connection, userId);

                  writer.write("{\"patients\": " + totalInpatients +
                               ", \"requests\": " + totalPendingRequests + "}");
              } 
              catch (Exception e) {
                  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  writer.write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
              }
          }

          private int findTotalInPatients(Connection connection, int userId) throws SQLException {
              int hospitalId = findHospitalId(connection, userId);
              try (PreparedStatement totalPatientStatement = connection.prepareStatement(SQLQueries.TOTAL_INPATIENT_IN_HOSPITAL)) {
                  totalPatientStatement.setInt(1, hospitalId);
                  try (ResultSet totalPatientResultSet = totalPatientStatement.executeQuery()) {
                      return totalPatientResultSet.next() ? totalPatientResultSet.getInt(1) : 0;
                  }
              }
          }

        private int findTotalPendingRequests(Connection connection, int userId) throws SQLException {
    int hospitalId = findHospitalId(connection, userId);
    try (PreparedStatement totalRequestStatement = connection.prepareStatement(SQLQueries.PENDING_REQUEST_IN_HOSPITAL)) {
        totalRequestStatement.setInt(1, hospitalId);
        totalRequestStatement.setInt(2, hospitalId);
        try (ResultSet totalRequestResultSet = totalRequestStatement.executeQuery()) {
            return totalRequestResultSet.next() ? totalRequestResultSet.getInt(1) : 0;
        }
    }
}

          private int findHospitalId(Connection connection, int userId) throws SQLException {
              try (PreparedStatement hospitalIdStatement = connection.prepareStatement(SQLQueries.GET_HOSPITAL_ID_BY_STAFF_ID)) {
                  hospitalIdStatement.setInt(1, userId);
                  try (ResultSet hospitalIdResultSet = hospitalIdStatement.executeQuery()) {
                      return hospitalIdResultSet.next() ? hospitalIdResultSet.getInt(1) : 0;
                  }
              }
          }
      }

