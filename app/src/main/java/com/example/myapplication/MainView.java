package com.example.myapplication;

import java.util.ArrayList;

public interface MainView {
    void showStocks(ArrayList<String> stocks);
    void showError(String message);
}