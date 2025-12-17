package com.example.mad_theory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class PomodoroSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PomodoroSettings";
    private static final String KEY_FOCUS_DURATION = "focus_duration";
    private static final String KEY_SHORT_BREAK_DURATION = "short_break_duration";
    private static final String KEY_LONG_BREAK_DURATION = "long_break_duration";

    private TextView tvFocusSessionDuration;
    private TextView tvShortBreakDuration;
    private TextView tvLongBreakDuration;

    private CardView cardFocusSession;
    private CardView cardShortBreak;
    private CardView cardLongBreak;

    private SharedPreferences settings;

    // Enum to identify which setting is being edited
    private enum SettingType {
        FOCUS_SESSION,
        SHORT_BREAK,
        LONG_BREAK
    }
    private SettingType currentSettingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pomodora_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Pomodoro Settings");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        tvFocusSessionDuration = findViewById(R.id.tvFocusSessionDuration);
        tvShortBreakDuration = findViewById(R.id.tvShortBreakDuration);
        tvLongBreakDuration = findViewById(R.id.tvLongBreakDuration);

        cardFocusSession = findViewById(R.id.cardFocusSession);
        cardShortBreak = findViewById(R.id.cardShortBreak);
        cardLongBreak = findViewById(R.id.cardLongBreak);
    }

    private void loadSettings() {
        int focusDuration = settings.getInt(KEY_FOCUS_DURATION, 25); // Default 5 minutes
        int shortBreakDuration = settings.getInt(KEY_SHORT_BREAK_DURATION, 5); // Default 5 minutes
        int longBreakDuration = settings.getInt(KEY_LONG_BREAK_DURATION, 15); // Default 15 minutes

        tvFocusSessionDuration.setText(formatDuration(focusDuration));
        tvShortBreakDuration.setText(formatDuration(shortBreakDuration));
        tvLongBreakDuration.setText(formatDuration(longBreakDuration));
    }

    private void setupClickListeners() {
        cardFocusSession.setOnClickListener(v -> {
            currentSettingType = SettingType.FOCUS_SESSION;
            showDurationPicker(settings.getInt(KEY_FOCUS_DURATION, 25));
        });

        cardShortBreak.setOnClickListener(v -> {
            currentSettingType = SettingType.SHORT_BREAK;
            showDurationPicker(settings.getInt(KEY_SHORT_BREAK_DURATION, 5));
        });

        cardLongBreak.setOnClickListener(v -> {
            currentSettingType = SettingType.LONG_BREAK;
            showDurationPicker(settings.getInt(KEY_LONG_BREAK_DURATION, 15));
        });
    }

    private void showDurationPicker(int currentDuration) {
        // For now, we'll use a simple approach with predefined options
        // You can enhance this with a proper bottom sheet dialog later
        showSimpleDurationDialog(currentDuration);
    }

    private void showSimpleDurationDialog(int currentDuration) {
        String[] durations;
        int[] durationValues;

        switch (currentSettingType) {
            case SHORT_BREAK:
                // Restrict short break options to max 30 minutes
                durations = new String[]{"5 min", "10 min", "15 min", "20 min", "25 min", "30 min"};
                durationValues = new int[]{5, 10, 15, 20, 25, 30};
                break;
            case FOCUS_SESSION:
                // Common focus options (includes longer sessions)
                durations = new String[]{"25 min", "30 min", "45 min", "60 min", "75 min", "90 min","105 min","120 min"};
                durationValues = new int[]{25, 30, 45, 60, 75, 90, 105, 120};
                break;
            case LONG_BREAK:
            default:
                durations = new String[]{"10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "60 min"};
                durationValues = new int[]{10, 15, 20, 25, 30, 45, 60};
                break;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Duration");

        int currentIndex = 0;
        for (int i = 0; i < durationValues.length; i++) {
            if (durationValues[i] == currentDuration) {
                currentIndex = i;
                break;
            }
        }

        final int[] finalDurationValues = durationValues;
        builder.setSingleChoiceItems(durations, currentIndex, (dialog, which) -> {
            int selectedDuration = finalDurationValues[which];
            updateSetting(selectedDuration);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateSetting(int selectedDurationMinutes) {
        SharedPreferences.Editor editor = settings.edit();
        String keyToUpdate = "";
        TextView textViewToUpdate = null;

        switch (currentSettingType) {
            case FOCUS_SESSION:
                keyToUpdate = KEY_FOCUS_DURATION;
                textViewToUpdate = tvFocusSessionDuration;
                break;
            case SHORT_BREAK:
                keyToUpdate = KEY_SHORT_BREAK_DURATION;
                textViewToUpdate = tvShortBreakDuration;
                break;
            case LONG_BREAK:
                keyToUpdate = KEY_LONG_BREAK_DURATION;
                textViewToUpdate = tvLongBreakDuration;
                break;
        }

        if (!keyToUpdate.isEmpty() && textViewToUpdate != null) {
            editor.putInt(keyToUpdate, selectedDurationMinutes);
            editor.apply();
            textViewToUpdate.setText(formatDuration(selectedDurationMinutes));
            
            String settingName = currentSettingType.name().replace("_", " ").toLowerCase();
            Toast.makeText(this, settingName + " set to " + formatDuration(selectedDurationMinutes), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDuration(int totalMinutes) {
        if (totalMinutes <= 60) {
            return totalMinutes + " min";
        } else {
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "m";
            }
        }
    }
}
