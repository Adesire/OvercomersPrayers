package com.overcomersprayers.app.overcomersprayers.models;

import androidx.annotation.NonNull;

class Response {
    String status;
    String chargecode;
    float appfee;
    float merchantfee;
    float amount;

}

public class RaveResponse {
    public Response data;

    public RaveResponse(){}

    public Response getData() {
        return data;
    }

    public String getStatus() {
        return data.status;
    }

    public String getChargecode() {
        return data.chargecode;
    }

    public float getAppfee() {
        return data.appfee;
    }

    public float getMerchantfee() {
        return data.merchantfee;
    }

    public float getAmount() {
        return data.amount;
    }

    @NonNull
    @Override
    public String toString() {
        return data.status;
    }
}

