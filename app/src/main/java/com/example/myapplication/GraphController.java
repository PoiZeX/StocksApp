package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class GraphController extends AppCompatActivity {

    private Button buttonIntraday, buttonDaily, buttonWeekly, buttonMonthly;
    private GraphModel graphModel;

    private StockManager stockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_controller);
        graphModel = GraphModel.getInstance();
        stockManager = StockManager.getInstance();


        buttonIntraday = findViewById(R.id.buttonIntraday);
        buttonDaily = findViewById(R.id.buttonDaily);
        buttonWeekly = findViewById(R.id.buttonWeekly);
        buttonMonthly = findViewById(R.id.buttonMonthly);

//        //init chart
//        LineChart lineChart = findViewById(R.id.lineChart);
//        lineChart.getLegend().setTextColor(Color.WHITE);
//        lineChart.getXAxis().setTextColor(Color.WHITE);
//        lineChart.getAxisLeft().setTextColor(Color.WHITE);
//        lineChart.getAxisRight().setTextColor(Color.WHITE);
//        lineChart.setTouchEnabled(true);
//        lineChart.setHighlightPerTapEnabled(true);
//        lineChart.setDrawMarkers(true);
//        lineChart.setMarker(new MyMarkerView(this, R.layout.marker_view_layout));

        initChart();

        buttonIntraday.setOnClickListener(v -> {
            fetchStockData("intraday");
        });

        buttonDaily.setOnClickListener(v -> { fetchStockData("daily");  });

        buttonWeekly.setOnClickListener(v -> {
            fetchStockData("weekly");
        });

        buttonMonthly.setOnClickListener(v -> {
            // Handle monthly button click
            fetchStockData("monthly");
        });
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
    lineChart.setMarker(new MyMarkerView(this, R.layout.marker_view_layout));

    XAxis xAxis = lineChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setTextColor(Color.WHITE);
//    xAxis.setValueFormatter(new IndexAxisValueFormatter());

    YAxis yAxis = lineChart.getAxisLeft();
    yAxis.setTextColor(Color.WHITE);

    lineChart.getAxisRight().setEnabled(false);
}
    private void fetchStockData(String timeframe) {
//        graphModel.fetchStockData(stockManager.currentStock, timeframe);

        // get entries from model
        List<Entry> entries = graphModel.listForChart(stockManager.currentStock, timeframe);

        // update chart
        updateChart(entries);
        //    xAxis.setValueFormatter(new IndexAxisValueFormatter());


    }



    private void updateChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Stock Price");
        LineData lineData = new LineData(dataSet);

        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}


/*
1D
5D
1M
6M
YTD
1Y
5Y
MAX
 */
