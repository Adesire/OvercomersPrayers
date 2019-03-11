package com.overcomersprayers.app.overcomersprayers.models;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

@Parcel(Parcel.Serialization.BEAN)
public class Prayer {
    private String id;
    private String heading;
    private String scriptures;
    private String prayerPointsPreview;
    private String prayerPoints;

    public Prayer() {
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("heading", heading);
        map.put("scriptures", scriptures);
        return map;
    }
}
