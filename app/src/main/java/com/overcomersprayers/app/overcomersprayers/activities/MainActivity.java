package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener {
    public static final int LOGIN_REQUEST_CODE = 1099;
    FirebaseAuth firebaseAuth;
    FirebaseUser mUser;
    Prayer currentPrayerSelected;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private ProgressDialog progressDialog;
    Retrofit retrofit;
    Transactions transactions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //FirebaseAuth.getInstance().signOut();
        retrofit = new Retrofit.Builder()
                .baseUrl(RaveApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        replaceFragmentContent(MainPageFragment.NewInstance(), false);

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

    @Override
    public void onPreviewClicked(Prayer prayer) {
        PrayerPageFragment.X=0;
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    @Override
    public void onCardClicked(Prayer prayer) {
        PrayerPageFragment.X=1;
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
                verifyPayment();
                Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void verifyPayment() {
        JSONObject jsonObject;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("txref", transactions.getTrxRef());
            jsonObject.put("SECKEY", "FLWSECK_TEST-0f31c54c2fe862c29694c1cc45e27c12-X");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<RaveResponse> raveResponseCall = retrofit.create(RaveApi.class).getRaveResponse(requestBody);
        raveResponseCall.enqueue(new Callback<RaveResponse>() {
            @Override
            public void onResponse(Call<RaveResponse> call, Response<RaveResponse> response) {
                Log.e("LOG", response.body().toString());
            }

            @Override
            public void onFailure(Call<RaveResponse> call, Throwable t) {

            }
        });
    }

    private void initializePayment() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage("Initializing transaction, Please wait...");
        progressDialog.show();
        DatabaseReference userTransactionRef = rootRef.child("transactions").child(mUser.getUid());
        String key = userTransactionRef.push().getKey();
        String uuid = UUID.randomUUID().toString();
        transactions = new Transactions(uuid, 1, currentPrayerSelected.getId());
        userTransactionRef.child(key).updateChildren(transactions.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                new RavePayManager(MainActivity.this)
                        .setAmount(transactions.getAmount())
                        .setCurrency("USD")
                        .setCountry("NG")
                        .setfName("name")
                        .setlName("lname")
                        .setEmail(mUser.getEmail())
                        .setTxRef(uuid)
                        .setEncryptionKey("FLWSECK_TEST0963fcaa831e")
                        .setPublicKey("FLWPUBK_TEST-3d6789e869a4b16248acae3c1de9f649-X")
                        .onStagingEnv(true)
                        .allowSaveCardFeature(true)
                        .acceptCardPayments(true)
                        .setNarration("Narration")
                        .initialize();
            }
        });
    }


}
