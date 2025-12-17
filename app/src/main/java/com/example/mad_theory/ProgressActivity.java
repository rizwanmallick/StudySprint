package com.example.mad_theory;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvTotalStudyTime;
    private TextView tvCompletedTasks;
    private TextView tvCompletionRate;
    private TextView tvStreakDays;
    private ProgressBar progressBarCompletion;
    
    private RecyclerView recyclerViewWeeklyProgress;
    private RecyclerView recyclerViewSubjectProgress;
    
    private TaskDBhelper taskDb;
    private WeeklyProgressAdapter weeklyAdapter;
    private SubjectProgressAdapter subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        initializeViews();
        initializeData();
        loadProgressData();
    }

    private void initializeViews() {
        tvTotalStudyTime = findViewById(R.id.tvTotalStudyTime);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvStreakDays = findViewById(R.id.tvStreakDays);
        progressBarCompletion = findViewById(R.id.progressBarCompletion);
        
        recyclerViewWeeklyProgress = findViewById(R.id.recyclerViewWeeklyProgress);
        recyclerViewSubjectProgress = findViewById(R.id.recyclerViewSubjectProgress);
    }

    private void initializeData() {
        taskDb = new TaskDBhelper(this);
        
        // Setup RecyclerViews
        weeklyAdapter = new WeeklyProgressAdapter();
        recyclerViewWeeklyProgress.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWeeklyProgress.setAdapter(weeklyAdapter);
        
        subjectAdapter = new SubjectProgressAdapter();
        recyclerViewSubjectProgress.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubjectProgress.setAdapter(subjectAdapter);
    }

    private void loadProgressData() {
        // This Week Overview
        int totalStudyMinutes = taskDb.getTotalStudyTimeThisWeek();
        int completedTasks = taskDb.getCompletedTasksThisWeek();
        int totalTasks = taskDb.getTotalTasksThisWeek();
        int streak = taskDb.getStudyStreak();
        
        // Display total study time
        String studyTime = formatTime(totalStudyMinutes);
        tvTotalStudyTime.setText(studyTime);
        
        // Display completed tasks
        tvCompletedTasks.setText(String.valueOf(completedTasks));
        
        // Calculate and display completion rate
        int completionRate = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
        tvCompletionRate.setText(completionRate + "%");
        progressBarCompletion.setProgress(completionRate);
        
        // Display streak
        tvStreakDays.setText(streak + " days");
        
        // Load weekly breakdown
        List<TaskDBhelper.WeeklyProgress> weeklyData = taskDb.getWeeklyBreakdown();
        int maxMinutes = 0;
        for (TaskDBhelper.WeeklyProgress progress : weeklyData) {
            if (progress.studyMinutes > maxMinutes) {
                maxMinutes = progress.studyMinutes;
            }
        }
        if (maxMinutes == 0) maxMinutes = 1; // Avoid division by zero
        weeklyAdapter.updateData(weeklyData, maxMinutes);
        
        // Load subject performance
        List<TaskDBhelper.SubjectProgress> subjectData = taskDb.getSubjectPerformance();
        subjectAdapter.updateData(subjectData);
    }

    private String formatTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%dm", minutes);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProgressData(); // Refresh data when returning to this activity
    }
}