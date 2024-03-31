package com.example.myapplication;

import android.provider.ContactsContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ApiCalls {
    // i made this class static because it not depends on anything. (it doesn't maintain data)
    // Facade and Strategy


    public static String getBuffer(String apiUrl, DataProcessor dataProcessor) {
        return fetchBuffer(apiUrl, dataProcessor);
    }

    private static String fetchBuffer(String apiUrl, DataProcessor dataProcessor) {
        StringBuilder buffer = new StringBuilder();

        Thread thread = new Thread(new Runnable() {   // new thread to open connection not from the main gthread.
            @Override
            public void run() {
                try {
                    // encapsulate the implementation calls/ facade
                    HttpURLConnection urlConnection = openConnection(apiUrl);
                    buffer.append(readResponse(urlConnection));
                    dataProcessor.processData(buffer.toString()); // Strategy pattern

                } catch (IOException e) { e.printStackTrace();  }
            }
        });

        thread.start();

        try {
            thread.join();  // wait to finis
        } catch (InterruptedException e) { e.printStackTrace();  }

        return buffer.toString();
    }

    private static HttpURLConnection openConnection(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        return urlConnection;
    }

    private static StringBuilder readResponse(HttpURLConnection urlConnection) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        try {
            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            urlConnection.disconnect();
        }
        return buffer;
    }
}


