package com.overcomersprayers.app.overcomersprayers.models;

import androidx.annotation.NonNull;

class Response {
    public String status;
    public String chargecode;
    public float appfee;
    public float merchantfee;
    public float amount;
}

public class RaveResponse {
    Response data;

    @NonNull
    @Override
    public String toString() {
        return data.status;
    }
}

