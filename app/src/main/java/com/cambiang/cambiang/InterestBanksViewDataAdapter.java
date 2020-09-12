package com.cambiang.cambiang;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;

import java.util.List;

public class InterestBanksViewDataAdapter extends RecyclerView.Adapter<InterestBanksViewDataAdapter.ViewHolder> {

    Context mContext;
    Utilities utilities;
    private List<Bank> interestBankList;
    String KEY_EXTRA = "fragment";
    String tabPosition = "3";

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView interestBankImage;
        public TextView interestBankTitle;
        public TextView interestBankSubTitle;
        public CardView cardView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            this.interestBankImage = (ImageView) itemView.findViewById(R.id.interest_banks_img);
            this.interestBankTitle = (TextView) itemView.findViewById(R.id.interest_banks_title);
            this.interestBankSubTitle = (TextView) itemView.findViewById(R.id.interest_banks_subtitle);
            this.cardView = (CardView) itemView.findViewById(R.id.interest_banks_card_view);
        }
    }

    //This constructor is used on the activity to load data into Adapter
    public InterestBanksViewDataAdapter(List<Bank> interestBankList)
    {
        this.interestBankList = interestBankList;

        Log.wtf("ADAPTER", "BankDataAdapter");

    }


    @Override
    public InterestBanksViewDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View interestBankListView = inflater.inflate(R.layout.interest_banks_card, parent, false);

        this.mContext = parent.getContext();

        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(interestBankListView);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return this.interestBankList.size();
    }

    @Override
    public void onBindViewHolder(InterestBanksViewDataAdapter.ViewHolder holder, int position)
    {

        Bank chosenbank = new Bank();
             chosenbank = this.interestBankList.get(position);

        this.setInterestBanksCardView(holder, chosenbank);



        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                holder.cardView.setCardBackgroundColor(Color.rgb(240,255,255));

                if(position >= 0)
                {
                    showJuros(position);
                }

            }
        });


    }



    public void setInterestBanksCardView(InterestBanksViewDataAdapter.ViewHolder viewHolder, Bank bank )
    {

        //Set views based on views and data model
        ImageView bankLogo = viewHolder.interestBankImage;
        TextView bankName = viewHolder.interestBankTitle;
        TextView bankFullName = viewHolder.interestBankSubTitle;


        if(bank != null)
        {

            if(bank.getLogo() != null)
            {
                //Get the logo image from DataBase and not Local
                String cleanImage = bank.getLogo().replace("data:image/jpeg;base64,","");

                byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if(decodedByte != null)
                    bankLogo.setImageBitmap(decodedByte);
            }


            if(bank.getName() != null)
            {
                if(!bank.getName().equals("KINGUILAS") && !bank.getName().equals("-"))
                {
                    if(bank.getName()!= null && !bank.getName().isEmpty())
                        bankName.setText(bank.getName());

                    if(bank.getLogoColor() != null && !bank.getLogoColor().isEmpty())
                        bankName.setTextColor(Color.parseColor(bank.getLogoColor()));

                    if(bank.getFullName()!= null && !bank.getFullName().isEmpty())
                        bankFullName.setText(bank.getFullName());

                    if(bank.getLogoColor() != null && !bank.getLogoColor().isEmpty())
                        bankFullName.setTextColor(Color.parseColor(bank.getLogoColor()));
                }

            }


        }

    }



    public void showJuros(int position)
    {
        Intent intent;
        intent = new Intent(mContext,JurosActivity.class);

        Bank bank = this.interestBankList.get(position);

        if(bank != null)
        {
                if(bank.getName() != null)
                {
                    if(!bank.getName().equals("-") && !bank.getName().equals("BNA") && !bank.getName().equals("KINGUILAS"))
                    {
                        intent.putExtra("bankName",bank.getName());
                        intent.putExtra("bankFullName",bank.getFullName());
                        intent.putExtra("bankLogo",bank.getLogo());
                        intent.putExtra("bankLogoColor",bank.getLogoColor());

                        intent.putExtra("juros30",bank.getInterestRate30());
                        intent.putExtra("juros90",bank.getInterestRate90());
                        intent.putExtra("juros360",bank.getInterestRate360());
                        intent.putExtra("dateRef",bank.getInterestRateLastUpdateDate());


                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                }


        }


    }





}
