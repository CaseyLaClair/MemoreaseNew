package com.memorease.laclair.android.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

public class CreateNewTopic extends AppCompatActivity {

    //Declare vars and dbs
    private TextView topicTextView;

    //Create access to database
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_topic);

        db = cardsDbHelper.getWritableDatabase();

        topicTextView = findViewById(R.id.autoCompleteTopics);
    }

    /**
     * This method takes to create card activity with newly created
     * topic value passed.
     *
     * @param view
     */
    public void createWithCards(View view) {

        //Get topic text
        String topic = topicTextView.getText().toString().trim();

        //If topic and subtopic typed, match the topic and subtopic, ignoring case, display error
        if (checkExistance()) {
            Toast.makeText(this, "Topic Already Exists", Toast.LENGTH_LONG).show();
        } else {
            createTopicInDb(topic);

            //Send to create cards activity and pass in topic and subtopic
            Intent intent = new Intent(this, CreateCardActivity.class);
            intent.putExtra("topicName", topic);
            startActivity(intent);
        }

    }

    /**
     * This method checks the existance of the topic in the topics table
     *
     * @return boolean
     */
    public boolean checkExistance() {

        //Get topic text for view
        String topic = topicTextView.getText().toString().trim();

        //Set flag to flase for later use
        boolean flag = false;

        //Loop through each value and compare to db, create flag variable and set to false.
        String query = "SELECT topic FROM " + CardContract.CardEntry.TABLE_NAME;
        Cursor cursor = cardsDbHelper.getReadableDatabase().rawQuery(query, null);

        //Sort through to get list of topics
        if (cursor.moveToFirst()) {
            do {
                String sqlTopic = cursor.getString(cursor.getColumnIndex("topic"));

                if (topic.equalsIgnoreCase(sqlTopic.trim())) {
                    flag = true;
                }

            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return flag;
    }

    /**
     * This method creates the topic without taking to a new activity.
     *
     * @param view
     */
    public void createCardsLater(View view) {

        //Get topic text
        String topic = topicTextView.getText().toString().trim();

        //Check to verify if the topic exists or not
        if (checkExistance()) {
            Toast.makeText(this, "Topic Already Exists", Toast.LENGTH_LONG).show();
        } else {
            createTopicInDb(topic);
        }
    }

    /**
     * This method creates a new topic in the topics table
     *
     * @param topic
     */
    public void createTopicInDb(String topic) {

        //Check that the topic isn't already created
        //If it's not, create.
        ContentValues cv = new ContentValues();
        if (TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "No Topic Entered", Toast.LENGTH_LONG).show();
        } else {
            cv.put(CardContract.CardEntry.TOPIC, topic);
            db.insert(CardContract.CardEntry.TABLE_NAME, null, cv);
        }

        topicTextView.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
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
