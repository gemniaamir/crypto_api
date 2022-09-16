package com.go.cryptoapi.model;

import java.util.ArrayList;

public class CurrencyRateModel {

    private String date;

    private String base;

    private ArrayList<SelectedCurrencyModel> rates;

    public void setDate(String date){
        this.date = date;
    }
    public String getDate(){
        return this.date;
    }
    public void setBase(String base){
        this.base = base;
    }
    public String getBase(){
        return this.base;
    }

    public ArrayList<SelectedCurrencyModel> getRates() {
        return rates;
    }

    public void setRates(ArrayList<SelectedCurrencyModel> rates) {
        this.rates = rates;
    }

    public CurrencyRateModel(String date, String base, ArrayList<SelectedCurrencyModel> rates) {
        this.date = date;
        this.base = base;
        this.rates = rates;
    }
}
