package com.lostandfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.lostandfound.R;
import com.lostandfound.databinding.ActivitySignupBinding;
import com.lostandfound.models.User;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;
import com.lostandfound.utils.ValidationUtils;

/**
 * Sign-up screen. Creates a Firebase Auth user, then writes a User document to Firestore.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseHelper firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = FirebaseHelper.getInstance();

        binding.btnSignUp.setOnClickListener(v -> attemptSignUp());
        binding.tvGoToLogin.setOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void attemptSignUp() {
        String name     = binding.etName.getText().toString().trim();
        String email    = binding.etEmail.getText().toString().trim();
        String phone    = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirm  = binding.etConfirmPassword.getText().toString();

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean valid = true;
        if (!ValidationUtils.isValidName(name)) {
            binding.tilName.setError(getString(R.string.error_empty_field));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            binding.tilPhone.setError("Enter a valid 10-digit phone number");
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_weak_password));
            valid = false;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);

        firebase.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser.getUid(), name, email, phone);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("SignUpActivity", "Firebase user creation failed", e);
                    if (e.getMessage() != null && e.getMessage().contains("email")) {
                        binding.tilEmail.setError("This email is already registered");
                    } else {
                        Snackbar.make(binding.getRoot(),
                                "Account creation failed: " + e.getLocalizedMessage(),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone) {
        User newUser = new User(uid, name, email, phone);

        firebase.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(newUser)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("SignUpActivity", "Firestore user save failed", e);
                    
                    // If Firestore fails, we should delete the Auth user to allow them to retry
                    if (firebase.getAuth().getCurrentUser() != null) {
                        firebase.getAuth().getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                Snackbar.make(binding.getRoot(),
                                    "Profile setup failed. Please try again: " + e.getLocalizedMessage(),
                                    Snackbar.LENGTH_LONG).show();
                            });
                    } else {
                        Snackbar.make(binding.getRoot(),
                            "Account created but profile save failed: " + e.getLocalizedMessage(),
                            Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnSignUp.setEnabled(!loading);
        binding.btnSignUp.setText(loading
                ? getString(R.string.creating_account)
                : getString(R.string.sign_up));
    }
}
