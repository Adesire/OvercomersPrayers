package com.overcomersprayers.app.overcomersprayers.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Prayer {
    private String id;
    private String heading;
    private String bibleReference;
    private String prayerPointsPreview;
    private String prayerPoints;

    public String getId() {
        return id;
    }

    public String getHeading() {
        return heading;
    }

    public String getBibleReference() {
        return bibleReference;
    }

    public String getPrayerPointsPreview() {
        return prayerPointsPreview;
    }

    public String getPrayerPoints() {
        return prayerPoints;
    }
}
