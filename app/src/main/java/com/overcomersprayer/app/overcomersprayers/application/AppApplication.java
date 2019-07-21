package com.overcomersprayer.app.overcomersprayers.application;

import android.app.Application;
import android.content.Context;

import com.evernote.android.state.StateSaver;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;


public class AppApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(getApplicationContext());
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        StateSaver.setEnabledForAllActivitiesAndSupportFragments(this, true);
    }
}
