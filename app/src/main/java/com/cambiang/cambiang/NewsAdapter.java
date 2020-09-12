package com.cambiang.cambiang;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cambiang.cambiang.data.Cambio;

import java.net.URI;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>
{
    public String cambioType;
    public Spinner  typeCambioSpinner;
    Context mContext;
    Utilities utilities;


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView newsImg;
        public TextView newsContent;
        public TextView newsTitle;
        public ImageButton newsReadMoreBtn;
        public CardView cardView;


        public ViewHolder(View itemView)
        {
            super(itemView);

            newsImg = (ImageView) itemView.findViewById(R.id.news_img);
            newsContent = (TextView) itemView.findViewById(R.id.news_content);
            newsTitle = (TextView) itemView.findViewById(R.id.news_title);
            newsReadMoreBtn = (ImageButton) itemView.findViewById(R.id.news_read_more_btn);

            cardView = (CardView) itemView.findViewById(R.id.news_card_view);


        }
    }


    private List<News> newsList;



    public NewsAdapter(List<News> newsList)
    {
        this.newsList = newsList;
    }

    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View newsListView = inflater.inflate(R.layout.news_card, parent, false);

        mContext = parent.getContext();


        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(newsListView);

        return viewHolder;
    }


    //Populating data into the item view holder

    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder viewHolder, int position)
    {
        utilities = new Utilities();

        //Get the data based on position
        final News news = this.newsList.get(position);

        TextView newsTitle = viewHolder.newsTitle;
        newsTitle.setText(news.getTitle().toUpperCase());

        ImageView newsImg = viewHolder.newsImg;
        String cleanImage = news.getImageURL().replace("data:image/jpeg;base64,","");
        byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        newsImg.setImageBitmap(decodedByte);


        TextView newsContentTrimmed = viewHolder.newsContent;

        if(news.getContentText() != null && news.getContentText().length() > 100 ) // To avoid being throwing errors
        {
            newsContentTrimmed .setText(news.getContentText().substring(0,100)+"..."); //Show only 100 chars.

        }else
            {
                if(news.getContentText() != null && news.getContentText().length() > 0 ) // To avoid being throwing errors
                {
                    newsContentTrimmed .setText(news.getContentText().substring(0,news.getContentText().length()-1)+"..."); //Show all chars.

                }

            }


        viewHolder.newsReadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(news != null)
                {
                    utilities.sendAnalyticsEvent("NewsClicked", news.getTitle());

                    showNews(news);
                }

            }
        });

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(news != null)
                {
                    utilities.sendAnalyticsEvent("NewsClicked", news.getTitle());

                    Log.w("ADAPTER CARD CLICK",news.getTitle());
                    showNews(news);
                }

            }
        });


    }

    public void showNews(News news)
    {

        Intent intent;
        intent = new Intent(mContext,NewsPostActivity.class);

        if(allRequiredProprietiesExist(news))
        {
            intent.putExtra("title",news.getTitle());
            intent.putExtra("image",news.getImageURL());
            intent.putExtra("content", news.getContentText());
            intent.putExtra("contentHTML", news.getContent());
            intent.putExtra("author", news.getAuthor());
            intent.putExtra("date", news.getDate());

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }

    }

    public boolean allRequiredProprietiesExist(News news)
    {
        if(news != null)
        {
            if(news.getTitle() != null && news.getImageURL() != null && news.getContentText() != null && news.getAuthor() != null
                    && news.getDate() != null)
            {
                return true;
            }

            return false;
        }

        return false;

    }


    @Override
    public int getItemCount()
    {
        return newsList.size();
    }


}
