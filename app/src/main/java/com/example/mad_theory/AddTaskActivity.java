package com.example.mad_theory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import android.app.TimePickerDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddTaskActivity extends AppCompatActivity {

    Spinner spinnerSubject;
    TextView tvStartTime, tvEndTime, tvDate;
    Button btnAdd, btnCancel;
    TaskDBhelper taskDb;
    String startTime = "", endTime = "", subject = "";
    int startHour, startMinute, endHour, endMinute;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "SubjectPrefs";
    private static final String KEY_CUSTOM_SUBJECTS = "custom_subjects";
    private List<String> subjectList;
    private ArrayAdapter<String> subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        spinnerSubject = findViewById(R.id.spinnerSubject);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvDate = findViewById(R.id.tvDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        taskDb = new TaskDBhelper(this);

        // Load subjects (default + custom)
        loadSubjects();
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        // Handle spinner selection
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = subjectList.get(position);
                if (selected.equals("+ Add Custom Subject")) {
                    showAddSubjectDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText("Date: " + currentDate);

        // Time pickers
        tvStartTime.setOnClickListener(v -> pickTime(true));
        tvEndTime.setOnClickListener(v -> pickTime(false));

        btnCancel.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            subject = spinnerSubject.getSelectedItem().toString();
            
            // Don't allow adding task with "Add Custom Subject" option
            if (subject.equals("+ Add Custom Subject")) {
                Toast.makeText(this, "Please select a subject!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please select both start and end time!", Toast.LENGTH_SHORT).show();
                return;
            }

            int durationMinutes = calculateDuration();
            // Create a StudyTask and add it to the database
            StudyTask studyTask = new StudyTask();
            studyTask.setSubject(subject);
            studyTask.setDate(currentDate);
            studyTask.setStartTime(startTime);
            studyTask.setEndTime(endTime);
            studyTask.setTargetDuration(durationMinutes + " min");
            studyTask.setStatus("Pending");
            
            long id = taskDb.insertStudyTask(studyTask);
            
            // Request notification permission if needed (Android 13+)
            if (checkNotificationPermission()) {
                scheduleReminder(currentDate, startHour, startMinute, subject);
            }
            
            Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void pickTime(boolean isStart) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            if (isStart) {
                startHour = hourOfDay;
                startMinute = minute1;
                startTime = time;
                tvStartTime.setText("Start: " + time);
            } else {
                endHour = hourOfDay;
                endMinute = minute1;
                endTime = time;
                tvEndTime.setText("End: " + time);
            }
        }, hour, minute, true);
        timePicker.show();
    }

    private int calculateDuration() {
        int start = startHour * 60 + startMinute;
        int end = endHour * 60 + endMinute;
        return Math.max(0, end - start);
    }

    /**
     * Load subjects from default list and custom subjects from SharedPreferences
     */
    private void loadSubjects() {
        // Default subjects
        String[] defaultSubjects = {"Mathematics", "Physics", "Chemistry", "Biology", "Computer Science", "English"};
        subjectList = new ArrayList<>(Arrays.asList(defaultSubjects));

        // Load custom subjects from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> customSubjects = prefs.getStringSet(KEY_CUSTOM_SUBJECTS, new HashSet<String>());
        if (customSubjects != null && !customSubjects.isEmpty()) {
            // Add custom subjects (avoid duplicates)
            for (String customSubject : customSubjects) {
                if (!subjectList.contains(customSubject) && !customSubject.trim().isEmpty()) {
                    subjectList.add(customSubject);
                }
            }
        }

        // Sort the list (default first, then custom alphabetically)
        List<String> sortedList = new ArrayList<>();
        List<String> defaultList = new ArrayList<>(Arrays.asList(defaultSubjects));
        List<String> customList = new ArrayList<>();
        if (customSubjects != null && !customSubjects.isEmpty()) {
            customList.addAll(customSubjects);
            customList.removeAll(defaultList);
            java.util.Collections.sort(customList);
        }

        sortedList.addAll(defaultList);
        sortedList.addAll(customList);
        
        // Add the "Add Custom Subject" option at the end
        sortedList.add("+ Add Custom Subject");

        subjectList = sortedList;
    }

    /**
     * Show dialog to add a new custom subject
     */
    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Subject");

        // Create EditText for input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter subject name");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newSubject = input.getText().toString().trim();
            if (newSubject.isEmpty()) {
                Toast.makeText(this, "Subject name cannot be empty!", Toast.LENGTH_SHORT).show();
                // Reset spinner to first item
                spinnerSubject.setSelection(0);
                return;
            }

            // Check if subject already exists
            if (subjectList.contains(newSubject)) {
                Toast.makeText(this, "Subject already exists!", Toast.LENGTH_SHORT).show();
                // Select the existing subject
                spinnerSubject.setSelection(subjectList.indexOf(newSubject));
                return;
            }

            // Save custom subject to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> customSubjects = new HashSet<>(prefs.getStringSet(KEY_CUSTOM_SUBJECTS, new HashSet<String>()));
            customSubjects.add(newSubject);
            prefs.edit().putStringSet(KEY_CUSTOM_SUBJECTS, customSubjects).apply();

            // Reload subjects and update adapter
            loadSubjects();
            subjectAdapter.clear();
            subjectAdapter.addAll(subjectList);
            subjectAdapter.notifyDataSetChanged();

            // Select the newly added subject
            int newPosition = subjectList.indexOf(newSubject);
            spinnerSubject.setSelection(newPosition);

            Toast.makeText(this, "Subject '" + newSubject + "' added successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            // Reset spinner to first item
            spinnerSubject.setSelection(0);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Check if notification permission is granted (Android 13+)
     * Returns true if permission is granted or not needed (Android < 13)
     */
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
                return false; // Permission not granted yet
            }
        }
        return true; // Permission granted or not needed
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders won't work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Schedule a reminder notification for the task
     */
    private void scheduleReminder(String dateYmd, int hour, int minute, String subject) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String[] parts = dateYmd.split("-");
            if (parts.length != 3) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]) - 1; // zero-based
            int d = Integer.parseInt(parts[2]);
            cal.set(java.util.Calendar.YEAR, y);
            cal.set(java.util.Calendar.MONTH, m);
            cal.set(java.util.Calendar.DAY_OF_MONTH, d);
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
            cal.set(java.util.Calendar.MINUTE, minute);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);

            long triggerAt = cal.getTimeInMillis();
            long currentTime = System.currentTimeMillis();

            if (triggerAt < currentTime) {
                Toast.makeText(this, "Cannot schedule reminder for past time", Toast.LENGTH_SHORT).show();
                return;
            }

            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (am == null) {
                Toast.makeText(this, "AlarmManager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if exact alarm permission is granted (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!am.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Please enable exact alarm permission in settings", Toast.LENGTH_LONG).show();
                    // Could open settings here: startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                }
            }

            android.content.Intent intent = new android.content.Intent(this, ReminderReceiver.class);
            intent.putExtra("title", "Study Reminder");
            intent.putExtra("message", subject + " starts now");

            int requestCode = (int) (triggerAt % Integer.MAX_VALUE);
            android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            // Use setExact if exact alarms are not allowed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } else {
                    am.set(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else {
                am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }

            // Show confirmation
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Toast.makeText(this, "Reminder set for " + subject + " at " + timeStr, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error scheduling reminder: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
