package com.overcomersprayer.app.overcomersprayers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.activities.LoginActivity;
import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayer.app.overcomersprayers.adapters.ExpandableCategoriesAdapter;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.utils.AppExecutors;
import com.overcomersprayer.app.overcomersprayers.utils.OpHelper;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryFragment extends Fragment {

    private static final String LOG_TAG = PrayerListFragment.class.getSimpleName();
    private static final String MOTION_X_ARG = null;
    private static final String MOTION_Y_ARG = null;
    private ListOfCategoriesWithHeading listOfCategoriesWithHeading;
    @BindView(R.id.prayerHeadingList)
    RecyclerView prayerHeadingList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    Bundle bundle;
    ExpandableCategoriesAdapter mainPageAdapter;
    Listerners.PrayerListener prayerListener;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    public static Listerners.SearchListener sSearchListener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static int groupClicked = -1;

    public static CategoryFragment NewInstance() {
        return new CategoryFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prayerHeadingList.setLayoutManager(new LinearLayoutManager(getContext()));
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Seems like your session expired, Please login again", Toast.LENGTH_SHORT).show();
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        refreshLayout.setOnRefreshListener(this::getPrayers);
        refreshLayout.setRefreshing(true);
        //getPrayers();
//      prayerHeadingList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void getPrayers() {
        if (listOfCategoriesWithHeading == null) {
            Log.e("logd,", "null list");
            refreshLayout.setRefreshing(false);
            return;
        }
        mainPageAdapter = new ExpandableCategoriesAdapter(listOfCategoriesWithHeading.getCategoryWithHeadingsList(), prayerListener);
        mainPageAdapter.setOnGroupClickListener(flatPos -> {
            groupClicked = flatPos;
            return true;
        });
        prayerHeadingList.setAdapter(mainPageAdapter);
        refreshLayout.setRefreshing(false);
        if (groupClicked > -1)
            mainPageAdapter.onGroupClick(groupClicked);

//        String table = "userprayer";
//
//        rootRef.child(table).child(user.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<Prayer> prayers = new ArrayList<>();
//                if (dataSnapshot.getChildrenCount() > 0) {
//                    for (DataSnapshot prayerSnapshot : dataSnapshot.getChildren()) {
//                        Prayer prayer = prayerSnapshot.getValue(Prayer.class);
//                        prayer.setId(prayerSnapshot.getKey());
//                        prayers.add(prayer);
//                    }
//                }
//                refreshLayout.setRefreshing(false);
//                mainPageAdapter.swapData(prayers);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                refreshLayout.setRefreshing(false);
//            }
//        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_content, container, false);
        ButterKnife.bind(this, view);
        AppExecutors.getInstance().diskIO().execute(() -> {
            OpHelper.readOpDoc(getContext());
        });
        bundle = getArguments();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
        getActivityCast().hideSearchMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivityCast().showSearchMenu();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //getActivityCast().showFavButton();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showData(ListOfCategoriesWithHeading listOfCategoriesWithHeading) {
        //Toast.makeText(getContext(), ""+listOfCategoriesWithHeading.getCategoryWithHeadingsList(), Toast.LENGTH_LONG).show();
        this.listOfCategoriesWithHeading = listOfCategoriesWithHeading;
        getPrayers();
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }

}