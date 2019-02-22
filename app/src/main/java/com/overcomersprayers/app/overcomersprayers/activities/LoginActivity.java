package com.overcomersprayers.app.overcomersprayers.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;

import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.overcomersprayers.app.overcomersprayers.AuthPresenter;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.models.Users;
import com.overcomersprayers.app.overcomersprayers.utils.ExtraUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnClickListener, Listerners.AuthListener {

    private static final int REQUEST_READ_CONTACTS = 0;

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.confirm_password)
    EditText mConfirmPasswordView;
    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.firstname)
    EditText mFirstNameView;
    @BindView(R.id.lastname)
    EditText mLastNameView;
    @BindView(R.id.phone)
    EditText mPhoneView;
    @BindView(R.id.confirm_password_wrapper)
    View mConfirmPasswordWrapper;
    @BindView(R.id.first_name_wrapper)
    View mFirstNameWrapper;
    @BindView(R.id.last_name_wrapper)
    View mLastNameWrapper;
    @BindView(R.id.phone_wrapper)
    View mPhoneWrapper;
    @BindView(R.id.forgot_password_button)
    TextView mForgotPasswordView;
    @BindView(R.id.no_account_button)
    TextView mNoAccountButton;
    @BindView(R.id.email_sign_in_button)
    Button mEmailSignInButton;
    boolean isSignIn = true;
    private AuthPresenter authPresenter;
    Intent i;
    boolean shouldReturnResult = false;
    int casee;
    public static final int CASE_DEFAULT = 999;
    public static final int RESULT_SUCCESSFUL = 10;
    public static final int RESULT_CANCELLED = 89;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        populateAutoComplete();
        mForgotPasswordView.setOnClickListener(this);
        mNoAccountButton.setOnClickListener(this);
        mEmailSignInButton.setOnClickListener(this);
        FirebaseApp app = FirebaseApp.initializeApp(this);
        authPresenter = new AuthPresenter(this);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                validateFieldsAndAuth();
                return true;
            }
            return false;
        });
        i = getIntent();
        if (i != null) {
            shouldReturnResult = true;
            casee = i.getIntExtra(MainActivity.CASE, CASE_DEFAULT);
            Snackbar.make(mLoginFormView, "You have to login to continue", Snackbar.LENGTH_LONG).show();
        }

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void validateFieldsAndAuth() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mLastNameView.getText().toString().trim();
        String confirmPassword = mConfirmPasswordView.getText().toString().trim();
        String phone = mPhoneView.getText().toString().trim();

        View focusView;
        String errorMessage;


        if (TextUtils.isEmpty(email)) {
            errorMessage = getString(R.string.error_field_required);
            mEmailView.setError(errorMessage);
            mEmailView.requestFocus();
            return;
        } else if (!isEmailValid(email)) {
            errorMessage = getString(R.string.error_invalid_email);
            mEmailView.setError(errorMessage);
            mEmailView.requestFocus();
            Snackbar.make(mLoginFormView, errorMessage, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!isSignIn) {
            if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                errorMessage = getString(R.string.error_invalid_password_toast);
                Snackbar.make(mLoginFormView, errorMessage, Snackbar.LENGTH_LONG).show();
                mPasswordView.requestFocus();
                return;
            }
            focusView = isSignUpFormComplete(mFirstNameView, mLastNameView, mPhoneView);
            if (focusView != null) {
                focusView.requestFocus();
                errorMessage = "All fields are required";
                Snackbar.make(mLoginFormView, errorMessage, Snackbar.LENGTH_LONG).show();
                return;
            }
            if (!mConfirmPasswordView.getText().toString().equals(password)) {
                errorMessage = "Passwords do not match";
                mConfirmPasswordView.requestFocus();
                Snackbar.make(mLoginFormView, errorMessage, Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        toggleProgress(true);
        if (isSignIn)
            authPresenter.signIn(email, password);
        else {
            Users user = new Users(email, firstName, lastName, email, phone);
            authPresenter.signUp(user, password);
        }
    }

    private View isSignUpFormComplete(EditText... fields) {
        for (EditText field : fields) {
            if (TextUtils.isEmpty(field.getText().toString().trim()))
                return field;
        }
        return null;
    }

    private boolean isEmailValid(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isPasswordValid(String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void toggleProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        ExtraUtils.hideKeyboard(this);
        switch (v.getId()) {
            case R.id.email_sign_in_button:
                validateFieldsAndAuth();
                break;
            case R.id.forgot_password_button:
                String email = mEmailView.getText().toString().trim();
                if (!isEmailValid(email)) {
                    Snackbar.make(mLoginFormView, "enter a valid email", Snackbar.LENGTH_SHORT).show();
                    mEmailView.setError("enter a valid email");
                    mEmailView.requestFocus();
                } else {
                    toggleProgress(true);
                    authPresenter.sendPasswordResetLink(email);
                }
                break;
            case R.id.no_account_button:
                toggleAuthForm();
                break;
        }
    }

    private void toggleAuthForm() {
        isSignIn = !isSignIn;
        mFirstNameWrapper.setVisibility(isSignIn ? View.GONE : View.VISIBLE);
        mLastNameWrapper.setVisibility(isSignIn ? View.GONE : View.VISIBLE);
        mPhoneWrapper.setVisibility(isSignIn ? View.GONE : View.VISIBLE);
        mConfirmPasswordWrapper.setVisibility(isSignIn ? View.GONE : View.VISIBLE);
        mForgotPasswordView.setVisibility(isSignIn ? View.VISIBLE : View.GONE);
        mEmailSignInButton.setText(getString(isSignIn ? R.string.action_sign_in : R.string.sign_up));
        mNoAccountButton.setText(getString(isSignIn ? R.string.don_t_have_an_account : R.string.already_registered));
    }


    @Override
    public void onAuthSuccess() {
        Toast.makeText(this, "login success", Toast.LENGTH_SHORT).show();
        if (shouldReturnResult) {
            Intent resultData = new Intent();
            resultData.putExtra(MainActivity.CASE, casee);
            setResult(RESULT_SUCCESSFUL, resultData);
        }
        finish();
    }

    @Override
    public void onAuthFailed(String failureMessage) {
        toggleProgress(false);
        Snackbar.make(mLoginFormView, failureMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPasswordResetLinkSent() {
        toggleProgress(false);
        Snackbar.make(mLoginFormView, "Password reset link sent to " + mEmailView.getText().toString(), Snackbar.LENGTH_LONG).show();
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELLED, new Intent());
        super.onBackPressed();
    }
}


