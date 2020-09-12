package com.cambiang.cambiang;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.FoldingCube;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Utilities {


    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";
    public static  final  int JUROS_ID = 100;
    public static  final  int INFORMATION_ID = 101;
    public static final String adTestId = "ca-app-pub-3940256099942544/6300978111";
    public static final String adRealId = "ca-app-pub-6453558774926417/6691272299";
    SharedPreferences settings;

    public void Utilities(){}



    synchronized public void getDefaultTracker(Context context)
    {
         /*  UNCOMMMENT THIS METHOD BEFORE GENERATE APP BUNDLE FOR RELEASE */

        /*
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(1800);
        sTracker = analytics.newTracker(GOOGLE_TRACK_ID);
        sTracker.enableExceptionReporting(true);
        sTracker.enableAdvertisingIdCollection(false);
        sTracker.enableAutoActivityTracking(true);

         */



    }


    public void sendAnalyticsEvent(String category, String action)
    {
        /*  UNCOMMMENT THIS METHOD BEFORE GENERATE APP BUNDLE FOR RELEASE */

        /*

        if(category != null && action != null)
        {
            //Send first Analytics event before go to activity
            sTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
        }

         */



    }

    //UNCOMMENT REAL ID AND COMMENT TEST ID BEFORE RELEASE

    public LinearLayout addAds(Activity activity, AdSize adSize, int visibility, int adID, int bottomPadding)
    {
        LinearLayout layout = new LinearLayout(activity.getApplicationContext());

        // add AdMob
        if(activity != null && adSize != null)
        {
            /*  UNCOMMMENT THIS METHOD BEFORE GENERATE APP BUNDLE FOR RELEASE */


            AdView adView = new AdView(activity.getApplicationContext());


           // adView.setAdUnitId(adRealId); //reall ID

            adView.setAdUnitId(adTestId); // Test ID
            adView.setAdSize(adSize);
            adView.setId(adID);


            LinearLayout.LayoutParams adLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            adView.setLayoutParams(adLayoutParams);
            // 広告表示位置は画面下部
            layout.addView(adView);
            layout.setPadding(0,0,0,bottomPadding);
            layout.setGravity(Gravity.BOTTOM);
            layout.setVisibility(visibility);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            activity.addContentView(layout, layoutParams);

            // load ad
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

        }

        return layout;
    }


    public boolean isOnline(Context context)
    {
        ConnectivityManager connMngr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMngr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
        {
                return true;
        }else
            {
                return false;
            }
    }

    public static double roundWithDecimalPlaces(double value, int places)
    {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }


    public String putDateYearMonthDay(String date)
    {
        String[] aux = date.split("/");

        return aux[2]+"/"+aux[1]+"/"+aux[0];


    }

    public String extractMonthFromRefDate(String date)
    {
        String[] aux = date.split("-");

        return aux[1];

    }

    public String extractYearFromRefDate(String date)
    {
        String[] aux = date.split("-");

        return aux[2];

    }
    public Date convertDateFormat(String date)
    {

        try
        {

            return new SimpleDateFormat("dd/MM/yyyy").parse(date);

        } catch (ParseException e)
        {
            e.printStackTrace();
        }


        return null;
    }


    public String getThisYear()
    {
        Date calendar = Calendar.getInstance().getTime();

        String formattedDate = new SimpleDateFormat("yyyy").format(calendar);

      return formattedDate;
    }

    public String getThisMonth()
    {
        Date calendar = Calendar.getInstance().getTime();

        String formattedDate = new SimpleDateFormat("MM").format(calendar);

        //Log.wtf("date",formattedDate);
        return formattedDate;
    }

    public Double getArrayDoublesMin(ArrayList<Double> arrayDoubs)
    {
        Double min = 0.0;

        if(arrayDoubs.size() > 0)
        {
            min = arrayDoubs.get(0);

            for (int i = 1; i < arrayDoubs.size(); i++)
            {
                if (arrayDoubs.get(i) < min) min = arrayDoubs.get(i);
            }
        }

        return roundWithDecimalPlaces(min,0);
    }

    public Double getArrayDoublesMax(ArrayList<Double> arrayDoubs)
    {
        Double max = 0.0;

        if(arrayDoubs.size() > 0)
        {
            max = arrayDoubs.get(0);

            for (int i = 1; i < arrayDoubs.size(); i++)
            {
                if (arrayDoubs.get(i) > max) max = arrayDoubs.get(i);
            }
        }

        return roundWithDecimalPlaces(max,0);
    }

    public Double getArrayDoublesMed(ArrayList<Double> arrayDoubs)
    {
        int counter = 0;
        Double total = 0.0;
        Double med = 0.0;

        if(arrayDoubs.size() > 0)
        {
            for (int i = 0; i < arrayDoubs.size(); i++)
            {
                total = total + arrayDoubs.get(i);
                counter++;

            }

            if(counter != 0) med = total/(new Double(counter)); //convert counter to Double before dividing it
        }

        return roundWithDecimalPlaces(med,0);
    }






    public void loadingAnimation(int visibility, String type, Activity activity, int spinkitId)
    {

        if(activity != null)
        {
            ProgressBar progressBar = (ProgressBar) activity.findViewById(spinkitId);
            progressBar.setVisibility(visibility);

            if (type.equals("chasingDots")) {
                ChasingDots cd = new ChasingDots();
                progressBar.setIndeterminateDrawable(cd);

            } else {
                if (type.equals("circle")) {
                    Circle crc = new Circle();
                    progressBar.setIndeterminateDrawable(crc);

                } else {
                    if (type.equals("foldingCube")) {
                        FoldingCube fc = new FoldingCube();
                        progressBar.setIndeterminateDrawable(fc);

                    }
                }
            }

        }



        /*Type of style
        RotatingPlane	RotatingPlane
        DoubleBounce	DoubleBounce
        Wave	Wave
        WanderingCubes	WanderingCubes
        Pulse	Pulse
        ChasingDots	ChasingDots
        ThreeBounce	ThreeBounce
        Circle	Circle
        CubeGrid	CubeGrid
        FadingCircle	FadingCircle
        FoldingCube	FoldingCube
        RotatingCircle*/

        /*
        @style/SpinKitView
        @style/SpinKitView.Circle
        @style/SpinKitView.Large
        @style/SpinKitView.Small
        @style/SpinKitView.Small.DoubleBounce
        */
    }


    public AdSize getTypeOfAd(String adType)
    {

        if (adType.equals("BANNER"))
        {
            return AdSize.BANNER;
        }

        if (adType.equals("LARGE_BANNER"))
        {
            return AdSize.LARGE_BANNER;
        }

        if (adType.equals("SMART_BANNER"))
        {
            return AdSize.SMART_BANNER;
        }

        if (adType.equals("FULL_BANNER"))
        {
            return AdSize.FULL_BANNER;
        }


        return AdSize.BANNER;
    }


    public void updatePilotIfFoundChanged(String dbPathName, String[] pilotArray, String keyToUpdate, String command,String command2, Activity activity, String keyName, String PREFS_NAME)
    {
        Boolean isNotSameDate = false;
        Log.wtf("KEY","FUNCTION 1");


        if(!dbPathName.isEmpty() && pilotArray != null && PREFS_NAME != null && activity != null && keyName != null)
        {
            DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference(dbPathName);

            for(int i=0; i < pilotArray.length; i++)
            {
                String pilotStr = pilotArray[i];
                Log.wtf("KEY","FUNCTION 2: " +dbPathName + "--" + pilotStr + dbReference);

                dbReference.child(pilotArray[i]).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        String dbValue = (String) dataSnapshot.getValue();

                        String localValue = getDataSingleValueLocally(activity, keyName, PREFS_NAME);

                        Log.wtf("X",localValue);


                        if(localValue != null && dbValue != null)
                        {
                            if(!localValue.equals(dbValue))
                            {
                                Log.wtf("X2","#####");
                                saveSingleDataValueLocally(dbValue,keyName,PREFS_NAME,activity);

                                if(command != null && !keyToUpdate.isEmpty())
                                {

                                    saveSingleDataValueLocally(command,keyToUpdate,PREFS_NAME,activity);

                                }
                            }else
                                {
                                    if(command2 != null && !keyToUpdate.isEmpty())
                                    {

                                        saveSingleDataValueLocally(command2,keyToUpdate,PREFS_NAME,activity);

                                    }
                                }
                        }else
                            {
                                if((localValue == null || localValue.isEmpty()) && dbValue != null)
                                {
                                    Log.wtf("KEY","2#####");

                                    if(command != null && !keyToUpdate.isEmpty())
                                    {
                                        saveSingleDataValueLocally(dbValue,keyName,PREFS_NAME,activity);
                                        saveSingleDataValueLocally(command,keyToUpdate,PREFS_NAME,activity);

                                    }
                                }
                            }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }



    }


    public String getDataSingleValueLocally(Activity activity, String keyName, String PREFS_NAME)
    {

        if(activity != null)
        {
            // Restore preferences
            settings = activity.getSharedPreferences(PREFS_NAME, 0);

            if(settings != null)
            {
                //if some preference does not exist returns NOT OK - NOK
                String localPropertyValue = settings.getString(keyName, "NOK");

                if(localPropertyValue != null)
                    return  localPropertyValue;
            }
        }

        return "EMPTY";
    }


    public void saveSingleDataValueLocally(String newValue,String keyName, String PREFS_NAME,Activity activity)
    {
        if(activity != null)
        {
            settings = activity.getSharedPreferences(PREFS_NAME, 0);

            if(settings != null)
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(keyName,newValue);
                // Commit the edits!
                editor.commit();
            }
        }

    }



    public String doubleToFormattedCurrencyString(Double value)
    {
        return   String.format(Locale.GERMANY,"%,.2f",value); //New, GERMANY is closest to ANgola using "." eg. 1.000.000,00

        //OLD: return   String.format(Locale.FRANCE,"%,.2f",value); FRANCE uses spaces, eg. 1 000 000, 00.
    }


    public Double stringToDouble(String str)
    {
        if(str != null)
        {
            if(!str.isEmpty())
            {
                return Double.parseDouble(str);
            }
        }

        return 0.0;
    }






}
