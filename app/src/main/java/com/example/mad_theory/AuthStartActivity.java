package com.example.mad_theory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthStartActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) {
                    Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogle(account.getIdToken());
                    }
                } catch (ApiException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Google sign-in failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_start);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        MaterialButton btnSignInEmail = findViewById(R.id.btnSignInEmail);
        MaterialButton btnSignUpEmail = findViewById(R.id.btnSignUpEmail);

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        btnSignInEmail.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnSignUpEmail.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        findViewById(R.id.tvPrivacyLink).setOnClickListener(v -> openPrivacyPolicy());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            goToDashboard();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Snackbar.make(findViewById(android.R.id.content), "Missing Google token", Snackbar.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        goToDashboard();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Google auth failed", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void openPrivacyPolicy() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Privacy policy link unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}

