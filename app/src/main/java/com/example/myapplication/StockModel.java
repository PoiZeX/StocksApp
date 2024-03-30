package com.example.myapplication;

public class StockModel {
    private String symbol;
    private double price;
    private double dailyChange;

    public StockModel(String symbol, double price, double dailyChange) {
        this.symbol = symbol;
        this.price = price;
        this.dailyChange = dailyChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getDailyChange() {
        return dailyChange;
    }
}
