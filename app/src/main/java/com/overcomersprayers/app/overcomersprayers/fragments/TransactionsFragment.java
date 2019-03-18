package com.overcomersprayers.app.overcomersprayers.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayers.app.overcomersprayers.Listerners;
import com.overcomersprayers.app.overcomersprayers.PaymentPresenter;
import com.overcomersprayers.app.overcomersprayers.R;
import com.overcomersprayers.app.overcomersprayers.activities.LoginActivity;
import com.overcomersprayers.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayers.app.overcomersprayers.models.Transactions;
import com.overcomersprayers.app.overcomersprayers.utils.ExtraUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsFragment extends Fragment implements Listerners.TransactionsItemListener {

    private final String TAG = this.getClass().getSimpleName();
    @BindView(R.id.ticket_recyclerView)
    RecyclerView ticketsRecyclerView;
    TransactionsAdapter transactionsAdapter;
    @BindView(R.id.ticket_swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.emptyText)
    TextView emptyTextView;
    AppCompatActivity activity;
    private DatabaseReference rootRef;
    private FirebaseUser user;
    PaymentPresenter paymentPresenter;
    Listerners.PaymentListener paymentListener;
    private FirebaseUser mUser;

    public static TransactionsFragment NewInstance() {
        Bundle bundle = new Bundle();
        TransactionsFragment historyFragment = new TransactionsFragment();
        historyFragment.setArguments(bundle);
        return historyFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transactions, container, false);
        ButterKnife.bind(this, v);
        rootRef = FirebaseDatabase.getInstance().getReference();
        activity = (AppCompatActivity) getActivity();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        refreshLayout.setColorSchemeColors(Color.CYAN, Color.RED, Color.GREEN);
        refreshLayout.setRefreshing(true);
        refreshLayout.setOnRefreshListener(this::getTickets);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        transactionsAdapter = new TransactionsAdapter(new ArrayList<>(), this);
        ticketsRecyclerView.setLayoutManager(manager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        ticketsRecyclerView.addItemDecoration(dividerItemDecoration);
        ticketsRecyclerView.setAdapter(transactionsAdapter);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyTextView.setVisibility(View.GONE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("Seems like your session expired, Please login again");
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        getTickets();
    }

    private void getTickets() {
        rootRef.child(Transactions.getTableName()).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Transactions> transactionsList = new ArrayList<>();
                for (DataSnapshot ticketSnapshot : dataSnapshot.getChildren()) {
                    Transactions transactions = ticketSnapshot.getValue(Transactions.class);
                    transactionsList.add(transactions);
                }
                Log.e(TAG, "" + transactionsList.size());
                refreshLayout.setRefreshing(false);
                transactionsAdapter.swapData(transactionsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Can't retrieve transactions at the moment");
            }
        });
    }

    private void showToast(String message) {
        refreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        paymentListener = (Listerners.PaymentListener) context;
    }

    @Override
    public void onTransactionItemClicked(Transactions transactions) {
        if (transactions != null && user != null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            assert getFragmentManager() != null;
            Fragment prev = getChildFragmentManager().findFragmentByTag("verify");
            if (prev != null) {
                ft.remove(prev);
            }

            if (paymentPresenter == null)
                paymentPresenter = new PaymentPresenter(getContext(), transactions, paymentListener, null);
            else
                paymentPresenter.setNewTransaction(transactions, null);

            //TransactionProcessDialogFragment.NewInstance(transactions, u).show(ft, "verify");
            MainActivity.showPaymentProcessDialog(getContext(), paymentPresenter);
        } else {
            Log.e(TAG, "trans is null");
        }
    }


    public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder> {


        List<Transactions> transactionsList;
        Listerners.TransactionsItemListener transactionsItemListener;
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);


        TransactionsAdapter(List<Transactions> transactions, Listerners.TransactionsItemListener transactionsItemListener) {
            this.transactionsList = transactions;
            this.transactionsItemListener = transactionsItemListener;
        }

        @NonNull
        @Override
        public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
            return new TransactionsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
            Transactions transactions = transactionsList.get(getItemCount() - position - 1);
            Log.e(TAG, "" + transactions.getDate() + " : " + ExtraUtils.getHumanReadableString(transactions.getDate()));
            holder.date.setText(ExtraUtils.getHumanReadableString(transactions.getDate()));
            holder.serviceOrderedTextView.setText(transactions.getPrayerHeading());
            holder.emailAddress.setText(transactions.getTrxRef().substring(0, 5));
            String amount = format.format(transactions.getAmount());
            holder.priceView.setText(amount);
            GradientDrawable magnitudeCircle = (GradientDrawable) holder.circleImageView.getBackground();
            if (transactions.isHasBeenUpdated()) {
                if (transactions.isWasSuccesful()) {
                    holder.circleImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_check_black_24dp));
                    DrawableCompat.setTint(holder.circleImageView.getDrawable(), ContextCompat.getColor(getContext(), R.color.white));
                    magnitudeCircle.setColor(Color.parseColor("#388E3C"));
                } else {
                    holder.circleImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_close_black_24dp));
                    DrawableCompat.setTint(holder.circleImageView.getDrawable(), ContextCompat.getColor(getContext(), R.color.white));
                    magnitudeCircle.setColor(Color.parseColor("#F44336"));
                }
            } else {
                holder.circleImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_error_outline_black_24dp));
                DrawableCompat.setTint(holder.circleImageView.getDrawable(), ContextCompat.getColor(getContext(), R.color.white));
                magnitudeCircle.setColor(Color.parseColor("#FF9800"));
            }
        }


        @Override
        public int getItemCount() {
            return transactionsList.size();
        }

        void swapData(List<Transactions> transactions) {
            this.transactionsList = transactions;
            this.notifyDataSetChanged();
        }

        class TransactionsViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.prayer_purchased)
            AppCompatTextView serviceOrderedTextView;
            @BindView(R.id.status_image)
            ImageView circleImageView;
            @BindView(R.id.date)
            AppCompatTextView date;
            @BindView(R.id.price)
            AppCompatTextView priceView;
            @BindView(R.id.email_address)
            AppCompatTextView emailAddress;

            TransactionsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    transactionsItemListener.onTransactionItemClicked(transactionsList.get(getItemCount() - getAdapterPosition() - 1));
                });
            }
        }

    }

}
