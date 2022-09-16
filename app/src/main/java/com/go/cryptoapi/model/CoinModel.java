package com.go.cryptoapi.model;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class CoinModel {

    private String coinName;
    private String coinIcon;
    private String rank;

    public CoinModel(String coinName, String coinIcon, String ID) {
        this.coinName = coinName;
        this.coinIcon = coinIcon;
        this.rank = ID;
    }

    public CoinModel(String coinName, String coinIcon) {
        this.coinName = coinName;
        this.coinIcon = coinIcon;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinIcon() {
        return coinIcon;
    }

    public void setCoinIcon(String coinIcon) {
        this.coinIcon = coinIcon;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
