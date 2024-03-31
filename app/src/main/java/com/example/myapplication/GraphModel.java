package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GraphModel {

    private static GraphModel instance;

//    String apiKey = "D370QTABFLDJH2E6"; // https://www.alphavantage.co/support/#api-key
    String apiKey = "lE8wpwBng_ppahJDaKI6kPxFWipws82l";  //https://polygon.io/docs/stocks/get_v2_aggs_ticker__stocksticker__range__multiplier___timespan___from___to
    String baseApiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_";

    private GraphModel() {

    }

    public static synchronized GraphModel getInstance() {
        if (instance == null) {
            instance = new GraphModel();
        }
        return instance;
    }

    private List<Entry>  fetchStockData(String apiUrl) {
        final JSONObject[] jsonResponse = {null};
//        String temp = timeframe.toUpperCase();
//        if (timeframe.equals("2year") || timeframe.equals("3year") || timeframe.equals("all")) {
//            temp = "Monthly"; //because its custom. for me
//        }
//        final String timeframeToAPI = temp;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                String apiUrl = baseApiUrl + timeframeToAPI + "&symbol=" + symbol + "&apikey=" + apiKey;

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
            return parseStockData(jsonResponse[0]);

        }
        catch (Exception e)
        {e.printStackTrace();}{
            return new ArrayList<>();
        }
    }


    private List<Entry> parseStockData(JSONObject jsonData) throws JSONException {
        List<Entry> entries = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    JSONArray resultsArray = jsonData.getJSONArray("results");

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        float price = (float) result.getDouble("c");
                        long timestamp = result.getLong("t");
                        // i will handle timestamp later
                        entries.add(new Entry(timestamp, price));
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



    public List<Entry> listForChart(String symbol, String timeframe)  {
        String timeSpan;
        String fromDate;
        String toDate;

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        toDate = dateFormat.format(today);

        switch(timeframe) {
            case "1D":
                timeSpan = "hour";
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "5D":
                timeSpan = "day";
                calendar.add(Calendar.DAY_OF_YEAR, -5);
                break;
            case "1M":
                timeSpan = "day";
                calendar.add(Calendar.MONTH, -1);
                break;
            case "6M":
                timeSpan = "day";
                calendar.add(Calendar.MONTH, -6);
                break;
            case "1Y":
                timeSpan = "month";
                calendar.add(Calendar.YEAR, -1);
                break;
            case "5Y":
                timeSpan = "month";
                calendar.add(Calendar.YEAR, -5);
                break;
            default:
                //max
                timeSpan = "month";
                calendar.add(Calendar.YEAR, -10);
                break;
        }
        fromDate = dateFormat.format(calendar.getTime());

        String apiUrl = "https://api.polygon.io/v2/aggs/ticker/" +
                symbol + "/range/1/" + timeSpan + "/" + fromDate + "/" + toDate +
                "?adjusted=true&sort=asc&limit=50000&apiKey=" + apiKey;




        List<Entry> res = new ArrayList<>();
        try {
            return fetchStockData(apiUrl);
        }
        catch (Exception e) {
            e.printStackTrace();
            return res;
        }

    }



}
