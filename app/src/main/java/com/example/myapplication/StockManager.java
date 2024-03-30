package com.example.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StockManager {
    private ArrayList<StockObserver> observers; // list of observers

    private static StockManager instance;
    private final String apiKey = "cnjibnhr01qmfbtbdcggcnjibnhr01qmfbtbdch0";
    private final String apiKey_GRAPH = "7G1FM8ZRXAL6DQYD";

    public String currentStock;  // store the current selected stock

    //https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=5min&apikey=7G1FM8ZRXAL6DQYD

    private StockManager() {
        observers = new ArrayList<>();

    }

    public static synchronized StockManager getInstance() {
        if (instance == null) {
            instance = new StockManager();
        }
        return instance;
    }


    //region Get All Stocks
    protected List<String> getAllStocksSymbols() {
        final List<String> symbols = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    String apiUrl = "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + apiKey;
                    URL url = new URL(apiUrl);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) return;

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) return;

                    String stockSymbolsJson = buffer.toString();
                    symbols.addAll(stocksJsonToList(stockSymbolsJson));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // need to disconnect anyway
                    if (urlConnection != null) urlConnection.disconnect();

                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return symbols;
    }

    public List<String> stocksJsonToList(String stockSymbolsJson) {
        List<String> symbols = new ArrayList<>();
        try {
            JSONArray symbolsArray = new JSONArray(stockSymbolsJson);
            for (int i = 0; i < symbolsArray.length(); i++) {
                JSONObject stockObject = symbolsArray.getJSONObject(i);
                String symbol = stockObject.getString("symbol");
                symbols.add(symbol);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return symbols;
    }

    //endregion

    //region Get Single Stock data
    protected StockModel getStockData(String symbol) {
        String quoteJsonString = getSingleStockDataJson(symbol);
        if (quoteJsonString != null)
            return parseStockQuote(symbol, quoteJsonString);
        else
            return new StockModel("error", -1, -1);

    }

    private String getSingleStockDataJson(String symbol) {
        final StringBuilder quoteJsonString = new StringBuilder();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    String apiUrl = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + apiKey;

                    URL url = new URL(apiUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null) return;

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        quoteJsonString.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return quoteJsonString.toString();
    }

    private StockModel parseStockQuote(String symbol, String quoteJsonString) {
        final StockModel[] res = new StockModel[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject quoteJson = new JSONObject(quoteJsonString);
                    res[0] = new StockModel(symbol, quoteJson.getDouble("c"), quoteJson.getDouble("d"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    res[0] = new StockModel("Error", -1, -1);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    //endregion

    //region Stock observer pattern
    public void addObserver(StockObserver observer) {
        //sijmple method to add observer
        observers.add(observer);
    }

    public void removeObserver(StockObserver observer) {
        //simple method to remove observer
        observers.remove(observer);
    }

    public void notifyObservers(StockModel stock) {
        // uses (from diagram)
        // notify to my  registered (maincontroller and graphcontroller)
        for (StockObserver observer : observers) {
            observer.onStockDataChanged(stock);
        }
    }

    //endregion stock observer

    public void refreshStockData(String symbol) {
        // get data and notify
        StockModel updatedStock = getStockData(symbol);
        notifyObservers(updatedStock);
    }

}
