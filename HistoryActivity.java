package com.example.weighttrackingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private TableLayout tableHistory;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        userId = getIntent().getIntExtra("USER_ID", -1);

        tableHistory = findViewById(R.id.tableHistory);
        Button addBtn = findViewById(R.id.buttonAddWeight);
        Button smsBtn = findViewById(R.id.buttonSmsSettings);

        dbHelper = new DatabaseHelper(this);

        addBtn.setOnClickListener(v -> showAddDialog());

        smsBtn.setOnClickListener(v -> {
            Intent i = new Intent(HistoryActivity.this, SmsSettingsActivity.class);
            startActivity(i);
        });

        loadWeights();
    }

    private void loadWeights() {
        // Keep header row (index 0), clear everything else
        int rows = tableHistory.getChildCount();
        if (rows > 1) tableHistory.removeViews(1, rows - 1);

        Cursor cursor = dbHelper.getAllWeightsForUser(userId);

        while (cursor.moveToNext()) {
            int weightId = cursor.getInt(0);
            String date = cursor.getString(1);
            String weight = cursor.getString(2);
            String notes = cursor.getString(3);
            if (notes == null) notes = "";

            TableRow row = new TableRow(this);


            TextView tvDate = makeDateCell(date);
            TextView tvWeight = makeSingleLineCell(weight, 1.8f);
            TextView tvNotes = makeMultiLineCell(notes, 2.4f);

            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setAllCaps(false);
            deleteBtn.setTextColor(Color.WHITE);
            deleteBtn.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_500)
            );

            // Button width so it doesn’t squeeze columns
            TableRow.LayoutParams btnParams =
                    new TableRow.LayoutParams(dp(120), TableRow.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(dp(6), dp(6), dp(6), dp(6));
            deleteBtn.setLayoutParams(btnParams);

            deleteBtn.setOnClickListener(v -> {
                dbHelper.deleteWeightEntry(weightId);
                loadWeights();
            });

            row.addView(tvDate);
            row.addView(tvWeight);
            row.addView(tvNotes);
            row.addView(deleteBtn);

            tableHistory.addView(row);
        }

        cursor.close();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Today's Weight");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(10), dp(18), dp(6));

        EditText inputDate = new EditText(this);
        inputDate.setHint("Date (MM/dd/yyyy)");
        inputDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date()));
        layout.addView(inputDate);

        EditText inputWeight = new EditText(this);
        inputWeight.setHint("Weight (ex: 215 lb)");
        inputWeight.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(inputWeight);

        EditText inputNotes = new EditText(this);
        inputNotes.setHint("Notes (optional)");
        layout.addView(inputNotes);

        builder.setView(layout);

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());

        builder.setPositiveButton("Save", (d, w) -> {
            String date = inputDate.getText().toString().trim();
            String weight = inputWeight.getText().toString().trim();
            String notes = inputNotes.getText().toString().trim();

            if (date.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Date and weight are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.addWeightEntry(userId, date, weight, notes);
            loadWeights();
        });

        builder.show();
    }

    // ---------- UI helpers ----------

    private TextView makeSingleLineCell(String text, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(13);

        tv.setSingleLine(true);
        tv.setMaxLines(1);

        TableRow.LayoutParams params =
                new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        params.setMargins(dp(6), dp(6), dp(6), dp(6));
        tv.setLayoutParams(params);

        tv.setPadding(dp(6), dp(10), dp(6), dp(10));
        return tv;
    }

    private TextView makeMultiLineCell(String text, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(13);

        tv.setSingleLine(false);
        tv.setMaxLines(2);

        TableRow.LayoutParams params =
                new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        params.setMargins(dp(6), dp(6), dp(6), dp(6));
        tv.setLayoutParams(params);

        tv.setPadding(dp(6), dp(10), dp(6), dp(10));
        return tv;
    }

    private TextView makeDateCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(13);

        tv.setSingleLine(true);
        tv.setMaxLines(1);

        // Keep date wide enough
        TableRow.LayoutParams params =
                new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2.9f);
        params.setMargins(dp(6), dp(6), dp(6), dp(6));
        tv.setLayoutParams(params);

        tv.setPadding(dp(6), dp(10), dp(6), dp(10));
        return tv;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}