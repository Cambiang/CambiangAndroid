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

import com.cambiang.cambiang.data.Cambio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    public Toolbar toolbar;

    Intent notificationIntent;
    Utilities utilities;

    Integer counter = 0;

    FirebaseDatabase database;
    DatabaseReference ref;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NewsAdapter mAdapter;
    private ArrayList<News> newsArrayList;
    private Context mContext;

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;
    public static final String GOOGLE_TRACK_ID ="UA-132922035-1";


    public static final String  PREFS_NAME_NEWS = "newsUpdates";

    Activity mainActivity = getActivity();
    SharedPreferences settings;


    public NewsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);

    }


    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onActivityCreated(saveInstanceState);

        utilities = new Utilities();


        utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

        newsArrayList = new ArrayList<News>(0);

        recyclerView = (RecyclerView)  getActivity().findViewById(R.id.news_recycler_view);
        recyclerView.setHasFixedSize(true);

        //Layout Manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        // Loading Data, show loading animation while loading...
        utilities.loadingAnimation(View.VISIBLE,"circle",getActivity(),R.id.spin_kit_news);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("News");

        settings = getActivity().getSharedPreferences( PREFS_NAME_NEWS, 0);


            //Initialize and react in case of changes on the News
        generalManager();

       // manager();


        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "NewsView");

    }


    public void generalManager()
    {
        if(restoreSharedPreferences())
        {
            //Check if there is DB update; version 1 (didnt work well)
            //checkNewsUpdatesAndUpdateList();

            //Check if there is DB update;
            checkNewsUpdatesAndUpdateListX();

            //Simply clear existent newsArray and copy a new one from DB; (problems for those news added, notification dont open and app closes
            //because new introduced news is not in the arrayList
            //copyFreshNewsUpdatesAndUpdateList();

            Collections.sort(newsArrayList, new NewsComparator());

            //If not updates display display old news
            mAdapter = new NewsAdapter(newsArrayList);

            recyclerView.setAdapter(mAdapter);

            utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
            //remove background
            LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
            imgBackg.setVisibility(View.GONE);

        }else
            {
                //For the first time load data from DB

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


                            if(dataSnapshot.getValue(News.class) != null)
                            {
                                News news = dataSnapshot.getValue(News.class);

                                if(news != null)
                                {
                                    addNewsList(news);

                                    //Log.wtf("News",news.getTitle());
                                }

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
                    {
                        //Update News with new values

                        try {
                            if(dataSnapshot.getValue(News.class) != null)
                            {
                                News news = dataSnapshot.getValue(News.class);

                                updateList(news);
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


    public void addNewsList(News news)
    {

        if(newsArrayList != null && news != null)
        {
           // Log.wtf("NEWS: ", news.getTitle() + "-- id: "+ news.getId());


            newsArrayList.add(news);

            //Order the arrayList
            Collections.sort(newsArrayList, new NewsComparator());

            storeSharedPreferences();


            mAdapter = new NewsAdapter(newsArrayList);

            recyclerView.setAdapter(mAdapter);

            //remove background

            if((LinearLayout) getActivity().findViewById(R.id.mybackgNews) != null)
            {
                try
                {
                    utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
                    LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
                    imgBackg.setVisibility(View.GONE);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }



        }

    }

    public void addNewsListX(News news)
    {

        if(newsArrayList != null && news != null)
        {
            // Log.wtf("NEWS: ", news.getTitle() + "-- id: "+ news.getId());


            newsArrayList.add(news);

            //Order the arrayList
            Collections.sort(newsArrayList, new NewsComparator());

            storeSharedPreferences();


            mAdapter = new NewsAdapter(newsArrayList);

            recyclerView.setAdapter(mAdapter);

        }

    }


    public void updateList(News news)
    {

        if(news != null && this.newsArrayList != null)
        {

            for(int index = 0; index < this.newsArrayList.size(); index++)
            {
                if(this.newsArrayList.get(index).getId().equals(news.getId()))
                {

                    this.newsArrayList.set(index,news);

                    storeSharedPreferences();


                    //Log.wtf("news Added", news.getTitle());

                    mAdapter = new NewsAdapter(newsArrayList);

                    recyclerView.setAdapter(mAdapter);

                    utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
                    //remove background
                    LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
                    imgBackg.setVisibility(View.GONE);

                    break;

                }
            }

        }

    }



    public class NewsComparator implements Comparator<News>
    {

        @Override
        public int compare(News news1, News news2)
        {

            Date date1 = utilities.convertDateFormat(news1.getDate());
            Date date2 = utilities.convertDateFormat(news2.getDate());

            if(date1 != null && date2 != null)
            {
                return date2.compareTo(date1);

            }

            return 0;
        }
    }

    public void checkNewsUpdatesAndUpdateList()
    {

        ArrayList<IndexNewsPair>  indexNewsPairArrayList = new ArrayList<IndexNewsPair>();

        //Get All Cambios Last Updates
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for( DataSnapshot data : dataSnapshot.getChildren())
                {
                    if(data != null)
                    {

                        if(data.getValue(News.class) != null)
                        {
                            News news = data.getValue(News.class);
                            boolean isfresh = true;

                            if(newsArrayList.size() > 0)
                            {
                                for(int idx = 0; idx < newsArrayList.size(); idx++)
                                {
                                    if(newsArrayList.get(idx).getId().equals(news.getId())) //news exist already
                                    {
                                        isfresh = false;
                                        if(newsArrayList.indexOf(news) == -1) //But news was not found, meaning it's been changed
                                        {
                                            //save news and index to be updated afterwards
                                            if(news != null)
                                            {
                                                IndexNewsPair indexNewsPair = new IndexNewsPair(idx,news);

                                                if(indexNewsPair != null)
                                                    indexNewsPairArrayList.add(indexNewsPair);
                                                // Log.wtf("index,cambio: ", Integer.toString(idx) + " - BANK: " + cambio.getBank());

                                            }

                                        }
                                    }
                                }

                            }

                            //Fresh news
                            if(isfresh)
                            {
                                if(news != null)
                                {

                                    addNewsList(news);
                                }
                            }

                        }

                    }

                }

                if(indexNewsPairArrayList.size() > 0) // there's news modifications
                {
                    for (IndexNewsPair indexNewsPair : indexNewsPairArrayList)
                    {
                        if(indexNewsPair != null)
                        {
                            updateNewsListAndSharedPref(indexNewsPair.news, indexNewsPair.index);
                        }

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void copyFreshNewsUpdatesAndUpdateList()
    {

        ArrayList<News> newsArrayListVienDeArriver = new ArrayList<News>();

        //Get All Cambios Last Updates
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for( DataSnapshot data : dataSnapshot.getChildren())
                {
                    if(data != null)
                    {

                        if(data.getValue(News.class) != null)
                        {
                            News news = data.getValue(News.class);

                            if(news != null)
                            {
                                if(newsArrayListVienDeArriver != null)
                                    newsArrayListVienDeArriver.add(news);
                            }

                        }

                    }
                }

                //Check if any element has deleted on DB and must be locally deleted also


                if(newsArrayList != null && newsArrayListVienDeArriver != null)
                {
                    newsArrayList.clear();

                    for(int idx = 0; idx < newsArrayListVienDeArriver.size(); idx++)
                    {
                        if(newsArrayListVienDeArriver.get(idx) != null)
                        newsArrayList.add(newsArrayListVienDeArriver.get(idx));
                    }
                            /*
                    if(newsArrayList.size() > newsArrayListVienDeArriver.size() && newsArrayListVienDeArriver.size() > 0 && newsArrayList.size() > 0)
                    {
                        //Something have been deleted on DB, local update shall be performed also
                        Log.w("newsArrayList",Integer.toString(newsArrayList.size()));
                        for(int idx = 0; idx < newsArrayList.size(); idx++)
                        {

                            if(newsArrayList.get(idx) != null)
                                if(newsArrayListVienDeArriver.indexOf(newsArrayList.get(idx)) == -1)
                                    newsArrayList.remove(newsArrayList.get(idx));
                        }
                    }else
                        {
                            if(newsArrayList.size() < newsArrayListVienDeArriver.size())
                            {
                                //Something have been added on DB, local update shall be performed also
                                for (News news : newsArrayListVienDeArriver)
                                {
                                    if(news != null)
                                        if(newsArrayList.indexOf(news) == -1)
                                            newsArrayList.add(news);
                                }
                            }

                        }
                        */


                    //Order, Store and update news List
                    if(newsArrayList != null)
                    {
                        if(newsArrayList.size() > 0)
                        {
                            Collections.sort(newsArrayList, new NewsComparator());

                            storeSharedPreferences();

                            mAdapter = new NewsAdapter(newsArrayList);

                            recyclerView.setAdapter(mAdapter);

                            // utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
                            //remove background
                            //LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
                            // imgBackg.setVisibility(View.GONE);
                        }

                    }
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    public void storeSharedPreferences()
    {
        if(newsArrayList != null)
        {
            if(newsArrayList.size() > 0 )
            {
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonNewsLastUdpate = gson.toJson(newsArrayList);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("newsUpdate",""); // flush it first
                    editor.putString("newsUpdate",jsonNewsLastUdpate);

                    // Commit the edits!

                   // apply() changes the in-memory SharedPreferences object immediately but writes the updates to disk asynchronously.
                    //Alternatively, you can use commit() to write the data to disk synchronously.
                      //  But because commit() is synchronous, you should avoid calling it from your main thread because it could
                    //pause your UI rendering.
                    //editor.commit();
                    editor.apply();
                }

            }
        }


    }


    public boolean restoreSharedPreferences()
    {

        List<News> newsList = new ArrayList<>();
        // Restore preferences

        if(settings != null)
        {
            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();
            String jsonNewsUdpate = settings.getString("newsUpdate", "NOK");

            if(!jsonNewsUdpate.equals("NOK") && jsonNewsUdpate != null)
            {
                newsList =  Arrays.asList(gson.fromJson(jsonNewsUdpate, News[].class));

                newsArrayList.clear();
                newsArrayList = new ArrayList<>(newsList);

                if(newsArrayList != null)
                {
                    if(newsArrayList.size() > 0)
                    {
                        // Log.wtf("cambiosLastUpdate:", Integer.toString(cambiosLastUpdate.size()) + "--"+Integer.toString(banksArray.size()));
                        return true;
                    }
                }
            }

        }

        return false;

    }


    public void checkNewsUpdatesAndUpdateListX()
    {
        final Boolean[] clearFlag = {false};

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                News news = dataSnapshot.getValue(News.class);
                if(news != null)
                {
                    if(newsArrayList != null)
                    {

                        if(!clearFlag[0])
                        {
                            //Log.w("CLEAR FLAG", "FALSE");

                            newsArrayList.clear();
                            clearFlag[0] = true;
                        }

                        if (clearFlag[0])
                        {
                            addNewsListX(news);
                            //Log.w("CLEAR FLAG", news.getTitle());
                        }


                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                News news = dataSnapshot.getValue(News.class);
                if(news != null)
                {
                    if(newsArrayList != null)
                    {
                        for(int idx = 0; idx < newsArrayList.size(); idx++)
                        {
                            if(newsArrayList.get(idx).getTitle().equals(news.getTitle()) ||
                                    newsArrayList.get(idx).getId().equals(news.getId()))
                            {

                                updateNewsListAndSharedPref(news, idx);
                            }
                        }

                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                News news = dataSnapshot.getValue(News.class);
                if(news != null)
                {
                    if(newsArrayList != null)
                    {
                        updateNewsListAfterDeleteAndSharedPref(news);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    public int ifIsNewElement(ArrayList<News> arrayList, News element)
    {
        int ret = -1;
        if(arrayList != null && element != null)
        {
            if(arrayList.size() > 0)
            {
                for (News news : arrayList)
                {
                    if(news.getTitle().equals(element.getTitle()) || news.getId().equals(element.getId()))
                    {
                        return 1; //Element exist
                    }
                }

                return 0; // element doesnt exist
            }

        }

        return ret;
    }

    public void updateNewsListAndSharedPref(News news, Integer index)
    {

        if(news != null && index >= 0)
        {

            if(newsArrayList.get(index).getTitle().equals(news.getTitle()) ||
                    newsArrayList.get(index).getId().equals(news.getId()))
            {

               // Log.w("UPDATED", this.newsArrayList.get(index).getTitle());

                this.newsArrayList.set(index,news);

                //Order the arrayList
                Collections.sort(newsArrayList, new NewsComparator());

                //remove duplicates in case exist
                //removeDuplicatesInCaseItExist();

                storeSharedPreferences();

                //Log.wtf("news Added", news.getTitle());

                mAdapter = new NewsAdapter(newsArrayList);

                recyclerView.setAdapter(mAdapter);

                // utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
                //remove background
                //LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
                // imgBackg.setVisibility(View.GONE);

            }

        }

    }


    public void updateNewsListAfterDeleteAndSharedPref(News news)
    {

        if(news != null)
        {

            if(newsArrayList.size() > 0)
            {

                this.newsArrayList.removeIf(mnews -> mnews.getTitle().equals(news.getTitle()) ||mnews.getId().equals(news.getId()));

                if(this.newsArrayList.size() > 0)
                {

                    //remove duplicates in case exist
                    //removeDuplicatesInCaseItExist();
                    //Order the arrayList
                    Collections.sort(newsArrayList, new NewsComparator());


                    storeSharedPreferences();

                    //Log.wtf("news Added", news.getTitle());

                    mAdapter = new NewsAdapter(newsArrayList);

                    recyclerView.setAdapter(mAdapter);

                    // utilities.loadingAnimation(View.GONE,"foldingCube",getActivity(),R.id.spin_kit_news);
                    //remove background
                    //LinearLayout imgBackg = (LinearLayout) getActivity().findViewById(R.id.mybackgNews);
                    // imgBackg.setVisibility(View.GONE);
                }

            }

        }

    }


    public void removeDuplicatesInCaseItExist()
    {


    }


    public class IndexNewsPair
    {
        Integer index;
        News news;

        public IndexNewsPair(Integer index, News news)
        {
            this.index = index;
            this.news = news;
        }
    }


}
