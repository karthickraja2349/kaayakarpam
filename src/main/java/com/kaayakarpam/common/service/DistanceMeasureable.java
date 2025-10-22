package com.kaayakarpam.common.service;

public interface DistanceMeasureable{

        public static final double EARTH_RADIUS_KM = 6371;
        
        double calculateDistance(double lat1, double lon1, double lat2, double lon2);
}



