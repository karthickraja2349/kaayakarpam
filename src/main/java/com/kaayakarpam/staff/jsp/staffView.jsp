<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Kaayakarpam - Staff Dashboard</title>

  <!-- Font Awesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

  <style>
    body {
      font-family: Arial, sans-serif;
      margin: 0;
      padding: 0;
      background-color: #f4f6f9;
      display: flex;
      min-height: 100vh;
    }

    /* Session expired modal */
    .modal {
      display: none;
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.7);
      z-index: 1000;
      justify-content: center;
      align-items: center;
    }

    .modal-content {
      background-color: white;
      padding: 25px;
      border-radius: 10px;
      text-align: center;
      max-width: 400px;
      width: 80%;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
    }

    .modal-content h2 {
      color: #e74c3c;
      margin-top: 0;
    }

    .modal-content p {
      margin: 15px 0;
      color: #555;
    }

    .modal-button {
      background-color: #3498db;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 5px;
      cursor: pointer;
      font-size: 16px;
      transition: background-color 0.3s;
    }

    .modal-button:hover {
      background-color: #2980b9;
    }

    /* Sidebar Navigation */
      nav {
      width: 240px;
      background-color: #2c3e50;
      padding: 20px 10px;
      box-shadow: 2px 0 5px rgba(0,0,0,0.1);
    }

    nav h2 {
      color: #ecf0f1;
      text-align: center;
      margin-bottom: 25px;
      font-size: 22px;
      margin-top:15px;
      margin-left : 20px;
    }

    nav a {
      display: flex;
      align-items: center;
      color: #ecf0f1;
      text-decoration: none;
      margin: 6px 0; /* Reduced from 12px to 6px */
    padding: 8px 12px; /* Reduced padding from 10px to 8px */
      border-radius: 6px;
      transition: 0.3s;
      font-size: 15px;
    }

    nav a i {
      margin-right: 10px;
      width: 20px;
      text-align: center;
    }

    nav a:hover {
      background-color: #1abc9c;
      color: #fff;
    }


    /* Main Section */
    .main {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    header {
      background-color: #34495e;
      color: white;
      padding: 15px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    header h1 {
      margin: 0;
      font-size: 20px;
    }

    #datetime {
      font-size: 14px;
      color: #f1c40f;
      font-weight: bold;
    }

    .container {
      padding: 20px;
    }

    /* Dashboard Stats */
    .stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 25px;
    }
     .stat-card-link {
        text-decoration: none;
        color: inherit;
        display: block;
    }
    
    .stat-card-link:hover .stat-card {
    background-color: #f0f8ff;
    transform: translateY(-2px); 
    box-shadow: 0 4px 8px rgba(0,0,0,0.1); /* Enhanced shadow */
    transition: all 0.2s ease;
}

    .stat-card {
      cursor : pointer;
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.1);
      text-align: center;
      transition: 0.3s;
    }

    .stat-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .stat-card i {
      font-size: 30px;
      margin-bottom: 10px;
      color: #2980b9;
    }

    .stat-card h3 {
      margin: 10px 0 5px;
      font-size: 18px;
      color: #2c3e50;
    }

    .stat-card p {
      margin: 0;
      font-size: 14px;
      color: #7f8c8d;
    }

    /* Regular Card */
    .card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      margin: 15px 0;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }

    .card h3 {
      margin-top: 0;
      color: #2980b9;
    }

    /* Notifications */
    .notification-card {
      background: #fff7e6;
      border-left: 5px solid #f39c12;
      border-radius: 8px;
      padding: 20px;
      margin: 15px 0;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }

    .notification-card h2 {
      margin-top: 0;
      color: #d35400;
      display: flex;
      align-items: center;
    }

    .notification-card h2 i {
      margin-right: 8px;
    }

    .notification-card p {
      margin: 6px 0;
    }
  </style>
</head>
<body>
  <!-- Session Expired Modal -->
  <div class="modal" id="sessionExpiredModal">
    <div class="modal-content">
      <h2><i class="fas fa-exclamation-triangle"></i> Session Expired</h2>
      <p>Your session has expired due to inactivity. You will be redirected to the login page.</p>
      <button class="modal-button" onclick="redirectToLogin()">OK</button>
    </div>
  </div>

  <!-- Sidebar -->
  <nav>
    <h2><i class="fa-solid fa-user-shield"></i> Staff Panel</h2>
     <a href="/kaayakarpam/index.html"><i class="fa fa-home" aria-hidden="true"></i> Home</a>
     <a href="#"><i class="fa fa-clipboard" aria-hidden="true"></i> DashBoard</a>
    <a href="../html/viewInPatients.html"><i class="fa-solid fa-bed"></i> View InPatients</a>
    <a href="../html/viewHospitalDetails.html"><i class="fa-solid fa-hospital"></i> Hospital Details</a>
    <a href="../html/viewScanAppointments.html"><i class="fa fa-calendar" aria-hidden="true"></i> Today Scan Appointments </a>
    <a href="../html/viewUpcomingScanAppointments.html"><i class="fas fa-hourglass"></i> Upcoming Scan Appointments </a>
     <a href="../html/scanAppointmentsHistory.html"><i class="fas fa-hourglass-end"></i> Scan Appointments History</a>
    <a href="../html/patientManagement.html"><i class="fa-solid fa-file-medical"></i> Patient Requests</a>
     <a href="../../common/web/html/viewProfile.html"><i class="fa-solid fa-user"></i> View Profile</a>
  <!--  <a href="#" onclick="logout()"><i class="fas fa-sign-out-alt"></i> Logout</a> -->
  </nav>

  <!-- Main Content -->
  <div class="main">
    <header>
      <h1>Welcome, <span id="userRole">Staff</span></h1>
      <div id="datetime"></div>
    </header>

    <div class="container">
      <!-- Dashboard Stats -->
      <div class="stats">
       
        
           <a href="../html/viewInPatients.html" class="stat-card-link">
              <div class="stat-card">
                  <i class="fa-solid fa-bed-pulse"></i>
                  <h3 id="patientCount">0</h3>
                  <p>Currently admitted</p>
              </div>
          </a>
          
          <a href="../html/patientManagement.html" class="stat-card-link">
              <div class="stat-card">
                   <i class="fa-solid fa-envelope"></i>
                    <h3 id="requestCount">0</h3>
                 <p>Pending approvals</p>
              </div>
          </a>
          
      </div>

      <!-- Quick Actions -->
      <div class="card">
        <h3><i class="fa-solid fa-bolt"></i> Quick Actions</h3>
        <p>Use the navigation menu to manage hospital and patients efficiently.</p>
      </div>

      <!-- Notifications -->
      <div class="notification-card">
        <h2><i class="fa-solid fa-bell"></i> Notifications</h2>
        <p> Regional Language feature update in progress.</p>
        <p> User feedback feature update in progress.</p>
        <p> UI enhancements coming soon.</p>
      </div>
    </div>
  </div>

  <script>
    // Session management variables
    let sessionCheckInterval;
    let inactivityTimer;
    const SESSION_CHECK_INTERVAL = 60000; // Check every 60 seconds
    const INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds

    // Function to redirect to login page
    function redirectToLogin() {
      window.location.href = '/kaayakarpam/index.html'; 
    }

    // Function to show session expired modal
    function showSessionExpiredModal() {
      const modal = document.getElementById('sessionExpiredModal');
      modal.style.display = 'flex';
      
      // Redirect after 5 seconds even if user doesn't click OK
      setTimeout(redirectToLogin, 5000);
    }

    // Function to check session status
    function checkSessionStatus() {
      // Try to fetch data from a protected endpoint
      fetch('/kaayakarpam/staffDashboardStats', {
        method: 'GET',
        credentials: 'include' // Include cookies in the request
      })
      .then(response => {
        if (response.status === 401 || response.status === 403) {
          // Session expired or unauthorized
          clearInterval(sessionCheckInterval);
          clearTimeout(inactivityTimer);
          showSessionExpiredModal();
        } else {
          // Session is still valid, reset inactivity timer
          resetInactivityTimer();
        }
        return response.json();
      })
      .then(data => {
        if (data && data.patients !== undefined && data.requests !== undefined) {
          document.getElementById("patientCount").textContent = data.patients;
          document.getElementById("requestCount").textContent = data.requests;
        }
      })
      .catch(error => {
        console.error("Error checking session:", error);
        // If there's an error, assume session might be expired
        clearInterval(sessionCheckInterval);
        clearTimeout(inactivityTimer);
        showSessionExpiredModal();
      });
    }

    // Function to reset inactivity timer
    function resetInactivityTimer() {
      clearTimeout(inactivityTimer);
      inactivityTimer = setTimeout(() => {
        showSessionExpiredModal();
      }, INACTIVITY_TIMEOUT);
    }

    // Function to set up activity listeners
    function setupActivityListeners() {
      const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
      events.forEach(event => {
        document.addEventListener(event, resetInactivityTimer);
      });
    }

    // Function to handle logout
    function logout() {
      // Clear any session data
      clearInterval(sessionCheckInterval);
      clearTimeout(inactivityTimer);
      
      // Simply redirect to login page since we don't have a logout endpoint
      redirectToLogin();
    }

    // Function to update date and time
    function updateDateTime() {
      const now = new Date();
      const options = { 
        weekday: 'long', year: 'numeric', month: 'long', 
        day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' 
      };
      document.getElementById("datetime").textContent = now.toLocaleString("en-IN", options);
    }

    // Initialize the dashboard
    function initDashboard() {
      // Set up session checking
      setupActivityListeners();
      resetInactivityTimer();
      
      // Start checking session status periodically
      sessionCheckInterval = setInterval(checkSessionStatus, SESSION_CHECK_INTERVAL);
      
      // Initial session check
      checkSessionStatus();
      
      // Set up date/time display
      setInterval(updateDateTime, 1000);
      updateDateTime();
      
      // Set user role if available
      try {
        // This would normally come from your server-side template
        const roleElement = document.getElementById('userRole');
        if (roleElement) {
          // If you have a way to get the role from session, set it here
          // roleElement.textContent = <%= session.getAttribute("role") %> || 'Staff';
        }
      } catch (e) {
        console.log("Could not set user role from session");
      }
    }

    // Initialize the dashboard when page loads
    window.addEventListener('load', initDashboard);
  </script>
</body>
</html>




