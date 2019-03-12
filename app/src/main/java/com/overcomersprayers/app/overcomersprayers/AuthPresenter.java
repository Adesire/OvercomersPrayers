package com.overcomersprayers.app.overcomersprayers;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.overcomersprayers.app.overcomersprayers.models.Users;

import androidx.annotation.NonNull;

public class AuthPresenter {
    private Listerners.AuthListener mAuthListener;
    private FirebaseAuth mAuth;
    DatabaseReference reference;

    public AuthPresenter(Listerners.AuthListener authListener) {
        this.mAuthListener = authListener;
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
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
