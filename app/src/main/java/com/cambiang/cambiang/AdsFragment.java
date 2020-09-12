package com.cambiang.cambiang;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

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
        utilities.loadingAnimation(View.VISIBLE,"circle",getActivity(),R.id.spin_kit_ads);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Ad");
        refAdmin = database.getReference("Admin");




        //Initialize and react in case of changes on the Ad
        manager();


        // notificationIntent = new Intent(getActivity().getApplicationContext(), NotificationService.class);
        //getActivity().startService(notificationIntent);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "AdView");


        //utilities.addAds(getActivity(),AdSize.F);
        this.manageGooglMobAds();
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



 }
