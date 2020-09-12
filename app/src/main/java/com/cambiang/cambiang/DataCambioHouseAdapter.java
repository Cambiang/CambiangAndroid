package com.cambiang.cambiang;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;

import java.util.List;

public class DataCambioHouseAdapter extends RecyclerView.Adapter<DataCambioHouseAdapter.ViewHolder>
{
    Context mContext;
    Utilities utilities;


    public class ViewHolder extends RecyclerView.ViewHolder
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
        public TextView cambioHousePosition;
        public TextView address;
        public TextView telephone;
        public TextView email;


        public ViewHolder(View itemView)
        {
            super(itemView);

            bankLogo = (ImageView) itemView.findViewById(R.id.cambio_house_bankLogo);
            bankName = (TextView) itemView.findViewById(R.id.cambio_house_bankName);
            bankFullName = (TextView) itemView.findViewById(R.id.cambio_house_bankFullName);
            refDate = (TextView) itemView.findViewById(R.id.cambio_house_refDate);
            usdValue = (TextView) itemView.findViewById(R.id.cambio_house_usdValue);
            euroValue = (TextView) itemView.findViewById(R.id.cambio_house_euroValue);
            usdArrow = (ImageView) itemView.findViewById(R.id.cambio_house_usdArrow);
            badgeUsdValue = (TextView) itemView.findViewById(R.id.cambio_house_badgeUsdValue);
            euroArrow = (ImageView) itemView.findViewById(R.id.cambio_house_euroArrow);
            badgeEuroValue = (TextView) itemView.findViewById(R.id.cambio_house_badgeEuroValue);
            usdValuePrev = (TextView) itemView.findViewById(R.id.cambio_house_prevUsdValue);
            euroValuePrev = (TextView) itemView.findViewById(R.id.cambio_house_prevEuroValue);
            cambioHousePosition = (TextView) itemView.findViewById(R.id.cambio_house_position);
            address = (TextView) itemView.findViewById(R.id.cambio_house_address);
            telephone = (TextView) itemView.findViewById(R.id.cambio_house_telephone);
            email = (TextView) itemView.findViewById(R.id.cambio_house_email);

        }
    }


    private List<Cambio> cambioLastUpdate;
    private List<Bank> banksArray;



    public DataCambioHouseAdapter(List<Cambio> cambioLastUpdate, List<Bank> banksArray)
    {
        this.cambioLastUpdate = cambioLastUpdate;
        this.banksArray = banksArray;
    }

    @Override
    public DataCambioHouseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom view
        View cambiosLastUpdatesView = inflater.inflate(R.layout.cambio_house_card, parent, false);

        mContext = parent.getContext();


        //Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(cambiosLastUpdatesView);

        return viewHolder;
    }


    //Populating data into the item view holder

    @Override
    public void onBindViewHolder(DataCambioHouseAdapter.ViewHolder viewHolder, int position)
    {

        utilities = new Utilities();

        //Get the data based on position
        Cambio cambioLastUpdate = this.cambioLastUpdate.get(position);

        for( int i = 0 ; i < this.banksArray.size(); i++)
        {
            if(banksArray != null)
            {
                if(banksArray.get(i).getName() != null && cambioLastUpdate.getBank() != null)
                {
                    if(banksArray.get(i).getName().equals(cambioLastUpdate.getBank()))
                    {
                        this.setLogoAndDate(viewHolder, cambioLastUpdate, banksArray.get(i), position);

                        this.setCambio(viewHolder, cambioLastUpdate);

                        // Log.wtf("this.cambioLastUpdate.get(position)",this.cambioLastUpdate.get(position) +"--"+position);

                    }
                }
            }

        }



    }



    public void setLogoAndDate(DataCambioHouseAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate, Bank bank, int position )
    {

        //Set views based on views and data model
        TextView refDate = viewHolder.refDate;
        ImageView bankLogo = viewHolder.bankLogo;
        TextView bankName = viewHolder.bankName;
        TextView bankFullName = viewHolder.bankFullName;
        TextView cambioHousePostion = viewHolder.cambioHousePosition;
        TextView address = viewHolder.address;
        TextView telephone = viewHolder.telephone;
        TextView email = viewHolder.email;


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


            //Ranking Position
            position = position + 1;
            cambioHousePostion.setText(Integer.toString(position));
            cambioHousePostion.setTextColor(Color.parseColor(bank.getLogoColor()));


            if(cambioLastUpdate.getRefDate() != null)
                refDate.setText(cambioLastUpdate.getRefDate());

            if(bank.getAddress() != null && !bank.getAddress().isEmpty())
            {
                address.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0);
                address.setText(bank.getAddress());
                address.setCompoundDrawablePadding(10);


            }else
                {
                    address.setText("");
                }



            if(bank.getTelephoneNumberA() != null && !bank.getTelephoneNumberA().isEmpty())
            {
                if(bank.getTelephoneNumberB() != null && !bank.getTelephoneNumberB().isEmpty())
                {
                    telephone.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_black_24dp, 0, 0, 0);
                    telephone.setText(bank.getTelephoneNumberA() + " |  "+bank.getTelephoneNumberB());
                    telephone.setCompoundDrawablePadding(10);


                }else
                    {
                        telephone.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_black_24dp, 0, 0, 0);
                        telephone.setText(bank.getTelephoneNumberA());
                        telephone.setCompoundDrawablePadding(10);

                    }
            }else
                {
                    telephone.setText( "");
                }



            if(bank.getEmail() != null && !bank.getEmail().isEmpty())
            {
                email.setText(" " + bank.getEmail());
                email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_black_24dp, 0, 0, 0);

            }else
                {
                    email.setText("");
                }



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


    public void setCambio(DataCambioHouseAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
    {
        TextView usdValue = viewHolder.usdValue;
        TextView usdValuePrev = viewHolder.usdValuePrev;

        if(cambioLastUpdate.getUsdValue() != null)
        {
            if(cambioLastUpdate.getType().equals("Venda"))
            {
                usdValue.setText(cambioLastUpdate.getUsdValue());
                //Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
            }

            if(cambioLastUpdate.getUsdValuePrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Venda"))
                {
                    usdValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getUsdValuePrev());
                    //Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
                }

            }

             
        }

        if(cambioLastUpdate.getUsdValueBuying() != null)
        {
            if(cambioLastUpdate.getType().equals("Compra"))
            {
                usdValue.setText(cambioLastUpdate.getUsdValueBuying());

                //Log.wtf("getUsdValueBuying()",cambioLastUpdate.getUsdValueBuying());
            }

            if(cambioLastUpdate.getUsdValueBuyingPrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Compra"))
                {
                    usdValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getUsdValueBuyingPrev());
                    //Log.wtf("getUsdValue()",cambioLastUpdate.getUsdValue());
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
                //Log.wtf("getEuroValue()",cambioLastUpdate.getEuroValue());

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

               // Log.wtf("getEuroValueBuying()",cambioLastUpdate.getEuroValueBuying());
            }

            if(cambioLastUpdate.getEuroValueBuyingPrev() != null)
            {
                if(cambioLastUpdate.getType().equals("Compra"))
                {
                    euroValuePrev.setText(mContext.getString(R.string.valor_anterior_450) +" " + cambioLastUpdate.getEuroValueBuyingPrev());
                    //Log.wtf("getEuroValue()",cambioLastUpdate.getEuroValue());
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


    public void setChangeRateSelling(DataCambioHouseAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
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

                //Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getUsdChangeRate() != null)
                {
                    badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRate());
                    //Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());

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

                //Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getEuroChangeRate() != null)
                {
                    badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRate());
                    //Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());
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


    public void setChangeRateBuying(DataCambioHouseAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate)
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

                //Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getUsdChangeRateBuying() != null)
                {
                    badgeUsdValue.setText(cambioLastUpdate.getUsdChangeRateBuying());
                    //Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());

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

                //Log.wtf("DataAdapter 1",cambioLastUpdate.getUsdChangeRate());

                if(cambioLastUpdate.getEuroChangeRateBuying() != null)
                {
                    badgeEuroValue.setText(cambioLastUpdate.getEuroChangeRateBuying());
                    //Log.wtf("DataAdapter 2",cambioLastUpdate.getUsdChangeRate());
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





    public void updateCambioList(int position, String type, DataCambioHouseAdapter.ViewHolder viewHolder, Cambio cambioLastUpdate, Bank bank)
    {
        //Get the data based on position
        this.cambioLastUpdate.get(position).setType(type);

        if(cambioLastUpdate != null && bank != null)
        {
            setLogoAndDate(viewHolder, cambioLastUpdate, bank,position);

            setCambio(viewHolder, cambioLastUpdate);


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

    }


}
