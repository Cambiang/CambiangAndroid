package com.cambiang.cambiang;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static com.cambiang.cambiang.AdsFragment.PREFS_NAME;


public class MainActivity extends AppCompatActivity  {


    public Toolbar toolbar;
    private TabLayout tabLayout;
    public ViewPager viewPager;
    Utilities utilities;
    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference refAdmin;
    public boolean showAds = false;
    FloatingActionButton adButton;
    private ArrayList<Ad> adArrayList  = new ArrayList<>();
    private ArrayList<Ad> adArrayListExternal  = new ArrayList<>();
    FirebaseDatabase databaseAds;
    DatabaseReference refAds,refCambiosCambioHouse,refCambioHouse,refPrevCambiosCambioHouse,refBanks;
    Context mContext;
    int counter = 0;
    ComercialAverage comercialAverage = new ComercialAverage();
    String gMobAdsAtConverterState = "OFF";
    String gMobAdsAtConverterType = "BANNER";
    LinearLayout adLayout;
    Integer adsRunningTime = 10; // 10s equivalent
    String adsRunningTimeStr = ""; // 10s equivalent
    public static final String PREFS_NAME = "adsRunningTimeValue";
    public static final String PREFS_NAME_CAMBIO_HOUSE = "cambiosCambioHouseLastUpdates";
    public static final String PREFS_NAME_BANKS = "banks_list";

    public ArrayList<Cambio> cambiosCambiosHouseLastUpdate;
    public ArrayList<Cambio> cambiosCambiosHousePrevious;
    public ArrayList<Bank> cambioHousesArray;
    public ArrayList<Bank> banksArray;
    SharedPreferences settings;
    SharedPreferences settingsCambioHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();


        cambiosCambiosHouseLastUpdate = new ArrayList<Cambio>(0);
        cambiosCambiosHousePrevious = new ArrayList<Cambio>(0);
        cambioHousesArray = new ArrayList<Bank>(0);
        banksArray = new ArrayList<Bank>(0);

        // Write a message to the database
        if(FirebaseDatabase.getInstance() != null)
        {
            database = FirebaseDatabase.getInstance();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Ad");
        refAdmin = database.getReference("Admin");
        refCambiosCambioHouse = database.getReference("CambioHouseLastUpdates");
        refCambioHouse = database.getReference("CambioHouses");
        refPrevCambiosCambioHouse = database.getReference("CambioHousePreviousValues");
        refBanks = database.getReference("Banks");


        settings = getSharedPreferences(PREFS_NAME, 0);
        settingsCambioHouse = getSharedPreferences(PREFS_NAME_CAMBIO_HOUSE, 0);

        this.adButton = findViewById(R.id.adBtn);

        utilities = new Utilities();


        utilities.getDefaultTracker(getApplicationContext()); //Analytics


        adLayout = new LinearLayout(getApplicationContext());

        //Set Toolbar for Main Activity
        setToolbar();


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition())
                {
                    case 0 :

                       // Log.wtf("0", "onTabSelected: 0"); CambioView


                        break;

                    case 2:
                        //Log.wtf("2", "onTabSelected: 2"); Finanças

                        break;

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        //FB NOTIFICATIONS
        firebaseNotificationsListener();
        //Update AdsRunningTime Value
        updateAdRunningTime();

        //ADS
        adsManager();
        getAds();


        //Running Ads
        runningAds();

        //initially to hide Ads button
        this.hideAds();

        //Check if there're some Ads to get displayed
        this.checkAds();


        //Display Ads Activity on Click
        adButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                try
                {
                    displayAdsActivity();

                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        });



        //store cambios cambio house into shared Preferences
       // new ThreadPerTaskExecutor().execute(storeCambioHouseSharedPreferencesTask());

        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        utilities.sendAnalyticsEvent("LEFT", "LEFT");


    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DailyCambioFragment(), getString(R.string.cambioOption));
        adapter.addFragment(new ConverterFragment(), getString(R.string.conversorOption));
        adapter.addFragment(new NewsFragment(), getString(R.string.newsOption));
        adapter.addFragment(new MoreMenuFragment(), getString(R.string.More));
        //adapter.addFragment(new SimulatorFragment(), getString(R.string.simulationOption));
        viewPager.setAdapter(adapter);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

           // Log.wtf("Fragment Position", Integer.toString(position));

            return mFragmentList.get(position);
        }




        @Override
        public int getCount() {
            return mFragmentList.size();
        }


        public void addFragment(Fragment fragment, String title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_info:

                    showInformation();

                return true;

            case R.id.action_statistics:


                runOnUiThread(new Runnable() {

                      @Override
                      public void run()
                      {
                          showStatistics();

                      }
                });



                return true;

            case R.id.action_ranking:

                    showRanking();

                return true;

            case R.id.action_show_ads:

                    displayAdsActivity();

                return true;

            case R.id.action_show_cambio_houses:

                    showCambioHouses();

                return true;




            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }

    }



    //To inflate the MENU; The method should be customized to show the required menu
    //options for this activity
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
       // inflater.inflate(R.menu.menu, menu);

        /*
        runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }




                        //Make Statistics Menu ON or OFF as per Admin Commands
                        manageStatisticsMenuOption(menu);

                        //Make Ranking Menu ON or OFF as per Admin Commands
                        manageRankingMenuOption(menu);
                    }
                });


         */


        return true; //super.onCreateOptionsMenu(menu);
    }


    public void setToolbar()
    {


        toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        // Log.wtf("ELEVATION", Float.toString(getSupportActionBar().getElevation()) );

        toolbar.setTitle("Cambiang");
        toolbar.setTitleTextColor(Color.WHITE);


    }

    public void showCambioHouses()
    {

        Intent intent;
        intent = new Intent(this, CambioHouseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "CambioHouses");
        startActivity(intent);

    }

    public void showInformation()
    {

        Intent intent;
        intent = new Intent(this, Information.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "Information");
        startActivity(intent);

    }

    public void showStatistics()
    {
        Intent intent;
        intent = new Intent(this, Statistics.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        startActivity(intent);

    }

    public void showRanking()
    {
        Intent intent;
        intent = new Intent(this, RankingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

    }


    @Override
    protected void onResume() {
        super.onResume();


        //Log.wtf("Intent2", getIntent().getStringExtra("fragment"));

        if(getIntent().getStringExtra("fragment") != null)
        {
            String fragmentPosition = getIntent().getStringExtra("fragment");
            this.viewPager.setCurrentItem(Integer.parseInt(fragmentPosition));
        }

        utilities.sendAnalyticsEvent("LEFT", "LEFT");

    }

    public void checkAds()
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
                            if(dataSnapshot.getValue(Ad.class) != null)
                            {
                                Ad ad = dataSnapshot.getValue(Ad.class);

                                if(ad != null)
                                {

                                    if(ad.getState().equals("ON"))
                                    {
                                        refAdmin.addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                                                if(dataSnapshot.getValue() != null)
                                                {
                                                    if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("floatingButton"))
                                                    {
                                                        //Confirmed there are some ads to display
                                                        hideAds();
                                                    }else
                                                    {
                                                        if(dataSnapshot.getValue().equals("ON")&& dataSnapshot.getKey().equals("floatingButton"))
                                                        {
                                                            //Confirmed there are some ads to display
                                                            showAds();
                                                        }
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                if(dataSnapshot.getValue() != null)
                                                {
                                                    if(dataSnapshot.getKey().equals("floatingButton"))
                                                    {
                                                        if (dataSnapshot.getValue().equals("OFF"))
                                                        {
                                                            //Confirmed there are some ads to display
                                                            hideAds();

                                                        } else
                                                        {
                                                            //Confirmed there are some ads to display
                                                            showAds();
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

                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
                    {
                        //Update Ad with new values

                        try {
                            if(dataSnapshot.getValue(Ad.class) != null)
                            {
                                Ad ad = dataSnapshot.getValue(Ad.class);

                                if(ad != null)
                                {

                                    if(ad.getState().equals("ON"))
                                    {
                                        refAdmin.addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                                                if(dataSnapshot.getValue() != null)
                                                {
                                                    if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("floatingButton"))
                                                    {
                                                        //Confirmed there are some ads to display
                                                         hideAds();
                                                    }else
                                                    {
                                                        if(dataSnapshot.getValue().equals("ON")&& dataSnapshot.getKey().equals("floatingButton"))
                                                        {
                                                            //Confirmed there are some ads to display
                                                            showAds();
                                                        }
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                if(dataSnapshot.getValue() != null)
                                                {
                                                    if(dataSnapshot.getKey().equals("floatingButton"))
                                                    {
                                                        if (dataSnapshot.getValue().equals("OFF"))
                                                        {
                                                            //Confirmed there are some ads to display
                                                            hideAds();

                                                        } else
                                                            {
                                                                //Confirmed there are some ads to display
                                                                showAds();
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
                    }
                }
        );
    }


    public void hideAds()
    {

        adButton.hide();

    }

    public void showAds()
    {

        adButton.show();

    }


    public void displayAdsActivity()
    {
        Intent intent;
        intent = new Intent(this, AdsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "Ads");

        startActivity(intent);

    }


    public void manageStatisticsMenuOption(final Menu menu)
    {

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(menu != null)
                {
                    //Init hide everything
                    if(Build.VERSION.SDK_INT > 11)
                    {
                        menu.findItem(R.id.action_statistics).setVisible(false);
                    }else
                    {
                        if(Build.VERSION.SDK_INT > 11)
                        {
                            menu.findItem(R.id.action_statistics).setVisible(false);
                        }
                    }
                }

                refAdmin.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        if(dataSnapshot.getValue() != null)
                        {
                            if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("statisticsMenuOption"))
                            {
                                //Log.wtf("MENU: ",dataSnapshot.getKey().toString()+"---"+dataSnapshot.getValue().toString());

                                if(Build.VERSION.SDK_INT > 11)
                                {
                                    menu.findItem(R.id.action_statistics).setVisible(false);
                                }
                            }else
                            {
                                if(dataSnapshot.getValue().equals("ON"))
                                {
                                    if(Build.VERSION.SDK_INT > 11)
                                    {
                                        menu.findItem(R.id.action_statistics).setVisible(true);
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getValue() != null)
                        {
                            if(dataSnapshot.getKey().equals("statisticsMenuOption"))
                            {
                                if (dataSnapshot.getValue().equals("OFF")) {
                                    if (Build.VERSION.SDK_INT > 11) {
                                        menu.findItem(R.id.action_statistics).setVisible(false);
                                    }
                                } else {
                                    if (dataSnapshot.getValue().equals("ON")) {
                                        if (Build.VERSION.SDK_INT > 11) {
                                            menu.findItem(R.id.action_statistics).setVisible(true);
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
            }
        });

    }


    public void manageRankingMenuOption(final Menu menu)
    {


        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                refAdmin.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        if(dataSnapshot.getValue() != null)
                        {
                            if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("rankingMenuOption"))
                            {
                                //Log.wtf("MENU: ",dataSnapshot.getKey().toString()+"---"+dataSnapshot.getValue().toString());

                                if(Build.VERSION.SDK_INT > 11)
                                {
                                    menu.findItem(R.id.action_ranking).setVisible(false);
                                }else
                                {
                                    if(dataSnapshot.getValue().equals("ON"))
                                    {
                                        if(Build.VERSION.SDK_INT > 11)
                                        {
                                            menu.findItem(R.id.action_ranking).setVisible(true);
                                        }
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getValue() != null)
                        {
                            if(dataSnapshot.getKey().equals("rankingMenuOption"))
                            {
                                if(dataSnapshot.getValue().equals("OFF"))
                                {
                                    if(Build.VERSION.SDK_INT > 11)
                                    {
                                        menu.findItem(R.id.action_ranking).setVisible(false);
                                    }
                                }else
                                {
                                    if(dataSnapshot.getValue().equals("ON"))
                                    {
                                        if(Build.VERSION.SDK_INT > 11)
                                        {
                                            menu.findItem(R.id.action_ranking).setVisible(true);
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
            }
        });
    }


    public ArrayList<Ad> getAdsArray()
    {

        return adArrayList;

    }


    public void adsManager()
    {

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                // Write a message to the database
                databaseAds = FirebaseDatabase.getInstance();
                refAds = databaseAds.getReference("Ad");

                refAds.addChildEventListener(
                        new ChildEventListener()
                        {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
                            {
                                //Log.d("TAG", "Key:" + dataSnapshot.getKey());
                                //Log.d("TAG", "Value:" + dataSnapshot.getValue());

                                // A new comment has been added, add it to the displayed list

                                try {


                                    if(dataSnapshot.getValue(Ad.class) != null)
                                    {
                                        Ad ad = dataSnapshot.getValue(Ad.class);

                                        if(ad != null)
                                        {

                                            if(ad.getState().equals("ON"))
                                            {
                                                addAdList(ad);
                                            }

                                            //Log.wtf("Ad",ad.getTitle());
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
                                //Update Ad with new values

                                try {
                                    if(dataSnapshot.getValue(Ad.class) != null)
                                    {
                                        Ad ad = dataSnapshot.getValue(Ad.class);

                                        if(ad.getState().equals("ON"))
                                        {
                                            updateList(ad);
                                        }

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
        });

    }


    public void addAdList(Ad ad)
    {


        if(ad != null && this.adArrayList != null)
        {
            this.adArrayList.add(ad);

            Collections.sort(adArrayList, new MainActivity.AdComparator());


        }

    }


    public void updateList(Ad ad)
    {

        if(ad != null && this.adArrayList != null)
        {

            for(int index = 0; index < this.adArrayList.size(); index++)
            {
                if(this.adArrayList.get(index).getId().equals(ad.getId()))
                {

                    this.adArrayList.set(index,ad);

                    break;

                }
            }

        }

    }


    public class AdComparator implements Comparator<Ad>
    {

        @Override
        public int compare(Ad ad1, Ad ad2)
        {

            Date date1 = utilities.convertDateFormat(ad1.getDate());
            Date date2 = utilities.convertDateFormat(ad2.getDate());

            if(date1 != null && date2 != null)
            {
                return date2.compareTo(date1);

            }

            return 0;
        }
    }


    public void getAds()
    {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();

        Query query = database.getReference("Ad").orderByChild("category").equalTo("external");

        //---------------------INIT-------------------------------------------------------------------------
        if(adArrayListExternal != null)
        {
            if(adArrayListExternal.size() > 0)
            {
                manageGooglMobAds(160);
            }else
            {
                manageGooglMobAds(0);
            }
        }else
        {
            manageGooglMobAds(0);
        }
     //  ----------------------INIT-------------------------------------------------------------------------


        query.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

               // Log.wtf("query2", query.toString());

                if(dataSnapshot.getValue(Ad.class) != null)
                {
                    Ad ad = dataSnapshot.getValue(Ad.class);

                    if(ad.getState().equals("ON"))
                    {
                        adArrayListExternal.add(ad);

                    }
                }


                if(adArrayListExternal != null)
                {
                    if(adArrayListExternal.size() > 0)
                    {
                        manageGooglMobAds(160);
                    }else
                        {
                            manageGooglMobAds(0);
                        }
                }else
                    {
                        manageGooglMobAds(0);
                    }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                if(dataSnapshot.getValue(Ad.class) != null)
                {
                    Ad ad = dataSnapshot.getValue(Ad.class);

                    if(ad.getState().equals("ON"))
                    {

                        if(adArrayListExternal.size() > 0)
                        {

                            for(int index = 0; index < adArrayListExternal.size(); index++)
                            {
                                if(adArrayListExternal.get(index).getId().equals(ad.getId()))
                                {
                                    adArrayListExternal.set(index,ad);

                                }

                                //Remove in case of find any internal
                                if(adArrayListExternal.get(index).getCategory().equals("internal"))
                                {
                                    adArrayListExternal.remove(index);
                                }
                            }
                        }else
                            {
                                adArrayListExternal.add(ad);
                            }


                    }
                }

                if(adArrayListExternal != null)
                {
                    if(adArrayListExternal.size() > 0)
                    {
                        manageGooglMobAds(160);
                    }else
                        {
                            manageGooglMobAds(0);
                        }
                }else
                {
                    manageGooglMobAds(0);
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


    public void showMyAds(Ad ad)
    {
        String cleanImage = ad.getImageURL().replace("data:image/jpeg;base64,","");

        byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


        Drawable img = new BitmapDrawable(mContext.getResources(), decodedByte);

        /*
        if(ad.getTitle().length() > 26)
        {
            String titleTruncated = ad.getTitle().substring(0,25)+"...";
            ad.setTitle(titleTruncated);
        }

         */

        View contextView = findViewById(R.id.snackbar_land_x);
        Snackbar snackbar = Snackbar.make(contextView, ad.getTitle().toUpperCase(), Snackbar.LENGTH_INDEFINITE);
        View snackbarLayout = snackbar.getView();

        //Drawable img = context.getDrawable(R.drawable.bancacomercial);
        // You need to setBounds before setCompoundDrawables , or it couldn't display
        img.setBounds(0, 0, 64, 64);
        TextView textView = (TextView)snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text);
        //textView.setWidth(10);
        //textView.setHeight(10);
        textView.setSingleLine();
        textView.setCompoundDrawablePadding(10);
        //textView.setCompoundDrawablesWithIntrinsicBounds(img, 0, 0, 0);
        textView.setCompoundDrawables(img, null, null, null);


        snackbar.setAction("Ver", new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                redirectAd(ad);
            }
        }).show();
    }

    public void showAdsX(Ad ad)
    {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                LinearLayout linearLayoutLogo  = (LinearLayout) findViewById(R.id.ad_normal);
                LinearLayout linearLayoutBanner  = (LinearLayout) findViewById(R.id.ad_banner);

                String cleanImage = ad.getImageURL().replace("data:image/jpeg;base64,","");

                byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Drawable img = new BitmapDrawable(mContext.getResources(), decodedByte);

                if(ad.getImgType().equals("logo"))
                {
                    linearLayoutBanner.setVisibility(View.GONE);
                    ImageView imgViewAdImage = (ImageView) findViewById(R.id.ad_image);
                    TextView textViewAdTitle = (TextView) findViewById(R.id.ad_title);

                    imgViewAdImage.setImageDrawable(img);
                    textViewAdTitle.setText(ad.getTitle().toUpperCase());

                    linearLayoutLogo.setVisibility(View.VISIBLE);


                }

                if(ad.getImgType().equals("banner"))
                {
                    linearLayoutLogo.setVisibility(View.GONE);
                    ImageView imgViewAdImage = (ImageView) findViewById(R.id.ad_image_banner);

                    imgViewAdImage.setImageDrawable(img);

                    linearLayoutBanner.setVisibility(View.VISIBLE);


                }

                linearLayoutBanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        redirectAd(ad);
                    }
                });

                linearLayoutLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        redirectAd(ad);
                    }
                });

            }


        });

    }


    public void redirectAd(Ad ad)
    {
        if(ad != null)
        {
            utilities.sendAnalyticsEvent("AdClicked", ad.getTitle());

            //  Log.wtf("ad.getHref()",ad.getHref());

            if(ad.getHref()!= null)
            {
                if(ad.getHref().contains("http") || ad.getHref().contains("https"))// missing 'http://' will cause crashed
                {
                    Uri uri = Uri.parse(ad.getHref());


                    Intent intent;
                    //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent = new Intent(mContext,WebviewActivity.class);
                    intent.putExtra("url",uri.toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);



                    try
                    {
                        mContext.getApplicationContext().startActivity(intent);

                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }


                }

            }

        }

    }


    public void runningAds()
    {
        if(restoreSharedPreferences())
        {
            //Run ads with last updated value
            adsTimer();

        }else
            {
                //Run ads with default value of 10 seconds
                adsTimer();
            }
    }

    public void adsTimer()
    {
        //Declare the timer
        Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask()
                              {

                                  @Override
                                  public void run()
                                  {
                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                      if(adArrayListExternal.size() > 0)
                                      {
                                          if(counter < adArrayListExternal.size())
                                          {
                                              Ad ad = adArrayListExternal.get(counter);
                                              if(ad != null)
                                              {
                                                  //showAds(ad);
                                                  showAdsX(ad);
                                              }

                                              counter++;
                                          }else
                                          {
                                              counter = 0;
                                          }



                                      }

                                  }

                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                0,
//Set the amount of time between each execution (in milliseconds)
                adsRunningTime*1000);
    }

    public void manageGooglMobAds(int padding)
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                    {

                        gMobAdsAtConverterState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                        {
                            gMobAdsAtConverterState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtConverterType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtConverterType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtConverterState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getTypeOfAd(gMobAdsAtConverterType)!= null)
                            adLayout = utilities.addAds(MainActivity.this, getTypeOfAd(gMobAdsAtConverterType), View.VISIBLE,1,padding);
                    }else
                    {
                        if(gMobAdsAtConverterState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1, adLayout);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                    // Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);
                    // Log.wtf("WTF: "+dataSnapshot.getValue().toString(),dataSnapshot.getKey().toString());
                    // Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                    {
                        gMobAdsAtConverterState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                        {
                            gMobAdsAtConverterState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtConverterType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtConverterType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtConverterState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getTypeOfAd(gMobAdsAtConverterType)!= null)
                            adLayout = utilities.addAds(MainActivity.this, getTypeOfAd(gMobAdsAtConverterType), View.VISIBLE,1,padding);
                        // Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                    {
                        if(gMobAdsAtConverterState.equals("OFF"))
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

    public void firebaseNotificationsListener()
    {

        //Private (individually if desired by send to a specific token (which represents a user)) or Public messages (sending to all the tokens, iterating with a loop)
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                manageTokens(newToken);
            }
        });

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

    public void manageTokens(final String mToken)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("userTokens");

        final int[] counter = {0};

       ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               for (DataSnapshot data : dataSnapshot.getChildren())
               {
                   String token = data.getValue().toString();
                   if(token.equals(mToken))
                   {
                       counter[0] = counter[0] + 1;
                   }
               }

            if(counter[0] == 0)
            {
                saveRegistrationToken(mToken);
            }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });


    }

    public void updateAdRunningTime()
    {
        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("adsRunningTime"))
                {
                   String adsRunningTimeStr = dataSnapshot.getValue().toString();

                   if(adsRunningTimeStr != null)
                   {
                       storeSharedPreferences(adsRunningTimeStr);

                   }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue() != null)
                    {
                        String adsRunningTimeStr = dataSnapshot.getValue().toString();

                        if(adsRunningTimeStr != null && dataSnapshot.getKey().equals("adsRunningTime"))
                        {
                            storeSharedPreferences(adsRunningTimeStr);
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

    public void storeSharedPreferences(String newValue)
    {
        if(settings != null)
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("adsRunningTimeStr",newValue);
            // Commit the edits!
            editor.commit();
        }

    }


    public boolean restoreSharedPreferences()
    {

        if(settings != null)
        {

            //if some preference does not exist returns NOT OK - NOK
            adsRunningTimeStr = settings.getString("adsRunningTimeStr", "NOK");

            if(adsRunningTimeStr != null)
            {
               // Log.wtf("adsRunningTimeStr",adsRunningTimeStr);

                if(!adsRunningTimeStr.equals("NOK") && !adsRunningTimeStr.isEmpty())
                {
                   // Log.wtf("adsRunningTimeStr",adsRunningTimeStr);
                    adsRunningTime = Integer.parseInt(adsRunningTimeStr);

                    return true;
                }

            }

        }

        return false;

    }


    class ThreadPerTaskExecutor implements Executor
    {
        public void execute(Runnable r)
        {
            new Thread(r).start();
        }
    }


    private Runnable storeCambioHouseSharedPreferencesTask()
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                getCambiosCambioHouses();
               // Log.wtf("cambiosCambiosHouseLastUpdate: ", "NOK");
            }
        };

        return runnable;
    }

    public void storeCambiosCambioHouseSharedPref()
    {
       // Log.wtf("cambiosCambiosHouseLastUpdate: ", "NOK");


        if(cambiosCambiosHouseLastUpdate != null)
        {
            if(cambiosCambiosHouseLastUpdate.size() > 0 )
            {
                //Set the values
                if(settingsCambioHouse != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonCambiosLastUdpate = gson.toJson(cambiosCambiosHouseLastUpdate);

                    SharedPreferences.Editor editor = settingsCambioHouse.edit();
                    editor.putString("cambiosCambiosHouseLastUpdate",jsonCambiosLastUdpate);

                    //Log.wtf("cambiosCambiosHouseLastUpdate: ", jsonCambiosLastUdpate);

                    // Commit the edits!
                    editor.commit();
                }

            }
        }

        if(cambioHousesArray != null)
        {
            if(cambioHousesArray.size() > 0)
            {
                //Set the values
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();


                String jsonBanksArray = gson.toJson(cambioHousesArray);

                SharedPreferences.Editor editor = settingsCambioHouse.edit();
                editor.putString("cambioHousesArray",jsonBanksArray);

                // Commit the edits!
                editor.commit();
            }
        }
    }

    public void getCambiosCambioHouses()
    {
        refCambiosCambioHouse.addChildEventListener(
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
                                    refCambioHouse.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                                        {
                                            try
                                            {
                                                if(dataSnapshot.getValue(Bank.class) != null)
                                                {
                                                    if(dataSnapshot.getValue(Bank.class) != null && cambio.getBank() != null)
                                                    {
                                                        Bank cambioHouse = dataSnapshot.getValue(Bank.class);

                                                        if(cambio.getBank() != null && cambioHouse.getName() != null)
                                                        {

                                                            if(cambio.getBank().equals(cambioHouse.getName()))
                                                            {
                                                                addCambioList(cambio, cambioHouse);

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


    public void addCambioList(final Cambio cambio, final Bank cambioHouse)
    {
        refPrevCambiosCambioHouse.addChildEventListener(
                new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
                    {

                        if(cambio != null && cambioHouse != null)
                        {

                            try {
                                if(dataSnapshot.getValue(Cambio.class) != null)
                                {
                                    Cambio cambioPrevious = dataSnapshot.getValue(Cambio.class);

                                    addAlsoChangingRate(cambio, cambioPrevious, cambioHouse);

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


    public void addAlsoChangingRate(Cambio cambio, Cambio cambioPrevious, Bank cambioHouse)
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

                // Add cambioHouse to List
                this.cambioHousesArray.add(cambioHouse);

                //Add Cambio to List
                this.cambiosCambiosHouseLastUpdate.add(cambio);

                //Sort Cambio by the Lower one
                this.sortByLowerCambio();


                //Store in Shared Pref
                storeCambiosCambioHouseSharedPref();



            }
        }


    }

    public void sortByLowerCambio()
    {

        if(this.cambiosCambiosHouseLastUpdate != null)
        {

            if(this.cambiosCambiosHouseLastUpdate.size() > 0)
            {
                Collections.sort(
                        this.cambiosCambiosHouseLastUpdate,
                        (cambio1, cambio2) -> Integer.parseInt(cambio1.getEuroValue())  - Integer.parseInt(cambio2.getEuroValue()));
            }
        }

    }



}




