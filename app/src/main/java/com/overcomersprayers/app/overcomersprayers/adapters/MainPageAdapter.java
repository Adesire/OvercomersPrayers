package com.overcomersprayers.app.overcomersprayers.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private FirebaseUser mUser;
    private DatabaseReference rootRef;

    public MainPageAdapter(Listerners.PrayerListener prayerListener, boolean isPrayerStore) {
        this.prayerList = new ArrayList<>();
        this.prayerListener = prayerListener;
        this.isPrayerStore = isPrayerStore;
        this.mUser = FirebaseAuth.getInstance().getCurrentUser();
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MainPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_item, parent, false);
        return new MainPageViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull MainPageViewHolder holder, int position) {
        Prayer prayer;
        if (isPrayerStore)
            prayer = prayerList.get(position);
        else
            prayer = prayerList.get(getItemCount() - position - 1);
        holder.bind(prayer);
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
        @BindView(R.id.purchased)
        TextView purchasedTextView;


        public MainPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Prayer prayer) {
            if (isPrayerStore) {
                if (prayer.getId().equals("-LZuCd9_DRH_FWurPmno") || prayer.getId().equals("-LZuCd9at4GXkUCAVAmf") || prayer.getId().equals("-LZuCd9bS47B0oLQqaNQ")) {
                    setCardListener(prayer);
                } else {
                    checkUserAlreadyPurchased(prayer);
                }
            } else {
                setCardListener(prayer);
            }
            String prayerHeadingString = prayer.getHeading().replace(". ", "");
            prayerHeadingString = prayerHeadingString.replace(".", "");
            prayerHeading.setText(prayerHeadingString);
            if (prayer.getScriptures() != null) {
                String scriptureCut = isPrayerStore ? prayer.getScriptures().substring(0, 20) + "...." : prayer.getScriptures();
                scriptureReference.setText(scriptureCut);
            }
        }

        private void checkUserAlreadyPurchased(Prayer prayer) {
            if (mUser != null) {
                FirebaseDatabase.getInstance().getReference().child("userprayer").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(prayer.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            preview.setVisibility(View.GONE);
                            purchase.setVisibility(View.GONE);
                            purchasedTextView.setVisibility(View.VISIBLE);
                            setCardListener(prayer);
                        } else {
                            unsetCardListener(prayer);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        unsetCardListener(prayer);
                    }
                });
            } else {
                unsetCardListener(prayer);
            }
        }

        private void unsetCardListener(Prayer prayer) {
            card.setOnClickListener(null);
            preview.setVisibility(View.VISIBLE);
            purchase.setVisibility(View.VISIBLE);
            purchasedTextView.setVisibility(View.GONE);
            preview.setOnClickListener(v -> prayerListener.onPreviewClicked(prayer));
            purchase.setOnClickListener(v -> prayerListener.onPurchaseInitialized(prayer));
        }

        private void setCardListener(Prayer prayer) {
            preview.setVisibility(View.GONE);
            purchase.setVisibility(View.GONE);
            card.setOnClickListener(v -> prayerListener.onCardClicked(prayer));
        }

    }

}
