package com.example.pisspal;

public class ShopDomain {
    private String title;
    private String picUrl;
    private double price;
    private String url; // Add this field

    public ShopDomain(String title, String picUrl, double price, String url) {
        this.title = title;
        this.picUrl = picUrl;
        this.price = price;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
