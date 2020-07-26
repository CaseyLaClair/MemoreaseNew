package com.memorease.laclair.android.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

/**
 * This class displays analytics for individual topics
 */
public class TopicScoreActivity extends AppCompatActivity {

    //Declare all variables
    private TextView topicTextView;
    private TextView totalText, proficientText, goodText, okText, needsWorkText;
    int proficient, good, ok, needsWork, answerValue, totalValue;
    Cursor cursor;
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase cardReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_score_activity);

        //Declare variables for all views
        totalText = findViewById(R.id.totalCards);
        proficientText = findViewById(R.id.proficient);
        goodText = findViewById(R.id.good);
        okText = findViewById(R.id.ok);
        needsWorkText = findViewById(R.id.needsWork);

        topicTextView = findViewById(R.id.myScoresTitle);
        cardReader = cardsDbHelper.getReadableDatabase();

        //Bundle extras for any info passed from other activities
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String topicPassed = extras.getString("topic");

            topicTextView.setText(topicPassed);
        }

        //Select topic from DB
        String topic = (String) topicTextView.getText();
        String query = "SELECT * FROM " + CardContract.CardEntry.TABLE_NAME + " WHERE topic LIKE \"%" + topic + "%\";";
        cursor = cardReader.rawQuery(query, null);

        //Sort through correctly answered
        if (cursor.moveToFirst()) {
            do {
                answerValue = cursor.getInt(cursor.getColumnIndex("correctanswered"));
                incrementValue(answerValue);
                totalValue++;
            } while (cursor.moveToNext());
        }

        //Set texts
        totalText.setText("Total Cards: " + totalValue);
        proficientText.setText("Proficient (3/3): " + getPercentage(totalValue, proficient));
        goodText.setText("Good (2/3): " + getPercentage(totalValue, good));
        okText.setText("Ok (1/3): " + getPercentage(totalValue, ok));
        needsWorkText.setText("Needs Work (0/3): " + getPercentage(totalValue, needsWork));
    }

    /**
     * This method gets the percentage for values of the
     * cards studied and their proficiency.
     *
     * @param total
     * @param amount
     * @return String
     */
    public String getPercentage(int total, int amount) {
        String str;
        String temp;
        int percentage;

        //Calculate percentages for cards correctly answered
        if (total != 0) {
            percentage = (int) (((double) amount / total) * 100);
            temp = String.valueOf(percentage);
            str = temp + "%";
        } else
            str = "No Cards";

        return str;
    }

    //Increment values for how proficient the studier is
    public void incrementValue(int correct) {
        switch (correct) {
            case 4:
                proficient++;
                break;
            case 3:
                proficient++;
                break;
            case 2:
                good++;
                break;
            case 1:
                ok++;
                break;
            default:
                needsWork++;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (cardReader.isOpen()) {
            cardReader.close();
        }
        cardsDbHelper.close();
        finish();
    }
}