package com.go.cryptoapi.model;

public class ValueLineModel {
    String legend;
    float value;

    public ValueLineModel(String legend, float value) {
        this.legend = legend;
        this.value = value;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
