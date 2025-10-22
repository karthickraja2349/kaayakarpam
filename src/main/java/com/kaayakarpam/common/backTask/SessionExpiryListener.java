package com.kaayakarpam.common.backTask;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionExpiryListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
      
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        session.setAttribute("SESSION_EXPIRED", "true");
    }
}
/*
report summary for my web application
name kaayakarpam (meaning)

roles admin, staff, patient

admin manages the infracture of the hospital and manages ambulances and scan centres
staff can able to view their hospital status, ambulances, scan appoinmnets(previous, today, future), view patient requests, and inpatient details
patient can able to book appoinment for scan centres and ambulance request

all are able to view profile, edit profile, reset password

forget password option was also enable

after login session will be handled, if session is out user will directly redirect to login page

in login page, there is an option named as request ambulance, for non registered users to book ambulance in emergency like (108)

mysql group replication was enabled
hicaricp was added
leaflet map was enabled to access location(latitude and longitude)

background tasks for session listening, handle ambulance request, allocate bed was added

distance calculation was done by heversine formula

if a non registered user requests an ambulance, 
     location, request type(critical , normal, book)
     latitude and longitude (by leaflet map)
     
     after entering , the background task will start active to find which hospital is near(as well as free bed) and which ambulance is available nearer and calculate estimate time and shown in ui.
     the request was delivered to patient requests page of the respective hospital staff
     the staff can able to allocate bed and transfer the patient request to another hospital
     if bed was allocated , the patient was added to inpatient details
     
     if the request was not do anything for particular time, bed is automatically allocated by background task
     
     after that the staff can able to edit the details in patient details page
     
if an admin wants to delete the hospital, then the hospital staffs automatically loss their access
the inpatients was transferred to nearby hospitals (distance and free beds)

the upcoming scan appointments was automatically cancelled and notified to patients
     */
