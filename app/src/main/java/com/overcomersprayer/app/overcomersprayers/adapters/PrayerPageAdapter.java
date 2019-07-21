package com.overcomersprayer.app.overcomersprayers.adapters;

import android.graphics.BlurMaskFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerPageAdapter extends RecyclerView.Adapter<PrayerPageAdapter.PrayerPageViewHolder> {
    List<String> prayerPoints;
    private boolean a;
    Listerners.TTSRequest ttsRequest;

    public PrayerPageAdapter(Bundle b, Listerners.TTSRequest ttsRequest) {
        this.a = b.getBoolean("IS_LOCKED");
        this.prayerPoints = new ArrayList<>();
        this.ttsRequest = ttsRequest;
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
        @BindView(R.id.speaker)
        ImageView speaker;

        public PrayerPageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            prayerPointTextview.setText(prayerPoints.get(position));
            serialNumber.setText(String.valueOf(position + 1));
            if (a) {
                if (position > 4) {
                    prayerPointTextview.setOnClickListener(null);
                    prayerPointTextview.setOnLongClickListener(null);
                    speaker.setVisibility(View.GONE);
                    prayerPointTextview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    float radius = prayerPointTextview.getTextSize() / 3;
                    BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
                    prayerPointTextview.getPaint().setMaskFilter(filter);
                } else {
                    prayerPointTextview.getPaint().setMaskFilter(null);
                    speaker.setVisibility(View.VISIBLE);
                    prayerPointTextview.setOnClickListener(v -> ttsRequest.onSmallClick(prayerPoints.get(getAdapterPosition())));
                }
            } else {
                speaker.setVisibility(View.VISIBLE);
                prayerPointTextview.getPaint().setMaskFilter(null);
                prayerPointTextview.setOnClickListener(v -> ttsRequest.onSmallClick(prayerPoints.get(getAdapterPosition())));
                prayerPointTextview.setOnLongClickListener(v -> {
                    ttsRequest.onTTSRequested(((TextView) v).getText().toString());
                    return false;
                });
            }
        }

        void setBlurryText(int position) {

        }
    }
}
