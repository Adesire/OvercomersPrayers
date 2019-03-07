package com.overcomersprayers.app.overcomersprayers.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Parcel(Parcel.Serialization.BEAN)
public class Transactions {

    private String uid;
    private String trxRef;
    private String flwRef;
    private long date;
    private String status;
    private boolean wasSuccesful;
    private boolean hasBeenUpdated;
    private double amount;
    private String prayerId;
    private String raveRef;
    private String trxKey;

    public Transactions() {
    }

    public Transactions(String trxRef, double amount, String prayerId) {
        this.trxRef = trxRef;
        this.amount = amount;
        this.prayerId = prayerId;
        this.status = "Incomplete";
        this.wasSuccesful = false;
        this.hasBeenUpdated = false;
        this.uid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTrxRef() {
        return trxRef;
    }

    public void setTrxRef(String trxRef) {
        this.trxRef = trxRef;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPrayerId() {
        return prayerId;
    }

    public void setPrayerId(String prayerId) {
        this.prayerId = prayerId;
    }

    public String getTrxKey() {
        return trxKey;
    }

    public void setTrxKey(String trxKey) {
        this.trxKey = trxKey;
    }

    public String getRaveRef() {
        return raveRef;
    }

    public void setRaveRef(String raveRef) {
        this.raveRef = raveRef;
    }

    public String getFlwRef() {
        return flwRef;
    }

    public void setFlwRef(String flwRef) {
        this.flwRef = flwRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isWasSuccesful() {
        return wasSuccesful;
    }

    public void setWasSuccesful(boolean wasSuccesful) {
        this.wasSuccesful = wasSuccesful;
    }

    public boolean isHasBeenUpdated() {
        return hasBeenUpdated;
    }

    public void setHasBeenUpdated(boolean hasBeenUpdated) {
        this.hasBeenUpdated = hasBeenUpdated;
    }

    public static String getTableName() {
        return "transactions";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("trxRef", trxRef);
        data.put("date", ServerValue.TIMESTAMP);
        data.put("wasSuccesful", wasSuccesful);
        data.put("hasBeenUpdated", hasBeenUpdated);
        data.put("status", status);
        data.put("prayerId", prayerId);
        data.put("amount", amount);
        data.put("flwRef", flwRef);
        data.put("raveRef", raveRef);
        data.put("trxKey", trxKey);
        return data;
    }

}
