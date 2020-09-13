package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SimulatorActivity extends AppCompatActivity
{

    Toolbar toolbar;
    Utilities utilities = new Utilities();
    String senderTabPosition = "0";
    String KEY_EXTRA = "fragment";
    LinearLayout adLayout;
    String gMobAdsAtWebViewType = "BANNER";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);

        utilities.getDefaultTracker(getApplicationContext()); //Analytics


        //Set Ads Toolbar
        setToolbar();

        if (getIntent().hasExtra(this.KEY_EXTRA))
        {
            this.senderTabPosition = getIntent().getStringExtra(this.KEY_EXTRA);

            //Clear the Intent After use it
            getIntent().removeExtra(this.KEY_EXTRA);
        }

        try
        {
            //init Ad view
            adLayout = new LinearLayout(this.getApplicationContext());
            //Google Ads
            this.manageGooglMobAds();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(this.KEY_EXTRA,this.senderTabPosition);
        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        startActivity(intent);

    }


    //If back arrow is clicked on to go back to previews activity
    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(this.KEY_EXTRA,this.senderTabPosition);
        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.simulator_tool_bar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            toolbar.setTitle(getResources().getString(R.string.simulationOption));
            toolbar.setTitleTextColor(Color.WHITE);
        }


    }

    public void manageGooglMobAds()
    {
        adLayout = utilities.addAds(SimulatorActivity.this, utilities.getTypeOfAd(gMobAdsAtWebViewType), View.VISIBLE,1,0);
    }

    @Override
    protected void onResume() {

        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        super.onResume();
    }

    @Override
    protected void onDestroy() {

        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        super.onDestroy();
    }
}
