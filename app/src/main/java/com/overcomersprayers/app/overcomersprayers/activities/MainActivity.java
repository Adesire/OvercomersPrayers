package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.overcomersprayers.app.overcomersprayers.R;

public class MainActivity extends AppCompatActivity {
    public static final String CASE = "case";
    public static final int LOGIN_REQUEST_CODE = 1099;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
