package com.overcomersprayer.app.overcomersprayers.models;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Parcel(Parcel.Serialization.BEAN)
public class Prayer {
    private String id;
    private String heading;
    private String scriptures;
    private String instructions;
    private String Note;
    private String prayer52;
    private ArrayList<String> Days;
    private String prayerPointsPreview;
    private String prayerPoints;
    private boolean isLoader = false;


    public Prayer() {
    }

    public Prayer(String heading) {
        this.heading = heading;
    }

    public Prayer(boolean b) {
        this.isLoader = b;
    }

    public boolean isLoader() {
        return isLoader;
    }

    public void setLoader(boolean loader) {
        isLoader = loader;
    }

    public String getId() {
        return id;
    }

    public String getHeading() {
        return heading;
    }

    public String getScriptures() {
        return scriptures;
    }

    public String getPrayerPointsPreview() {
        return prayerPointsPreview;
    }

    public String getPrayerPoints() {
        return prayerPoints;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getNote() {
        return Note;
    }

    public String getPrayer52() {
        return prayer52;
    }

    public ArrayList<String> getDays() {
        return Days;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("heading", heading);
        map.put("scriptures", scriptures);
        map.put("instructions", instructions);
        map.put("Note", Note);
        map.put("prayer52", prayer52);
        map.put("Days", Days);
        return map;
    }
}
