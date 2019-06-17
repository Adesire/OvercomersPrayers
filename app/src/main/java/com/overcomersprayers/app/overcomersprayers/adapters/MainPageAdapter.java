package com.overcomersprayers.app.overcomersprayers.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
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

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.BaseViewHolder> implements Filterable {

    private static final String LOG_TAG = MainPageAdapter.class.getSimpleName();
    public List<Prayer> prayerList;
    Listerners.PrayerListener prayerListener;
    CustomFilter filter;
    boolean isPrayerStore;
    private FirebaseUser mUser;
    private DatabaseReference rootRef;
    private final int VIEW_TYPE_PRAYER = 109;
    private final int VIEW_TYPE_LOADING = 199;
    private boolean loaderVisible = false;

    public MainPageAdapter(Listerners.PrayerListener prayerListener, boolean isPrayerStore) {
        this.prayerList = new ArrayList<>();
        this.prayerListener = prayerListener;
        this.isPrayerStore = isPrayerStore;
        this.mUser = FirebaseAuth.getInstance().getCurrentUser();
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new FooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_item_loading, parent, false));
            case VIEW_TYPE_PRAYER:

            default:
                return new MainPageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_item, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Prayer prayer;
        if (isPrayerStore)
            prayer = prayerList.get(position);
        else
            prayer = prayerList.get(getItemCount() - position - 1);
        if (!prayer.isLoader())
            holder.bind(prayer);
    }

    public void showLoader() {
        loaderVisible = true;
        prayerList.add(new Prayer(true));
        notifyItemInserted(prayerList.size() - 1);
    }

    public void removeLoader() {
        loaderVisible = false;
        if (prayerList.size() < 1)
            return;
        int position = prayerList.size() - 1;
        Prayer item = prayerList.get(position);
        if (item != null) {
            prayerList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Prayer prayer = prayerList.get(position);
        if (prayer.isLoader())
            return VIEW_TYPE_LOADING;
        else
            return VIEW_TYPE_PRAYER;
    }

    @Override
    public int getItemCount() {
        return prayerList.size();
    }

    public void clear() {
        prayerList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addAll(List<Prayer> prayers) {
        int initialSize = prayerList.size();
        prayerList.addAll(prayers);
        notifyItemRangeInserted(initialSize, prayers.size());

    }

    public String getLastItemId() {
        return prayerList.get(prayerList.size() - 1).getId();
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

    class MainPageViewHolder extends BaseViewHolder {
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

        public void bind(Prayer prayer) {
            super.bind(prayer);
            if (isPrayerStore) {
                if (prayer.getId().equals("-Lh0ZHAi3AKMjCUqAB5o") || prayer.getId().equals("-Lh0ZHAjQzOzgiMWmnIO") || prayer.getId().equals("-Lh0ZHAjQzOzgiMWmnIP")) {
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
            String scripturePreview = prayer.getScriptures();
            if (!(scripturePreview.equals(""))) {
                if(prayer.getScriptures().length()<1){
                    Log.e("BAD GUY",prayerHeadingString+"\n"+prayer.getScriptures());
                }
                String scriptureCut = isPrayerStore ? scripturePreview.substring(0, 20) + "...." : scripturePreview;
                scriptureReference.setText(scriptureCut);
            }else {
                scripturePreview = "No Scripture Reference";
                scriptureReference.setText(scripturePreview);
            }

            purchase.setText(prayer.getHeading().equals("SELF-DELIVERANCE PRAYERS")? "$4.99" : "$1.05");

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

    abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Prayer prayer) {

        }

    }

    public class FooterHolder extends BaseViewHolder {
        @BindView(R.id.progressBar)
        ProgressBar mProgressBar;


        FooterHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }

}
