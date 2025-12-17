package com.example.mad_theory;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvEditInitials;
    private TextInputEditText etFullName;
    private TextInputEditText etPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tvEditInitials = findViewById(R.id.tvEditInitials);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        MaterialButton btnSave = findViewById(R.id.btnSaveChanges);

        loadCurrentData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadCurrentData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserPrefs prefs = new UserPrefs(this);

        String name = user != null ? user.getDisplayName() : null;
        if (name == null || name.trim().isEmpty()) {
            name = prefs.getName();
        }
        if (name != null) {
            etFullName.setText(name);
            tvEditInitials.setText(getInitials(name));
        }

        // Simple phone storage in prefs (not Firebase)
        String phone = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                .getString("phone", "");
        if (!TextUtils.isEmpty(phone)) {
            etPhone.setText(phone);
        }
    }

    private void saveChanges() {
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(request).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to prefs as well
            new UserPrefs(this).saveUser(user.getUid(), name, user.getEmail());
            getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                    .edit()
                    .putString("phone", phone)
                    .apply();

            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
        });
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
}


