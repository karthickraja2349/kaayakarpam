package com.kaayakarpam.common.backTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.Date;

import com.kaayakarpam.common.db.DatabaseConnection;
import com.kaayakarpam.common.db.SQLQueries;
/*
class AmbulanceStatusWorker{
         
         private final ScheduledExecutorService scheduler  = Executors.newSingleThreadScheduledExecutor();
         
          public void start() {
                // Run every 30 seconds to check for status updates
              scheduler.scheduleAtFixedRate(this::checkAndUpdateStatuses, 0, 30, TimeUnit.SECONDS);
          }
    
          public void stop() {
              scheduler.shutdown();
          }
          
           private void checkAndUpdateStatuses() {
                     try (Connection conn = DatabaseConnection.getConnection()) {
                          updateExpiredRequests(conn);
                          updateCompletedRequests(conn);
                          updateBedAllocationStatus(conn);
                     }
                     catch (SQLException e) {
                         e.printStackTrace();
                     }
            }
            
      private void updateExpiredRequests(Connection conn) throws SQLException {
                  String sql = "UPDATE ambulance_request ar " +
                    "JOIN ambulance a ON ar.ambulance_id = a.ambulance_id " +
                    "SET ar.status = 'EXPIRED', a.is_available = 1 " +
                    "WHERE ar.status = 'PENDING' " +
                    "AND ar.created_at < NOW() - INTERVAL 5 MINUTE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Marked " + updated + " requests as EXPIRED");
            }
        }
    }
    
    private void updateCompletedRequests(Connection conn) throws SQLException {
        // Simple approach: mark as completed 1 hour after allocation
        String sql = "UPDATE ambulance_request ar " +
                    "JOIN ambulance a ON ar.ambulance_id = a.ambulance_id " +
                    "SET ar.status = 'COMPLETED', a.is_available = 1 " +
                    "WHERE ar.status = 'ALLOCATED' " +
                    "AND ar.created_at < NOW() - INTERVAL 1 MINUTE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Marked " + updated + " requests as COMPLETED");
            }
        }
    }
    
    private void updateBedAllocationStatus(Connection conn) throws SQLException {

          String sql = "UPDATE patient_temporary_requests ptr " +
                     "JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                     "SET ptr.isBedAllocated = 1 " +
                     "WHERE ptr.request_time < NOW() - INTERVAL 12 MINUTE " +
                     "AND ptr.isBedAllocated = 0 " +
                     "AND ar.request_type != 'BOOK'";  // Skip BOOK requests
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Updated " + updated + " temporary requests with bed allocation status");
            }
        }
    }
            
            

}
*/

class AmbulanceStatusWorker {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public void start() {
        // Run every 30 seconds to check for status updates
        scheduler.scheduleAtFixedRate(this::checkAndUpdateStatuses, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
    
    private void checkAndUpdateStatuses() {
     System.out.println("=== Worker running at: " + new Date() + " ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateExpiredRequests(conn);
            updateCompletedRequests(conn);
            updateBedAllocationStatus(conn);
            updateTransferRequestStatus(conn); //  method for transfr requests
             conn.setAutoCommit(false);
          updateBedCountsForDischargedPatients(conn);
          conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateExpiredRequests(Connection conn) throws SQLException {
        String sql = "UPDATE ambulance_request ar " +
                    "JOIN ambulance a ON ar.ambulance_id = a.ambulance_id " +
                    "SET ar.status = 'EXPIRED', a.is_available = 1 " +
                    "WHERE ar.status = 'PENDING' " +
                    "AND ar.created_at < NOW() - INTERVAL 5 MINUTE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Marked " + updated + " ambulance requests as EXPIRED");
            }
        }
    }
    
    private void updateCompletedRequests(Connection conn) throws SQLException {
        // Mark as completed 1 hour after allocation
        String sql = "UPDATE ambulance_request ar " +
                    "JOIN ambulance a ON ar.ambulance_id = a.ambulance_id " +
                    "SET ar.status = 'COMPLETED', a.is_available = 1 " +
                    "WHERE ar.status = 'ALLOCATED' " +
                    "AND ar.created_at < NOW() - INTERVAL 1 HOUR";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Marked " + updated + " ambulance requests as COMPLETED");
            }
        }
    }
    /*
    private void updateBedAllocationStatus(Connection conn) throws SQLException {
        // For regular ambulance requests (not transfers)
        String sql = "UPDATE patient_temporary_requests ptr " +
                    "JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                    "SET ptr.isBedAllocated = 1 " +
                    "WHERE ptr.request_time < NOW() - INTERVAL 12 MINUTE " +
                    "AND ptr.isBedAllocated = 0 " +
                    "AND ar.request_type != 'BOOK'" +
                    "AND ptr.ambulance_request_id IS NOT NULL";  // Only regular ambulance requests
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Auto-allocated beds for " + updated + " regular ambulance requests");
            }
            insertIntoInpatientDetails(conn, false);
        }
    }
    */
    private void updateBedAllocationStatus(Connection conn) throws SQLException {
    // For regular ambulance requests (not transfers)
    String sql = "UPDATE patient_temporary_requests ptr " +
                "JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                "SET ptr.isBedAllocated = 1 , ptr.isProcessed = 1 " +
                "WHERE ptr.request_time < NOW() - INTERVAL 12 MINUTE " +
                "AND ptr.isBedAllocated = 0 " +
                "AND ar.request_type != 'BOOK' " +
                "AND ar.status IN ('ALLOCATED', 'COMPLETED','EXPIRED') " + 
                "AND ptr.ambulance_request_id IS NOT NULL";  // Only regular ambulance requests
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int updated = stmt.executeUpdate();
        if (updated > 0) {
            System.out.println("Auto-allocated beds for " + updated + " regular ambulance requests");
        }
        insertIntoInpatientDetails(conn, false);
        updateHospitalBedsForRegularRequests(conn); 
    }
}
    
    private void updateTransferRequestStatus(Connection conn) throws SQLException {
        // For transfer requests (ambulance_request_id is NULL)
        // Auto-allocate beds for transfer requests after a certain time
        String sql = "UPDATE patient_temporary_requests ptr " +
                    "SET ptr.isBedAllocated = 1 , ptr.isProcessed = 1 " +
                    "WHERE ptr.request_time < NOW() - INTERVAL 15 MINUTE " +
                    "AND ptr.isBedAllocated = 0 " +
                    "AND ptr.ambulance_request_id IS NULL";  // Only transfer requests
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Auto-allocated beds for " + updated + " transfer requests");
                 insertIntoInpatientDetails(conn, true);
                // Also update hospital bed counts for transfer requests
                updateHospitalBedsForTransfers(conn);
            }
        }
    }
    
    private void updateHospitalBedsForTransfers(Connection conn) throws SQLException {
        // Update hospital bed counts for completed transfer requests
        String sql = "UPDATE hospital_facilities hf " +
                    "JOIN patient_temporary_requests ptr ON hf.hospital_id = ptr.transferred_to " +
                    "SET hf.free_general_beds = hf.free_general_beds - 1 " +
                    "WHERE ptr.isBedAllocated = 1 " +
                    "AND ptr.ambulance_request_id IS NULL " +  // Transfer requests only
                    "AND ptr.request_time < NOW() - INTERVAL 15 MINUTE " +
                    "AND hf.free_general_beds > 0 " +
                    "AND NOT EXISTS ( " +
                    "   SELECT 1 FROM bed_allocation_audit baa " +
                    "   WHERE baa.temporary_request_id = ptr.temporary_request_id" +
                    ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Updated bed counts for " + updated + " hospitals due to transfer requests");
                
                // Create audit records to prevent duplicate processing
                createBedAllocationAudit(conn);
            }
        }
    }
    
      private void createBedAllocationAudit(Connection conn) throws SQLException {
        // Create audit records for ANY processed request not yet in the audit table.
        // This method is now generic and works for both regular and transfer requests.
        String sql = "INSERT INTO bed_allocation_audit (temporary_request_id, processed_at) " +
                    "SELECT ptr.temporary_request_id, NOW() " +
                    "FROM patient_temporary_requests ptr " +
                    "WHERE ptr.isProcessed = 1 " + // Find all processed requests
                    "AND NOT EXISTS ( " +
                    "   SELECT 1 FROM bed_allocation_audit baa " +
                    "   WHERE baa.temporary_request_id = ptr.temporary_request_id" +
                    ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                System.out.println("Created " + inserted + " new bed allocation audit records.");
            }
        }
    }
 /*   
    private void createBedAllocationAudit(Connection conn) throws SQLException {
        // Create audit records for processed transfer requests
        String sql = "INSERT INTO bed_allocation_audit (temporary_request_id, processed_at) " +
                    "SELECT ptr.temporary_request_id, NOW() " +
                    "FROM patient_temporary_requests ptr " +
                    "WHERE ptr.isBedAllocated = 1 " +
                    "AND ptr.ambulance_request_id IS NULL " +
                    "AND ptr.request_time < NOW() - INTERVAL 15 MINUTE " +
                    "AND NOT EXISTS ( " +
                    "   SELECT 1 FROM bed_allocation_audit baa " +
                    "   WHERE baa.temporary_request_id = ptr.temporary_request_id" +
                    ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                System.out.println("Created audit records for " + inserted + " transfer requests");
            }
        }
    }
    */
   /* 
    private void insertIntoInpatientDetails(Connection conn, boolean isTransfer) throws SQLException {
    String sql = "INSERT INTO inpatient_details (" +
                "patient_name, patient_aadhar_number, patient_condition, " +
                "reason, booked_date, hospital_id, isDischarged" + // Removed roomNo
                ") " +
                "SELECT " +
                "p.patient_name, p.patient_aadhar_number, p.patient_condition, " +
                "p.reason, NOW(), " + // Use current timestamp for booked_date
                "CASE WHEN ? THEN ptr.transferred_to ELSE ar.hospital_id END, " + // hospital_id
                "0 " + // isDischarged = false
                "FROM patient_temporary_requests ptr " +
                "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                "LEFT JOIN patient p ON ar.request_id = p.ambulance_request_id " + // Assuming patient table exists
                "WHERE ptr.isBedAllocated = 1 " +
                "AND ptr.request_time < NOW() - INTERVAL " + (isTransfer ? "15" : "12") + " MINUTE " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM inpatient_details ipd " +
                "   WHERE ipd.patient_aadhar_number = p.patient_aadhar_number " +
                "   AND ipd.isDischarged = 0" + // Avoid duplicate active admissions
                ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setBoolean(1, isTransfer);
        int inserted = stmt.executeUpdate();
        if (inserted > 0) {
            System.out.println("Inserted " + inserted + " patients into inpatient_details");
        }
    }
}
*/

/*
private void insertIntoInpatientDetails(Connection conn, boolean isTransfer) throws SQLException {
    String sql = "INSERT INTO inpatient_details (" +
                "temporary_request_id, patient_name, patient_aadhar_number, patient_condition, " +
                "reason, booked_date, hospital_id, isDischarged" +
                ") " +
                "SELECT " +
                "ptr.temporary_request_id, p.patient_name, NULL, NULL, " +
                "NULL, NOW(), " +
                "CASE WHEN ? THEN ptr.transferred_to ELSE ar.hospital_id END, " +
                "0 " +
                "FROM patient_temporary_requests ptr " +
                "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                "LEFT JOIN patient p ON p.patient_id = ptr.admitted_by " +
                "WHERE ptr.isBedAllocated = 1 " +
                "AND ptr.request_time < NOW() - INTERVAL " + (isTransfer ? "15" : "12") + " MINUTE " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM inpatient_details ipd " +
                "   WHERE ipd.temporary_request_id = ptr.temporary_request_id " +
                ")";
                

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setBoolean(1, isTransfer);
        int inserted = stmt.executeUpdate();
        if (inserted > 0) {
            System.out.println("Inserted " + inserted + " patients into inpatient_details");
        }
    }
}
*/

private void insertIntoInpatientDetails(Connection conn, boolean isTransfer) throws SQLException {
    String sql = "INSERT INTO inpatient_details (" +
                "temporary_request_id, patient_name, patient_aadhar_number, patient_condition, " +
                "reason, booked_date, hospital_id, isDischarged" +
                ") " +
                "SELECT " +
                "ptr.temporary_request_id, p.patient_name, NULL, NULL, " +
                "NULL, NOW(), " +
                "CASE WHEN ? THEN ptr.transferred_to ELSE ar.hospital_id END, " +
                "0 " +
                "FROM patient_temporary_requests ptr " +
                "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
                "LEFT JOIN patient p ON p.patient_id = ptr.admitted_by " +
                "WHERE ptr.isBedAllocated = 1 " +
                "AND ptr.isProcessed = 0 " + // Only unprocessed requests
                "AND ptr.request_time < NOW() - INTERVAL " + (isTransfer ? "15" : "12") + " MINUTE " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM inpatient_details ipd " +
                "   WHERE ipd.temporary_request_id = ptr.temporary_request_id " +
                ")";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setBoolean(1, isTransfer);
        int inserted = stmt.executeUpdate();
        if (inserted > 0) {
            System.out.println("Inserted " + inserted + " patients into inpatient_details");
            // Mark processed
            String updateProcessed = "UPDATE patient_temporary_requests SET isProcessed = 1 WHERE isBedAllocated = 1 AND isProcessed = 0 AND request_time < NOW() - INTERVAL " + (isTransfer ? "15" : "12") + " MINUTE";
            try (PreparedStatement updStmt = conn.prepareStatement(updateProcessed)) {
                updStmt.executeUpdate();
            }
        }
    }
}
/*
private void updateBedCountsForDischargedPatients(Connection conn) throws SQLException {
    System.out.println("Checking for discharged patients...");
    
    // First mark patients as discharged
    int marked = markPatientsAsDischarged(conn);
    if (marked > 0) {
        System.out.println("Marked " + marked + " patients as discharged");
        
        // Update bed counts for each discharged patient
        String sql = "UPDATE hospital_facilities hf " +
                    "SET hf.free_general_beds = hf.free_general_beds + 1 " +
                    "WHERE hf.hospital_id IN ( " +
                    "   SELECT ipd.hospital_id " +
                    "   FROM inpatient_details ipd " +
                    "   WHERE ipd.isDischarged = 1 " +
                    "   AND ipd.discharge_date <= NOW() " +
                    "   AND NOT EXISTS ( " +
                    "       SELECT 1 FROM bed_discharge_audit bda " +
                    "       WHERE bda.inpatient_id = ipd.inpatient_id" +
                    "   )" +
                    ") " +
                    "AND hf.free_general_beds < hf.total_general_beds";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Freed " + updated + " beds for discharged patients");
                createBedDischargeAudit(conn);
            } else {
                System.out.println("No beds were freed - check audit table or bed limits");
            }
        }
    } else {
        System.out.println("No patients to mark as discharged");
    }
}
*/
/*
private void updateBedCountsForDischargedPatients(Connection conn) throws SQLException {
    // First, mark patients as discharged
    String markDischargedSql = "UPDATE inpatient_details " +
                             "SET isDischarged = 1 " +
                             "WHERE discharge_date IS NOT NULL " +
                             "AND discharge_date <= NOW() " +
                             "AND isDischarged = 0 " +
                             "AND NOT EXISTS ( " +
                             "   SELECT 1 FROM bed_discharge_audit bda " +
                             "   WHERE bda.inpatient_id = inpatient_details.inpatient_id" +
                             ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(markDischargedSql)) {
        int marked = stmt.executeUpdate();
    }

    // Then update bed counts
    String updateBedsSql = "UPDATE hospital_facilities hf " +
                          "JOIN inpatient_details ipd ON hf.hospital_id = ipd.hospital_id " +
                          "SET hf.free_general_beds = hf.free_general_beds + 1 " +
                          "WHERE ipd.isDischarged = 1 " +
                          "AND ipd.discharge_date <= NOW() " +
                          "AND hf.free_general_beds < hf.total_general_beds " +
                          "AND NOT EXISTS ( " +
                          "   SELECT 1 FROM bed_discharge_audit bda " +
                          "   WHERE bda.inpatient_id = ipd.inpatient_id" +
                          ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(updateBedsSql)) {
        int updated = stmt.executeUpdate();
        
        if (updated > 0) {
            // Create audit records
            String auditSql = "INSERT INTO bed_discharge_audit (inpatient_id, processed_at) " +
                            "SELECT ipd.inpatient_id, NOW() " +
                            "FROM inpatient_details ipd " +
                            "WHERE ipd.isDischarged = 1 " +
                            "AND ipd.discharge_date <= NOW() " +
                            "AND NOT EXISTS ( " +
                            "   SELECT 1 FROM bed_discharge_audit bda " +
                            "   WHERE bda.inpatient_id = ipd.inpatient_id" +
                            ")";
            
            try (PreparedStatement auditStmt = conn.prepareStatement(auditSql)) {
                auditStmt.executeUpdate();
            }
        }
    }
}

*/
private void updateBedCountsForDischargedPatients(Connection conn) throws SQLException {
    // First, mark patients as discharged and free their beds in one operation
    String sql = "UPDATE inpatient_details ipd " +
                "JOIN hospital_facilities hf ON ipd.hospital_id = hf.hospital_id " +
                "SET ipd.isDischarged = 1, " +
                "    hf.free_general_beds = hf.free_general_beds + 1 " +
                "WHERE ipd.discharge_date IS NOT NULL " +
                "AND ipd.discharge_date <= NOW() " +
                "AND ipd.isDischarged = 0 " +
                "AND hf.free_general_beds < hf.total_general_beds " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM bed_discharge_audit bda " +
                "   WHERE bda.inpatient_id = ipd.inpatient_id" +
                ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int updated = stmt.executeUpdate();
        if (updated > 0) {
            System.out.println("Freed " + updated + " beds for discharged patients");
            createBedDischargeAudit(conn);
        }
    }
}
private int markPatientsAsDischarged(Connection conn) throws SQLException {
    System.out.println("=== markPatientsAsDischarged() called ===");
    
    // Use direct datetime comparison instead of DATE() function
    String sql = "UPDATE inpatient_details " +
                "SET isDischarged = 1 " +
                "WHERE discharge_date IS NOT NULL " +
                "AND discharge_date <= NOW() " +  // Compare full datetime
                "AND isDischarged = 0 " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM bed_discharge_audit bda " +
                "   WHERE bda.inpatient_id = inpatient_details.inpatient_id" +
                ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int result = stmt.executeUpdate();
        System.out.println("markPatientsAsDischarged() updated " + result + " patients");
        return result;
    }
}

   private void createBedDischargeAudit(Connection conn) throws SQLException {
    String sql = "INSERT INTO bed_discharge_audit (inpatient_id, processed_at) " +
                "SELECT ipd.inpatient_id, NOW() " +
                "FROM inpatient_details ipd " +
                "WHERE ipd.isDischarged = 1 " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM bed_discharge_audit bda " +
                "   WHERE bda.inpatient_id = ipd.inpatient_id" +
                ")";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int inserted = stmt.executeUpdate();
        if (inserted > 0) {
            System.out.println("Created discharge audit records for " + inserted + " patients");
        }
    }
}

private void debugShowDischargedPatients(Connection conn) throws SQLException {
    String sql = "SELECT inpatient_id, patient_name, discharge_date, isDischarged " +
                "FROM inpatient_details " +
                "WHERE isDischarged = 1 " +
                "AND DATE(discharge_date) <= CURDATE()";
    
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        System.out.println("=== Currently discharged patients ===");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("inpatient_id") + 
                             ", Name: " + rs.getString("patient_name") +
                             ", Discharge Date: " + rs.getDate("discharge_date") +
                             ", isDischarged: " + rs.getInt("isDischarged"));
        }
    }
}

     private void updateHospitalBedsForRegularRequests(Connection conn) throws SQLException {
    String sql = "UPDATE hospital_facilities hf " +
                 "JOIN ambulance_request ar ON hf.hospital_id = ar.hospital_id " +
                 "JOIN patient_temporary_requests ptr ON ar.request_id = ptr.ambulance_request_id " +
                 "SET hf.free_general_beds = hf.free_general_beds - 1 " +
                 "WHERE ptr.isBedAllocated = 1 " +
                 "AND ptr.isProcessed = 1 " +
                 "AND ptr.ambulance_request_id IS NOT NULL " +
                 "AND hf.free_general_beds > 0 " +
                 "AND NOT EXISTS ( " +
                 "   SELECT 1 FROM bed_allocation_audit baa " +
                 "   WHERE baa.temporary_request_id = ptr.temporary_request_id" +
                 ")";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int updated = stmt.executeUpdate();
        if (updated > 0) {
            System.out.println("Updated bed counts for " + updated + " hospitals due to regular requests");
            createBedAllocationAudit(conn);
        }
    }
}
}

/*
SELECT      request_time,     NOW() as 'current_time',     DATE_ADD(request_time, INTERVAL 12 MINUTE) as allocation_time,     TIMESTAMPDIFF(MINUTE, request_time, NOW()) as minutes_passed,     CASE          WHEN NOW() >= DATE_ADD(request_time, INTERVAL 12 MINUTE)          THEN 'READY_FOR_ALLOCATION'          ELSE CONCAT('WAITING_', 12 - TIMESTAMPDIFF(MINUTE, request_time, NOW()), '_MINUTES')      END as status FROM patient_temporary_requests  WHERE temporary_request_id = 189;


SELECT ptr.temporary_request_id FROM patient_temporary_requests ptr WHERE ptr.isBedAllocated = 1   AND ptr.isProcessed = 1   AND NOT EXISTS (     SELECT 1 FROM bed_allocation_audit baa     WHERE baa.temporary_request_id = ptr.temporary_request_id   );

tail -f /home/karthick-ts476/Server/apache-tomcat-10.1.44/logs/catalina.out


SELECT 
    temporary_request_id,
    request_time,
    NOW() as 'current_time',
    DATE_ADD(request_time, INTERVAL 12 MINUTE) as allocation_ready_time,
    TIMESTAMPDIFF(MINUTE, request_time, NOW()) as minutes_elapsed,
    CASE 
        WHEN NOW() >= DATE_ADD(request_time, INTERVAL 12 MINUTE) 
        THEN 'READY_FOR_PROCESSING' 
        ELSE CONCAT('WAITING_', 
                   TIMESTAMPDIFF(MINUTE, NOW(), DATE_ADD(request_time, INTERVAL 12 MINUTE)), 
                   '_MINUTES') 
    END as allocation_status
FROM patient_temporary_requests 
WHERE temporary_request_id = 172;
*/


