package com.memorease.laclair.android.myapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.memorease.laclair.android.myapplication.data.CardContract.*;

/**
 * This class is a DbHelper class to support the creation
 * and upkeep of the cards database
 */
public class CardsDbHelper extends SQLiteOpenHelper {

    //Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Cards.db";

    //Create entries for tables in the database
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CardEntry.TABLE_NAME + " (" +
                    CardEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CardEntry.TOPIC + " TEXT NOT NULL," +
                    CardEntry.QUESTION + " TEXT NOT NULL," +
                    CardEntry.ANSWER + " TEXT NOT NULL," +
                    CardEntry.STUDY_DATE + " TEXT NOT NULL," +
                    CardEntry.CORRECT_ANSWERED + " INTEGER DEFAULT 0," +
                    CardEntry.STUDY_TODAY + " INTEGER DEFAULT 0);";


    //Delete entries for each table in the database
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CardEntry.TABLE_NAME;


    //Constructor
    public CardsDbHelper(Context context) {

        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES_2);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
