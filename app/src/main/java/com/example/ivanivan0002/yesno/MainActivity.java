package com.example.ivanivan0002.yesno;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    int test;
    TextView resultNo, resultYes, question;
    Button yes, no;
    ImageButton admin;
    Firebase firebaseRef, yesData, noData, questionData, pastQuestionsRef;
    int yesCount, noCount;
    ListView pastQuestions;
    ArrayAdapter<String> adapter;
    ArrayList<String> pastQuestions_string;
    ArrayList<Question> questions;
    ProgressDialog progress;
    Boolean showResult = false;
    YesNoService yesNoService;
    boolean isBound;

    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            YesNoService.MyBinder binder = (YesNoService.MyBinder) service;
            yesNoService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        question = (TextView) findViewById(R.id.textViewQuestion);
        yes = (Button) findViewById(R.id.buttonYes);
        no = (Button) findViewById(R.id.buttonNo);
        pastQuestions = (ListView) findViewById(R.id.listViewPastQuestions);
        admin = (ImageButton) findViewById(R.id.buttonAdmin);

        admin.setImageResource(R.mipmap.ic_launcher);

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Login
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter password");
                builder.setMessage("Enter password");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton("Login",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (input.getText().toString().compareTo("yugioh") == 0) {
                                    // Then proceed to the screen
                                    Intent intent = new Intent(getApplicationContext(), Admin.class);
                                    // intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                                    toast.show();
                                    startActivity(intent);
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                builder.show();
            }
        });

        //question.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        // Hide everything until data is loaded?
        question.setVisibility(View.GONE);
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        pastQuestions.setVisibility(View.GONE);
        admin.setVisibility(View.GONE);

        // Set loading
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Loading...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        Firebase.setAndroidContext(this);

        firebaseRef = new Firebase("https://testyesno.firebaseio.com");

        yesData = firebaseRef.child("Yes");
        noData = firebaseRef.child("No");
        questionData = firebaseRef.child("questionOfTheDay");
        pastQuestionsRef = firebaseRef.child("History");

        pastQuestions_string = new ArrayList<String>();
        questions = new ArrayList<Question>();
        adapter = new ArrayAdapter<String>(this, R.layout.question_cell, pastQuestions_string);
        pastQuestions.setAdapter(adapter);
        pastQuestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Question thisQuestion = questions.get(position);
                builder.setMessage(thisQuestion.getQuestion() + "\nYes: " + thisQuestion.getYesResult() + "\n" +
                        "No: " + thisQuestion.getNoResult());
                /*
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("itemLongClick()", "Clicked! " + position);
                    }
                });
                */
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        pastQuestions.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Question thisQuestion = questions.get(position);
                builder.setMessage(thisQuestion.getQuestion() + "\nYes: " + thisQuestion.getYesResult() + "\n" +
                        "No: " + thisQuestion.getNoResult());
                /*
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("itemLongClick()", "Clicked! " + position);
                    }
                });
                */
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
        // Add the previous questions here!
        pastQuestionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Need to refresh the arraylist: maybe delete everything in the array?
                pastQuestions_string.clear();
                for (DataSnapshot questionSnapShot : dataSnapshot.getChildren()) {
                    String thatPreviousQuestion = questionSnapShot.child("question").getValue().toString();
                    int thatPreviousYesResult = Integer.parseInt(questionSnapShot.child("yesResult").getValue().toString());
                    int thatPreviousNoResult = Integer.parseInt(questionSnapShot.child("noResult").getValue().toString());
                    questions.add(new Question(thatPreviousNoResult, thatPreviousQuestion, thatPreviousYesResult));
                    // Question question = dataSnapshot.getValue(Question.class);
                    // pastQuestions_string.add(question.getQuestion());
                    pastQuestions_string.add(thatPreviousQuestion);
                }
                Collections.reverse(pastQuestions_string);
                Collections.reverse(questions);
                adapter.notifyDataSetChanged();

                // All data should have been loaded, show all elements!
                progress.dismiss();
                question.setVisibility(View.VISIBLE);
                yes.setVisibility(View.VISIBLE);
                no.setVisibility(View.VISIBLE);
                pastQuestions.setVisibility(View.VISIBLE);
                admin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("dataCancel", "why?");
            }
        });


        questionData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                question.setText(dataSnapshot.getValue().toString());
                // New question has arrived! Maybe show a notification
                // So far, the notification doesn't work...
                /*
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).
                        setContentTitle("Ask the world!").
                        setContentText(dataSnapshot.getValue().toString());
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(001, builder.build());
                */
                yes.setEnabled(true);
                no.setEnabled(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        yesData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                yesCount = Integer.parseInt(dataSnapshot.getValue().toString());
                // resultYes.setText(dataSnapshot.getValue().toString());
                if(showResult == true){
                    // yes.setText("YES (" + dataSnapshot.getValue().toString() + ")");
                    yes.setText("YES (" + yesCount + ")");
                } else {
                    yes.setText("YES");
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        noData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noCount = Integer.parseInt(dataSnapshot.getValue().toString());
                // resultNo.setText(dataSnapshot.getValue().toString());
                if (showResult == true) {
                    // yes.setText("YES (" + dataSnapshot.getValue().toString() + ")");
                    no.setText("NO (" + noCount + ")");
                } else {
                    no.setText("NO");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesCount++;
                yesData.setValue(yesCount);
                showResult = true;
                no.setText("NO (" + noCount + ")");
                yes.setText("YES (" + yesCount + ")");
                yes.setEnabled(false);
                no.setEnabled(false);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noCount++;
                noData.setValue(noCount);
                showResult = true;
                no.setText("NO (" + noCount + ")");
                yes.setText("YES (" + yesCount + ")");
                no.setEnabled(false);
                yes.setEnabled(false);
            }
        });

        Intent intent = new Intent(this, YesNoService.class);
        // bindService(intent, sConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
        /*
        Toast toast = Toast.makeText(this, "Service started", Toast.LENGTH_LONG);
        toast.show();
        */

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ivanivan0002.yesno/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ivanivan0002.yesno/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
