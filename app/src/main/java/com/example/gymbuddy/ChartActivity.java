package com.example.gymbuddy;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerImage;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {
    private TextView txtDateH, txtTotalH, txtLxR1H, txtLxR2H, txtLxR3H, txtLxR4H, txtLxR5H, txtLxR6H, txtLxR7H, txtLxR8H;
    private View separator3H, separator4H, separator5H, separator6H;
    private TextView txtDateB, txtTotalB, txtLxR1B, txtLxR2B, txtLxR3B, txtLxR4B, txtLxR5B, txtLxR6B, txtLxR7B, txtLxR8B;
    private View separator3B, separator4B, separator5B, separator6B;
    private ArrayList<TextView> txtLxRH = new ArrayList<>();
    private ArrayList<TextView> txtLxRB = new ArrayList<>();
    private TextView txtTotalProgress, txtLastProgress, txtLastProgressPercentage, txtTotalProgressPercentage;

    private int position;
    private float bestTotal = 0;
    private float total = 0;
    private int bestSessionPosition;
    private Exercise exercise;
    private ArrayList<Session> sessions;
    private ArrayList<Entry> entries = new ArrayList<>();
    private LineDataSet set1;
    private ArrayList<ILineDataSet> dataSet;
    private LineData data;
    private LineChart exerciseChart;
//    private AdView chartAd;
    private FrameLayout chartAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Intent intent = getIntent();
        exercise = (Exercise) intent.getSerializableExtra(Helpers.CHART_KEY);
        sessions = exercise.getSessions();
        for (int i = 0; i < sessions.size(); i++) {
            total = Helpers.arrayToTotal(sessions.get(i).getReps(), sessions.get(i).getLoad());
            if (total > bestTotal) {
                bestTotal = total;
                bestSessionPosition = i;
            }
            entries.add(0, new Entry(Helpers.stringDateToMillis(sessions.get(i).getDate()), total));
            entries.get(0).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_hexagon_icon));
        }

        initViews();

        Helpers.handleAds(chartAdContainer,this);

        txtTotalProgress.setText(Helpers.stringFormat(entries.get(entries.size()-1).getY()-entries.get(0).getY()));
        txtLastProgress.setText(Helpers.stringFormat(entries.get(entries.size()-1).getY()-entries.get(entries.size()-2).getY()));
        txtTotalProgressPercentage.setText(Helpers.stringFormatPercentage((entries.get(entries.size()-1).getY()-entries.get(0).getY())/entries.get(0).getY()*100)+"%");
        txtLastProgressPercentage.setText(Helpers.stringFormatPercentage((entries.get(entries.size()-1).getY()-entries.get(entries.size()-2).getY())/entries.get(entries.size()-2).getY()*100)+"%");


        for(int i=0; i<exercise.getSets();i++){
            txtLxRB.get(i).setText(Helpers.stringFormat(Math.round(10.0 * sessions.get(bestSessionPosition).getLoad()[i]) / 10.0) + "x" + sessions.get(bestSessionPosition).getReps()[i]);
        }
        txtDateB.setText(sessions.get(bestSessionPosition).getDate());
        txtTotalB.setText(String.valueOf(entries.get(entries.size()-1-bestSessionPosition).getY()));

        Helpers.setupActionBar(exercise.getName() + getResources().getString(R.string.progress),"",getSupportActionBar(),this);

        exerciseChart = findViewById(R.id.exerciseChart);

        dataSet = new ArrayList<>();
        dataSet.add(newSet(entries));
        data = new LineData(dataSet);
        exerciseChart.setData(data);

        exerciseChart.getAxisRight().setEnabled(false);
        exerciseChart.getLegend().setEnabled(false);
        exerciseChart.getDescription().setEnabled(false);
        exerciseChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        exerciseChart.getXAxis().setTextColor(ContextCompat.getColor(this,R.color.white));
        exerciseChart.getXAxis().setAxisLineWidth(2);
        exerciseChart.getAxisLeft().setDrawGridLines(false);
        exerciseChart.getAxisLeft().setTextColor(ContextCompat.getColor(this,R.color.orange_500));
        exerciseChart.getAxisLeft().setTextSize(15);
        exerciseChart.getAxisLeft().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        exerciseChart.getXAxis().setDrawGridLines(false);
        exerciseChart.getAxisLeft().setAxisLineWidth(2);
        exerciseChart.getXAxis().setValueFormatter(new LineChartXAxisValueFormatter(0));
        MarkerImage markerImage = new MarkerImage(this, R.drawable.ic_hexagon_marker);
        markerImage.setOffset(-13, -13);
        exerciseChart.setMarker(markerImage);
        exerciseChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                for(int i=0;i<entries.size();i++){
                    if(e.equalTo(entries.get(i))){
                        position=i;
                        viewHighlighted(position);
                    }
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
        exerciseChart.highlightValue(entries.get(entries.size() - 1).getX(), 0, true);
    }

    private LineDataSet newSet(ArrayList<Entry> yValues) {
        set1 = new LineDataSet(yValues, "Dataset1");
        set1.setHighLightColor(ContextCompat.getColor(this,R.color.orange_500));
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setDrawVerticalHighlightIndicator(false);
        set1.setHighlightLineWidth(0f);
        set1.setFillAlpha(110);
        set1.setColor(ContextCompat.getColor(this,R.color.orange_500));
        set1.setCircleRadius(2);
        set1.setCircleColor(ContextCompat.getColor(this,R.color.orange_700));
        set1.setCircleHoleColor(ContextCompat.getColor(this,R.color.grey_500));
        set1.setCircleHoleRadius(1);
        set1.setDrawValues(false);
        set1.setValueTextColor(ContextCompat.getColor(this,R.color.grey_200));
        set1.setLineWidth(4);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setCubicIntensity(0.2f);
        return set1;
    }

    private void initViews() {
        txtDateH = findViewById(R.id.txtDateH);
        txtTotalH = findViewById(R.id.txtTotalH);
        txtLxR1H = findViewById(R.id.txtLxR1H);
        txtLxR2H = findViewById(R.id.txtLxR2H);
        txtLxR3H = findViewById(R.id.txtLxR3H);
        txtLxR4H = findViewById(R.id.txtLxR4H);
        txtLxR5H = findViewById(R.id.txtLxR5H);
        txtLxR6H = findViewById(R.id.txtLxR6H);
        txtLxR7H = findViewById(R.id.txtLxR7H);
        txtLxR8H = findViewById(R.id.txtLxR8H);
        txtLxRH.add(txtLxR1H);
        txtLxRH.add(txtLxR2H);
        txtLxRH.add(txtLxR3H);
        txtLxRH.add(txtLxR4H);
        txtLxRH.add(txtLxR5H);
        txtLxRH.add(txtLxR6H);
        txtLxRH.add(txtLxR7H);
        txtLxRH.add(txtLxR8H);
        separator3H = findViewById(R.id.separator3H);
        separator4H = findViewById(R.id.separator4H);
        separator5H = findViewById(R.id.separator5H);
        separator6H = findViewById(R.id.separator6H);
        txtDateB = findViewById(R.id.txtDateB);
        txtTotalB = findViewById(R.id.txtTotalB);
        txtLxR1B = findViewById(R.id.txtLxR1B);
        txtLxR2B = findViewById(R.id.txtLxR2B);
        txtLxR3B = findViewById(R.id.txtLxR3B);
        txtLxR4B = findViewById(R.id.txtLxR4B);
        txtLxR5B = findViewById(R.id.txtLxR5B);
        txtLxR6B = findViewById(R.id.txtLxR6B);
        txtLxR7B = findViewById(R.id.txtLxR7B);
        txtLxR8B = findViewById(R.id.txtLxR8B);
        txtLxRB.add(txtLxR1B);
        txtLxRB.add(txtLxR2B);
        txtLxRB.add(txtLxR3B);
        txtLxRB.add(txtLxR4B);
        txtLxRB.add(txtLxR5B);
        txtLxRB.add(txtLxR6B);
        txtLxRB.add(txtLxR7B);
        txtLxRB.add(txtLxR8B);
        separator3B = findViewById(R.id.separator3B);
        separator4B = findViewById(R.id.separator4B);
        separator5B = findViewById(R.id.separator5B);
        separator6B = findViewById(R.id.separator6B);
        if (exercise.getSets() >= 4) {
            txtLxR4H.setVisibility(View.VISIBLE);
            separator3H.setVisibility(View.VISIBLE);
            txtLxR4B.setVisibility(View.VISIBLE);
            separator3B.setVisibility(View.VISIBLE);
        } else {
            txtLxR4H.setVisibility(View.GONE);
            separator3H.setVisibility(View.GONE);
            txtLxR4B.setVisibility(View.GONE);
            separator3B.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 5) {
            txtLxR5H.setVisibility(View.VISIBLE);
            txtLxR5B.setVisibility(View.VISIBLE);
        } else {
            txtLxR5H.setVisibility(View.GONE);
            txtLxR5B.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 6) {
            txtLxR6H.setVisibility(View.VISIBLE);
            separator4H.setVisibility(View.VISIBLE);
            txtLxR6B.setVisibility(View.VISIBLE);
            separator4B.setVisibility(View.VISIBLE);
        } else {
            txtLxR6H.setVisibility(View.GONE);
            separator4H.setVisibility(View.GONE);
            txtLxR6B.setVisibility(View.GONE);
            separator4B.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 7) {
            txtLxR7H.setVisibility(View.VISIBLE);
            separator5H.setVisibility(View.VISIBLE);
            txtLxR7B.setVisibility(View.VISIBLE);
            separator5B.setVisibility(View.VISIBLE);
        } else {
            txtLxR7H.setVisibility(View.GONE);
            separator5H.setVisibility(View.GONE);
            txtLxR7B.setVisibility(View.GONE);
            separator5B.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 8) {
            txtLxR8H.setVisibility(View.VISIBLE);
            separator6H.setVisibility(View.VISIBLE);
            txtLxR8B.setVisibility(View.VISIBLE);
            separator6B.setVisibility(View.VISIBLE);
        } else {
            txtLxR8H.setVisibility(View.GONE);
            separator6H.setVisibility(View.GONE);
            txtLxR8B.setVisibility(View.GONE);
            separator6B.setVisibility(View.GONE);
        }
        txtTotalProgress=findViewById(R.id.txtTotalProgress);
        txtLastProgress=findViewById(R.id.txtLastProgress);
        txtLastProgressPercentage=findViewById(R.id.txtLastProgressPercentage);
        txtTotalProgressPercentage=findViewById(R.id.txtTotalProgressPercentage);
        chartAdContainer=findViewById(R.id.chartAdContainer);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        chartAd.loadAd(adRequest);

    }

    private void viewHighlighted(int position) {
        for(int i=0; i<exercise.getSets();i++){
            txtLxRH.get(i).setText(Helpers.stringFormat(Math.round(10.0 * sessions.get(sessions.size()-1-position).getLoad()[i]) / 10.0) + "x" + sessions.get(sessions.size()-1-position).getReps()[i]);
        }
        txtDateH.setText(sessions.get(sessions.size()-1-position).getDate());
        txtTotalH.setText(String.valueOf(entries.get(position).getY()));
    }


}