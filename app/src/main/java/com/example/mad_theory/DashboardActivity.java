package com.example.mad_theory;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.ImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout llStudyPlan, llFocusTimer, llLogs, llProgress, llReminder, llSettings;
    FloatingActionButton btnAddTask;
    TextView tvDate, tvGreeting, tvStudyTime, tvTaskCount;
    ImageView profileIcon;
    ProgressBar progressBar;
    LinearLayout emptyStateLayout;
    RecyclerView recyclerViewTodayTasks;

    TaskDBhelper taskDb;
    String todayDate;
    DashboardTaskAdapter taskAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Views
        llStudyPlan = findViewById(R.id.llStudyPlan);
        llFocusTimer = findViewById(R.id.llFocusTimer);
        llLogs = findViewById(R.id.llLogs);
        llProgress = findViewById(R.id.llProgress);
        llReminder = findViewById(R.id.llReminder);
        llSettings = findViewById(R.id.llSettings);
        btnAddTask = findViewById(R.id.btnAddTask);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStudyTime = findViewById(R.id.tvStudyTime);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        recyclerViewTodayTasks = findViewById(R.id.recyclerViewTodayTasks);
        profileIcon = findViewById(R.id.profileIcon);

        // Database setup
        taskDb = new TaskDBhelper(this);
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Setup RecyclerView for today's tasks
        recyclerViewTodayTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new DashboardTaskAdapter();
        recyclerViewTodayTasks.setAdapter(taskAdapter);

        // Set Date & Greeting
        String currentDate = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(new Date());
        tvDate.setText("Date: " + currentDate);
        UserPrefs userPrefs = new UserPrefs(this);
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String name = firebaseUser.getDisplayName();
        if ((name == null || name.trim().isEmpty()) && userPrefs.getName() != null) {
            name = userPrefs.getName();
        }
        if (name == null || name.trim().isEmpty()) {
            tvGreeting.setText("Hello 👋");
        } else {
            tvGreeting.setText("Hello, " + name + " 👋");
        }

        // Update dashboard initially
        updateDashboard();

        // Button Clicks
        llStudyPlan.setOnClickListener(v -> startActivity(new Intent(this, StudyPlanActivity.class)));
        llFocusTimer.setOnClickListener(v -> startActivity(new Intent(this, FocusTimerActivity.class)));
        llLogs.setOnClickListener(v -> startActivity(new Intent(this, AttendanceActivity.class)));
        llProgress.setOnClickListener(v -> startActivity(new Intent(this, ProgressActivity.class)));
        llReminder.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));
        llSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Add Task Button
        btnAddTask.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));

        // Profile icon
        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }


    private void updateDashboard() {
        int taskCount = taskDb.getTaskCountForToday();
        int completedTasks = taskDb.getCompletedTaskCountForToday();
        int totalStudyTime = taskDb.getTotalStudyTimeForToday();

        // Format study time display
        String studyTimeText;
        if (totalStudyTime > 0) {
            int hours = totalStudyTime / 60;
            int minutes = totalStudyTime % 60;
            if (hours > 0) {
                studyTimeText = String.format("%dh %dm", hours, minutes);
            } else {
                studyTimeText = String.format("%dm", minutes);
            }
        } else {
            studyTimeText = "0h 0m";
        }

        tvTaskCount.setText(taskCount + " tasks");
        tvStudyTime.setText("Today's Study Time: " + studyTimeText);

        // Progress bar based on completed tasks
        int progress = taskCount > 0 ? (completedTasks * 100) / taskCount : 0;
        progressBar.setProgress(progress);

        // Update today's tasks display
        updateTodayTasksDisplay();
    }

    private void updateTodayTasksDisplay() {
        List<StudyTask> todayTasks = taskDb.getAllTasksForToday();
        
        if (todayTasks.isEmpty()) {
            // Show empty state
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewTodayTasks.setVisibility(View.GONE);
        } else {
            // Show task list
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewTodayTasks.setVisibility(View.VISIBLE);
            taskAdapter.updateTasks(todayTasks);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data when returning from other activities
        UserPrefs userPrefs = new UserPrefs(this);
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String name = firebaseUser.getDisplayName();
        if ((name == null || name.trim().isEmpty()) && userPrefs.getName() != null) {
            name = userPrefs.getName();
        }
        if (name == null || name.trim().isEmpty()) {
            tvGreeting.setText("Hello 👋");
        } else {
            tvGreeting.setText("Hello, " + name + " 👋");
        }
        updateDashboard();
    }
}
