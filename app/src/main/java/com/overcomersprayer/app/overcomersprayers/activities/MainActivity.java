package com.overcomersprayer.app.overcomersprayers.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;

import android.animation.Animator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewAnimationUtils;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.evernote.android.state.State;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.overcomersprayer.app.overcomersprayers.AuthPresenter;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.PaymentPresenter;
import com.overcomersprayer.app.overcomersprayers.PrayerReminder;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.fragments.CategoryFragment;
import com.overcomersprayer.app.overcomersprayers.fragments.PrayerFavouriteFragment;
import com.overcomersprayer.app.overcomersprayers.fragments.PrayerListFragment;
import com.overcomersprayer.app.overcomersprayers.fragments.PrayerPageFragment;
import com.overcomersprayer.app.overcomersprayers.fragments.PrayerStoreFragment;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.overcomersprayer.app.overcomersprayers.models.Transactions;
import com.overcomersprayer.app.overcomersprayers.utils.AppExecutors;
import com.overcomersprayer.app.overcomersprayers.utils.OpHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.util.Calendar;

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
    private static final int ACT_CHECK_TTS_DATA = 1101;
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
    boolean isChecked = true;

    private static final String CHECK_BOX = "check_box";

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (mUser == null /*&& item.getItemId() != R.id.navigation_dashboard*/) {
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
                    showWelcomeDialog(this);
                    replaceFragmentContent(CategoryFragment.NewInstance()/*PrayerStoreFragment.NewInstance()*/, false);
                    return true;
                }
            /*case R.id.navigation_notifications:
                if (CURRENT_FRAGMENT.equals(FRAGMENT_TRANSACTION))
                    return false;
                else {
                    CURRENT_FRAGMENT = FRAGMENT_TRANSACTION;
                    replaceFragmentContent(TransactionsFragment.NewInstance(), false);
                    return true;
                }*/
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
        userNotification(true);
        isChecked = loadCheckboxValue();

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

        fab.setVisibility(View.GONE);
        //fab.setOnClickListener(view -> onFabClicked());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void hideFavButton() {
        fab.setVisibility(View.INVISIBLE);
    }

    /*public void showFavButton() {
        fab.setVisibility(View.VISIBLE);
    }*/

    private void replaceFragmentContent(Fragment fragment, boolean shouldAddBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_acivity_content, fragment);
        if (shouldAddBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setSelected(true);
        toolbarTitle.setText(title);
    }

    @Override
    public void onPurchaseInitialized(Prayer prayer) {
        currentPrayerSelected = prayer;
        double amount = prayer.getHeading().equals("SELF-DELIVERANCE PRAYERS") ? 4.99 : 1.05;
        if (mUser == null) {
            signIn(null);
        } else
            initializePayment(amount);


    }

    private void initializePayment(double amount) {
        transactions = new Transactions("", amount, currentPrayerSelected.getId());
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

    String categoryName;

    @Override
    public void onCategoryClick(String cat) {
        replaceFragmentContent(PrayerStoreFragment.NewInstance(), true);
        categoryName = cat;
    }

    @Override
    public void onCategoryItemClicked(Prayer prayer) {
        replaceFragmentContent(PrayerPageFragment.newInstance(prayer), true);
    }

    @Override
    public void addToNewClick(Prayer prayer) {
        //Log.e("PRAYERS re-arrange","Successful");
        rootRef.child(categoryName).child(prayer.getId()).updateChildren(prayer.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e("PRAYERS re-arrange", "Successful");
                Toast.makeText(getApplicationContext(), "Successful Push to " + categoryName, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkTTS() {
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
    }

    private void openPrayerHeadingActivity(Prayer prayer) {
        Intent intent = new Intent(this, PrayerHeadingActivity.class);
        intent.putExtra("PRAYER_POINTS", Parcels.wrap(prayer));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            fab.setVisibility(View.GONE);
            Log.e(LOG_TAG, "user is null");
            navigationView.findViewById(R.id.navigation_dashboard).performClick();
        } else {
            if (menu != null)
                onPrepareOptionsMenu(menu);
            //fab.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(CURRENT_FRAGMENT)) {
                Log.e(LOG_TAG, "onstart: replacing here");
                navigationView.findViewById(R.id.navigation_home).performClick();
            }
        }
    }

    public void goToCategoryFrag() {
        navigationView.findViewById(R.id.navigation_dashboard).performClick();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showData(ListOfCategoriesWithHeading listOfCategoriesWithHeading) {
        //Toast.makeText(this, ""+listOfCategoriesWithHeading.getCategoryWithHeadingsList(), Toast.LENGTH_LONG).show();
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
                        initializePayment(currentPrayerSelected.getHeading().equals("SELF-DELIVERANCE PRAYERS") ? 4.99 : 1.05);
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
        } else if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode ==
                    TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Data exists, so we instantiate the TTS engine
                //mTTS = new TextToSpeech(this, this);
            } else {
                // Data is missing, so we start the TTS installation
                // process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
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

    public static void showPaymentProcessDialog(Context context, PaymentPresenter
            paymentPresenter) {
        alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.payment_verification_dialog, null, false);
        gifImageView = view.findViewById(R.id.loading_image);
        paymentStatusTextView = view.findViewById(R.id.status_text_view);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        paymentPresenter.verifyPayment();
    }

    public static void showWelcomeDialog(Context context) {
        alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.payment_verification_dialog, null, false);
        gifImageView = view.findViewById(R.id.loading_image);
        paymentStatusTextView = view.findViewById(R.id.status_text_view);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        gifImageView.setImageResource(R.drawable.praise);
        paymentStatusTextView.setText("Welcome to Overcomers' Prayers");
        alertDialog.show();
    }

    @Override
    public void onPaymentInitialized(String key) {
        String displayName = mUser.getDisplayName();
        String firstname = "";
        String lastName = "";
        if (displayName != null || !TextUtils.isEmpty(displayName)) {
            if (displayName.contains(":::")) {
                String fullname[] = displayName.split(":::");
                firstname = fullname[0];
                lastName = fullname[1];
            } else if (displayName.contains(" ")) {
                String fullname[] = displayName.split(" ");
                firstname = fullname[0];
                lastName = fullname[1];
            } else {
                firstname = displayName;
                lastName = displayName;
            }
        }
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        /*SubAccount subAccount = new SubAccount();
        subAccount.setId("RS_35F4308B327CF38CE30F74D58FA86D96");
        List<SubAccount> subAccounts = new ArrayList<>();
        subAccounts.add(subAccount);*/
        new RavePayManager(this)
                .setAmount(transactions.getAmount())
                .setCurrency("USD")
                .setCountry("NG")
                .setfName(firstname)
                .setlName(lastName)
                .setEmail(mUser.getEmail())
                .setTxRef(key)
                .setEncryptionKey("55c6bfd79a522461a61aa4c9"/*"72f1acf17cde1095ecb4d5fb" "FLWSECK_TEST0963fcaa831e"*/)
                .setPublicKey("FLWPUBK-35bf065fb328e04781d024f9eceb2e02-X"/*"FLWPUBK-8ab041a894856e22ad20d9f65349feff-X"*//*"FLWPUBK_TEST-3d6789e869a4b16248acae3c1de9f649-X"*/)
                .onStagingEnv(false)
                .allowSaveCardFeature(true)
                .acceptCardPayments(true)
                .acceptAccountPayments(true)
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
        //just to be able to push back
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
        MenuItem checkable = menu.findItem(R.id.turnOff);
        checkable.setChecked(isChecked);
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
                        signOut();
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
        replaceFragmentContent(PrayerFavouriteFragment.NewInstance(), true);
        circularReveal();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.turnOff:
                isChecked = !item.isChecked();
                userCheckPreference(isChecked);
                item.setChecked(isChecked);

                if (item.isChecked()) {
                    userNotification(true);
                    Toast.makeText(this, "Alarm On", Toast.LENGTH_SHORT).show();
                    item.setTitle(R.string.turn_off_alarm);
                } else {
                    userNotification(false);
                    Toast.makeText(this, "Alarm Off", Toast.LENGTH_SHORT).show();
                    item.setTitle(R.string.turn_on_alarm);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void userCheckPreference(boolean v) {
        SharedPreferences checkbox = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = checkbox.edit();
        editor.putBoolean(CHECK_BOX, v);
        editor.apply();
    }

    private boolean loadCheckboxValue() {
        SharedPreferences checkbox = PreferenceManager.getDefaultSharedPreferences(this);
        return checkbox.getBoolean(CHECK_BOX, true);
    }

    private void userNotification(boolean a) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notifyIntent = new Intent(this, PrayerReminder.class);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (a && (Calendar.getInstance().getTime() == calendar.getTime())) {
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, notifyPendingIntent);
        } else {
            alarmManager.cancel(notifyPendingIntent);
        }
    }

    public void share(MenuItem item) {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("http://play.google.com/store/apps/details?id=com.overcomersprayer.app.overcomersprayers")
                .getIntent();
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        AuthPresenter.getGoogleSignInClient(this).signOut().addOnCompleteListener(
                task -> {
                    mUser = null;
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    navigationView.findViewById(R.id.navigation_dashboard).performClick();
                    if (menu != null)
                        onPrepareOptionsMenu(menu);
                });
    }


}
