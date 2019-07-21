package com.overcomersprayer.app.overcomersprayers;

import android.content.Context;
import android.util.Log;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.overcomersprayer.app.overcomersprayers.models.RaveResponse;
import com.overcomersprayer.app.overcomersprayers.models.Transactions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentPresenter {

    private static final String LOG_TAG = PaymentPresenter.class.getSimpleName();
    private FirebaseUser user;
    private DatabaseReference reference;
    private Transactions transactions;
    private Retrofit retrofit;
    private Listerners.PaymentListener paymentListener;
    private Prayer selectedPrayer;
    private Context mContext;

    public PaymentPresenter(Context context, Transactions transactions, Listerners.PaymentListener paymentListener, Prayer selectedPrayer) {
        this.mContext = context;
        this.reference = FirebaseDatabase.getInstance().getReference();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.transactions = transactions;
        this.selectedPrayer = selectedPrayer;
        this.paymentListener = paymentListener;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(RaveApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void initializePayment() {
        reference.child("userprayer").child(user.getUid()).child(selectedPrayer.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    paymentListener.onPaymentError("You have already purchased this prayer");
                else {
                    startPayment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                paymentListener.onPaymentError(databaseError.getMessage());
            }
        });
    }

    private void startPayment() {
        DatabaseReference userTransactionRef = reference.child("transactions").child(user.getUid());
        String key = userTransactionRef.push().getKey();
        transactions.setTrxRef(key);
        transactions.setTrxKey(key);
        userTransactionRef.child(key).updateChildren(transactions.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                paymentListener.onPaymentInitialized(key);
            } else {
                paymentListener.onPaymentError(mContext.getString(R.string.payment_status_error));
                Toast.makeText(mContext, "cannot initialize", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            paymentListener.onPaymentError(e.getLocalizedMessage());
        }).addOnCanceledListener(() -> {
            paymentListener.onPaymentError(mContext.getString(R.string.payment_status_error));
        });
    }

    public void verifyPayment() {
        if (transactions.isHasBeenUpdated())
            paymentListener.onPaymentCompleted(transactions.isWasSuccesful(), null);
        else {
            JSONObject jsonObject;
            jsonObject = new JSONObject();
            try {
                jsonObject.put("txref", transactions.getTrxRef());
                jsonObject.put("SECKEY","FLWSECK-55c6bfd79a52de578bf63a411a0ed309-X"/*"FLWSECK-72f1acf17cde44a8e85b861a8af957d6-X"*//*"FLWSECK_TEST-0f31c54c2fe862c29694c1cc45e27c12-X"*/);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<RaveResponse> raveResponseCall = retrofit.create(RaveApi.class).getRaveResponse(requestBody);
            raveResponseCall.enqueue(new Callback<RaveResponse>() {
                @Override
                public void onResponse(Call<RaveResponse> call, Response<RaveResponse> response) {
                    RaveResponse raveResponse = response.body();
                    if (raveResponse != null) {
                        if (raveResponse.getStatus().equals("successful")) {
                            addPrayerToUserPrayer();
                        } else {
                            endTransaction(false, mContext.getString(R.string.payment_failed));
                        }
                    } else {
                        Log.e(LOG_TAG, "rave response is empty for transaction: " + transactions.getTrxRef());
                        endTransaction(false, mContext.getString(R.string.payment_failed));
                    }
                }

                @Override
                public void onFailure(Call<RaveResponse> call, Throwable t) {
                    paymentListener.onPaymentError(t.getLocalizedMessage());
                }
            });
        }
    }

    public void addPrayerToUserPrayer() {
        Toast.makeText(mContext, "adding prayer to user", Toast.LENGTH_SHORT).show();
        Map<String, Object> map = new HashMap<>();
        map.put("prayerId", transactions.getPrayerId());
        if (selectedPrayer == null) {
            reference.child("prayer").child(transactions.getPrayerId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    selectedPrayer = dataSnapshot.getValue(Prayer.class);
                    reference.child("userprayer").child(user.getUid()).child(transactions.getPrayerId()).updateChildren(selectedPrayer.toMap()).addOnCompleteListener(task -> endTransaction(true, null));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            reference.child("userprayer").child(user.getUid()).child(transactions.getPrayerId()).updateChildren(selectedPrayer.toMap()).addOnCompleteListener(task -> endTransaction(true, null));
        }
    }

    public void endTransaction(boolean wasSuccessful, String message) {
        transactions.setWasSuccesful(wasSuccessful);
        transactions.setHasBeenUpdated(true);
        transactions.setStatus(wasSuccessful ? "Successful" : "Unsuccessful");
        reference.child(Transactions.getTableName()).child(user.getUid()).child(transactions.getTrxRef()).updateChildren(transactions.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                paymentListener.onPaymentCompleted(wasSuccessful, message);
        });
    }

    public void setNewTransaction(Transactions transactions, Prayer selectedPrayer) {
        this.transactions = transactions;
        this.selectedPrayer = selectedPrayer;
    }
}
