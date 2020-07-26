package com.memorease.laclair.android.myapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

/**
 * This class manages a single topic activity
 */
public class SingleTopic extends AppCompatActivity {

    //Setup DB helper
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase cards;

    private TextView topicTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_topic);

        //Get writable database
        cards = cardsDbHelper.getWritableDatabase();

        topicTextView = findViewById(R.id.topicTop);

        //Bundle extras together 
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String topicPassed = extras.getString("topic");

            topicTextView.setText(topicPassed);
        }
    }

    /**
     * This method takes the user to the study cards
     * activity for the current topic
     *
     * @param view
     */
    public void takeToStudyCards(View view) {
        Intent intent = new Intent(this, StudyCards.class);
        intent.putExtra("topic", topicTextView.getText());
        startActivity(intent);
    }

    /**
     * This method takes the user to create cards for the current topic
     *
     * @param view
     */
    public void takeToCreateCard(View view) {
        Intent intent = new Intent(this, CreateCardActivity.class);
        intent.putExtra("topicName", topicTextView.getText());
        startActivity(intent);
    }

    /**
     * This method deletes all cards related to an activity and
     * deletes the topic from the topics table
     *
     * @param view
     */
    public void deleteAllCards(View view) {

        //Prepare to display topic text
        String topic = (String) topicTextView.getText();

        //Delete card from SQL library
        cards.execSQL("DELETE FROM " + CardContract.CardEntry.TABLE_NAME + " WHERE topic LIKE \"%" + topic + "%\";");

        //Start activity from Alltopics
        Intent intent = new Intent(this, AllTopicsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cards.isOpen()) {
            cards.close();
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
