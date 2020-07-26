package com.memorease.laclair.android.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.memorease.laclair.android.myapplication.data.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class creates an instance to be used for activity_create_card
 */
public class CreateCardActivity extends AppCompatActivity {

    //Declare all vars and open Dbs
    private AutoCompleteTextView topicTextView;
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase db;
    Cursor cursor;

    /**
     * onCreate method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_card);

        //Get writable database
        db = cardsDbHelper.getWritableDatabase();

        //Init textview
        topicTextView = findViewById(R.id.autoCompleteTextViewTopics);

        //Get extras if info is being passed from a separate activity.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String topicPassed = extras.getString("topicName");

            topicTextView.setText(topicPassed);
        }

        //Fill array list of all topics to display as autocomplete text
        ArrayList<String> topics = new ArrayList<>(getTopicFromDB());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topics);
        topicTextView.setAdapter(adapter);
    }

    /**
     * This method gets all topic values from the Db and fills an array
     *
     * @return ArrayList of topics
     */
    public ArrayList<String> getTopicFromDB() {

        /*
        * Create an array list of topics created. 
        */
        ArrayList<String> topics = new ArrayList<>();
        String query = "SELECT topic FROM " + CardContract.CardEntry.TABLE_NAME;
        cursor = cardsDbHelper.getReadableDatabase().rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                topics.add(cursor.getString(cursor.getColumnIndex("topic")));
            } while (cursor.moveToNext());
        }
        return topics;
    }

    /**
     * This method creates a row in the cards database for
     * the cards and topics table
     *
     * @param view
     */
    public void createCardOnDone(View view) {

        //Declare each view and get the text input from each
        EditText acTopicText = findViewById(R.id.autoCompleteTextViewTopics);
        String topic = acTopicText.getText().toString().trim();

        //Declare question text for card
        EditText questionText = findViewById(R.id.questionCardText);
        String question = questionText.getText().toString().trim();

        //Get answer text for card
        EditText answerText = findViewById(R.id.answerCardText);
        String answer = answerText.getText().toString().trim();

        //Get the current calendar date to set when the card is created
        Calendar current = Calendar.getInstance();
        DateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateCreated = currentFormat.format(current.getTime());

        //Wrap all values up to be input in db
        ContentValues cv = new ContentValues();
        cv.put(CardContract.CardEntry.TOPIC, topic);
        cv.put(CardContract.CardEntry.QUESTION, question);
        cv.put(CardContract.CardEntry.ANSWER, answer);
        cv.put(CardContract.CardEntry.STUDY_DATE, dateCreated);
        cv.put(CardContract.CardEntry.CORRECT_ANSWERED, 0);
        cv.put(CardContract.CardEntry.STUDY_TODAY, 0);

        //Insert values and create new cards table row
        db.insert(CardContract.CardEntry.TABLE_NAME, null, cv);

        //Wrap topic value to create new row in topic table
        ContentValues cv2 = new ContentValues();
        cv2.put(CardContract.CardEntry.TOPIC, topic);

        //Check topics table if the topic already exists before inputting
        if (!checkExistance()) {
            db.insert(CardContract.CardEntry.TABLE_NAME_2, null, cv2);
        }

        //Clear text fields to allow creation of another card
        questionText.getText().clear();
        answerText.getText().clear();

        Toast.makeText(CreateCardActivity.this, "Card Created", Toast.LENGTH_LONG).show();
    }

    /**
     * This method checks the topics table to see if
     * the input value is already in the table
     *
     * @return boolean
     */
    public boolean checkExistance() {

        //Get the value from the textview
        String topic = topicTextView.getText().toString().trim();

        boolean flag = false;

        //Loop through each value and compare to db, create flag variable and set to false.
        String query = "SELECT topic FROM " + CardContract.CardEntry.TABLE_NAME_2;
        Cursor cursor = cardsDbHelper.getReadableDatabase().rawQuery(query, null);

        //Check each value in the cursor to see if it equates to current textview
        if (cursor.moveToFirst()) {
            do {
                String sqlTopic = cursor.getString(cursor.getColumnIndex("topic"));

                if (topic.equalsIgnoreCase(sqlTopic.trim())) {
                    flag = true;
                }

            } while (cursor.moveToNext());
        }
        //Close cursor if its still open
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return flag;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (db.isOpen()) {
            db.close();
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

