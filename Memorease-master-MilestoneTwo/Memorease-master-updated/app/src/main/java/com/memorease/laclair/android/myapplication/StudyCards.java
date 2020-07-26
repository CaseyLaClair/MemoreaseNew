package com.memorease.laclair.android.myapplication;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.memorease.laclair.android.myapplication.data.CardContract;
import com.memorease.laclair.android.myapplication.data.CardsDbHelper;

/**
 * This class manages the study cards activity
 */
public class StudyCards extends AppCompatActivity {

    //Declare all variables
    TextView topicTextView;
    CardsDbHelper cardsDbHelper = new CardsDbHelper(this);
    SQLiteDatabase db;
    SQLiteDatabase cardWriter;
    Cursor cursor;
    TextView qaTextView;
    String question;
    String answer;
    RadioButton incorrect;
    RadioButton correct;
    CheckBox delete;
    boolean mShowingBack;
    int rightOrWrong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_cards);

        //Create card manager
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new CardFrontFragment())
                    .commit();
        }

        //Create readable and writable database instance
        db = cardsDbHelper.getReadableDatabase();
        cardWriter = cardsDbHelper.getWritableDatabase();

        //Declare variables for all fields in view
        incorrect = findViewById(R.id.incorrectButton);
        correct = findViewById(R.id.correctButton);
        topicTextView = findViewById(R.id.topicStudy);
        qaTextView = findViewById(R.id.textView2);
        delete = findViewById(R.id.checkBox);

        //Pull in topic passed in from other activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String topicPassed = extras.getString("topic");

            topicTextView.setText(topicPassed);
        }

        //Get topic text
        String topic = (String) topicTextView.getText();
        String query = "SELECT * FROM " + CardContract.CardEntry.TABLE_NAME + " WHERE topic LIKE \"%" + topic + "%\";";
        cursor = db.rawQuery(query, null);

        //Get the first question and answer from the cursor
        //If none, display that none exist
        if (cursor.moveToFirst()) {
            question = cursor.getString(cursor.getColumnIndex("question"));
            answer = cursor.getString(cursor.getColumnIndex("answer"));
            qaTextView.setText(question);
        } else {
            question = "No Cards Available";
            answer = "Nothing On This Side Either";
            qaTextView.setText(question);
        }
    }

    //Create Front of the study card
    public static class CardFrontFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_card_front, container, false);

        }
    }

    //Create back of the study card
    public static class CardBackFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_card_back, container, false);
        }
    }

    /**
     * This method flips the card to display the answer or question
     * depending on which is already showing
     *
     * @param view
     */
    public void flipCard(View view) {

        if (qaTextView.getText().equals(question)) {
            qaTextView.setText(answer);
        } else {
            qaTextView.setText(question);
        }

        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for
        // the back of the card, uses custom animations, and is part of the fragment
        // manager's back stack.

        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources
                // representing rotations when switching to the back of the card, as
                // well as animator resources representing rotations when flipping
                // back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a
                // fragment representing the next page (indicated by the
                // just-incremented currentPage variable).
                .replace(R.id.container, new CardBackFragment())

                // Add this transaction to the back stack, allowing users to press
                // Back to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();

    }

    /**
     * This method pulls the next card to be studied
     * and also updates the previous card whether the user got it right or wrong.
     *
     * @param view
     * @throws SQLException
     */
    public void nextCard(View view) throws SQLException {

        //If no cards are available, display notification
        if (qaTextView.getText().equals("No Cards Available")||qaTextView.getText().equals("Nothing On This Side Either"))
            return;

        //Check if the card is about to be deleted
        if (delete.isChecked()) {
            cardWriter.delete(CardContract.CardEntry.TABLE_NAME, "question=? and answer=?", new String[]{question, answer});
            delete.setChecked(false);
            checkMoveToNext();
        } else {
            rightOrWrong = cursor.getInt(cursor.getColumnIndex("correctanswered"));
            if (incorrect.isChecked() && rightOrWrong != 0) {
                rightOrWrong--;
            } else if (correct.isChecked() && rightOrWrong < 4) {
                rightOrWrong++;
            }

            ContentValues values = new ContentValues();
            values.put(CardContract.CardEntry.CORRECT_ANSWERED, rightOrWrong);
            cardWriter.update(CardContract.CardEntry.TABLE_NAME, values, CardContract.CardEntry.QUESTION + " = ?", new String[]{question});

            checkMoveToNext();
        }
    }

    /**
     * This method checks if there are other cards to study.
     * If not, it cycles back to the beginning
     */
    public void checkMoveToNext() {
        if (cursor.moveToNext()) {
            question = cursor.getString(cursor.getColumnIndex("question"));
            answer = cursor.getString(cursor.getColumnIndex("answer"));
            qaTextView.setText(question);
            incorrect.setChecked(true);
        } else {
            onPause();
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
