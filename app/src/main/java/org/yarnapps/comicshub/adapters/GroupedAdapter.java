package org.yarnapps.comicshub.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.items.MangaInfo;
import org.yarnapps.comicshub.items.ThumbSize;
import org.yarnapps.comicshub.providers.staff.ProviderSummary;

import java.util.ArrayList;

public class GroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_HEADER = 0;

    private final ArrayList<Object> mDataset;
    private boolean mGrid;
    private ThumbSize mThumbSize;
    private final OnMoreClickListener mOnMoreClickListener;

    public GroupedAdapter(OnMoreClickListener moreClickListener) {
        mDataset = new ArrayList<>();
        mOnMoreClickListener = moreClickListener;
        mGrid = false;
    }

    public boolean setGrid(boolean grid) {
        if (mGrid != grid) {
            mGrid = grid;
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public void setThumbnailsSize(@NonNull ThumbSize size) {
        if (!size.equals(mThumbSize)) {
            mThumbSize = size;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public void append(ProviderSummary group, ArrayList<MangaInfo> data) {
        int last = mDataset.size();
        mDataset.add(group);
        mDataset.addAll(data);
        notifyItemRangeInserted(last, data.size() + 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return viewType == VIEW_HEADER ?
                new GroupViewHolder(inflater.inflate(R.layout.header_group, parent, false), mOnMoreClickListener) :
                new MangaListAdapter.MangaViewHolder(inflater
                        .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaListAdapter.MangaViewHolder) {
            ((MangaListAdapter.MangaViewHolder) holder).fill(getItem(position), mThumbSize, false);
        } else {
            ((GroupViewHolder) holder).fill((ProviderSummary) mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Nullable
    public ProviderSummary getGroup(int position) {
        for (int i = position; i > 0; i--) {
            if (mDataset.get(i) instanceof ProviderSummary) {
                return (ProviderSummary) mDataset.get(i);
            }
        }
        return null;
    }

    @Nullable
    public MangaInfo getItem(int position) {
        Object object = mDataset.get(position);
        return object instanceof MangaInfo ? (MangaInfo) object : null;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position) instanceof MangaInfo ? VIEW_ITEM : VIEW_HEADER;
    }

    public void onLayoutManagerChanged(boolean grid) {
        if (grid != mGrid) {
            mGrid = grid;
        }
        notifyDataSetChanged();
    }

    public interface OnMoreClickListener {
        void onMoreClick(String title, ProviderSummary provider);
    }

    protected static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final OnMoreClickListener mMoreClickListener;
        private ProviderSummary mData;

        public GroupViewHolder(View itemView, OnMoreClickListener moreClickListener) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            mMoreClickListener = moreClickListener;
        }

        public void fill(ProviderSummary data) {
            mData = data;
            mTextView.setText(data.name);
        }

        @Override
        public void onClick(View v) {
            mMoreClickListener.onMoreClick(mData.name, mData);
        }
    }
}
