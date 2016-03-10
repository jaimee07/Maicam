package com.project.maico.maicam;

/**
 * Created by Clover on 11/24/2015.
 */
public class Utility {

    public static String convertToDMS(double latitude, double longitude){

        String latitudeLongitude = "";

        String latitudeText = convertToDMS(latitude);
        String longitudeText = convertToDMS(longitude);

        char latLetter = (latitude > 0) ? 'N' : 'S';
        char lonLetter = (longitude > 0) ? 'E' : 'W';

        return String.format("%s %c, %s %c", latitudeText, latLetter, longitudeText, lonLetter);
    }

    public static String convertToDMS(double latlong){
        latlong = Math.abs(latlong);

        int degrees;
        int minutes;
        double seconds;
        double temp;

        //degrees
        degrees = (int)latlong;

        //minutes
        temp =  latlong%degrees; //get the decimal part of latitude
        temp = temp*60; //convert to minutes
        minutes = (int) temp; //get whole number part of min

        //seconds
        temp = temp%minutes; //get the decimal part of minutes
        seconds = temp*60; // convert to seconds

        return String.format("%d°%d\'%.2f\"", degrees, minutes, seconds);
    }

    // returns a string required by ExifInterface for gps coord
    public static String gpsExif(double latlong){
        int degrees;
        int minutes;
        double seconds;
        double temp;

        //degrees
        degrees = (int)latlong;

        //minutes
        temp =  latlong%degrees; //get the decimal part of latitude
        temp = temp*60; //convert to minutes
        minutes = (int) temp; //get whole number part of min

        //seconds
        temp = temp%minutes; //get the decimal part of minutes
        seconds = temp*60; // convert to seconds

        return String.format("%d/1,%d/1,%f/100", degrees, minutes, seconds*100);


    }
}
