package com.cambiang.cambiang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JurosActivity extends AppCompatActivity {

    Toolbar toolbar;
    Utilities utilities = new Utilities();
    Context mContext;
    FirebaseDatabase database;
    DatabaseReference refAdmin;
    String gMobAdsAtJurosState = "OFF";
    String gMobAdsAtJurosType = "BANNER";
    LinearLayout adLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        setContentView(R.layout.activity_juros);


        adLayout = new LinearLayout(getApplicationContext());


        // Firebase Database Reference
        database = FirebaseDatabase.getInstance();
        refAdmin = database.getReference("Admin");

        //Set Juros Toolbar
        setToolbar();

        try
        {
            //show Juros
            this.showJuros();


            //Send first Analytics event before go to activity
            utilities.sendAnalyticsEvent("TaxasDeJuro", "TaxasDeJuro");


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
        intent = new Intent(this, InterestBanksViewActivity.class);
        //intent.putExtra("fragment","3"); // To get back to the caller Fragment
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        utilities.sendAnalyticsEvent("LEFT", "LEFT");

        startActivity(intent);

        return true;
    }


    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.juros_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.taxa_juros));
        toolbar.setTitleTextColor(Color.WHITE);

    }


    public void showJuros()
    {


        //Get Juros data
        String bankName = getIntent().getStringExtra("bankName");
        String bankFullName = getIntent().getStringExtra("bankFullName");
        String bankLogo = getIntent().getStringExtra("bankLogo");
        String bankLogoColor = getIntent().getStringExtra("bankLogoColor");
        String juros30 = getIntent().getStringExtra("juros30");
        String juros90 = getIntent().getStringExtra("juros90");
        String juros360 = getIntent().getStringExtra("juros360");
        String dateRef = getIntent().getStringExtra("dateRef");

        TableLayout tableLayout = (TableLayout) findViewById(R.id.table_juros);


        TextView jurosbankName = (TextView) findViewById(R.id.jurosBankName);
        TextView jurosbankFullName = (TextView) findViewById(R.id.jurosBankFullName);
        ImageView jurosbankLogo = (ImageView) findViewById(R.id.jurosBankLogo);
        TextView juros30Days = (TextView) findViewById(R.id.juros_30_days);
        TextView juros90Days = (TextView) findViewById(R.id.juros_90_days);
        TextView juros360Days = (TextView) findViewById(R.id.juros_360_days);
        TextView jurosdateRef = (TextView) findViewById(R.id.jurosRefDate);

        if(bankName != null && bankLogo != null) // Chosen arbitrary only two as representative for the rest, it's just a choice.
        {
            tableLayout.setBackgroundColor(Color.parseColor(bankLogoColor));
            jurosbankName.setText(bankName);
            jurosbankName.setTextColor(Color.parseColor(bankLogoColor));
            jurosbankFullName.setText(bankFullName);
            jurosbankFullName.setTextColor(Color.parseColor(bankLogoColor));
            setLogoImage(bankLogo, jurosbankLogo);
            juros30Days.setText(juros30 + "%");
            juros90Days.setText(juros90 + "%");
            juros360Days.setText(juros360 + "%");
            jurosdateRef.setText(dateRef);


            explanation(juros30, juros90, juros360,bankName,bankLogoColor);
        }


    }



    public void setLogoImage(String logo, ImageView bankLogo)
    {
        if(logo != null && bankLogo != null)
        {
            String cleanImage = logo.replace("data:image/jpeg;base64,","");

            byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if(decodedByte != null)
                bankLogo.setImageBitmap(decodedByte);
        }

    }


    public void explanation(String juros30,String juros90,String juros360, String bankName, String bankLogoColor)
    {

        String part1 = getString(R.string.juros_explanation_part_1);
        String part2 = getString(R.string.juros_explanation_part_2);
        String part3 = getString(R.string.juros_explanation_part_3);

        String gain = getString(R.string.juros_explanation_part_gain);
        String total = getString(R.string.juros_explanation_part_total);
        String deadline30 = getString(R.string.juros_explanation_part_deadline30);
        String deadline90 = getString(R.string.juros_explanation_part_deadline90);
        String deadline360 = getString(R.string.juros_explanation_part_deadline360);



        String explainJuros_1 = part1 + bankName + part2 + part3;

        TextView explanation_1 = (TextView)findViewById(R.id.juros_explanation_1);
        explanation_1.setText(explainJuros_1);

        Double totalInvestiment = 100000.0 ; //100 mil Kwanzas

        if(juros30 != null && !juros30.equals("-"))
        {
            Double juros30Doub = (1+Double.parseDouble(juros30)/100.0)*totalInvestiment;

            String explainJuros30_2 = gain + Double.toString(Math.round(Double.parseDouble(juros30)/100*totalInvestiment)) + "  AKZ";
            String explainJuros30_3 = total+Double.toString(Math.round(juros30Doub)) + "  AKZ";;

            TextView explanation_2 = (TextView)findViewById(R.id.juros_explanation_2);
            explanation_2.setText(explainJuros30_2);

            TextView explanation_3 = (TextView)findViewById(R.id.juros_explanation_3);
            explanation_3.setText(explainJuros30_3);

            TextView explanation_4 = (TextView)findViewById(R.id.juros_explanation_4);
            explanation_4.setText(deadline30);
            explanation_4.setTextColor(Color.parseColor(bankLogoColor));


        }

        if(juros90 != null && !juros90.equals("-"))
        {
            Double juros90Doub = (1+Double.parseDouble(juros90)/100.0)*totalInvestiment;

            String explainJuros90_2 = gain + Double.toString(Math.round((Double.parseDouble(juros90)/100)*totalInvestiment)) + "  AKZ";
            String explainJuros90_3 = total+Double.toString(Math.round(juros90Doub)) + "  AKZ";;

            TextView explanation_90_2 = (TextView)findViewById(R.id.juros_explanation_90_2);
            explanation_90_2.setText(explainJuros90_2);

            TextView explanation_90_3 = (TextView)findViewById(R.id.juros_explanation_90_3);
            explanation_90_3.setText(explainJuros90_3);

            TextView explanation_90_4 = (TextView)findViewById(R.id.juros_explanation_90_4);
            explanation_90_4.setText(deadline90);
            explanation_90_4.setTextColor(Color.parseColor(bankLogoColor));


        }

        if(juros360 != null && !juros360.equals("-"))
        {
            Double juros360Doub = (1+Double.parseDouble(juros360)/100.0)*totalInvestiment;

            String explainJuros360_2 = gain + Double.toString(Math.round((Double.parseDouble(juros360)/100)*totalInvestiment)) + "  AKZ";
            String explainJuros360_3 = total+Double.toString(Math.round(juros360Doub)) + "  AKZ";;

            TextView explanation_360_2 = (TextView)findViewById(R.id.juros_explanation_360_2);
            explanation_360_2.setText(explainJuros360_2);

            TextView explanation_360_3 = (TextView)findViewById(R.id.juros_explanation_360_3);
            explanation_360_3.setText(explainJuros360_3);

            TextView explanation_360_4 = (TextView)findViewById(R.id.juros_explanation_360_4);
            explanation_360_4.setText(deadline360);
            explanation_360_4.setTextColor(Color.parseColor(bankLogoColor));


        }


    }



    public void manageGooglMobAds()
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtJurosState"))
                    {

                        gMobAdsAtJurosState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtJurosState"))
                        {
                            gMobAdsAtJurosState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtJurosType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtJurosType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtJurosState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getTypeOfAd(gMobAdsAtJurosType)!= null)
                            adLayout = utilities.addAds(JurosActivity.this, getTypeOfAd(gMobAdsAtJurosType), View.VISIBLE,1,0);
                    }else
                    {
                        if(gMobAdsAtJurosState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtJurosType), View.GONE,1, adLayout);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                    // Log.wtf("gMobAdsAtJurosState: ", gMobAdsAtJurosState);
                    // Log.wtf("WTF: "+dataSnapshot.getValue().toString(),dataSnapshot.getKey().toString());
                    // Log.wtf("gMobAdsAtJurosState: ", gMobAdsAtJurosState);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtJurosState"))
                    {
                        gMobAdsAtJurosState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtJurosState"))
                        {
                            gMobAdsAtJurosState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtJurosType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtJurosType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtJurosState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getTypeOfAd(gMobAdsAtJurosType)!= null)
                            adLayout = utilities.addAds(JurosActivity.this, getTypeOfAd(gMobAdsAtJurosType), View.VISIBLE,1,0);
                        // Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                    {
                        if(gMobAdsAtJurosState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtJurosType), View.GONE,1,adLayout);
                            adLayout.setVisibility(View.GONE);
                            //Log.wtf("OFF: ", "Im GONE!!");

                        }
                    }

                }


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

    public AdSize getTypeOfAd(String adType)
    {

        if (adType.equals("BANNER"))
        {
            return AdSize.BANNER;
        }

        if (adType.equals("LARGE_BANNER"))
        {
            return AdSize.LARGE_BANNER;
        }

        if (adType.equals("SMART_BANNER"))
        {
            return AdSize.SMART_BANNER;
        }

        if (adType.equals("FULL_BANNER"))
        {
            return AdSize.FULL_BANNER;
        }


        return AdSize.BANNER;
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
