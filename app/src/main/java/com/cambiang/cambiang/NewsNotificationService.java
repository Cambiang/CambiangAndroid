package com.cambiang.cambiang;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewsNotificationService extends Service {

    public static final String PREFS_NAME = "SaveToNotification";

    public NewsNotificationService() {
    }


    @Override
    public int onStartCommand(Intent intent , int flags, int startId)
    {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //Log.wtf("service: ", "Im running man! 1");
                notifyUser();

            }
        });

        thread.start();


        return START_STICKY;
    }


    @Override
    public void  onDestroy()
    {


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //Not supported
        return null;
    }




    @RequiresApi(api = Build.VERSION_CODES.P)
    public void showNotification(News news)
    {


        if(news.getTitle()!= null )
        {


            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            int notificationId = 2;
            String channelId = "channel-01";
            String channelName = "news Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                notificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.mipmap.logo_notification)
                    .setContentTitle(news.getTitle()).setColor(Color.rgb(80,158,224))
                    .setContentText("Click para Ler")
                    .setAutoCancel(true);



            // Create an explicit intent for an Activity in your app
           // Intent intent = new Intent(this, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Intent intent = new Intent(getApplicationContext(),NewsPostActivity.class);
            intent.putExtra("title",news.getTitle());
            intent.putExtra("image",news.getImageURL());
            intent.putExtra("content", news.getContentText());
            intent.putExtra("contentHTML", news.getContent());
            intent.putExtra("author", news.getAuthor());
            intent.putExtra("date", news.getDate());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            mBuilder.setContentIntent(resultPendingIntent);


             //startForeground(notificationId,mBuilder.build());
            //stopForeground(false);


            notificationManager.notify(notificationId, mBuilder.build());

            // notificationManager.cancel(notificationId);

           // Log.wtf("service: ", "Im running man! Change IN:  "+news.getTitle());


        }




    }



    public void notifyUser()
    {

        try
        {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("News");


        //DatabaseReference ref2 = database.getReference("NewssPreviousValues");

        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


            }

            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                final News news = dataSnapshot.getValue(News.class);



                if(news != null)
                {
                    //Log.wtf("service: ", "Im running man! Change :  "+news.getTitle());

                    showNotification(news);

                    //Log.wtf("title:  ", news.getTitle());

                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void showNews(News news)
    {

        Intent intent;
        intent = new Intent(getApplicationContext(),NewsPostActivity.class);
        intent.putExtra("title",news.getTitle());
        intent.putExtra("image",news.getImageURL());
        intent.putExtra("content", news.getContentText());
        intent.putExtra("contentHTML", news.getContent());
        intent.putExtra("author", news.getAuthor());
        intent.putExtra("date", news.getDate());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);


    }






}
