package com.overcomersprayer.app.overcomersprayers.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ListOfCategoriesWithHeading {
    private List<CategoryWithHeadings> categoryWithHeadingsList;

    public ListOfCategoriesWithHeading(List<CategoryWithHeadings> categoryWithHeadingsList) {
        this.categoryWithHeadingsList = categoryWithHeadingsList;
    }

    public List<CategoryWithHeadings> getCategoryWithHeadingsList() {
        return categoryWithHeadingsList;
    }

    public static class CategoryWithHeadings extends ExpandableGroup<PrayerHeadings> {
        private String category;
        private List<PrayerHeadings> prayerHeadings;

        public CategoryWithHeadings(String category, List<PrayerHeadings> prayerHeadings) {
            super(category, prayerHeadings);
            this.category = category;
            this.prayerHeadings = prayerHeadings;
        }

        public String getCategory() {
            return category;
        }

        public List<PrayerHeadings> getPrayerHeadings() {
            return prayerHeadings;
        }
    }

    public static class PrayerHeadings implements Parcelable {
        private String heading;

        public PrayerHeadings(String heading) {
            this.heading = heading;
        }

        protected PrayerHeadings(Parcel in) {
            heading = in.readString();
        }

        public String getHeading() {
            return heading;
        }

        public static final Creator<PrayerHeadings> CREATOR = new Creator<PrayerHeadings>() {
            @Override
            public PrayerHeadings createFromParcel(Parcel in) {
                return new PrayerHeadings(in);
            }

            @Override
            public PrayerHeadings[] newArray(int size) {
                return new PrayerHeadings[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(heading);
        }
    }

}