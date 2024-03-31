package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GraphController extends AppCompatActivity implements StockObserver{

    private Button button1D, button5D, button1M, button6M, button1Y, button5Y, buttonMAX;
    private GraphModel graphModel;
    private TextView textViewStockName;
    private StockManager stockManager;
    private String currentTimeFrame;
    private MarkerView customMarkerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_controller);

        initObject();
        initListeners();
        initChart();
        stockManager.addObserver(this); //register

        button1M.performClick(); // default i want to show the 1M
    }
    private void initObject(){
        graphModel = GraphModel.getInstance();
        stockManager = StockManager.getInstance();

        // init buttons
        button1D = findViewById(R.id.button1D);
        button5D = findViewById(R.id.button5D);
        button1M = findViewById(R.id.button1M);
        button6M = findViewById(R.id.button6M);
        button1Y = findViewById(R.id.button1Y);
        button5Y = findViewById(R.id.button5Y);
        buttonMAX = findViewById(R.id.buttonMAX);
        textViewStockName = findViewById(R.id.textViewStockName);
        textViewStockName.setText(isNullOrEmpty(stockManager.currentStock) ? ""  : stockManager.currentStock );
        customMarkerView = new MyMarkerView(this, R.layout.marker_view_layout);
    }

    private void initListeners(){
        button1D.setOnClickListener(v -> { fetchStockData("1D"); });
        button5D.setOnClickListener(v -> { fetchStockData("5D"); });
        button1M.setOnClickListener(v -> { fetchStockData("1M"); });
        button6M.setOnClickListener(v -> { fetchStockData("6M");  });
        button1Y.setOnClickListener(v -> { fetchStockData("1Y"); });
        button5Y.setOnClickListener(v -> { fetchStockData("5Y"); });
        buttonMAX.setOnClickListener(v -> { fetchStockData("MAX"); });
    }
    private void initChart(){
            //init chart
            LineChart lineChart = findViewById(R.id.lineChart);
            lineChart.getLegend().setTextColor(Color.WHITE);
            lineChart.getXAxis().setTextColor(Color.WHITE);
            lineChart.getAxisLeft().setTextColor(Color.WHITE);
            lineChart.getAxisRight().setTextColor(Color.WHITE);
            lineChart.setTouchEnabled(true);
            lineChart.setHighlightPerTapEnabled(true);
            lineChart.setDrawMarkers(true);
            lineChart.setMarker(customMarkerView);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(Color.WHITE);


            YAxis yAxis = lineChart.getAxisRight();
            yAxis.setTextColor(Color.WHITE);
            lineChart.getAxisLeft().setEnabled(false);
            lineChart.getXAxis().setLabelCount(5, true);
            lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                @Override
                public String getFormattedValue(float value) {
                    // i want to convert timestamp to date
                    Date date = new Date((long) value);
                    return dateFormat.format(date);
                }
            });

    }

    private void fetchStockData(String timeframe) {
        currentTimeFrame = timeframe;
        List<Entry> entries = graphModel.listForChart(stockManager.currentStock, timeframe);
        updateChart(entries);
    }


    private void updateChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Stock Price");
        LineData lineData = new LineData(dataSet);

        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
    @Override

    public void onStockDataChanged(StockModel stock) {
        // refresh graph online
        if(stock.getSymbol().equals(stockManager.currentStock))
        {
            fetchStockData(currentTimeFrame);
            textViewStockName.setText(isNullOrEmpty(stockManager.currentStock) ? ""  : stockManager.currentStock );
        }

    }

    private boolean isNullOrEmpty(String str){
        return str == null || str.equals("");
    }
}


