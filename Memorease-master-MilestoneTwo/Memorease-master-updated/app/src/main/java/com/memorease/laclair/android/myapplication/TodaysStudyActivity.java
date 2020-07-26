package com.memorease.laclair.android.myapplication;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class manages the study today activity and does the same
 * as the stuudy cards activity but only pulling in cards
 * that are due for study.
 */
public class TodaysStudyActivity extends AppCompatActivity {

    //Declare all variables
    TextView topicTextView;
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase db;
    SQLiteDatabase cardWriter;
    Cursor cursor;
    TextView qaTextView;
    String topic;
    String question;
    String answer;
    RadioButton incorrect;
    RadioButton correct;
    CheckBox delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_study);

        //Create readable and writable database instance
        db = cardsDbHelper.getReadableDatabase();
        cardWriter = cardsDbHelper.getWritableDatabase();

        //Declare variables for all views
        incorrect = findViewById(R.id.incorrect2);
        correct = findViewById(R.id.correct2);
        topicTextView = findViewById(R.id.topicStudyToday);
        qaTextView = findViewById(R.id.textView4);
        delete = findViewById(R.id.checkBox2);

        //Check if there are cards to study
        String query = "SELECT * FROM " + CardContract.CardEntry.TABLE_NAME + " WHERE studytoday = 1;";
        cursor = db.rawQuery(query, null);

        //Move through the first cards to show
        if (cursor.moveToFirst()) {
            topic = cursor.getString(cursor.getColumnIndex("topic"));
            question = cursor.getString(cursor.getColumnIndex("question"));
            answer = cursor.getString(cursor.getColumnIndex("answer"));
            topicTextView.setText(topic);
            qaTextView.setText(question);
        } else {
            question = "No Cards Available";
            answer = "Nothing On This Side Either";
            qaTextView.setText(question);
        }
    }

    //Flip card action between question and answer
    public void flipCardStudy(View view) {

        if (qaTextView.getText().equals(question)) {
            qaTextView.setText(answer);
        } else {
            qaTextView.setText(question);
        }

    }

    //Set the next card to show
    public void nextCardStudy(View view) throws SQLException {

        //Notify if there are no cards to show
        if (qaTextView.getText().equals("No Cards Available"))
            return;

        //Prepare to delete the card if checked
        if (delete.isChecked()) {
            cardWriter.delete(CardContract.CardEntry.TABLE_NAME, "question=? and answer=?", new String[]{question, answer});
            delete.setChecked(false);
            checkMoveToNext();
        } else {
            Calendar current = Calendar.getInstance();
            DateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd");
            String studyDate = currentFormat.format(current.getTime());

            int rightOrWrong = cursor.getInt(cursor.getColumnIndex("correctanswered"));

            //Check if answer was correct or incorrect
            if (incorrect.isChecked() && rightOrWrong != 0) {
                rightOrWrong--;
            } else if (correct.isChecked() && rightOrWrong < 4) {
                rightOrWrong++;
            }

            //Update right or wrong
            String update = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.CORRECT_ANSWERED +
                    " = " + rightOrWrong + " WHERE " + CardContract.CardEntry.QUESTION + " = '" + question + "'";

            //Update to study or not
            String updateStudy = "UPDATE " + CardContract.CardEntry.TABLE_NAME + " SET " + CardContract.CardEntry.STUDY_TODAY +
                    " = 0 WHERE " + CardContract.CardEntry.QUESTION + " = '" + question + "'";

            //Update the date
            String updateDate = "UPDATE " + CardContract.CardEntry.STUDY_DATE + " SET " + CardContract.CardEntry.STUDY_DATE +
                    " = " + studyDate + " WHERE " + CardContract.CardEntry.QUESTION + " = '" + question + "'";

            //Execute writables to the database
            cardWriter.execSQL(update);
            cardWriter.execSQL(updateStudy);
            cardWriter.execSQL(updateDate);

            checkMoveToNext();
        }
    }

    //Move to the next card for study
    public void checkMoveToNext() {
        if (cursor.moveToNext()) {
            topic = cursor.getString(cursor.getColumnIndex("topic"));
            question = cursor.getString(cursor.getColumnIndex("question"));
            answer = cursor.getString(cursor.getColumnIndex("answer"));
            topicTextView.setText(topic);
            qaTextView.setText(question);
            incorrect.setChecked(true);
        } else {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (db.isOpen()) {
            db.close();
        }
        if (cardWriter.isOpen()) {
            cardWriter.close();
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        cardsDbHelper.close();
        finish();
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
