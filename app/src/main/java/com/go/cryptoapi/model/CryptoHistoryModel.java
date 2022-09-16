package com.go.cryptoapi.model;

public class CryptoHistoryModel {
    private String priceUsd;
    private int time;
    private String date;

    public void setPriceUsd(String priceUsd){
        this.priceUsd = priceUsd;
    }
    public String getPriceUsd(){
        return this.priceUsd;
    }
    public void setTime(int time){
        this.time = time;
    }
    public int getTime(){
        return this.time;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getDate(){
        return this.date;
    }

    public CryptoHistoryModel(String priceUsd, int time, String date) {
        this.priceUsd = priceUsd;
        this.time = time;
        this.date = date;
    }
}
