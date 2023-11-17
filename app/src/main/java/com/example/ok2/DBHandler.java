// Source: https://www.geeksforgeeks.org/how-to-create-and-add-data-to-sqlite-database-in-android/
// Somehow this was easier than trying to make a database using Kotlin and the Room library

package com.example.ok2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "GEO_DB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "geo";
    private static final String TABLE_NAME_2 = "cheevos";
    private static final String ID_COL = "id";
    private static final String DIFFICULTY_COL = "difficulty";
    private static final String START_LAT_COL = "start_lat";
    private static final String START_LONG_COL = "start_long";
    private static final String END_LAT_COL = "end_lat";
    private static final String END_LONG_COL = "end_long";
    private static final String RANK_COL = "rank";
    private static final String POINTS_COL = "points";
    private static final String SD_COL = "start_date";
    private static final String ED_COL = "end_date";
    private static final String TT_COL = "time_taken";
    private static final String CHEEVO_COL = "cheevo_name";
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DIFFICULTY_COL + " STRING,"
                + START_LAT_COL + " REAL,"
                + START_LONG_COL + " REAL,"
                + END_LAT_COL + " REAL,"
                + END_LONG_COL + " REAL,"
                + RANK_COL + " STRING,"
                + POINTS_COL + " INTEGER,"
                + SD_COL + " STRING,"
                + ED_COL + " STRING,"
                + TT_COL + " STRING)";
        db.execSQL(query);
        String query2 = "CREATE TABLE " + TABLE_NAME_2 + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CHEEVO_COL + " STRING,"
                + ED_COL + " STRING)";
        db.execSQL(query2);
    }

    public void addEntry(String selectedOption_global, Double lat, Double _long, Double random_lat_global, Double random_long_global, String rank_global, Integer points_global, String startdate_global, String enddate_global, String time_taken) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DIFFICULTY_COL, selectedOption_global);
        values.put(START_LAT_COL, lat);
        values.put(START_LONG_COL, _long);
        values.put(END_LAT_COL, random_lat_global);
        values.put(END_LONG_COL, random_long_global);
        values.put(RANK_COL, rank_global);
        values.put(POINTS_COL, points_global);
        values.put(SD_COL, startdate_global);
        values.put(ED_COL, enddate_global);
        values.put(TT_COL, time_taken);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void addEntry2(String cheevo, String enddate_global) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHEEVO_COL, cheevo);
        values.put(ED_COL, enddate_global);
        db.insert(TABLE_NAME_2, null, values);
        db.close();
    }

    public String getData() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String data = "";

        if (cursor.moveToFirst()) {
            data += "____________________________________\n\n";
            do {
                data += "MISSION " + cursor.getString(0) + "\n\n" +
                        "DIFFICULTY: " + cursor.getString(1) + "\n"
                + "START: " + cursor.getFloat(2) + ", "
                + cursor.getFloat(3) + "\n"
                + "END: " + cursor.getFloat(4) + ", " +
                        cursor.getFloat(5) + "\n" +
                        "RANK: " + cursor.getString(6) + "\n" +
                        "POINTS: " + cursor.getInt(7) + "\n" +
                        "DATE: " + cursor.getString(9) + "\n" +
                        "TIME TAKEN: " + cursor.getString(10)
                        + "\n\n____________________________________\n\n";
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    public String getCheevos_Name() {
        String selectQuery = "SELECT DISTINCT cheevo_name FROM " + TABLE_NAME_2;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME_2, new String[] { CHEEVO_COL, ED_COL, }, null, null, CHEEVO_COL, null, null, null);
        String cheevos_name = "";

        if (cursor.moveToFirst()) {
            do {
                cheevos_name += cursor.getString(0) + "*";
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cheevos_name;
    }

    public String getCheevos_Date() {
        String selectQuery = "SELECT DISTINCT cheevo_name FROM " + TABLE_NAME_2;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME_2, new String[] { CHEEVO_COL, ED_COL, }, null, null, CHEEVO_COL, null, null, null);
        String cheevos_date = "";

        if (cursor.moveToFirst()) {
            do {
                cheevos_date += cursor.getString(1) + "*";
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cheevos_date;
    }



    public Integer getPoints() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Integer points = 0;

        if (cursor.moveToFirst()) {
            do {
                points += cursor.getInt(7);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return points;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
