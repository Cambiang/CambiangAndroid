package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class RankingActivity extends AppCompatActivity {


    public Toolbar toolbar;

    Intent notificationIntent;
    Intent newsNotificationIntent;
    Utilities utilities;

    String kingUSD;
    Double comercialBanksAvarageCambioUSD  = 0.0;
    String kingEUR;
    Double comercialBanksAvarageCambioEUR = 0.0 ;
    Integer counter = 0;

    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference ref2;
    DatabaseReference ref3;
    DatabaseReference refAdmin;


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DataRankingAdapter mAdapter;
    private ArrayList<Cambio> cambiosLastUpdate;
    private ArrayList<Cambio> cambiosPrevious;
    private Context mContext;
    private  ArrayList<Bank> banksArray;

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";
    
    public static final String PREFS_NAME = "cambiosLastUpdates";
    public static final String ALLOWED_BANKS = "ALLOWED_BANKS";


    //For GMob Ads
    String gMobAdsAtRankingState = "OFF";
    String gMobAdsAtRankingType = "BANNER";
    LinearLayout adLayout;

    SharedPreferences settings;
    String senderTabPosition = "0";
    String KEY_EXTRA = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        //Displays Toolbar
        this.setToolbar();

        utilities = new Utilities();


        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        //init Ad view
        adLayout = new LinearLayout(this.getApplicationContext());


        cambiosLastUpdate = new ArrayList<Cambio>(0);
        cambiosPrevious = new ArrayList<Cambio>(0);
        banksArray = new ArrayList<Bank>();

        recyclerView = (RecyclerView)  findViewById(R.id.ranking_cambiang_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        settings = getSharedPreferences(PREFS_NAME, 0);


        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"chasingDots",this,R.id.ranking_spin_kit);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CambiosLastUpdates");
        ref2 = database.getReference("CambiosPreviousValues");
        ref3 = database.getReference("Banks");
        refAdmin = database.getReference("Admin");

        //ref2 = database.getReference("CambiosPreviousValues");




        //Initialize and react in case of changes on the Cambio values
        //manager();

        //manage all data, either from DB or from shared Preference
         generalManager();

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("BestCambio", "BestCambioView");

        //show Ads
        this.manageGooglMobAds();


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


    public void generalManager()
    {
        if(restoreSharedPreferences())
        {
            //Log.wtf("Shared Pref","IM HERE MAN");
            //Get all previously stored Cambios and Bank sarrays from Shared Preference
            getCambiosFromSharedPreferences("EUR");
        }else
            {
                //Log.wtf("NOT SP","IM HERE MAN");

                //Probably this methods will never be executed, because at this stage it is expected some stored Cambios on the SharedPreferences
                manager();
            }
    }

    public void manager()
    {

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
                                        final Cambio cambio = dataSnapshot.getValue(Cambio.class);

                                        if(cambio != null)
                                        {
                                            ref3.addChildEventListener(new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                                                {
                                                    try
                                                    {
                                                        if(dataSnapshot.getValue(Bank.class) != null)
                                                        {
                                                            if(dataSnapshot.getValue(Bank.class) != null && cambio.getBank() != null)
                                                            {
                                                                Bank bank = dataSnapshot.getValue(Bank.class);

                                                                if(cambio.getBank() != null && bank.getName() != null)
                                                                {
                                                                    if(!cambio.getBank().equals("BNA")   && !bank.getName().equals("BNA"))
                                                                    {
                                                                        if(cambio.getBank().equals(bank.getName()))
                                                                        {
                                                                            addCambioList(cambio, bank);

                                                                            getCambiosValues(cambio);
                                                                        }

                                                                    }
                                                                }

                                                            }

                                                        }


                                                    }catch (Exception e)
                                                    {
                                                        e.printStackTrace();
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


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                // ...
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
                            {
                                //Update Cambios with new values

                                try {
                                    if(dataSnapshot.getValue(Cambio.class) != null)
                                    {
                                        final Cambio cambio = dataSnapshot.getValue(Cambio.class);


                                        ref3.addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                                            {
                                                try
                                                {
                                                    if(dataSnapshot.getValue(Bank.class) != null)
                                                    {
                                                        Bank bank = dataSnapshot.getValue(Bank.class);
                                                        if(!cambio.getBank().equals("BNA")   && !bank.getName().equals("BNA"))
                                                        {
                                                            if(cambio.getBank().equals(bank.getName()))
                                                                updateList(cambio, bank);
                                                        }



                                                    }


                                                }catch (Exception e)
                                                {
                                                    e.printStackTrace();
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
                                }catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

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



    }

    public void OrderCentralComercialInformalBanks()
    {

        int BNAidx = -1;


        if(this.cambiosLastUpdate != null)
        {

            if(this.cambiosLastUpdate.size() > 0)
            {
                for(int i = 0; i < this.cambiosLastUpdate.size(); i++)
                {
                    if(this.cambiosLastUpdate.get(i).getBank().equals("BNA"))
                    {
                        BNAidx = i;
                    }
                }

                if(BNAidx != 0 && BNAidx != -1)
                {
                    Cambio elementSwapped = new Cambio();

                    elementSwapped = this.cambiosLastUpdate.get(0);

                    //Copy BNA to first array position
                    this.cambiosLastUpdate.set(0, this.cambiosLastUpdate.get(BNAidx));

                    //copy element to BNA place
                    this.cambiosLastUpdate.set(BNAidx, elementSwapped);

                }
            }

        }

    }

    public void sortByLowerCambioEUR()
    {

        if(this.cambiosLastUpdate != null)
        {

            if(this.cambiosLastUpdate.size() > 0)
            {
                Collections.sort(
                    this.cambiosLastUpdate,
                        (cambio1, cambio2) -> Integer.parseInt(cambio1.getEuroValue())  - Integer.parseInt(cambio2.getEuroValue()));
            }
        }

    }

    public void sortByLowerCambioUSD()
    {

        if(this.cambiosLastUpdate != null)
        {
            if(this.cambiosLastUpdate.size() > 0)
            {
                Collections.sort(
                        this.cambiosLastUpdate,
                        (cambio1, cambio2) -> Integer.parseInt(cambio1.getUsdValue())  - Integer.parseInt(cambio2.getUsdValue()));
            }
        }

    }

    public void addCambioList(final Cambio cambio, final Bank bank)
    {
        ref2.addChildEventListener(
                new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
                    {

                        if(cambio != null && bank != null)
                        {

                            try {
                                if(dataSnapshot.getValue(Cambio.class) != null)
                                {
                                    Cambio cambioPrevious = dataSnapshot.getValue(Cambio.class);

                                    if(!cambio.getBank().equals("BNA")   && !bank.getName().equals("BNA"))
                                    addAlsoChangingRate(cambio, cambioPrevious, bank);

                                }
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }


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

    }


    public void addAlsoChangingRate(Cambio cambio, Cambio cambioPrevious, Bank bank)
    {
        // Log.wtf("ChangingRate", cambio.getBank() + "--" + cambioPrevious.getBank());

        if(cambio != null && cambioPrevious != null)
        {
            if(cambio.getBank().equals(cambioPrevious.getBank()))
            {
                Double usdChangeRate = utilities.roundWithDecimalPlaces ((1.0 - Double.parseDouble(cambio.getUsdValue())/Double.parseDouble(cambioPrevious.getUsdValue()))*100,1);
                Double euroChangeRate = utilities.roundWithDecimalPlaces (  (1.0 - Double.parseDouble(cambio.getEuroValue())/Double.parseDouble(cambioPrevious.getEuroValue()))*100,1);

                Double usdChangeRateBuying = utilities.roundWithDecimalPlaces ((-1.0 + Double.parseDouble(cambio.getUsdValueBuying())/Double.parseDouble(cambioPrevious.getUsdValueBuying()))*100,1);
                Double euroChangeRateBuying = utilities.roundWithDecimalPlaces (  (-1.0 + Double.parseDouble(cambio.getEuroValueBuying())/Double.parseDouble(cambioPrevious.getEuroValueBuying()))*100,1);


                cambio.setUsdArrowType(null);
                cambio.setEuroArrowType(null);

                if(usdChangeRate > 0)
                {
                    cambio.setUsdArrowType("down");
                }else
                {
                    if(usdChangeRate < 0)
                    {
                        cambio.setUsdArrowType("up");
                    }else
                    {
                        if(usdChangeRate == 0)
                        {
                            cambio.setUsdArrowType("flat");
                        }
                    }
                }

                if(euroChangeRate > 0)
                {
                    cambio.setEuroArrowType("down");
                }else
                {
                    if(euroChangeRate < 0)
                    {
                        cambio.setEuroArrowType("up");
                    }else
                    {
                        if(euroChangeRate == 0)
                        {
                            cambio.setEuroArrowType("flat");
                        }
                    }
                }

                //Buying Values
                if(usdChangeRateBuying > 0)
                {
                    cambio.setUsdArrowTypeBuying("up");
                }else
                {
                    if(usdChangeRateBuying < 0)
                    {
                        cambio.setUsdArrowTypeBuying("down");
                    }else
                    {
                        if(usdChangeRateBuying == 0)
                        {
                            cambio.setUsdArrowTypeBuying("flat");
                        }
                    }
                }

                if(euroChangeRateBuying > 0)
                {
                    cambio.setEuroArrowTypeBuying("up");
                }else
                {
                    if(euroChangeRateBuying < 0)
                    {
                        cambio.setEuroArrowTypeBuying("down");
                    }else
                    {
                        if(euroChangeRateBuying == 0)
                        {
                            cambio.setEuroArrowTypeBuying("flat");
                        }
                    }
                }

                // use modulus abs to remove minus sign
                usdChangeRate = Math.abs(usdChangeRate);
                euroChangeRate = Math.abs(euroChangeRate);
                usdChangeRateBuying = Math.abs(usdChangeRateBuying);
                euroChangeRateBuying = Math.abs(euroChangeRateBuying);

                String usdChangeRateStr = Double.toString (usdChangeRate) + "%";
                String euroChangeRateStr = Double.toString (euroChangeRate) + "%";
                String usdChangeRateStrBuying = Double.toString (usdChangeRateBuying) + "%";
                String euroChangeRateStrBuying = Double.toString (euroChangeRateBuying) + "%";
                //Log.wtf("ChangingRate", cambio.getBank() + "--" + cambioPrevious.getBank() +">>" + usdChangeRateStr);


                // usdChangeRateStr = usdChangeRateStr.substring(0,3) + "%"; // to limit the charaters
                // euroChangeRateStr = euroChangeRateStr.substring(0,3) + "%"; // to limit the charaters

                // Log.wtf("ChangingRate", cambio.getBank() + "--" + cambioPrevious.getBank() +">><<" + usdChangeRateStr + "--" + euroChangeRateStr);

                cambio.setUsdChangeRate(usdChangeRateStr);
                cambio.setEuroChangeRate(euroChangeRateStr);
                cambio.setUsdChangeRateBuying(usdChangeRateStrBuying);
                cambio.setEuroChangeRateBuying(euroChangeRateStrBuying);

                //Add Previous Cambio
                cambio.setUsdValuePrev(cambioPrevious.getUsdValue());
                cambio.setEuroValuePrev(cambioPrevious.getEuroValue());
                cambio.setUsdValueBuyingPrev(cambioPrevious.getUsdValueBuying());
                cambio.setEuroValueBuyingPrev(cambioPrevious.getEuroValueBuying());

                //Log.wtf("getEuroValuePrev-1",cambioPrevious.getUsdValuePrev());


                // Add bank to List
                if(!bank.getName().equals("BNA"))
                this.banksArray.add(bank);

                //Add Cambio to List
                if(!cambio.getBank().equals("BNA"))
                this.cambiosLastUpdate.add(cambio);

                //Order Array CambiosLastUpdate
                //this.OrderCentralComercialInformalBanks();

                //Sort Cambio by the Lower one
                this.sortByLowerCambioEUR();


                mAdapter = new DataRankingAdapter(cambiosLastUpdate, banksArray);

                recyclerView.setAdapter(mAdapter);

                utilities.loadingAnimation(View.GONE,"chasingDots",this,R.id.ranking_spin_kit);

                //remove background
                LinearLayout imgBackg = (LinearLayout) findViewById(R.id.ranking_mybackg);
                imgBackg.setVisibility(View.GONE);

            }
        }


    }

    public void updateList(final Cambio cambio, Bank bank)
    {
        //Log.d("Hi update:", cambio.getBank());

        if(cambio != null && this.cambiosLastUpdate != null && bank != null && this.banksArray != null )
        {

            updateWorker(cambio, bank);

        }

    }


    public void updateWorker(Cambio cambio, Bank bank)
    {
        // Log.d("Hi update2:", i.toString());

        for(int index = 0; index < this.cambiosLastUpdate.size(); index++)
        {
            if(this.cambiosLastUpdate.get(index).getBank().equals(cambio.getBank()))
            {

                //Before update Cambio, copy it to calculate changeRate
                Cambio cambioPrevious = new Cambio();
                cambioPrevious = this.cambiosLastUpdate.get(index);

                Double usdChangeRate = utilities.roundWithDecimalPlaces ((1.0 - Double.parseDouble(cambio.getUsdValue())/Double.parseDouble(cambioPrevious.getUsdValue()))*100,1);
                Double euroChangeRate = utilities.roundWithDecimalPlaces (  (1.0 - Double.parseDouble(cambio.getEuroValue())/Double.parseDouble(cambioPrevious.getEuroValue()))*100,1);

                Double usdChangeRateBuying = utilities.roundWithDecimalPlaces ((-1.0 + Double.parseDouble(cambio.getUsdValueBuying())/Double.parseDouble(cambioPrevious.getUsdValueBuying()))*100,1);
                Double euroChangeRateBuying = utilities.roundWithDecimalPlaces (  (-1.0 + Double.parseDouble(cambio.getEuroValueBuying())/Double.parseDouble(cambioPrevious.getEuroValueBuying()))*100,1);


                cambio.setUsdArrowType(null);
                cambio.setEuroArrowType(null);

                if(usdChangeRate > 0)
                {
                    cambio.setUsdArrowType("down");
                }else
                {
                    if(usdChangeRate < 0)
                    {
                        cambio.setUsdArrowType("up");
                    }else
                    {
                        if(usdChangeRate == 0)
                        {
                            cambio.setUsdArrowType("flat");
                        }
                    }
                }

                if(euroChangeRate > 0)
                {
                    cambio.setEuroArrowType("down");
                }else
                {
                    if(euroChangeRate < 0)
                    {
                        cambio.setEuroArrowType("up");
                    }else
                    {
                        if(euroChangeRate == 0)
                        {
                            cambio.setEuroArrowType("flat");
                        }
                    }
                }

                //Buying Values
                if(usdChangeRateBuying > 0)
                {
                    cambio.setUsdArrowTypeBuying("up");
                }else
                {
                    if(usdChangeRateBuying < 0)
                    {
                        cambio.setUsdArrowTypeBuying("down");
                    }else
                    {
                        if(usdChangeRateBuying == 0)
                        {
                            cambio.setUsdArrowTypeBuying("flat");
                        }
                    }
                }

                if(euroChangeRateBuying > 0)
                {
                    cambio.setEuroArrowTypeBuying("up");
                }else
                {
                    if(euroChangeRateBuying < 0)
                    {
                        cambio.setEuroArrowTypeBuying("down");
                    }else
                    {
                        if(euroChangeRateBuying == 0)
                        {
                            cambio.setEuroArrowTypeBuying("flat");
                        }
                    }
                }
                // use modulus abs to remove minus sign
                usdChangeRate = Math.abs(usdChangeRate);
                euroChangeRate = Math.abs(euroChangeRate);
                usdChangeRateBuying = Math.abs(usdChangeRateBuying);
                euroChangeRateBuying = Math.abs(euroChangeRateBuying);

                String usdChangeRateStr = Double.toString (usdChangeRate) + "%";
                String euroChangeRateStr = Double.toString (euroChangeRate) + "%";
                String usdChangeRateStrBuying = Double.toString (usdChangeRateBuying) + "%";
                String euroChangeRateStrBuying = Double.toString (euroChangeRateBuying) + "%";
                //Log.wtf("ChangingRate", cambio.getBank() + "--" + cambioPrevious.getBank() +">>" + usdChangeRateStr);


                // usdChangeRateStr = usdChangeRateStr.substring(0,3) + "%"; // to limit the charaters
                // euroChangeRateStr = euroChangeRateStr.substring(0,3) + "%"; // to limit the charaters

                // Log.wtf("ChangingRate", cambio.getBank() + "--" + cambioPrevious.getBank() +">><<" + usdChangeRateStr + "--" + euroChangeRateStr);

                cambio.setUsdChangeRate(usdChangeRateStr);
                cambio.setEuroChangeRate(euroChangeRateStr);
                cambio.setUsdChangeRateBuying(usdChangeRateStrBuying);
                cambio.setEuroChangeRateBuying(euroChangeRateStrBuying);


                //update Previous Cambio
                cambio.setUsdValuePrev(cambioPrevious.getUsdValue());
                cambio.setEuroValuePrev(cambioPrevious.getEuroValue());
                cambio.setUsdValueBuyingPrev(cambioPrevious.getUsdValueBuying());
                cambio.setEuroValueBuyingPrev(cambioPrevious.getEuroValueBuying());



                //Udpate Cambio List
                this.cambiosLastUpdate.set(index, cambio);

                //Order Array CambiosLastUpdate
                //this.OrderCentralComercialInformalBanks();

                //Sort Cambio by the Lower one
                this.sortByLowerCambioEUR();

                mAdapter = new DataRankingAdapter(cambiosLastUpdate, banksArray);

                recyclerView.setAdapter(mAdapter);

                utilities.loadingAnimation(View.GONE,"chasingDots",this,R.id.ranking_spin_kit);
                //remove background
                LinearLayout imgBackg = (LinearLayout) findViewById(R.id.ranking_mybackg);
                imgBackg.setVisibility(View.GONE);

                break;


            }
        }

    }


    public void loadingAnimation(int visibility)
    {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.ranking_spin_kit);
        progressBar.setVisibility(visibility);
        //Circle circle = new Circle();
        //FoldingCube fc = new FoldingCube();
        ChasingDots cd = new ChasingDots();
        progressBar.setIndeterminateDrawable(cd);

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


    public void getCambiosValues(Cambio cambio)
    {

        if(cambio != null)
        {
            if(cambio.getBank().equals("KINGUILAS"))
            {
                kingUSD = cambio.getUsdValue();
                kingEUR = cambio.getEuroValue();
                //Log.d("counter1", counter.toString()+cambio.getBank());

            }else
            {
                if(!cambio.getBank().equals("KINGUILAS") && !cambio.getBank().equals("BNA") && cambio.getUsdValue() != null && cambio.getEuroValue() != null
                        && comercialBanksAvarageCambioUSD  != null && comercialBanksAvarageCambioEUR != null)
                {
                    counter++;
                    comercialBanksAvarageCambioUSD += Double.parseDouble(cambio.getUsdValue());
                    comercialBanksAvarageCambioEUR += Double.parseDouble(cambio.getEuroValue());

                    //comercialBanksAvarageCambioUSD =  Double.parseDouble(cambio.getUsdValue()) + comercialBanksAvarageCambioUSD ;


                    //Log.d("counter3", counter.toString()+cambio.getBank()+cambio.getUsdValue()+comercialBanksAvarageCambioUSD);
                }

            }
        }

    }


    public int getAvaregeComercial()
    {
        int ret = -1;
        if(comercialBanksAvarageCambioUSD != null && comercialBanksAvarageCambioEUR!= null && counter != 0)
        {

            //Log.d("counter3", counter.toString()+comercialBanksAvarageCambioUSD.toString());

            comercialBanksAvarageCambioUSD = comercialBanksAvarageCambioUSD / Double.parseDouble(counter.toString());

            comercialBanksAvarageCambioEUR = comercialBanksAvarageCambioEUR / Double.parseDouble(counter.toString());

            ret = 0;
        }

        return ret;
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


        //Send first Analytics event before go to activity
        //utilities.sendAnalyticsEvent("information", "InfoShown");


        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.ranking_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.best_c_mbio));
        toolbar.setTitleTextColor(Color.WHITE);


    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_best_cambio, menu);

        MenuItem item = (MenuItem) menu.findItem(R.id.switchId);
        item.setActionView(R.layout.toggle_euro_dolar);
        Switch switchAB = item.getActionView().findViewById(R.id.switchAB);

        switchAB.setChecked(false);

        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    //Toast.makeText(getApplication(), "USD", Toast.LENGTH_SHORT).show();
                    //Show best cambio based on DOLAR ORDER
                    if(restoreSharedPreferences())
                    {
                        //Log.wtf("Shared Pref","IM HERE MAN");
                        //Get all previously stored Cambios and Bank sarrays from Shared Preference
                        getCambiosFromSharedPreferences("USD");
                        utilities.sendAnalyticsEvent("Switch", "USD");

                    }
                }
                else
                    {
                        //Toast.makeText(getApplication(), "EUR", Toast.LENGTH_SHORT).show();
                        //Show best cambio based on EUR ORDER
                        if(restoreSharedPreferences())
                        {
                            //Log.wtf("Shared Pref","IM HERE MAN");
                            //Get all previously stored Cambios and Bank sarrays from Shared Preference
                            getCambiosFromSharedPreferences("EUR");
                            utilities.sendAnalyticsEvent("Switch", "EUR");

                        }
                    }
            }
        });

        return true; //super.onCreateOptionsMenu(menu);
    }


    public void manageGooglMobAds()
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtRankingState"))
                    {

                        gMobAdsAtRankingState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtRankingState"))
                        {
                            gMobAdsAtRankingState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtRankingType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtRankingType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtRankingState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(RankingActivity.this, utilities.getTypeOfAd(gMobAdsAtRankingType), View.VISIBLE,1,0);
                    }else
                    {
                        if(gMobAdsAtRankingState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1, adLayout);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                    //Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);
                    //Log.wtf("WTF: "+dataSnapshot.getValue().toString(),dataSnapshot.getKey().toString());
                    //Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtRankingState"))
                    {
                        gMobAdsAtRankingState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtRankingState"))
                        {
                            gMobAdsAtRankingState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtRankingType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtRankingType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtRankingState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(RankingActivity.this,utilities.getTypeOfAd(gMobAdsAtRankingType), View.VISIBLE,2,0);
                        //Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                    {
                        if(gMobAdsAtRankingState.equals("OFF"))
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


    public boolean restoreSharedPreferences()
    {

        //EMPTY FIRST
        this.cambiosLastUpdate = new ArrayList<>();
        this.banksArray = new ArrayList<>();

        // Restore preferences

        List<Cambio> cambiosList = new ArrayList<>();
        List<Bank> banksList = new ArrayList<>();

        if(settings != null)
        {

            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();
            String jsonCambiosLastUdpate = settings.getString("cambiosLastUpdate", "NOK");
            String jsonBanksArray = settings.getString(ALLOWED_BANKS, "NOK");

            if(!jsonCambiosLastUdpate.equals("NOK") && jsonCambiosLastUdpate != null &&
                    !jsonBanksArray.equals("NOK") && jsonBanksArray != null)
            {

                cambiosList =  Arrays.asList(gson.fromJson(jsonCambiosLastUdpate, Cambio[].class));
                banksList = Arrays.asList(gson.fromJson(jsonBanksArray, Bank[].class));

                cambiosLastUpdate = new ArrayList<>(cambiosList);
                banksArray = new ArrayList<>(banksList);

                if(cambiosLastUpdate != null && banksArray != null)
                {
                    if(cambiosLastUpdate.size() > 0 && banksArray.size() > 0)
                    {
                        // Log.wtf("cambiosLastUpdate:", Integer.toString(cambiosLastUpdate.size()) + "--"+Integer.toString(banksArray.size()));
                        return true;
                    }
                }
            }

        }

        return false;

    }


    public void getCambiosFromSharedPreferences(String typeOfCurrency)
    {

            if(this.cambiosLastUpdate != null && this.banksArray != null )
            {

                if(this.cambiosLastUpdate.size() > 0 && this.banksArray.size() > 0)
                {
                    int idxBNA = 0;

                    for (int idx = 0; idx < cambiosLastUpdate.size(); idx++)
                    {
                        if(cambiosLastUpdate.get(idx).getBank().equals("BNA"))
                        {
                            idxBNA = idx;
                        }

                    }

                    cambiosLastUpdate.remove(idxBNA);

                    if(typeOfCurrency.equals("EUR"))
                    {
                        //Sort Cambio by the Lower one
                        this.sortByLowerCambioEUR();
                    }else
                        {
                            if(typeOfCurrency.equals("USD"))
                            {
                                //Sort Cambio by the Lower one
                                this.sortByLowerCambioUSD();
                            }

                        }


                    mAdapter = new DataRankingAdapter(cambiosLastUpdate, banksArray);

                    recyclerView.setAdapter(mAdapter);

                    utilities.loadingAnimation(View.GONE,"chasingDots",this,R.id.ranking_spin_kit);

                    //remove background
                    LinearLayout imgBackg = (LinearLayout) findViewById(R.id.ranking_mybackg);
                    imgBackg.setVisibility(View.GONE);

                }

            }
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

    public void printCambioListValues()
    {
        ArrayList<Bank> auxBanksList = new ArrayList<>();
        if(this.isArrayListGoodToBeUsed(this.cambiosLastUpdate))
        {
            for(Cambio cambio : this.cambiosLastUpdate)
            {
                Log.wtf("USD:",cambio.getUsdValue());
            }

            this.sortByLowerCambioUSD();

            for(Cambio cambio : this.cambiosLastUpdate)
            {
                Log.wtf("USD2:",cambio.getUsdValue());
            }

        }

    }



    public Boolean isArrayListGoodToBeUsed(ArrayList arrayList)
    {
        if(arrayList != null)
        {
            if(arrayList.size() > 0)
            {
                return true;
            }
        }

        return false;
    }

}
