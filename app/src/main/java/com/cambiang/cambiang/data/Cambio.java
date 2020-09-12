package com.cambiang.cambiang.data;


public class Cambio {
    //Dont need to serialize because variables in db are in camel typed already, but
    //as formalization serialization is done anyway

    private String id;

    private String bank;

    private String usdValue;
    private String euroValue;

    private String refDate;
    private  String usdChangeRate;
    private  String euroChangeRate;
    private  String usdArrowType;
    private  String euroArrowType;

    private  String usdValueBuying;
    private  String euroValueBuying;
    private  String usdChangeRateBuying;
    private  String euroChangeRateBuying;
    private  String usdArrowTypeBuying;
    private  String euroArrowTypeBuying;

    private String type;

    private String usdValuePrev;
    private String euroValuePrev;

    private  String usdValueBuyingPrev;
    private  String euroValueBuyingPrev;





            public String getId() {
                return id;
            }

    public void setId(String id) {
        this.id = id;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getUsdValue() {
        return usdValue;
    }

    public void setUsdValue(String usdValue) {
        this.usdValue = usdValue;
    }

    public String getEuroValue() {
        return euroValue;
    }

    public void setEuroValue(String euroValue) {
        this.euroValue = euroValue;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public String getUsdChangeRate() {
        return usdChangeRate;
    }

    public void setUsdChangeRate(String usdChangeRate) {
        this.usdChangeRate = usdChangeRate;
    }

    public String getEuroChangeRate() {
        return euroChangeRate;
    }

    public void setEuroChangeRate(String euroChangeRate) {
        this.euroChangeRate = euroChangeRate;
    }


    public String getUsdArrowType() {
        return usdArrowType;
    }

    public void setUsdArrowType(String usdArrowType) {
        this.usdArrowType = usdArrowType;
    }

    public String getEuroArrowType() {
        return euroArrowType;
    }

    public void setEuroArrowType(String euroArrowType) {
        this.euroArrowType = euroArrowType;
    }

    public String getUsdValueBuying() {
        return usdValueBuying;
    }

    public void setUsdValueBuying(String usdValueBuying) {
        this.usdValueBuying = usdValueBuying;
    }

    public String getEuroValueBuying() {
        return euroValueBuying;
    }

    public void setEuroValueBuying(String euroValueBuying) {
        this.euroValueBuying = euroValueBuying;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getUsdChangeRateBuying() {
        return usdChangeRateBuying;
    }

    public void setUsdChangeRateBuying(String usdChangeRateBuying) {
        this.usdChangeRateBuying = usdChangeRateBuying;
    }

    public String getEuroChangeRateBuying() {
        return euroChangeRateBuying;
    }

    public void setEuroChangeRateBuying(String euroChangeRateBuying) {
        this.euroChangeRateBuying = euroChangeRateBuying;
    }

    public String getUsdArrowTypeBuying() {
        return usdArrowTypeBuying;
    }

    public void setUsdArrowTypeBuying(String usdArrowTypeBuying) {
        this.usdArrowTypeBuying = usdArrowTypeBuying;
    }

    public String getEuroArrowTypeBuying() {
        return euroArrowTypeBuying;
    }

    public void setEuroArrowTypeBuying(String euroArrowTypeBuying) {
        this.euroArrowTypeBuying = euroArrowTypeBuying;
    }


    public String getUsdValueBuyingPrev() {
        return usdValueBuyingPrev;
    }

    public void setUsdValueBuyingPrev(String usdValueBuyingPrev) {
        this.usdValueBuyingPrev = usdValueBuyingPrev;
    }

    public String getEuroValueBuyingPrev() {
        return euroValueBuyingPrev;
    }

    public void setEuroValueBuyingPrev(String euroValueBuyingPrev) {
        this.euroValueBuyingPrev = euroValueBuyingPrev;
    }


    public String getUsdValuePrev() {
        return usdValuePrev;
    }

    public void setUsdValuePrev(String usdValuePrev) {
        this.usdValuePrev = usdValuePrev;
    }

    public String getEuroValuePrev() {
        return euroValuePrev;
    }

    public void setEuroValuePrev(String euroValuePrev) {
        this.euroValuePrev = euroValuePrev;
    }



}


