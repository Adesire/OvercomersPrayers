package com.overcomersprayers.app.overcomersprayers.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.overcomersprayers.app.overcomersprayers.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

public class PrayerPageAdapter extends RecyclerView.Adapter<PrayerPageAdapter.PrayerPageViewHolder> {
    @NonNull
    @Override
    public PrayerPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_points,parent,false);
        return new PrayerPageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerPageViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

     class PrayerPageViewHolder extends RecyclerView.ViewHolder {

         public PrayerPageViewHolder(@NonNull View itemView) {
             super(itemView);
             ButterKnife.bind(this, itemView);
         }
     }
}
