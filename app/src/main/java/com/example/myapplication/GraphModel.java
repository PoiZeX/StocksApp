package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GraphModel {

    private static GraphModel instance;

    private static final String TAG = GraphModel.class.getSimpleName();
    String apiKey = "D370QTABFLDJH2E6";
    String baseApiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_";

    private GraphModel() {

    }

    public static synchronized GraphModel getInstance() {
        if (instance == null) {
            instance = new GraphModel();
        }
        return instance;
    }

    private List<Entry>  fetchStockData(final String symbol, final String timeframe) {
        final JSONObject[] jsonResponse = {null};
        String temp = timeframe.toUpperCase();
        if (timeframe.equals("2year") || timeframe.equals("3year") || timeframe.equals("all")) {
            temp = "Monthly"; //because its custom. for me
        }
        final String timeframeToAPI = temp;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String apiUrl = baseApiUrl + timeframeToAPI + "&symbol=" + symbol + "&apikey=" + apiKey;

                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    urlConnection.connect();

                    //  code 200
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // read the response data
                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            response.append(line);
                        }

                        jsonResponse[0] = new JSONObject(response.toString());

                        bufferedReader.close();
                        inputStream.close();
                    } else {
                        throw new IOException("API request failed with response code " + responseCode);
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /// parse

        try{
            return parseStockData(jsonResponse[0], timeframe);

        }
        catch (Exception e)
        {e.printStackTrace();}{
            return new ArrayList<>();
        }
    }


    private List<Entry> parseStockData(JSONObject jsonData, String timeframe) throws JSONException {
        List<Entry> entries = new ArrayList<>();
        String finalTimeframe = timeframe.substring(0, 1).toUpperCase() + timeframe.substring(1); // make sure

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject timeSeries = jsonData.getJSONObject(finalTimeframe +" Time Series");
                    Iterator<String> keys = timeSeries.keys();
                    int index = 0;
                    while (keys.hasNext() && index < numOfEntriesToGet(timeframe)) {
                        String date = keys.next();
                        JSONObject data = timeSeries.getJSONObject(date);
                        float price = Float.parseFloat(data.getString("4. close")); // i think
                        entries.add(new Entry(index++, price));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return entries;

    }

private  int numOfEntriesToGet(String timeframe){
    timeframe = timeframe.toLowerCase();
        switch(timeframe){
            case "weekly":
                return 4; // for last month
            case "monthly":
                return 12; // for last year
            case "2year":
                return 24;
            case "3year":
                return 36;


        }
        return 12*100; // all time...
    }
    public List<Entry> listForChart(String symbol, String timeframe)  {
        List<Entry> res = new ArrayList<>();
        try {
            return fetchStockData(symbol, timeframe);
        }
        catch (Exception e) {
            e.printStackTrace();
            return res;
        }

    }

}
