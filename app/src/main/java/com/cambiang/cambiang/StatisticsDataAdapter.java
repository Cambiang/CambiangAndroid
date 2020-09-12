package com.cambiang.cambiang;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import org.w3c.dom.Text;

import java.util.List;

public class StatisticsDataAdapter extends RecyclerView.Adapter<StatisticsDataAdapter.ViewHolder>
{
    public String cambioType;
    public Spinner  typeStatisticsDataSpinner;
    Context mContext;
    Utilities utilities;


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView bankLogo;
        public TextView bankName;
        public TextView minUSD;
        public TextView minEUR;
        public TextView medUSD;
        public TextView medEUR;
        public TextView maxUSD;
        public TextView maxEUR;
        public TableRow header;
        public TextView refDate;




        public ViewHolder(View itemView)
        {
            super(itemView);

            bankLogo = (ImageView) itemView.findViewById(R.id.staticisBankLogo);
            bankName = (TextView) itemView.findViewById(R.id.statisticsBankName);
            minUSD = (TextView) itemView.findViewById(R.id.minUSD);
            minEUR = (TextView) itemView.findViewById(R.id.minEUR);
            medUSD = (TextView) itemView.findViewById(R.id.medUSD);
            medEUR = (TextView) itemView.findViewById(R.id.medEUR);
            maxUSD = (TextView) itemView.findViewById(R.id.maxUSD);
            maxEUR = (TextView) itemView.findViewById(R.id.maxEUR);
            header = (TableRow) itemView.findViewById(R.id.statisticsRowHeader);
            refDate = (TextView) itemView.findViewById(R.id.statisticsRefDate);
        }
    }


    private List<StatisticsData> statisticsDataArrayList;



    public StatisticsDataAdapter(List<StatisticsData> statisticsDataArrayList)
    {
        this.statisticsDataArrayList = statisticsDataArrayList;
    }

    @Override
    public StatisticsDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View cambiosHistoricView = inflater.inflate(R.layout.statistics_minimaxi_card, parent, false);

        mContext = parent.getContext();


        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(cambiosHistoricView);

        return viewHolder;
    }


    //Populating data into the item view holder

    @Override
    public void onBindViewHolder(StatisticsDataAdapter.ViewHolder viewHolder, int position)
    {

        utilities = new Utilities();

        //Get the data based on position
        StatisticsData statisticsDataArrayList = this.statisticsDataArrayList.get(position);

        this.setLogoAndDate(viewHolder, statisticsDataArrayList);
        this.setStatistics(viewHolder, statisticsDataArrayList);

    }



    public void setLogoAndDate(StatisticsDataAdapter.ViewHolder viewHolder, StatisticsData statisticsDataArrayList)
    {

        //Set views based on views and data model
        ImageView bankLogo = viewHolder.bankLogo;
        TextView bankName = viewHolder.bankName;
        TableRow headerStatisticsTable = viewHolder.header;

        if(statisticsDataArrayList.getBankName() != null)
        {

            switch (statisticsDataArrayList.getBankName())
            {

                case "Banca Central":
                    bankLogo.setImageResource(R.drawable.bna);
                    break;

                case "Banca Comercial":
                    bankLogo.setImageResource(R.drawable.bancacomercial);
                    break;


                case "Mercado Informal":
                    bankLogo.setImageResource(R.drawable.kinguilas);
                    break;
            }
        }

// Set Bank Name and name color
        if(statisticsDataArrayList.getBankName() != null)
            switch (statisticsDataArrayList.getBankName())
            {

                case "Banca Central" :
                    bankName.setText("Banca Central");
                    bankName.setTextColor(Color.rgb(182,152,91));
                    headerStatisticsTable.setBackgroundColor(Color.rgb(182,152,91));

                    break;


                case "Banca Comercial" :
                    bankName.setText("Banca Comercial");
                    bankName.setTextColor(Color.rgb(220,90,1));
                    headerStatisticsTable.setBackgroundColor(Color.rgb(220,90,1));

                    break;


                case "Mercado Informal" :
                    bankName.setText("Mercado Informal");
                    bankName.setTextColor(Color.rgb(107,150,42));
                    headerStatisticsTable.setBackgroundColor(Color.rgb(107,150,42));

                    break;
            }


    }


    public void setStatistics(StatisticsDataAdapter.ViewHolder viewHolder, StatisticsData statisticsDataArrayList)
    {
        if(statisticsDataArrayList != null)
        {
             viewHolder.refDate.setText("Valores Antigidos Últimos 12 Mêses Até: "+statisticsDataArrayList.getYear());

            viewHolder.minUSD.setText(statisticsDataArrayList.getMinUSD());
            viewHolder.minEUR.setText(statisticsDataArrayList.getMinEUR());

            viewHolder.medUSD.setText(statisticsDataArrayList.getMedUSD());
            viewHolder.medEUR.setText(statisticsDataArrayList.getMedEUR());

            viewHolder.maxUSD.setText(statisticsDataArrayList.getMaxUSD());
            viewHolder.maxEUR.setText(statisticsDataArrayList.getMaxEUR());

        }
    }




    @Override
    public int getItemCount()
    {
        return statisticsDataArrayList.size();
    }



}
