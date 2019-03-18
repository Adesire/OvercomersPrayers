package com.overcomersprayers.app.overcomersprayers.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.evernote.android.state.State;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.PaymentPresenter;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerFavouriteFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerListFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.PrayerStoreFragment;
import com.overcomersprayers.app.overcomersprayers.fragments.TransactionsFragment;
import com.overcomersprayers.app.overcomersprayers.models.Prayer;
import com.overcomersprayers.app.overcomersprayers.models.Transactions;

import org.parceler.Parcels;

public class MainActivity extends AppCompatActivity implements Listerners.PrayerListener, Listerners.PaymentListener {
    public static final int LOGIN_REQUEST_CODE = 1099;
    public static final String CASE = "case";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
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
    @BindView(R.id.main_acivity_content)
    View mainView;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    public static int CASE_DEFAULT = 192;
    public static int CASE_LOGIN_NORMAL = 195;
    public static final int CASE_LOGIN_THEN_PAY = 196;
    public static final String FRAGMENT_HOME = "home";
    public static final String FRAGMENT_TRANSACTION = "transaction";
    public static final String FRAGMENT_PRAYER_STORE = "prayer_store";
    @State
    public String CURRENT_FRAGMENT = "";
    FragmentManager fragmentManager = getSupportFragmentManager();
    boolean isNavigationBarHidden;
    float d;
    int marginBottomInDp;
    ConstraintLayout.LayoutParams params;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    Menu menu;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (mUser == null && item.getItemId() != R.id.navigation_dashboard) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra(CASE, item.getItemId());
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
            return false;
        }
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
                    replaceFragmentContent(PrayerStoreFragment.NewInstance(), false);
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
        setSupportActionBar(mToolbar);
        progressDialog = new ProgressDialog(this);
        d = getResources().getDisplayMetrics().density;
        marginBottomInDp = (int) d * 56;
        params = (ConstraintLayout.LayoutParams) mainView.getLayoutParams();
        //mToolbar.setTitle(getString(R.string.app_name));

        setToolbarTitle(getString(R.string.app_name));
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (fragmentManager.getBackStackEntryCount() < 1) {
                setToolbarTitle(getString(R.string.app_name));
                navigationView.setVisibility(View.VISIBLE);
                params.setMargins(0, 0, 0, marginBottomInDp);
            } else {
                navigationView.setVisibility(View.GONE);
                params.setMargins(0, 0, 0, 0);
            }
            mainView.setLayoutParams(params);

        });

        fab.setOnClickListener(view -> onFabClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void replaceFragmentContent(Fragment fragment, boolean shouldAddBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_acivity_content, fragment);
        if (shouldAddBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
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
            paymentPresenter.setNewTransaction(transactions, currentPrayerSelected);
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
            fab.setVisibility(View.GONE);
            Log.e(LOG_TAG, "user is null");
            navigationView.findViewById(R.id.navigation_dashboard).performClick();
        } else {
            if (menu != null)
                onPrepareOptionsMenu(menu);
            fab.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(CURRENT_FRAGMENT)) {
                Log.e(LOG_TAG, "onstart: replacing here");
                navigationView.findViewById(R.id.navigation_home).performClick();
            }
        }
    }


    private void toggleBottomNavVisibility() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (data != null) {
                    int a = data.getIntExtra(CASE, CASE_DEFAULT);
                    if (a == CASE_LOGIN_THEN_PAY)
                        initializePayment();
                    else if (a == CASE_LOGIN_NORMAL) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    } else
                        try {
                            navigationView.findViewById(a).performClick();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "A mighty exception " + e.getLocalizedMessage());
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
                .withTheme(R.style.RaveTheme)
                .initialize();
    }

    @Override
    public void onPaymentError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        if (alertDialog != null) {
            gifImageView.setVisibility(View.GONE);
            paymentStatusTextView.setText(errorMessage);
        }
    }

    @Override
    public void onPaymentCompleted(boolean wasSuccessful, String message) {
        if (wasSuccessful) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
                PrayerPageFragment.X = 1;
                replaceFragmentContent(PrayerPageFragment.newInstance(currentPrayerSelected), true);
            }
            gifImageView.setImageResource(R.drawable.praise);
            paymentStatusTextView.setText(getString(R.string.payment_successful));
        } else {
            gifImageView.setVisibility(View.GONE);
            paymentStatusTextView.setText(message);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Prayers");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.e("TAAAAG1", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.e("TAAAAG1", newText);
                if (CURRENT_FRAGMENT.equals(FRAGMENT_PRAYER_STORE))
                    PrayerStoreFragment.sSearchListener.onPrayerSearched(newText);
                else if (CURRENT_FRAGMENT.equals(FRAGMENT_HOME))
                    PrayerListFragment.sSearchListener.onPrayerSearched(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuItem menuItem = this.menu.findItem(R.id.sign_button);
        if (mUser == null) menuItem.setTitle("Sign In");
        else menuItem.setTitle("Sign Out");

        return true;
    }

    public void signIn(MenuItem item) {
        if (mUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            if (item == null)
                intent.putExtra(CASE, CASE_LOGIN_THEN_PAY);
            else
                intent.putExtra(CASE, CASE_LOGIN_NORMAL);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else {
            AlertDialog.OnClickListener onClickListener = (dialog, which) -> {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE:
                        FirebaseAuth.getInstance().signOut();
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        navigationView.findViewById(R.id.navigation_dashboard).performClick();
                        if (menu != null)
                            onPrepareOptionsMenu(menu);
                        break;
                }
                dialog.cancel();
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out")
                    .setPositiveButton("Yes", onClickListener)
                    .setNegativeButton("No", onClickListener);
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            });
            dialog.show();
        }
    }

    private void onFabClicked() {
        replaceFragmentContent(PrayerListFragment.NewInstance(true), true);
        circularReveal();
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.prayerFavouriteList);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) (fab.getX() + (fab.getWidth() / 2));
        int cy = (int) (fab.getY()) + fab.getHeight() + 56;


        if (b) {
            Animator revealAnimator = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);
            }

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim =
                    null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(700);
            anim.start();
        }

    }

    private void circularReveal() {
        // previously invisible view
        View myView = findViewById(R.id.main_acivity_content);

// Check if the runtime version is at least Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            int cx = (fab.getLeft() + fab.getRight()) / 2;
            int cy = (fab.getTop() + fab.getBottom()) / 2;

            int width = myView.getWidth();
            int height = myView.getHeight();

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(width, height);

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0f, finalRadius);

            // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
            anim.setDuration(200);
            anim.start();
        } else {
            // set the view to visible without a circular reveal animation below Lollipop
            myView.setVisibility(View.VISIBLE);
        }
        fab.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fab.setVisibility(View.VISIBLE);
    }
}
