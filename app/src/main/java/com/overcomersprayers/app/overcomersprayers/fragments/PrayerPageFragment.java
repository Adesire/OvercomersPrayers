package com.overcomersprayers.app.overcomersprayers.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.activities.PrayerHeadingActivity;
import com.overcomersprayers.app.overcomersprayers.adapters.PrayerPageAdapter;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerPageFragment extends Fragment {

    @BindView(R.id.scriptures)
    TextView scriptures;
    @BindView(R.id.prayerContentList)
    RecyclerView prayerContentList;
    PrayerPageAdapter mPrayerPageAdapter;
    Prayer p;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    public static PrayerPageFragment newInstance(Prayer prayer) {
        PrayerPageFragment fragment = new PrayerPageFragment();
        Bundle b = new Bundle();
        b.putParcelable("PRAYER_OBJECT", Parcels.wrap(prayer));
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_heading_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public PrayerHeadingActivity getActivityCast() {
        return (PrayerHeadingActivity) getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();
        p = Parcels.unwrap(b.getParcelable("PRAYER_OBJECT"));
        scriptures.setText(p.getScriptures());
        prayerContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mPrayerPageAdapter = new PrayerPageAdapter();
        prayerContentList.setAdapter(mPrayerPageAdapter);

        getPrayerPoints();
    }

    private void getPrayerPoints() {
        rootRef.child("prayerpoints").child(p.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> prayerpoints = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        prayerpoints.add(snapshot.getValue().toString());
                    }

                    mPrayerPageAdapter.swapData(prayerpoints);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
