package com.cambiang.cambiang;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.Index;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;


public class DailyCambioFragment extends Fragment {

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

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DataAdapter mAdapter;
    private ArrayList<Cambio> cambiosLastUpdate;
    private ArrayList<Cambio> cambiosPrevious;
    private Context mContext;
    private ArrayList<Bank> banksArray;

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";

    public static final String PREFS_NAME = "cambiosLastUpdates";
    public String ALLOWED_BANKS = "ALLOWED_BANKS";

    Activity mainActivity = getActivity();
    public ArrayList<Ad> adArrayList;
    DatabaseReference refAd;
    SharedPreferences settings;


    public DailyCambioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily_cambio, container, false);

    }


    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onActivityCreated(saveInstanceState);

        mContext = getActivity().getApplicationContext();


        utilities = new Utilities();


        utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

        cambiosLastUpdate = new ArrayList<>(0);
        cambiosPrevious = new ArrayList<Cambio>(0);
        banksArray = new ArrayList<>();
        adArrayList = new ArrayList<Ad>();

        recyclerView = (RecyclerView)  getActivity().findViewById(R.id.cambiang_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        registerForContextMenu(recyclerView);


        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);


        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"chasingDots",getActivity(),R.id.spin_kit);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CambiosLastUpdates");
        ref2 = database.getReference("CambiosPreviousValues");
        ref3 = database.getReference("Banks");
        refAd = database.getReference("Ad");
        //ref2 = database.getReference("CambiosPreviousValues");


            //Initialize and react in case of changes on the Cambio values
            generalManager();

            //Cambio NotificationService
            //notificationIntent = new Intent(getActivity().getApplicationContext(), NotificationService.class);
            //getActivity().startService(notificationIntent);

            //News Notification Service
            //newsNotificationIntent = new Intent(getActivity().getApplicationContext(), NewsNotificationService.class);
            //getActivity().startService(newsNotificationIntent);

            //Send first Analytics event before go to activity
            utilities.sendAnalyticsEvent("Arrival", "CambioView");


       // saveFirebaseToFile();

    }


    public void initDisplay()
    {
        if(cambiosLastUpdate.size() > 0 && banksArray.size() > 0)
        {

            //cambiosLastUpdate, banksArray both arrays come from SharedPreferences stored data
            mAdapter = new DataAdapter(cambiosLastUpdate, banksArray);
            recyclerView.setAdapter(mAdapter);
            utilities.loadingAnimation(View.GONE,"chasingDots",getActivity(),R.id.spin_kit);
            //remove background
            LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackg);
            imgBackg.setVisibility(View.GONE);

        }
    }


    public void generalManager()
    {
        if(restoreSharedPreferences())
        {
            offlineManager();

        }else
        {
            manager();
        }
    }

    public void offlineManager()
    {

        initDisplay();

        //Copy cambiosLastUpdate to cambiosLastUpdateAux
        List<Cambio> cambiosLastUpdateAux = new ArrayList(cambiosLastUpdate);

        getCambiosValuesOffline();

        //Order Array CambiosLastUpdate
        this.OrderCentralComercialInformalBanks();

        //Check for udpated from DB if not just display old values
        //checkCambiosUpdates();

        //Check if dates is changed on pilot Banks then loadData
        checkAndUpdateIfConfirmedNewUpdates("CambiosLastUpdates");
        checkAndUpdateIfConfirmedNewUpdatesInformal("CambiosLastUpdates");


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
                                                    Bank bank = dataSnapshot.getValue(Bank.class);

                                                    if(cambio.getBank().equals(bank.getName()))
                                                    {
                                                        addCambioList(cambio, bank);

                                                        getCambiosValues(cambio);

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

                                                    if(cambio.getBank().equals(bank.getName()))
                                                        updateList(cambio, bank);


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
        int KINGUILASidx = -1;


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

                    if(this.cambiosLastUpdate.get(i).getBank().equals("KINGUILAS"))
                    {
                        KINGUILASidx = i;
                    }
                }



                if(BNAidx != 0 && BNAidx != -1)
                {
                    Cambio elementSwapped = new Cambio();

                    elementSwapped = this.cambiosLastUpdate.get(0);

                    //Copy BNA to the FIRST array position
                    this.cambiosLastUpdate.set(0, this.cambiosLastUpdate.get(BNAidx));

                    //copy element to BNA place
                    this.cambiosLastUpdate.set(BNAidx, elementSwapped);

                }


                if(KINGUILASidx != (this.cambiosLastUpdate.size()-1) && KINGUILASidx != -1)
                {
                    Cambio elementSwappedK = new Cambio();

                    elementSwappedK = this.cambiosLastUpdate.get(this.cambiosLastUpdate.size()-1);

                    //Copy KINGUILAS to the LAST array position
                    this.cambiosLastUpdate.set(this.cambiosLastUpdate.size()-1, this.cambiosLastUpdate.get(KINGUILASidx));

                    //copy element to KINGUILAS' place
                    this.cambiosLastUpdate.set(KINGUILASidx, elementSwappedK);


                    /*
                    for(int i = 0; i < this.cambiosLastUpdate.size(); i++)
                    {

                        Log.wtf("Strange: ",this.cambiosLastUpdate.get(i).getBank() + " ");

                    }*/


                }
            }

        }

    }

    public void OrderBanks()
    {

        int BNAidx = -1;
        int KINGUILASidx = -1;


        if(this.banksArray != null)
        {

            if(this.banksArray.size() > 0)
            {
                for(int i = 0; i < this.banksArray.size(); i++)
                {
                    if(this.banksArray.get(i) != null)
                    {
                        if(this.banksArray.get(i).getName() != null)
                        {
                            if(this.banksArray.get(i).getName().equals("BNA"))
                            {
                                BNAidx = i;
                            }

                            if(this.banksArray.get(i).getName().equals("KINGUILAS"))
                            {
                                KINGUILASidx = i;
                            }
                        }
                    }

                }



                if(BNAidx != 0 && BNAidx != -1)
                {
                    Bank elementSwapped = new Bank();

                    elementSwapped = this.banksArray.get(0);

                    //Copy BNA to the FIRST array position
                    this.banksArray.set(0, this.banksArray.get(BNAidx));

                    //copy element to BNA place
                    this.banksArray.set(BNAidx, elementSwapped);

                }


                if(KINGUILASidx != (this.banksArray.size()-1) && KINGUILASidx != -1)
                {
                    Bank elementSwappedK = new Bank();

                    elementSwappedK = this.banksArray.get(this.banksArray.size()-1);

                    //Copy KINGUILAS to the LAST array position
                    this.banksArray.set(this.banksArray.size()-1, this.banksArray.get(KINGUILASidx));

                    //copy element to KINGUILAS' place
                    this.banksArray.set(KINGUILASidx, elementSwappedK);


                    /*
                    for(int i = 0; i < this.cambiosLastUpdate.size(); i++)
                    {

                        Log.wtf("Strange: ",this.cambiosLastUpdate.get(i).getBank() + " ");

                    }*/


                }
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
                this.banksArray.add(bank);
                this.OrderBanks();

                //Add Cambio to List
                this.cambiosLastUpdate.add(cambio);

                //save cambiosLastUpdate into SharedPreferences
                storeSharedPreferences();

                //Order Array CambiosLastUpdate
                this.OrderCentralComercialInformalBanks();

                mAdapter = new DataAdapter(cambiosLastUpdate, banksArray);

                recyclerView.setAdapter(mAdapter);

                utilities.loadingAnimation(View.GONE,"chasingDots",getActivity(),R.id.spin_kit);

                //remove background
                LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackg);
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

                //save cambiosLastUpdate into SharedPreferences
                storeSharedPreferences();

                //Order Array CambiosLastUpdate
                this.OrderCentralComercialInformalBanks();

                 mAdapter = new DataAdapter(cambiosLastUpdate, banksArray);

                 recyclerView.setAdapter(mAdapter);


                utilities.loadingAnimation(View.GONE,"chasingDots",getActivity(),R.id.spin_kit);
                //remove background
                LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackg);
                imgBackg.setVisibility(View.GONE);

                 break;


            }
        }

    }


    public void loadingAnimation(int visibility)
    {
        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.spin_kit);
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

    public void getCambiosValuesOffline()
    {

        if(cambiosLastUpdate != null)
        {
            if(cambiosLastUpdate.size() > 0)
            {
                for (Cambio cambio : this.cambiosLastUpdate)
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

    //NOT BEING USED ANYMORE
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        /*
        int position = -1;
        try {
            position = mAdapter.getBankClickedPosition();
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId())
        {
            case Utilities.JUROS_ID:

                showJuros(position);

                break;
            case Utilities.INFORMATION_ID:

                sharingCambio(position);

                break;
        }
        */

        return super.onContextItemSelected(item);
    }


    public void showJuros(int position)
    {
        Intent intent;
        intent = new Intent(mContext,JurosActivity.class);

        if(this.cambiosLastUpdate.get(position) != null)
        {
            for (Bank bank : this.banksArray)
            {
                if(bank.getName() != null && this.cambiosLastUpdate.get(position).getBank() != null)
                if(bank.getName().equals(this.cambiosLastUpdate.get(position).getBank()))
                {
                    intent.putExtra("bankName",bank.getName());
                    intent.putExtra("bankFullName",bank.getFullName());
                    intent.putExtra("bankLogo",bank.getLogo());
                    intent.putExtra("bankLogoColor",bank.getLogoColor());

                    intent.putExtra("juros30",bank.getInterestRate30());
                    intent.putExtra("juros90",bank.getInterestRate90());
                    intent.putExtra("juros360",bank.getInterestRate360());
                    intent.putExtra("dateRef",bank.getInterestRateLastUpdateDate());
                }

            }



            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(!this.cambiosLastUpdate.get(position).getBank().equals("BNA") && !this.cambiosLastUpdate.get(position).getBank().equals("KINGUILAS"))
            {
                mContext.startActivity(intent);
            }

        }


    }

    public void sharingCambio(int position)
    {

        if(this.cambiosLastUpdate.size() > 0)
        {
            if(this.cambiosLastUpdate.get(position) != null)
            {
                String bankName = this.cambiosLastUpdate.get(position).getBank();
                String dolar = this.cambiosLastUpdate.get(position).getUsdValue();
                String euro = this.cambiosLastUpdate.get(position).getEuroValue();
                String dateRef = this.cambiosLastUpdate.get(position).getRefDate();
                String bankFullName = "";

                if(this.cambiosLastUpdate.get(position) != null && banksArray != null)
                {
                    if(banksArray.size() > 0)
                    {
                        for (Bank bank : this.banksArray)
                        {
                            if(bank.getName() != null && cambiosLastUpdate.get(position).getBank() != null)
                            if(bank.getName().equals(this.cambiosLastUpdate.get(position).getBank()))
                            {
                                bankFullName = bank.getFullName();
                            }
                        }
                    }


                }

                if(bankName != null && dolar != null && euro != null && dateRef != null && bankFullName != null)
                {

                    if(!bankName.isEmpty() && !dolar.isEmpty() && !euro.isEmpty() && !bankFullName.isEmpty())
                    {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "*"+bankName+"* ("+bankFullName+ ")\n" + "Câmbio aos *" + dateRef + "*\n"+
                                "1 USD : " + "*" + dolar + ",00 AKZ*"+"\n" +
                                "1 EUR : " + "*" + euro + ",00 AKZ*"+"\n" +
                                "\n*Câmbio é no Cambiang*\n"
                                + "*Google Play:* " + "https://play.google.com/store/apps/details?id=com.cambiang.cambiang" + "\n\n"
                                + "*Apple Store:* " + "https://apps.apple.com/us/app/cambiang/id1472229549#?platform=iphone" + "\n\n"
                                + "*Web:* "+ "https://cambiang.com"

                        );

                        sendIntent.setType("text/plain");


                        //Send Analytics event
                        utilities.sendAnalyticsEvent("SharingCambio", "DailyCambio");

                        startActivity(Intent.createChooser(sendIntent,"send"));

                    }

                }

            }
        }

    }

    public void storeSharedPreferences()
    {
        if(cambiosLastUpdate != null)
        {
            if(cambiosLastUpdate.size() > 0 )
            {
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonCambiosLastUdpate = gson.toJson(cambiosLastUpdate);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("cambiosLastUpdate",jsonCambiosLastUdpate);

                    // Commit the edits!
                    //editor.commit();
                    editor.apply();
                }

            }
        }


        if(banksArray != null)
        {
            if(banksArray.size() > 0)
            {
                //Set the values
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonBanksArray = gson.toJson(banksArray);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(ALLOWED_BANKS,jsonBanksArray);

                    // Commit the edits!
                    //editor.commit();
                    editor.apply();

                }

            }
        }



    }

    public void storeSharedPreferencesCambiosList(ArrayList newCambioList)
    {
        if(newCambioList != null)
        {
            if(newCambioList.size() > 0 )
            {
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonCambiosLastUdpate = gson.toJson(newCambioList);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("cambiosLastUpdate",jsonCambiosLastUdpate);

                    // Commit the edits!
                    //editor.commit();
                    editor.apply();
                }


            }
        }

    }

    public boolean restoreSharedPreferences()
    {
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
                        //Log.wtf("cambiosLastUpdate:", Integer.toString(cambiosLastUpdate.size()) + "--"+Integer.toString(banksArray.size()));
                        return true;
                    }
                }
            }

        }

        return false;

    }


    public void checkCambiosUpdates()
    {

        ArrayList<IndexCambioPair>  indexCambioPairArrayList = new ArrayList<IndexCambioPair>();

        //Get All Cambios Last Updates
      ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               for( DataSnapshot data : dataSnapshot.getChildren())
               {
                   if(data != null)
                   {

                       if(data.getValue(Cambio.class) != null)
                       {
                           Cambio cambio = data.getValue(Cambio.class);

                           if(cambiosLastUpdate.size() > 0)
                           {
                               for(int idx = 0; idx < cambiosLastUpdate.size(); idx++)
                               {
                                   if(cambiosLastUpdate.get(idx).getBank().equals(cambio.getBank()))
                                   {
                                        if(cambiosLastUpdate.indexOf(cambio) == -1) //cambio not found meaning it's been changed
                                        {
                                            //save cambio and index to be updated afterwards
                                            if(cambio != null)
                                            {
                                                IndexCambioPair indexCambioPair = new IndexCambioPair(idx,cambio);

                                                if(indexCambioPair != null)
                                                    indexCambioPairArrayList.add(indexCambioPair);
                                               // Log.wtf("index,cambio: ", Integer.toString(idx) + " - BANK: " + cambio.getBank());

                                            }

                                        }
                                   }
                               }

                           }

                       }

                   }
               }


               //If there's not updates, just update the list with same values
               if(indexCambioPairArrayList.size() > 0)
               {
                       //In case of updates, then cambio is updated with change rate also and stored back to Shared preferences
                       ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                           {
                               for(DataSnapshot data : dataSnapshot.getChildren())
                               {
                                   if(data.getValue() != null)
                                   {
                                       Cambio cambioPrev = data.getValue(Cambio.class);

                                       if(indexCambioPairArrayList != null && cambioPrev != null)
                                       {
                                           if(indexCambioPairArrayList.size() > 0)
                                           {
                                               for (IndexCambioPair indexCambioPair : indexCambioPairArrayList)
                                               {
                                                   if(indexCambioPair != null)
                                                   {
                                                       if(indexCambioPair.cambio != null && indexCambioPair.index != null)
                                                       {
                                                           updateWorkerCambio(indexCambioPair.cambio, cambioPrev,indexCambioPair.index);
                                                       }
                                                   }

                                               }
                                           }
                                       }

                                   }
                               }


                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });

                   }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });


    }

    public void updateWorkerCambio(Cambio cambio, Cambio cambioPrevious, int index)
    {

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
                //this.banksArray.add(bank);
                //this.OrderBanks();

                //Add Cambio to List
                this.cambiosLastUpdate.set(index,cambio);

               // Log.wtf("index,cambio: ", Integer.toString(index) + " - BANK: " + cambio.getBank() + "--"+cambio.getUsdValue() + "--" + cambio.getEuroValue());


                //save cambiosLastUpdate into SharedPreferences
                storeSharedPreferences();

                //Order Array CambiosLastUpdate
                this.OrderCentralComercialInformalBanks();

                mAdapter = new DataAdapter(cambiosLastUpdate, banksArray);

                recyclerView.setAdapter(mAdapter);

                //utilities.loadingAnimation(View.GONE,"chasingDots",getActivity(),R.id.spin_kit);

                //remove background
               // LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackg);
                //imgBackg.setVisibility(View.GONE);

            }
        }

    }


    public void banksArraySharedPreferences()
    {
        final boolean[] clearFlag = {false};

        ref3.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.getValue(Bank.class)!=null)
                {
                    Bank bank = dataSnapshot.getValue(Bank.class);

                    if(bank != null)
                    {
                        if(banksArray != null)
                        {
                            if(!clearFlag[0])
                            {
                                banksArray.clear();
                                clearFlag[0] = true;
                            }

                            if (clearFlag[0])
                            {
                                prepareBanksArrayAndSaveIt(bank);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.getValue(Bank.class) != null)
                {


                    if(dataSnapshot.getValue(Bank.class) != null)
                    {
                        Bank bank = dataSnapshot.getValue(Bank.class);

                        if(bank != null)
                        {
                            if(banksArray != null)
                            {
                                for(int idx = 0; idx < banksArray.size(); idx++)
                                {
                                    if(banksArray.get(idx).getName() != null && bank.getName() != null)
                                    if(banksArray.get(idx).getName().equals(bank.getName()))
                                    {
                                        updateBanksArrayAndSaveIt(bank, idx);
                                    }
                                }

                            }
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

/*
        ref3.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getChildren() != null)
                {
                    //clear bank array before copying new data
                    if(!clearFlag[0] && banksArray != null)
                    {
                        banksArray.clear();
                        clearFlag[0] = true;
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        Bank bank = data.getValue(Bank.class);

                        if(bank != null)
                        {
                            prepareBanksArrayAndSaveIt(bank);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        */
    }


    public  void prepareBanksArrayAndSaveIt(Bank bank)
    {
        if(bank != null && banksArray != null)
        {
            try
            {
                banksArray.add(bank);
                saveBanksArray();

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void saveBanksArray()
    {
        if(banksArray != null)
        {
            if(banksArray.size() > 0)
            {
                //Set the values
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonBanksArray = gson.toJson(banksArray);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(ALLOWED_BANKS,jsonBanksArray);

                   // Log.wtf(ALLOWED_BANKS,jsonBanksArray);

                    // Commit the edits!
                   // editor.commit();
                    editor.apply();

                }

            }
        }
    }

    public void updateBanksArrayAndSaveIt(Bank bank, int idx)
    {
        if(bank != null && idx >= 0)
        {
            if(banksArray != null)
            {
                if(banksArray.size() > 0)
                {
                    banksArray.set(idx, bank);
                    saveBanksArray();
                }
            }
        }
    }

    //Method was not completely finished
    public void checkCambiosUpdatesX()
    {

        final boolean[] clearFlag = {false};
        ArrayList<IndexCambioPair>  indexCambioPairArrayList = new ArrayList<IndexCambioPair>();

        //Get All Cambios Last Updates
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                try {

                    if(dataSnapshot.getValue(Cambio.class) != null)
                    {
                        Cambio cambio = dataSnapshot.getValue(Cambio.class);

                        if(cambio != null)
                        {
                            if(banksArray != null)
                            {
                                if(banksArray.size() > 0)
                                {
                                    for (Bank bank : banksArray)
                                    {
                                        if(bank.getName() != null && cambio.getBank() != null)
                                        if(cambio.getBank().equals(bank.getName()))
                                        {
                                            if(!clearFlag[0])
                                            {
                                                cambiosLastUpdate.clear();
                                                clearFlag[0] = true;
                                            }

                                            if(clearFlag[0])
                                            {
                                                addCambioList(cambio, bank);
                                                getCambiosValues(cambio);
                                            }

                                        }
                                    }

                                }
                            }

                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                try {

                    if(dataSnapshot.getValue(Cambio.class) != null)
                    {
                        Cambio cambio = dataSnapshot.getValue(Cambio.class);

                        if(cambio != null)
                        {
                            if(banksArray != null)
                            {
                                if(banksArray.size() > 0)
                                {
                                    for (Bank bank : banksArray)
                                    {
                                        if(bank.getName() != null && cambio.getBank() != null)
                                            if(cambio.getBank().equals(bank.getName()))
                                            {

                                                //UNDER CONSTRUCTION ****************!!!!
                                            }
                                    }

                                }
                            }

                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
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

    public class IndexCambioPair
    {
        Integer index;
        Cambio cambio;

        public IndexCambioPair(Integer index, Cambio cambio)
        {
            this.index = index;
            this.cambio = cambio;
        }
    }


    /*
    public void storeSharedPreferencesAllowedBanksList(ArrayList<Bank> allowedBanksList, String dataKey)
    {
        if(allowedBanksList != null && dataKey != null)
        {
            if(allowedBanksList.size() > 0 )
            {
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonBanks = gson.toJson(allowedBanksList);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(dataKey,jsonBanks);

                    // Commit the edits!
                    //editor.commit();
                    editor.apply();
                }


            }
        }

    }
    */

    public void checkAndUpdateIfConfirmedNewUpdates(String dbPath)
    {
        final Boolean[] isNotSameDate = {false};

        DatabaseReference refUpdates = FirebaseDatabase.getInstance().getReference(dbPath);

        for (String pilotBank : this.utilities.PILOTBANKS)
        {
            //print("pilot banks: \(pilotBank)")
            //observing the data changes

            refUpdates.child(pilotBank).child("refDate").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String refDate = dataSnapshot.getValue(String.class);
                    if(refDate != null)
                    {
                        if (!refDate.equals(getDateOfCambioByBankLocally(pilotBank)))
                        {
                            //Log.wtf("DATE NS",refDate + " -- " + getDateOfCambioByBankLocally(pilotBank));

                            //Just load once, before confirmation isNotSame is put in TRUE
                            if(!isNotSameDate[0])
                            {
                                //Load Cambios
                                loadDataFromDB();

                                //Update banks
                                banksArraySharedPreferences();
                            }

                            isNotSameDate[0] = true;

                        }
                      //  Log.wtf("DATE S",refDate + " -- " + getDateOfCambioByBankLocally(pilotBank));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });


            if (isNotSameDate[0])
            {
               // Log.wtf("DATE NS", "BREAKING LOOP");

                //If is not the same date, meaning update was requested by loadDataFromDB so it can break the loop
                break;
            }

        }
    }

    public void checkAndUpdateIfConfirmedNewUpdatesInformal(String dbPath)
    {
        final Boolean[] isNotSameDate = {false};

        DatabaseReference refUpdates = FirebaseDatabase.getInstance().getReference(dbPath);
        String pilotBank = "KINGUILAS";
            //print("pilot banks: \(pilotBank)")
            //observing the data changes

            refUpdates.child(pilotBank).child("refDate").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String refDate = dataSnapshot.getValue(String.class);
                    if(refDate != null)
                    {
                        if (!refDate.equals(getDateOfCambioByBankLocally(pilotBank)))
                        {
                           // Log.wtf("DATE INFORMAL NS",refDate + " -- " + getDateOfCambioByBankLocally(pilotBank));

                            if(!isNotSameDate[0])
                            {
                                //Load Cambios
                                loadDataFromDB();

                                //Update banks
                                ///banksArraySharedPreferences();
                            }

                            isNotSameDate[0] = true;

                        }
                        //Log.wtf("DATE INFORMAL S",refDate + " -- " + getDateOfCambioByBankLocally(pilotBank));


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });


    }


    public String getDateOfCambioByBankLocally(String bankName)
    {
        String ret = "00-00-0000";

        if(cambiosLastUpdate != null)
        if (cambiosLastUpdate.size() > 0)
        {
            for(Cambio cambio : cambiosLastUpdate)
            {

                if (cambio.getId() != null)
                {
                    if(cambio.getBank().equals(bankName))
                    {
                        ret = cambio.getRefDate();
                    }
                }

            }
        }
        //ret = "00/00/0000"

        return ret;
    }


    public void loadDataFromDB()
    {
        manager();
    }
    
}



