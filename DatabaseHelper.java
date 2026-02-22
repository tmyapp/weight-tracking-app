package com.example.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weight_tracker.db";
    private static final int DB_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "Users";
    public static final String COL_USER_ID = "userId";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // DailyWeight table
    public static final String TABLE_DAILY_WEIGHT = "DailyWeight";
    public static final String COL_WEIGHT_ID = "weightId";
    public static final String COL_USER_FK = "userFk";
    public static final String COL_DATE = "date";
    public static final String COL_WEIGHT = "weight";
    public static final String COL_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                        COL_PASSWORD + " TEXT NOT NULL" +
                        ");";

        String createDailyWeight =
                "CREATE TABLE " + TABLE_DAILY_WEIGHT + " (" +
                        COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USER_FK + " INTEGER NOT NULL, " +
                        COL_DATE + " TEXT NOT NULL, " +
                        COL_WEIGHT + " TEXT NOT NULL, " +
                        COL_NOTES + " TEXT, " +
                        "FOREIGN KEY(" + COL_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                        ");";

        db.execSQL(createUsers);
        db.execSQL(createDailyWeight);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple approach for class project: drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ---------- USER METHODS ----------

    public boolean createUser(String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int getUserIdIfValid(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public boolean userExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // ---------- WEIGHT METHODS (CRUD) ----------

    public long addWeightEntry(int userId, String date, String weight, String notes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_FK, userId);
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);
        values.put(COL_NOTES, notes);

        return db.insert(TABLE_DAILY_WEIGHT, null, values);
    }

    public Cursor getAllWeightsForUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_DAILY_WEIGHT,
                new String[]{COL_WEIGHT_ID, COL_DATE, COL_WEIGHT, COL_NOTES},
                COL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                COL_DATE + " DESC"
        );
    }

    public boolean deleteWeightEntry(int weightId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_DAILY_WEIGHT, COL_WEIGHT_ID + "=?", new String[]{String.valueOf(weightId)});
        return rows > 0;
    }

    public boolean updateWeightEntry(int weightId, String date, String weight, String notes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);
        values.put(COL_NOTES, notes);

        int rows = db.update(TABLE_DAILY_WEIGHT, values, COL_WEIGHT_ID + "=?", new String[]{String.valueOf(weightId)});
        return rows > 0;
    }
}