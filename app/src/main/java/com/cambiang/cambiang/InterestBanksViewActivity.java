package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cambiang.cambiang.data.Bank;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterestBanksViewActivity extends AppCompatActivity
{
    public static final String PREFS_NAME = "cambiosLastUpdates";
    public SharedPreferences settings;
    public String ALLOWED_BANKS = "ALLOWED_BANKS";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private InterestBanksViewDataAdapter mAdapter;
    String KEY_EXTRA = "fragment";
    String senderTabPosition = "0";
    Utilities utilities = new Utilities();
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_banks_view);

        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        this.setToolbar();

        settings = getSharedPreferences(PREFS_NAME, 0);

        recyclerView = (RecyclerView) findViewById(R.id.interest_banks_recycler_view);
        recyclerView.setHasFixedSize(true);
        //Layout Manager
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        this.manager();

        if (getIntent().hasExtra(this.KEY_EXTRA))
        {
            this.senderTabPosition = getIntent().getStringExtra(this.KEY_EXTRA);

            //Clear the Intent After use it
            getIntent().removeExtra(this.KEY_EXTRA);
        }
        else
        {
            //throw new IllegalArgumentException("Activity cannot find  extras " + this.KEY_EXTRA);
        }
    }

    public void manager()
    {
        ArrayList<Bank> banksList = this.restoreSharedPreferences(ALLOWED_BANKS);
        ArrayList<Bank> banksAllowedList = new ArrayList<Bank>();

        if(banksAllowedList != null)
        {
            if(banksList.size() > 0)
            {
                for(Bank bank : banksList)
                {
                    if(bank != null)
                    {
                        if(bank.getName() != null)
                        {
                            if(!bank.getName().equals("KINGUILAS") && !bank.getName().equals("BNA") && !bank.getName().equals("-"))
                            {
                                if(!banksAllowedList.contains(bank))
                                {
                                    banksAllowedList.add(bank);
                                }

                            }
                        }

                    }

                }

                mAdapter = new InterestBanksViewDataAdapter(banksAllowedList);
                recyclerView.setAdapter(mAdapter);
            }
        }

    }


    public ArrayList<Bank> restoreSharedPreferences(String dataKey)
    {

        ArrayList<Bank> ret = new ArrayList<>();
        // Restore preferences
        List<Bank> allowedBanksList = new ArrayList<>();


        if(settings != null && dataKey != null)
        {
            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();

            if(settings.getString(dataKey, "NOK") != null)
            {
                String jsonAllowedbanksList = settings.getString(dataKey, "NOK");

                if(!jsonAllowedbanksList.equals("NOK") && !jsonAllowedbanksList.isEmpty())
                {
                    if(Arrays.asList(gson.fromJson(jsonAllowedbanksList, IrtTax[].class)) != null)
                    {
                        allowedBanksList =  Arrays.asList(gson.fromJson(jsonAllowedbanksList, Bank[].class));
                        ret = new ArrayList<>(allowedBanksList);
                    }
                }
            }

        }

        return ret;
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

    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.interest_banks_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.banks_list));
        toolbar.setTitleTextColor(Color.WHITE);

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
