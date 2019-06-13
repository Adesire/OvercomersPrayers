package com.overcomersprayers.app.overcomersprayers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flutterwave.raveandroid.responses.SubAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.overcomersprayers.app.overcomersprayers.activities.LoginActivity;
import com.overcomersprayers.app.overcomersprayers.models.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.overcomersprayers.app.overcomersprayers.activities.LoginActivity.RC_SIGN_IN;

public class AuthPresenter {
    private static final String TAG = AuthPresenter.class.getCanonicalName();
    private Listerners.AuthListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private AppCompatActivity loginActivity;
    private GoogleSignInClient mGoogleSignInClient;

    public AuthPresenter(Listerners.AuthListener authListener, AppCompatActivity loginActivity) {
        this.mAuthListener = authListener;
        mAuth = FirebaseAuth.getInstance();
        this.loginActivity = loginActivity;
        reference = FirebaseDatabase.getInstance().getReference();
        mGoogleSignInClient = getGoogleSignInClient(loginActivity);
        mAuth = FirebaseAuth.getInstance();
    }

    public static GoogleSignInClient getGoogleSignInClient(AppCompatActivity appCompatActivity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(appCompatActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(appCompatActivity, gso);
    }


    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loginActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        mAuthListener.onAuthSuccess();
                    } else {
                        mAuthListener.onAuthFailed("signInWithCredential:failure " + task.getException().getLocalizedMessage());
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = task.getResult().getUser().getUid();
                reference.child(Users.getTableName()).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            reference.child(Users.getTableName()).child(uid).child("deviceToken").setValue(FirebaseInstanceId.getInstance().getToken());
                            mAuthListener.onAuthSuccess();
                        } else {
                            mAuthListener.onAuthFailed("User not found");
                            mAuth.signOut();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mAuthListener.onAuthFailed(databaseError.getMessage());
                        mAuth.signOut();
                    }
                });
            } else
                mAuthListener.onAuthFailed(task.getException().getMessage());
        });
    }

    public void signUp(Users user, String password) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(user.getFirstName().concat(":::").concat(user.getLastName())).build();
                firebaseUser.updateProfile(profileChangeRequest).addOnCompleteListener(task12 -> {
                    if (task12.isSuccessful()) {
                        user.setId(firebaseUser.getUid());
                        user.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
                        reference.child(Users.getTableName()).child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                mAuthListener.onAuthSuccess();
                            } else {
                                mAuthListener.onAuthFailed(task1.getException().getMessage());
                            }
                        });
                    }
                });

            } else {
                mAuthListener.onAuthFailed(task.getException().getMessage());
            }
        });
    }

    public void sendPasswordResetLink(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                mAuthListener.onPasswordResetLinkSent();
            else
                mAuthListener.onAuthFailed(task.getException().getMessage());

        });
    }

}
