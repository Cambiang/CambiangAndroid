package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class NewsPostActivity extends AppCompatActivity {

    Toolbar toolbar;
    Utilities utilities = new Utilities();
    Context mContext;
    LinearLayout adLayout;
    String gMobAdsAtWebViewType = "BANNER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        setContentView(R.layout.activity_news_post);

        //Set News Post Toolbar
        setToolbar();

        //show News Post
        this.showNewsPost();



        try
        {
            //init Ad view
            adLayout = new LinearLayout(this.getApplicationContext());
            //Google Ads
            this.manageGooglMobAds();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    //If back arrow is clicked on to go back to previews activity
    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment","2"); // To get back to the caller Fragment
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("LEFT", "LEFT");


        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.news_post_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle("News");
        toolbar.setTitleTextColor(Color.WHITE);

    }


    public void showNewsPost()
    {
        //Get news Post data
        String title = getIntent().getStringExtra("title");
        String imageURL = getIntent().getStringExtra("image");
        String content = getIntent().getStringExtra("content");
        String contentHTML = getIntent().getStringExtra("contentHTML");
        String author = getIntent().getStringExtra("author");
        String date = getIntent().getStringExtra("date");

        TextView newsTitle = (TextView) findViewById(R.id.title);
        ImageView newsImage = (ImageView) findViewById(R.id.image);
        TextView newsContent = (TextView) findViewById(R.id.content);
        TextView newsAuthorDate = (TextView) findViewById(R.id.author_date);

        if(content != null && author != null && date != null && title != null && imageURL != null)
        {
            //Log.wtf("title",title);
            //Log.wtf("author", author + " and Date " + date );

            //Title
            newsTitle.setText(title.toUpperCase());


            //Convert image from base64 to bitmap
            String cleanImage = imageURL.replace("data:image/jpeg;base64,","");
            byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            //set image
            newsImage.setImageBitmap(decodedByte);

            //Content
            //newsContent.setText(content);


            //Log.wtf("contentHTML",contentHTML);

            //display HTML in TextView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newsContent.setText(Html.fromHtml(contentHTML, Html.FROM_HTML_MODE_COMPACT));
            } else {
                 newsContent.setText(Html.fromHtml(contentHTML,HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            //Author and Date
            newsAuthorDate.setText(author + " | " + date);


            utilities.sendAnalyticsEvent("ReadingNews", title);




        }

    }

    public void manageGooglMobAds()
    {
        adLayout = utilities.addAds(NewsPostActivity.this, utilities.getTypeOfAd(gMobAdsAtWebViewType), View.VISIBLE,1,0);
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
