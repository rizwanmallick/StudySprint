package com.example.mad_theory;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewReminders;
    private DashboardTaskAdapter adapter;
    private TaskDBhelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DashboardTaskAdapter();
        recyclerViewReminders.setAdapter(adapter);

        db = new TaskDBhelper(this);
        loadReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        List<StudyTask> tasks = db.getAllTasksForToday();
        // sort by start_time
        Collections.sort(tasks, (a, b) -> timeToMinutes(a.getStartTime()) - timeToMinutes(b.getStartTime()));
        adapter.updateTasks(tasks);
    }

    private int timeToMinutes(String t) {
        try {
            String[] parts = t.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
