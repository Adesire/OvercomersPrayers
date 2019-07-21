package com.overcomersprayer.app.overcomersprayers.utils;

import android.widget.Filter;

import com.overcomersprayer.app.overcomersprayers.adapters.MainPageAdapter;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;

import java.util.ArrayList;
import java.util.List;

public class CustomFilter extends Filter {
    MainPageAdapter adapter;
    List<Prayer> filterList;

    public CustomFilter(List<Prayer> filterList, MainPageAdapter adapter) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //CHECK CONSTRAINT VALIDITY
        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            List<Prayer> filteredPlayers = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //CHECK
                if (filterList.get(i).getHeading().toUpperCase().contains(constraint)) {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }
            results.count = filteredPlayers.size();
            results.values = filteredPlayers;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.prayerList = (List<Prayer>) results.values;
        //REFRESH
        adapter.notifyDataSetChanged();
    }

    public CustomFilter(boolean isSearching) {
        super();
    }
}
