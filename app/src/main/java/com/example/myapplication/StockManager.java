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

    private static StockManager instance;
    private final String apiKey = "cnjibnhr01qmfbtbdcggcnjibnhr01qmfbtbdch0";

    private StockManager() {
    }

    public static synchronized StockManager getInstance() {
        if (instance == null) {
            instance = new StockManager();
        }
        return instance;
    }

//    public void fetchStockSymbols(StockListener listener) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<String> symbols = fetchStockSymbolsFromAPI();
//                if (symbols != null) {
//                    for (String symbol : symbols) {
//                        listener.onStockFetched(symbol, -1, -1);
//                    }
//                } else {
//                    listener.onStockFetchFailed("Failed to fetch stock symbols.");
//                }
//            }
//        }).start();
//    }

//    protected List<String> getAllStocksSymbols() {
//        String stockSymbolsJson = getAllStocksJson();
//        if (stockSymbolsJson != null) {
//            return stocksJsonToList(stockSymbolsJson);
//        }
//        return null;
//    }

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


    protected Stock getStockData(String symbol) {
        String quoteJsonString = getSingleStockDataJson(symbol);
        if (quoteJsonString != null)
            return parseStockQuote(symbol, quoteJsonString);
        else
            return new Stock("error", -1, -1);

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

    private Stock parseStockQuote(String symbol, String quoteJsonString) {
        final Stock[] res = new Stock[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject quoteJson = new JSONObject(quoteJsonString);
                    res[0] = new Stock(symbol, quoteJson.getDouble("c"), quoteJson.getDouble("d"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    res[0] = new Stock("Error", -1, -1);
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
}
