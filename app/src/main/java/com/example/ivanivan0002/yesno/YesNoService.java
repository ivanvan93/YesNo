package com.example.ivanivan0002.yesno;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class YesNoService extends IntentService {

    Firebase firebaseRef, question;
    private final IBinder binder = new MyBinder();
    int count = 0;

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    public YesNoService(){
        super("YesNoService");
        firebaseRef = new Firebase("https://testyesno.firebaseio.com");
        question = firebaseRef.child("questionOfTheDay");

    }

    protected void onHandleIntent(Intent intent){
        question = firebaseRef.child("questionOfTheDay");
        question.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(count == 0){
                    count++;
                } else
                {
                    Log.d("Service()", "Data has been changed");
                    // Show notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("YesNo");
                    builder.setContentText("A new question has arrived!");
                    builder.setAutoCancel(true);
                    builder.setDefaults(NotificationCompat.DEFAULT_ALL);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

                    builder.setContentIntent(pendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public class MyBinder extends Binder {
        YesNoService getService(){
            return YesNoService.this;
        }
    }
}
