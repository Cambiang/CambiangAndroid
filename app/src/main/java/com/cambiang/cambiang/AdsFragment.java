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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdsFragment extends Fragment {
    public Toolbar toolbar;

    Intent notificationIntent;
    Utilities utilities;

    Integer counter = 0;

    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference refAdmin;


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdsAdapter mAdapter;
    private ArrayList<Ad> adArrayList;
    private Context mContext;

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";

    public static final String PREFS_NAME = "SaveToNotification";
    public static final String LOCAL_ADS_KEY = "LOCAL_ADS_KEY";

    String gMobAdsAtAdsState = "OFF";
    String gMobAdsAtAdsType = "BANNER";
    LinearLayout adLayout;



    Activity mainActivity = getActivity();




    public AdsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ads, container, false);
    }


    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onActivityCreated(saveInstanceState);

        utilities = new Utilities();


        utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

        adLayout = new LinearLayout(getActivity().getApplicationContext());


        adArrayList = new ArrayList<Ad>(0);

        recyclerView = (RecyclerView)  getActivity().findViewById(R.id.ads_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"foldingCube",getActivity(),R.id.spin_kit_ads);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Ad");
        refAdmin = database.getReference("Admin");


        //Initialize and react in case of changes on the Ad
        loadData();

        //Flush and reload all Data
        flushAndReloadAllAds();

        // notificationIntent = new Intent(getActivity().getApplicationContext(), NotificationService.class);
        //getActivity().startService(notificationIntent);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "AdView");


        //utilities.addAds(getActivity(),AdSize.F);
        this.manageGooglMobAds();
    }


    public void addAdList(Ad ad)
    {

        if(ad != null && this.adArrayList != null)
        {
            this.adArrayList.add(ad);

            Collections.sort(adArrayList, new AdsFragment.AdComparator());


            mAdapter = new AdsAdapter(adArrayList);

            recyclerView.setAdapter(mAdapter);

            utilities.loadingAnimation(View.GONE, "foldingCube", getActivity(), R.id.spin_kit_ads);
            //remove background
            LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgAd);
            imgBackg.setVisibility(View.GONE);

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

                    //Log.wtf("ad Added", ad.getTitle());

                    mAdapter = new AdsAdapter(adArrayList);

                    recyclerView.setAdapter(mAdapter);

                    utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_ads);
                    //remove background
                    LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgAd);
                    imgBackg.setVisibility(View.GONE);

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


    public void manageGooglMobAds()
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtAdsState"))
                    {

                        gMobAdsAtAdsState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtAdsState"))
                        {
                            gMobAdsAtAdsState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtAdsType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtAdsType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtAdsState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(getActivity(), utilities.getTypeOfAd(gMobAdsAtAdsType), View.VISIBLE,1,0);
                    }else
                    {
                        if(gMobAdsAtAdsState.equals("OFF"))
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
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtAdsState"))
                    {
                        gMobAdsAtAdsState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtAdsState"))
                        {
                            gMobAdsAtAdsState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtAdsType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtAdsType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtAdsState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);
                        adLayout = utilities.addAds(getActivity(), utilities.getTypeOfAd(gMobAdsAtAdsType), View.VISIBLE,2,0);
                        //Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                    {
                        if(gMobAdsAtAdsState.equals("OFF"))
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

    public void onDataAddedUpdateList()
    {
        ref.limitToLast(3).addChildEventListener(
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
                                        if(!isAdLocallySaved(ad)) //ONLY SAVE IF AD WAS NOT ALREADY SAVED
                                        {
                                            updateList(ad); //Update list with the last 3 Ads added; IN case the user
                                            //have been a while without opening the App
                                            saveAdsLocally();
                                        }
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
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TAG", "postComments:onCancelled", databaseError.toException());
                        Toast.makeText(mContext, "Failed to load comments.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void onDataChangedUpdateList()
    {
        ref.addChildEventListener(
                new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}

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
                                    if(isAdLocallySaved(ad)) //ONLY SAVE IF AD WAS NOT ALREADY SAVED
                                    {
                                        updateList(ad);
                                        saveAdsLocally();

                                        //Log.wtf("LIST U",ad.getTitle());
                                    }else
                                        {
                                           // Log.wtf("LIST A",ad.getTitle());

                                            addAdList(ad);
                                            saveAdsLocally();
                                        }

                                }else
                                    {
                                        if(isAdLocallySaved(ad)) //ONLY SAVE IF AD WAS NOT ALREADY SAVED
                                        {
                                            //Log.wtf("LIST R",ad.getTitle());

                                            removeAdLocally(ad);
                                            updateScreen();
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
                        Toast.makeText(mContext, "Failed to load comments.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void onDataRemovedUpdateList()
    {
        ref.addChildEventListener(
                new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {
                        try {
                            if(dataSnapshot.getValue(Ad.class) != null)
                            {
                                Ad ad = dataSnapshot.getValue(Ad.class);

                               if(isAdLocallySaved(ad))
                                {
                                    removeAdLocally(ad);
                                    updateScreen();

                                }
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

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

    public void flushAndReloadAllAds()
    {

        refAdmin.child("reloadAllAds").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getValue() != null)
                {
                    String cmd = dataSnapshot.getValue().toString();

                    if (cmd.equals("ON"))
                    {
                        if(adArrayList != null)
                        {
                            if(adArrayList.size()>0)
                            {
                                adArrayList.clear();
                            }
                        }else
                            {
                                adArrayList = new ArrayList<>();
                            }
                       // Log.wtf("COMMAND","ON");
                        loadDataFromDB();
                    }else
                    {
                        //AUTO MODE

                        if(!utilities.isTheSameMonth("MONTH_FOR_ADS_RELOAD",getActivity()))
                        {
                            if(adArrayList != null)
                            {
                                if(adArrayList.size()>0)
                                {
                                    adArrayList.clear();
                                }
                            }else
                            {
                                adArrayList = new ArrayList<>();
                            }

                            loadDataFromDB();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void saveAdsLocally()
    {
        if(adArrayList != null)
        {
            if(adArrayList.size() > 0 )
            {
                SharedPreferences settings = getActivity().getSharedPreferences( this.LOCAL_ADS_KEY, 0);

                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonAdsLastUdpate = gson.toJson(adArrayList);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(this.LOCAL_ADS_KEY,""); // flush it first
                    editor.putString(this.LOCAL_ADS_KEY,jsonAdsLastUdpate);
                    editor.apply();


                    // Commit the edits!

                    // apply() changes the in-memory SharedPreferences object immediately but writes the updates to disk asynchronously.
                    //Alternatively, you can use commit() to write the data to disk synchronously.
                    //  But because commit() is synchronous, you should avoid calling it from your main thread because it could
                    //pause your UI rendering.
                    //editor.commit();
                }

            }
        }


    }

    public boolean readAdsLocally()
    {
        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"foldingCube",getActivity(),R.id.spin_kit_ads);

        List<Ad> adsList = new ArrayList<>();

        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences( this.LOCAL_ADS_KEY, 0);

        if(settings != null)
        {
            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();
            String jsonAdsUdpate = settings.getString(this.LOCAL_ADS_KEY, "NOK");

            if(!jsonAdsUdpate.equals("NOK") && jsonAdsUdpate != null)
            {
                adsList =  Arrays.asList(gson.fromJson(jsonAdsUdpate, Ad[].class));

                adArrayList.clear();
                adArrayList = new ArrayList<>(adsList);

                if(adArrayList != null)
                {
                    if(adArrayList.size() > 0)
                    {
                        return true;
                    }
                }
            }

        }

        return false;

    }

    public void removeAdLocally(Ad adToRemove)
    {
        if(adToRemove != null)
        {
            if(isAdLocallySaved(adToRemove))
            {
                int idx = getAdIndex(adToRemove);
                if(idx >= 0)
                {
                    adArrayList.remove(idx);
                    this.saveAdsLocally();

                }

            }
        }
    }

    public boolean isAdLocallySaved(Ad ad)
    {
        if(ad != null && adArrayList.size() > 0)
        {
            for (Ad mAd : adArrayList)
            {
                if(mAd != null)
                {
                    if(ad.getTitle().equals(mAd.getTitle()) && ad.getId().equals(mAd.getId()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int getAdIndex(Ad ad)
    {

        if(ad != null && adArrayList.size() > 0)
        {
            for(int idx = 0; idx < adArrayList.size(); idx++)
            {
                if(adArrayList.get(idx).getId().equals(ad.getId()))
                {
                    return idx;
                }
            }

        }

        return -1;
    }

    public void updateScreen()
    {

            Collections.sort(adArrayList, new AdsFragment.AdComparator());

            mAdapter = new AdsAdapter(adArrayList);

            recyclerView.setAdapter(mAdapter);


        utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_ads);
        //remove background
        LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgAd);
        imgBackg.setVisibility(View.GONE);

    }

    public void loadData()
    {
        if(!readAdsLocally())
        {
            loadDataFromDB();

           // Log.wtf("LOAD","CLOUDY");
        }


        //Data Listners for change
        onDataAddedUpdateList();
        onDataChangedUpdateList();
        onDataRemovedUpdateList();

       // Log.wtf("LOAD","LOCALLY");

        updateScreen();

    }

    public void loadDataFromDB()
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
                                        addAdList(ad);
                                        saveAdsLocally();
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

 }
