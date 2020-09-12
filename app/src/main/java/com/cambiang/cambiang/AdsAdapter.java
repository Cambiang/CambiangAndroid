package com.cambiang.cambiang;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.ViewHolder>
{
    public String cambioType;
    public Spinner  typeCambioSpinner;
    Context mContext;
    Utilities utilities;
    private List<Ad> adList;




    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView adImg;
        public TextView adContent;
        public TextView adTitle;
        public ImageButton adSeeMoreBtn;
        public CardView cardView;
        public LinearLayout   linearLayoutAdText;


        public ViewHolder(View itemView)
        {
            super(itemView);

            adImg = (ImageView) itemView.findViewById(R.id.ad_img);
            adContent = (TextView) itemView.findViewById(R.id.ad_content);
            adTitle = (TextView) itemView.findViewById(R.id.ad_title);
            adSeeMoreBtn = (ImageButton) itemView.findViewById(R.id.ad_see_more_btn);

            cardView = (CardView) itemView.findViewById(R.id.ads_card_view);

            linearLayoutAdText = (LinearLayout) itemView.findViewById(R.id.ad_text_linearLayout);

        }
    }





    public AdsAdapter(List<Ad> adList)
    {
        this.adList = adList;
    }

    @Override
    public AdsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View adListView = inflater.inflate(R.layout.ad_card, parent, false);

        mContext = parent.getContext();


        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(adListView);

        return viewHolder;
    }


    //Populating data into the item view holder

    @Override
    public void onBindViewHolder(AdsAdapter.ViewHolder viewHolder, int position)
    {
        utilities = new Utilities();

        TextView adTitle = viewHolder.adTitle;
        TextView adContentTrimmed = viewHolder.adContent;
        ImageView adImg = viewHolder.adImg;

        LinearLayout linearLayoutAdText = viewHolder.linearLayoutAdText;
        //Get the data based on position
        final Ad ad = this.adList.get(position);



        String cleanImage = ad.getImageURL().replace("data:image/jpeg;base64,","");
        byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        adImg.setImageBitmap(decodedByte);


        if(ad.getImgType().equals("logo"))
        {
            adTitle.setText(ad.getTitle());


            if(ad.getContentText() != null && ad.getContentText().length() > 100 ) // To avoid being throwing errors
            {

                adContentTrimmed.setText(ad.getContentText().substring(0,100)+"..."); //Show only 100 chars.

            }else
            {
                if(ad.getContentText() != null && ad.getContentText().length() > 0 ) // To avoid being throwing errors
                {
                    adContentTrimmed .setText(ad.getContentText().substring(0,ad.getContentText().length()-1)); //Show all chars.

                }

            }
        }else
            {
                adImg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                linearLayoutAdText.setVisibility(View.GONE);
            }



        viewHolder.adSeeMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
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
        });

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
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
        });

    }


    /*
    public void showAd(Ad ad)
    {

        Intent intent;
        intent = new Intent(mContext,AdPostActivity.class);
        intent.putExtra("title",ad.getTitle());
        intent.putExtra("image",ad.getImageURL());
        intent.putExtra("content", ad.getContentText());
        intent.putExtra("contentHTML", ad.getContent());
        intent.putExtra("author", ad.getAuthor());
        intent.putExtra("date", ad.getDate());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);


    }

*/

    @Override
    public int getItemCount()
    {
        return adList.size();
    }


}



