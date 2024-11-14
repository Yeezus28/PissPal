package com.example.pisspal;

public class ReviewModel {
    private String locationName, otherComments;
    private double averageRating, latitude, longitude;
    private String imageUrl;


    public ReviewModel(String locationName, String otherComments, Double averageRating,  String imageUrl, double latitude, double longitude) {
        this.locationName = locationName;
        this.otherComments = otherComments;
        this.averageRating = averageRating;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public double getLongitude() {return longitude;}

    public double getLatitude() {return latitude;}

    public double getAverageRating() {
        return averageRating;
    }

    public String getOtherComments() {
        return otherComments;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getImageUrl() {return imageUrl;}
}
