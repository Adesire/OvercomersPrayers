package com.overcomersprayers.app.overcomersprayers.adapters;

import android.graphics.BlurMaskFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.overcomersprayers.app.overcomersprayers.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerPageAdapter extends RecyclerView.Adapter<PrayerPageAdapter.PrayerPageViewHolder> {
    List<String> prayerPoints;
    private boolean a;

    public PrayerPageAdapter(Bundle b) {
        this.a = b.getBoolean("IS_LOCKED");
        this.prayerPoints = new ArrayList<>();
    }

    @NonNull
    @Override
    public PrayerPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_points, parent, false);
        return new PrayerPageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerPageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return prayerPoints.size();
    }

    public void swapData(List<String> prayerPoints) {
        this.prayerPoints = prayerPoints;
        this.notifyDataSetChanged();
    }

    class PrayerPageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ppoint)
        TextView prayerPointTextview;
        @BindView(R.id.serial_number)
        TextView serialNumber;

        public PrayerPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            prayerPointTextview.setText(prayerPoints.get(position));
            serialNumber.setText(String.valueOf(position + 1));
            if(a){
                if (position > 2) {
                    prayerPointTextview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    float radius = prayerPointTextview.getTextSize() / 3;
                    BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
                    prayerPointTextview.getPaint().setMaskFilter(filter);
                } else {
                    prayerPointTextview.getPaint().setMaskFilter(null);
                }
            }else{
                prayerPointTextview.getPaint().setMaskFilter(null);
            }
        }

        void setBlurryText(int position){

        }
    }
}
