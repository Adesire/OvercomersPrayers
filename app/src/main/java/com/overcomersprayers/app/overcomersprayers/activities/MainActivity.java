package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.fragments.MainPageFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener {
    public static final String CASE = "case";
    public static final int LOGIN_REQUEST_CODE = 1099;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragmentContent(MainPageFragment.NewInstance());
    }

    private void replaceFragmentContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_acivity_content, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onPurchaseInitialized(Prayer prayer) {
        Toast.makeText(this, prayer.getHeading(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPreviewClicked(Prayer prayer) {
        Toast.makeText(this, prayer.getHeading(), Toast.LENGTH_SHORT).show();
    }
}
