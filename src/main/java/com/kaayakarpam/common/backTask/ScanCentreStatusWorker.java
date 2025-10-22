package com.kaayakarpam.common.backTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;

class ScanCentreStatusWorker {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public void start() {
        // Run every 30 seconds to check for deleted hospitals and cancel related scan bookings
        scheduler.scheduleAtFixedRate(this::checkAndUpdateStatuses, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
    
    private void checkAndUpdateStatuses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            cancelBookingsForDeletedHospitals(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void cancelBookingsForDeletedHospitals(Connection conn) throws SQLException {
        String sql = "UPDATE scan_bookings sb " +
                    "SET sb.isCancelled = 1 " +
                    "WHERE sb.hospital_id IN ( " +
                    "    SELECT dh.hospital_id FROM deleted_hospitals dh " +
                    ") " +
                    "AND sb.isCancelled = 0 " +
                    "AND sb.booked_date > NOW()"; //future bookings
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Cancelled " + updated + " scan bookings for deleted hospitals");
            }
        }
    }
}
