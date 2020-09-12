package com.cambiang.cambiang;

import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

public class Information extends AppCompatActivity {

    Toolbar toolbar;
    Utilities utilities = new Utilities();
    String KEY_EXTRA = "fragment";
    String senderTabPosition = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        setContentView(R.layout.activity_information);

        //Set Information Toolbar
        setToolbar();

        //Show information
        information();

        utilities.sendAnalyticsEvent("AboutApp", "AboutApp");


        if (getIntent().hasExtra(this.KEY_EXTRA))
        {
            this.senderTabPosition = getIntent().getStringExtra(this.KEY_EXTRA);
        }
        else
        {
            //throw new IllegalArgumentException("Activity cannot find  extras " + this.KEY_EXTRA);
        }


    }




    @Override
    public void onBackPressed() {

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(this.KEY_EXTRA,this.senderTabPosition);
        //Send first Analytics event before go to activity
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
        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        //Send first Analytics event before go to activity
        //utilities.sendAnalyticsEvent("information", "InfoShown");


        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.info_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.information));
        toolbar.setTitleTextColor(Color.WHITE);

    }


    public void information()
    {
        TextView infoText = (TextView) findViewById(R.id.info_text);
        TextView email = (TextView) findViewById(R.id.email);

        TextView website = (TextView) findViewById(R.id.website);

        infoText.setText(R.string.information_text);

        email.setText(getString(R.string.information_email));
        website.setText(getString(R.string.information_website));
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
