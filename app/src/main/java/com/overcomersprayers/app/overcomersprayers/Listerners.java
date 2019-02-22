package com.overcomersprayers.app.overcomersprayers;

public interface Listerners {


    interface AuthListener {
        void onAuthSuccess();

        void onAuthFailed(String failureMessage);

        void onPasswordResetLinkSent();
    }
}
