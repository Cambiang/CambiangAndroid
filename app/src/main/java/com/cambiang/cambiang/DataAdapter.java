package com.cambiang.cambiang;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder>
{
    public String cambioType;
    Context mContext;
    Utilities utilities;
    private int bankClickedPosition;
    private Context context;
    ArrayList<Ad> adArrayList = new ArrayList<>();
    DatabaseReference refAd;
    int counter = 0;


    public int getBankClickedPosition() {
        return bankClickedPosition;
    }

    public void setBankClickedPosition(int position) {
        this.bankClickedPosition = position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        public ImageView bankLogo;
        public ImageView usdArrow;
        public ImageView euroArrow;
        public TextView bankName;
        public TextView bankFullName;
        public TextView refDate;
        public TextView usdValue;
        public TextView euroValue;
        public TextView usdValuePrev;
        public TextView euroValuePrev;
        public TextView badgeUsdValue;
        public TextView badgeEuroValue;
        public ImageButton seeMoreBtn;
        public ImageButton shareCambioBtn;
        public View contextView;
        public Spinner  typeCambioSpinner;


        public ViewHolder(View itemView)
        {
            super(itemView);

            bankLogo = (ImageView) itemView.findViewById(R.id.bankLogo);
            bankName = (TextView) itemView.findViewById(R.id.bankName);
            bankFullName = (TextView) itemView.findViewById(R.id.bankFullName);
            refDate = (TextView) itemView.findViewById(R.id.refDate);
            usdValue = (TextView) itemView.findViewById(R.id.usdValue);
            euroValue = (TextView) itemView.findViewById(R.id.euroValue);
            usdArrow = (ImageView) itemView.findViewById(R.id.usdArrow);
            badgeUsdValue = (TextView) itemView.findViewById(R.id.badgeUsdValue);
            euroArrow = (ImageView) itemView.findViewById(R.id.euroArrow);
            badgeEuroValue = (TextView) itemView.findViewById(R.id.badgeEuroValue);
            typeCambioSpinner = (Spinner) itemView.findViewById(R.id.type_cambio);
            usdValuePrev = (TextView) itemView.findViewById(R.id.prevUsdValue);
            euroValuePrev = (TextView) itemView.findViewById(R.id.prevEuroValue);
            seeMoreBtn = (ImageButton) itemView.findViewById(R.id.see_more_btn);
            shareCambioBtn = (ImageButton) itemView.findViewById(R.id.share_cambio_btn);

            //seeMoreBtn.setOnCreateContextMenuListener(this);


            contextView = itemView.findViewById(R.id.snackbar_land);


        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.add(Menu.NONE, Utilities.JUROS_ID, Menu.NONE, mContext.getString(R.string.juros));
            menu.add(Menu.NONE, Utilities.INFORMATION_ID, Menu.NONE, mContext.getString(R.string.partilhar));
        }
    }


    private List<Cambio> cambioLastUpdate;
    private List<Bank> banksArray;



    public DataAdapter(List<Cambio> cambioLastUpdate, List<Bank> banksArray)
    {
        this.cambioLastUpdate = cambioLastUpdate;
        this.banksArray = banksArray;

        //Get Ads
        getAds();
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View cambiosLastUpdatesView = inflater.inflate(R.layout.cambio_card, parent, false);

        mContext = parent.getContext();


        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(cambiosLastUpdatesView);

        return viewHolder;
    }


    //Populating data into the item view holder

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int position)
    {

        utilities = new Utilities();

        if(this.cambioLastUpdate != null && this.banksArray != null)
        {
            if(this.cambioLastUpdate.get(position) != null && this.banksArray.size() > 0)
            {
                //Get the data based on position
                Cambio cambioLastUpdate = this.cambioLastUpdate.get(position);

                if(cambioLastUpdate.getBank().equals("KINGUILAS"))
                {
                    //Log.wtf("ONLY",cambioLastUpdate.getBank());

                    //Init spinner
                    this.initSpinners(mContext, viewHolder, position);
                }else
                {
                    viewHolder.typeCambioSpinner.setVisibility(View.GONE);

                }

                for( int i = 0 ; i < this.banksArray.size(); i++)
                {
                    if(banksArray.get(i).getName() != null && cambioLastUpdate.getBank() != null)
                    if(banksArray.get(i).getName().equals(cambioLastUpdate.getBank()))
                    {
                        this.setLogoAndDate(viewHolder, cambioLastUpdate, banksArray.get(i));

                        this.setCambio(viewHolder, cambioLastUpdate);


                        ////Log.wtf("this.cambioLastUpdate.get(position)",this.cambioLastUpdate.get(position).getBank() +"--"+position);
                        //Log.wtf("this.banksArray",this.banksArray.get(i).getName() +"--"+i);



                    }
                }

                /*
                viewHolder.seeMoreBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        if(viewHolder != null)
                        {
                            setBankClickedPosition(viewHolder.getAdapterPosition());

                        }else
                        {
                            //Default
                            setBankClickedPosition(0);
                        }

                        return false;
                    }


                });

                */

                viewHolder.seeMoreBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        showJuros(position);
                    }
                });

                viewHolder.shareCambioBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        sharingCambio(position);
                    }
                });
            }
        }

    }

    /*
    @Override
    public void onViewRecycled(ViewHolder viewHolder)
    {
        viewHolder.seeMoreBtn.setOnLongClickListener(null);
        super.onViewRecycled(viewHolder);
    }
    */



    public void setLogoAndDate(DataAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate, Bank bank )
    {

        //Set views based on views and data model
        ImageView bankLogo = viewHolder.bankLogo;
        TextView bankName = viewHolder.bankName;
        TextView bankFullName = viewHolder.bankFullName;


        if(cambioLastUpdate.getBank() != null && bank != null)
        {

            //Get the logo image from DataBase and not Local

            String cleanImage = bank.getLogo().replace("data:image/jpeg;base64,","");

            byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if(decodedByte != null)
                bankLogo.setImageBitmap(decodedByte);


            if(cambioLastUpdate.getBank()!= null && !cambioLastUpdate.getBank().isEmpty())
            {

                bankName.setText(cambioLastUpdate.getBank());

                //Change Kinguila Name
                if(cambioLastUpdate.getBank().equals("KINGUILAS"))
                    bankName.setText("INFORMAL");
            }

            if(bank.getLogoColor() != null && !bank.getLogoColor().isEmpty())
                bankName.setTextColor(Color.parseColor(bank.getLogoColor()));

            if(bank.getFullName()!= null && !bank.getFullName().isEmpty())
                bankFullName.setText(bank.getFullName());

            if(bank.getLogoColor() != null && !bank.getLogoColor().isEmpty())
                bankFullName.setTextColor(Color.parseColor(bank.getLogoColor()));


            TextView refDate = viewHolder.refDate;
            if(cambioLastUpdate.getRefDate() != null)
                refDate.setText(cambioLastUpdate.getRefDate());


        }


      /*
            switch (cambioLastUpdate.getBank())
            {
                case "ATLANTICO" :
                    bankLogo.setImageResource(R.drawable.atlantico);

                    break;

                case "BFA" :
                    bankLogo.setImageResource(R.drawable.bfa);
                    break;

                case "BIC" :
                    bankLogo.setImageResource(R.drawable.bic);
                    break;

                case "BSOL" :
                    bankLogo.setImageResource(R.drawable.bsol);
                    break;

                case "BNA" :
                    bankLogo.setImageResource(R.drawable.bna);
                    break;

                case "BAI" :
                    bankLogo.setImageResource(R.drawable.bai);
                    break;


                case "KINGUILAS" :
                    bankLogo.setImageResource(R.drawable.kinguilas);
                    break;


            }

       */

// Set Bank Name and name color
     /*   if(cambioLastUpdate.getBank() != null)
            switch (cambioLastUpdate.getBank())
            {
                case "ATLANTICO" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(7,153,192));

                    break;

                case "BFA" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(220,90,1));
                   // bankName.setTextColor(Color.parseColor("#96f905"));


                    break;

                case "BIC" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(235,28,36));
                    break;

                case "BSOL" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(254,188,20));
                    break;

                case "BNA" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(182,152,91));
                    break;

                case "BAI" :
                    bankName.setText(cambioLastUpdate.getBank());
                    bankName.setTextColor(Color.rgb(25,67,110));
                    break;


                case "KINGUILAS" :
                    bankName.setText("INFORMAL");
                    bankName.setTextColor(Color.rgb(107,150,42));
                    break;
            }

      */



    }


    public void setCambio(DataAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
    {
        TextView usdValue = viewHolder.usdValue;
        TextView usdValuePrev = viewHolder.usdValuePrev;

        if(cambioLastUpdate.getUsdValue() != null)
        {
            if(cambioLastUpdate.getType().equals("Venda"))
            {
                usdValue.setText(cambioLastUpdate.getUsdValue());
                ////Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
            }

            if(cambioLastUpdate.getUsdValuePrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Venda"))
                {
                    usdValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getUsdValuePrev());
                    ////Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
                }

            }


        }

        if(cambioLastUpdate.getUsdValueBuying() != null)
        {
            if(cambioLastUpdate.getType().equals("Compra"))
            {
                usdValue.setText(cambioLastUpdate.getUsdValueBuying());

                ////Log.wtf("getUsdValueBuying()",cambioLastUpdate.getUsdValueBuying());
            }

            if(cambioLastUpdate.getUsdValueBuyingPrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Compra"))
                {
                    usdValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getUsdValueBuyingPrev());
                    ////Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
                }

            }
        }

        TextView euroValue = viewHolder.euroValue;
        TextView euroValuePrev = viewHolder.euroValuePrev;

        if(cambioLastUpdate.getEuroValue() != null)
        {
            if(cambioLastUpdate.getType().equals("Venda"))
            {
                euroValue.setText(cambioLastUpdate.getEuroValue());
                ////Log.wtf("getEuroValue()",cambioLastUpdate.getEuroValue());

            }

            if(cambioLastUpdate.getEuroValuePrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Venda"))
                {
                    euroValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getEuroValuePrev());
                }


            }

        }

        if(cambioLastUpdate.getEuroValueBuying() != null)
        {
            if(cambioLastUpdate.getType().equals("Compra"))
            {
                euroValue.setText(cambioLastUpdate.getEuroValueBuying());

               // //Log.wtf("getEuroValueBuying()",cambioLastUpdate.getEuroValueBuying());
            }

            if(cambioLastUpdate.getEuroValueBuyingPrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Compra"))
                {
                    euroValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getEuroValueBuyingPrev());
                    ////Log.wtf("getEuroValue()",cambioLastUpdate.getEuroValue());
                }


            }
        }


        if(cambioLastUpdate.getType().equals("Venda") )
        {
            setChangeRateSelling(viewHolder, cambioLastUpdate);
        }else
        {
            if(cambioLastUpdate.getType().equals("Compra"))
            {
                setChangeRateBuying(viewHolder,cambioLastUpdate);
            }
        }


    }


    public void setChangeRateSelling(DataAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
    {
        ImageView usdArrow= viewHolder.usdArrow;
        TextView badgeUsdValue = viewHolder.badgeUsdValue;
        ImageView euroArrow= viewHolder.euroArrow;
        TextView badgeEuroValue = viewHolder.badgeEuroValue;


        if(cambioLastUpdate.getUsdArrowType() != null)
        {
            if(cambioLastUpdate.getUsdArrowType().equals("up"))
            {
                usdArrow.setImageResource(R.drawable.ic_arrow_upward_black_24dp);

                ////Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getUsdChangeRate() != null)
                {
                    badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRate());
                    ////Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());

                    badgeUsdValue.setTextColor(Color.rgb(179,0,12));
                }

            }else
            {
                if(cambioLastUpdate.getUsdArrowType().equals("down"))
                {
                    usdArrow.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    if(cambioLastUpdate.getUsdChangeRate() != null)
                    {
                        badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRate());
                        badgeUsdValue.setTextColor(Color.rgb(34,140,34));
                    }
                }else
                    {
                        if(cambioLastUpdate.getUsdArrowType().equals("flat"))
                        {
                           usdArrow.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
                            if(cambioLastUpdate.getUsdChangeRate() != null)
                            {
                                badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRate());
                                badgeUsdValue.setTextColor(Color.rgb(35,46,43));
                            }
                        }
                    }
            }

        }

        if(cambioLastUpdate.getEuroArrowType() != null)
        {
            if(cambioLastUpdate.getEuroArrowType().equals("up"))
            {
                euroArrow.setImageResource(R.drawable.ic_arrow_upward_black_24dp);

                ////Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getEuroChangeRate() != null)
                {
                    badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRate());
                    ////Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());
                    badgeEuroValue.setTextColor(Color.rgb(179,0,12));
                }

            }else
            {
                if(cambioLastUpdate.getEuroArrowType().equals("down"))
                {
                    euroArrow.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    if(cambioLastUpdate.getEuroChangeRate() != null)
                    {
                        badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRate());
                        badgeEuroValue.setTextColor(Color.rgb(34,140,34));
                    }
                }else
                    {
                        if(cambioLastUpdate.getEuroArrowType().equals("flat"))
                        {
                            euroArrow.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
                            if(cambioLastUpdate.getEuroChangeRate() != null)
                            {
                                badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRate());
                                badgeEuroValue.setTextColor(Color.rgb(35,46,43));
                            }
                        }
                    }
            }

        }
    }


    public void setChangeRateBuying(DataAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
    {
        ImageView usdArrow = viewHolder.usdArrow;
        TextView badgeUsdValue = viewHolder.badgeUsdValue;
        ImageView euroArrow= viewHolder.euroArrow;
        TextView badgeEuroValue = viewHolder.badgeEuroValue;


        if(cambioLastUpdate.getUsdArrowTypeBuying() != null)
        {
            if(cambioLastUpdate.getUsdArrowTypeBuying().equals("up"))
            {
                usdArrow.setImageResource(R.drawable.buying_ic_arrow_upward_black_24dp);

                ////Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getUsdChangeRateBuying() != null)
                {
                    badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRateBuying());
                    ////Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());

                    badgeUsdValue.setTextColor(Color.rgb(34,140,34));
                }

            }else
            {
                if(cambioLastUpdate.getUsdArrowTypeBuying().equals("down"))
                {
                    usdArrow.setImageResource(R.drawable.buying_ic_arrow_downward_black_24dp);
                    if(cambioLastUpdate.getUsdChangeRateBuying() != null)
                    {
                        badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRateBuying());
                        badgeUsdValue.setTextColor(Color.rgb(179,0,12));
                    }
                }else
                    {
                        if(cambioLastUpdate.getUsdArrowTypeBuying().equals("flat"))
                        {
                            usdArrow.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
                            if(cambioLastUpdate.getUsdChangeRateBuying() != null)
                            {
                                badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRateBuying());
                                badgeUsdValue.setTextColor(Color.rgb(35,46,43));
                            }
                        }
                    }
            }

        }

        if(cambioLastUpdate.getEuroArrowTypeBuying() != null)
        {
            if(cambioLastUpdate.getEuroArrowTypeBuying().equals("up"))
            {
                euroArrow.setImageResource(R.drawable.buying_ic_arrow_upward_black_24dp);

                ////Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getEuroChangeRateBuying() != null)
                {
                    badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRateBuying());
                    ////Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());
                    badgeEuroValue.setTextColor(Color.rgb(34,140,34));
                }

            }else
            {
                if(cambioLastUpdate.getEuroArrowTypeBuying().equals("down"))
                {
                    euroArrow.setImageResource(R.drawable.buying_ic_arrow_downward_black_24dp);
                    if(cambioLastUpdate.getEuroChangeRateBuying() != null)
                    {
                        badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRateBuying());
                        badgeEuroValue.setTextColor(Color.rgb(179,0,12));
                    }
                }else
                    {
                        if(cambioLastUpdate.getEuroArrowTypeBuying().equals("flat"))
                        {
                            euroArrow.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
                            if(cambioLastUpdate.getEuroChangeRateBuying() != null)
                            {
                                badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRateBuying());
                                badgeEuroValue.setTextColor(Color.rgb(35,46,43));
                            }
                        }
                    }
            }

        }
    }

    @Override
    public int getItemCount()
    {
        return cambioLastUpdate.size();
    }


    public void updateCambios(List<Cambio> cambios) {
        cambioLastUpdate = cambios;
        notifyDataSetChanged();
    }



    public void initSpinners(Context mContext, ViewHolder viewHolder, int positionCamb)
    {

        if(positionCamb == viewHolder.getAdapterPosition())
        {
            //Log.wtf("tag",viewHolder.itemView.getTag().toString());

            viewHolder.typeCambioSpinner.setVisibility(View.VISIBLE);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,R.array.cambio_type_choices,R.layout.spinner_card_view);

            // specify layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_item);

            //Apply adapter to the spinners
            viewHolder.typeCambioSpinner.setAdapter(adapter);
        }else
            {
                viewHolder.typeCambioSpinner.setVisibility(View.GONE);

            }



        //Spinner OnListener
        viewHolder.typeCambioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int posCambio = viewHolder.getAdapterPosition();

                cambioType = parent.getItemAtPosition(position).toString();

                ////Log.wtf("Cambio_type ", cambioType);
                ////Log.wtf("parent.getItemAtPosition 2",viewHolder.itemView.getTag().toString());

               // Log.wtf("itemView.getTag()",viewHolder.itemView.getTag().toString() + "Type: "+cambioType);

                if(cambioType != null)
                {
                    utilities.sendAnalyticsEvent("CambioType", cambioType);


                    updateCambioList(posCambio, cambioType, viewHolder);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });






    }


    public void updateCambioList(int position, String type, DataAdapter.ViewHolder viewHolder)
    {


        if(this.cambioLastUpdate.get(position) != null)
        {
            Cambio cambio =  this.cambioLastUpdate.get(position);

            cambio.setType(type); //Type must be set to display what user selected in the spinner, either "Venda" or "Compra" values

            for (Bank bank : banksArray)
            {
                if(cambio.getBank().equals(bank.getName()))
                {
                    setLogoAndDate(viewHolder, cambio, bank);

                    setCambio(viewHolder, cambio);


                    if(cambio.getType().equals("Venda") )
                    {
                        setChangeRateSelling(viewHolder, cambio);
                    }else
                    {
                        if(cambio.getType().equals("Compra"))
                        {
                            setChangeRateBuying(viewHolder,cambio);
                        }
                    }

                }

            }


        }


    }


    public void getAds()
    {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();

        database.getReference("Ad").orderByChild("category").equalTo("external").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                if(dataSnapshot.getValue(Ad.class) != null)
                {
                    Ad ad = dataSnapshot.getValue(Ad.class);

                    if(ad.getState().equals("ON"))
                    {
                        adArrayList.add(ad);

                    }

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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


    public void showAds(Ad ad, ViewHolder viewHolder)
    {
        String cleanImage = ad.getImageURL().replace("data:image/jpeg;base64,","");

        byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


        Drawable img = new BitmapDrawable(context.getResources(), decodedByte);

        if(ad.getTitle().length() > 30)
        {
            String titleTruncated = ad.getTitle().substring(0,30)+" ...";
            ad.setTitle(titleTruncated);
        }


        Snackbar snackbar = Snackbar.make(viewHolder.contextView, ad.getTitle().toUpperCase(), Snackbar.LENGTH_INDEFINITE);
        View snackbarLayout = snackbar.getView();
        //Drawable img = context.getDrawable(R.drawable.bancacomercial);
        // You need to setBounds before setCompoundDrawables , or it couldn't display
        img.setBounds(0, 0, 64, 64);
        TextView textView = (TextView)snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text);
        //textView.setWidth(10);
        //textView.setHeight(10);
        textView.setCompoundDrawablePadding(10);
        //textView.setCompoundDrawablesWithIntrinsicBounds(img, 0, 0, 0);
        textView.setCompoundDrawables(img, null, null, null);


        snackbar.setAction("Ver", new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                redirectAd(ad);
            }
        }).show();
    }


   public void redirectAd(Ad ad)
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


   public void rotateAds(ViewHolder viewHolder)
   {
       final int interval = 10000; // 1 Second
       Handler handler = new Handler();

       Runnable runnable = new Runnable(){
           public void run()
           {

               if(adArrayList.size() > 0)
               {
                   if(counter < adArrayList.size())
                   {
                       Ad ad = adArrayList.get(counter);
                       if(ad != null)
                       {
                           showAds(ad, viewHolder);
                           Log.wtf("runnable ","RUNNING MAN");

                       }
                   }else
                       {
                           counter = 0;
                       }

               }

               counter++;

           }
       };
       handler.postAtTime(runnable, System.currentTimeMillis()+interval);
       handler.postDelayed(runnable, interval);



   }

    public void showJuros(int position)
    {
        Intent intent;
        intent = new Intent(mContext,JurosActivity.class);

        Cambio cambioLastUpdate = this.cambioLastUpdate.get(position);

        if(cambioLastUpdate != null && this.banksArray != null)
        {
            if(this.banksArray.size() > 0)
            {
                for (Bank bank : this.banksArray)
                {
                    if(bank.getName() != null && cambioLastUpdate.getBank() != null)
                        if(bank.getName().equals(cambioLastUpdate.getBank()) && !cambioLastUpdate.getBank().equals("BNA") && !cambioLastUpdate.getBank().equals("KINGUILAS"))
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

    public void sharingCambio(int position)
    {
        if(this.cambioLastUpdate != null)
        {
            if(this.cambioLastUpdate.size() > 0)
            {
                if(this.cambioLastUpdate.get(position) != null)
                {
                    String bankName = this.cambioLastUpdate.get(position).getBank();
                    String dolar = this.cambioLastUpdate.get(position).getUsdValue();
                    String euro = this.cambioLastUpdate.get(position).getEuroValue();
                    String dateRef = this.cambioLastUpdate.get(position).getRefDate();
                    String bankFullName = "";

                    if(this.cambioLastUpdate.get(position) != null && banksArray != null)
                    {
                        if(banksArray.size() > 0)
                        {
                            for (Bank bank : this.banksArray)
                            {
                                if(bank.getName() != null && cambioLastUpdate.get(position).getBank() != null)
                                    if(bank.getName().equals(this.cambioLastUpdate.get(position).getBank()))
                                    {
                                        bankFullName = bank.getFullName();
                                    }
                            }
                        }


                    }

                    if(bankName != null && dolar != null && euro != null && dateRef != null && bankFullName != null)
                    {

                        if(!bankName.isEmpty() && !dolar.isEmpty() && !euro.isEmpty() && !bankFullName.isEmpty())
                        {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "*"+bankName+"* ("+bankFullName+ ")\n" + "Câmbio aos *" + dateRef + "*\n"+
                                    "1 USD : " + "*" + dolar + ",00 AKZ*"+"\n" +
                                    "1 EUR : " + "*" + euro + ",00 AKZ*"+"\n" +
                                    "\n*Câmbio é no Cambiang*\n"
                                    + "*Google Play:* " + "https://play.google.com/store/apps/details?id=com.cambiang.cambiang" + "\n\n"
                                    + "*Apple Store:* " + "https://apps.apple.com/us/app/cambiang/id1472229549#?platform=iphone" + "\n\n"
                                    + "*Web:* "+ "https://cambiang.com"

                            );

                            sendIntent.setType("text/plain");


                            //Send Analytics event
                            utilities.sendAnalyticsEvent("SharingCambio", "DailyCambio");

                            mContext.startActivity(Intent.createChooser(sendIntent,"send"));

                        }

                    }

                }
            }
        }

    }

}




