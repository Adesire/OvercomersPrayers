package com.overcomersprayer.app.overcomersprayers.fragments;

import android.app.ProgressDialog;
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
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayer.app.overcomersprayers.adapters.PrayerPageAdapter;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;

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
    @BindView(R.id.add_text)
    TextView addText;
    PrayerPageAdapter mPrayerPageAdapter;
    Listerners.PrayerListener prayerListener;
    List<Prayer> favouritedPrayers = new ArrayList<>();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    Prayer pray;
    boolean isFavourite;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    TextToSpeech defaultTTS;

    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
    Bundle bool;
    ProgressDialog progressDialog;
    private Context mContext;

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
        bool = new Bundle();
        Bundle b = getArguments();
        X = 1;

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        pray = Parcels.unwrap(b.getParcelable("PRAYER_OBJECT"));
        String prayerHeadingString = pray.getHeading().replace(". ", "");
        prayerHeadingString = prayerHeadingString.replace(".", "");
        getActivityCast().setToolbarTitle(prayerHeadingString);
        if (pray.getScriptures() != null)
            fillUpViews(pray);
        else
            getFullPrayer();
    }

    private void getFullPrayer() {
        rootRef.child("prayer").orderByChild("heading").equalTo(pray.getHeading()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Prayer prayer = snapshot.getValue(Prayer.class);
                    if (prayer != null) {
                        prayer.setId(snapshot.getKey());
                        pray = prayer;
                        fillUpViews(prayer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(mContext, "error " + databaseError.getDetails(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillUpViews(Prayer p) {
        progressDialog.dismiss();
        String scripturesText = "";
        String instructions = p.getInstructions();
        String noteTxt = p.getNote();
        String prayer52Txt = "  " + p.getPrayer52();
        SpannableString prayer52TxtBlur = null;

        Log.e("TAG", instructions + "\n" + noteTxt);

        if (p.getScriptures() != null && !(p.getScriptures().equals(""))) {
            scripturesText = p.getScriptures().substring(0, 20) + "...";
        } else {
            scripturesText = "No Scripture Reference";
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
        if (prayer52Txt != null) {
            if (prayer52Txt.contains("\\n")) {
                prayer52Txt = prayer52Txt.replace("\\n", "\n\n");

                prayer52TxtBlur = new SpannableString(prayer52Txt);
                float radius = prayer52.getTextSize() / 3;
                MaskFilter blur = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
                prayer52TxtBlur.setSpan(new MaskFilterSpan(blur), 100, 2443, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                //Log.e("TAGGGGGTESTTTTTT",prayer52Txt.length()+"");

                prayer52.setVisibility(View.VISIBLE);
                prayer52Scroll.setVisibility(View.VISIBLE);
                prayerContentList.setVisibility(View.GONE);
            }
        }

        if (X == 0) {
            favourite.setVisibility(View.GONE);
            scriptures.setText(scripturesText);
            prayer52.setText(prayer52TxtBlur);
            viewMore.setText(p.getHeading().equals("SELF-DELIVERANCE PRAYERS") ? "View More ($4.99)" : "View More ($1.05)");
            bool.putBoolean("IS_LOCKED", false);

            prayerContentList.setOnScrollListener(mScrollListener);

        } else {
            scriptures.setText(p.getScriptures().equals("") ? "No Scripture reference" : p.getScriptures());
            prayer52.setText(prayer52Txt);
            viewMore.setVisibility(View.GONE);
        }
        prayerContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        prayerContentList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mPrayerPageAdapter = new PrayerPageAdapter(bool, this);
        prayerContentList.setAdapter(mPrayerPageAdapter);
        //toolbarTitle.setText(prayerHeadingString);
        toolbarTitle.setSelected(true);
        getPrayerPoints(p);

        favourite.setVisibility(View.GONE);

        if (user != null) {
            getIsFavourite(p);
            onFavouriteClicked(p);
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
            } else {
                viewMore.setVisibility(View.VISIBLE);
            }
        }
    };

    private void getIsFavourite(Prayer p) {
        String table = "userprayer";
        rootRef.child(table/*"userFavourite"*/).child(user.getUid()).child(p.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isFavourite = true;
                    favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                    addText.setText(getString(R.string.remove));
                } else {
                    isFavourite = false;
                    favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
                    addText.setText(getString(R.string.add_prayer));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick(R.id.view_more)
    public void initPayment() {
        prayerListener.onPurchaseInitialized(pray);
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

    private void onFavouriteClicked(Prayer p) {
        favourite.setOnClickListener(view -> {
            if (isFavourite) {
                favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
                rootRef.child("userprayer"/*"userFavourite"*/).child(user.getUid()).child(p.getId()).setValue(null).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addText.setText(getString(R.string.add_prayer));
                        Toast.makeText(getContext(), "Prayer removed from my prayers", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                favourite.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                rootRef.child("userprayer"/*"userFavourite"*/).child(user.getUid()).child(p.getId()).updateChildren(p.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            addText.setText(getString(R.string.remove));
                            Toast.makeText(getContext(), "Prayer added to my prayers", Toast.LENGTH_SHORT).show();
                        } else {
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
        mContext = context;
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
