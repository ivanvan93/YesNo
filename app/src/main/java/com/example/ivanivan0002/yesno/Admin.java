package com.example.ivanivan0002.yesno;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class Admin extends AppCompatActivity {

    Button submit, clearHistory;
    EditText newQuestion;
    Firebase firebaseRef, history, newOldEntry;
    int historyCount;
    String oldQuesiton;
    int oldNoResult, oldYesResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        firebaseRef = new Firebase("https://testyesno.firebaseio.com");

        submit = (Button)findViewById(R.id.buttonSubmitNewQuestion);
        newQuestion = (EditText)findViewById(R.id.editTextNewQuestion);
        clearHistory = (Button) findViewById(R.id.buttonClearHistory);

        newQuestion.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Get the history count
        history = firebaseRef.child("History");

        history.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyCount = (int)dataSnapshot.getChildrenCount();
                Log.d("Count", Integer.toString(historyCount));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                oldQuesiton = dataSnapshot.child("questionOfTheDay").getValue().toString();
                oldYesResult = Integer.parseInt(dataSnapshot.child("Yes").getValue().toString());
                oldNoResult = Integer.parseInt(dataSnapshot.child("No").getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i <= historyCount; i++) {
                    firebaseRef.child("History").child(Integer.toString(i)).setValue(null);
                    finish();
                }
                Toast toast = Toast.makeText(getApplicationContext(), "History cleared!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if the string ends with a question mark
                if(newQuestion.getText().toString().substring(newQuestion.getText().toString().length()-1).compareTo("?") != 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "Your question does not end with a ?", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                // Get the old result and put it in the new one
                newOldEntry = history.child(Integer.toString(historyCount));
                /*
                newOldEntry.child("noResult").setValue(oldNoResult);
                newOldEntry.child("yesResult").setValue(oldYesResult);
                newOldEntry.child("question").setValue(oldQuesiton);
                */

                Question newQuestion1 = new Question(oldNoResult, oldQuesiton, oldYesResult);
                newOldEntry.setValue(newQuestion1);

                // Update the database with the new question and default yes/no values
                firebaseRef.child("Yes").setValue(0);
                firebaseRef.child("No").setValue(0);
                firebaseRef.child("questionOfTheDay").setValue(newQuestion.getText().toString());

                // Return to main screen once complete
                /*
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                */
                finish();
            }
        });

    }

}
