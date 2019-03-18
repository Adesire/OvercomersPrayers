package com.overcomersprayers.app.overcomersprayers.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import butterknife.OnClick;

public class PrayerPageFragment extends Fragment {

    @BindView(R.id.scriptures)
    TextView scriptures;
    @BindView(R.id.prayerContentList)
    RecyclerView prayerContentList;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.view_more)
    Button viewMore;
    @BindView(R.id.favourite)
    ImageView favourite;
    PrayerPageAdapter mPrayerPageAdapter;
    Listerners.PrayerListener prayerListener;
    List<Prayer> favouritedPrayers = new ArrayList<>();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    Prayer p;
    boolean isFavourite;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public static int X;

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

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bool = new Bundle();
        Bundle b = getArguments();

        p = Parcels.unwrap(b.getParcelable("PRAYER_OBJECT"));
        String prayerHeadingString = p.getHeading().replace(". ", "");
        prayerHeadingString = prayerHeadingString.replace(".", "");
        getActivityCast().setToolbarTitle(prayerHeadingString);


        String scripturesText = null;
        if (p.getScriptures() != null) {
            scripturesText = p.getScriptures().substring(0, 20) + "...";
        } else {
            scripturesText = "No Scripture reference";
        }
        if (X == 0) {
            favourite.setVisibility(View.GONE);
            scriptures.setText(scripturesText);
            bool.putBoolean("IS_LOCKED", true);

        } else {
            scriptures.setText(p.getScriptures());
            viewMore.setVisibility(View.GONE);
        }
        prayerContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mPrayerPageAdapter = new PrayerPageAdapter(bool);
        prayerContentList.setAdapter(mPrayerPageAdapter);
        toolbarTitle.setText(prayerHeadingString);
        toolbarTitle.setSelected(true);
        getPrayerPoints(p);

        favourite.setVisibility(View.GONE);

        if(user != null){
            getIsFavourite();
            onFavouriteClicked();
            favourite.setVisibility(View.VISIBLE);
        }

    }

    private void getIsFavourite() {
        rootRef.child("userFavourite").child(user.getUid()).child(p.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isFavourite = true;
                    favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                } else {
                    isFavourite = false;
                    favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick(R.id.view_more)
    public void initPayment() {
        prayerListener.onPurchaseInitialized(p);
    }

    private void getPrayerPoints(Prayer p) {
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

    private void onFavouriteClicked() {
        favourite.setOnClickListener(view -> {
            if (isFavourite) {
                rootRef.child("userFavourite").child(user.getUid()).child(p.getId()).setValue(null).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(getContext(), "Prayer removed from favourites", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getContext(), "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                rootRef.child("userFavourite").child(user.getUid()).child(p.getId()).updateChildren(p.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Toast.makeText(getContext(), "Prayer added to favourites", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(getContext(), "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
    }
}
