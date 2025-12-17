package com.example.mad_theory;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_CHECKED_IN = "checked_in";
    private static final String KEY_CHECK_IN_TIME = "check_in_time";
    private static final String KEY_SESSION_START_TIME = "session_start_time";

    private TextView tvSessionDate;
    private TextView tvSessionLive;
    private TextView tvStudiedTime;
    private TextView tvGoalTime;
    private TextView tvProgressPercent;
    private TextView tvTodayStudied;
    private TextView tvTodayGoal;
    private ProgressBar progressBar;
    private Button btnCheckInOut;

    private TaskDBhelper taskDb;
    private SharedPreferences attendancePrefs;
    private Handler timerHandler;
    private Runnable timerRunnable;

    private boolean isCheckedIn = false;
    private long checkInTime = 0;
    private long totalGoalTimeMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Daily Attendance");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        initializeData();
        setupClickListeners();
        updateUI();
    }

    private void initializeViews() {
        tvSessionDate = findViewById(R.id.tvSessionDate);
        tvSessionLive = findViewById(R.id.tvSessionLive);
        tvStudiedTime = findViewById(R.id.tvStudiedTime);
        tvGoalTime = findViewById(R.id.tvGoalTime);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBar = findViewById(R.id.progressBar);
        btnCheckInOut = findViewById(R.id.btnCheckInOut);
        tvTodayStudied = findViewById(R.id.tvTodayStudied);
        tvTodayGoal = findViewById(R.id.tvTodayGoal);
    }

    private void initializeData() {
        taskDb = new TaskDBhelper(this);
        attendancePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        timerHandler = new Handler();

        // Load check-in status
        isCheckedIn = attendancePrefs.getBoolean(KEY_CHECKED_IN, false);
        checkInTime = attendancePrefs.getLong(KEY_CHECK_IN_TIME, 0);

        // Set current date
        String currentDate = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(new Date());
        tvSessionDate.setText(currentDate);

        // Calculate goal time from today's study plan
        calculateGoalTime();
        updateTodayProgress();
    }

    private void calculateGoalTime() {
        List<StudyTask> todayTasks = taskDb.getAllTasksForToday();
        totalGoalTimeMinutes = 0;

        for (StudyTask task : todayTasks) {
            // Parse duration from "1h 30m" format
            String duration = task.getTargetDuration();
            if (duration != null && !duration.isEmpty()) {
                totalGoalTimeMinutes += parseDurationToMinutes(duration);
            }
        }

        // Update goal time display
        tvGoalTime.setText(formatMinutesToTime(totalGoalTimeMinutes));
    }

    private long parseDurationToMinutes(String duration) {
        try {
            if (duration.contains("h") && duration.contains("m")) {
                // Format: "1h 30m"
                String[] parts = duration.split("h");
                int hours = Integer.parseInt(parts[0].trim());
                String minutesPart = parts[1].replace("m", "").trim();
                int minutes = Integer.parseInt(minutesPart);
                return hours * 60 + minutes;
            } else if (duration.contains("h")) {
                // Format: "2h"
                String hoursPart = duration.replace("h", "").trim();
                return Integer.parseInt(hoursPart) * 60;
            } else if (duration.contains("min")) {
                // Format: "30 min"
                String minutesPart = duration.replace("min", "").trim();
                return Integer.parseInt(minutesPart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setupClickListeners() {
        btnCheckInOut.setOnClickListener(v -> {
            if (isCheckedIn) {
                checkOut();
            } else {
                checkIn();
            }
        });
    }

    private void checkIn() {
        isCheckedIn = true;
        checkInTime = System.currentTimeMillis();

        // Save to preferences
        SharedPreferences.Editor editor = attendancePrefs.edit();
        editor.putBoolean(KEY_CHECKED_IN, true);
        editor.putLong(KEY_CHECK_IN_TIME, checkInTime);
        editor.apply();

        // Start timer
        startTimer();

        // Update UI
        updateUI();
        Toast.makeText(this, "Checked in successfully!", Toast.LENGTH_SHORT).show();
    }

    private void checkOut() {
        isCheckedIn = false;

        // Save to preferences
        SharedPreferences.Editor editor = attendancePrefs.edit();
        editor.putBoolean(KEY_CHECKED_IN, false);
        editor.apply();

        // Stop timer
        stopTimer();

        // Show completed subjects dialog
        showCompletedSubjectsDialog();

        // Update UI
        updateUI();
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isCheckedIn) {
                    updateStudiedTime();
                    timerHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void updateStudiedTime() {
        if (checkInTime > 0) {
            long currentTime = System.currentTimeMillis();
            long studiedTimeMillis = currentTime - checkInTime;
            long studiedTimeMinutes = studiedTimeMillis / (1000 * 60);

            // Update studied time display
            tvStudiedTime.setText(formatMinutesToTime(studiedTimeMinutes));

            // Update progress
            updateProgress(studiedTimeMinutes);
        }
    }

    private void updateProgress(long studiedTimeMinutes) {
        if (totalGoalTimeMinutes > 0) {
            int progress = (int) ((studiedTimeMinutes * 100) / totalGoalTimeMinutes);
            progress = Math.min(progress, 100); // Cap at 100%
            
            progressBar.setProgress(progress);
            tvProgressPercent.setText(progress + "%");
        }
    }

    private void updateUI() {
        if (isCheckedIn) {
            btnCheckInOut.setText("Check Out");
            btnCheckInOut.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, null));
            tvSessionLive.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            btnCheckInOut.setText("Check In");
            btnCheckInOut.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
            tvSessionLive.setVisibility(View.GONE);
            stopTimer();
            // Reset live session counters
            checkInTime = 0;
            tvStudiedTime.setText("0m");
            progressBar.setProgress(0);
            tvProgressPercent.setText("0%");
            updateTodayProgress();
        }
    }

    private void showCompletedSubjectsDialog() {
        List<StudyTask> todayTasks = taskDb.getAllTasksForToday();
        
        if (todayTasks.isEmpty()) {
            Toast.makeText(this, "No study tasks found for today", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create custom view for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_completed_subjects, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewSubjects);
        Button btnMarkCompleted = dialogView.findViewById(R.id.btnMarkCompleted);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Setup RecyclerView
        CompletedSubjectsAdapter adapter = new CompletedSubjectsAdapter(todayTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        btnMarkCompleted.setOnClickListener(v -> {
            List<StudyTask> completedTasks = adapter.getCompletedTasks();
            markTasksAsCompleted(completedTasks);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void markTasksAsCompleted(List<StudyTask> completedTasks) {
        for (StudyTask task : completedTasks) {
            taskDb.markTaskAsComplete(task.getId());
        }
        
        String message = completedTasks.size() + " subject(s) marked as completed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Refresh goal time calculation and today progress
        calculateGoalTime();
        updateTodayProgress();
    }

    private String formatMinutesToTime(long totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + "m";
        } else {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "m";
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        calculateGoalTime();
        if (isCheckedIn) {
            updateStudiedTime();
        }
        updateTodayProgress();
    }

    private void updateTodayProgress() {
        int studied = taskDb.getTodayStudiedMinutes();
        int goal = taskDb.getTodayGoalMinutes();
        tvTodayStudied.setText(formatAsHHMM(studied));
        tvTodayGoal.setText(formatAsHHMM(goal));
    }

    private String formatAsHHMM(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02dh %02dm", h, m);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }
}