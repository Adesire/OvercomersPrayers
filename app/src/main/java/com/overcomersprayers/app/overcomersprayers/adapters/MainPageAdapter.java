package com.overcomersprayers.app.overcomersprayers.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;
import com.overcomersprayers.app.overcomersprayers.utils.CustomFilter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MainPageViewHolder> implements Filterable {

    private static final String LOG_TAG = MainPageAdapter.class.getSimpleName();
    public List<Prayer> prayerList;
    Listerners.PrayerListener prayerListener;
    CustomFilter filter;
    boolean isPrayerStore;

    public MainPageAdapter(Listerners.PrayerListener prayerListener, boolean isPrayerStore) {
        this.prayerList = new ArrayList<>();
        this.prayerListener = prayerListener;
        this.isPrayerStore = isPrayerStore;
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

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter(prayerList, this);
        }
        return filter;
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
        @BindView(R.id.prayer_card)
        CardView card;


        public MainPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Prayer prayer, int p) {
            if (isPrayerStore){
                if (p <= 2) {
                    preview.setVisibility(View.GONE);
                    purchase.setVisibility(View.GONE);
                    card.setOnClickListener(v -> prayerListener.onCardClicked(prayer));
                } else {
                    preview.setVisibility(View.VISIBLE);
                    purchase.setVisibility(View.VISIBLE);
                    preview.setOnClickListener(v -> prayerListener.onPreviewClicked(prayer));
                    purchase.setOnClickListener(v -> prayerListener.onPurchaseInitialized(prayer));
                }
            }
            String prayerHeadingString = prayer.getHeading().replace(". ", "");
            prayerHeadingString = prayerHeadingString.replace(".", "");
            prayerHeading.setText(prayerHeadingString);
            if (prayer.getScriptures() != null && preview.getVisibility() == View.GONE && purchase.getVisibility() == View.GONE) {
                scriptureReference.setText(prayer.getScriptures());
            } else if (prayer.getScriptures() != null) {
                String scriptureCut = prayer.getScriptures().substring(0, 20) + "....";
                scriptureReference.setText(scriptureCut);
            }
        }

    }

}
