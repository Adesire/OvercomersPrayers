package com.overcomersprayers.app.overcomersprayers;

import com.overcomersprayers.app.overcomersprayers.models.Prayer;

public interface Listerners {


    interface AuthListener {
        void onAuthSuccess();

        void onAuthFailed(String failureMessage);

        void onPasswordResetLinkSent();
    }

    interface PrayerListener {
        void onPurchaseInitialized(Prayer prayer);
        void onPreviewClicked(Prayer prayer);
    }


}
