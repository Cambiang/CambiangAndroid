package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cambiang.cambiang.data.Cambio;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Dictionary;

public class Statistics extends AppCompatActivity {

    public Toolbar toolbar;

    Utilities utilities;


    FirebaseDatabase database;
    DatabaseReference ref;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private StatisticsDataAdapter mAdapter;
    private ArrayList<Cambio> cambiosHistoric;
    private ArrayList<Cambio> comercialCambios;
    private ArrayList<StatisticsData> statisticsDataArrayList;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);



        //Set Information Toolbar
        setToolbar();

        utilities = new Utilities();


        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        cambiosHistoric = new ArrayList<Cambio>(0);
        comercialCambios = new ArrayList<Cambio>(0);
        statisticsDataArrayList = new ArrayList<StatisticsData>(0);

        recyclerView = (RecyclerView)  findViewById(R.id.statistics_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);



        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"chasingDots",this,R.id.spin_kit_statistics);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CambiosHistory/");
        //ref2 = database.getReference("CambiosPreviousValues");



        //Initialize and react in case of changes on the Cambio values
         this.manager();


        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "Statistics");
    }




    public void manager()
    {
        /*
        ref.addChildEventListener(
                new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
                    {
                        //Log.d("TAG", "Key:" + dataSnapshot.getKey());
                        //Log.d("TAG", "Value:" + dataSnapshot.getValue());

                        // A new comment has been added, add it to the displayed list

                        try {


                            if(dataSnapshot.getValue(Cambio.class) != null)
                            {
                                Cambio cambio = dataSnapshot.getValue(Cambio.class);

                                if(cambio != null)
                                {
                                    addCambioList(cambio);

                                }

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        // ...
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TAG", "postComments:onCancelled", databaseError.toException());
                        Toast.makeText(mContext, "Failed to load comments.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        */
        // The number of children will always be equal to 'count' since the value of
// the dataSnapshot here will include every child_added event triggered before this point.

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        try {

                            for (DataSnapshot data : dataSnapshot.getChildren())
                            {

                                if(data != null)
                                {
                                    Cambio cambio = data.getValue(Cambio.class);

                                    if(cambio != null)
                                    {
                                        addCambioList(cambio);

                                    }

                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            }
        });


    }


    public void addCambioList(final Cambio cambio)
    {

        //Add Cambio to List
        if(cambio.getBank().equals("BNA"))
        {
            cambio.setBank("Banca Central");

            this.cambiosHistoric.add(cambio);

            this.fillStatisticsData("Banca Central");


        }else
            {
                if(cambio.getBank().equals("KINGUILAS"))
                {
                    cambio.setBank("Mercado Informal");

                    this.cambiosHistoric.add(cambio);

                    this.fillStatisticsData("Mercado Informal");


                }else
                    {
                        cambio.setBank("Banca Comercial");
                        this.cambiosHistoric.add(cambio);

                        this.fillStatisticsData("Banca Comercial");

                    }

            }



        //Order Array CambiosLastUpdate
       // this.OrderCentralComercialInformalBanks();

        if(statisticsDataArrayList != null)
        {
            mAdapter = new StatisticsDataAdapter(statisticsDataArrayList);

            recyclerView.setAdapter(mAdapter);

            utilities.loadingAnimation(View.GONE,"chasingDots",this,R.id.spin_kit_statistics);

            //remove background
            LinearLayout imgBackg = (LinearLayout) findViewById(R.id.mybackg_statistics);
            imgBackg.setVisibility(View.GONE);

        }



    }


    public void fillStatisticsData(String bankName)
    {
        ArrayList<Double> usdDoubs = new ArrayList<Double>(0);
        ArrayList<Double> euroDoubs = new ArrayList<Double>(0);
        StatisticsData statisticsData = new StatisticsData();


        String thisMonth = utilities.getThisMonth();
        String thisYear = utilities.getThisYear();

        Integer thisMonthInt = Integer.parseInt(thisMonth);
        Integer prevYearInt = Integer.parseInt(thisYear) - 1;

        String prevYear = prevYearInt.toString();

        Integer missingMonthsFromPrevYear = 12 - thisMonthInt;
        ArrayList<Integer> missingMonthsFromPrevYearArray = new ArrayList<Integer>(0);

        if(missingMonthsFromPrevYear > 0)
        {
            for(int i = 0; i < missingMonthsFromPrevYear; i++)
            {
                missingMonthsFromPrevYearArray.add(12-i);
            }
        }

      //  Log.wtf("thisMonth: ",thisMonth + " "+"thisYear: " +thisYear + " "+"prevYear: " +prevYear+ " "+thisMonthInt + " "+ missingMonthsFromPrevYear);

        if(cambiosHistoric != null)
            {
                if(cambiosHistoric.size() > 0)
                {

                    for(int i = 0; i < cambiosHistoric.size(); i++)
                    {
                        //Add from this year
                        if(cambiosHistoric.get(i).getBank().equals(bankName))
                        {

                            if(utilities.extractYearFromRefDate(cambiosHistoric.get(i).getRefDate()).equals(thisYear))
                            {
                                usdDoubs.add(Double.parseDouble(cambiosHistoric.get(i).getUsdValue()));
                                euroDoubs.add(Double.parseDouble(cambiosHistoric.get(i).getEuroValue()));
                            }

                        }


                    }


                    if(missingMonthsFromPrevYear > 0)
                    {
                        for(int k = 0; k < missingMonthsFromPrevYearArray.size(); k++)
                        {
                            for(int j = 0; j < cambiosHistoric.size(); j++)
                            {
                                if(cambiosHistoric.get(j).getBank().equals(bankName))
                                {

                                    //Log.wtf("PrevYear",utilities.extractYearFromRefDate(cambiosHistoric.get(j).getRefDate())+ " xxx: "+prevYear);
                                   // Log.wtf("PrevMonthPrevYearM",utilities.extractMonthFromRefDate(cambiosHistoric.get(j).getRefDate()));

                                    //Log.wtf("Month1",utilities.extractMonthFromRefDate(cambiosHistoric.get(j).getRefDate())+ " xxx: "+missingMonthsFromPrevYearArray.get(k));

                                   // if(utilities.extractMonthFromRefDate(cambiosHistoric.get(j).getRefDate()).equals(Integer.toString(missingMonthsFromPrevYearArray.get(k))))
                                     //   Log.wtf("Month2",utilities.extractMonthFromRefDate(cambiosHistoric.get(j).getRefDate())+ " xxx: "+missingMonthsFromPrevYearArray.get(k));

                                    //Add from previous Year
                                    if(utilities.extractYearFromRefDate(cambiosHistoric.get(j).getRefDate()).equals(prevYear) &&
                                            utilities.extractMonthFromRefDate(cambiosHistoric.get(j).getRefDate()).equals(Integer.toString(missingMonthsFromPrevYearArray.get(k))))
                                    {
                                        //Log.wtf("PrevMonthPrevYear2",missingMonthsFromPrevYearArray.get(k).toString());
                                        usdDoubs.add(Double.parseDouble(cambiosHistoric.get(j).getUsdValue()));
                                        euroDoubs.add(Double.parseDouble(cambiosHistoric.get(j).getEuroValue()));
                                    }

                                }

                            }
                        }
                    }






                    if(statisticsData != null)
                    {

                        statisticsData.bankName = bankName;

                        statisticsData.year = utilities.getThisYear();

                        statisticsData.minUSD = Double.toString(utilities.getArrayDoublesMin(usdDoubs));
                        statisticsData.medUSD = Double.toString(utilities.getArrayDoublesMed(usdDoubs));
                        statisticsData.maxUSD = Double.toString(utilities.getArrayDoublesMax(usdDoubs));

                        statisticsData.minEUR = Double.toString(utilities.getArrayDoublesMin(euroDoubs));
                        statisticsData.medEUR = Double.toString(utilities.getArrayDoublesMed(euroDoubs));
                        statisticsData.maxEUR = Double.toString(utilities.getArrayDoublesMax(euroDoubs));

                        this.updateStatisticsList(statisticsData.bankName, statisticsData);
                    }

                }

            }

    }


    public void updateStatisticsList(String bankName, StatisticsData statistics)
    {
        Boolean found = false;
        if(statistics != null)
        {
            for (int i = 0; i < statisticsDataArrayList.size(); i++)
            {
                if (statisticsDataArrayList.get(i).bankName.equals(bankName))
                {
                    statisticsDataArrayList.set(i, statistics);

                    found = true;

                    //break;
                }
            }

            if(!found)
            {
                statisticsDataArrayList.add(statistics);
            }

            this.OrderCentralComercialInformalBanks();
        }
    }




    public void OrderCentralComercialInformalBanks()
    {

        int BNAidx = -1;


        if(this.statisticsDataArrayList!= null)
        {

            if(this.statisticsDataArrayList.size() > 0)
            {
                for(int i = 0; i < this.statisticsDataArrayList.size(); i++)
                {
                    if(this.statisticsDataArrayList.get(i).getBankName().equals("Banca Central"))
                    {
                        BNAidx = i;
                    }
                }

                if(BNAidx != 0 && BNAidx != -1)
                {
                    StatisticsData elementSwapped = new StatisticsData();

                    elementSwapped = this.statisticsDataArrayList.get(0);

                    //Copy BNA to first array position
                    this.statisticsDataArrayList.set(0, this.statisticsDataArrayList.get(BNAidx));

                    //copy element to BNA place
                    this.statisticsDataArrayList.set(BNAidx, elementSwapped);

                }
            }

        }

    }


    @Override
    public void onBackPressed()
    {

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment","0"); // To get back to the caller Fragment
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    //If back arrow is clicked on to go back to previews activity
    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment","0"); // To get back to the caller Fragment
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.statistics_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle("Dados Estatísticos");
        toolbar.setTitleTextColor(Color.WHITE);

    }



}


class StatisticsData
{
    String minUSD;
    String medUSD;
    String maxUSD;
    String minEUR;
    String medEUR;
    String maxEUR;
    String year;
    String bankName;

    public String getMinUSD() {
        return minUSD;
    }

    public void setMinUSD(String minUSD) {
        this.minUSD = minUSD;
    }

    public String getMedUSD() {
        return medUSD;
    }

    public void setMedUSD(String medUSD) {
        this.medUSD = medUSD;
    }

    public String getMaxUSD() {
        return maxUSD;
    }

    public void setMaxUSD(String maxUSD) {
        this.maxUSD = maxUSD;
    }

    public String getMinEUR() {
        return minEUR;
    }

    public void setMinEUR(String minEUR) {
        this.minEUR = minEUR;
    }

    public String getMedEUR() {
        return medEUR;
    }

    public void setMedEUR(String medEUR) {
        this.medEUR = medEUR;
    }

    public String getMaxEUR() {
        return maxEUR;
    }

    public void setMaxEUR(String maxEUR) {
        this.maxEUR = maxEUR;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }




    public StatisticsData()
    {
        minUSD = "0.0";
        medUSD = "0.0";
        maxUSD = "0.0";
        minEUR = "0.0";
        medEUR = "0.0";
        maxEUR = "0.0";
        bankName = "Bank Name";
        year = "0000";

    }

}
