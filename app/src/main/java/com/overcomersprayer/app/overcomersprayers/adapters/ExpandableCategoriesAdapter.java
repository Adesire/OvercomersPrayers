package com.overcomersprayer.app.overcomersprayers.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading.PrayerHeadings;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class ExpandableCategoriesAdapter extends ExpandableRecyclerViewAdapter<ExpandableCategoriesAdapter.CategoriesGroupViewHolder, ExpandableCategoriesAdapter.HeadingsChildViewHolder> {

    public Listerners.PrayerListener prayerListener;

    public ExpandableCategoriesAdapter(List<? extends ExpandableGroup> groups, Listerners.PrayerListener prayerListener) {
        super(groups);
        this.prayerListener = prayerListener;
    }

    @Override
    public CategoriesGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return CategoriesGroupViewHolder.from(parent);
    }

    @Override
    public HeadingsChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        return HeadingsChildViewHolder.from(parent);
    }

    @Override
    public void onBindChildViewHolder(HeadingsChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final PrayerHeadings prayerHeadings = (PrayerHeadings) group.getItems().get(childIndex);
        holder.bind(prayerHeadings, prayerListener);
    }

    @Override
    public void onBindGroupViewHolder(CategoriesGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.bind(group.getTitle());
    }

    static class CategoriesGroupViewHolder extends GroupViewHolder {

        @BindView(R.id.category_text)
        TextView textView;
        @BindView(R.id.arrow)
        ImageView arrow;

        private CategoriesGroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String s) {
            textView.setText(s);
        }

        public static CategoriesGroupViewHolder from(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);
            return new CategoriesGroupViewHolder(view);
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }
    }

    static class HeadingsChildViewHolder extends ChildViewHolder {
        @BindView(R.id.prayer_heading)
        TextView textView;

        private HeadingsChildViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(ListOfCategoriesWithHeading.PrayerHeadings prayerHeadings, Listerners.PrayerListener prayerListener) {
            textView.setText(prayerHeadings.getHeading());
            Prayer prayer = new Prayer(prayerHeadings.getHeading());
            itemView.setOnClickListener(v -> prayerListener.onCategoryItemClicked(prayer));
        }

        public static HeadingsChildViewHolder from(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_item, parent, false);
            return new HeadingsChildViewHolder(view);
        }
    }

}
