package com.kaayakarpam.common.db;

public final class SQLQueries {

    public static final String VERIFY_ADMIN =
        "SELECT a.admin_id AS id, a.email, c.password_hash, c.password_salt " +
        "FROM admin a JOIN admin_credentials c ON a.admin_id = c.admin_id " +
        "WHERE a.email = ?";
//1
    public static final String VERIFY_PATIENT =
        "SELECT p.patient_id AS id, c.email, c.password_hash, c.password_salt " +
        "FROM patient p JOIN patient_credentials c ON p.patient_id = c.patient_id " +
        "WHERE c.email = ?";

    public static final String VERIFY_STAFF =
    "SELECT s.staff_id AS id, c.email, c.password_hash, c.password_salt " +
    "FROM staff s " +
    "JOIN staff_credentials c ON s.staff_id = c.staff_id " +
    "WHERE c.email = ? " +
    "AND s.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";

        
     public static final String INSERT_ADMIN =
        "INSERT INTO admin (name, email, mobile_number, age, gender, address) VALUES (?, ?, ?, ?, ?, ?)";
        
      public static final String INSERT_ADMIN_CRED =
          "INSERT INTO admin_credentials (admin_id, password_hash, password_salt) VALUES (?, ?, ?)";
          
      public static final String INSERT_PATIENT =
          "INSERT INTO patient (patient_name, age, gender, address, mobile_number) VALUES (?, ?, ?, ?, ?)";
          
      public static final String INSERT_PATIENT_CRED =
          "INSERT INTO patient_credentials (patient_id, email, password_hash, password_salt) VALUES (?, ?, ?, ?)";

      public static final String INSERT_STAFF =
          "INSERT INTO staff (hospital_id, staff_name, age, gender, is_on_duty, mobile_number, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
          
      public static final String INSERT_STAFF_CRED =
          "INSERT INTO staff_credentials (staff_id, email, password_hash, password_salt) VALUES (?, ?, ?, ?)";
          
      public static final String INSERT_HOSPITAL = 
            "INSERT INTO hospital(hospital_name, address, ceo_name, location, phone_number,latitude,longitude) VALUES (?,?,?,?,?,?,?)";
            
      public static final String INSERT_HOSPITAL_FACILITIES = 
            "INSERT INTO hospital_facilities(hospital_id,total_general_beds, free_general_beds, total_icu_beds, free_icu_beds, lab_count,ambulance_count) VALUES (?,?,?,?,?,?,?)";
            
       public static final String GET_ADMIN_BY_ID= 
             "SELECT name, email, mobile_number, age, gender , address from admin WHERE admin_id = ?";
      //2  
        public static final String GET_PATIENT_BY_ID= 
             "SELECT p.patient_name, pc.email, p.mobile_number, p.age, p.gender , p.address from patient p JOIN patient_credentials pc ON p.patient_id = pc.patient_id WHERE p.patient_id = ?";
         
        public static final String GET_STAFF_BY_ID= 
             "SELECT s.staff_name, sc.email, s.mobile_number, s.age, s.gender , s.address FROM staff s JOIN staff_credentials sc ON s.staff_id = sc.staff_id  WHERE s.staff_id = ?";
            
        public static final String SEARCH_HOSPITAL = 
             "SELECT h.hospital_name, h.address, h.phone_number, h.location,  hf.free_general_beds, hf.free_icu_beds, hf.ambulance_count from hospital h JOIN hospital_facilities hf ON h.hospital_id = hf.hospital_id WHERE 1=1";
             
        public static final String FIND_NEARBY_HOSPITAL =     
    "SELECT h.hospital_id , ROUND(6371 * ACOS ( " +
    " COS(RADIANS(?)) * COS(RADIANS(h.latitude)) *" +
    " COS(RADIANS(h.longitude) - RADIANS(?)) +" +
    " SIN(RADIANS(?)) * SIN(RADIANS(h.latitude)) " +
    " ),2 ) AS distance_km " +
    "FROM hospital h JOIN hospital_facilities hf " +
    "ON h.hospital_id = hf.hospital_id " +
    "WHERE hf.free_general_beds > 0 " +
    "AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) " +
    "ORDER BY distance_km ASC LIMIT 1";

              
           public static final String FIND_AVAILABLE_AMBULANCE = 
                "SELECT a.ambulance_id , ROUND(6371 * ACOS ( " +
                "COS(RADIANS(?)) * COS(RADIANS(a.latitude)) * " +
                "COS(RADIANS(a.longitude) - RADIANS(?)) +" +
                "SIN(RADIANS(?)) * SIN(RADIANS(a.latitude)) " +
                " ),2 ) AS distance_km " +
                "FROM ambulance a " +
                "WHERE a.hospital_id = ? AND a.is_available > 0  " +
                "AND a.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)" +
                 "ORDER BY distance_km ASC limit 1 FOR UPDATE";
          
          public static final String ASSIGN_AMBULANCE = 
                "INSERT INTO ambulance_request(location, latitude, longitude, request_type, hospital_id, ambulance_id,created_at) VALUES(?,?,?,?,?,?,?)";
          
          public static final String UPDATE_AMBULANCE_STATUS = 
               "UPDATE ambulance SET is_available = 0 WHERE ambulance_id = ?";
          
          public static final String GET_HOSPITAL_GEO_LOCATION = 
               "SELECT latitude, longitude FROM hospital WHERE hospital_id = ?";
               
          public static final String GET_HOSPITAL_ID = 
               "SELECT hospital_id FROM hospital WHERE phone_number = ? AND hospital_id NOT IN(SELECT hospital_id FROM deleted_hospitals)";
               
         public static final String TEMPORARY_REGISTRATION = 
                "INSERT INTO patient_temporary_requests(ambulance_request_id, request_time) VALUES (?,?)";
                
         /*public static final String GET_TEMPORARY_REQUESTS = 
    "SELECT ptr.temporary_request_id, ptr.ambulance_request_id, " +
    "COALESCE(ar.request_type, 'N/A') AS request_type, " +
    "ptr.isBedAllocated, ptr.request_time " +
    "FROM patient_temporary_requests ptr " +
    "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
    "WHERE ptr.isBedAllocated = 0 " +
    "AND (ptr.ambulance_request_id = 0 OR ar.hospital_id = ?) ";*/
    
  /*  public static final String GET_TEMPORARY_REQUESTS = 
    "SELECT ptr.temporary_request_id, ptr.ambulance_request_id, " +
    "COALESCE(ar.request_type, 'TRANSFER') AS request_type, " +
    "ptr.isBedAllocated, ptr.request_time, " +
    "ptr.transferred_from, ptr.transferred_to " +
    "FROM patient_temporary_requests ptr " +
    "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
    "WHERE ptr.isBedAllocated = 0 " +
    "AND ptr.transferred_to = ?";*/
    
    public static final String GET_TEMPORARY_REQUESTS = 
    // For transfer requests
    "SELECT ptr.temporary_request_id, ptr.ambulance_request_id, " +
    "'TRANSFER' AS request_type, ptr.isBedAllocated, ptr.request_time, " +
    "ptr.transferred_from AS source_hospital_id, ptr.transferred_to AS destination_hospital_id, " +
    "ptm.patient_name, ptm.patient_aadhar, ptm.patient_condition, " +
    "'TRANSFER' AS request_category " +
    "FROM patient_temporary_requests ptr " +
    "LEFT JOIN patient_transfer_mapping ptm ON ptr.temporary_request_id = ptm.transfer_request_id " +
    "WHERE ptr.isBedAllocated = 0 AND ptr.transferred_to = ? AND ptr.ambulance_request_id IS NULL " +
    "UNION ALL " +
    // For ambulance requests - show basic info without patient details
    "SELECT ptr.temporary_request_id, ptr.ambulance_request_id, " +
    "ar.request_type, ptr.isBedAllocated, ptr.request_time, " +
    "ar.hospital_id AS source_hospital_id, ar.hospital_id AS destination_hospital_id, " +
    "'Unknown' AS patient_name, 'Unknown' AS patient_aadhar, ar.request_type AS patient_condition, " +
    "'AMBULANCE' AS request_category " +
    "FROM patient_temporary_requests ptr " +
    "JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
    "WHERE ptr.isBedAllocated = 0 AND ar.hospital_id = ?";

                         
        public static final String GET_HOSPITAL_ID_BY_STAFF_ID = 
               "SELECT hospital_id FROM staff where staff_id = ?";
         
        public static final String IS_BED_FREE = 
              "SELECT free_general_beds FROM hospital_facilities WHERE  hospital_id  = ?";
              
         public  static final String ALLOCATE_BED = 
               "UPDATE patient_temporary_requests SET  isBedAllocated = 1 , admitted_by = ? WHERE temporary_request_id = ?";
         
         public static final String GET_PATIENT_DETAILS = 
                "SELECT ar.request_type, ptr.request_time FROM patient_temporary_requests ptr JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id WHERE ar.request_id = ?";
                
         public static final String UPDATE_INPATIENT_DETAILS = 
               "INSERT INTO inpatient_details(patient_condition, booked_date,hospital_id)VALUES(?,NOW(),?)";    
          
         public static final String UPDATE_PROCESSED_STATUS = 
               "UPDATE  patient_temporary_requests SET isProcessed = 1 WHERE temporary_request_id = ?";
         
         public static final String GET_IN_PATIENT_DETAILS = 
              "SELECT inpatient_id, patient_name, patient_aadhar_number, patient_condition, reason as reason_for_admittance,booked_date as admission_date, roomNo, discharge_date FROM inpatient_details  WHERE hospital_id = ? ORDER BY admission_date  DESC";
              
         public static final String GET_FREE_BEDS = 
              "SELECT  free_general_beds FROM hospital_facilities WHERE hospital_id = ?";
        
        public static final String ALTER_FREE_BEDS = 
              "UPDATE hospital_facilities SET free_general_beds =  ? WHERE hospital_id = ?";
        
        public static final String GET_HOSPITAL_BASIC_DETAILS = 
             "SELECT hospital_name, address, phone_number FROM hospital WHERE hospital_id = ?";
             
      public static final String FIND_NEARBY_HOSPITALS =     
    "SELECT h.hospital_id , ROUND(6371 * ACOS ( " +
    " COS(RADIANS(?)) * COS(RADIANS(h.latitude)) *" +
    " COS(RADIANS(h.longitude) - RADIANS(?)) +" +
    " SIN(RADIANS(?)) * SIN(RADIANS(h.latitude)) " +
    " ),2 ) AS distance_km " +
    "FROM hospital h JOIN hospital_facilities hf " +
    "ON h.hospital_id = hf.hospital_id " +
    "WHERE hf.free_general_beds > 0 " +
    "AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) " +
    "AND h.hospital_id <> ? " +   // exclude the given hospital_id
    "ORDER BY distance_km ASC";


              
          public static final String HANDOVER_PATIENT_REQUEST = 
               "UPDATE patient_temporary_requests SET transferred_to = ? , transferred_from = ? WHERE temporary_request_id = ?";
               
          public static final String ALTER_HOSPITAL_ID_DUE_TO_TRANSFER = 
               "UPDATE  ambulance_request SET hospital_id  = ? WHERE request_id = ?";
           
          public static final String GET_AMBULANCE_REQUEST_ID =
               "SELECT ambulance_request_id FROM patient_temporary_requests WHERE temporary_request_id = ?";
               
           public static final String DELETE_HOSPITAL = 
                "DELETE FROM hospital WHERE hospital_id = ? AND hospital_name = ?" ;
                
            public static final String VIEW_HOSPITAL = 
                "SELECT DISTINCT h.hospital_id,h.hospital_name, h.address, h.phone_number,hf.total_general_beds, hf.free_general_beds,hf.lab_count FROM hospital h JOIN hospital_facilities hf  ON h.hospital_id = hf.hospital_id WHERE  h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";
                
            public static final String GET_HOSPITAL_DETAILS = 
                "SELECT  h.hospital_name, h.address, h.phone_number, h.ceo_name, hf.total_general_beds, hf.free_general_beds, hf.total_icu_beds, lab_count, hf.ambulance_count FROM hospital h JOIN hospital_facilities hf ON h.hospital_id = hf.hospital_id WHERE h.hospital_id = ?";
            
            public static final String IS_SUPERIOR_STAFF = 
                 "select is_superior_staff from staff where staff_id = ?";
             
             public static final String TOTAL_HOSPITALS = 
                  "SELECT COUNT(*) FROM hospital WHERE hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";
              
             public static final String TOTAL_INPATIENT_IN_HOSPITAL = 
                  "SELECT COUNT(*) from inpatient_details where hospital_id = ? AND isDischarged = 0";
                  
           /*public static final String PENDING_REQUEST_IN_HOSPITAL = 
    "SELECT COUNT(*) AS total_unallocated " +
    "FROM patient_temporary_requests ptr " +
    "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
    "WHERE ptr.isBedAllocated = 0 " +
    "AND (ptr.ambulance_request_id = 0 OR ar.hospital_id = ?)";*/
    
    public static final String PENDING_REQUEST_IN_HOSPITAL = 
    "SELECT COUNT(*) AS total_unallocated " +
    "FROM patient_temporary_requests ptr " +
    "LEFT JOIN ambulance_request ar ON ptr.ambulance_request_id = ar.request_id " +
    "WHERE ptr.isBedAllocated = 0 " +
    "AND (ptr.transferred_to = ? OR ar.hospital_id = ?)";


            
            public static final String OVERALL_INPATIENT_COUNT = 
                    "SELECT COUNT(*) FROM inpatient_details WHERE hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";
            
            public static final String OVERALL_PENDING_REQUESTS = 
                    "SELECT COUNT(*) FROM patient_temporary_requests WHERE isBedAllocated = 0";
                    
            public static final String OVERALL_SCANCENTRE_COUNT = 
                    "SELECT COUNT(*) FROM scan_centres WHERE hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) AND is_available = 1";
                    
            public static final String OVERALL_AMBULANCE_COUNT = 
                    "SELECT COUNT(*) FROM ambulance WHERE hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) AND is_deleted = 0";
                    
            public static final String GET_SCAN_TYPES  = 
                   "SELECT scan_type_id, scan_type_name FROM scan_type";
                   
            public static final String ADD_SCAN_CENTRE = 
                   "INSERT INTO scan_centres(scan_type_id, hospital_id) VALUES (?,?)";
            
           /* public static final String GET_HOSPITALS_BY_SCAN_TYPE = 
                   "SELECT h.hospital_id, h.hospital_name, h.address, h.phone_number FROM hospital h JOIN scan_centres sc ON h.hospital_id = sc.hospital_id WHERE sc.scan_type_id = ? AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";
                   */
            /*  public static final String GET_HOSPITALS_BY_SCAN_TYPE = 
    "SELECT h.hospital_id, h.hospital_name, h.address, h.phone_number " +
    "FROM hospital h " +
    "JOIN scan_centres sc ON h.hospital_id = sc.hospital_id " +
    "WHERE sc.scan_type_id = ? " +
    "AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) " +
    "AND (sc.total_booking_per_day > 0 " +
    "    AND (SELECT COUNT(*) " +
    "         FROM scan_bookings " +
    "         WHERE hospital_id = h.hospital_id " +
    "         AND DATE(booked_date) = CURDATE() " +
    "         AND isCancelled = 0) < sc.total_booking_per_day)";
            */
            
            public static final String GET_HOSPITALS_BY_SCAN_TYPE = 
    "SELECT h.hospital_id, h.hospital_name, h.address, h.phone_number " +
    "FROM hospital h " +
    "JOIN scan_centres sc ON h.hospital_id = sc.hospital_id " +
    "LEFT JOIN scan_bookings sb ON h.hospital_id = sb.hospital_id " +
    "   AND DATE(sb.booked_date) = ? " +
    "   AND sb.isCancelled = 0 " +
    "WHERE sc.scan_type_id = ?  AND sc.is_available = 1" +
    "  AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) " +
    "GROUP BY h.hospital_id, h.hospital_name, h.address, h.phone_number, sc.total_booking_per_day " +
    "HAVING COUNT(sb.scan_booking_id) < sc.total_booking_per_day";
    
         public static final String CHECK_SCAN_BOOKING = 
             //  " SELECT COUNT(*) FROM scan_bookings WHERE patient_id = ? AND hospital_id = ? AND booked_date = DATE(?)";
               "SELECT COUNT(*) FROM scan_bookings " +
                "WHERE patient_id = ? AND hospital_id = ? AND booked_date = DATE(?)";
                
         public static final String CHECK_SLOT_AVAILABILITY = 
                     "SELECT COUNT(sb.scan_booking_id) as booked_count, sc.total_booking_per_day " +
                "FROM scan_centres sc " +
                "LEFT JOIN scan_bookings sb ON sc.hospital_id = sb.hospital_id " +
                "AND sc.scan_type_id = sb.scan_type " +
                "AND sb.booked_date = ? " +
                "WHERE sc.hospital_id = ? AND sc.scan_type_id = ? " +
                "AND sc.is_available = 1 " +
                "GROUP BY sc.scan_center_id, sc.total_booking_per_day " +
                "FOR UPDATE";
         
         public static final String IS_HOSPITAL_PRESENT = 
                "SELECT hospital_id FROM deleted_hospitals WHERE hospital_id = ?";
               
          public static final String BOOK_SCAN = 
                    "INSERT INTO scan_bookings(patient_id, hospital_id, booked_date, scan_type) VALUES (?,?,?,?)";

            public static final String UPCOMING_BOOKINGS = 
                  " SELECT sb.scan_booking_id,h.hospital_name,h.address,h.phone_number,DATE(sb.booked_date) AS booked_date,st.scan_type_name FROM scan_bookings sb JOIN hospital h ON sb.hospital_id = h.hospital_id JOIN scan_type st ON sb.scan_type = st.scan_type_id WHERE sb.booked_date >= CURDATE() AND sb.patient_id = ? AND sb.isCancelled = 0 ORDER BY sb.booked_date";
                  
            public static final String CANCEL_BOOKING = 
                  "UPDATE scan_bookings SET iscancelled = 1 WHERE scan_booking_id = ?";
            
            public static final String TOTAL_UPCOMING_BOOKINGS = 
                  "SELECT COUNT(*) from scan_bookings WHERE patient_id = ? AND isCancelled = 0 AND booked_date >= CURDATE()";
            
            public static final String ADD_AMBULANCE = 
                  "INSERT INTO ambulance(hospital_id, vehicle_number, current_location, latitude, longitude)VALUES(?,?,?,?,?)";
           
           public static final String PATIENT_BOOKING_HISTORY = 
                 "SELECT h.hospital_name, h.address, h.phone_number, DATE(sb.booked_date) AS booked_date, st.scan_type_name, sb.isCancelled as status FROM scan_bookings sb  JOIN hospital h ON sb.hospital_id = h.hospital_id JOIN scan_type st ON sb.scan_type = st.scan_type_id WHERE  sb.patient_id = ? AND  ( ( sb.booked_date < CURDATE() AND sb.isCancelled = 0) OR sb.isCancelled = 1 ) ORDER BY sb.booked_date DESC";
                 
          public static final String GET_NEARBY_HOSPITALS =
                "SELECT h.hospital_id, h.hospital_name, h.address,hf.free_general_beds, " +
                "ROUND(6371 * ACOS( " +
                "    COS(RADIANS(?)) * COS(RADIANS(h.latitude)) * " +
                "    COS(RADIANS(h.longitude) - RADIANS(?)) + " +
                "    SIN(RADIANS(?)) * SIN(RADIANS(h.latitude)) " +
                "), 2) AS distance_km " +
                "FROM hospital h " +
                "JOIN hospital_facilities hf ON h.hospital_id = hf.hospital_id " +
                "WHERE hf.free_general_beds > 0 " +
                "  AND h.hospital_id != ? " +
                " AND h.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals) " +
                "ORDER BY distance_km ASC";
    
    public static final String INSERT_DELETED_HOSPITALS = 
          "INSERT INTO deleted_hospitals(hospital_id, deleted_date) VALUES (?,?)";
    
    public static final String GET_INPATIENT_DETAILS = 
           "SELECT * FROM inpatient_details WHERE hospital_id = ? AND isDischarged = 0";
           
           
          // New queries for request-based transfer system
    public static final String CREATE_TRANSFER_REQUEST = 
       "INSERT INTO patient_temporary_requests " +
        "(transferred_from, transferred_to, request_time, isBedAllocated) " +
        "VALUES (?, ?, ?, 0)";
    
    public static final String CREATE_PATIENT_TRANSFER_MAPPING = 
         "INSERT INTO patient_transfer_mapping " +
        "(patient_id, transfer_request_id, patient_name, patient_aadhar, patient_condition) " +
        "VALUES (?, ?, ?, ?, ?)";
    
    public static final String UPDATE_PATIENT_HOSPITAL =
          "UPDATE inpatient_details SET hospital_id = ? WHERE inpatient_id = ?";
           
    public static final String CHECK_HOSPITAL = 
          "SELECT COUNT(*) FROM hospital WHERE hospital_id = ? AND hospital_name = ?";
           
    public static final String GET_SCAN_CENTRES_OF_HOSPITAL = 
        "SELECT sc.scan_center_id, st.scan_type_name , sc.total_booking_per_day FROM scan_type st JOIN scan_centres sc ON sc.scan_type_id = st.scan_type_id WHERE sc.hospital_id = ? AND sc.is_available = 1";
        
     public static final String UPDATE_SCAN_CENTRE = 
           "UPDATE scan_centres SET total_booking_per_day = ? WHERE scan_center_id = ? ";
     
     /*public static final String TODAY_SCAN_APPOINTMENTS = 
            "SELECT p.patient_name, p.age, p.gender, p.mobile_number, st.scan_type_name FROM patient p JOIN scan_bookings sb ON p.patient_id = sb.patient_id JOIN scan_type st ON st.scan_type_id = sb.scan_type WHERE sb.hospital_id = ? AND DATE(sb.booked_date) = CURDATE() AND sb.isCancelled = 0";*/
           
         public static final String VIEW_UPCOMING_SCAN_APPOINTMENTS = 
            "SELECT scan_booking_id, p.patient_name, p.age, p.gender, p.mobile_number, st.scan_type_name,sb.booked_date FROM patient p JOIN scan_bookings sb ON p.patient_id = sb.patient_id JOIN scan_type st ON st.scan_type_id = sb.scan_type WHERE sb.hospital_id = ? AND DATE(sb.booked_date) > curDate() AND sb.isCancelled = 0";
            
       /*   public static final String RESCHEDULE_SCAN_APPOINTMENT = 
                "UPDATE scan_bookings SET booked_date = ? WHERE scan_booking_id = ?";
          
          public static final String CANCEL_SCAN_APPOINTMENT = 
               "UPDATE scan_bookings SET isCancelled = 1 WHERE scan_booking_id = ?"; */
               
          public static final String RESCHEDULE_SCAN_APPOINTMENT = 
              "UPDATE scan_bookings SET booked_date = ?, version = version + 1 WHERE scan_booking_id = ? AND version = ? AND isCancelled = 0";

            public static final String CANCEL_SCAN_APPOINTMENT = 
               "UPDATE scan_bookings SET isCancelled = 1, version = version + 1 WHERE scan_booking_id = ? AND version = ? AND isCancelled = 0";     
               
            public static final String CHECK_VERSION = 
                 "SELECT version FROM scan_bookings WHERE scan_booking_id = ?";
               
             public static final String IS_APPOINTMENT_CANCELLED = 
                   "SELECT isCancelled FROM scan_bookings WHERE scan_booking_id = ?";
              
                   
               public static final String FIXED_SCAN_BOOKING_OF_THE_DAY = 
                    "SELECT sc.total_booking_per_day FROM scan_centres sc JOIN scan_bookings sb ON sc.hospital_id = sb.hospital_id AND sc.scan_type_id = sb.scan_type WHERE sb.scan_booking_id = ? LIMIT 1";

            public static final String COUNT_BOOKINGS_FOR_DATE = 
                     "SELECT COUNT(*) as current_bookings FROM scan_bookings WHERE hospital_id = ? AND scan_type = ? AND DATE(booked_date) = ? AND isCancelled = 0";

           public static final String GET_BOOKING_DETAILS = 
                 "SELECT hospital_id, scan_type FROM scan_bookings WHERE scan_booking_id = ?";
                   
         public static final String SCAN_APPOINTMENT_HISTORY = 
               "SELECT sb.scan_booking_id ,p.patient_name, p.age, p.gender, p.mobile_number, st.scan_type_name,sb.booked_date FROM patient p JOIN scan_bookings sb ON p.patient_id = sb.patient_id JOIN scan_type st ON st.scan_type_id = sb.scan_type WHERE sb.hospital_id = ? AND DATE(sb.booked_date) < curDate() AND sb.isCancelled = 0";
               
         public static final String ADMIN_PASSWORD_RESET = 
                "UPDATE admin_credentials SET password_hash = ? , password_salt = ? WHERE admin_id = ?";
        
        public static final String STAFF_PASSWORD_RESET = 
                "UPDATE staff_credentials SET password_hash = ? , password_salt = ? WHERE staff_id = ?";
       //3  
        public static final String PATIENT_PASSWORD_RESET = 
                "UPDATE patient_credentials SET password_hash = ? , password_salt = ? WHERE patient_id = ?"; 
       
       public static final String VERIFY_ADMIN_FOR_FORGET_PASSWORD = 
             "SELECT a.admin_id AS id, a.email, a.mobile_number, c.password_hash, c.password_salt "+ 
              "FROM admin a JOIN admin_credentials c ON a.admin_id = c.admin_id " +
              "WHERE a.email = ? AND a.mobile_number = ?";     
        //4
        public static final String VERIFY_PATIENT_FOR_FORGET_PASSWORD = 
               "SELECT p.patient_id AS id, c.email, p.mobile_number, c.password_hash, c.password_salt " +
              "FROM patient p JOIN patient_credentials c ON p.patient_id = c.patient_id " +
              "WHERE c.email = ? AND p.mobile_number = ?";    
              
         public static final String VERIFY_STAFF_FOR_FORGET_PASSWORD = 
               "SELECT s.staff_id AS id, c.email, s.mobile_number,  c.password_hash, c.password_salt " +
              "FROM staff s " +
              "JOIN staff_credentials c ON s.staff_id = c.staff_id " +
              "WHERE c.email = ?  AND s.mobile_number = ?" +
              "AND s.hospital_id NOT IN (SELECT hospital_id FROM deleted_hospitals)";
              
         public static final String CHECK_SCAN_CENTRE_IN_HOSPITAL = 
               "SELECT COUNT(*) FROM scan_centres WHERE hospital_id = ? AND scan_type_id = ? AND is_available = 1";
               
          public static final String VIEW_SCAN_CENTRES = 
                "SELECT sc.scan_center_id, sc.scan_type_id, st.scan_type_name  FROM scan_centres sc JOIN scan_type st ON sc.scan_type_id = st.scan_type_id WHERE hospital_id = ? AND sc.is_available = 1";
          
          public static final String DELETE_SCAN_CENTRE = 
                "UPDATE scan_centres SET is_available = 0 WHERE hospital_id = ? AND scan_type_id = ?";
          
          public static final String UPDATE_SCAN_BOOKINGS = 
                "UPDATE scan_bookings SET isCancelled = 1 WHERE scan_type = ? AND hospital_id = ? AND DATE(booked_date) >= CURDATE()";
                
          public static final String VIEW_AMBULANCES = 
                "SELECT vehicle_number, current_location FROM ambulance WHERE hospital_id = ? AND hospital_id NOT IN(SELECT hospital_id FROM deleted_hospitals) AND is_deleted = 0";
                
          public static final String DELETE_AMBULANCE = 
                "UPDATE ambulance SET is_deleted = 1 , is_available = 0 WHERE vehicle_number = ?";
           
          public static final String CHECK_AMBULANCE = 
                "SELECT is_deleted FROM ambulance WHERE vehicle_number = ?";
                
           public static final String UPDATE_AMBULANCE = 
                "UPDATE ambulance SET is_deleted = 0, is_available = 1, hospital_id = ?, current_location = ?,  latitude = ?, longitude = ? WHERE vehicle_number = ?";
          public static final String GET_AMBULANCES_OF_THE_HOSPITAL = 
               "SELECT ambulance_id, vehicle_number, current_location, is_available FROM ambulance WHERE hospital_id = ?";
               
          public static final String DELETE_ACCOUNT = 
               "UPDATE patient SET is_deleted = 1 WHERE patient_ = ";
               
          public static final String CHECK_AVAILABILITY = 
                "SELECT COUNT(*) as booked_count, sc.total_booking_per_day " +
                "FROM scan_centres sc " +
                "LEFT JOIN scan_bookings sb ON sc.hospital_id = sb.hospital_id " +
                "AND sc.scan_type_id = sb.scan_type " +
                "AND sb.booked_date = ? " +
                "WHERE sc.hospital_id = ? AND sc.scan_type_id = ? " +
                "AND sc.is_available = 1 " +
                "GROUP BY sc.scan_center_id, sc.total_booking_per_day " +
                "FOR UPDATE";     
         
         public static final String GET_ADMIN_ID_BY_MAILID = 
              "SELECT admin_id FROM admin WHERE email = ?";
              
          public static final String GET_STAFF_ID_BY_MAILID = 
              "SELECT staff_id FROM staff_credentials WHERE email = ?";
          
          public static final String  GET_PATIENT_ID_BY_MAILID = 
              "SELECT patient_id FROM patient_credentials WHERE email = ?";
              
          public static final String VERIFY_OTP = 
              "SELECT generated_at FROM otp_access WHERE user_id = ? AND role = ? AND otp = ?";
              
          public static final String DELETE_OTP =
              "DELETE FROM otp_access WHERE user_id = ? AND role = ?";    
              
          public static final String INSERT_OTP = 
              "INSERT INTO otp_access(user_id, role, otp, generated_at) VALUES (?,?,?,?)";
              
          public static final String IS_BED_ALLOCATED = 
              "SELECT isBedAllocated FROM patient_temporary_requests WHERE temporary_request_id = ?";
              
         public static final String GET_HOSPITALS = 
              "SELECT hospital_name, latitude, longitude, phone_number,  hospital_id FROM hospital";
          
         public static final String IS_HOSPITAL_DELETED = 
               "SELECT hospital_id FROM deleted_hospitals WHERE hospital_id = ?";
}

