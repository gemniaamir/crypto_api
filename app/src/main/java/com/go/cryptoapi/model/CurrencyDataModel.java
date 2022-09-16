package com.go.cryptoapi.model;

public class CurrencyDataModel {

    private String id;

    private String rank;

    private String symbol;

    private String name;

    private String supply;

    private String maxSupply;

    private String marketCapUsd;

    private String volumeUsd24Hr;

    private String priceUsd;

    private String changePercent24Hr;

    private String vwap24Hr;

    private String explorer;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setRank(String rank){
        this.rank = rank;
    }
    public String getRank(){
        return this.rank;
    }
    public void setSymbol(String symbol){
        this.symbol = symbol;
    }
    public String getSymbol(){
        return this.symbol;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setSupply(String supply){
        this.supply = supply;
    }
    public String getSupply(){
        return this.supply;
    }
    public void setMaxSupply(String maxSupply){
        this.maxSupply = maxSupply;
    }
    public String getMaxSupply(){
        return this.maxSupply;
    }
    public void setMarketCapUsd(String marketCapUsd){
        this.marketCapUsd = marketCapUsd;
    }
    public String getMarketCapUsd(){
        return this.marketCapUsd;
    }
    public void setVolumeUsd24Hr(String volumeUsd24Hr){
        this.volumeUsd24Hr = volumeUsd24Hr;
    }
    public String getVolumeUsd24Hr(){
        return this.volumeUsd24Hr;
    }
    public void setPriceUsd(String priceUsd){
        this.priceUsd = priceUsd;
    }
    public String getPriceUsd(){
        return this.priceUsd;
    }
    public void setChangePercent24Hr(String changePercent24Hr){
        this.changePercent24Hr = changePercent24Hr;
    }
    public String getChangePercent24Hr(){
        return this.changePercent24Hr;
    }
    public void setVwap24Hr(String vwap24Hr){
        this.vwap24Hr = vwap24Hr;
    }
    public String getVwap24Hr(){
        return this.vwap24Hr;
    }
    public void setExplorer(String explorer){
        this.explorer = explorer;
    }
    public String getExplorer(){
        return this.explorer;
    }

    public CurrencyDataModel(String id, String rank, String symbol, String name, String supply, String maxSupply,
                             String marketCapUsd, String volumeUsd24Hr, String priceUsd, String changePercent24Hr,
                             String vwap24Hr, String explorer) {
        this.id = id;
        this.rank = rank;
        this.symbol = symbol;
        this.name = name;
        this.supply = supply;
        this.maxSupply = maxSupply;
        this.marketCapUsd = marketCapUsd;
        this.volumeUsd24Hr = volumeUsd24Hr;
        this.priceUsd = priceUsd;
        this.changePercent24Hr = changePercent24Hr;
        this.vwap24Hr = vwap24Hr;
        this.explorer = explorer;
    }
}
