<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Kaayakarpam - Patient Dashboard</title>

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
    }

    nav a {
      display: flex;
      align-items: center;
      color: #ecf0f1;
      text-decoration: none;
      margin: 12px 0;
      padding: 10px 12px;
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
  <!-- Sidebar -->
<nav>
  <h2><i class="fa-solid fa-user-shield"></i> Patient Panel</h2>
  <a href="/kaayakarpam/index.html"><i class="fa fa-home" aria-hidden="true"></i> Home</a>
   <a href="../jsp/patientView.jsp"><i class="fa fa-clipboard" aria-hidden="true"></i> DashBoard</a>
   <a href="/kaayakarpam/bookAmbulance.html"><i class="fa-solid fa-truck-medical"></i> Book Ambulance</a>
  <a href="../html/bookScanCenter.html"><i class="fa-solid fa-x-ray"></i> Book Scan Centre</a>
  <a href="../html/upcomingBookings.html"><i class="fa-solid fa-id-card"></i> Upcoming Appointments</a>
  <a href="../html/viewHistory.html"><i class="fa-solid fa-clock-rotate-left"></i> Patient History</a>
  <a href="../../common/web/html/viewProfile.html"><i class="fa-solid fa-id-card"></i> View Profile</a>
</nav>

  <!-- Main Content -->
  <div class="main">
    <header>
      <h1>Welcome, <%= session.getAttribute("role") %></h1>
      <div id="datetime"></div>
    </header>

    <div class="container">
      <!-- Dashboard Stats -->
             <div class="stats">
             
                  <a href="../html/upcomingBookings.html" class="stat-card-link">
                      <div class="stat-card">
                          <i class="fa-solid fa-bed-pulse"></i>
                          <h3 id="upComingAppointments">0</h3>
                         <p>upComingAppointments</p>
                      </div>
                  </a>
           
              </div>
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
    // Function to update date and time
    function updateDateTime() {
      const now = new Date();
      const options = { 
        weekday: 'long', year: 'numeric', month: 'long', 
        day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' 
      };
      document.getElementById("datetime").textContent = now.toLocaleString("en-IN", options);
    }

    setInterval(updateDateTime, 1000);
    updateDateTime();
    
      async function loadDashboardStats() {
            try {
              const response = await fetch("/kaayakarpam/patientDashboardStats");
              const data = await response.json();

          
              document.getElementById("upComingAppointments").textContent = data.upComingAppointments;
              //document.getElementById("requestCount").textContent = data.requests;
            }
            catch (err) {
              console.error("Error fetching stats:", err);
            }
     }
  setInterval(loadDashboardStats, 2000);   
  loadDashboardStats();
  
  </script>
</body>
</html>
 
