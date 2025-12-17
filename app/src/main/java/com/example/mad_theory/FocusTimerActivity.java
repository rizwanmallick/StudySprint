package com.example.mad_theory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FocusTimerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PomodoroSettings";
    private static final String KEY_FOCUS_DURATION = "focus_duration";
    private static final String KEY_SHORT_BREAK_DURATION = "short_break_duration";
    private static final String KEY_LONG_BREAK_DURATION = "long_break_duration";

    private TextView tvTimerDisplay;
    private TextView tvTimerStatus;
    private TextView tvSessionCount;
    private ProgressBar progressBarTimer;
    private FloatingActionButton fabStartPause;
    private FloatingActionButton fabStop;
    private Button btnSettings;

    private CountDownTimer countDownTimer;
    private SharedPreferences settings;
    
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long timeLeftInMillis = 0;
    private long totalTimeInMillis = 0;
    
    private int currentSession = 1;
    private int totalSessions = 4;
    private TimerState currentState = TimerState.FOCUS;

    private enum TimerState {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focustimer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Focus Timer");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initializeViews();
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSettings();
        updateDisplay();
        setupClickListeners();
    }

    private void initializeViews() {
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        tvTimerStatus = findViewById(R.id.tvTimerStatus);
        tvSessionCount = findViewById(R.id.tvSessionCount);
        progressBarTimer = findViewById(R.id.progressBarTimer);
        fabStartPause = findViewById(R.id.fabStartPause);
        fabStop = findViewById(R.id.fabStop);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void loadSettings() {
        int focusDuration = settings.getInt(KEY_FOCUS_DURATION, 5); // Default 5 minutes
        int shortBreakDuration = settings.getInt(KEY_SHORT_BREAK_DURATION, 5);
        int longBreakDuration = settings.getInt(KEY_LONG_BREAK_DURATION, 15);

        // Set initial timer based on current state
        if (currentState == TimerState.FOCUS) {
            totalTimeInMillis = focusDuration * 60 * 1000;
        } else if (currentState == TimerState.SHORT_BREAK) {
            totalTimeInMillis = shortBreakDuration * 60 * 1000;
        } else {
            totalTimeInMillis = longBreakDuration * 60 * 1000;
        }
        
        timeLeftInMillis = totalTimeInMillis;
    }

    private void setupClickListeners() {
        fabStartPause.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });

        fabStop.setOnClickListener(v -> stopTimer());

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(FocusTimerActivity.this, PomodoroSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void startTimer() {
        if (!isPaused) {
            loadSettings(); // Reload settings in case they changed
        }
        
        isRunning = true;
        isPaused = false;
        fabStartPause.setImageResource(android.R.drawable.ic_media_pause);

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateDisplay();
            }

            @Override
            public void onFinish() {
                timerFinished();
            }
        }.start();
    }

    private void pauseTimer() {
        isRunning = false;
        isPaused = true;
        fabStartPause.setImageResource(android.R.drawable.ic_media_play);
        countDownTimer.cancel();
    }

    private void stopTimer() {
        isRunning = false;
        isPaused = false;
        fabStartPause.setImageResource(android.R.drawable.ic_media_play);
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        loadSettings();
        updateDisplay();
    }

    private void timerFinished() {
        isRunning = false;
        isPaused = false;
        fabStartPause.setImageResource(android.R.drawable.ic_media_play);

        // Move to next state
        switch (currentState) {
            case FOCUS:
                currentSession++;
                if (currentSession <= totalSessions) {
                    currentState = TimerState.SHORT_BREAK;
                } else {
                    currentState = TimerState.LONG_BREAK;
                    currentSession = 1; // Reset for next cycle
                }
                break;
            case SHORT_BREAK:
            case LONG_BREAK:
                currentState = TimerState.FOCUS;
                break;
        }

        loadSettings();
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimerDisplay.setText(timeFormatted);

        // Update status text
        switch (currentState) {
            case FOCUS:
                tvTimerStatus.setText("Focus Session");
                break;
            case SHORT_BREAK:
                tvTimerStatus.setText("Short Break");
                break;
            case LONG_BREAK:
                tvTimerStatus.setText("Long Break");
                break;
        }

        // Update session count
        tvSessionCount.setText(String.valueOf(currentSession));

        // Update progress bar
        if (totalTimeInMillis > 0) {
            int progress = (int) ((totalTimeInMillis - timeLeftInMillis) * 100 / totalTimeInMillis);
            progressBarTimer.setProgress(progress);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRunning) {
            pauseTimer();
        }
    }
}
