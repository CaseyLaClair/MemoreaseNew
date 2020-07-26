package com.memorease.laclair.android.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Main activity. This opens with the opening of the application.
 */
public class MainActivity extends AppCompatActivity {

    //Declare all vars and dbs
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase dbReader;
    SQLiteDatabase dbWriter;
    Cursor cursor;
    int correctAnswered;
    String startDate;
    long daysStart;
    long daysCurrent;
    long milliStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get current time in millis and a flag.
        boolean flag;
        Calendar current = Calendar.getInstance();
        daysCurrent = (current.getTimeInMillis() / (24 * 60 * 60 * 1000));

        //Get a readable and writable version of the db
        dbReader = cardsDbHelper.getReadableDatabase();
        dbWriter = cardsDbHelper.getWritableDatabase();

        //Prepare query
        String query = "SELECT * FROM " + CardContract.CardEntry.TABLE_NAME + ";";
        cursor = dbReader.rawQuery(query, null);

        //Cycle through cursor and find values and check
        //if they should be studied today.
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(CardContract.CardEntry._ID));
            startDate = cursor.getString(cursor.getColumnIndex(CardContract.CardEntry.STUDY_DATE));
            correctAnswered = cursor.getInt(cursor.getColumnIndex("correctanswered"));

            //Create format used in db dates
            SimpleDateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date myDate = currentFormat.parse(startDate);
                milliStart = myDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Find days since the card was created.
            daysStart = (milliStart / (24 * 60 * 60 * 1000));

            //Days that have elapsed since it's creation or last time it was studied.
            int days = (int) (daysCurrent - daysStart);
            flag = studyCheck(days, correctAnswered);

            //Update to be studied today if it meets studyCheck criteria.
            if (flag == true) {
                String dateCreated = currentFormat.format(current.getTime());
                String updateDate = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.STUDY_DATE +
                        " = '" + dateCreated + "' WHERE " + CardContract.CardEntry._ID + " = " + id;
                String updateStudy = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.STUDY_TODAY +
                        " = 1 WHERE " + CardContract.CardEntry._ID + " = " + id;

                dbWriter.execSQL(updateDate);
                dbWriter.execSQL(updateStudy);
            }
        //Do the same as above for the next card if there is another.
        } else if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CardContract.CardEntry._ID));
            startDate = cursor.getString(cursor.getColumnIndex("studydate"));
            correctAnswered = cursor.getInt(cursor.getColumnIndex("correctanswered"));

            //Get the simple date format for the current date
            SimpleDateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date myDate = currentFormat.parse(startDate);
                milliStart = myDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            daysStart = (milliStart / (24 * 60 * 60 * 1000));

            //Calculate number of days since start
            int days = (int) (daysCurrent - daysStart);
            flag = studyCheck(days, correctAnswered);

            //Flag if a card should be updated as needing to be studied or not
            if (flag == true) {
                String dateCreated = currentFormat.format(current.getTime());
                String updateDate = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.STUDY_DATE +
                        " = '" + dateCreated + "' WHERE " + CardContract.CardEntry._ID + " = " + id;
                String updateStudy = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.STUDY_TODAY +
                        " = 1 WHERE " + CardContract.CardEntry._ID + " = " + id;

                dbWriter.execSQL(updateDate);
                dbWriter.execSQL(updateStudy);
            }
        }
    }

    //Check if a cards values indicate a time to study
    public boolean studyCheck(int days, int correct) {
        boolean value = false;

        //Values to check if a card should be studied.
        if (correct == 0 && days != 0) {
            value = true;
        } else if (correct == 1 && days > 1) {
            value = true;
        } else if (correct == 2 && days > 4) {
            value = true;
        } else if (correct == 3 && days > 11) {
            value = true;
        } else if (correct == 4 && days % 27 == 0) {
            value = true;
        }

        return value;
    }

    // Sends user to CreateCardActivity.
    public void sendToCreateCardActivity(View view) {
        Intent intent = new Intent(this, CreateCardActivity.class);
        startActivity(intent);
    }

    // Sends user to AllTopicsActivity.
    public void sendToAllTopicsActivity(View view) {
        Intent intent = new Intent(this, AllTopicsActivity.class);
        startActivity(intent);
    }

    // Sends user to TodaysStudyActivity.
    public void sendToTodaysStudyActivity(View view) {
        Intent intent = new Intent(this, TodaysStudyActivity.class);
        startActivity(intent);
    }

    // Sends user to MyScoresActivity.
    public void sendToMyScoresActivity(View view) {
        Intent intent = new Intent(this, MyScoresActivity.class);
        startActivity(intent);
    }

    //Close database connections and cursor image if the activity is paused.
    @Override
    protected void onPause() {
        super.onPause();
        if (dbReader.isOpen()) {
            dbReader.close();
        }
        if (dbWriter.isOpen()) {
            dbWriter.close();
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        cardsDbHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

