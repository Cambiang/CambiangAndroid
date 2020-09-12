package com.cambiang.cambiang;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cambiang.cambiang.data.Cambio;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class SimulatorFragment extends Fragment {
    Toolbar toolbar;
    Utilities utilities;

   String inputAmountUsdEurRateStr;
    String simulationRate;
    String valueToConvert;
    String convertedValue;
    String convertedValueComercial;
    String inputCurrency;
    String inputCurrencyRateSimul;
    String outputCurrency;
    Spinner inputCorrencySpinner;
    Spinner inputCorrencyRateSimulSpinner;
    Spinner outputCorrencySpinner;
    EditText inputCurrencyAmount;
    EditText inputCurrencyAmountKzRate;
    EditText inputAmountUsdEurRate;
    TextView outputSimulationAmount;

    FirebaseDatabase database;
    DatabaseReference ref;
    Integer counter = 0;


    public SimulatorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_simulator, container, false);

    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);

        try {

            utilities = new Utilities();

            utilities.getDefaultTracker(getActivity().getApplicationContext()); //Analytics

            this.simulationRate = new String();

            this.inputAmountUsdEurRateStr = new String();

            // Write a message to the database
            database = FirebaseDatabase.getInstance();
            ref = database.getReference("CambiosLastUpdates");


            getCambioSimulation();


            outputSimulationAmount = (TextView) getActivity().findViewById(R.id.output_informal_amount_simul);
            outputSimulationAmount = (TextView) getActivity().findViewById(R.id.output_informal_amount_simul);


            //init spinners
            initSpinners();


            getInputCurrencyAmount();

            //Send first Analytics event before go to activity
            utilities.sendAnalyticsEvent("Arrival", "Simulator");

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    public String currencyChoicesEqualizer(String inputCurrency)
    {
        switch (inputCurrency)
        {
            case "USD":
                return "Dolar";

            case "EUR":
                return "Euro";

            case "AKZ":
                return "Kwanza";
        }

        return "";
    }


    public void initSpinners() {
        this.inputCorrencySpinner = (Spinner) getActivity().findViewById(R.id.input_currency_simul);
        this.outputCorrencySpinner = (Spinner) getActivity().findViewById(R.id.output_currency_simul);
        this.inputCorrencyRateSimulSpinner = (Spinner) getActivity().findViewById(R.id.input_currency_rate_simul);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_usd_euro, R.layout.spinner_item);

        // specify layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item);

        //Apply adapter to the spinners
        inputCorrencyRateSimulSpinner.setAdapter(adapter);

        this.inputCorrencyRateSimulSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                inputCurrencyRateSimul = parent.getItemAtPosition(position).toString();

                //Log.d("inputCurrency ",inputCurrency );

                if (inputCurrencyRateSimul != null)
                {
                    switch (inputCurrencyRateSimul)
                    {
                        case "USD":
                            ArrayAdapter<CharSequence> adapterUSD = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_usd, R.layout.spinner_item);
                            inputCorrencySpinner.setAdapter(adapterUSD);

                            ArrayAdapter<CharSequence> adapterKZUSD = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_kz_usd, R.layout.spinner_item);
                            outputCorrencySpinner.setAdapter(adapterKZUSD);
                            break;

                        case "EUR":
                            ArrayAdapter<CharSequence> adapterEUR = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_euro, R.layout.spinner_item);
                            inputCorrencySpinner.setAdapter(adapterEUR);

                            ArrayAdapter<CharSequence> adapterKZEUR = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_kz_euro, R.layout.spinner_item);
                            outputCorrencySpinner.setAdapter(adapterKZEUR);                            break;

                        case "AKZ":
                            ArrayAdapter<CharSequence> adapterKZ = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.currency_choices_kz, R.layout.spinner_item);
                            inputCorrencySpinner.setAdapter(adapterKZ);
                            outputCorrencySpinner.setAdapter(adapterKZ);
                            break;
                    }


                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        this.inputCorrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                inputCurrency = parent.getItemAtPosition(position).toString();
                //Log.d("inputCurrency ",inputCurrency );

                if (inputCurrency != null && outputCurrency != null && inputCurrencyAmount != null) {

                    //valueToConvert = inputCurrencyAmount.getText().toString();
                    valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");


                    if (simulationRate != null && !simulationRate.isEmpty()) {
                        convert(inputCurrency, outputCurrency);

                        if (convertedValue != null) {
                            outputSimulationAmount.setText(convertedValue);
                        }
                    }

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        this.outputCorrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                outputCurrency = parent.getItemAtPosition(position).toString();

                //Log.d("outputCurrency",outputCurrency);

                if (inputCurrency != null && outputCurrency != null && inputCurrencyAmount != null) {
                    //valueToConvert = inputCurrencyAmount.getText().toString();
                    valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");


                    if (simulationRate != null &&  !simulationRate.isEmpty()) {
                        convert(inputCurrency, outputCurrency);

                        if (convertedValue != null) {
                            outputSimulationAmount.setText(convertedValue);
                        }
                    }


                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void goBackToMainActivity() {
        Intent intent;
        intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("Arrival", "From: Simulator->CambioView");


        startActivity(intent);
    }


    public void convert(String currencyInput, String currencyOutput) {
        switch (currencyInput) {
            case "USD":
                try {
                    dollarConverter(currencyOutput);
                    //Send Analytics event
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case "EUR":
                try {
                    euroConverter(currencyOutput);
                    //Send Analytics event
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case "AKZ":
                try {
                    kzConverter(currencyOutput);
                    //Send Analytics event

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                break;
        }
    }


    public void dollarConverter(String currency) {
        switch (currency) {
            case "USD":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));

                    //Send Analytics event
                    utilities.sendAnalyticsEvent("Simulator", "usd->usd");

                } else {
                    convertedValue = "";

                }


                break;
            case "EUR":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueEUR = Double.parseDouble(this.valueToConvert) * (Double.parseDouble(this.simulationRate) / Double.parseDouble(this.simulationRate));

                    if (convertedValueEUR != null) {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueEUR);

                        //Send Analytics event
                        utilities.sendAnalyticsEvent("Simulator", "usd->euro");

                    }
                } else {
                    convertedValue = "";
                }

                break;
            case "AKZ":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueKz = Double.parseDouble(this.valueToConvert) * (Double.parseDouble(this.simulationRate) / 1.0);


                    if (convertedValueKz != null) {
                        //convertedValue = convertedValueKz.toString();
                        convertedValue = passToDotNotation(convertedValueKz);

                        //Send Analytics event
                        utilities.sendAnalyticsEvent("Simulator", "usd->kz");

                    }
                } else {
                    convertedValue = "";
                }

                break;
        }
    }

    public void euroConverter(String currency) {
        switch (currency) {
            case "EUR":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));
                    utilities.sendAnalyticsEvent("Simulator", "euro->euro");


                } else {
                    convertedValue = "";

                }


                break;
            case "USD":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueUSD = Double.parseDouble(this.valueToConvert) * (Double.parseDouble(this.simulationRate) / Double.parseDouble(this.simulationRate));

                    if (convertedValueUSD != null) {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueUSD);
                        utilities.sendAnalyticsEvent("Simulator", "euro->usd");


                    }
                } else {
                    convertedValue = "";
                }

                break;
            case "AKZ":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueKz = Double.parseDouble(this.valueToConvert) * (Double.parseDouble(this.simulationRate) / 1.0);


                    if (convertedValueKz != null) {
                        //convertedValue = convertedValueKz.toString();
                        convertedValue = passToDotNotation(convertedValueKz);
                        utilities.sendAnalyticsEvent("Simulator", "euro->kz");


                    }
                } else {
                    convertedValue = "";
                }

                break;
        }
    }


    public void kzConverter(String currency) {
        switch (currency) {
            case "AKZ":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    convertedValue = passToDotNotation(Double.parseDouble(valueToConvert));
                    utilities.sendAnalyticsEvent("Simulator", "kz->kz");


                } else {
                    convertedValue = "";
                }


                break;
            case "USD":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueUSD = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.simulationRate));

                    if (convertedValueUSD != null) {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueUSD);
                        utilities.sendAnalyticsEvent("Simulator", "kz->usd");

                    }
                } else {
                    convertedValue = "";
                }

                break;
            case "EUR":

                if (valueToConvert != null && !valueToConvert.isEmpty()) {
                    Double convertedValueEUR = Double.parseDouble(this.valueToConvert) * (1.0 / Double.parseDouble(this.simulationRate));

                    if (convertedValueEUR != null) {
                        //convertedValue = convertedValueEUR.toString();
                        convertedValue = passToDotNotation(convertedValueEUR);
                        utilities.sendAnalyticsEvent("Simulator", "kz->euro");

                    }
                } else {
                    convertedValue = "";
                }

                break;
        }
    }


    public String passToDotNotation(Double value)
    {
        return   String.format(Locale.GERMANY,"%,.2f",value); //New, GERMANY is closest to ANgola using "." eg. 1.000.000,00

        //OLD: return   String.format(Locale.FRANCE,"%,.2f",value); FRANCE uses spaces, eg. 1 000 000, 00.
    }


    public void getInputCurrencyAmount() {
        inputCurrencyAmount = (EditText) getActivity().findViewById(R.id.input_amount_simul);

        ////Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

        inputCurrencyAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if (inputCurrency != null && outputCurrency != null)
                {
                    //valueToConvert = inputCurrencyAmount.getText().toString();
                    valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");


                    if (simulationRate != null && simulationRate != null && !simulationRate.isEmpty() && !simulationRate.isEmpty()) {
                        convert(inputCurrency, outputCurrency);

                        if (convertedValue != null) {
                            outputSimulationAmount.setText(convertedValue);
                        }
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s)
            {
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

    public void getCambioUsdEuro()
    {


        inputAmountUsdEurRate = (EditText) getActivity().findViewById(R.id.input_amount_usd_euro_simul);


        ////Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

        inputAmountUsdEurRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if (inputAmountUsdEurRate.getText().toString() != null && !inputAmountUsdEurRate.getText().toString().isEmpty())
                {

                   // inputAmountUsdEurRateStr = inputAmountUsdEurRate.getText().toString();
                    inputAmountUsdEurRateStr  = inputAmountUsdEurRate.getText().toString().replace(".","");


                    ////Log.wtf("usdEuroRate",inputAmountUsdEurRateStr);

                    if (inputCurrencyAmountKzRate.getText().toString() != null && !inputCurrencyAmountKzRate.getText().toString().isEmpty()
                            && inputAmountUsdEurRateStr != null && !inputAmountUsdEurRateStr.isEmpty())
                    {

                        try {

                            String inputCurrencyAmountKzRateStr = inputCurrencyAmountKzRate.getText().toString().replace(".","");

                            if(inputCurrencyAmountKzRateStr != null)
                            {
                                Double simul =  Double.parseDouble(inputCurrencyAmountKzRateStr) / Double.parseDouble(inputAmountUsdEurRateStr);
                                ////Log.wtf("simulation__x",inputCurrencyAmountKzRate.getText().toString()+ " -- " +inputAmountUsdEurRateStr);


                                if(simul.toString() != null)
                                {
                                    simulationRate = simul.toString();

                                    ////Log.wtf("simulationx",simulationRate);

                                    convertOnChange();
                                }

                            }

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }


                }

            }
            @Override
            public void afterTextChanged(Editable s)
            {
                inputAmountUsdEurRate.removeTextChangedListener(this);

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
                        inputAmountUsdEurRate.setText(formattedString);
                        inputAmountUsdEurRate.setSelection(inputAmountUsdEurRate.getText().length());
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                inputAmountUsdEurRate.addTextChangedListener(this);


            }
        });

    }






    public void getCambiosValues()
    {


        inputCurrencyAmountKzRate = (EditText) getActivity().findViewById(R.id.input_amount_kz_rate);

        inputCurrencyAmountKzRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if (inputCurrencyAmountKzRate.getText().toString() != null && !inputCurrencyAmountKzRate.getText().toString().isEmpty()
                       && inputAmountUsdEurRateStr != null && !inputAmountUsdEurRateStr.isEmpty())
                {

                    try {

                        inputAmountUsdEurRateStr  = inputAmountUsdEurRate.getText().toString().replace(".","");

                        if(inputCurrencyAmountKzRate.getText().toString().replace(".","") != null &&
                                inputAmountUsdEurRate.getText().toString().replace(".","") != null)
                        {
                            String inputCurrencyAmountKzRatStr = inputCurrencyAmountKzRate.getText().toString().replace(".","");



                            if(inputCurrencyAmountKzRatStr != null)
                            {

                                Double simul =  Double.parseDouble(inputCurrencyAmountKzRatStr) / Double.parseDouble(inputAmountUsdEurRateStr);
                                //Log.wtf("simulation__1",inputCurrencyAmountKzRate.getText().toString()+ " -- " +inputAmountUsdEurRateStr);


                                if(simul.toString() != null)
                                {
                                    simulationRate = simul.toString();

                                    //Log.wtf("simulation",simulationRate);

                                    convertOnChange();
                                }

                            }

                        }

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }


                }

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                inputCurrencyAmountKzRate.removeTextChangedListener(this);

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
                        inputCurrencyAmountKzRate.setText(formattedString);
                        inputCurrencyAmountKzRate.setSelection(inputCurrencyAmountKzRate.getText().length());
                    }

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                inputCurrencyAmountKzRate.addTextChangedListener(this);
            }
        });



    }


    public void getCambioSimulation()
    {
        getCambioUsdEuro();
        getCambiosValues();



    }


    public void convertOnChange()
    {

        if (inputCurrency != null && outputCurrency != null) {
            //valueToConvert = inputCurrencyAmount.getText().toString();
            valueToConvert = inputCurrencyAmount.getText().toString().replace(".","");

            if (simulationRate != null && simulationRate != null && !simulationRate.isEmpty() && !simulationRate.isEmpty()
                && inputCurrency != null && !inputCurrency.isEmpty() && outputCurrency != null && !outputCurrency.isEmpty())
            {
                convert(inputCurrency, outputCurrency);

                if (convertedValue != null) {
                    outputSimulationAmount.setText(convertedValue);
                }
            }

        }
    }

}