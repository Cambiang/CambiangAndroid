package com.cambiang.cambiang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class MoreMenuFragment extends Fragment {
    Intent notificationIntent;
    Utilities utilities;

    Integer counter = 0;

    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference refAdmin;


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MoreMenuDataAdapter mAdapter;
    private ArrayList<MoreMenu> moreMenuArrayList;
    private Context mContext;

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";

    public static final String PREFS_NAME = "SaveToNotification";

    String gMobAdsAtAdsState = "OFF";
    String gMobAdsAtAdsType = "BANNER";
    LinearLayout moreMenuLayout;
    Activity mainActivity = getActivity();



    public MoreMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onActivityCreated(saveInstanceState);

        utilities = new Utilities();


        utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

        moreMenuLayout = new LinearLayout(getActivity().getApplicationContext());


        moreMenuArrayList = new ArrayList<MoreMenu>(0);

        recyclerView = (RecyclerView)  getActivity().findViewById(R.id.more_menu_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);



        //Initialize and react in case of changes on the Ad
        this.manager();


        // notificationIntent = new Intent(getActivity().getApplicationContext(), NotificationService.class);
        //getActivity().startService(notificationIntent);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("MenuMore", "MoreMenuView");


        //utilities.addAds(getActivity(),AdSize.F);
        this.manageGooglMobAds();
    }


    public void manager()
    {
        this.setupMoreMenuArrayList();
        if(this.moreMenuArrayList != null)
        {
            mAdapter = new MoreMenuDataAdapter(this.moreMenuArrayList);

            recyclerView.setAdapter(mAdapter);

        }
    }


    public void manageGooglMobAds()
    {

        //moreMenuLayout = utilities.addAds(getActivity(), utilities.getTypeOfAd(gMobAdsAtAdsType), View.VISIBLE,1,0);
    }

    public void setupMoreMenuArrayList()
    {

        MoreMenu moreMenu0 = new MoreMenu();
        moreMenu0.setTitle(getResources().getString(R.string.anuncios));
        moreMenu0.setImage(R.drawable.book);

        MoreMenu moreMenu1 = new MoreMenu();
        moreMenu1.setTitle(getResources().getString(R.string.best_c_mbio));
        moreMenu1.setImage(R.drawable.trophy);

        MoreMenu moreMenu2 = new MoreMenu();
        moreMenu2.setTitle(getResources().getString(R.string.casas_de_c_mbio));
        moreMenu2.setImage(R.drawable.cambio_house);

        MoreMenu moreMenu3 = new MoreMenu();
        moreMenu3.setTitle(getResources().getString(R.string.simulationOption));
        moreMenu3.setImage(R.drawable.simulator_i);

        MoreMenu moreMenu4 = new MoreMenu();
        moreMenu4.setTitle(getResources().getString(R.string.irt_calculator));
        moreMenu4.setImage(R.drawable.irt);

        MoreMenu moreMenu5 = new MoreMenu();
        moreMenu5.setTitle(getResources().getString(R.string.taxa_juros));
        moreMenu5.setImage(R.drawable.tax);

        MoreMenu moreMenu6 = new MoreMenu();
        moreMenu6.setTitle(getResources().getString(R.string.sobre_a_app));
        moreMenu6.setImage(R.drawable.about_app);

        this.moreMenuArrayList.add(moreMenu0);
        this.moreMenuArrayList.add(moreMenu1);
        this.moreMenuArrayList.add(moreMenu2);
        this.moreMenuArrayList.add(moreMenu3);
        this.moreMenuArrayList.add(moreMenu4);
        this.moreMenuArrayList.add(moreMenu5);
        this.moreMenuArrayList.add(moreMenu6);

    }
}
