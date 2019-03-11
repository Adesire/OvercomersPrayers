package com.overcomersprayers.app.overcomersprayers;

import com.overcomersprayers.app.overcomersprayers.models.Prayer;
import com.overcomersprayers.app.overcomersprayers.models.Transactions;

public interface Listerners {


    interface AuthListener {
        void onAuthSuccess();

        void onAuthFailed(String failureMessage);

        void onPasswordResetLinkSent();
    }

    interface PrayerListener {
        void onPurchaseInitialized(Prayer prayer);

        void onPreviewClicked(Prayer prayer);

        void onCardClicked(Prayer prayer);
    }

    interface PaymentListener {
        void onPaymentInitialized(String key);

        void onPaymentError(String errorMessage);

        void onPaymentCompleted(boolean wasSuccessful);
    }
    interface TransactionsItemListener {
        void onTransactionItemClicked(Transactions transactions);
    }

}
