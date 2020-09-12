package com.cambiang.cambiang;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;

import com.google.android.gms.ads.AdSize;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConverterFragment extends Fragment {
    Toolbar toolbar;
    Utilities utilities;

    String kingUSD;
    String comercialBanksAvarageCambioUSD;
    String kingEUR;
    String comercialBanksAvarageCambioEUR;
    String valueToConvert;
    String valueToConvertCopy;
    String convertedValue;
    String convertedValueComercial;
    String convertedValueComercialByBank;
    String inputCurrency;
    String outputCurrency;
    Spinner inputCorrencySpinner;
    Spinner outputCorrencySpinner;
    EditText inputCurrencyAmount;
    TextView outputKinguilaAmount;
    TextView outputComercialAmount;
    TextView outputComercialAmountByBank;

    FirebaseDatabase database;
    DatabaseReference ref, ref2;
    Integer counter = 0;
    ComercialAverage comercialAverage = new ComercialAverage();
    DatabaseReference refAdmin;
    String gMobAdsAtConverterState = "OFF";
    String gMobAdsAtConverterType = "BANNER";
    LinearLayout adLayout;
    MaterialButtonToggleGroup btnToggleFrom;
    MaterialButtonToggleGroup btnToggleTo;
    NumberPicker picker;
    ArrayList<Cambio> cambios = new ArrayList<>();
    ArrayList<String> banksNames;
    HashMap<String,String> cambiosByBank = new HashMap<>();
    String npBankNamesState;
    public static final String PREFS_NAME = "cambiosLastUpdates";
    List<Cambio> cambiosLastUpdate = new ArrayList<>(0);
    List<Bank> banksArray ;
    SharedPreferences settings;



    public ConverterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_converter, container, false);

    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onActivityCreated(saveInstanceState);


        try {
            utilities = new Utilities();

            utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

            adLayout = new LinearLayout(getActivity().getApplicationContext());


            //utilities.showGoogleAds(getActivity().getApplicationContext(),getActivity(),R.id.adView_converter);

            comercialBanksAvarageCambioUSD = new String();
            comercialBanksAvarageCambioEUR = new String();

            banksNames = new ArrayList<>();

            cambiosLastUpdate = new ArrayList<>(0);
            banksArray = new ArrayList<Bank>();


            settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

            // Write a message to the database
            database = FirebaseDatabase.getInstance();
            ref = database.getReference("CambiosLastUpdates");
            ref2 = database.getReference("Banks");

            refAdmin = database.getReference("Admin");

            picker = (NumberPicker) getActivity().findViewById(R.id.np);


            //General manager decides whether values comes from DB or SharedPreferences
            generalManager();


            outputKinguilaAmount = (TextView) getActivity().findViewById(R.id.output_informal_amount);
            outputComercialAmount = (TextView) getActivity().findViewById(R.id.output_comercial_amount);
            outputComercialAmountByBank = (TextView) getActivity().findViewById(R.id.output_comercial_amount_by_bank);




            //init spinners
            //initSpinners();

            toggleLisner();

            getInputCurrencyAmount();

            //Send first Analytics event before go to activity
            utilities.sendAnalyticsEvent("Arrival", "Converter");


            //utilities.addAds(getActivity(),AdSize.F);
           // this.manageGooglMobAds();


            ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);
            shareCambioConversionBtn.setImageResource(R.drawable.ic_share_24p);
            shareCambioConversionBtn.setVisibility(View.GONE);



            //Number Picker with Banks Names Listener
            npProcessing();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }




    public void toggleLisner()
    {


        btnToggleFrom = (MaterialButtonToggleGroup) getActivity().findViewById(R.id.currency_from_options);
        btnToggleTo = (MaterialButtonToggleGroup) getActivity().findViewById(R.id.currency_to_options);

        MaterialButton btnInitFrom =  btnToggleFrom.findViewById(R.id.ic_from_dolar);
        MaterialButton btnInitTo =  btnToggleTo.findViewById(R.id.ic_to_kwanza);

        if(btnInitFrom != null)
            btnInitFrom.setChecked(true);

        if(btnInitTo != null)
            btnInitTo.setChecked(true);

        //init
        inputCurrency = "Dolar"; outputCurrency = "Kwanza";


        btnToggleFrom.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked)
            {


                MaterialButton btn = btnToggleFrom.findViewById(checkedId);

                if(btn != null)
                {
                    if(isChecked)
                    {
                        inputCurrency = btn.getText().toString();

                        if(inputCurrency.equals("USD"))
                        {
                            inputCurrency = "Dolar";
                        }
                        if(inputCurrency.equals("EUR"))
                        {
                            inputCurrency = "Euro";
                        }
                        if(inputCurrency.equals("AKZ"))
                        {
                            inputCurrency = "Kwanza";
                        }

                    }
                    if(isChecked && inputCurrency != null && outputCurrency != null && inputCurrencyAmount != null)
                    {


                        valueToConvertCopy = inputCurrencyAmount.getText().toString();
                        valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");



                        if(kingUSD != null && kingEUR!= null && comercialBanksAvarageCambioUSD != null && comercialBanksAvarageCambioEUR!= null
                                && !kingUSD.isEmpty() && !kingEUR.isEmpty() && !comercialBanksAvarageCambioUSD.isEmpty() && !comercialBanksAvarageCambioEUR.isEmpty() && npBankNamesState != null )
                        {
                            convert(inputCurrency,outputCurrency);

                            if(convertedValue != null)
                            {
                                outputKinguilaAmount.setText(convertedValue);
                                outputComercialAmount.setText(convertedValueComercial);
                                outputComercialAmountByBank.setText(convertedValueComercialByBank);


                                ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);
                                shareCambioConversionBtn.setImageResource(R.drawable.ic_share_24p);

                            }
                        }

                    }

                }


            }

        });

        btnToggleTo.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked)
            {

                MaterialButton btn = btnToggleTo.findViewById(checkedId);

                if(isChecked)
                {
                    outputCurrency = btn.getText().toString();

                    if(outputCurrency.equals("USD"))
                    {
                        outputCurrency = "Dolar";
                    }
                    if(outputCurrency.equals("EUR"))
                    {
                        outputCurrency = "Euro";
                    }
                    if(outputCurrency.equals("AKZ"))
                    {
                        outputCurrency = "Kwanza";
                    }
                }

                if(btn != null)
                {

                    if(isChecked && inputCurrency != null && outputCurrency != null && inputCurrencyAmount != null)
                    {

                        valueToConvertCopy = inputCurrencyAmount.getText().toString();
                        valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");


                        if(kingUSD != null && kingEUR!= null && comercialBanksAvarageCambioUSD != null && comercialBanksAvarageCambioEUR!= null
                                && !kingUSD.isEmpty() && !kingEUR.isEmpty() && !comercialBanksAvarageCambioUSD.isEmpty() && !comercialBanksAvarageCambioEUR.isEmpty()
                        && npBankNamesState != null)
                        {
                            convert(inputCurrency,outputCurrency);

                            if(convertedValue != null)
                            {
                                outputKinguilaAmount.setText(convertedValue);
                                outputComercialAmount.setText(convertedValueComercial);
                                outputComercialAmountByBank.setText(convertedValueComercialByBank);

                                ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);
                                shareCambioConversionBtn.setImageResource(R.drawable.ic_share_24p);

                            }
                        }


                    }
                }


            }

        });

    }


    public void goBackToMainActivity()
    {
        Intent intent;
        intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "From: Converter->CambioView");


        startActivity(intent);
    }

    public void convert(String currencyInput, String currencyOutput)
    {

        //Log.wtf("currencyInput",currencyInput);
        switch (currencyInput)
        {
            case "Dolar" :
                try {

                    dollarConverter(currencyOutput);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case "Euro" :
                try {

                    euroConverter(currencyOutput);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case "Kwanza" :
                try {

                    kzConverter(currencyOutput);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
        }
    }


    public void dollarConverter(String currency)
    {
        switch (currency)
        {
            case "Dolar" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                   convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercial = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercialByBank = passToDotNotation(Double.parseDouble(valueToConvert));

                    //Share Conversion
                    sharingCambioConversion("usd->usd",valueToConvertCopy,convertedValue,"1");


                    //Send Analytics event
                    utilities.sendAnalyticsEvent("Converter", "usd->usd");

                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";

                }


                break;
            case "Euro" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueEUR = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.kingUSD) / Double.parseDouble(this.kingEUR));
                    Double convertedValueComercialEUR = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.comercialBanksAvarageCambioUSD) / Double.parseDouble(this.comercialBanksAvarageCambioEUR));
                    Double convertedValueComercialByBankEUR = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Dolar")) / Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Euro")));

                    if(convertedValueEUR != null && convertedValueComercialEUR != null && convertedValueComercialByBankEUR != null)
                    {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueEUR);
                        convertedValueComercial = passToDotNotation(convertedValueComercialEUR);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankEUR);

                        Double rate = (Double.parseDouble(this.kingUSD) / Double.parseDouble(this.kingEUR));
                        rate = utilities.roundWithDecimalPlaces(rate,2);


                        //Share Conversion
                        sharingCambioConversion("usd->euro",valueToConvertCopy,convertedValue,rate.toString());

                        //Send Analytics event
                        utilities.sendAnalyticsEvent("Converter", "usd->euro");

                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
            case "Kwanza" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.kingUSD) / 1.0);
                    Double convertedValueComercialKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.comercialBanksAvarageCambioUSD) / 1.0);
                    Double convertedValueComercialByBankKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Dolar")) / 1.0);


                    if(convertedValueKz != null && convertedValueComercialKz != null && convertedValueComercialByBankKz != null)
                    {
                        //convertedValue = convertedValueKz.toString();
                        convertedValue = passToDotNotation(convertedValueKz);
                        convertedValueComercial = passToDotNotation(convertedValueComercialKz);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankKz);

                        //Share Conversion
                        sharingCambioConversion("usd->kz",valueToConvertCopy,convertedValue, this.kingUSD);

                        //Send Analytics event
                        utilities.sendAnalyticsEvent("Converter", "usd->kz");

                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
        }
    }

    public void euroConverter(String currency)
    {
        switch (currency)
        {
            case "Euro" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercial = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercialByBank = passToDotNotation(Double.parseDouble(valueToConvert));

                    //Share Conversion
                    sharingCambioConversion("euro->euro",valueToConvertCopy,convertedValue, "1");

                    utilities.sendAnalyticsEvent("Converter", "euro->euro");


                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";

                }


                break;
            case "Dolar" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueUSD = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.kingEUR) / Double.parseDouble(this.kingUSD));
                    Double convertedValueComercialUSD = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.comercialBanksAvarageCambioEUR ) / Double.parseDouble(this.comercialBanksAvarageCambioUSD));
                    Double convertedValueComercialByBankUSD = Double.parseDouble(this.valueToConvert)*(Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Euro") ) / Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Dolar")));

                    if(convertedValueUSD != null && convertedValueComercialUSD != null && convertedValueComercialByBankUSD != null)
                    {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueUSD);
                        convertedValueComercial = passToDotNotation(convertedValueComercialUSD);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankUSD);

                        Double rate = (Double.parseDouble(this.kingEUR) / Double.parseDouble(this.kingUSD));
                        rate = utilities.roundWithDecimalPlaces(rate,2);
                        //Share Conversion
                        sharingCambioConversion("euro->usd",valueToConvertCopy,convertedValue,  rate.toString());

                        utilities.sendAnalyticsEvent("Converter", "euro->usd");


                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
            case "Kwanza" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.kingEUR) / 1.0);
                    Double convertedValueComercialKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.comercialBanksAvarageCambioEUR) / 1.0);
                    Double convertedValueComercialByBankKz = Double.parseDouble(this.valueToConvert)*( Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Euro")) / 1.0);

                    if(convertedValueKz != null && convertedValueComercialKz != null && convertedValueComercialByBankKz != null)
                    {
                        //convertedValue = convertedValueKz.toString();
                        convertedValue = passToDotNotation(convertedValueKz);
                        convertedValueComercial = passToDotNotation(convertedValueComercialKz);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankKz);


                        //Share Conversion
                        sharingCambioConversion("euro->kz",valueToConvertCopy,convertedValue, this.kingEUR);

                        utilities.sendAnalyticsEvent("Converter", "euro->kz");


                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
        }
    }


    public void kzConverter(String currency)
    {
        switch (currency)
        {
            case "Kwanza" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercial = passToDotNotation(Double.parseDouble(valueToConvert));
                    convertedValueComercialByBank = passToDotNotation(Double.parseDouble(valueToConvert));


                    //Share Conversion
                    sharingCambioConversion("kz->kz",valueToConvertCopy,convertedValue, "1");

                    utilities.sendAnalyticsEvent("Converter", "kz->kz");


                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";

                }


                break;
            case "Dolar" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueUSD = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.kingUSD));
                    Double convertedValueComercialUSD = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.comercialBanksAvarageCambioUSD));
                    Double convertedValueComercialByBankUSD = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Dolar")));


                    if(convertedValueUSD != null && convertedValueComercialUSD!= null && convertedValueComercialByBankUSD != null)
                    {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueUSD);
                        convertedValueComercial = passToDotNotation(convertedValueComercialUSD);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankUSD);

                        Double rate = (1 / Double.parseDouble(this.kingUSD));
                        rate = utilities.roundWithDecimalPlaces(rate,5);

                        //Share Conversion
                        sharingCambioConversion("kz->usd",valueToConvertCopy,convertedValue, rate.toString());

                        utilities.sendAnalyticsEvent("Converter", "kz->usd");


                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
            case "Euro" :

                if(valueToConvert != null && !valueToConvert.isEmpty())
                {
                    Double convertedValueEUR = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.kingEUR));
                    Double convertedValueComercialEUR = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.comercialBanksAvarageCambioEUR));
                    Double convertedValueComercialByBankEUR = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.cambiosByBank.get(npBankNamesState+"->Euro")));

                    if(convertedValueEUR != null && convertedValueComercialEUR != null && convertedValueComercialByBankEUR != null)
                    {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueEUR);
                        convertedValueComercial = passToDotNotation(convertedValueComercialEUR);
                        convertedValueComercialByBank = passToDotNotation(convertedValueComercialByBankEUR );

                        Double rate = (1 / Double.parseDouble(this.kingEUR));
                        rate = utilities.roundWithDecimalPlaces(rate,5);

                        //Share Conversion
                        sharingCambioConversion("kz->euro",valueToConvertCopy,convertedValue, rate.toString());

                        utilities.sendAnalyticsEvent("Converter", "kz->euro");


                    }
                }else
                {
                    convertedValue = "";
                    convertedValueComercial = "";
                    convertedValueComercialByBank = "";
                }

                break;
        }
    }


    public String passToDotNotation(Double value)
    {
        return   String.format(Locale.GERMANY,"%,.2f",value); //New, GERMANY is closest to ANgola using "." eg. 1.000.000,00

        //OLD: return   String.format(Locale.FRANCE,"%,.2f",value); FRANCE uses spaces, eg. 1 000 000, 00.
    }

    public void getInputCurrencyAmount()
    {
        inputCurrencyAmount = (EditText) getActivity().findViewById(R.id.input_amount);

        //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

        inputCurrencyAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

               // Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if(inputCurrency != null && outputCurrency != null)
                {


                    valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");

                    if(!valueToConvert.isEmpty())
                    {
                        ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);
                        shareCambioConversionBtn.setVisibility(View.VISIBLE);
                        shareCambioConversionBtn.setImageResource(R.drawable.ic_share_24p);
                    }else
                        {
                            ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);
                            shareCambioConversionBtn.setImageResource(R.drawable.ic_share_24p);
                            shareCambioConversionBtn.setVisibility(View.GONE);

                        }



                    if(kingUSD != null && kingEUR!= null && comercialBanksAvarageCambioUSD != null && comercialBanksAvarageCambioEUR!= null
                            && !kingUSD.isEmpty() && !kingEUR.isEmpty() && !comercialBanksAvarageCambioUSD.isEmpty() && !comercialBanksAvarageCambioEUR.isEmpty()
                    && npBankNamesState != null)
                    {
                        convert(inputCurrency,outputCurrency);

                        if(convertedValue != null)
                        {
                            outputKinguilaAmount.setText(convertedValue);
                            outputComercialAmount.setText(convertedValueComercial);
                            outputComercialAmountByBank.setText(convertedValueComercialByBank);

                        }
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                inputCurrencyAmount.removeTextChangedListener(this);

                try {
                    String originalString = s.toString();

                    Double doubval;

                    if(!originalString.isEmpty())
                    {
                        if (originalString.contains("."))
                        {
                            originalString = originalString.replace(".","");

                        }


                        doubval = Double.parseDouble(originalString);

                        Locale locale  = new Locale("de","DE");
                        String pattern = "###,###";

                        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(locale);
                        formatter.applyPattern(pattern);

                        String formattedString = formatter.format(doubval);



                        //setting text after format to EditText
                        inputCurrencyAmount.setText(formattedString);
                        inputCurrencyAmount.setSelection(inputCurrencyAmount.getText().length());

                    }

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                inputCurrencyAmount.addTextChangedListener(this);

            }
        });

    }

    public void getCambioFromDb()
    {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Cambio cambio = dataSnapshot.getValue(Cambio.class);

                if(cambio != null)
                    getCambiosValues(cambio);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Cambio cambio = dataSnapshot.getValue(Cambio.class);

                if(cambio != null)
                    getCambiosValues(cambio);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getCambiosValues(Cambio cambio)
    {

        Double comercialBanksAvarageCambioUSD = 0.0;
        Double comercialBanksAvarageCambioEUR = 0.0;
        //Log.wtf("counter1", counter.toString());

        if(cambio != null)
        {
           // Log.wtf("counter2", counter.toString()+cambio.getBank()+cambio.getUsdValue());

            if(cambio.getBank().equals("KINGUILAS"))
            {
                kingUSD = cambio.getUsdValue();
                kingEUR = cambio.getEuroValue();

                //Log.d("counter1", counter.toString()+cambio.getBank());
               // Log.wtf("counter3", counter.toString()+cambio.getBank()+cambio.getUsdValue());


            }else
            {
                if(!cambio.getBank().equals("KINGUILAS") && !cambio.getBank().equals("BNA") && cambio.getUsdValue() != null && cambio.getEuroValue() != null)
                {

                    counter++;

                     this.comercialAverage.totalUSD += Double.parseDouble(cambio.getUsdValue());
                     this.comercialAverage.totalEUR += Double.parseDouble(cambio.getEuroValue());
                     this.comercialAverage.bankCounter = counter;

                    this.comercialAverage.averageUSD =  this.comercialAverage.totalUSD/ this.comercialAverage.bankCounter;
                    this.comercialAverage.averageEUR = this.comercialAverage.totalEUR / this.comercialAverage.bankCounter;


                    this.comercialBanksAvarageCambioUSD = Double.toString( this.utilities.roundWithDecimalPlaces(this.comercialAverage.averageUSD,1));
                    this.comercialBanksAvarageCambioEUR = Double.toString(this.utilities.roundWithDecimalPlaces(this.comercialAverage.averageEUR,1));


                    mapComercialCambiosByBank(cambio); //Fill up NumberPicker with Banks Names

                    //comercialBanksAvarageCambioUSD =  Double.parseDouble(cambio.getUsdValue()) + comercialBanksAvarageCambioUSD ;

                   // Log.wtf("counter4", counter.toString()+"---"+cambio.getBank()+"---"+"usdValue: "+cambio.getUsdValue()+"---"+"totalUSD: "+this.comercialAverage.totalUSD+"---"+"averageUSD: "+this.comercialAverage.averageUSD + "---"+
                          //  this.comercialAverage.averageEUR );
                }

            }
        }

    }

    public void mapComercialCambiosByBank(Cambio mCambio)
    {
        if(mCambio != null)
        {
            cambios.add(mCambio);

            //Map cambio vs bankName
            cambiosByBank.put(mCambio.getBank()+"->Dolar", mCambio.getUsdValue());
            cambiosByBank.put(mCambio.getBank()+"->Euro", mCambio.getEuroValue());

        }
    }

    public void getComercialBanks()
    {

        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                Long fbDataSize = dataSnapshot.getChildrenCount();

                for (DataSnapshot data : dataSnapshot.getChildren())
                {

                    if(data != null)
                    {
                        try
                        {
                            if(data.getValue(Bank.class) != null)
                            {
                                Bank bank = data.getValue(Bank.class);

                                if(bank != null)
                                {

                                    if(bank.getName() != null)
                                    {
                                        if(!bank.getName().equals("KINGUILAS") && !bank.getName().equals("BNA"))
                                        banksNames.add(bank.getName());

                                    }
                                }

                            }

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }

                }

                try {
                    if(banksNames.size() > 0)
                    {
                        String[] bankNames = banksNames.toArray(new String[banksNames.size()]);

                        picker.setMaxValue(bankNames.length-1);
                        picker.setDisplayedValues(bankNames);
                        picker.setWrapSelectorWheel(true);

                        npBankNamesState = "ATL"; // NUmber picker is initialized by ATL, so initial state is ATL
                    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void npProcessing()
    {
        //Set a value change listener for NumberPicker
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                //Display the newly selected number from picker
               //Log.d("picker",banksNames.get(oldVal)  + "--new: " + banksNames.get(newVal));

                npBankNamesState = banksNames.get(newVal);

                if(npBankNamesState!= null && inputCurrency!= null && inputCurrency!= convertedValueComercialByBank)
                {
                    convert(inputCurrency,outputCurrency);

                    if(convertedValue != null)
                    {
                        outputComercialAmountByBank.setText(convertedValueComercialByBank);

                        utilities.sendAnalyticsEvent("Bank", npBankNamesState);

                    }
                }


            }
        });
    }

    public void manageGooglMobAds()
    {

        refAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                    {

                        gMobAdsAtConverterState = "ON";


                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                        {
                            gMobAdsAtConverterState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtConverterType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtConverterType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtConverterState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getActivity()!= null && getTypeOfAd(gMobAdsAtConverterType)!= null)
                            adLayout = utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.VISIBLE,1,160);
                    }else
                    {
                        if(gMobAdsAtConverterState.equals("OFF"))
                        {
                            //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1, adLayout);
                            adLayout.setVisibility(View.GONE);
                        }
                    }

                   // Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);
                   // Log.wtf("WTF: "+dataSnapshot.getValue().toString(),dataSnapshot.getKey().toString());
                   // Log.wtf("gMobAdsAtConverterState: ", gMobAdsAtConverterState);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                {
                    if(dataSnapshot.getValue().equals("ON") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                    {
                        gMobAdsAtConverterState = "ON";

                    }else
                    {
                        if(dataSnapshot.getValue().equals("OFF") && dataSnapshot.getKey().equals("gMobAdsAtConverterState"))
                        {
                            gMobAdsAtConverterState = "OFF";

                        }
                    }

                    if(dataSnapshot.getKey().equals("gMobAdsAtConverterType"))
                    {
                        if(dataSnapshot.getValue().toString() != null)
                            gMobAdsAtConverterType = dataSnapshot.getValue().toString();
                    }

                    if(gMobAdsAtConverterState.equals("ON"))
                    {
                        adLayout.setVisibility(View.GONE);

                        if(getActivity()!= null && getTypeOfAd(gMobAdsAtConverterType)!= null)
                            adLayout = utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.VISIBLE,1,160);
                       // Log.wtf("ON: ", "Im BACK ON!!");

                    }else
                        {
                            if(gMobAdsAtConverterState.equals("OFF"))
                            {
                                //utilities.addAds(getActivity(), getTypeOfAd(gMobAdsAtConverterType), View.GONE,1,adLayout);
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

    public void sharingCambioConversion(String conversionType, String from, String to, String cambioToday)
    {

        ImageButton shareCambioConversionBtn = (ImageButton) getActivity().findViewById(R.id.share_conversion_btn);


        shareCambioConversionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "*MERCADO INFORMAL* \n" + "Conversão de " + conversionType + "\n"+
                        conversionType.split("->")[0] + " : " + from + ",00"+"\n" +
                        conversionType.split("->")[1] + " : " + to + "\n" +
                        "Valor do Câmbio : " + cambioToday + " " + conversionType.split("->")[1] + " / " +conversionType.split("->")[0] + "\n"+
                        "\n*Câmbio é no Cambiang*\n"
                                + "*Google Play:* " + "https://play.google.com/store/apps/details?id=com.cambiang.cambiang" + "\n\n"
                                + "*Apple Store:* " + "https://apps.apple.com/us/app/cambiang/id1472229549#?platform=iphone" + "\n\n"
                                + "*Web:* "+ "https://cambiang.com"

                        );

                        sendIntent.setType("text/plain");


                //Send Analytics event
                utilities.sendAnalyticsEvent("SharingCambio", conversionType);

                startActivity(Intent.createChooser(sendIntent,"send"));

            }
        });

    }

    //This Method is not working, "permission denied"
    private void takeScreenshot()
    {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();


            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        //Uri uri = Uri.fromFile(imageFile);

        Uri uriToImage = FileProvider.getUriForFile(getActivity().getApplicationContext(),"YES",imageFile);

        intent.setDataAndType(uriToImage, "image/*");
        startActivity(intent);
    }

    public boolean restoreSharedPreferences()
    {
        // Restore preferences

        if(settings != null)
        {
            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();
            String jsonCambiosLastUdpate = settings.getString("cambiosLastUpdate", "NOK");
            String jsonBanksArray = settings.getString("banksArray", "NOK");

            if(!jsonCambiosLastUdpate.equals("NOK") && jsonCambiosLastUdpate != null &&
                    !jsonBanksArray.equals("NOK") && jsonBanksArray != null)
            {
                cambiosLastUpdate =  Arrays.asList(gson.fromJson(jsonCambiosLastUdpate, Cambio[].class));
                banksArray = Arrays.asList(gson.fromJson(jsonBanksArray, Bank[].class));

                if(cambiosLastUpdate != null && banksArray != null)
                {
                    if(cambiosLastUpdate.size() > 0 && banksArray.size() > 0)
                    {
                        // Log.wtf("cambiosLastUpdate:", Integer.toString(cambiosLastUpdate.size()) + "--"+Integer.toString(banksArray.size()));
                        return true;
                    }
                }
            }

        }

        return false;

    }

    public void getCambiosValuesFromSharedPref()
    {

        Double comercialBanksAvarageCambioUSD = 0.0;
        Double comercialBanksAvarageCambioEUR = 0.0;

            for (Cambio cambio : cambiosLastUpdate)
            {
                if(cambio != null)
                {
                    // Log.wtf("counter2", counter.toString()+cambio.getBank()+cambio.getUsdValue());

                    if(cambio.getBank().equals("KINGUILAS"))
                    {
                        kingUSD = cambio.getUsdValue();
                        kingEUR = cambio.getEuroValue();

                        //Log.d("counter1", counter.toString()+cambio.getBank());
                        // Log.wtf("counter3", counter.toString()+cambio.getBank()+cambio.getUsdValue());


                    }else
                    {
                        if(!cambio.getBank().equals("KINGUILAS") && !cambio.getBank().equals("BNA") && cambio.getUsdValue() != null && cambio.getEuroValue() != null)
                        {

                            counter++;

                            this.comercialAverage.totalUSD += Double.parseDouble(cambio.getUsdValue());
                            this.comercialAverage.totalEUR += Double.parseDouble(cambio.getEuroValue());
                            this.comercialAverage.bankCounter = counter;

                            this.comercialAverage.averageUSD =  this.comercialAverage.totalUSD/ this.comercialAverage.bankCounter;
                            this.comercialAverage.averageEUR = this.comercialAverage.totalEUR / this.comercialAverage.bankCounter;


                            this.comercialBanksAvarageCambioUSD = Double.toString( this.utilities.roundWithDecimalPlaces(this.comercialAverage.averageUSD,1));
                            this.comercialBanksAvarageCambioEUR = Double.toString(this.utilities.roundWithDecimalPlaces(this.comercialAverage.averageEUR,1));


                            mapComercialCambiosByBank(cambio); //Fill up NumberPicker with Banks Names

                        }

                    }
                }
            }




    }

    public void getComercialBanksFromSharedPref()
    {

            for (Bank bank : banksArray)
            {
                if(bank != null)
                {

                    if(bank.getName() != null)
                    {
                        if(!bank.getName().equals("KINGUILAS") && !bank.getName().equals("BNA"))
                            banksNames.add(bank.getName());

                    }
                }
            }

            try {
                if(banksNames.size() > 0)
                {
                    String[] bankNames = banksNames.toArray(new String[banksNames.size()]);

                    picker.setMaxValue(bankNames.length-1);
                    picker.setDisplayedValues(bankNames);
                    picker.setWrapSelectorWheel(true);

                    npBankNamesState = "ATL"; // NUmber picker is initialized by ATL, so initial state is ATL
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }




    }


    public void generalManager()
    {

        if(restoreSharedPreferences())
        {
            getCambiosValuesFromSharedPref();

            getComercialBanksFromSharedPref();
        }else
            {
                getCambioFromDb();
                getComercialBanks();
            }


    }
}





class ComercialAverage
{
    Integer bankCounter;
    Double averageUSD;
    Double averageEUR;

    Double totalUSD;
    Double totalEUR;


    public  ComercialAverage()
    {
        bankCounter = 0;
        averageUSD = 0.0;
        averageEUR = 0.0;
        totalUSD = 0.0;
        totalEUR = 0.0;
    }

}

