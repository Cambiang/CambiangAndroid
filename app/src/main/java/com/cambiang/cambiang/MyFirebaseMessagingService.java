package com.cambiang.cambiang;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Random;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Cambio cambio;
    Utilities utilities;


    public MyFirebaseMessagingService() {

    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {

        if(remoteMessage != null)
        {
            if(remoteMessage.getData() != null)
            {
                if(remoteMessage.getData().get("cambio") != null)
                {
                    showCambioNotification(remoteMessage);
                }

                //Log.wtf("NEWS",remoteMessage.getData().get("newsId").toString());

                if(remoteMessage.getData().get("newsId") != null)
                {
                    showNewsNotification(remoteMessage);
                }

                if(remoteMessage.getData().get("ad_id") != null)
                {
                    showAdsNotification(remoteMessage);
                }
            }

            if(remoteMessage.getNotification() != null)
            {
                showMessage(remoteMessage);
            }

            if(remoteMessage.getData().get("cambioHouse") != null)
            {
                showCambioHouseNotification(remoteMessage);
            }


        }

    }





    @Override
    public void onNewToken(String token) {
        //Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
       // sendRegistrationToServer(token);
        saveRegistrationToken(token);
    }

    public void saveRegistrationToken(String token)
    {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("userTokens");
        String tokenId = ref.push().getKey();

        if(token != null && !token.isEmpty())
            ref.child(tokenId).setValue(token);


    }


    /********************CAMBIO NOTIFICATION**************************************************/

    public void showCambioNotification(RemoteMessage remoteMessage)
    {


        if(remoteMessage.getData().get("cambio") != null)
        {
            cambio = new Cambio();

            String cambioStr = remoteMessage.getData().get("cambio");

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            cambio = gson.fromJson(cambioStr,Cambio.class);



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/Banks");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                try {

                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {

                        if(data != null)
                        {
                            Bank bank = data.getValue(Bank.class);

                            if(bank != null)
                            {
                                showOnlyApprovedBanks(cambio, bank);
                            }

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        }


    }

    public void showOnlyApprovedBanks(Cambio cambio, Bank bank)
    {
        if(cambio != null && bank != null)
        {
            if(cambio.getBank().equals(bank.getName()) && bank.getLogo() != null && bank.getLogoColor() != null)
            {
                if(!bank.getLogo().isEmpty() && !bank.getLogoColor().isEmpty())
                {
                    String bankName = "";
                    String tendencyUSD = "Constante";
                    String tendencyEUR = "Constante";

                    //Cambio cambioPreviousValue = new Cambio();

                    if(cambio.getBank()!= null && cambio.getBank().equals("KINGUILAS"))
                    {
                        if(cambio.getBank()!= null)
                        {
                            // cambioPreviousValue = getNotificationSharedPreferences(bankName);
                            bankName = "M. INFORMAL";
                        }

                    }else
                    {

                        if(cambio.getBank() != null)
                        {
                            bankName = cambio.getBank();
                            //cambioPreviousValue = getNotificationSharedPreferences(bankName);
                        }

                    }

                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                    int notificationId = 1;
                    String channelId = "channel-01";
                    String channelName = "Channel Name";
                    int importance = NotificationManager.IMPORTANCE_HIGH;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        NotificationChannel mChannel = new NotificationChannel(
                                channelId, channelName, importance);
                        notificationManager.createNotificationChannel(mChannel);
                    }

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                            .setSmallIcon(R.mipmap.logo_notification)
                            .setContentTitle("Banco - " + bankName).setColor(Color.rgb(8,103,146))
                            .setContentText("Nova actualização do câmbio, click para ver!")
                            .setAutoCancel(true);


                    // Create an explicit intent for an Activity in your app
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


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

                }
            }
        }
    }

    public void showNewsNotification(RemoteMessage remoteMessage)
    {

        if(remoteMessage.getData().get("newsId") != null)
        {
            String newsId = remoteMessage.getData().get("newsId");

            if(!newsId.isEmpty())
            {
                getNews(newsId);
            }

        }


    }


    public void getNews(String newsId)
    {

        if(!newsId.isEmpty())
        {
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/News");

            ref.orderByKey().equalTo(newsId).addChildEventListener(
                    new ChildEventListener()
                    {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
                        {

                            try {


                                if(dataSnapshot.getValue(News.class) != null)
                                {
                                    News news = dataSnapshot.getValue(News.class);

                                    if(news != null)
                                    {
                                        showNews(news);
                                    }

                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
                        {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName)
                        {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    }
            );
        }

    }


    public void showNews(News news)
    {

        if(news.getTitle()!= null )
        {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int notificationId = 2;
            String channelId = "channel-02";
            String channelName = "news Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                notificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.logo_notification)
                    .setContentTitle(news.getTitle()).setColor(Color.rgb(8,103,146))
                    .setContentText("Click para Ler")
                    .setAutoCancel(true);



            // Create an explicit intent for an Activity in your app
            // Intent intent = new Intent(this, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Intent intent = new Intent(this,NewsPostActivity.class);
            intent.putExtra("title",news.getTitle());
            intent.putExtra("image",news.getImageURL());
            intent.putExtra("content", news.getContentText());
            intent.putExtra("contentHTML", news.getContent());
            intent.putExtra("author", news.getAuthor());
            intent.putExtra("date", news.getDate());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            mBuilder.setContentIntent(resultPendingIntent);

            notificationManager.notify(notificationId, mBuilder.build());


        }


    }


    public void showAdsNotification(RemoteMessage remoteMessage)
    {


        if(remoteMessage.getData().get("ad_id") != null)
        {

            String adId = remoteMessage.getData().get("ad_id");
            String adTitle = remoteMessage.getData().get("ad_title");
            String adHref = remoteMessage.getData().get("ad_href");
            String adState = remoteMessage.getData().get("ad_state");



            if(!adId.isEmpty() )
            {

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int notificationId = 2;
                String channelId = "channel-03";
                String channelName = "ads Channel";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    NotificationChannel mChannel = new NotificationChannel(
                            channelId, channelName, importance);
                    notificationManager.createNotificationChannel(mChannel);
                }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.logo_notification)
                        .setContentTitle(adTitle).setColor(Color.rgb(8,103,146))
                        .setContentText("Click para ver")
                        .setAutoCancel(true);



                // Create an explicit intent for an Activity in your app
                // Intent intent = new Intent(this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


                //  Log.wtf("ad.getHref()",ad.getHref());

                if(!adState.isEmpty() && adState.equals("ON"))
                {
                    if(adHref.contains("http") || adHref.contains("https"))// missing 'http://' will cause crashed
                    {
                        try
                        {
                            Uri uri = Uri.parse(adHref);

                            Intent intent = new Intent(this,WebviewActivity.class);
                            intent.putExtra("url",uri.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                            stackBuilder.addNextIntent(intent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            mBuilder.setContentIntent(resultPendingIntent);

                            notificationManager.notify(notificationId, mBuilder.build());

                        } catch (Exception e){
                            throw new RuntimeException(e);
                        }


                    }

                }

            }




        }


    }

    public void showMessage(RemoteMessage remoteMessage)
    {
        if(remoteMessage.getNotification() != null)
        {
            RemoteMessage.Notification message = remoteMessage.getNotification();

            if(message.getTitle() != null && message.getBody() != null)
            {
                if(!message.getTitle().isEmpty() && !message.getBody().isEmpty())
                {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        int notificationId = 2;
                        String channelId = "channel-04";
                        String channelName = "ads Channel";
                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            NotificationChannel mChannel = new NotificationChannel(
                                    channelId, channelName, importance);
                            notificationManager.createNotificationChannel(mChannel);
                        }



                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.mipmap.logo_notification)
                                .setContentTitle(message.getTitle()).setColor(Color.rgb(8,103,146))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message.getBody()))
                                .setAutoCancel(true);

                    // Create an explicit intent for an Activity in your app
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


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


                }
            }
        }
    }

    public void showCambioHouseNotification(RemoteMessage remoteMessage)
    {


        if(remoteMessage.getData().get("cambioHouse") != null)
        {
            cambio = new Cambio();

            String cambioStr = remoteMessage.getData().get("cambioHouse");

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            cambio = gson.fromJson(cambioStr,Cambio.class);



            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/CambioHouses");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    try {

                        for (DataSnapshot data : dataSnapshot.getChildren())
                        {

                            if(data != null)
                            {
                                Bank bank = data.getValue(Bank.class);

                                if(bank != null)
                                {
                                    showOnlyApprovedCambioHouses(cambio, bank);
                                }

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }            }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }


    }

    public void showOnlyApprovedCambioHouses(Cambio cambio, Bank bank)
    {
        if(cambio != null && bank != null)
        {
            if(cambio.getBank().equals(bank.getName()) && bank.getLogo() != null && bank.getLogoColor() != null)
            {
                if(!bank.getLogo().isEmpty() && !bank.getLogoColor().isEmpty())
                {
                    String bankName = "";
                    String tendencyUSD = "Constante";
                    String tendencyEUR = "Constante";

                    //Cambio cambioPreviousValue = new Cambio();

                    if(cambio.getBank()!= null && cambio.getBank().equals("KINGUILAS"))
                    {
                        if(cambio.getBank()!= null)
                        {
                            // cambioPreviousValue = getNotificationSharedPreferences(bankName);
                            bankName = "M. INFORMAL";
                        }

                    }else
                    {

                        if(cambio.getBank() != null)
                        {
                            bankName = cambio.getBank();
                            //cambioPreviousValue = getNotificationSharedPreferences(bankName);
                        }

                    }

                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                    int notificationId = 1;
                    String channelId = "channel-05";
                    String channelName = "Channel Name";
                    int importance = NotificationManager.IMPORTANCE_HIGH;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        NotificationChannel mChannel = new NotificationChannel(
                                channelId, channelName, importance);
                        notificationManager.createNotificationChannel(mChannel);
                    }

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                            .setSmallIcon(R.mipmap.logo_notification)
                            .setContentTitle("Casa de Câmbio - " + bankName).setColor(Color.rgb(8,103,146))
                            .setContentText("Nova actualização do câmbio, click para ver!")
                            .setAutoCancel(true);


                    // Create an explicit intent for an Activity in your app
                    Intent intent = new Intent(this, CambioHouseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


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

                }
            }
        }
    }


}
