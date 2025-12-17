package com.example.mad_theory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnCreate = findViewById(R.id.btnCreateAccount);
        TextView tvHaveAccount = findViewById(R.id.tvHaveAccount);

        UserPrefs userPrefs = new UserPrefs(this);
        auth = FirebaseAuth.getInstance();

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates);
                            userPrefs.saveUser(firebaseUser.getUid(), name, email);
                        }
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    });
        });

        tvHaveAccount.setOnClickListener(v -> {
            finish();
        });
    }
}


