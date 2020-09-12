package com.cambiang.cambiang;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoreMenuDataAdapter extends RecyclerView.Adapter<MoreMenuDataAdapter.ViewHolder> {

    Context mContext;
    Utilities utilities;
    private List<MoreMenu> moreMenuList;
    String KEY_EXTRA = "fragment";
    String tabPosition = "3";

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView moreMenuImage;
        public TextView moreMenuTitle;
        public CardView cardView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            this.moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_img);
            this.moreMenuTitle = (TextView) itemView.findViewById(R.id.more_menu_title);
            this.cardView = (CardView) itemView.findViewById(R.id.more_menu_card_view);
        }
    }

    //This constructor is used on the activity to load data into Adapter
    public MoreMenuDataAdapter(List<MoreMenu> moreMenuList)
    {
        this.moreMenuList = moreMenuList;

        Log.wtf("ADAPTER", "MoreMenuDataAdapter");

    }


    @Override
    public MoreMenuDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View moreMenuListView = inflater.inflate(R.layout.more_menu_card, parent, false);

        this.mContext = parent.getContext();

        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(moreMenuListView);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return this.moreMenuList.size();
    }

    @Override
    public void onBindViewHolder(MoreMenuDataAdapter.ViewHolder holder, int position)
    {
        holder.moreMenuImage.setImageResource(this.moreMenuList.get(position).getImage());
        holder.moreMenuTitle.setText(moreMenuList.get(position).getTitle());

        String chosenMoreMenuTitle = moreMenuList.get(position).getTitle();

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                holder.cardView.setCardBackgroundColor(Color.rgb(240,255,255)
                );
                menuCommuter(chosenMoreMenuTitle);
            }
        });


    }

    public void menuCommuter(String menuTitle)
    {
        if(!menuTitle.isEmpty())
        {
            if (this.mContext.getResources().getString(R.string.anuncios).equals(menuTitle))
            {
                this.displayAdsActivity();
            }
            if (this.mContext.getResources().getString(R.string.best_c_mbio).equals(menuTitle))
            {
                this.showRanking();
            }
            if (this.mContext.getResources().getString(R.string.casas_de_c_mbio).equals(menuTitle))
            {
                this.showCambioHouse();
            }
            if (this.mContext.getResources().getString(R.string.simulationOption).equals(menuTitle))
            {
                this.showSimulator();
            }
            if (this.mContext.getResources().getString(R.string.irt_calculator).equals(menuTitle))
            {
                this.showIrtCalculator();
            }
            if (this.mContext.getResources().getString(R.string.taxa_juros).equals(menuTitle))
            {
                this.showBankInterest();
            }
            if (this.mContext.getResources().getString(R.string.sobre_a_app).equals(menuTitle))
            {
                this.showInformation();
            }


        }
    }







    public void displayAdsActivity()
    {


        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), AdsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);
    }

    public void showRanking()
    {
        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), RankingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);

    }

    public void showCambioHouse()
    {
        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), CambioHouseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);

    }

    public void showSimulator()
    {
        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), SimulatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);

    }

    public void showIrtCalculator()
    {
        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), IrtCalculatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);

    }

    public void showBankInterest()
    {
        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), InterestBanksViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);

    }

    public void showInformation()
    {

        Intent intent;
        intent = new Intent(mContext.getApplicationContext(), Information.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_EXTRA,tabPosition);

        mContext.startActivity(intent);;

    }

}
