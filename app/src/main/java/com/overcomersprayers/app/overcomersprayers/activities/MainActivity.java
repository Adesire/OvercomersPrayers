package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.PaymentPresenter;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerListFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.TransactionsFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;
import com.overcomersprayers.app.overcomersprayers.models.Transactions;

import org.parceler.Parcels;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener, Listerners.PaymentListener {
    public static final int LOGIN_REQUEST_CODE = 1099;
    public static final String CASE = "case";
    FirebaseUser mUser;
    Prayer currentPrayerSelected;
    Transactions transactions;
    PaymentPresenter paymentPresenter;
    static AlertDialog.Builder alertDialogBuilder;
    static AlertDialog alertDialog;
    static GifImageView gifImageView;
    static TextView paymentStatusTextView;
    ProgressDialog progressDialog;
    @BindView(R.id.navigation)
    BottomNavigationView navigationView;
    public static int CASE_DEFAULT = 192;
    public static int CASE_LOGIN_NORMAL = 195;
    public static final int CASE_LOGIN_THEN_PAY = 196;
    public static final String FRAGMENT_HOME = "home";
    public static final String FRAGMENT_TRANSACTION = "transaction";
    public static final String FRAGMENT_PRAYER_STORE = "prayer_store";
    public static String CURRENT_FRAGMENT = FRAGMENT_HOME;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (CURRENT_FRAGMENT.equals(FRAGMENT_HOME))
                    return false;
                else {
                    CURRENT_FRAGMENT = FRAGMENT_HOME;
                    replaceFragmentContent(PrayerListFragment.NewInstance(), false);
                    return true;
                }
            case R.id.navigation_dashboard:
                if (CURRENT_FRAGMENT.equals(FRAGMENT_PRAYER_STORE))
                    return false;
                else {
                    CURRENT_FRAGMENT = FRAGMENT_PRAYER_STORE;
                    //replaceFragmentContent(HistoryFragment.NewInstance(u), false);
                    return true;
                }
            case R.id.navigation_notifications:
                if (CURRENT_FRAGMENT.equals(FRAGMENT_TRANSACTION))
                    return false;
                else {
                    CURRENT_FRAGMENT = FRAGMENT_TRANSACTION;
                    replaceFragmentContent(TransactionsFragment.NewInstance(), false);
                    return true;
                }
        }
        return false;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //FirebaseAuth.getInstance().signOut();
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        replaceFragmentContent(PrayerListFragment.NewInstance(), false);
        progressDialog = new ProgressDialog(this);
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
        currentPrayerSelected = prayer;
        if (mUser == null) {
            signIn(null);
        } else
            initializePayment();


    }

    private void initializePayment() {
        transactions = new Transactions("", 1, currentPrayerSelected.getId());
        transactions.setPrayerHeading(currentPrayerSelected.getHeading());
        if (paymentPresenter == null) {
            paymentPresenter = new PaymentPresenter(this, transactions, this, currentPrayerSelected);
        } else
            paymentPresenter.setNewTransaction(transactions);
        progressDialog.setMessage("Initializing transaction, Please wait...");
        progressDialog.show();
        paymentPresenter.initializePayment();
    }

    @Override
    public void onPreviewClicked(Prayer prayer) {
        PrayerPageFragment.X = 0;
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    @Override
    public void onCardClicked(Prayer prayer) {
        PrayerPageFragment.X = 1;
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    private void openPrayerHeadingActivity(Prayer prayer) {
        Intent intent = new Intent(this, PrayerHeadingActivity.class);
        intent.putExtra("PRAYER_POINTS", Parcels.wrap(prayer));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            navigationView.setVisibility(View.GONE);
        } else {
            navigationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    int a = data.getIntExtra(CASE, CASE_DEFAULT);
                    switch (a) {
                        case CASE_LOGIN_THEN_PAY:
                            initializePayment();
                            break;
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You didn't login", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                showPaymentProcessDialog(this, paymentPresenter);
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void showPaymentProcessDialog(Context context, PaymentPresenter paymentPresenter) {
        alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.payment_verification_dialog, null, false);
        gifImageView = view.findViewById(R.id.loading_image);
        paymentStatusTextView = view.findViewById(R.id.status_text_view);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        paymentPresenter.verifyPayment();
    }

    @Override
    public void onPaymentInitialized(String key) {
        String displayName = mUser.getDisplayName();
        String firstname = "";
        String lastName = "";
        if (displayName != null || !TextUtils.isEmpty(displayName)) {
            String fullname[] = displayName.split(":::");
            firstname = fullname[0];
            lastName = fullname[1];
        }
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        new RavePayManager(this)
                .setAmount(transactions.getAmount())
                .setCurrency("USD")
                .setCountry("NG")
                .setfName(firstname)
                .setlName(lastName)
                .setEmail(mUser.getEmail())
                .setTxRef(key)
                .setEncryptionKey("FLWSECK_TEST0963fcaa831e")
                .setPublicKey("FLWPUBK_TEST-3d6789e869a4b16248acae3c1de9f649-X")
                .onStagingEnv(true)
                .allowSaveCardFeature(true)
                .acceptCardPayments(true)
                .setNarration("Payment for prayer")
                .initialize();
    }

    @Override
    public void onPaymentError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        if (alertDialog != null) {
            gifImageView.setVisibility(View.GONE);
            paymentStatusTextView.setText(errorMessage);
        }
    }

    @Override
    public void onPaymentCompleted(boolean wasSuccessful) {
        if (wasSuccessful) {
            gifImageView.setImageResource(R.drawable.praise);
            paymentStatusTextView.setText(getString(R.string.payment_successful));
        }
    }


    public void signIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        if (view == null)
            intent.putExtra(CASE, CASE_LOGIN_THEN_PAY);
        else
            intent.putExtra(CASE, CASE_LOGIN_NORMAL);
        startActivityForResult(intent, LOGIN_REQUEST_CODE);
    }
}
