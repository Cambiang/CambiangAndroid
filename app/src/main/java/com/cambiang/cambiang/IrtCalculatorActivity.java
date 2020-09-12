package com.cambiang.cambiang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.cambiang.cambiang.data.Bank;
import com.cambiang.cambiang.data.Cambio;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class IrtCalculatorActivity extends AppCompatActivity {

    Toolbar toolbar;
    Utilities utilities = new Utilities();
    String senderTabPosition = "0";
    String KEY_EXTRA = "fragment";
    Boolean selfmadeBtnIsChecked = false;
    String baseSalaryInputAmountStr;
    String subsidiesInputAmountStr;
    String socialSecurity = "EMPTY";
    String irtSelfmadeTax = "EMPTY";
    DatabaseReference refIrtDb;
    DatabaseReference refAdmin;
    FirebaseDatabase database;
    ArrayList<IrtTax> irtTaxes = new ArrayList<>();
    TextView irtAmount;
    TextView socialSecurityAmount;
    TextView liquidSalaryAmount;
    EditText subsidiesInputAmountField;
    EditText baseSalaryInputAmountField;
    Switch switchAB;
    SharedPreferences settings;
    public static final String PREFS_NAME = "MIXED_DATA";
    public String IRT_TAXES_KEY = "irtTaxes";
    public String SOCIAL_SECURITY_KEY = "socialSecurity";
    public String SELFMADE_KEY = "selfmadeTax";
    LinearLayout adLayout;
    String gMobAdsAtWebViewType = "BANNER";

    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irt_calculator);

        utilities.getDefaultTracker(getApplicationContext()); //Analytics

        //Init
        this.initViews();

        //Set Ads Toolbar
        setToolbar();

        database = FirebaseDatabase.getInstance();
        refIrtDb = database.getReference("Taxs");
        refAdmin = database.getReference("Admin");

        settings = getSharedPreferences(PREFS_NAME, 0);

        //Show loading View
        utilities.loadingAnimation(View.VISIBLE,"chasingDots",this,R.id.spin_kit_irt);

        //Load data and do Calculation
        this.loadData();



        if (getIntent().hasExtra(this.KEY_EXTRA))
        {
            this.senderTabPosition = getIntent().getStringExtra(this.KEY_EXTRA);
        }
        else
        {
            //throw new IllegalArgumentException("Activity cannot find  extras " + this.KEY_EXTRA);
        }



        try
        {
            //Send first Analytics event before go to activity
            utilities.sendAnalyticsEvent("irt", "irtView");
            //Google Ads
            this.manageGooglMobAds();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    public void initViews()
    {
        this.irtAmount = (TextView) findViewById(R.id.irt_amount);
        this.socialSecurityAmount = (TextView) findViewById(R.id.social_security_amount);
        this.liquidSalaryAmount = (TextView) findViewById(R.id.salary_liquid);
        this.subsidiesInputAmountField = (EditText) findViewById(R.id.subsidies_input_amount);
        this.baseSalaryInputAmountField = (EditText) findViewById(R.id.base_salary_input_amount);
        this.switchAB = (Switch) findViewById(R.id.switchSelfmade);
        //init Ad view
        adLayout = new LinearLayout(this.getApplicationContext());

        this.subsidiesInputAmountStr = "0.0";
        this.baseSalaryInputAmountStr = "0.0";


        this.getBasaSalaryInputAmount();
        this.getSubsidiesInputAmount();
        this.selfmadeBtnSwitch();
    }

    @Override
    public void onBackPressed() {

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(this.KEY_EXTRA,this.senderTabPosition);
        //Send first Analytics event before go to activity
        utilities.sendAnalyticsEvent("LEFT", "LEFT");
        startActivity(intent);

    }

    //If back arrow is clicked on to go back to previews activity
    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(this.KEY_EXTRA,this.senderTabPosition);
        utilities.sendAnalyticsEvent("LEFT", "LEFT");


        startActivity(intent);

        return true;
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

    public void setToolbar()
    {

        toolbar = (Toolbar) findViewById(R.id.irt_calculator_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getResources().getString(R.string.irt_calculator));
        toolbar.setTitleTextColor(Color.WHITE);

    }

    public void getBasaSalaryInputAmount()
    {
       baseSalaryInputAmountField = (EditText) findViewById(R.id.base_salary_input_amount);

        //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

        baseSalaryInputAmountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if(baseSalaryInputAmountField != null)
                {
                    baseSalaryInputAmountStr = baseSalaryInputAmountField.getText().toString().replace(".","");

                    if(baseSalaryInputAmountStr != null)
                    {
                        doCalculation();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                baseSalaryInputAmountField.removeTextChangedListener(this);

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
                        baseSalaryInputAmountField.setText(formattedString);
                        baseSalaryInputAmountField.setSelection(baseSalaryInputAmountField.getText().length());

                    }

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                baseSalaryInputAmountField.addTextChangedListener(this);

            }
        });

    }

    public void getSubsidiesInputAmount()
    {
        subsidiesInputAmountField = (EditText) findViewById(R.id.subsidies_input_amount);

        //Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

        subsidiesInputAmountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Log.wtf("OninputCurrency",inputCurrencyAmount.getText().toString());

                if(subsidiesInputAmountField != null)
                {
                    subsidiesInputAmountStr = subsidiesInputAmountField.getText().toString().replace(".","");

                    if(subsidiesInputAmountStr != null)
                    {
                        doCalculation();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                subsidiesInputAmountField.removeTextChangedListener(this);

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
                        subsidiesInputAmountField.setText(formattedString);
                        subsidiesInputAmountField.setSelection(subsidiesInputAmountField.getText().length());

                    }

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                subsidiesInputAmountField.addTextChangedListener(this);

            }
        });

    }

    public void selfmadeBtnSwitch()
    {
        switchAB.setChecked(false);
        //Log.wtf("SWITCH","-");


        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Log.wtf("SWITCH","ON");

                selfmadeBtnIsChecked = isChecked;

                if (isChecked)
                {
                    //ON STATE
                    //Log.wtf("SWITCH","ON");
                    subsidiesInputAmountField.setEnabled(false);
                    doCalculation();
                }
                else
                {
                    //OFF STATE
                    //Log.wtf("SWITCH","OFF");
                    subsidiesInputAmountField.setEnabled(true);
                    doCalculation();

                }
            }
        });
    }


    public  void loadData()
    {
        if(!this.loadDataLocally())
        {
            loadDataFromDB();
        }
        //Data Change Listner
        this.loadTaxesOnChange();

        //this.deleteLocalData();

    }
    public void loadDataFromDB()
    {
        this.loadTaxes();
    }

    public boolean loadDataLocally()
    {
        if(this.restoreSharedPreferences(IRT_TAXES_KEY) != null)
        {
            ArrayList<IrtTax> irttaxesLocal = this.restoreSharedPreferences(IRT_TAXES_KEY);

            String socialSecurityLocal = this.getDataSingleValueLocally(SOCIAL_SECURITY_KEY);
            String selfmadeTaxLocal = this.getDataSingleValueLocally(SELFMADE_KEY);

            if(irttaxesLocal != null && socialSecurityLocal != null && selfmadeTaxLocal != null)
            {
                if(irttaxesLocal.size() > 0 && selfmadeTaxLocal.length() > 0 && selfmadeTaxLocal.length() > 0)
                {
                    if(!selfmadeTaxLocal.equals("NOK") && selfmadeTaxLocal.equals("NOK"))
                    {
                        utilities.loadingAnimation(View.GONE,"chasingDots",this,R.id.spin_kit_irt);

                        //remove background
                        LinearLayout imgBackg = (LinearLayout) findViewById(R.id.mybackg_irt);
                        imgBackg.setVisibility(View.GONE);

                        return true;
                    }

                }else
                {
                    return false;
                }

            }
        }


        return false;
    }

    public void loadTaxes()
    {
        this.refIrtDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot != null)
                {
                    Long dataSize = dataSnapshot.getChildrenCount();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {

                        if(data != null)
                        {
                            try
                            {
                                if(data.getValue(IrtTax.class) != null)
                                {
                                    IrtTax irtTax = data.getValue(IrtTax.class);

                                    if(irtTax != null)
                                    {
                                        if(!irtTaxes.contains(irtTax))
                                        {
                                            irtTaxes.add(irtTax);
                                            storeSharedPreferencesIrtTaxesList(irtTaxes,IRT_TAXES_KEY);
                                            loadSocialSecurityAndSelfmade();

                                            //Log.wtf("SIZE:",irtTaxes.size() + "--" + dataSize);
                                            if(irtTaxes.size() == dataSize)
                                            {
                                                utilities.loadingAnimation(View.GONE,"chasingDots",getParent(),R.id.spin_kit);
                                                //remove background
                                                LinearLayout imgBackg = (LinearLayout) findViewById(R.id.mybackg_irt);
                                                imgBackg.setVisibility(View.GONE);
                                            }

                                        }
                                    }

                                }

                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadTaxesOnChange()
    {
        this.refIrtDb.addChildEventListener(new ChildEventListener() {

            //NOT USED - ONY CHECK FOR CHANGES
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }


            //USED
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.getValue(IrtTax.class) != null)
                {
                    IrtTax irttax = dataSnapshot.getValue(IrtTax.class);

                    if(irtTaxes != null)
                    {
                        if(irtTaxes.size() > 0)
                        {
                            if(!irtTaxes.contains(irttax))
                            {
                                irtTaxes.add(irttax);

                                storeSharedPreferencesIrtTaxesList(irtTaxes,IRT_TAXES_KEY);
                            }
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getValue(IrtTax.class) != null)
                {
                    IrtTax irttax = dataSnapshot.getValue(IrtTax.class);

                    if(irtTaxes != null)
                    {
                        if(irtTaxes.size() > 0)
                        {
                            if(irtTaxes.contains(irttax))
                            {
                                irtTaxes.remove(irttax);

                                storeSharedPreferencesIrtTaxesList(irtTaxes,IRT_TAXES_KEY);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadSocialSecurityAndSelfmade()
    {

        this.refAdmin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(irtSelfmadeTax.equals("EMPTY") || socialSecurity.equals("EMPTY"))
                {
                    for (DataSnapshot data:dataSnapshot.getChildren())
                    {
                        if(data.getKey().equals(SOCIAL_SECURITY_KEY))
                        {
                            if(data.getValue() != null)
                            {
                                socialSecurity = data.getValue().toString();
                                //Log.wtf("socialSecurity:",socialSecurity);

                                saveSingleDataValueLocally(socialSecurity,SOCIAL_SECURITY_KEY);

                            }
                        }
                        if(data.getKey().equals(SELFMADE_KEY))
                        {
                            if(data.getValue() != null)
                            {
                                irtSelfmadeTax = data.getValue().toString();
                                saveSingleDataValueLocally(irtSelfmadeTax,SELFMADE_KEY);

                                //Log.wtf("selfmadeTax:",irtSelfmadeTax);

                            }
                        }

                    }
                }else
                    {
                        doCalculation();
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void doCalculation()
    {
        if(this.baseSalaryInputAmountStr != null && this.subsidiesInputAmountStr != null)
        {
            if(!this.selfmadeBtnIsChecked)
            {
                if(!this.baseSalaryInputAmountStr.isEmpty())
                {
                    ArrayList<IrtTax> excessArray  = new ArrayList<>();

                    Double materiaColectavel = this.utilities.stringToDouble(this.baseSalaryInputAmountStr) * (1 - this.utilities.stringToDouble(this.socialSecurity) / 100.0 /*SS is in[%]*/) + this.utilities.stringToDouble(this.subsidiesInputAmountStr);

                    if(this.irtTaxes.size() > 0)
                    {
                        for(IrtTax irttax : this.irtTaxes)
                        {
                            Double excess = materiaColectavel - this.utilities.stringToDouble(irttax.getLowerLimit());

                            if(excess > 0.0)
                            {
                                // the tax we looking for is the last one which excess is above ZERO
                                excessArray.add(irttax);
                            }
                        }
                    }

                    //get last element
                    if(excessArray.size() > 0)
                    {
                        IrtTax searchedIrtTax = excessArray.get(excessArray.size()-1);

                        if(searchedIrtTax != null)
                        {
                            if(!searchedIrtTax.tax.isEmpty())
                            {
                                Double irt = this.utilities.stringToDouble(searchedIrtTax.fixedParcel) +

                                        (materiaColectavel - this.utilities.stringToDouble(searchedIrtTax.lowerLimit))
                                                * this.utilities.stringToDouble(searchedIrtTax.tax)/100.0;

                                Double socialSecurityPaid = this.utilities.stringToDouble(this.baseSalaryInputAmountStr)
                                        * this.utilities.stringToDouble(this.socialSecurity)/100.0;

                                Double liquidSalary = materiaColectavel - irt;

                                if(irt != null && socialSecurityPaid != null && liquidSalary != null)
                                {
                                    this.irtAmount.setText(this.utilities.doubleToFormattedCurrencyString(irt)  + " AKZ ");
                                    this.socialSecurityAmount.setText(this.utilities.doubleToFormattedCurrencyString(socialSecurityPaid)  + " AKZ ");
                                    this.liquidSalaryAmount.setText(this.utilities.doubleToFormattedCurrencyString(liquidSalary)  + " AKZ ");

                                }


                                if(this.utilities.stringToDouble(this.baseSalaryInputAmountStr) > 70000)
                                {
                                    //Google Analytics
                                    utilities.sendAnalyticsEvent("irtSalaryBase", this.baseSalaryInputAmountStr);
                                    utilities.sendAnalyticsEvent("irtSubsidy", this.subsidiesInputAmountStr);

                                }


                            }
                        }

                    }


                }
            }else
                {
                    if(!this.baseSalaryInputAmountStr.isEmpty())
                    {

                            //Work by himself
                            Double irt = 0.0;
                            //print("Checks: \(self.utilities.stringToDouble(str: self.irtSelfmadeTax)/100.0) - \(self.irtSelfmadeTax)")

                            if(this.utilities.stringToDouble(this.baseSalaryInputAmountStr) > 70000)
                            {
                                //Work by himself
                                irt = this.utilities.stringToDouble(this.baseSalaryInputAmountStr) * this.utilities.stringToDouble(this.irtSelfmadeTax)/100.0;

                                utilities.sendAnalyticsEvent("irtSalaryBase", this.baseSalaryInputAmountStr);

                            }

                            Double socialSecurityPaid = 0.0;

                            Double liquidSalary = this.utilities.stringToDouble(baseSalaryInputAmountStr) - irt;

                            if(irt != null && socialSecurityPaid != null && liquidSalary != null)
                            {
                                this.irtAmount.setText(this.utilities.doubleToFormattedCurrencyString(irt)  + " AKZ ");
                                this.socialSecurityAmount.setText(this.utilities.doubleToFormattedCurrencyString(socialSecurityPaid)  + " AKZ ");
                                this.liquidSalaryAmount.setText(this.utilities.doubleToFormattedCurrencyString(liquidSalary)  + " AKZ ");

                            }
                    }

                }
        }

    }

    public void storeSharedPreferencesIrtTaxesList(ArrayList<IrtTax> irtTaxesList, String dataKey)
    {
        if(irtTaxesList != null && dataKey != null)
        {
            if(irtTaxesList.size() > 0 )
            {
                //Set the values
                if(settings != null)
                {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();


                    String jsonIrtTaxes = gson.toJson(irtTaxesList);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(dataKey,jsonIrtTaxes);

                    // Commit the edits!
                    //editor.commit();
                    editor.apply();
                }


            }
        }

    }

    public ArrayList<IrtTax> restoreSharedPreferences(String dataKey)
    {

        ArrayList<IrtTax> ret = new ArrayList<>();
        // Restore preferences
        List<IrtTax> irtTaxesList = new ArrayList<>();


        if(settings != null && dataKey != null)
        {
            //if some preference does not exist returns NOT OK - NOK
            Gson gson = new Gson();

            if(settings.getString(dataKey, "NOK") != null)
            {
                String jsonIrtTaxesList = settings.getString(dataKey, "NOK");

                if(!jsonIrtTaxesList.equals("NOK") && !jsonIrtTaxesList.isEmpty())
                {
                    if(Arrays.asList(gson.fromJson(jsonIrtTaxesList, IrtTax[].class)) != null)
                    {
                        irtTaxesList =  Arrays.asList(gson.fromJson(jsonIrtTaxesList, IrtTax[].class));
                        ret = new ArrayList<>(irtTaxesList);
                    }
                }
            }

        }

        return ret;
    }


    public String getDataSingleValueLocally(String keyName)
    {

        if(keyName != null && PREFS_NAME != null)
        {
            // Restore preferences
            settings = getSharedPreferences(PREFS_NAME, 0);

            if(settings != null)
            {
                //if some preference does not exist returns NOT OK - NOK
                String localPropertyValue = settings.getString(keyName, "NOK");

                if(localPropertyValue != null)
                    return  localPropertyValue;
            }
        }

        return "EMPTY";
    }


    public void saveSingleDataValueLocally(String newValue,String keyName)
    {
        if(newValue != null && keyName != null && PREFS_NAME != null)
        {
            settings = getSharedPreferences(PREFS_NAME, 0);

            if(settings != null)
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(keyName,newValue);
                // Commit the edits!
                editor.commit();
            }
        }

    }


    public void deleteLocalData()
    {

        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(IRT_TAXES_KEY,"");
        editor.commit();


        saveSingleDataValueLocally("",SOCIAL_SECURITY_KEY);
        saveSingleDataValueLocally("",SELFMADE_KEY);
    }


    public void manageGooglMobAds()
    {
        adLayout = utilities.addAds(IrtCalculatorActivity.this, utilities.getTypeOfAd(gMobAdsAtWebViewType), View.VISIBLE,1,0);
    }

}

