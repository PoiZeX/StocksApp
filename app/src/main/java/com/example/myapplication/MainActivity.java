package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.text.TextWatcher;
import android.text.Editable;
import android.widget.AdapterView;


public class MainActivity extends AppCompatActivity {

    private EditText editTextStockName;
    private Button buttonAddStock;
    private TableLayout tableLayout;
    private String currentStock = "";
    private Set<String> displayedStocks = new HashSet<>();
    TextView textView;
    ArrayList<String> arrayList;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        initListeners();
        // fetch stock symbols when the activity starts
//        fetchStockSymbols();
//        StockManager.getInstance().getStockData();
//        StockManager.getInstance().getAllStocksSymbols();
        for (String symbol : StockManager.getInstance().getAllStocksSymbols()) {
            arrayList.add(symbol);
        }
    }

    private void init() {
        // find view by id section
        buttonAddStock = findViewById(R.id.buttonAddStock);
        tableLayout = findViewById(R.id.tableLayout);
        textView = findViewById(R.id.testView);
        arrayList = new ArrayList<>();
    }


    private void initListeners() {

//        buttonAddStock.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String stockName = textView.getText().toString().toUpperCase();
//                if (!stockName.isEmpty()) {
//                    new FetchStockQuoteTask().execute(stockName);
//                }
//            }
//        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new...
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_spinner, null);
                builder.setView(dialogView);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // set transparent background

                EditText editText = dialogView.findViewById(R.id.edit_text);
                ListView listView = dialogView.findViewById(R.id.list_view);

                // set adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        textView.setText(adapter.getItem(position));
                        dialog.dismiss();
                        onItemSelected();
                    }
                });

                dialog.show();
            }
        });
    }

    private void onItemSelected(){
        // when clicking on item from combobox
        //                        buttonAddStock.performClick();
        String stockName = textView.getText().toString().toUpperCase();
        if (!stockName.isEmpty()) {
            Stock s = StockManager.getInstance().getStockData(stockName);
            currentStock = s.getSymbol();
            if (!isStockDisplayed(currentStock)) {
                addStockToTable(currentStock, s.getPrice(), s.getDailyChange());
            } else {
                showToast("Stock already exists in the table");
            }
        }
    }
    // fetch stock symbols from the API
    private void fetchStockSymbols() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String stockSymbolsJson = null;

                try {
                    String apiKey = "cnjibnhr01qmfbtbdcggcnjibnhr01qmfbtbdch0";
                    String apiUrl = "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + apiKey;
                    URL url = new URL(apiUrl);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        return null;
                    }
                    stockSymbolsJson = buffer.toString();
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
                return stockSymbolsJson;
            }

            @Override
            protected void onPostExecute(String stockSymbolsJson) {
                if (stockSymbolsJson != null) {
                    try {
                        JSONArray symbolsArray = new JSONArray(stockSymbolsJson);
                        for (int i = 0; i < symbolsArray.length(); i++) {
                            JSONObject stockObject = symbolsArray.getJSONObject(i);
                            String symbol = stockObject.getString("symbol");
                            arrayList.add(symbol);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private class FetchStockQuoteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String symbol = params[0];
            currentStock = symbol; // keep for later to add row
            String apiKey = "cnjibnhr01qmfbtbdcggcnjibnhr01qmfbtbdch0";
            String apiUrl = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + apiKey;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String quoteJsonString = null;

            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                quoteJsonString = buffer.toString();
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

            return quoteJsonString;
        }

        @Override
        protected void onPostExecute(String quoteJsonString) {
            if (quoteJsonString != null) {
                try {
                    JSONObject quoteJson = new JSONObject(quoteJsonString);
                    double price = quoteJson.getDouble("c");
                    double dailyChange = quoteJson.getDouble("d");

                    if (!isStockDisplayed(currentStock)) {
                        addStockToTable(currentStock, price, dailyChange);
                    } else {
                        showToast("Stock already exists in the table");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("Stock not found");
                }
            } else {
                showToast("Stock not found");
            }
        }
    }

    private void addStockToTable(String symbol, double price, double dailyChange) {
        TableRow row = new TableRow(this);

        TextView symbolTextView = new TextView(this);
        symbolTextView.setText(symbol);
        row.addView(symbolTextView);

        TextView priceTextView = new TextView(this);
        priceTextView.setText(String.valueOf(price));
        row.addView(priceTextView);

        TextView dailyChangeTextView = new TextView(this);
        dailyChangeTextView.setText(String.valueOf(dailyChange));
        row.addView(dailyChangeTextView);

        // Adding click listener to remove the stock
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeStockConfirmation(symbol, row);
            }
        });

        tableLayout.addView(row);
        displayedStocks.add(symbol);
    }

    private void removeStockConfirmation(String symbol, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Stock");
        builder.setMessage("Are you sure you want to remove this stock?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Remove stock from the table
            tableLayout.removeView(row);
            displayedStocks.remove(symbol);
            showToast("Stock removed successfully");
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isStockDisplayed(String symbol) {
        return displayedStocks.contains(symbol);
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}