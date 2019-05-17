package com.overcomersprayers.app.overcomersprayers.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayers.app.overcomersprayers.adapters.MainPageAdapter;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.codetail.animation.ViewAnimationUtils;

public class PrayerFavouriteFragment extends Fragment {

    @BindView(R.id.prayerFavouriteList)
    RecyclerView prayerFavouriteList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    MainPageAdapter mainPageAdapter;
    Listerners.PrayerListener prayerListener;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public static PrayerFavouriteFragment NewInstance() {
        return new PrayerFavouriteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_prayers, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prayerFavouriteList.setLayoutManager(new LinearLayoutManager(getContext()));
        mainPageAdapter = new MainPageAdapter(prayerListener, false);
        prayerFavouriteList.setAdapter(mainPageAdapter);
        getPrayers();
    }


    private void getPrayers() {
        String table = "userFavourite";
        rootRef.child(table).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Prayer> prayers = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot prayerSnapshot : dataSnapshot.getChildren()) {
                        Prayer prayer = prayerSnapshot.getValue(Prayer.class);
                        prayer.setId(prayerSnapshot.getKey());
                        prayers.add(prayer);
                    }
                }
                refreshLayout.setRefreshing(false);
                mainPageAdapter.swapData(prayers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
    }

    private void revealFavourites(View view) {

        RecyclerView infoContainer = prayerFavouriteList;

        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
        float radius = Math.max(infoContainer.getWidth(), infoContainer.getHeight()) * 2.0f;

        if (infoContainer.getVisibility() == View.INVISIBLE) {
            infoContainer.setVisibility(View.VISIBLE);
            ViewAnimationUtils.createCircularReveal(infoContainer, cx, cy, 0, radius).start();
        } else {
            Animator reveal = ViewAnimationUtils.createCircularReveal(
                    infoContainer, cx, cy, radius, 0);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    infoContainer.setVisibility(View.INVISIBLE);
                }
            });
            reveal.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivityCast().setToolbarTitle("My Favourites");
        getActivityCast().hideFavButton();
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }
}
