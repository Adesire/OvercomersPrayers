package com.overcomersprayer.app.overcomersprayers.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading.PrayerHeadings;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExpandableCategoriesAdapter extends ExpandableRecyclerViewAdapter<ExpandableCategoriesAdapter.CategoriesGroupViewHolder, ExpandableCategoriesAdapter.HeadingsChildViewHolder> {

    public ExpandableCategoriesAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public CategoriesGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_points, parent, false);
        return new CategoriesGroupViewHolder(view);
    }

    @Override
    public HeadingsChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_points, parent, false);
        return new HeadingsChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(HeadingsChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final PrayerHeadings prayerHeadings = (PrayerHeadings) group.getItems().get(childIndex);
        holder.bind(prayerHeadings);
    }

    @Override
    public void onBindGroupViewHolder(CategoriesGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.bind(group.getTitle());
    }

    class CategoriesGroupViewHolder extends GroupViewHolder {
        @BindView(R.id.ppoint)
        TextView textView;

        public CategoriesGroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String s) {
            textView.setText(s);
        }
    }

    class HeadingsChildViewHolder extends ChildViewHolder {
        @BindView(R.id.ppoint)
        TextView textView;

        public HeadingsChildViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            textView.setTextColor(Color.RED);
            textView.setTextSize(13f);
        }

        void bind(ListOfCategoriesWithHeading.PrayerHeadings prayerHeadings) {
            textView.setText(prayerHeadings.getHeading());
        }
    }
}
