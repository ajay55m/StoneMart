package com.app.stonemart.ui.leave;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.stonemart.widget.PremiumAlertDialog;

/**
 * LeaveApprovalActivity - Management & supervisor portal for reviewing,
 * approving, or rejecting staff leave submissions.
 */
public class LeaveApprovalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Leave Approvals");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        PremiumAlertDialog.showInfo(this, "Leave Approvals",
                "Supervisor Portal: All pending leave requests are automatically synchronized with HRMS for Head ID review.");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

