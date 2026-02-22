package com.example.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        db = new DatabaseHelper(this);

        buttonCreateAccount.setOnClickListener(v -> createAccount());
        buttonLogin.setOnClickListener(v -> login());
    }

    private void createAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.userExists(email)) {
            Toast.makeText(this, "Account already exists. Please log in.", Toast.LENGTH_SHORT).show();
            editTextPassword.setText("");
            return;
        }

        boolean created = db.createUser(email, password);
        if (created) {
            Toast.makeText(this, "Account created! Now log in.", Toast.LENGTH_SHORT).show();
            editTextPassword.setText("");
        } else {
            Toast.makeText(this, "Account creation failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = db.getUserIdIfValid(email, password);
        if (userId == -1) {
            Toast.makeText(this, "Invalid login. Try again or create an account.", Toast.LENGTH_SHORT).show();
            editTextPassword.setText("");
            return;
        }

        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);

        // Optional: clear fields after successful login
        editTextEmail.setText("");
        editTextPassword.setText("");
    }
}