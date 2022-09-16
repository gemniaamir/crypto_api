package com.go.cryptoapi.model;

public class SelectedCurrencyModel {

    private String symbol;
    private String rate;

    public SelectedCurrencyModel(String symbol, String rate) {
        this.symbol = symbol;
        this.rate = rate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
