package com.memorease.laclair.android.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

import java.util.ArrayList;

/**
 * This class manages the users scores across each topic
 */
public class MyScoresActivity extends AppCompatActivity {

    //Setup db helper
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scores);

        //Get list of topics to display
        ArrayList<String> topics = new ArrayList<>(getTopicFromDB());

        //Display list of topics
        ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topics);

        //Set to list view
        ListView listView = findViewById(R.id.myScoresListView);
        listView.setAdapter(listAdapter);

        //Set click listener for my scores activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent i = new Intent(MyScoresActivity.this, TopicScoreActivity.class);
                i.putExtra("topic", String.valueOf(adapterView.getItemAtPosition(position)));
                startActivity(i);
            }
        });
    }

    /**
     * This method gets a list of the topics currently
     * created by the user.
     *
     * @return ArrayList of topics
     */
    public ArrayList<String> getTopicFromDB() {

        //Create cursor of all topics in db
        ArrayList<String> topics = new ArrayList<>();
        String query = "SELECT topic FROM " + CardContract.CardEntry.TABLE_NAME_2;
        Cursor cursor = cardsDbHelper.getReadableDatabase().rawQuery(query, null);

        //Add each topic from the table to the list
        if (cursor.moveToFirst()) {
            do {
                topics.add(cursor.getString(cursor.getColumnIndex("topic")));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return topics;
    }

    @Override
    protected void onPause() {
        super.onPause();
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
