package com.example.myapplication;

public class GraphEntry {
    private float id;
    private String date;
    private float price;

    public GraphEntry(float id, String date, float price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }
    public float getId() {
        return id;
    }
    public String getDate() {
        return date;
    }

    public float getPrice() {
        return price;
    }
}
