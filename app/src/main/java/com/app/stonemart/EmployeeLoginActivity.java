package com.app.stonemart;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.app.stonemart.widget.CyberPillButton;
import com.app.stonemart.widget.PremiumAlertDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmployeeLoginActivity extends AppCompatActivity {
    private UserLocalStore userStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        ScrollView scrollViewLogin = findViewById(R.id.scrollViewLogin);
        View cardLoginContainer = findViewById(R.id.cardLoginContainer);

        // Apply System Bars & Soft Keyboard (IME) Padding dynamically
        View root = findViewById(R.id.rootLogin);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
                v.setPadding(0, systemBars.top, 0, bottomPadding);

                // When keyboard becomes visible, scroll card above keyboard
                if (ime.bottom > 0 && scrollViewLogin != null && cardLoginContainer != null) {
                    scrollViewLogin.postDelayed(() -> {
                        scrollViewLogin.smoothScrollTo(0, cardLoginContainer.getTop());
                    }, 150);
                }
                return insets;
            });
        }

        userStore = new UserLocalStore(this);
        EditText etEmpId = findViewById(R.id.etEmpId);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        CyberPillButton btnLogin = findViewById(R.id.btnLogin);

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                PremiumAlertDialog.showInfo(EmployeeLoginActivity.this,
                        "Password Recovery",
                        "To reset or recover your login password, please contact the Stone Mart IT Support Helpdesk or your department supervisor.");
            });
        }

        // Eye Icon Password Visibility Toggle
        final boolean[] isPasswordVisible = { false };
        if (ivTogglePassword != null) {
            ivTogglePassword.setAlpha(0.6f);
            ivTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible[0]) {
                    // Hide Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivTogglePassword.setAlpha(0.6f);
                    isPasswordVisible[0] = false;
                } else {
                    // Show Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivTogglePassword.setAlpha(1.0f);
                    isPasswordVisible[0] = true;
                }
                // Keep cursor at the end of password text
                etPassword.setSelection(etPassword.getText().length());
            });
        }

        // Smoothly scroll the card up when user focuses on Username or Password input field
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewLogin != null && cardLoginContainer != null) {
                scrollViewLogin.postDelayed(() -> {
                    scrollViewLogin.smoothScrollTo(0, cardLoginContainer.getTop());
                }, 200);
            }
        };

        etEmpId.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);

        // Leave input fields empty for dynamic employee login
        etEmpId.setText("");
        etPassword.setText("");

        btnLogin.setOnClickListener(v -> {
            String empId = etEmpId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (empId.isEmpty() || password.isEmpty()) {
                showErrorAlert("Input Required", "Please enter both Username and Password to log in.");
                return;
            }

            // Set button loading state dynamically
            btnLogin.setEnabled(false);
            btnLogin.setText("AUTHENTICATING...");

            Map<String, String> params = new HashMap<>();
            params.put("id", empId);
            params.put("pwd", password);

            ApiClient.postRequest(ApiConfig.LOGIN_URL, params, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            JSONObject userObj = response.optJSONObject("user");
                            int userId = userObj != null ? userObj.optInt("id", 0) : 0;
                            String username = userObj != null ? userObj.optString("username", empId) : empId;
                            int staffId = userObj != null ? userObj.optInt("staffid", 0) : 0;
                            String userRole = userObj != null ? userObj.optString("userrole", "employee") : "employee";
                            String email = userObj != null ? userObj.optString("email", "") : "";

                            String formattedName = username.length() > 0
                                    ? username.substring(0, 1).toUpperCase() + username.substring(1)
                                    : empId;
                            String storeName = "Plot 12 • Heavy Sawn Slab Yard (Sector 4B)";

                            userStore.storeUserSession(String.valueOf(userId > 0 ? userId : empId), formattedName,
                                    storeName, 26.9124, 75.7873, 200.0f, staffId, userRole, email);

                            Toast.makeText(EmployeeLoginActivity.this, "Welcome, " + formattedName + "!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EmployeeLoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            resetLoginButton(btnLogin);
                            String msg = response.optString("message", "Invalid Username or Password. Please verify your credentials and try again.");
                            showErrorAlert("Authentication Failed", msg);
                        }
                    } catch (Exception e) {
                        resetLoginButton(btnLogin);
                        showErrorAlert("Response Error", "Unable to parse server response. Please try again.");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    resetLoginButton(btnLogin);
                    showErrorAlert("Connection Error", "Unable to connect to Stone Mart server (" + errorMessage + "). Please check your network connection and try again.");
                }
            });
        });
    }

    private void resetLoginButton(CyberPillButton btnLogin) {
        if (btnLogin != null) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Log In");
        }
    }

    private void showErrorAlert(String title, String message) {
        PremiumAlertDialog.showError(this, title, message);
    }
}