package com.example.myapplication;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final DecimalFormat format;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        format = new DecimalFormat("#.##");
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        tvContent.setText(format.format(e.getY()) + "$" + ", " + dateFormat.format(new Date((long) e.getX())));

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
