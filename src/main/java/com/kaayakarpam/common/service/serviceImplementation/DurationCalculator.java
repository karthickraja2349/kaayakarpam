package com.kaayakarpam.common.service.serviceImplementation;

import com.kaayakarpam.common.service.DurationAnalyser;

public class DurationCalculator implements DurationAnalyser{
           
           public double calculateDuration(double distance, double speed) {
                if (speed <= 0) {
                    throw new IllegalArgumentException("Speed must be greater than zero");
                }
                return (distance / speed) * 60; // Convert hours to minutes
            }
            
            public static String formatDuration(double minutes) {
                      int hours = (int) (minutes / 60);
                      int mins = (int) (minutes % 60);
        
                      if (hours > 0) {
                          return String.format("%d hour%s %d minute%s", 
                              hours, hours != 1 ? "s" : "", 
                              mins, mins != 1 ? "s" : "");
                      } 
                      else {
                          return String.format("%d minute%s", mins, mins != 1 ? "s" : "");
                      }
                  }
}
