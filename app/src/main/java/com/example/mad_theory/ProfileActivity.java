package com.example.mad_theory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private LinearLayout llEditProfile;
    private LinearLayout llProfileSettings;
    private LinearLayout llLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvInitials = findViewById(R.id.tvInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        llEditProfile = findViewById(R.id.llEditProfile);
        llProfileSettings = findViewById(R.id.llProfileSettings);
        llLogout = findViewById(R.id.llLogout);

        populateUserInfo();

        llEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        llProfileSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        llLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateUserInfo();
    }

    private void populateUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, AuthStartActivity.class));
            finish();
            return;
        }
        UserPrefs prefs = new UserPrefs(this);
        String name = user.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            String stored = prefs.getName();
            if (stored != null && !stored.trim().isEmpty()) {
                name = stored;
            }
        }
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = prefs.getEmail();
        }

        if (name == null || name.trim().isEmpty()) {
            name = "Student";
        }
        tvProfileName.setText(name);
        tvProfileEmail.setText(email != null ? email : "");
        tvInitials.setText(getInitials(name));
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && i < 2; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "U";
    }

    private void showLogoutDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null, false);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelLogout);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmLogout);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_MaterialComponents_Dialog)
                .setView(view)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            new UserPrefs(this).logout();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthStartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}


