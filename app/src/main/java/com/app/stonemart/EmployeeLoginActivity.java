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
import androidx.lifecycle.ViewModelProvider;

import com.app.stonemart.core.Resource;
import com.app.stonemart.viewmodel.LoginViewModel;
import com.app.stonemart.widget.CyberPillButton;
import com.app.stonemart.widget.PremiumAlertDialog;

/**
 * EmployeeLoginActivity - MVVM View for user authentication.
 * Observes LoginViewModel and updates UI reactively.
 */
public class EmployeeLoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

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

        // Observe ViewModel LiveData State
        loginViewModel.loginState.observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    btnLogin.setEnabled(false);
                    btnLogin.setText("AUTHENTICATING...");
                    break;
                case SUCCESS:
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Log In");
                    String name = resource.getData() != null ? resource.getData() : "Employee";
                    Toast.makeText(EmployeeLoginActivity.this, "Welcome, " + name + "!",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EmployeeLoginActivity.this, DashboardActivity.class));
                    finish();
                    break;
                case ERROR:
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Log In");
                    showErrorAlert("Authentication Failed", resource.getMessage() != null ? resource.getMessage() : "Unable to log in. Please check credentials.");
                    break;
            }
        });

        btnLogin.setOnClickListener(v -> {
            String empId = etEmpId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (empId.isEmpty() || password.isEmpty()) {
                showErrorAlert("Input Required", "Please enter both Username and Password to log in.");
                return;
            }

            loginViewModel.login(empId, password);
        });
    }

    private void showErrorAlert(String title, String message) {
        PremiumAlertDialog.showError(this, title, message);
    }
}
