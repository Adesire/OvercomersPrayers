package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.fragments.MainPageFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import org.parceler.Parcels;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener {
    public static final String CASE = "case";
    public static final int LOGIN_REQUEST_CODE = 1099;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragmentContent(MainPageFragment.NewInstance(), false);

        /*card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
            }
        });*/
    }

    private void replaceFragmentContent(Fragment fragment, boolean shouldAddBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_acivity_content, fragment);
        if (shouldAddBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onPurchaseInitialized(Prayer prayer) {
        Toast.makeText(this, prayer.getHeading(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPreviewClicked(Prayer prayer) {
        Toast.makeText(this, prayer.getHeading(), Toast.LENGTH_SHORT).show();
        PrayerPageFragment.X=0;
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    @Override
    public void onCardClicked(Prayer prayer) {
        PrayerPageFragment.X=1;
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    private void openPrayerHeadingActivity(Prayer prayer) {
        Intent intent = new Intent(this, PrayerHeadingActivity.class);
        intent.putExtra("PRAYER_POINTS", Parcels.wrap(prayer));
        startActivity(intent);
    }

}
