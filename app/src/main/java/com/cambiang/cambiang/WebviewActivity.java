package com.cambiang.cambiang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;


import android.os.Bundle;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WebviewActivity extends AppCompatActivity {

    public Toolbar toolbar;
    WebView myWebView;
    Utilities utilities;
    DatabaseReference refAdmin;
    FirebaseDatabase database;
    //For GMob Ads
    String gMobAdsAtWebViewState = "OFF";
    String gMobAdsAtWebViewType = "BANNER";
    LinearLayout adLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        //Displays Toolbar
        this.setToolbar();

        utilities = new Utilities();
        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        database = FirebaseDatabase.getInstance();
        refAdmin = database.getReference("Admin");

        //init Ad view
        adLayout = new LinearLayout(this.getApplicationContext());

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyWebViewClient()
        {

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                utilities.loadingAnimation(View.GONE,"chasingDots",WebviewActivity.this,R.id.webview_spin_kit);

                //remove background
                LinearLayout imgBackg = (LinearLayout) findViewById(R.id.webview_mybackg);
                imgBackg.setVisibility(View.GONE);
            }

        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"chasingDots",this,R.id.webview_spin_kit);

        //Log.wtf("url: ", "brand n url");

        if(getIntent().getStringExtra("url") != null)
        {
            String url = getIntent().getStringExtra("url");

           try {
               myWebView.loadUrl(url);

           }catch (Exception e)
           {
           }


           // Log.wtf("url: ", url);

        }


        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "WebvieView");


        //show Ads
        this.manageGooglMobAds();

    }



    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.webview_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle("Anúncios Cambiang");
        toolbar.setTitleTextColor(Color.WHITE);


    }

    @Override
    public void onBackPressed() {

        Intent intent;
        intent = new Intent(this, AdsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    //If back arrow is clicked on to go back to previews activity
    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent;
        intent = new Intent(this, AdsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Send first Analytics event before go to activity
        //utilities.sendAnalyticsEvent("information", "InfoShown");


        startActivity(intent);

        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }





    /*
    When the user clicks a link from a web page in your WebView, the default behavior is for Android to launch an app that handles URLs.
    Usually, the default web browser opens and loads the destination URL.
    However, you can override this behavior for your WebView, so links open within your WebView.
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            /*if ("www.example.com".equals(Uri.parse(url).getHost())) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }*/
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            return false;
        }


    }


    public void manageGooglMobAds()
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtWebViewState"))
                    {

                        gMobAdsAtWebViewState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtWebViewState"))
                        {
                            gMobAdsAtWebViewState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtWebViewType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtWebViewType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtWebViewState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(WebviewActivity.this, utilities.getTypeOfAd(gMobAdsAtWebViewType), View.VISIBLE,1,0);
                    }else
                    {
                        if(gMobAdsAtWebViewState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1, adLayout);
                            adLayout.setVisibility(View.GONE);
                        }
                    }


                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtWebViewState"))
                    {
                        gMobAdsAtWebViewState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtWebViewState"))
                        {
                            gMobAdsAtWebViewState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtWebViewType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtWebViewType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtWebViewState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(WebviewActivity.this,utilities.getTypeOfAd(gMobAdsAtWebViewType), View.VISIBLE,2,0);
                        //Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                    {
                        if(gMobAdsAtWebViewState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1,adLayout);
                            adLayout.setVisibility(View.GONE);
                            //Log.wtf("OFF: ", "Im GONE!!");

                        }
                    }

                }


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
