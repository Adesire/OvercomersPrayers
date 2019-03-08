package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayInitializer;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.PaymentPresenter;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.RaveApi;
import com.overcomersprayers.app.overcomersprayers.fragments.MainPageFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;
import com.overcomersprayers.app.overcomersprayers.models.RaveResponse;
import com.overcomersprayers.app.overcomersprayers.models.Transactions;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener, Listerners.PaymentListener {
    public static final int LOGIN_REQUEST_CODE = 1099;
    FirebaseAuth firebaseAuth;
    FirebaseUser mUser;
    Prayer currentPrayerSelected;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    Retrofit retrofit;
    Transactions transactions;
    PaymentPresenter paymentPresenter;
    AlertDialog.Builder alertDialogBuilder;
    AlertDialog alertDialog;
    GifImageView gifImageView;
    TextView paymentStatusTextView;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //FirebaseAuth.getInstance().signOut();
        replaceFragmentContent(MainPageFragment.NewInstance(), false);
        progressDialog = new ProgressDialog(this);
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
        currentPrayerSelected = prayer;
        if (mUser == null) {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
        } else
            initializePayment();


    }

    private void initializePayment() {
        transactions = new Transactions("", 1, currentPrayerSelected.getId());
        if (paymentPresenter == null) {
            paymentPresenter = new PaymentPresenter(this, transactions, this);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initializePayment();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You didn't login", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                alertDialogBuilder = new AlertDialog.Builder(this);
                View view = getLayoutInflater().inflate(R.layout.payment_verification_dialog, null, false);
                gifImageView = view.findViewById(R.id.loading_image);
                paymentStatusTextView = view.findViewById(R.id.status_text_view);
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
                alertDialogBuilder.setView(view);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                paymentPresenter.verifyPayment();
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
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
}
