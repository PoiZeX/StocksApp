package com.example.myapplication;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainController extends AppCompatActivity implements StockObserver  {

    //region binding
    private TableLayout tableLayout;
    private String currentStock = "";
    private Set<String> displayedStocks = new HashSet<>();
    TextView textView;
    ArrayList<String> arrayList;
    AlertDialog dialog;
//endregion


    private StockManager stockManager;  // MODEL
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL = 60*1000; // 60 sec for now


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // usual thing... bind the view to controller
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init as functions
        init();
        initListeners();
        scheduleStockDataRefresh();  // start refresh thrnead

    }

    //region init
    private void init() {
        // attach view by id section
        tableLayout = findViewById(R.id.tableLayout);
        textView = findViewById(R.id.testView);
        arrayList = new ArrayList<>();

        // Create objects
        stockManager = StockManager.getInstance();

        // fill data
        arrayList.addAll(stockManager.getAllStocksSymbols());

        // register
        stockManager.addObserver(this);

    }


    private void initListeners() {

        // onClick for the combobox dialog (textview at first)
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new...
                AlertDialog.Builder builder = new AlertDialog.Builder(MainController.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_spinner, null);
                builder.setView(dialogView);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // set transparent background
                //find in new
                EditText editText = dialogView.findViewById(R.id.edit_text);
                ListView listView = dialogView.findViewById(R.id.list_view);

                // set adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainController.this, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.getFilter().filter(s); }

                    @Override
                    public void afterTextChanged(Editable s) {       }
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
//endregion

    private void onItemSelected(){
        // when clicking on item from combobox (dialog)
        // the view triggered this, the controller call the model to get data and return this to view again :)

        String stockName = textView.getText().toString().toUpperCase();
        if (!stockName.isEmpty()) {
            StockModel stock = stockManager.getStockData(stockName);
            currentStock = stock.getSymbol();
            if (!isStockDisplayed(currentStock)) {
                addStockToTable(currentStock, stock.getPrice(), stock.getDailyChange());
            } else {
                showToast("Stock already exists in the table");
            }
        }
    }

//    @Override
//    public void processData(String bufferData){
//        // must implement the Strategy
//
//        // get buffer to
//
//        // add stock to table
//    }
    private void addStockToTable(String symbol, double price, double dailyChange) {
        // creating new row and new textview for each column (as tamir wants..)
        TableRow row = new TableRow(this);

        TextView symbolTextView = new TextView(this);
        symbolTextView.setText(symbol);
        symbolTextView.setTextSize(18);
        row.addView(symbolTextView);

        TextView priceTextView = new TextView(this);
        priceTextView.setText(String.valueOf(price));
        priceTextView.setTextSize(18);
        row.addView(priceTextView);

        TextView dailyChangeTextView = new TextView(this);
        dailyChangeTextView.setText(String.valueOf(dailyChange));
        dailyChangeTextView.setTextSize(18);
        dailyChangeTextView.setTextColor(dailyChange > 0 ? Color.GREEN : Color.RED);
        row.addView(dailyChangeTextView);

        row.setPadding(0, 10, 0, 0);

        // adding click listener to remove the stock
        row.setOnClickListener(
        new View.OnClickListener() {
                @Override
            public void onClick(View v) {
                    showOptionsDialog(row);
            }
        });

        // update view :)
        tableLayout.addView(row);
        displayedStocks.add(symbol);
    }

    private void showOptionsDialog(TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action");
        builder.setItems(new CharSequence[]{"Show Graph", "Remove Stock"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedSymbolStock = ((TextView) row.getChildAt(0)).getText().toString();
                switch (which) {
                    case 0:
                        stockManager.currentStock = selectedSymbolStock;
                        Intent intent = new Intent(MainController.this, GraphController.class);
                        startActivity(intent);
                        break;
                    case 1:
                        // remove stock
                        removeStockConfirmation(selectedSymbolStock, row);
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void removeStockConfirmation(String symbol, TableRow row) {
        // create new dialog view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Stock");
        builder.setMessage("Are you sure you want to remove this stock?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // remove stock from the table
            tableLayout.removeView(row);
            displayedStocks.remove(symbol);
            showToast("Stock removed successfully");
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //region utils
    private boolean isStockDisplayed(String symbol) {
        return displayedStocks.contains(symbol);
    }

    private void showToast(String message) {
        Toast.makeText(MainController.this, message, Toast.LENGTH_SHORT).show();
    }


    //endregion

    //region data refresh
    @Override
    public void onStockDataChanged(StockModel stock) {
        // handle the updated stock data
        updateStockDataInTable(stock);
    }

    private void scheduleStockDataRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStockDataForAllDisplayedStocks();

                // keep refresh
                mHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void refreshStockDataForAllDisplayedStocks() {
        for (String symbol : displayedStocks)
            stockManager.refreshStockData(symbol);
    }

//endregion

    private void updateStockDataInTable(StockModel stock) {
        //find
        TableRow rowToUpdate = findTableRowBySymbol(stock.getSymbol());

        // update
        if (rowToUpdate != null) {

            // finding by index (that i know its constant) instead of saving instances of rows...
            TextView priceTextView = (TextView) rowToUpdate.getChildAt(1);
            TextView dailyChangeTextView = (TextView) rowToUpdate.getChildAt(2);
            dailyChangeTextView.setTextColor(stock.getDailyChange() > 0 ? Color.GREEN : Color.RED);

            // updating
            priceTextView.setText(String.valueOf(stock.getPrice()));
            dailyChangeTextView.setText(String.valueOf(stock.getDailyChange()));

        }
    }

    private TableRow findTableRowBySymbol(String symbol) {
        // find the child with this symbol to update
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View childView = tableLayout.getChildAt(i);
            if (childView instanceof TableRow) {
                TableRow row = (TableRow) childView;
                TextView symbolTextView = (TextView) row.getChildAt(0);
                String rowSymbol = symbolTextView.getText().toString();
                if (rowSymbol.equals(symbol)) {
                    return row; // found
                }
            }
        }
        return null; // not found
    }

}