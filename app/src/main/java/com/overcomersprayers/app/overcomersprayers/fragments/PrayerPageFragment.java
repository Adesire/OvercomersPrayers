package com.overcomersprayers.app.overcomersprayers.fragments;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.MaskFilter;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.MaskFilterSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import com.overcomersprayers.app.overcomersprayers.adapters.PrayerPageAdapter;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrayerPageFragment extends Fragment implements Listerners.TTSRequest {

    @BindView(R.id.scriptures)
    TextView scriptures;
    @BindView(R.id.instruction)
    TextView instruction;
    @BindView(R.id.note)
    TextView note;
    @BindView(R.id.instructTag)
    TextView instructionTag;
    @BindView(R.id.noteTag)
    TextView noteTag;
    @BindView(R.id.prayer52_txt)
    TextView prayer52;
    @BindView(R.id.pry52_scroll)
    ScrollView prayer52Scroll;
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
    TextToSpeech defaultTTS;

    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());

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

        String scripturesText = "";
        String instructions = p.getInstructions();
        String noteTxt = p.getNote();
        String prayer52Txt ="  "+ p.getPrayer52();
        SpannableString prayer52TxtBlur = null;

        Log.e("TAG", instructions + "\n" + noteTxt);

        if (!(p.getScriptures().equals(""))) {
            scripturesText = p.getScriptures().substring(0, 20) + "...";
        } else {
            scripturesText = "No Scripture reference";
        }

        if (instructions != null) {
            instruction.setText(instructions);
            instruction.setVisibility(View.VISIBLE);
            instructionTag.setVisibility(View.VISIBLE);
        }
        if (noteTxt != null) {
            note.setText(noteTxt);
            noteTag.setVisibility(View.VISIBLE);
            note.setVisibility(View.VISIBLE);
        }
        if(prayer52Txt != null){
            if(prayer52Txt.contains("\\n")){
                prayer52Txt = prayer52Txt.replace("\\n","\n\n");

                prayer52TxtBlur = new SpannableString(prayer52Txt);
                float radius = prayer52.getTextSize() / 3;
                MaskFilter blur = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
                prayer52TxtBlur.setSpan(new MaskFilterSpan(blur), 100, 2443, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                Log.e("TAGGGGGTESTTTTTT",prayer52Txt.length()+"");

                prayer52.setVisibility(View.VISIBLE);
                prayer52Scroll.setVisibility(View.VISIBLE);
                prayerContentList.setVisibility(View.GONE);
            }
        }

        if (X == 0) {
            favourite.setVisibility(View.GONE);
            scriptures.setText(scripturesText);
            prayer52.setText(prayer52TxtBlur);
            bool.putBoolean("IS_LOCKED", true);

            prayerContentList.setOnScrollListener(mScrollListener);

        } else {
            scriptures.setText(p.getScriptures());
            prayer52.setText(prayer52Txt);
            viewMore.setVisibility(View.GONE);
        }
        prayerContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        prayerContentList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mPrayerPageAdapter = new PrayerPageAdapter(bool, this);
        prayerContentList.setAdapter(mPrayerPageAdapter);
        toolbarTitle.setText(prayerHeadingString);
        toolbarTitle.setSelected(true);
        getPrayerPoints(p);

        favourite.setVisibility(View.GONE);

        if (user != null) {
            getIsFavourite();
            onFavouriteClicked();
            if (X != 0)
                favourite.setVisibility(View.VISIBLE);
        }

        defaultTTS = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                defaultTTS.setLanguage(Locale.ENGLISH);
                //defaultTTS.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
            if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                viewMore.setVisibility(View.INVISIBLE);
            }else{
                viewMore.setVisibility(View.VISIBLE);
            }
        }
    };

    private void getIsFavourite() {
        rootRef.child("userFavourite").child(user.getUid()).child(p.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    String value;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        value = snapshot.getValue().toString();
                        if (snapshot.hasChildren()) {
                            int i = 0;
                            for (DataSnapshot shot : snapshot.getChildren()) {
                                value = shot.getValue().toString();
                                prayerpoints.add(value);
                                Log.e("TAAAAAAAAGGGGG", (i++) + "  " + value);

                            }
                        }
                        prayerpoints.add(value);
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
                favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
                rootRef.child("userFavourite").child(user.getUid()).child(p.getId()).setValue(null).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(getContext(), "Prayer removed from favourites", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getContext(), "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
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

    @Override
    public void onTTSRequested(String textToSpeak) {
        Toast.makeText(getContext(), "speaking", Toast.LENGTH_SHORT).show();
        if (defaultTTS.isSpeaking())
            defaultTTS.stop();
        defaultTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onSmallClick(String textToSpeak) {
        Toast.makeText(getContext(), "Long press to start speaking", Toast.LENGTH_SHORT).show();
        if (defaultTTS.isSpeaking())
            defaultTTS.stop();
        defaultTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (defaultTTS != null) {
            defaultTTS.stop();
            defaultTTS.shutdown();
        }
    }
}
