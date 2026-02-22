package com.example.weighttrackingapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SmsSettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "sms_prefs";
    public static final String KEY_SMS_ALLOWED = "sms_allowed";
    public static final String KEY_SMS_PHONE = "sms_phone";

    private EditText editPhone;
    private TextView textStatus;
    private Button btnAllow, btnDeny;

    private SharedPreferences prefs;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    saveAllowed(true);
                    updateStatus();
                    Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();

                } else {
                    saveAllowed(false);
                    updateStatus();
                    Toast.makeText(this, "SMS permission denied. App still works.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editPhone = findViewById(R.id.editTextPhone);
        textStatus = findViewById(R.id.textSmsStatus);
        btnAllow = findViewById(R.id.buttonAllowSms);
        btnDeny = findViewById(R.id.buttonDenySms);

        // Load saved phone (if any)
        editPhone.setText(prefs.getString(KEY_SMS_PHONE, ""));

        btnAllow.setOnClickListener(v -> {
            String phone = editPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter a phone number first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save phone right away
            prefs.edit().putString(KEY_SMS_PHONE, phone).apply();

            // Request permission if needed
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                saveAllowed(true);
                updateStatus();
                Toast.makeText(this, "SMS already allowed.", Toast.LENGTH_SHORT).show();
            } else {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        });

        btnDeny.setOnClickListener(v -> {
            saveAllowed(false);
            updateStatus();
            Toast.makeText(this, "SMS notifications disabled. App still works.", Toast.LENGTH_SHORT).show();
        });

        updateStatus();
    }

    private void saveAllowed(boolean allowed) {
        prefs.edit().putBoolean(KEY_SMS_ALLOWED, allowed).apply();
    }

    private void updateStatus() {
        boolean allowed = prefs.getBoolean(KEY_SMS_ALLOWED, false);
        textStatus.setText("Current status: " + (allowed ? "Allowed" : "Denied"));
    }

    @SuppressWarnings("unused")
    private void sendTestSms() {
        boolean allowed = prefs.getBoolean(KEY_SMS_ALLOWED, false);
        String phone = prefs.getString(KEY_SMS_PHONE, "");
        if (!allowed || phone.isEmpty()) return;

        try {
            SmsManager.getDefault().sendTextMessage(
                    phone, null,
                    "WeightTracker test: SMS enabled ✅",
                    null, null
            );
            Toast.makeText(this, "Test SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}