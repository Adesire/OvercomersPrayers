package com.overcomersprayer.app.overcomersprayers.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryFragment extends Fragment {

    Listerners.PrayerListener mListener;

    @BindView(R.id.prayerCategoryList)
    RecyclerView categoryList;

    public static CategoryFragment newInstance(Listerners.PrayerListener mListener){
        /*Fragment f = new Fragment();
        Bundle b = new Bundle();
        b.putParcelable("Listen");
        f.setArguments();*/
        return new CategoryFragment(mListener);
    }

    public CategoryFragment(Listerners.PrayerListener mListener){
        this.mListener = mListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category,container,false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readOpDoc();
    }

    public void readOpDoc(){
        ArrayList<String> op2List = new ArrayList<String>();
        ArrayList<String> titleList = new ArrayList<String>();
        InputStream fileStream=getResources().openRawResource(R.raw.op_contents2);
        String ovpr = readTextFile(fileStream);

        JSONObject prayerBook = null;
        try {
            prayerBook = new JSONObject(ovpr);

            JSONObject article = prayerBook.getJSONObject("article");
            JSONArray orderedList = article.getJSONArray("orderedlist");

            HashMap<String, List<String>> map = new HashMap<>();
            HashMap<Integer, String> listMap = new HashMap<>();

            int index=0;
            String s = null,title = null;
            for(int i=0;i<8;i++){
                int x = ((2*i) + 1);
                JSONObject o = orderedList.getJSONObject(x);
                Object o2 = orderedList.getJSONObject(2*i).getJSONObject("listitem").opt("para");
                //Log.e("TAGGEROG",o2.toString());
                JSONArray arr = o.getJSONArray("listitem");
                title = o2.toString();
                titleList.add(title);
                Log.e("TEGG",title);
                for(int j=0;j<arr.length();j++){
                    //index=j;
                    Object pra = arr.getJSONObject(j).opt("para");
                    s = pra.toString();

                    //System.out.println(j+" "+s+"\n");
                    //Log.e("TEGG"+j,s);
                    //map.put(titleList.get(i),op2List.subList(0,arr.length()));
                    op2List.add(s);
                }
                //System.out.println("\n\n");
            }
            /*for(int k=0;k<8;k++){
                List<String> mine = null;
                if(k==0){
                    mine = op2List.subList(0,5);
                }
                if(k==0){
                    mine = op2List.subList(5,10);
                }
                if(k==1){
                    mine = op2List.subList(10,15);
                }
                if(k==2){
                    mine = op2List.subList(20,25);
                }
                if(k==3){
                    mine = op2List.subList(0,5);
                }
                if(k==4){
                    mine = op2List.subList(0,5);
                }
                if(k==5){
                    mine = op2List.subList(0,5);
                }
                map.put(titleList.get(k),mine);
            }*/
           // Log.e("MAP",map.toString());

            for(String x: op2List){
                //listMap.put(index++,x);
                Log.e("TAGGER"+index++,x);
            }
            for(int x: listMap.keySet()){
                Log.e("TAGGER"+x,listMap.get(x));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        categoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryList.setAdapter(new CategoryAdapter(titleList,mListener));
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

}
class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    ArrayList<String> categories;
    Listerners.PrayerListener mListener;

    public CategoryAdapter(ArrayList<String> categories, Listerners.PrayerListener mListener){
        this.categories = categories;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.category_card)
        CardView categoryCard;

        @BindView(R.id.category_text)
        TextView categoryText;

        int pos;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            categoryCard.setOnClickListener(this);
        }

        void bind(int position){
            categoryText.setText(categories.get(position));
            pos = position;
        }

        @Override
        public void onClick(View v) {
            mListener.onCategoryClick(categories.get(pos));
        }

    }

}