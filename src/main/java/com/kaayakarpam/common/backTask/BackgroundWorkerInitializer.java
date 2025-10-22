package com.kaayakarpam.common.backTask;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener

public class BackgroundWorkerInitializer implements ServletContextListener {

        private AmbulanceStatusWorker ambulanceWorker;
        private ScanCentreStatusWorker scanCentreWorker;
        
        @Override
        public void contextInitialized(ServletContextEvent event) {
        System.out.println("=== Starting Background Workers ===");
            ambulanceWorker = new AmbulanceStatusWorker();
            ambulanceWorker.start();
            
            scanCentreWorker = new ScanCentreStatusWorker();
            scanCentreWorker.start();
        }
        
        @Override
        public void contextDestroyed(ServletContextEvent event) {
            if (ambulanceWorker != null) {
                ambulanceWorker.stop();
            }
            if (scanCentreWorker != null) {
                scanCentreWorker.stop();
            }
        }
}
