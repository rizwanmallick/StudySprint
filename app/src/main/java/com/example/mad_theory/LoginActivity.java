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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvNoAccount = findViewById(R.id.tvNoAccount);

        UserPrefs userPrefs = new UserPrefs(this);
        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String name = firebaseUser != null && firebaseUser.getDisplayName() != null
                                ? firebaseUser.getDisplayName()
                                : "";
                        userPrefs.saveUser(firebaseUser != null ? firebaseUser.getUid() : "", name, email);

                        Toast.makeText(this, "Welcome back, " + (name.isEmpty() ? "!" : name + "!"), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
        });

        tvNoAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}


