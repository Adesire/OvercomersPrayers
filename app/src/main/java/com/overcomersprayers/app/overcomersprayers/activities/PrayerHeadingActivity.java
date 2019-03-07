package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

import org.parceler.Parcels;

public class PrayerHeadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_heading);

        Prayer p = Parcels.unwrap(getIntent().getParcelableExtra("PRAYER_POINTS"));
        //getSupportActionBar().setTitle(p.getHeading());
        if (p == null){
            Log.e("TAG", "prayer is null");
        }else {
            Log.e("TAG",""+p.getHeading());
        }

        replacePrayerPageContent(PrayerPageFragment.newInstance(p));
    }

    private void replacePrayerPageContent(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.prayer_acivity_content, fragment).addToBackStack(null).commit();
    }

}
