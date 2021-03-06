package com.overcomersprayer.app.overcomersprayers;

import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.overcomersprayer.app.overcomersprayers.models.Transactions;

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

        void onCategoryClick(String cat);

        void onCategoryItemClicked(Prayer prayer);

        void addToNewClick(Prayer prayer);
    }

    interface PaymentListener {
        void onPaymentInitialized(String key);

        void onPaymentError(String errorMessage);

        void onPaymentCompleted(boolean wasSuccessful, String message);
    }

    interface TransactionsItemListener {
        void onTransactionItemClicked(Transactions transactions);
    }

    interface SearchListener {
        void onPrayerSearched(String query);
    }

    interface TTSRequest {
        void onTTSRequested(String textToSpeak);

        void onSmallClick(String text);
    }


}
