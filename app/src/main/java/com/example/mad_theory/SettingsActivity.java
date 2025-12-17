package com.example.mad_theory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.appcheck.interop.BuildConfig;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    private RadioButton rbLightTheme;
    private RadioButton rbDarkTheme;
    private LinearLayout llLightTheme;
    private LinearLayout llDarkTheme;
    private LinearLayout llNotifications;
    private LinearLayout llAbout;
    private LinearLayout llShare;
    private LinearLayout llFeedback;
    private LinearLayout llRate;
    private LinearLayout llPrivacyPolicy;
    private LinearLayout llDeleteAccount;
    
    private SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        initializeData();
        setupClickListeners();
        updateThemeSelection();
    }

    private void initializeViews() {
        rbLightTheme = findViewById(R.id.rbLightTheme);
        rbDarkTheme = findViewById(R.id.rbDarkTheme);
        llLightTheme = findViewById(R.id.llLightTheme);
        llDarkTheme = findViewById(R.id.llDarkTheme);
        llNotifications = findViewById(R.id.llNotifications);
        llAbout = findViewById(R.id.llAbout);
        llShare = findViewById(R.id.llShare);
        llFeedback = findViewById(R.id.llFeedback);
        llRate = findViewById(R.id.llRate);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        llDeleteAccount = findViewById(R.id.llDeleteAccount);
    }

    private void initializeData() {
        themePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupClickListeners() {
        llLightTheme.setOnClickListener(v -> {
            rbLightTheme.setChecked(true);
            rbDarkTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
        });

        llDarkTheme.setOnClickListener(v -> {
            rbDarkTheme.setChecked(true);
            rbLightTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
        });

        // Also handle direct RadioButton clicks to ensure exclusivity
        rbLightTheme.setOnClickListener(v -> {
            rbLightTheme.setChecked(true);
            rbDarkTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
        });

        rbDarkTheme.setOnClickListener(v -> {
            rbDarkTheme.setChecked(true);
            rbLightTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
        });

        llNotifications.setOnClickListener(v -> {
            // Open reminder / notifications screen
            startActivity(new Intent(this, ReminderActivity.class));
        });

        llAbout.setOnClickListener(v -> showAboutDialog());

        llShare.setOnClickListener(v -> shareApp());

        llFeedback.setOnClickListener(v -> sendFeedback());

        llRate.setOnClickListener(v -> openPlayStore());

        llPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());

        llDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void updateThemeSelection() {
        int currentThemeMode = themePrefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        
        if (currentThemeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            rbDarkTheme.setChecked(true);
            rbLightTheme.setChecked(false);
        } else {
            rbLightTheme.setChecked(true);
            rbDarkTheme.setChecked(false);
        }
    }

    private void applyTheme(int themeMode) {
        // Save theme preference
        SharedPreferences.Editor editor = themePrefs.edit();
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
        // Show feedback
        String themeName = themeMode == AppCompatDelegate.MODE_NIGHT_YES ? "Dark" : "Light";
        Toast.makeText(this, themeName + " theme applied", Toast.LENGTH_SHORT).show();
        
        // Restart activity to apply theme changes
        recreate();
    }

    public static void applySavedTheme(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedThemeMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);
    }

    private void showAboutDialog() {
        String versionName = BuildConfig.VERSION_NAME;
        new AlertDialog.Builder(this)
                .setTitle("About " + getString(R.string.app_name))
                .setMessage("Version " + versionName + "\n\nA simple study planner and tracker.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void shareApp() {
        String packageName = getPackageName();
        String shareText = "Check out " + getString(R.string.app_name) +
                " to plan and track your study sessions:\nhttps://play.google.com/store/apps/details?id=" + packageName;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share app via"));
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - Feedback");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlayStore() {
        String packageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    private void openPrivacyPolicy() {
        String url = getString(R.string.privacy_policy_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Privacy policy link unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently remove your account. This action cannot be undone. Continue?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .show();
    }

    private void deleteAccount() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No account signed in", Toast.LENGTH_SHORT).show();
            return;
        }
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                auth.signOut();
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Delete failed. Please sign in again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
