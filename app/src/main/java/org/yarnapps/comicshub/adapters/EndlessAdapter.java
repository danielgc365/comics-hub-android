package org.yarnapps.comicshub.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.lists.PagedList;

public abstract class EndlessAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROGRESS = 0;

    private final PagedList<T> mDataset;
    private final int mVisibleThreshold = 2;
    private int mLastVisibleItem, mTotalItemCount;
    private boolean mLoading;
    private OnLoadMoreListener mOnLoadMoreListener;
    private GridLayoutManager mLayoutManager;

    public EndlessAdapter(PagedList<T> dataset, RecyclerView recyclerView) {
        mDataset = dataset;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (!mLoading && isLoadEnabled() && mTotalItemCount <= (mLastVisibleItem + mVisibleThreshold)) {
                    // End has been reached
                    // Do something
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                        mLoading = true;
                    }
                }
            }
        });
        mLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        onLayoutManagerChanged(mLayoutManager);
    }

    public void onLayoutManagerChanged(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public long getItemId(int position) {
        if (position == mDataset.size()) {
            return 0;
        } else {
            return getItemId(mDataset.get(position));
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return onCreateHolder(parent);
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_loading, parent, false));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        T item = getItem(position);
        if (item != null) {
            onBindHolder((VH) holder, item, position);
        } else if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).setVisible(isLoadEnabled());
        }
    }

    public void setLoaded() {
        mLoading = false;
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    @Nullable
    public T getItem(int position) {
        if (position == mDataset.size()) {
            return null;
        } else {
            return mDataset.get(position);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    private boolean isLoadEnabled() {
        return mDataset.size() != 0 && mDataset.isHasNext();
    }

    public abstract VH onCreateHolder(ViewGroup parent);

    public abstract void onBindHolder(VH viewHolder, T data, int position);

    public abstract long getItemId(T data);

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar mProgressBar;

        ProgressViewHolder(View v) {
            super(v);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }

        public void setVisible(boolean visible) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}