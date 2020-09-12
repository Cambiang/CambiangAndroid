package com.cambiang.cambiang;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.cambiang.cambiang.data.Cambio;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {

    public static final String PREFS_NAME = "SaveToNotification";

    public NotificationService() {
    }


    @Override
    public int onStartCommand(Intent intent , int flags, int startId)
    {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

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
    public void showNotification(Cambio cambio)
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
                    .setContentTitle("Cambiang - " + bankName).setColor(Color.rgb(80,158,224))
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



    public void notifyUser()
    {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("CambiosLastUpdates");
        //DatabaseReference ref2 = database.getReference("CambiosPreviousValues");


        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                final Cambio cambio = dataSnapshot.getValue(Cambio.class);

                if(cambio != null)
                showNotification(cambio);

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
    }



    @RequiresApi(api = Build.VERSION_CODES.P)
    public void showSimpleNotification(Cambio cambio)
    {
        String bankName = "";

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
                .setContentTitle(bankName + "  actualização")
                .setContentText("Cambio actualiza-se agora!")
                .setAutoCancel(true)
                ;



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


    public Cambio getNotificationSharedPreferences(String bankName)
    {
        Cambio prevCambio = new Cambio();
        SharedPreferences previousCambios = getSharedPreferences(PREFS_NAME, 0);

        if(previousCambios != null && bankName != null && prevCambio != null) {
            //if some preference does not exist returns NOT OK - NOK
            if(previousCambios.getString(bankName, "NOK") != null)
            {
               String msg =  previousCambios.getString(bankName, "NOK");

               if(msg != null)
               {
                   String[] msgs = msg.split("_");
                   if(msgs.length >= 2 && msgs != null)
                   {
                       prevCambio.setUsdValue(msgs[0]);
                       prevCambio.setEuroValue(msgs[1]);

                       Log.wtf("GetSharedPref:",prevCambio.getUsdValue()+"_____"+prevCambio.getEuroValue());
                   }

               }



            }


        }


        return prevCambio;
    }




}
