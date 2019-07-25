package com.overcomersprayer.app.overcomersprayers.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.activities.LoginActivity;
import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayer.app.overcomersprayers.adapters.ExpandableCategoriesAdapter;
import com.overcomersprayer.app.overcomersprayers.adapters.MainPageAdapter;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.overcomersprayer.app.overcomersprayers.utils.AppExecutors;
import com.overcomersprayer.app.overcomersprayers.utils.OpHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerListFragment extends Fragment implements Listerners.SearchListener {

    private static final String LOG_TAG = PrayerListFragment.class.getSimpleName();
    private static final String MOTION_X_ARG = null;
    private static final String MOTION_Y_ARG = null;
    @BindView(R.id.prayerHeadingList)
    RecyclerView prayerHeadingList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    Bundle bundle;
    MainPageAdapter mainPageAdapter;
    Listerners.PrayerListener prayerListener;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private Context mContext;
    public static Listerners.SearchListener sSearchListener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static PrayerListFragment NewInstance() {
        return new PrayerListFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prayerHeadingList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mainPageAdapter == null) {
            mainPageAdapter = new MainPageAdapter(prayerListener, true, new MainPageAdapter.PrayerDiffUtil());
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Seems like your session expired, Please login again", Toast.LENGTH_SHORT).show();
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        prayerHeadingList.setAdapter(mainPageAdapter);
        refreshLayout.setOnRefreshListener(this::getPrayers);
        refreshLayout.setRefreshing(true);
        getPrayers();
//      prayerHeadingList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


    }

    private void getPrayers() {
        String table = "userprayer";

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
                mainPageAdapter.submitList(prayers);
                if (mainPageAdapter.getItemCount() < 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                            .setMessage("You have not bookmarked any prayers")
                            .setTitle("Nothing here")
                            .setPositiveButton("See all prayers", (dialog, which) -> {
                                getActivityCast().goToCategoryFrag();
                            });
                    builder.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_content, container, false);
        ButterKnife.bind(this, view);
        bundle = getArguments();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
        sSearchListener = this;
        mContext = context;
    }

    @Override
    public void onPrayerSearched(String query) {
        //Log.e("TAAAAG1", query);
        mainPageAdapter.getFilter().filter(query);
    }

    @Override
    public void onStart() {
        super.onStart();
        //getActivityCast().showFavButton();
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }
}