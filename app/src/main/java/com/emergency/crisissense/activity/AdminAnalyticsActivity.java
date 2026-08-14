package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.util.FirebaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsActivity extends AppCompatActivity {
    private PieChart pieChart;
    private BarChart barChart;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        pieChart = findViewById(R.id.pie_chart_types);
        barChart = findViewById(R.id.bar_chart_severity);

        firebaseHelper = new FirebaseHelper();

        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        firebaseHelper.getIncidents(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                Map<String, Integer> typeCounts = new HashMap<>();
                Map<String, Integer> severityCounts = new HashMap<>();

                // Initialize maps
                severityCounts.put("low", 0);
                severityCounts.put("medium", 0);
                severityCounts.put("high", 0);
                severityCounts.put("critical", 0);

                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Incident incident = doc.toObject(Incident.class);
                    if (incident != null) {
                        String type = incident.getType();
                        typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);

                        String severity = incident.getSeverity().toLowerCase();
                        severityCounts.put(severity, severityCounts.getOrDefault(severity, 0) + 1);
                    }
                }

                populatePieChart(typeCounts);
                populateBarChart(severityCounts);
            } else {
                Toast.makeText(AdminAnalyticsActivity.this, "Offline Mode: Loading simulated metrics.", Toast.LENGTH_SHORT).show();
                loadMockAnalytics();
            }
        });
    }

    private void loadMockAnalytics() {
        // Pie Chart Mock Data
        Map<String, Integer> mockTypes = new HashMap<>();
        mockTypes.put("Fire", 4);
        mockTypes.put("Flood", 2);
        mockTypes.put("Accident", 5);
        mockTypes.put("Medical", 3);
        mockTypes.put("Crime", 2);
        mockTypes.put("Other", 1);
        populatePieChart(mockTypes);

        // Bar Chart Mock Data
        Map<String, Integer> mockSeverity = new HashMap<>();
        mockSeverity.put("low", 3);
        mockSeverity.put("medium", 5);
        mockSeverity.put("high", 4);
        mockSeverity.put("critical", 2);
        populateBarChart(mockSeverity);
    }

    private void populatePieChart(Map<String, Integer> typeCounts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(R.color.white));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Incident Types");
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void populateBarChart(Map<String, Integer> severityCounts) {
        List<BarEntry> entries = new ArrayList<>();
        
        float lowVal = severityCounts.getOrDefault("low", 0);
        float medVal = severityCounts.getOrDefault("medium", 0);
        float highVal = severityCounts.getOrDefault("high", 0);
        float critVal = severityCounts.getOrDefault("critical", 0);

        entries.add(new BarEntry(0f, lowVal));
        entries.add(new BarEntry(1f, medVal));
        entries.add(new BarEntry(2f, highVal));
        entries.add(new BarEntry(3f, critVal));

        BarDataSet dataSet = new BarDataSet(entries, "Severity Count");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);

        // Set Labels on X axis
        String[] labels = new String[]{"Low", "Medium", "High", "Critical"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }
}
