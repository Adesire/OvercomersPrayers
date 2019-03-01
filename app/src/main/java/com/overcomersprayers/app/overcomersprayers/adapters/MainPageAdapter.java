package com.overcomersprayers.app.overcomersprayers.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MainPageViewHolder> {

    private static final String LOG_TAG = MainPageAdapter.class.getSimpleName();
    List<Prayer> prayerList;
    Listerners.PrayerListener prayerListener;

    public MainPageAdapter(Listerners.PrayerListener prayerListener) {
        this.prayerList = new ArrayList<>();
        this.prayerListener = prayerListener;
    }

    @NonNull
    @Override
    public MainPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_item, parent, false);
        return new MainPageViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull MainPageViewHolder holder, int position) {
        Prayer prayer = prayerList.get(position);
        holder.bind(prayer, position);
    }

    @Override
    public int getItemCount() {
        return prayerList.size();
    }

    public void swapData(List<Prayer> prayers) {
        this.prayerList = prayers;
        notifyDataSetChanged();
    }

    class MainPageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.prayer_heading)
        TextView prayerHeading;
        @BindView(R.id.scripture_reference)
        TextView scriptureReference;
        @BindView(R.id.preview)
        TextView preview;
        @BindView(R.id.purchase)
        TextView purchase;

        public MainPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Prayer prayer, int p) {
            if (p <= 2) {
                preview.setVisibility(View.GONE);
                purchase.setVisibility(View.GONE);
            } else {
                preview.setVisibility(View.VISIBLE);
                purchase.setVisibility(View.VISIBLE);
                preview.setOnClickListener(v -> prayerListener.onPreviewClicked(prayer));
                purchase.setOnClickListener(v -> prayerListener.onPurchaseInitialized(prayer));
            }
            String prayerHeadingString = prayer.getHeading().replace(". ", "");
            prayerHeadingString = prayerHeadingString.replace(".", "");
            prayerHeading.setText(prayerHeadingString);
            if (prayer.getScriptures() != null) {
                scriptureReference.setText(prayer.getScriptures());
            }
        }

    }

}
