package com.overcomersprayer.app.overcomersprayers.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.overcomersprayer.app.overcomersprayers.utils.CustomFilter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainPageAdapter extends ListAdapter<Prayer, MainPageAdapter.BaseViewHolder> implements Filterable {

    private static final String LOG_TAG = MainPageAdapter.class.getSimpleName();
    //public List<Prayer> prayerList;
    Listerners.PrayerListener prayerListener;
    CustomFilter filter;
    boolean isPrayerStore;
    private FirebaseUser mUser;
    private DatabaseReference rootRef;
    private final int VIEW_TYPE_PRAYER = 109;
    private final int VIEW_TYPE_LOADING = 199;
    private boolean loaderVisible = false;

    public MainPageAdapter(Listerners.PrayerListener prayerListener, boolean isPrayerStore, PrayerDiffUtil prayerDiffUtil) {
        super(prayerDiffUtil);
        //this.prayerList = new ArrayList<>();
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
            prayer = getItem(position);
        else
            prayer = getItem(getItemCount() - position - 1);
        if (!prayer.isLoader())
            holder.bind(prayer);
    }

    public void showLoader() {
        loaderVisible = true;
        getCurrentList().add(new Prayer(true));
        submitList(getCurrentList());
        //notifyItemInserted(prayerList.size() - 1);
    }


    public void removeLoader() {
        loaderVisible = false;
        if (getItemCount() < 1)
            return;
        int position = getItemCount() - 1;
        Prayer item = getItem(position);
        if (item != null) {
            getCurrentList().remove(position);
            submitList(getCurrentList());
            //notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Prayer prayer = getItem(position);
        if (prayer.isLoader())
            return VIEW_TYPE_LOADING;
        else
            return VIEW_TYPE_PRAYER;
    }


    public void clear() {
        // prayerList = new ArrayList<>();
        submitList(new ArrayList<>());
    }

    public void addAll(List<Prayer> prayers) {
        int initialSize = getCurrentList().size();
        getCurrentList().addAll(prayers);
        submitList(getCurrentList());
        //notifyItemRangeInserted(initialSize, prayers.size());

    }

    public String getLastItemId() {
        return getCurrentList().get(getCurrentList().size() - 1).getId();
    }


    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter(getCurrentList(), this);
        }
        return filter;
    }

    public static class PrayerDiffUtil extends DiffUtil.ItemCallback<Prayer> {

        @Override
        public boolean areItemsTheSame(@NonNull Prayer oldItem, @NonNull Prayer newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Prayer oldItem, @NonNull Prayer newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
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
        CoordinatorLayout card;
        @BindView(R.id.purchased)
        TextView purchasedTextView;
        @BindView(R.id.addToNew)
        ImageButton imageButton;


        public MainPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            card.setBackgroundColor(Color.WHITE);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) card.getLayoutParams();
            layoutParams.setMargins(10, 20, 10, 0);
            scriptureReference.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.GONE);
            card.setLayoutParams(layoutParams);
        }

        public void bind(Prayer prayer) {
            super.bind(prayer);
//            addBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    prayerListener.addToNewClick(prayer);
//                }
//            });
            if (isPrayerStore) {
                /*if (prayer.getId().equals("-Lh0ZHAi3AKMjCUqAB5o") || prayer.getId().equals("-Lh0ZHAjQzOzgiMWmnIO") || prayer.getId().equals("-Lh0ZHAjQzOzgiMWmnIP")) {
                    setCardListener(prayer);
                } else {
                    checkUserAlreadyPurchased(prayer);
                }
            } else {*/
                setCardListener(prayer);
            }
            String prayerHeadingString = prayer.getHeading().replace(". ", "");
            prayerHeadingString = prayerHeadingString.replace(".", "");
            prayerHeading.setText(prayerHeadingString);
            String scripturePreview = prayer.getScriptures();
            if (!(scripturePreview.equals(""))) {
                if (prayer.getScriptures().length() < 1) {
                    Log.e("BAD GUY", prayerHeadingString + "\n" + prayer.getScriptures());
                }
                String scriptureCut = isPrayerStore ? scripturePreview.substring(0, 20) + "...." : scripturePreview;
                scriptureReference.setText(scriptureCut);
            } else {
                scripturePreview = "No Scripture Reference";
                scriptureReference.setText(scripturePreview);
            }

            purchase.setText(prayer.getHeading().equals("SELF-DELIVERANCE PRAYERS") ? "$4.99" : "$1.05");

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
