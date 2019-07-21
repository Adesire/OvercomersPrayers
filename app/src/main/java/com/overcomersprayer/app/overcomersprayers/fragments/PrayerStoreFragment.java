package com.overcomersprayer.app.overcomersprayers.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayer.app.overcomersprayers.adapters.MainPageAdapter;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerStoreFragment extends Fragment implements Listerners.SearchListener {

    private static final String LOG_TAG = PrayerListFragment.class.getSimpleName();
    @BindView(R.id.prayerHeadingList)
    RecyclerView prayerHeadingList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    MainPageAdapter mainPageAdapter;
    Listerners.PrayerListener prayerListener;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    public static Listerners.SearchListener sSearchListener;
    private String lastKey = "", lastNode = "";
    final int ITEM_LOAD_COUNT = 10;
    int totalItem = 0, lastVisibleItem;
    private boolean isMaxData, isLoading = false;

    public static PrayerStoreFragment NewInstance() {
        return new PrayerStoreFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        prayerHeadingList.setLayoutManager(layoutManager);
        if (mainPageAdapter == null) {
            mainPageAdapter = new MainPageAdapter(prayerListener, true);

        }
        FirebaseDatabase.getInstance().getReference().child("prayer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("ALl pr", "" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        prayerHeadingList.setAdapter(mainPageAdapter);
        refreshLayout.setOnRefreshListener(() -> {
            isMaxData = false;
            lastNode = "";
            mainPageAdapter = new MainPageAdapter(prayerListener, true);
            getPrayers();
        });
        getLastKey();
        prayerHeadingList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    if (!isMaxData && !isLoading) {
                        getPrayers();
                        isLoading = true;
                    }
                }
            }
        });
        prayerHeadingList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void getLastKey() {
        refreshLayout.setRefreshing(true);
        rootRef.child("prayer").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    lastKey = snapshot.getKey();
                    getPrayers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isLoading = false;
            }
        });
    }

    private void getPrayers() {

        ArrayList<String> op2List = new ArrayList<String>();
        ArrayList<String> titleList = new ArrayList<String>();
        InputStream fileStream=getResources().openRawResource(R.raw.op_contents2);
        String ovpr = readTextFile(fileStream);

        HashMap<Integer, String> listMap = new HashMap<>();
        HashMap<Integer, String> dbListMap = new HashMap<>();

        JSONObject prayerBook = null;
        try {
            prayerBook = new JSONObject(ovpr);

            JSONObject article = prayerBook.getJSONObject("article");
            JSONArray orderedList = article.getJSONArray("orderedlist");

            HashMap<String,List<String>> map = new HashMap<>();

            int index=0;
            String s,title = null;
            for(int i=0;i<8;i++){
                int x = ((2*i) + 1);
                JSONObject o = orderedList.getJSONObject(x);
                Object o2 = orderedList.getJSONObject(2*i).getJSONObject("listitem").opt("para");
                //Log.e("TAGGEROG",o2.toString());
                JSONArray arr = o.getJSONArray("listitem");
                title = o2.toString();
                titleList.add(title);
                //op2List.add(0,title);
                for(int j=0;j<arr.length();j++){
                    //index=j;
                    Object pra = arr.getJSONObject(j).opt("para");
                    s = pra.toString();
                    //System.out.println(j+" "+s+"\n");
                    op2List.add(s);

                    //Log.e("TEGG"+j,s);
                    //map.put(titleList.get(i),op2List.subList(0,arr.length()));
                }
                //System.out.println("\n\n");
            }
            for(int k=0;k<8;k++){
                //map.put(titleList.get(k),op2List.subList(0,5));
            }
            //Log.e("MAP",map.toString());
            for(String x: op2List){
                listMap.put(index++,x);
            }
            int k=0;
            for(int x: listMap.keySet()){
                Log.e("TAGGER"+x,listMap.get(x));
            }
            //Log.e("MAPP",listMap.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(lastNode)) {
                query = rootRef.child("prayer").orderByKey().limitToFirst(ITEM_LOAD_COUNT);
                refreshLayout.setRefreshing(true);
            } else {
                query = rootRef.child("prayer").orderByKey().startAt(lastNode).limitToFirst(ITEM_LOAD_COUNT);
                mainPageAdapter.showLoader();
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mainPageAdapter.removeLoader();
                    refreshLayout.setRefreshing(false);
                    List<Prayer> prayers = new ArrayList<>();
                    List<String> p = new ArrayList<>();
                    if (dataSnapshot.getChildrenCount() > 0) {

                        for (DataSnapshot prayerSnapshot : dataSnapshot.getChildren()) {
                            Prayer prayer = prayerSnapshot.getValue(Prayer.class);
                            prayer.setId(prayerSnapshot.getKey());
                            prayers.add(prayer);
                            //p.add(prayer.getHeading());
                        }

                        for(int i=0;i<=prayers.size();i++){
                            /*if(prayers.get(i).getHeading().equals(listMap.get)){

                            }*/
                        }
                        //p.removeAll(listMap.values());
                        Log.e("STaGG",p.size()+"");

                        lastNode = prayers.get(prayers.size() - 1).getId();
                        if (!lastNode.equals(lastKey))
                            prayers.remove(prayers.size() - 1);
                        else
                            lastNode = "end";
                        isLoading = false;
                        mainPageAdapter.addAll(prayers);
                    } else {
                        isLoading = false;
                        isMaxData = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    refreshLayout.setRefreshing(false);
                    mainPageAdapter.removeLoader();
                    isLoading = false;
                }
            });
        }
    }

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
        sSearchListener = this;
    }

    @Override
    public void onPrayerSearched(String query) {
        //Log.e(LOG_TAG, ""+mainPageAdapter.getItemCount());
        mainPageAdapter.getFilter().filter(query);

    }

    @Override
    public void onStart() {
        super.onStart();
        //if (FirebaseAuth.getInstance().getCurrentUser() != null)
            //getActivityCast().showFavButton();
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }
}
